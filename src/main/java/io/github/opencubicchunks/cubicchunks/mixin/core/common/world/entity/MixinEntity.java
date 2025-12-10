package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.entity;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.world.entity.EntityCubePosGetter;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelReader;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * We modify Entity to track its cube position, and to replace calls to chunk-specific methods with their corresponding cubic methods when in a cubic
 * Level.
 */
@Dasm(value = ChunkToCubeSet.class, target = @Ref(Entity.class))
@Mixin(Entity.class)
public abstract class MixinEntity implements EntityCubePosGetter {
    @Shadow private Level level;
    @Shadow private BlockPos blockPosition;

    @Shadow public abstract Level level();

    @Shadow public abstract void teleportTo(double x, double y, double z);

    @Shadow public abstract AABB getBoundingBox();

    @Shadow public abstract int getId();

    @AddFieldToSets(containers = ChunkToCubeSet.Entity_redirects.class, field = "chunkPosition:Lnet/minecraft/world/level/ChunkPos;")
    private CubePos cc_cubePosition = CubePos.of(0, 0, 0);

    @AddTransformToSets(ChunkToCubeSet.Entity_redirects.class)
    @TransformFromMethod("chunkPosition()Lnet/minecraft/world/level/ChunkPos;")
    public native CubePos cc_cubePosition();

    @AddMethodToSets(containers = ChunkToCloSet.Entity_redirects.class, method = "chunkPosition()Lnet/minecraft/world/level/ChunkPos;")
    public CloPos cc_cubePositionAsClo() {
        return CloPos.cube(cc_cubePosition());
    }

    // Update cube position when blockpos changes - this is the same location as where vanilla updates the chunk position
    @Inject(method = "setPosRaw",
        at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;blockPosition:Lnet/minecraft/core/BlockPos;",
            opcode = Opcodes.PUTFIELD))
    private void cc_onSetPosRaw(double x, double y, double z, CallbackInfo ci) {
        if (Coords.blockToCube(x) != cc_cubePosition.getX() || Coords.blockToCube(y) != cc_cubePosition.getY()
            || Coords.blockToCube(z) != cc_cubePosition.getZ()) {
            this.cc_cubePosition = CubePos.from(this.blockPosition);
        }
    }

    // In cubic levels, check for unloaded cubes instead of chunks
    @Inject(method = "touchingUnloadedChunk", at = @At("HEAD"), cancellable = true)
    private void cc_onTouchingUnloadedChunk(CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) this.level).cc_isCubic()) {
            return;
        }
        AABB aabb = this.getBoundingBox().inflate(1.0);
        int minX = Mth.floor(aabb.minX);
        int maxX = Mth.ceil(aabb.maxX);
        int minY = Mth.floor(aabb.minY);
        int maxY = Mth.ceil(aabb.maxY);
        int minZ = Mth.floor(aabb.minZ);
        int maxZ = Mth.ceil(aabb.maxZ);
        cir.setReturnValue(!((CubicLevelReader) this.level).cc_hasCubesAt(minX, minY, minZ, maxX, maxY, maxZ));
    }

    // TODO (P2) teleportation code needs CC changes
}
