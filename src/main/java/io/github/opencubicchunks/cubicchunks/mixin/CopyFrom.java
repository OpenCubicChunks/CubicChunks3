package io.github.opencubicchunks.cubicchunks.mixin;

public @interface CopyFrom {
    Class<?> clazz() default Object.class;

    String string() default "";
}
