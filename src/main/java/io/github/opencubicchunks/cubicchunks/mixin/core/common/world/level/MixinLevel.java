package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.llamalad7.mixinextras.sugar.Share;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevelReader;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubicChunkSource;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MixinLevel implements CubicLevel, MarkableAsCubic, LevelAccessor {
    @Shadow @Nullable public abstract ChunkAccess getChunk(int p_46502_, int p_46503_, ChunkStatus p_46504_, boolean p_46505_);

    @Shadow public abstract long getDayTime();

    protected boolean cc_isCubic;

    @Override
    public void cc_setCubic() {
        cc_isCubic = true;
    }

    @Override public boolean cc_isCubic() {
        return cc_isCubic;
    }

    public LevelCube cc_getCubeAt(BlockPos blockPos) {
        return (LevelCube)((CubicLevelReader)this).cc_getCube(Coords.blockToCube(blockPos.getX()), Coords.blockToCube(blockPos.getY()),
            Coords.blockToCube(blockPos.getZ()));
    }

    public LevelCube cc_getCube(int x, int y, int z) {
        return (LevelCube)this.cc_getCube(x, y, z, ChunkStatus.FULL);
    }

    @Nullable
    @Override
    public CubeAccess cc_getCube(int cubeX, int cubeY, int cubeZ, ChunkStatus status, boolean forceLoad) {
        CubeAccess cubeaccess = ((CubicChunkSource)this.getChunkSource()).cc_getCube(cubeX, cubeY, cubeZ, status, forceLoad);
        if (cubeaccess == null && forceLoad) {
            throw new IllegalStateException("Should always be able to create a cube!");
        } else {
            return cubeaccess;
        }
    }

    // setBlock
    @WrapOperation(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level"
        + "/Level;getChunkAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private LevelChunk cc_replaceLevelChunkInGetChunkAt(Level level, BlockPos blockPos, Operation<LevelChunk> original, @Share("levelCube") LocalRef<LevelCube> levelCubeLocalRef) {
        if(cc_isCubic) {
            levelCubeLocalRef.set(this.cc_getCubeAt(blockPos));
            return null;
        }
        return original.call(level, blockPos);
    }

    @WrapOperation(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState cc_replaceLevelChunkInSetBlockState(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState, boolean flag1, Operation<BlockState> original,
                                                           @Share("levelCube") LocalRef<LevelCube> levelCubeLocalRef) {
        if(cc_isCubic) {
            levelCubeLocalRef.get().setBlockState(blockPos, blockState, flag1);
            return null;
        }
        return original.call(levelChunk, blockPos, blockState, flag1);
    }

    @WrapWithCondition(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;markAndNotifyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;II)V"))
    private boolean cc_replaceLevelChunkInMarkAndNotifyBlock(Level level, BlockPos blockPos, LevelChunk levelChunk, BlockState blockStatePrev, BlockState blockStateNew, int flags,
                                                             int p_46607_, @Share("levelCube") LocalRef<LevelCube> levelCubeLocalRef) {
        if(cc_isCubic) {
            this.markAndNotifyBlock(blockPos, levelCubeLocalRef.get(), blockStatePrev, blockStateNew, flags, p_46607_);
            return false;
        }
        return true;
    }

    @TransformFrom("markAndNotifyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;II)V")
    public native void markAndNotifyBlock(BlockPos blockPos, @Nullable LevelCube levelCube, BlockState blockStatePrev, BlockState blockStateNew, int flags, int p_46608_);

    // getBlockState
    @Inject(method = "getBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getChunk(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private void cc_replaceLevelChunkInGetBlockState(BlockPos blockPos, CallbackInfoReturnable<BlockState> cir, @Share("levelCube") LocalRef<LevelCube> levelCubeLocalRef) {
        if(cc_isCubic) {
            levelCubeLocalRef.set(this.cc_getCubeAt(blockPos));
        }
    }

    @WrapOperation(method = "getBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;getBlockState(Lnet/minecraft/core/BlockPos;)"
        + "Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState cc_replaceLevelChunkInGetBlockState(LevelChunk levelChunk, BlockPos blockPos, Operation<BlockState> original,
                                                           @Share("levelCube") LocalRef<LevelCube> levelCubeLocalRef) {
        if(cc_isCubic) {
            return levelCubeLocalRef.get().getBlockState(blockPos);
        }
        return original.call(levelChunk, blockPos);
    }

    // getBlockEntity
    @Inject(method = "getBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getChunkAt(Lnet/minecraft/core/BlockPos;)"
        + "Lnet/minecraft/world/level/chunk/LevelChunk;"), cancellable = true)
    private void cc_replaceGetChunkAtInSetBlockEntity(BlockPos blockPos, CallbackInfoReturnable<BlockEntity> cir) {
        if(cc_isCubic) {
            cir.setReturnValue(this.cc_getCubeAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE));
        }
    }

    // getFluidState
    @WrapOperation(method = "getFluidState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private FluidState cc_replaceGetChunkAtInGetFluidState(LevelChunk levelChunk, BlockPos blockPos, Operation<FluidState> original) {
        if(cc_isCubic) {
            return this.cc_getCubeAt(blockPos).getFluidState(blockPos);
        }
        return original.call(levelChunk, blockPos);
    }

    // setBlockEntity
    @Inject(method = "setBlockEntity", at = @At(value="INVOKE", target="Lnet/minecraft/world/level/Level;getChunkAt(Lnet/minecraft/core/BlockPos;)"
        + "Lnet/minecraft/world/level/chunk/LevelChunk;"), cancellable=true)
    private void cc_replaceLevelChunkInSetBlockEntity(BlockEntity blockEntity, CallbackInfo ci, @Local(ordinal = 0) BlockPos blockPos) {
        if(cc_isCubic) {
            this.cc_getCubeAt(blockPos).addAndRegisterBlockEntity(blockEntity);
            ci.cancel();
        }
    }

    // removeBlockEntity
    @Inject(method = "removeBlockEntity", at = @At(value="INVOKE", target="Lnet/minecraft/world/level/Level;getChunkAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/chunk/LevelChunk;"), cancellable=true)
    private void cc_replaceGetChunkAtInRemoveBlockEntity(BlockPos blockPos, CallbackInfo ci) {
        if(cc_isCubic) {
            this.cc_getCubeAt(blockPos).removeBlockEntity(blockPos);
            ci.cancel();
        }
    }

    // isLoaded
    @WrapOperation(method = "isLoaded", at = @At(value="INVOKE", target="Lnet/minecraft/world/level/chunk/ChunkSource;hasChunk(II)Z"))
    private boolean cc_replaceHasChunkInIsLoaded(ChunkSource chunkSource, int x, int z, Operation<Boolean> original, BlockPos blockPos) {
        if(cc_isCubic) {
            return ((CubicChunkSource)this.getChunkSource()).cc_hasCube(Coords.blockToCube(blockPos.getX()), Coords.blockToCube(blockPos.getY()), Coords.blockToCube(blockPos.getZ()));
        }
        return false;
    }

    // loadedAndEntityCanStandOnFace
    @Inject(method = "loadedAndEntityCanStandOnFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)"
        + "Lnet/minecraft/world/level/chunk/ChunkAccess;"), cancellable = true)
    private void cc_replaceGetChunkAtInLoadedAndEntityCanStandOnFace(BlockPos blockPos, Entity entity, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if(cc_isCubic) {
            CubeAccess cubeAccess = this.cc_getCube(Coords.blockToCube(blockPos.getX()), Coords.blockToCube(blockPos.getY()), Coords.blockToCube(blockPos.getZ()), ChunkStatus.FULL, false);
            cir.setReturnValue(cubeAccess == null ? false : cubeAccess.getBlockState(blockPos).entityCanStandOnFace(this, blockPos, entity, direction));
        }
    }

    // blockEntityChanged
    @Inject(method = "blockEntityChanged", at = @At(value = "HEAD"), cancellable = true)
    private void cc_replaceBlockEntityChanged(BlockPos blockPos, CallbackInfo ci) {
        if(cc_isCubic) {
            if (this.cc_hasCubeAt(blockPos)) {
                this.cc_getCubeAt(blockPos).setUnsaved(true);
            }
            ci.cancel();
        }
    }

    // getCurrentDifficultyAt
    @Inject(method = "getCurrentDifficultyAt", at = @At(value = "HEAD"), cancellable = true)
    private void cc_replaceGetCurrentDifficultyAt(BlockPos blockPos, CallbackInfoReturnable<DifficultyInstance> cir) {
        if(cc_isCubic) {
            long i = 0L;
            float f = 0.0F;
            if (this.cc_hasCubeAt(blockPos)) {
                f = this.getMoonBrightness();
                i = this.cc_getCubeAt(blockPos).getInhabitedTime();
            }
            cir.setReturnValue(new DifficultyInstance(this.getDifficulty(), this.getDayTime(), i, f));
        }
    }
    // TODO: Phase 3 low priority: Add a method to modify isOutsideSpawnableHeight to respect the limits of the packing for CloPos
}
