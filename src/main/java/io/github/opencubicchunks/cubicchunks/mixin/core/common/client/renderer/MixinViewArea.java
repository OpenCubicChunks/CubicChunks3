package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ViewArea.class)
public abstract class MixinViewArea {
    @Shadow @Final protected Level level;
    @Shadow protected int sectionGridSizeY;
    @Shadow protected int sectionGridSizeX;
    @Shadow protected int sectionGridSizeZ;
    @Shadow private int viewDistance;
    @Shadow public SectionRenderDispatcher.RenderSection[] sections;
    @Shadow private SectionPos cameraSectionPos;
    @Shadow @Final protected LevelRenderer levelRenderer;

    @Shadow protected abstract int getSectionIndex(int x, int y, int z);

    // FIXME inject properly instead of overwriting the method
    @Inject(method = "setViewDistance", at = @At("HEAD"), cancellable = true)
    private void cc_onSetViewDistance(int renderDistanceChunks, CallbackInfo ci) {
        if (!((CanBeCubic) level).cc_isCubic()) return;
        ci.cancel();
        int i = renderDistanceChunks * 2 + 1;
        i *= CubicConstants.DIAMETER_IN_SECTIONS; // TODO is this correct?
        this.sectionGridSizeX = i;
        this.sectionGridSizeY = i;
        this.sectionGridSizeZ = i;
        this.viewDistance = renderDistanceChunks;
    }

    @WrapOperation(method = "createSections", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinSectionY()I"))
    private int cc_onCreateSections_getMinY(Level instance, Operation<Integer> original) {
        if (!((CanBeCubic) level).cc_isCubic()) return original.call(instance);
        return 0; // I don't really understand the logic here, but returning 0 makes the Y axis behave equivalently to X and Z, which *should* be what we want
    }

    // TODO can we do this without fully overwriting the method?
    @Inject(method = "getRenderSection(III)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;", at = @At("HEAD"), cancellable = true)
    private void cc_onGetRenderSection(int sectionX, int sectionY, int sectionZ, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (!((CanBeCubic) level).cc_isCubic()) return;
        if (!this.cc_containsSection(sectionX, sectionY, sectionZ)) {
            cir.setReturnValue(null);
            return;
        }
        int x = Math.floorMod(sectionX, this.sectionGridSizeX);
        int y = Math.floorMod(sectionY, this.sectionGridSizeY);
        int z = Math.floorMod(sectionZ, this.sectionGridSizeZ);
        cir.setReturnValue(this.sections[this.getSectionIndex(x, y, z)]);
    }

    // TODO can we do this with dasm + mixin? probably too messy
    // I don't really understand the coordinate maths here, but we replicate it for the Y axis
    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void cc_onRepositionCamera(SectionPos newSectionPos, CallbackInfo ci) {
        if (!((CanBeCubic) level).cc_isCubic()) return;
        ci.cancel();
        for (int sectionX = 0; sectionX < this.sectionGridSizeX; sectionX++) {
            int i1 = newSectionPos.x() - this.viewDistance;
            int originX = i1 + Math.floorMod(sectionX - i1, this.sectionGridSizeX);

            for (int sectionZ = 0; sectionZ < this.sectionGridSizeZ; sectionZ++) {
                int i2 = newSectionPos.z() - this.viewDistance;
                int originZ = i2 + Math.floorMod(sectionZ - i2, this.sectionGridSizeZ);

                for (int sectionY = 0; sectionY < this.sectionGridSizeY; sectionY++) {
                    int i3 = newSectionPos.y() - this.viewDistance;
                    int originY = i3 + Math.floorMod(sectionY - i3, this.sectionGridSizeY);
                    SectionRenderDispatcher.RenderSection renderSection = this.sections[this.getSectionIndex(sectionX, sectionY, sectionZ)];
                    long oldSectionNode = renderSection.getSectionNode();
                    if (oldSectionNode != SectionPos.asLong(originX, originY, originZ)) {
                        renderSection.setSectionNode(SectionPos.asLong(originX, originY, originZ));
                    }
                }
            }
        }

        this.cameraSectionPos = newSectionPos;
        this.levelRenderer.getSectionOcclusionGraph().invalidate();
    }

    private boolean cc_containsSection(int x, int y, int z) {
        return x >= this.cameraSectionPos.x() - this.viewDistance && x <= this.cameraSectionPos.x() + this.viewDistance
            && y >= this.cameraSectionPos.y() - this.viewDistance && y <= this.cameraSectionPos.y() + this.viewDistance
            && z >= this.cameraSectionPos.z() - this.viewDistance && z <= this.cameraSectionPos.z() + this.viewDistance;
    }
}
