package io.github.opencubicchunks.cubicchunks.world.level.cube;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.CubeAccessAndDescendantsSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import io.github.opencubicchunks.dasm.api.MethodSig;
import io.github.opencubicchunks.dasm.api.Ref;
import io.github.opencubicchunks.dasm.api.transform.DasmRedirect;
import io.github.opencubicchunks.dasm.api.transform.TransformFrom;
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

@DasmRedirect(CubeAccessAndDescendantsSet.class)
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

    @TransformFrom(
        value = @MethodSig("getBlockTicks()Lnet/minecraft/world/ticks/TickContainerAccess;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native TickContainerAccess<Block> getBlockTicks();

    @TransformFrom(
        value = @MethodSig("getFluidTicks()Lnet/minecraft/world/ticks/TickContainerAccess;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native TickContainerAccess<Fluid> getFluidTicks();

    @TransformFrom(
        value = @MethodSig("getTicksForSerialization()Lnet/minecraft/world/level/chunk/ChunkAccess$TicksToSave;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native ChunkAccess.TicksToSave getTicksForSerialization();

    // dasm + mixin
    @TransformFrom(
        value = @MethodSig("getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native BlockState getBlockState(BlockPos pos);

    // dasm + mixin
    @TransformFrom(
        value = @MethodSig("getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
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

    @TransformFrom(
        value = @MethodSig("setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void setBlockEntity(BlockEntity pBlockEntity);

    @TransformFrom(
        value = @MethodSig("getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override @Nullable public native BlockEntity getBlockEntity(BlockPos pPos);

    @TransformFrom(
        value = @MethodSig("getBlockEntities()Ljava/util/Map;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native Map<BlockPos, BlockEntity> getBlockEntities();

    @TransformFrom(
        value = @MethodSig("addEntity(Lnet/minecraft/nbt/CompoundTag;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void addEntity(CompoundTag pTag);

    @TransformFrom(
        value = @MethodSig("addEntity(Lnet/minecraft/world/entity/Entity;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void addEntity(Entity pEntity);

    // setStartForStructure: ProtoChunk logic handles below-zero retrogen then calls super, so we don't need to override

    @TransformFrom(
        value = @MethodSig("getEntities()Ljava/util/List;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native List<CompoundTag> getEntities();

    @TransformFrom(
        value = @MethodSig("getStatus()Lnet/minecraft/world/level/chunk/ChunkStatus;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native ChunkStatus getStatus();

    @TransformFrom(
        value = @MethodSig("setStatus(Lnet/minecraft/world/level/chunk/ChunkStatus;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void setStatus(ChunkStatus pStatus);

    @TransformFrom(
        value = @MethodSig("getNoiseBiome(III)Lnet/minecraft/core/Holder;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native Holder<Biome> getNoiseBiome(int pX, int pY, int pZ);

    @TransformFrom(
        value = @MethodSig("packOffsetCoordinates(Lnet/minecraft/core/BlockPos;)S"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    public native static short packOffsetCoordinates(BlockPos pPos);

    @TransformFrom(
        value = @MethodSig("unpackOffsetCoordinates(SILnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/core/BlockPos;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    public native static BlockPos unpackOffsetCoordinates(short pPackedPos, int pYOffset, ChunkPos pChunkPos);

    // dasm + mixin
    @TransformFrom(
        value = @MethodSig("markPosForPostprocessing(Lnet/minecraft/core/BlockPos;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void markPosForPostprocessing(BlockPos pPos);

    @TransformFrom(
        value = @MethodSig("addPackedPostProcess(SI)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void addPackedPostProcess(short pPackedPosition, int pIndex);

    @TransformFrom(
        value = @MethodSig("getBlockEntityNbts()Ljava/util/Map;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native Map<BlockPos, CompoundTag> getBlockEntityNbts();

    @TransformFrom(
        value = @MethodSig("getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override @Nullable public native CompoundTag getBlockEntityNbtForSaving(BlockPos pPos);

    @TransformFrom(
        value = @MethodSig("removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void removeBlockEntity(BlockPos pPos);

    @TransformFrom(
        value = @MethodSig("getCarvingMask(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)Lnet/minecraft/world/level/chunk/CarvingMask;"),
        copyFrom = @Ref(ProtoChunk.class))
    @Override @Nullable public native CarvingMask getCarvingMask(GenerationStep.Carving pStep);

    @TransformFrom(
        value = @MethodSig("getOrCreateCarvingMask(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;)Lnet/minecraft/world/level/chunk/CarvingMask;"),
        copyFrom = @Ref(ProtoChunk.class))
    @Override public native CarvingMask getOrCreateCarvingMask(GenerationStep.Carving pStep);

    @TransformFrom(
        value = @MethodSig("setCarvingMask(Lnet/minecraft/world/level/levelgen/GenerationStep$Carving;Lnet/minecraft/world/level/chunk/CarvingMask;)V"),
        copyFrom = @Ref(ProtoChunk.class))
    @Override public native void setCarvingMask(GenerationStep.Carving pStep, CarvingMask pCarvingMask);

    @TransformFrom(
        value = @MethodSig("setLightEngine(Lnet/minecraft/world/level/lighting/LevelLightEngine;)V"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native void setLightEngine(LevelLightEngine pLightEngine);

    @Override public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen pBelowZeroRetrogen) {
        // Below-zero retrogen is unused in CC, hence empty method body
    }

    @TransformFrom(
        value = @MethodSig("unpackTicks(Lnet/minecraft/world/ticks/ProtoChunkTicks;)Lnet/minecraft/world/ticks/LevelChunkTicks;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    private static native <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> pTicks);

    @TransformFrom(
        value = @MethodSig("unpackBlockTicks()Lnet/minecraft/world/ticks/LevelChunkTicks;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native LevelChunkTicks<Block> unpackBlockTicks();

    @TransformFrom(
        value = @MethodSig("unpackFluidTicks()Lnet/minecraft/world/ticks/LevelChunkTicks;"),
        copyFrom = @Ref(ProtoChunk.class)
    )
    @Override public native LevelChunkTicks<Fluid> unpackFluidTicks();

    @Override public LevelHeightAccessor getHeightAccessorForGeneration() {
        return this; // Vanilla has logic for below-zero retrogen here
    }
}
