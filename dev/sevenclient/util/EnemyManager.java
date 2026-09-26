package dev.sevenclient.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

public final class EnemyManager {
   private final Set<String> names = Collections.synchronizedSet(new LinkedHashSet());

   public boolean toggle(class_1657 player) {
      String key = key(player);
      if (this.names.contains(key)) {
         this.names.remove(key);
         return false;
      } else {
         this.names.add(key);
         return true;
      }
   }

   public boolean is(class_1297 e) {
      boolean var10000;
      if (e instanceof class_1657 p) {
         var10000 = this.names.contains(key(p));
      } else {
         var10000 = false;
      }

      return var10000;
   }

   public void clear() {
      this.names.clear();
   }

   public int size() {
      return this.names.size();
   }

   public Set<String> names() {
      return this.names;
   }

   private static String key(class_1657 p) {
      return p.method_5477().getString().toLowerCase(Locale.ROOT);
   }
}
