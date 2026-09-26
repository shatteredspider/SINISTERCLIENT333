package dev.sevenclient.config;

import dev.sevenclient.SevenClient;
import dev.sevenclient.module.Module;
import dev.sevenclient.module.ModuleManager;
import dev.sevenclient.module.setting.Setting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

      try {
         Files.createDirectories(this.file.getParent());
         Files.write(this.file, String.join(System.lineSeparator(), lines).getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
      } catch (IOException var6) {
         SevenClient.LOG.error("Failed to save config", var6);
      }

   }

   public void load() {
      if (!Files.exists(this.file, new LinkOption[0])) {
         this.save();
      } else {
         try {
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
                           m.setEnabled(Boolean.parseBoolean(value));
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
         } catch (IOException var11) {
            SevenClient.LOG.error("Failed to load config", var11);
         }
      }

   }
}
