package dev.sevenclient.util;

import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

public final class InputUtil2 {
   private InputUtil2() {
   }

   public static boolean isDown(int code, boolean mouse) {
      if (code == -1) {
         return false;
      } else {
         long handle = class_310.method_1551().method_22683().method_4490();
         int state = mouse ? GLFW.glfwGetMouseButton(handle, code) : GLFW.glfwGetKey(handle, code);
         return state == 1;
      }
   }

   public static String keyName(int code) {
      try {
         return class_3675.method_15985(new class_11908(code, 0, 0)).method_27445().getString().toUpperCase();
      } catch (Throwable var3) {
         String n = GLFW.glfwGetKeyName(code, 0);
         return n == null ? "KEY" + code : n.toUpperCase();
      }
   }

   public static String mouseName(int button) {
      String var10000;
      switch (button) {
         case 0 -> var10000 = "MOUSE1";
         case 1 -> var10000 = "MOUSE2";
         case 2 -> var10000 = "MOUSE3";
         default -> var10000 = "MOUSE" + (button + 1);
      }

      return var10000;
   }
}
