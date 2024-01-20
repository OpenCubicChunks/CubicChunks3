package io.github.opencubicchunks.cubicchunks.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface TransformFrom {

    String value();

    CopyFrom copyFrom() default @CopyFrom();

    Signature signature() default @Signature(fromString = true);

    ApplicationStage stage() default ApplicationStage.PRE_APPLY;

    boolean makeSyntheticAccessor() default false;

    @interface Signature {
        Class<?>[] args() default {};
        Class<?> ret() default void.class;
        boolean fromString() default false;
    }

    enum ApplicationStage {
        PRE_APPLY,
        POST_APPLY
    }
}
