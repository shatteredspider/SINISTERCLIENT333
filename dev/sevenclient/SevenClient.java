package dev.sevenclient;

import dev.sevenclient.config.ConfigManager;
import dev.sevenclient.module.ModuleManager;
import dev.sevenclient.ui.ClickGuiScreen;
import dev.sevenclient.util.EnemyManager;
import dev.sevenclient.util.FrameDispatcher;
import dev.sevenclient.util.FriendManager;
import dev.sevenclient.util.TargetPriority;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SevenClient implements ClientModInitializer {
   public static final String NAME = "777";
   public static final String TAG = "§8[§f777§8] §r";
   public static final Logger LOG = LoggerFactory.getLogger("777Client");
   private static SevenClient instance;
   public ModuleManager modules;
   public EnemyManager enemies;
   public FriendManager friends;
   public ConfigManager config;

   public static SevenClient get() {
      return instance;
   }

   public void onInitializeClient() {
      instance = this;
      this.enemies = new EnemyManager();
      this.friends = new FriendManager();
      this.modules = new ModuleManager();
      this.config = new ConfigManager(this.modules);
      this.config.load();
      ClientTickEvents.START_CLIENT_TICK.register((ClientTickEvents.StartTick)(mc) -> {
         TargetPriority.onWorldTick();
         if (mc.field_1724 != null && mc.field_1687 != null) {
            this.modules.pollBinds();
            this.modules.onTick();
         }

      });
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(mc) -> {
         if (mc.field_1724 != null && mc.field_1687 != null) {
            this.modules.onPostTick();
         }

      });
      HudRenderCallback.EVENT.register((HudRenderCallback)(ctx, tickCounter) -> {
         FrameDispatcher.fromRenderFallback();
         this.modules.onHudRender(ctx);
      });
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         try {
            this.config.save();
         } catch (Throwable var2) {
         }

      }));
      LOG.info("{} initialised.", "777");
   }

   public static void openClickGui() {
      class_310.method_1551().method_1507(new ClickGuiScreen());
   }

   public static void chat(String msg) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         mc.field_1724.method_7353(class_2561.method_43470("§8[§f777§8] §r" + msg), false);
      }

   }
}
