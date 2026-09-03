package io.github.opencubicchunks.cubicchunks.mixin.core.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
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

    @Unique
    private int cc_oldCameraX = Integer.MAX_VALUE;
    @Unique
    private int cc_oldCameraY = Integer.MAX_VALUE;
    @Unique
    private int cc_oldCameraZ = Integer.MAX_VALUE;

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

        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        int cameraX = Coords.blockToCube(cameraEntity.getX());
        int cameraY = Coords.blockToCube(cameraEntity.getY());
        int cameraZ = Coords.blockToCube(cameraEntity.getZ());

        int dx = this.sectionGridSizeX;
        int dy = this.sectionGridSizeY;
        int dz = this.sectionGridSizeZ;

        int px = newSectionPos.x() - this.viewDistance;
        int py = newSectionPos.y() - this.viewDistance;
        int pz = newSectionPos.z() - this.viewDistance;

        int minX = cameraX - (dx >> 1);
        int minY = cameraY - (dy >> 1);
        int minZ = cameraZ - (dz >> 1);

        long changeX = (long) cameraX - this.cc_oldCameraX;
        long changeY = (long) cameraY - this.cc_oldCameraY;
        long changeZ = (long) cameraZ - this.cc_oldCameraZ;
        this.cc_oldCameraX = cameraX;
        this.cc_oldCameraY = cameraY;
        this.cc_oldCameraZ = cameraZ;

        if (Math.abs(changeX) <= 1 && Math.abs(changeY) <= 1 && Math.abs(changeZ) <= 1) {
            if (changeX != 0) {
                int indexX = Math.floorMod(changeX < 0 ? minX - px : minX - px - 1, dx);
                int originX = px + Math.floorMod(indexX - px, dx);

                for (int indexZ = 0; indexZ < dz; indexZ++) {
                    int idxz = indexZ * dy * dx;
                    int originZ = pz + Math.floorMod(indexZ - pz, dz);

                    for (int indexY = 0; indexY < dy; indexY++) {
                        int idxyz = idxz + indexY * dx;
                        int originY = py + Math.floorMod(indexY - py, dy);

                        cc_refreshSectionNode(idxyz + indexX, originX, originY, originZ);
                    }
                }
            }

            if (changeY != 0) {
                int indexY = Math.floorMod(changeY < 0 ? minY - py : minY - py - 1, dy);
                int originY = py + Math.floorMod(indexY - py, dy);

                for (int indexZ = 0; indexZ < dz; indexZ++) {
                    int originZ = pz + Math.floorMod(indexZ - pz, dz);
                    int idxZ = indexZ * dy * dx;

                    int idxyz = idxZ + indexY * dx;

                    for (int indexX = 0; indexX < dx; indexX++) {
                        int originX = px + Math.floorMod(indexX - px, dx);

                        cc_refreshSectionNode(idxyz + indexX, originX, originY, originZ);
                    }
                }
            }

            if (changeZ != 0) {
                int indexZ = Math.floorMod(changeZ < 0 ? minZ - pz : minZ - pz - 1, dz);
                int originZ = pz + Math.floorMod(indexZ - pz, dz);
                int idxz = indexZ * dy * dx;

                for (int indexY = 0; indexY < dy; indexY++) {
                    int originY = py + Math.floorMod(indexY - py, dy);
                    int idxyz = idxz + indexY * dx;

                    for (int indexX = 0; indexX < dx; indexX++) {
                        int originX = px + Math.floorMod(indexX - px, dx);

                        cc_refreshSectionNode(idxyz + indexX, originX, originY, originZ);
                    }
                }
            }
        } else {
            for (int indexZ = 0; indexZ < dz; indexZ++) {
                int idxz = indexZ * dy * dx;
                int originZ = pz + Math.floorMod(indexZ - pz, dz);

                for (int indexY = 0; indexY < dy; indexY++) {
                    int idxyz = idxz + indexY * dx;
                    int originY = py + Math.floorMod(indexY - py, dy);

                    for (int indexX = 0; indexX < dx; indexX++) {
                        int originX = px + Math.floorMod(indexX - px, dx);

                        cc_refreshSectionNode(idxyz + indexX, originX, originY, originZ);
                    }
                }
            }
        }

        this.cameraSectionPos = newSectionPos;
        this.levelRenderer.getSectionOcclusionGraph().invalidate();
    }

    @Unique
    private void cc_refreshSectionNode(int sectionIndex, int originX, int originY, int originZ) {
        SectionRenderDispatcher.RenderSection renderSection = this.sections[sectionIndex];
        long oldSectionNode = renderSection.getSectionNode();
        if (oldSectionNode != SectionPos.asLong(originX, originY, originZ)) {
            renderSection.setSectionNode(SectionPos.asLong(originX, originY, originZ));
        }
    }

    @Unique
    private boolean cc_containsSection(int x, int y, int z) {
        return x >= this.cameraSectionPos.x() - this.viewDistance && x <= this.cameraSectionPos.x() + this.viewDistance
                && y >= this.cameraSectionPos.y() - this.viewDistance && y <= this.cameraSectionPos.y() + this.viewDistance
                && z >= this.cameraSectionPos.z() - this.viewDistance && z <= this.cameraSectionPos.z() + this.viewDistance;
    }
}
