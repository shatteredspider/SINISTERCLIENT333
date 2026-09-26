# 777 / SINISTERCLIENT333: source review report

**Scope:** The decompiled source already uploaded to this private repo. The audit is static. No original file was modified, no binary was built, and no Minecraft runtime or server test was performed. Claims below distinguish **verified source behavior** from consequences that still need runtime confirmation. Links point to the exact methods/files to inspect; avoid treating an untested outcome as a measured incident. See [GUIDE.md](GUIDE.md) for the module map and rotation formulas.

## Priorities: correctness and state handling

### High: failed module initialization can still report Enabled

[Module.setEnabled](dev/sevenclient/module/Module.java) assigns `enabled = state` **before** calling `onEnable()` or `onDisable()`. Its `catch (Throwable)` logs but does not roll back that field, then still sends the normal toggle notification. Unlike [ModuleManager's callback error handler](dev/sevenclient/module/ModuleManager.java), this path does not immediately disable a module whose initialization failed. A module with a partially completed `onEnable()` can therefore appear enabled until another callback fails or it is toggled again. **Verified control flow; actual incidence needs a forced-failure test.** Review initialization and state-change rollback separately from the per-tick crash handler.

### High: config enables modules before it applies their saved settings

[ConfigManager.save/load](dev/sevenclient/config/ConfigManager.java) writes each `Module.enabled` line before that module's settings, then loads lines in the same order by calling `setEnabled(...)` as soon as it encounters `enabled`. [SevenClient.onInitializeClient](dev/sevenclient/SevenClient.java) loads config at startup before a world/player exists. A module's `onEnable` sees its defaults, not its persisted settings; any player/world-dependent enable code may also fail at that time. In combination with the previous finding, such a failure may leave a misleading Enabled state. **Ordering is verified; impact varies by module.** This is the top lifecycle test case.

### High: JumpReset can fire after the conditions that armed it stop holding

[JumpReset.onServerKnockback/onTick/fire](dev/sevenclient/module/impl/JumpReset.java) checks `suitable(...)` and `conditionsOk()` when a velocity hook arms the jump. The delayed branch counts down and calls `fire()` without rechecking ground, water, sprint, facing, or whether the player is holding jump. A target state can change during the delay and still produce a jump. [EntityVelocityMixin](dev/sevenclient/mixin/EntityVelocityMixin.java) hooks a generic local-velocity setter rather than confirming a PvP damage cause, so other matching velocity changes can arm it. **Verified source path; reproduce under changing conditions before assigning a frequency.**

### High: AimAssist2 may retain a target that no longer satisfies filters

[AimAssist2.acquire/stillValid](dev/sevenclient/module/impl/AimAssist2.java) applies `Enemies Only` and `Players Only` on acquisition. `stillValid` checks only alive, not removed, and distance. Changing those filters while a target remains locked does not itself invalidate that target; the default `Require Mouse Down` setting also disables the sticky-timeout reacquire branch. **Verified** for filter changes during an active target. Weapon and block-break gates are checked elsewhere each frame, so do not generalize this finding to every setting.

### High: TargetESP drops the whole overlay at the near plane

[TargetESP.project/drawEntity](dev/sevenclient/module/impl/TargetESP.java) returns `null` for a corner with camera-space depth below `0.05`. `drawEntity` returns from the whole method on any such corner while projecting the eight AABB corners. That skips the box, health bar, name, and tracer for that entity in that frame. This makes a point-blank or partially behind-camera target a likely visual failure case. **Verified branch; visual frequency not measured.**

## Medium: rotation and timing semantics

### Camera/Glide "hidden" rotation is server-visible rotation

[RotationSync.applyHidden](dev/sevenclient/util/RotationSync.java) sets the actual player yaw/pitch and head yaw; [CameraMixin](dev/sevenclient/mixin/CameraMixin.java) subtracts the tracked offset only while updating the local camera. [SilentAim](dev/sevenclient/module/impl/SilentAim.java) Camera and Glide modes use this path and release any Packet-mode request. The server-facing player rotation is altered. If a user expects these modes to preserve the server-visible rotation, the label is misleading. Packet mode instead applies its requested rotation temporarily around [movement packet send](dev/sevenclient/mixin/ClientPlayerEntityMixin.java) and restores it. **Verified source distinction, not an assertion about successful hits.** Also, SilentAim's `raycastHits` checks a target AABB, not intervening blocks; geometric intersection is not proof of line of sight.

### HitFlick and Packet-mode SilentAim do not share one arbitration path

[HitFlick](dev/sevenclient/module/impl/HitFlick.java) updates real yaw through [MouseRotation](dev/sevenclient/util/MouseRotation.java) without calling [RotationSync.requestSilent](dev/sevenclient/util/RotationSync.java). Packet-mode SilentAim requests a different yaw which is used at movement-packet time. If both are active in one tick, the later packet-boundary rotation can override the flick's outgoing angle while leaving the view movement. **Verified routing; actual visual/packet conflict depends on callback timing.** The `PRIORITY_HIT_FLICK` constant alone does not mean HitFlick participates in arbitration. The full repository was not exhaustively searched for every possible caller.

### PingSpoof disable does not cancel scheduled keep-alives

[PingSpoofer.intercept](dev/sevenclient/util/PingSpoofer.java) cancels the immediate keep-alive send and schedules it on a single-thread background executor. [PingSpoof.onDisable](dev/sevenclient/module/impl/PingSpoof.java) changes active flags, but already-scheduled tasks do not recheck those flags and still run after disabling, with a configured delay up to about 920 ms. **Verified.** This may be deliberate: dropping an already-intercepted keep-alive can cause connection problems. Document the residual sends instead of assuming all pending packets should be discarded. A queued task captures its original connection; send failures are swallowed, so a disconnect during the delay is not diagnosable from this path.

### Clearing PingSpoof's modifier consumes ordinary scroll

[PingSpoof constructor/handleScroll](dev/sevenclient/module/impl/PingSpoof.java) binds `Scroll Modifier` to GLFW **342 (Left Alt)** by default. Its check is `isBound() && !down()`: **if the user clears the bind**, this condition no longer gates adjustment. With PingSpoof and Scroll Adjust enabled and no GUI open, any vertical scroll adjusts `Ping MS` and [MouseScrollMixin](dev/sevenclient/mixin/MouseScrollMixin.java) cancels the normal scroll, including hotbar selection. **Verified under that configuration; this is not the shipped default.**

### Saved config is not an atomic, escaped format

[ConfigManager](dev/sevenclient/config/ConfigManager.java) writes the whole plaintext file directly and parses one `module.setting=value` per line. A string value containing a newline breaks the line-oriented format; a process exit or write failure partway through can leave a partial file. Unknown/malformed lines are skipped without an error. The settings parsers also silently ignore some invalid values. `ClickGuiScreen` **does save when it closes** ([screen-close callback](dev/sevenclient/ui/ClickGuiScreen.java)); a [shutdown hook](dev/sevenclient/SevenClient.java) supplies another save path, but its outer catch is empty. There is no basis to claim that saving happens *only* on shutdown. **Verified code paths; failure modes require fault injection to observe.**

### Hooks can fail without a required-injection error

[sevenclient.mixins.json](sevenclient.mixins.json) sets `injectors.defaultRequire` to `0`; individual mixins such as [EntityVelocityMixin](dev/sevenclient/mixin/EntityVelocityMixin.java), [MouseMixin](dev/sevenclient/mixin/MouseMixin.java), [ClientConnectionMixin](dev/sevenclient/mixin/ClientConnectionMixin.java), and [MouseScrollMixin](dev/sevenclient/mixin/MouseScrollMixin.java) also use `require = 0`. A target/signature change may leave those features inactive without a failed required injection. **Verified configuration; no mapping drift was reproduced.** Prefer measuring the existing debug counters to assuming every mixin is live.

## Lower-priority inconsistencies and maintenance risks

- [Rotations.gcd](dev/sevenclient/util/Rotations.java) and [MouseRotation.degreesPerCount](dev/sevenclient/util/MouseRotation.java) compute the same cubic sensitivity step separately. The former has no local fallback for an unavailable options accessor; the latter does. They also differ in intermediate precision. This is a maintenance and diagnostic risk, not a proven bad angle at normal settings.
- [HitSwap](dev/sevenclient/module/impl/HitSwap.java) changes a slot before invoking slot synchronization. On a sync failure it returns without scheduling the attack/restore deadline; its following tick's restore path can correct the slot, leaving a temporary wrong-item state. The failure and one-tick consequence are source-grounded; a real accessor failure was not reproduced.
- [Triggerbot](dev/sevenclient/module/impl/Triggerbot.java) derives some reaction delays from random distributions without clamping the final sampled result to every displayed minimum. A minimum slider is therefore not necessarily a hard lower bound. The deliberate whiff path may also pulse attack with no crosshair entity; treat this as intended behavior unless the module's product requirement says otherwise.
- [Chams](dev/sevenclient/module/impl/Chams.java) caches animated `Ghost` boxes by integer entity id and clears them on disable or after 3 seconds without a touch, not on world changes. If another world reuses an id before expiry, a new box can animate from the prior entity's position. This is a visual artifact scenario, not evidence of a memory leak; routine in-world entity-id reuse is not assumed.
- [Debug](dev/sevenclient/module/impl/Debug.java) assigns the global `Module.notifyToggles` only during its enabled ticks. If Debug is disabled, that global retains the last value. This ties notification behavior to a diagnostics module's last live setting.
- The root is an extracted JAR layout, with no build scripts or tests. The [manifest](META-INF/MANIFEST.MF) names Minecraft 1.21.11, while [fabric.mod.json](fabric.mod.json) declares the broader `~1.21` dependency. Without a reproducible build/test setup, compatibility beyond the observed build and source correctness cannot be certified. Repo name `SINISTERCLIENT333` differs from the source's `777`/`sevenclient` identity.

## Benchmark interpretation

[RSSeeker/Vape-v4](https://github.com/RSSeeker/Vape-v4) is useful for context only. Its [English README](https://github.com/RSSeeker/Vape-v4/blob/main/README_EN.md) says it is an **independent recovery project, not official Vape source**. It describes a Windows x64 loader/native bridge and a multi-version runtime, whereas this repository is a Fabric mod snapshot built for 1.21.11. No controlled test here compared accuracy, input behavior, latency, visual quality, or detection between them. Do not claim parity or infer code quality from the name of the reference project.

## Review limits and verification order

1. Verify config load order and module enable failure by enabling a module with nondefault saved settings, then forcing `onEnable` to throw in a test setup.
2. Test JumpReset across a state change during its delayed tick; test AimAssist2 filter changes while locked; test TargetESP close to the camera plane.
3. Check Camera/Glide yaw in outbound movement packets versus the rendered camera and compare with Packet mode; test concurrent HitFlick use with explicit packet logging.
4. Check PingSpoof with Left Alt held, then with the modifier deliberately unbound; test disconnect during a delayed keep-alive. Preserve already-queued packets while testing.
5. Build and run only after obtaining or creating a **separate** reproducible build project with matching mappings and dependencies; this report makes no claim that the checked-in extracted tree compiles on its own.

**Coverage limit:** Large responses for parts of [AimAssist.java](dev/sevenclient/module/impl/AimAssist.java) and the second half of [AdaptiveAim.java](dev/sevenclient/util/AdaptiveAim.java) were truncated by the available repository content interface. Their complete activation and final adaptive output equations were **not** audited. Other modules were sampled by area rather than executed. Conclusions about anti-cheat detection or production reliability would need instrumented runtime tests and source coverage that this review does not have.
