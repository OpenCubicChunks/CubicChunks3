package io.github.opencubicchunks.cubicchunks.world.level.cube;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

@Dasm(ChunkToCubeSet.class)
public class ProtoCube extends CubeAccess implements ProtoClo {
    // Fields matching ProtoChunk
    private volatile @Nullable LevelLightEngine lightEngine;
    private volatile ChunkStatus status;
    private final List<CompoundTag> entities;
    private @Nullable CarvingMask carvingMask;
    private @Nullable BelowZeroRetrogen belowZeroRetrogen;
    private final ProtoChunkTicks<Block> blockTicks;
    private final ProtoChunkTicks<Fluid> fluidTicks;

    // Constructors mirroring vanilla signatures
    public ProtoCube(
            CubePos cubePos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry,
            @Nullable BlendingData blendingData
    ) {
        this(cubePos, upgradeData, null, new ProtoChunkTicks(), new ProtoChunkTicks(), levelHeightAccessor, biomeRegistry, blendingData);
    }

    public ProtoCube(
            CubePos cubePos, UpgradeData upgradeData, @Nullable LevelChunkSection[] sections, ProtoChunkTicks<Block> blockTicks,
            ProtoChunkTicks<Fluid> liquidTicks, LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry,
            @Nullable BlendingData blendingData
    ) {
        super(cubePos, upgradeData, levelHeightAccessor, biomeRegistry, 0L, sections, blendingData);
        this.status = ChunkStatus.EMPTY;
        this.entities = Lists.newArrayList();
        this.blockTicks = blockTicks;
        this.fluidTicks = liquidTicks;
    }

    @TransformFromMethod(value = "getBlockTicks()Lnet/minecraft/world/ticks/TickContainerAccess;", owner = @Ref(ProtoChunk.class))
    @Override public native TickContainerAccess<Block> getBlockTicks();

    @TransformFromMethod(value = "getFluidTicks()Lnet/minecraft/world/ticks/TickContainerAccess;", owner = @Ref(ProtoChunk.class))
    @Override public native TickContainerAccess<Fluid> getFluidTicks();

    @TransformFromMethod(value = "getTicksForSerialization(J)Lnet/minecraft/world/level/chunk/ChunkAccess$PackedTicks;", owner = @Ref(ProtoChunk.class))
    @Override public native ChunkAccess.PackedTicks getTicksForSerialization(long todoNameThis);

