package dev.sevenclient.config;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.ModuleManager;
import dev.sevenclient.module.setting.Setting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
   private final ModuleManager modules;
   private final Path file;

   public ConfigManager(ModuleManager modules) {
      this.modules = modules;
      this.file = FabricLoader.getInstance().getConfigDir().resolve("777client").resolve("config.txt");
   }

   public Path path() {
      return this.file;
   }

   public void save() {
      List<String> lines = new ArrayList();
      lines.add("# 777 Client config");

      for(Module m : this.modules.all()) {
         String var10001 = m.name();
         lines.add(var10001 + ".enabled=" + m.isEnabled());

         for(Setting<?> s : m.settings()) {
            var10001 = m.name();
            lines.add(var10001 + "." + s.name() + "=" + s.serialize());
         }
      }

      Path temp = null;
      try {
         Files.createDirectories(this.file.getParent());
         temp = Files.createTempFile(this.file.getParent(), "config-", ".tmp");
         Files.writeString(temp, String.join(System.lineSeparator(), lines), StandardCharsets.UTF_8);
         try {
            Files.move(temp, this.file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (AtomicMoveNotSupportedException unsupported) {
            // Never replace a valid config with a non-atomic move.
            SevenClient.LOG.error("Atomic config save is not supported", unsupported);
         }
      } catch (IOException error) {
         SevenClient.LOG.error("Failed to save config", error);
      } finally {
         if (temp != null) {
            try {
               Files.deleteIfExists(temp);
            } catch (IOException error) {
               SevenClient.LOG.error("Failed to remove temporary config", error);
            }
         }
      }
   }

   public void load() {
      if (!Files.exists(this.file, new LinkOption[0])) {
         this.save();
      } else {
         try {
            // Retain the last enabled value for each module, but apply it only after
            // all settings have been restored, regardless of config line order.
            Map<Module, Boolean> enabled = new LinkedHashMap();
            for(String line : Files.readAllLines(this.file, StandardCharsets.UTF_8)) {
               if (!line.isBlank() && !line.startsWith("#")) {
                  int eq = line.indexOf(61);
                  int dot = line.indexOf(46);
                  if (eq >= 0 && dot >= 0 && dot <= eq) {
                     String moduleName = line.substring(0, dot);
                     String key = line.substring(dot + 1, eq);
                     String value = line.substring(eq + 1);
                     Module m = this.modules.byName(moduleName);
                     if (m != null) {
                        if (key.equals("enabled")) {
                           enabled.put(m, Boolean.parseBoolean(value));
                        } else {
                           for(Setting<?> s : m.settings()) {
                              if (s.name().equals(key)) {
                                 s.deserialize(value);
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
            for(Map.Entry<Module, Boolean> entry : enabled.entrySet()) {
               entry.getKey().setEnabled(entry.getValue());
            }
         } catch (IOException error) {
            SevenClient.LOG.error("Failed to load config", error);
         }
      }

   }
}
