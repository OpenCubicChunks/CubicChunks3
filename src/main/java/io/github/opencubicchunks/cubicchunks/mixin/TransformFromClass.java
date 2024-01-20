package io.github.opencubicchunks.cubicchunks.mixin;

public @interface TransformFromClass {
    CopyFrom value();

    TransformFrom.ApplicationStage stage() default TransformFrom.ApplicationStage.PRE_APPLY;
}