    // dasm + mixin
    @TransformFromMethod(value = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", owner = @Ref(ProtoChunk.class))
    @Override public native BlockState getBlockState(BlockPos pos);

    // dasm + mixin
    @TransformFromMethod(value = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;", owner = @Ref(ProtoChunk.class))
    @Override public native FluidState getFluidState(BlockPos pos);

    @Override public @Nullable BlockState setBlockState(BlockPos pos, BlockState state, int flags) {
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

    @TransformFromMethod(value = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void setBlockEntity(BlockEntity blockEntity);

    @TransformFromMethod(value = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", owner = @Ref(ProtoChunk.class))
    @Override public native @Nullable BlockEntity getBlockEntity(BlockPos pos);

    @TransformFromMethod(value = "getBlockEntities()Ljava/util/Map;", owner = @Ref(ProtoChunk.class))
    @Override public native Map<BlockPos, BlockEntity> getBlockEntities();

    @TransformFromMethod(value = "addEntity(Lnet/minecraft/nbt/CompoundTag;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void addEntity(CompoundTag tag);

    @TransformFromMethod(value = "addEntity(Lnet/minecraft/world/entity/Entity;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void addEntity(Entity entity);

    // setStartForStructure: ProtoChunk logic handles below-zero retrogen then calls super, so we don't need to override

    @TransformFromMethod(value = "getEntities()Ljava/util/List;", owner = @Ref(ProtoChunk.class))
    @Override public native List<CompoundTag> getEntities();

    @TransformFromMethod(value = "getPersistedStatus()Lnet/minecraft/world/level/chunk/status/ChunkStatus;", owner = @Ref(ProtoChunk.class))
    @Override public native ChunkStatus getPersistedStatus();

    @TransformFromMethod(value = "setPersistedStatus(Lnet/minecraft/world/level/chunk/status/ChunkStatus;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void setPersistedStatus(ChunkStatus status);

    @TransformFromMethod(value = "getNoiseBiome(III)Lnet/minecraft/core/Holder;", owner = @Ref(ProtoChunk.class))
    @Override public native Holder<Biome> getNoiseBiome(int x, int y, int z);

    @TransformFromMethod(value = "packOffsetCoordinates(Lnet/minecraft/core/BlockPos;)S", owner = @Ref(ProtoChunk.class))
    public static native short packOffsetCoordinates(BlockPos pos);

    @TransformFromMethod(value = "unpackOffsetCoordinates(SILnet/minecraft/world/level/ChunkPos;)Lnet/minecraft/core/BlockPos;", owner = @Ref(ProtoChunk.class))
    public static native BlockPos unpackOffsetCoordinates(short packedPos, int yOffset, ChunkPos chunkPos);

    // dasm + mixin
    @TransformFromMethod(value = "markPosForPostprocessing(Lnet/minecraft/core/BlockPos;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void markPosForPostprocessing(BlockPos pos);

    @TransformFromMethod(value = "addPackedPostProcess(Lit/unimi/dsi/fastutil/shorts/ShortList;I)V", owner = @Ref(ProtoChunk.class))
    @Override public native void addPackedPostProcess(ShortList offsets, int index);

    @TransformFromMethod(value = "getBlockEntityNbts()Ljava/util/Map;", owner = @Ref(ProtoChunk.class))
    @Override public native Map<BlockPos, CompoundTag> getBlockEntityNbts();

    @TransformFromMethod(value = "getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;", owner = @Ref(ProtoChunk.class))
    @Override public native @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos, HolderLookup.Provider provider);

    @TransformFromMethod(value = "removeBlockEntity(Lnet/minecraft/core/BlockPos;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void removeBlockEntity(BlockPos pos);

    @TransformFromMethod(value = "getCarvingMask()Lnet/minecraft/world/level/chunk/CarvingMask;", owner = @Ref(ProtoChunk.class))
    @Override public native @Nullable CarvingMask getCarvingMask();

    @TransformFromMethod(value = "getOrCreateCarvingMask()Lnet/minecraft/world/level/chunk/CarvingMask;", owner = @Ref(ProtoChunk.class))
    @Override public native CarvingMask getOrCreateCarvingMask();

    @TransformFromMethod(value = "setCarvingMask(Lnet/minecraft/world/level/chunk/CarvingMask;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void setCarvingMask(CarvingMask carvingMask);

    @TransformFromMethod(value = "setLightEngine(Lnet/minecraft/world/level/lighting/LevelLightEngine;)V", owner = @Ref(ProtoChunk.class))
    @Override public native void setLightEngine(LevelLightEngine lightEngine);

    @Override public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen belowZeroRetrogen) {
        // Below-zero retrogen is unused in CC, hence empty method body
    }

    @TransformFromMethod(value = "unpackTicks(Lnet/minecraft/world/ticks/ProtoChunkTicks;)Lnet/minecraft/world/ticks/LevelChunkTicks;", owner = @Ref(ProtoChunk.class))
    private static native <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> ticks);

    @TransformFromMethod(value = "unpackBlockTicks()Lnet/minecraft/world/ticks/LevelChunkTicks;", owner = @Ref(ProtoChunk.class))
    @Override public native LevelChunkTicks<Block> unpackBlockTicks();

    @TransformFromMethod(value = "unpackFluidTicks()Lnet/minecraft/world/ticks/LevelChunkTicks;", owner = @Ref(ProtoChunk.class))
    @Override public native LevelChunkTicks<Fluid> unpackFluidTicks();

    @Override public LevelHeightAccessor getHeightAccessorForGeneration() {
        return this; // Vanilla has logic for below-zero retrogen here
    }
}
