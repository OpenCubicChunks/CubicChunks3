package io.github.opencubicchunks.cubicchunks.util.asm;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

/**
 * Can be placed on a native stub factory method within a mixin class, the generated method will call the constructor of the return type with the
 * given arguments
 * <h2>Example:</h2>
 * 
 * <pre>{@code
 * @FactoryFromConstructor
 * private native static Vec3i createVec3i(int x, int y, int z);
 * }</pre>
 * 
 * Will generate this method based on the stub signature.
 * 
 * <pre>{@code
 * private static Vec3i createVec3i(int x, int y, int z) {
 *     return new Vec3i(x, y, z);
 * }
 * }</pre>
 */
@Target({ METHOD })
public @interface FactoryFromConstructor {}
