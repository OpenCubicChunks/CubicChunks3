package io.github.opencubicchunks.cubicchunks.world.level.cube;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.CopyFrom;
import io.github.opencubicchunks.cubicchunks.mixin.DasmRedirect;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

@DasmRedirect({ "cubeAccessAndDescendants" })
public class ProtoCube extends CubeAccess implements ProtoClo {
    // Fields matching ProtoChunk
    @Nullable
    private volatile LevelLightEngine lightEngine;
    private volatile ChunkStatus status;
    private final List<CompoundTag> entities;
    private final Map<GenerationStep.Carving, CarvingMask> carvingMasks;
    @Nullable
    private BelowZeroRetrogen belowZeroRetrogen;
    private final ProtoChunkTicks<Block> blockTicks;
    private final ProtoChunkTicks<Fluid> fluidTicks;

    // Constructors mirroring vanilla signatures
    public ProtoCube(CloPos cloPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, @Nullable BlendingData blendingData) {
        this(cloPos, upgradeData, null, new ProtoChunkTicks(), new ProtoChunkTicks(), levelHeightAccessor, biomeRegistry, blendingData);
    }

    public ProtoCube(CloPos cloPos, UpgradeData upgradeData, @Nullable LevelChunkSection[] sections, ProtoChunkTicks<Block> blockTicks, ProtoChunkTicks<Fluid> liquidTicks,
                     LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry, @Nullable BlendingData blendingData) {
        super(cloPos, upgradeData, levelHeightAccessor, biomeRegistry, 0L, sections, blendingData);
        this.status = ChunkStatus.EMPTY;
        this.entities = Lists.newArrayList();
        this.carvingMasks = new Object2ObjectArrayMap();
        this.blockTicks = blockTicks;
        this.fluidTicks = liquidTicks;
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getBlockTicks()Lnet/minecraft/world/ticks/TickContainerAccess;")
    @Override public native TickContainerAccess<Block> getBlockTicks();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getFluidTicks()Lnet/minecraft/world/ticks/TickContainerAccess;")
    @Override public native TickContainerAccess<Fluid> getFluidTicks();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getTicksForSerialization()Lnet/minecraft/world/level/chunk/ChunkAccess$TicksToSave;")
    @Override public native ChunkAccess.TicksToSave getTicksForSerialization();

    // dasm + mixin
    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Override public native BlockState getBlockState(BlockPos pos);

    // dasm + mixin
    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;")
    @Override public native FluidState getFluidState(BlockPos pos);

    @Nullable
    @Override public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        LevelChunkSection section = this.getSection(Coords.blockToIndex(pos));
        boolean emptySection = section.hasOnlyAir();
        if (emptySection && state.is(Blocks.AIR)) {
            return state;
        } else {
            int sectionLocalX = SectionPos.sectionRelative(x);
            int sectionLocalY = SectionPos.sectionRelative(y);
            int sectionLocalZ = SectionPos.sectionRelative(z);
            BlockState blockstate = section.setBlockState(sectionLocalX, sectionLocalY, sectionLocalZ, state);
            // TODO (P2) lighting and heightmaps - see vanilla method - might be dasm-able once we do?

            return blockstate;
        }
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    @Override public native void setBlockEntity(BlockEntity pBlockEntity);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Override @Nullable public native BlockEntity getBlockEntity(BlockPos pPos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getBlockEntities()Ljava/util/Map;")
    @Override public native Map<BlockPos, BlockEntity> getBlockEntities();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "addEntity(Lnet/minecraft/nbt/CompoundTag;)V")
    @Override public native void addEntity(CompoundTag pTag);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "addEntity(Lnet/minecraft/world/entity/Entity;)V")
    @Override public native void addEntity(Entity pEntity);

    // setStartForStructure: ProtoChunk logic handles below-zero retrogen then calls super, so we don't need to override

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getEntities()Ljava/util/List;")
    @Override public native List<CompoundTag> getEntities();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getStatus()Lnet/minecraft/world/level/chunk/ChunkStatus;")
    @Override public native ChunkStatus getStatus();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "setStatus(Lnet/minecraft/world/level/chunk/ChunkStatus;)V")
    @Override public native void setStatus(ChunkStatus pStatus);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getNoiseBiome(III)Lnet/minecraft/core/Holder;")
    @Override public native Holder<Biome> getNoiseBiome(int pX, int pY, int pZ);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "packOffsetCoordinates(Lnet/minecraft/core/BlockPos;)S")
    public native static short packOffsetCoordinates(BlockPos pPos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "unpackOffsetCoordinates(SILnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/core/BlockPos;")
    public native static BlockPos unpackOffsetCoordinates(short pPackedPos, int pYOffset, ChunkPos pChunkPos);

    // dasm + mixin
    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "markPosForPostprocessing(Lnet/minecraft/core/BlockPos;)V")
    @Override public native void markPosForPostprocessing(BlockPos pPos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "addPackedPostProcess(SI)V")
    @Override public native void addPackedPostProcess(short pPackedPosition, int pIndex);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getBlockEntityNbts()Ljava/util/Map;")
    @Override public native Map<BlockPos, CompoundTag> getBlockEntityNbts();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;")
    @Override @Nullable public native CompoundTag getBlockEntityNbtForSaving(BlockPos pPos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "removeBlockEntity(Lnet/minecraft/core/BlockPos;)V")
    @Override public native void removeBlockEntity(BlockPos pPos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getCarvingMask(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)Lnet/minecraft/world/level/chunk/CarvingMask;")
    @Override @Nullable public native CarvingMask getCarvingMask(GenerationStep.Carving pStep);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "getOrCreateCarvingMask(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)Lnet/minecraft/world/level/chunk/CarvingMask;")
    @Override public native CarvingMask getOrCreateCarvingMask(GenerationStep.Carving pStep);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "setCarvingMask(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;Lnet/minecraft/world/level/chunk/CarvingMask;)V")
    @Override public native void setCarvingMask(GenerationStep.Carving pStep, CarvingMask pCarvingMask);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "setLightEngine(Lnet/minecraft/world/level/lighting/LevelLightEngine;)V")
    @Override public native void setLightEngine(LevelLightEngine pLightEngine);

    @Override public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen pBelowZeroRetrogen) {
        // Below-zero retrogen is unused in CC, hence empty method body
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "unpackTicks(Lnet/minecraft/world/ticks/ProtoChunkTicks;)Lnet/minecraft/world/ticks/LevelChunkTicks;")
    private static native <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> pTicks);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "unpackBlockTicks()Lnet/minecraft/world/ticks/LevelChunkTicks;")
    @Override public native LevelChunkTicks<Block> unpackBlockTicks();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ProtoChunk.class), value = "unpackFluidTicks()Lnet/minecraft/world/ticks/LevelChunkTicks;")
    @Override public native LevelChunkTicks<Fluid> unpackFluidTicks();

    @Override public LevelHeightAccessor getHeightAccessorForGeneration() {
        return this; // Vanilla has logic for below-zero retrogen here
    }
}
