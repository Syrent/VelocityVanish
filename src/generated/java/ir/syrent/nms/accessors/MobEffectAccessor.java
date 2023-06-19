package ir.syrent.nms.accessors;

import java.lang.Class;
import java.lang.reflect.Method;

/**
 * Class generated by NMS Mapper.
 * <p>
 * This class is a reflection accessor for net.minecraft.world.effect.MobEffect
 *
 * @since 2023-06-19 15:33:01
 */
public class MobEffectAccessor {
  /**
   * This method returns the {@link Class} object of the requested NMS class.
   * <p>
   * This method is safe to call: exception is handled and null is returned in case of failure.
   *
   * @return the resolved class object or null if the class does not exist
   */
  public static Class<?> getType() {
    return AccessorUtils.getType(MobEffectAccessor.class, mapper -> {

          /* SEARGE */
          mapper.map("SEARGE", "1.8.8", "net.minecraft.potion.Potion"); // 1.8.8 - 1.13.2
          mapper.map("SEARGE", "1.14", "net.minecraft.potion.Effect"); // 1.14 - 1.16.5
          mapper.map("SEARGE", "1.17", "net.minecraft.src.C_496_"); // 1.17 - 1.20.1

          /* SPIGOT */
          mapper.map("SPIGOT", "1.8.8", "net.minecraft.server.${V}.MobEffectList"); // 1.8.8 - 1.16.5
          mapper.map("SPIGOT", "1.17", "net.minecraft.world.effect.MobEffectList"); // 1.17 - 1.20.1

        });
  }

  /**
   * This method returns the {@link Method} object of the requested NMS method.
   * <p>
   * Requested method: byId, mapping: mojang
   * Parameters of requested method: (int)
   * <p>
   * This method is safe to call: exception is handled and null is returned in case of failure.
   *
   * @return the method object or null if either class does not exist or it does not have this field in the specific environment
   */
  public static Method getMethodById1() {
    return AccessorUtils.getMethod(MobEffectAccessor.class, "byId1", mapper -> {

          /* SEARGE */
          mapper.map("SEARGE", "1.9", "func_188412_a"); // 1.9 - 1.16.5
          mapper.map("SEARGE", "1.17", "m_19453_"); // 1.17 - 1.20.1

          /* SPIGOT */
          mapper.map("SPIGOT", "1.9", "fromId"); // 1.9 - 1.17.1
          mapper.map("SPIGOT", "1.18", "a"); // 1.18 - 1.20.1

        }, int.class);
  }
}
