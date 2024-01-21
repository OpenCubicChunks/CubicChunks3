package io.github.opencubicchunks.cubicchunks.world.level.chunklike;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.TickContainerAccess;

public interface CloAccess extends BlockGetter, BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess {
    GameEventListenerRegistry getListenerRegistry(int p_251437_);

    @Nullable BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving);

    void setBlockEntity(BlockEntity blockEntity);

    void addEntity(Entity entity);

    int getHighestFilledSectionIndex();

    // Deprecated
    int getHighestSectionPosition();

    Set<BlockPos> getBlockEntitiesPos();

    LevelChunkSection[] getSections();

    LevelChunkSection getSection(int index);

    Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps();

    void setHeightmap(Heightmap.Types type, long[] data);

    Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type);

    boolean hasPrimedHeightmap(Heightmap.Types type);

    int getHeight(Heightmap.Types type, int x, int z);

    // replacement of ChunkPos getPos()
    CloPos cc_getCloPos();

    Map<Structure, StructureStart> getAllStarts();

    void setAllStarts(Map<Structure, StructureStart> structureStarts);

    boolean isYSpaceEmpty(int startY, int endY);

    void setUnsaved(boolean unsaved);

    boolean isUnsaved();

    ChunkStatus getStatus();

    ChunkStatus getHighestGeneratedStatus();

    void removeBlockEntity(BlockPos pos);

    void markPosForPostprocessing(BlockPos pos);

    ShortList[] getPostProcessing();

    void addPackedPostProcess(short packedPosition, int index);

    void setBlockEntityNbt(CompoundTag tag);

    @Nullable CompoundTag getBlockEntityNbt(BlockPos pos);

    @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos pos);

    void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output);

    void findBlocks(java.util.function.BiPredicate<BlockState, BlockPos> predicate, BiConsumer<BlockPos, BlockState> output);

    TickContainerAccess<Block> getBlockTicks();

    TickContainerAccess<Fluid> getFluidTicks();

    ChunkAccess.TicksToSave getTicksForSerialization();

    UpgradeData getUpgradeData();

    boolean isOldNoiseGeneration();

    @Nullable BlendingData getBlendingData();

    void setBlendingData(BlendingData blendingData);

    long getInhabitedTime();

    void incrementInhabitedTime(long amount);

    void setInhabitedTime(long inhabitedTime);

    boolean isLightCorrect();

    void setLightCorrect(boolean lightCorrect);

    NoiseChunk getOrCreateNoiseChunk(Function<CloAccess, NoiseChunk> noiseChunkCreator);

    @Deprecated BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> carverBiomeSettingsProvider);

    void fillBiomesFromNoise(BiomeResolver resolver, Climate.Sampler sampler);

    boolean hasAnyStructureReferences();

    @Nullable BelowZeroRetrogen getBelowZeroRetrogen();

    boolean isUpgrading();

    LevelHeightAccessor getHeightAccessorForGeneration();

    void initializeLightSources();

    // TODO static methods
//    static ShortList getOrCreateOffsetList(ShortList[] p_62096_, int p_62097_);
//
//    static record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids);

    // TODO forge method
//    @Nullable public net.minecraft.world.level.LevelAccessor getWorldForge() { return null; }
}
