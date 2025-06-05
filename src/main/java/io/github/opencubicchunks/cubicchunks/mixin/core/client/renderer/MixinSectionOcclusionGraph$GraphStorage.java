package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.renderer.SectionOcclusionGraph$GraphStorage")
public class MixinSectionOcclusionGraph$GraphStorage {
    @WrapOperation(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ViewArea;sectionGridSizeY:I"))
    private int cc_onInit_constructOctree_getMinY(ViewArea instance, Operation<Integer> original) {
        if (((CanBeCubic) instance.getLevelHeightAccessor()).cc_isCubic()) {
            // Passing MAX_VALUE for sectionGridSizeY when constructing the Octree ensures that the Y axis is not special-cased and behaves the same as the X and Z axes.
            return Integer.MAX_VALUE;
        }
        return original.call(instance);
    }
}
