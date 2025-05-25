package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.renderer.CubicViewArea;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ViewArea.class)
public abstract class MixinViewArea implements CubicViewArea {
    @Shadow @Final protected Level level;
    @Shadow protected int sectionGridSizeY;
    @Shadow protected int sectionGridSizeX;
    @Shadow protected int sectionGridSizeZ;
    @Shadow private int viewDistance;
    @Shadow public SectionRenderDispatcher.RenderSection[] sections;

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

    @WrapOperation(method = "createSections", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinBuildHeight()I"))
    private int cc_onCreateSections_getMinBuildHeight(Level instance, Operation<Integer> original) {
        if (!((CanBeCubic) level).cc_isCubic()) return original.call(instance);
        return 0; // I don't really understand the logic here, but returning 0 makes the Y axis behave equivalently to X and Z, which *should* be what we want
    }

    @WrapOperation(method = "setDirty", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinSection()I"))
    private int cc_onSetDirty_getMinSection(Level instance, Operation<Integer> original) {
        if (!((CanBeCubic) level).cc_isCubic()) return original.call(instance);
        return 0; // As above, returning 0 makes the Y axis behave equivalently to X and Z
    }

    // TODO can we do this without fully overwriting the method?
    @Inject(method = "getRenderSectionAt", at = @At("HEAD"), cancellable = true)
    private void cc_onGetRenderSectionAt(BlockPos pos, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (!((CanBeCubic) level).cc_isCubic()) return;
        int x = Mth.positiveModulo(Mth.floorDiv(pos.getX(), 16), this.sectionGridSizeX);
        int y = Mth.positiveModulo(Mth.floorDiv(pos.getY(), 16), this.sectionGridSizeY);
        int z = Mth.positiveModulo(Mth.floorDiv(pos.getZ(), 16), this.sectionGridSizeZ);
        cir.setReturnValue(this.sections[this.getSectionIndex(x, y, z)]);
    }

    // TODO can we do this with dasm + mixin? probably too messy
    // I don't really understand the coordinate maths here, but we replicate it for the Y axis
    @Override public void cc_repositionCamera(double viewEntityX, double viewEntityY, double viewEntityZ) {
        int ceilX = Mth.ceil(viewEntityX);
        int ceilY = Mth.ceil(viewEntityY);
        int ceilZ = Mth.ceil(viewEntityZ);

        for(int sectionX = 0; sectionX < this.sectionGridSizeX; ++sectionX) {
            int maxX = this.sectionGridSizeX * 16;
            int i1 = ceilX - 8 - maxX / 2;
            int originX = i1 + Math.floorMod(sectionX * 16 - i1, maxX);

            for(int sectionZ = 0; sectionZ < this.sectionGridSizeZ; ++sectionZ) {
                int maxZ = this.sectionGridSizeZ * 16;
                int i2 = ceilZ - 8 - maxZ / 2;
                int originZ = i2 + Math.floorMod(sectionZ * 16 - i2, maxZ);

                for(int sectionY = 0; sectionY < this.sectionGridSizeY; ++sectionY) {
                    int maxY = this.sectionGridSizeY * 16;
                    int i3 = ceilY - 8 - maxY / 2;
                    int originY = i3 + Math.floorMod(sectionY * 16 - i3, maxY);

                    SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.sections[this.getSectionIndex(sectionX, sectionY, sectionZ)];
                    BlockPos blockpos = sectionrenderdispatcher$rendersection.getOrigin();
                    if (originX != blockpos.getX() || originY != blockpos.getY() || originZ != blockpos.getZ()) {
                        sectionrenderdispatcher$rendersection.setOrigin(originX, originY, originZ);
                    }
                }
            }
        }
    }
}
