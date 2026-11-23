package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique private int cc_oldCameraX = Integer.MAX_VALUE;
    @Unique private int cc_oldCameraY = Integer.MAX_VALUE;
    @Unique private int cc_oldCameraZ = Integer.MAX_VALUE;

    @Shadow protected abstract int getSectionIndex(int x, int y, int z);

    // This could be multiple more specific injects, but overwriting the method is probably cleaner
    @Inject(method = "setViewDistance", at = @At("HEAD"), cancellable = true)
    private void cc_onSetViewDistance(int renderDistanceChunks, CallbackInfo ci) {
        if (!((CanBeCubic) level).cc_isCubic()) {
            return;
        }
        ci.cancel();
        int i = renderDistanceChunks * 2 + 1;
        this.sectionGridSizeX = i;
        this.sectionGridSizeY = i;
        this.sectionGridSizeZ = i;
        this.viewDistance = renderDistanceChunks;
    }

    @WrapOperation(method = "createSections", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinSectionY()I"))
    private int cc_onCreateSections_getMinY(Level instance, Operation<Integer> original) {
        if (!((CanBeCubic) level).cc_isCubic()) {
            return original.call(instance);
        }
        return 0; // I don't really understand the logic here, but returning 0 makes the Y axis behave equivalently to X and Z, which *should* be what
        // we want
    }

    // TODO can we do this without fully overwriting the method?
    @Inject(method = "getRenderSection(III)Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection;", at = @At("HEAD"), cancellable = true)
    private void cc_onGetRenderSection(int sectionX, int sectionY, int sectionZ, CallbackInfoReturnable<SectionRenderDispatcher.RenderSection> cir) {
        if (!((CanBeCubic) level).cc_isCubic()) {
            return;
        }
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
    // 1.21.6+ port by Zonix-Official of daporkchop_'s fast-path logic that was implemented to 1.12.2
    @SuppressWarnings({ "checkstyle:CyclomaticComplexity", "checkstyle:JavaNCSS", "checkstyle:NPathComplexity" })
    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void cc_onRepositionCamera(SectionPos newSectionPos, CallbackInfo ci) {
        if (!((CanBeCubic) level).cc_isCubic()) {
            return;
        }
        ci.cancel();

        // The grid is in sections, so the camera position must be in sections too. Use the position we're given (from the camera) rather than the
        // camera entity's position, which can differ (e.g. in third person)
        int cameraX = newSectionPos.x();
        int cameraY = newSectionPos.y();
        int cameraZ = newSectionPos.z();

        int dx = this.sectionGridSizeX;
        int dy = this.sectionGridSizeY;
        int dz = this.sectionGridSizeZ;

        int minX = cameraX - this.viewDistance;
        int minY = cameraY - this.viewDistance;
        int minZ = cameraZ - this.viewDistance;

        long changeX = (long) cameraX - this.cc_oldCameraX;
        long changeY = (long) cameraY - this.cc_oldCameraY;
        long changeZ = (long) cameraZ - this.cc_oldCameraZ;
        this.cc_oldCameraX = cameraX;
        this.cc_oldCameraY = cameraY;
        this.cc_oldCameraZ = cameraZ;

        if (Math.abs(changeX) <= 1 && Math.abs(changeY) <= 1 && Math.abs(changeZ) <= 1) {
            // When the camera moves one section along an axis, only the slab of sections that wraps around to the new edge changes position: index
            // (minX - 1) when moving in the positive direction, and minX when moving in the negative direction
            if (changeX != 0) {
                int indexX = Math.floorMod(changeX > 0 ? minX - 1 : minX, dx);
                int sectionX = minX + Math.floorMod(indexX - minX, dx);

                for (int indexZ = 0; indexZ < dz; indexZ++) {
                    int idxZ = indexZ * dy * dx;
                    int sectionZ = minZ + Math.floorMod(indexZ - minZ, dz);

                    for (int indexY = 0; indexY < dy; indexY++) {
                        int idxYZ = idxZ + indexY * dx;
                        int sectionY = minY + Math.floorMod(indexY - minY, dy);

                        cc_refreshSectionNode(idxYZ + indexX, sectionX, sectionY, sectionZ);
                    }
                }
            }

            if (changeY != 0) {
                int indexY = Math.floorMod(changeY > 0 ? minY - 1 : minY, dy);
                int sectionY = minY + Math.floorMod(indexY - minY, dy);

                for (int indexZ = 0; indexZ < dz; indexZ++) {
                    int sectionZ = minZ + Math.floorMod(indexZ - minZ, dz);
                    int idxZ = indexZ * dy * dx;

                    int idxYZ = idxZ + indexY * dx;

                    for (int indexX = 0; indexX < dx; indexX++) {
                        int sectionX = minX + Math.floorMod(indexX - minX, dx);

                        cc_refreshSectionNode(idxYZ + indexX, sectionX, sectionY, sectionZ);
                    }
                }
            }

            if (changeZ != 0) {
                int indexZ = Math.floorMod(changeZ > 0 ? minZ - 1 : minZ, dz);
                int sectionZ = minZ + Math.floorMod(indexZ - minZ, dz);
                int idxZ = indexZ * dy * dx;

                for (int indexY = 0; indexY < dy; indexY++) {
                    int sectionY = minY + Math.floorMod(indexY - minY, dy);
                    int idxYZ = idxZ + indexY * dx;

                    for (int indexX = 0; indexX < dx; indexX++) {
                        int sectionX = minX + Math.floorMod(indexX - minX, dx);

                        cc_refreshSectionNode(idxYZ + indexX, sectionX, sectionY, sectionZ);
                    }
                }
            }

            // Check against a full rescan that all sections are in the correct position. This cancels out the benefit of the fast path, but only
            // runs with assertions enabled
            assert cc_allSectionsInPosition(minX, minY, minZ) : "Not all sections are in the correct position!";
        } else {
            for (int indexZ = 0; indexZ < dz; indexZ++) {
                int idxZ = indexZ * dy * dx;
                int sectionZ = minZ + Math.floorMod(indexZ - minZ, dz);

                for (int indexY = 0; indexY < dy; indexY++) {
                    int idxYZ = idxZ + indexY * dx;
                    int sectionY = minY + Math.floorMod(indexY - minY, dy);

                    for (int indexX = 0; indexX < dx; indexX++) {
                        int sectionX = minX + Math.floorMod(indexX - minX, dx);

                        cc_refreshSectionNode(idxYZ + indexX, sectionX, sectionY, sectionZ);
                    }
                }
            }
        }

        this.cameraSectionPos = newSectionPos;
        this.levelRenderer.getSectionOcclusionGraph().invalidate();
    }

    /**
     * Positions the sections at grid indices within the given ranges (min inclusive, max exclusive) for a grid whose minimum section position is
     * ({@code minX}, {@code minY}, {@code minZ})
     */
    @Unique private void cc_repositionSections(
            int minIndexX, int maxIndexX, int minIndexY, int maxIndexY, int minIndexZ, int maxIndexZ, int minX, int minY, int minZ
    ) {
        for (int indexZ = minIndexZ; indexZ < maxIndexZ; indexZ++) {
            int sectionZ = minZ + Math.floorMod(indexZ - minZ, this.sectionGridSizeZ);
            for (int indexY = minIndexY; indexY < maxIndexY; indexY++) {
                int sectionY = minY + Math.floorMod(indexY - minY, this.sectionGridSizeY);
                for (int indexX = minIndexX; indexX < maxIndexX; indexX++) {
                    int sectionX = minX + Math.floorMod(indexX - minX, this.sectionGridSizeX);
                    cc_refreshSectionNode(this.getSectionIndex(indexX, indexY, indexZ), sectionX, sectionY, sectionZ);
                }
            }
        }
    }

    /**
     * Whether every section is in the position that {@link #cc_repositionSections} would give it for a grid whose minimum section position is
     * ({@code minX}, {@code minY}, {@code minZ})
     */
    @Unique private boolean cc_allSectionsInPosition(int minX, int minY, int minZ) {
        for (int indexZ = 0; indexZ < this.sectionGridSizeZ; indexZ++) {
            int sectionZ = minZ + Math.floorMod(indexZ - minZ, this.sectionGridSizeZ);
            for (int indexY = 0; indexY < this.sectionGridSizeY; indexY++) {
                int sectionY = minY + Math.floorMod(indexY - minY, this.sectionGridSizeY);
                for (int indexX = 0; indexX < this.sectionGridSizeX; indexX++) {
                    int sectionX = minX + Math.floorMod(indexX - minX, this.sectionGridSizeX);
                    long sectionNode = this.sections[this.getSectionIndex(indexX, indexY, indexZ)].getSectionNode();
                    if (sectionNode != SectionPos.asLong(sectionX, sectionY, sectionZ)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Unique private void cc_refreshSectionNode(int sectionIndex, int sectionX, int sectionY, int sectionZ) {
        SectionRenderDispatcher.RenderSection renderSection = this.sections[sectionIndex];
        long oldSectionNode = renderSection.getSectionNode();
        if (oldSectionNode != SectionPos.asLong(sectionX, sectionY, sectionZ)) {
            renderSection.setSectionNode(SectionPos.asLong(sectionX, sectionY, sectionZ));
        }
    }

    @Unique private boolean cc_containsSection(int x, int y, int z) {
        return x >= this.cameraSectionPos.x() - this.viewDistance && x <= this.cameraSectionPos.x() + this.viewDistance
                && y >= this.cameraSectionPos.y() - this.viewDistance && y <= this.cameraSectionPos.y() + this.viewDistance
                && z >= this.cameraSectionPos.z() - this.viewDistance && z <= this.cameraSectionPos.z() + this.viewDistance;
    }
}
