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

    @Nullable BlockState setBlockState(BlockPos p_62087_, BlockState p_62088_, boolean p_62089_);

    void setBlockEntity(BlockEntity p_156114_);

    void addEntity(Entity p_62078_);

    int getHighestFilledSectionIndex();

    // Deprecated
    int getHighestSectionPosition();

    Set<BlockPos> getBlockEntitiesPos();

    LevelChunkSection[] getSections();

    LevelChunkSection getSection(int p_187657_);

    Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps();

    void setHeightmap(Heightmap.Types p_62083_, long[] p_62084_);

    Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types p_62079_);

    boolean hasPrimedHeightmap(Heightmap.Types p_187659_);

    int getHeight(Heightmap.Types p_62080_, int p_62081_, int p_62082_);

    // replacement of ChunkPos getPos()
    CloPos cc_getCloPos();

    Map<Structure, StructureStart> getAllStarts();

    void setAllStarts(Map<Structure, StructureStart> p_62090_);

    boolean isYSpaceEmpty(int p_62075_, int p_62076_);

    void setUnsaved(boolean p_62094_);

    boolean isUnsaved();

    ChunkStatus getStatus();

    ChunkStatus getHighestGeneratedStatus();

    void removeBlockEntity(BlockPos p_62101_);

    void markPosForPostprocessing(BlockPos p_62102_);

    ShortList[] getPostProcessing();

    void addPackedPostProcess(short p_62092_, int p_62093_);

    void setBlockEntityNbt(CompoundTag p_62091_);

    @Nullable CompoundTag getBlockEntityNbt(BlockPos p_62103_);

    @Nullable CompoundTag getBlockEntityNbtForSaving(BlockPos p_62104_);

    void findBlocks(Predicate<BlockState> p_285343_, BiConsumer<BlockPos, BlockState> p_285030_);

    void findBlocks(java.util.function.BiPredicate<BlockState, BlockPos> p_285343_, BiConsumer<BlockPos, BlockState> p_285030_);

    TickContainerAccess<Block> getBlockTicks();

    TickContainerAccess<Fluid> getFluidTicks();

    ChunkAccess.TicksToSave getTicksForSerialization();

    UpgradeData getUpgradeData();

    boolean isOldNoiseGeneration();

    @Nullable BlendingData getBlendingData();

    void setBlendingData(BlendingData p_187646_);

    long getInhabitedTime();

    void incrementInhabitedTime(long p_187633_);

    void setInhabitedTime(long p_62099_);

    boolean isLightCorrect();

    void setLightCorrect(boolean p_62100_);

    NoiseChunk getOrCreateNoiseChunk(Function<CloAccess, NoiseChunk> p_223013_);

    @Deprecated BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> p_223015_);

    void fillBiomesFromNoise(BiomeResolver p_187638_, Climate.Sampler p_187639_);

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
