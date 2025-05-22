package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.entity;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.server.level.ServerCubeCache;
import io.github.opencubicchunks.cubicchunks.world.entity.EntityCubePosGetter;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelReader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
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
 *  We modify Entity to track its cube position, and to replace calls to chunk-specific methods with their corresponding cubic methods when in a cubic Level.
 */
@Dasm(ChunkToCubeSet.class)
@Mixin(Entity.class)
public abstract class MixinEntity implements EntityCubePosGetter {
    @Shadow private Level level;
    @Shadow private BlockPos blockPosition;

    @Shadow public abstract void teleportTo(double x, double y, double z);
    @Shadow public abstract AABB getBoundingBox();
    @Shadow public abstract int getId();

    @AddFieldToSets(sets = ChunkToCubeSet.class, owner = @Ref(Entity.class), field = @FieldSig(type = @Ref(ChunkPos.class), name = "chunkPosition"))
    private CloPos cc_cubePosition = CloPos.cube(0, 0, 0);

    @AddTransformToSets(ChunkToCubeSet.class) @TransformFromMethod(@MethodSig("chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
    public native CloPos cc_cubePosition();

    // Update cube position when blockpos changes - this is the same location as where vanilla updates the chunk position
    @Inject(method = "setPosRaw", at = @At(value = "FIELD", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;blockPosition:Lnet/minecraft/core/BlockPos;", opcode = Opcodes.PUTFIELD))
    private void cc_onSetPosRaw(double x, double y, double z, CallbackInfo ci) {
        if (Coords.blockToCube(x) != cc_cubePosition.getX() || Coords.blockToCube(y) != cc_cubePosition.getY() || Coords.blockToCube(z) != cc_cubePosition.getZ()) {
            this.cc_cubePosition = CloPos.cube(this.blockPosition);
        }
    }

    // In cubic levels, force-load the destination cube instead of chunk
    @Inject(method = "teleportToWithTicket", at = @At("HEAD"), cancellable = true)
    private void cc_onTeleportToWithTicket(double x, double y, double z, CallbackInfo ci) {
        if (!((CanBeCubic) this.level).cc_isCubic()) return;
        ci.cancel();
        if (this.level instanceof ServerLevel) {
            CloPos cubePos = CloPos.cube(BlockPos.containing(x, y, z));
            ((ServerCubeCache) ((ServerLevel) this.level).getChunkSource()).cc_addRegionTicket(TicketType.POST_TELEPORT, cubePos, 0, this.getId());
            ((CubicLevel) this.level).cc_getCube(cubePos.getX(), cubePos.getY(), cubePos.getZ());
            this.teleportTo(x, y, z);
        }
    }

    // In cubic levels, check for unloaded cubes instead of chunks
    @Inject(method = "touchingUnloadedChunk", at = @At("HEAD"), cancellable = true)
    private void cc_onTouchingUnloadedChunk(CallbackInfoReturnable<Boolean> cir) {
        if (!((CanBeCubic) this.level).cc_isCubic()) return;
        AABB aabb = this.getBoundingBox().inflate(1.0);
        int minX = Mth.floor(aabb.minX);
        int maxX = Mth.ceil(aabb.maxX);
        int minY = Mth.floor(aabb.minY);
        int maxY = Mth.ceil(aabb.maxY);
        int minZ = Mth.floor(aabb.minZ);
        int maxZ = Mth.ceil(aabb.maxZ);
        cir.setReturnValue(!((CubicLevelReader) this.level).cc_hasCubesAt(minX, minY, minZ, maxX, maxY, maxZ));
    }
}
