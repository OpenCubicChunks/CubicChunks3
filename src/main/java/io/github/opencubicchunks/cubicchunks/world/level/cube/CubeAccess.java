package io.github.opencubicchunks.cubicchunks.world.level.cube;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.mixin.CopyFrom;
import io.github.opencubicchunks.cubicchunks.mixin.DasmRedirect;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.Nullable;

@DasmRedirect({ "cubeAccessAndDescendants" })
public abstract class CubeAccess implements CloAccess {
    // Fields copied from ChunkAccess, except ChunkPos -> CloPos
    protected final ShortList[] postProcessing;
    protected volatile boolean unsaved;
    private volatile boolean isLightCorrect;
    protected final CloPos cloPos;
    private long inhabitedTime;
    @Nullable
    @Deprecated
    private BiomeGenerationSettings carverBiomeSettings;
    // TODO (P3) NoiseChunk might need to be different
    @Nullable protected NoiseChunk noiseChunk;

    protected final UpgradeData upgradeData;
    @Nullable
    protected BlendingData blendingData;
    protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    protected ChunkSkyLightSources skyLightSources;
    private final Map<Structure, StructureStart> structureStarts = Maps.newHashMap();
    private final Map<Structure, LongSet> structuresRefences = Maps.newHashMap();
    protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    protected final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    protected final LevelHeightAccessor levelHeightAccessor;
    protected final LevelChunkSection[] sections;

    // Constructor signature matches ChunkAccess for DASM redirect purposes
    public CubeAccess(
        CloPos cloPos,
        UpgradeData upgradeData,
        LevelHeightAccessor levelHeightAccessor,
        Registry<Biome> biomeRegistry,
        long inhabitedTime,
        @Nullable LevelChunkSection[] chunkSections,
        @Nullable BlendingData blendingData
    ) {
        this.cloPos = cloPos;
        this.upgradeData = upgradeData;
        this.levelHeightAccessor = levelHeightAccessor;
        this.sections = new LevelChunkSection[CubicConstants.SECTION_COUNT];
        this.inhabitedTime = inhabitedTime;
        this.postProcessing = new ShortList[CubicConstants.SECTION_COUNT];
        this.blendingData = blendingData;
        this.skyLightSources = new ChunkSkyLightSources(levelHeightAccessor);
        if (chunkSections != null) {
            if (this.sections.length == chunkSections.length) {
                System.arraycopy(chunkSections, 0, this.sections, 0, this.sections.length);
            } else {
                CubicChunks.LOGGER.warn("Could not set level cube sections, array length is {} instead of {}", chunkSections.length, this.sections.length);
            }
        }

        replaceMissingSections(biomeRegistry, this.sections);
    }

    private static void replaceMissingSections(Registry<Biome> biomeRegistry, LevelChunkSection[] sections) {
        for(int i = 0; i < sections.length; ++i) {
            if (sections[i] == null) {
                sections[i] = new LevelChunkSection(biomeRegistry);
            }
        }
    }

    @Override @Nullable public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving);

    @Override public abstract void setBlockEntity(BlockEntity blockEntity);

    @Override public abstract void addEntity(Entity entity);

    // Next two methods are used for vanilla heightmaps/lighting/end gateways. We shouldn't need these
    @Override public int getHighestFilledSectionIndex() {
        // TODO is there a better dummy value to return?
        return -1;
    }

    @Override public int getHighestSectionPosition() {
        return this.getMinBuildHeight();
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getBlockEntitiesPos()Ljava/util/Set;")
    @Override public native Set<BlockPos> getBlockEntitiesPos();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getSections()[Lnet/minecraft/world/level/chunk/LevelChunkSection;")
    @Override public native LevelChunkSection[] getSections();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getSection(I)Lnet/minecraft/world/level/chunk/LevelChunkSection;")
    @Override public native LevelChunkSection getSection(int index);

    // TODO (P2) heightmap methods on cubes
    @Override public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        throw new UnsupportedOperationException();
    }

    @Override public void setHeightmap(Heightmap.Types type, long[] data) {
    }

    @Override public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean hasPrimedHeightmap(Heightmap.Types type) {
        return false;
    }

    @Override public int getHeight(Heightmap.Types type, int x, int z) {
        return CubicChunks.SUPERFLAT_HEIGHT;
    }
    // end heightmaps

    @Override public CloPos cc_getCloPos() {
        return cloPos;
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getStartForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;)"
        + "Lnet/minecraft/world/level/levelgen/structure/StructureStart;")
    @Override @Nullable public native StructureStart getStartForStructure(Structure structure);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setStartForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;"
        + "Lnet/minecraft/world/level/levelgen/structure/StructureStart;)V")
    @Override public native void setStartForStructure(Structure structure, StructureStart structureStart);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getAllStarts()Ljava/util/Map;")
    @Override public native Map<Structure, StructureStart> getAllStarts();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setAllStarts(Ljava/util/Map;)V")
    @Override public native void setAllStarts(Map<Structure, StructureStart> structureStarts);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getReferencesForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;)"
        + "Lit/unimi/dsi/fastutil/longs/LongSet;")
    @Override public native LongSet getReferencesForStructure(Structure structure);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "addReferenceForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;J)V")
    @Override public native void addReferenceForStructure(Structure structure, long reference);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getAllReferences()Ljava/util/Map;")
    @Override public native Map<Structure, LongSet> getAllReferences();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setAllReferences(Ljava/util/Map;)V")
    @Override public native void setAllReferences(Map<Structure, LongSet> structureReferencesMap);

    @Override public boolean isYSpaceEmpty(int startY, int endY) {
        // TODO
        return false;
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setUnsaved(Z)V")
    @Override public native void setUnsaved(boolean unsaved);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "isUnsaved()Z")
    @Override public native boolean isUnsaved();

    @Override public abstract ChunkStatus getStatus();

    @Override public ChunkStatus getHighestGeneratedStatus() {
        // In ChunkAccess this method is only used for below-zero retrogen; with no retrogen it does this
        return this.getStatus();
    }

    @Override public abstract void removeBlockEntity(BlockPos pos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "markPosForPostprocessing(Lnet/minecraft/core/BlockPos;)V")
    @Override public native void markPosForPostprocessing(BlockPos pos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getPostProcessing()[Lit/unimi/dsi/fastutil/shorts/ShortList;")
    @Override public native ShortList[] getPostProcessing();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "addPackedPostProcess(SI)V")
    @Override public native void addPackedPostProcess(short packedPosition, int index);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setBlockEntityNbt(Lnet/minecraft/nbt/CompoundTag;)V")
    @Override public native void setBlockEntityNbt(CompoundTag tag);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getBlockEntityNbt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;")
    @Override @Nullable public native CompoundTag getBlockEntityNbt(BlockPos pos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;")
    @Override @Nullable public native CompoundTag getBlockEntityNbtForSaving(BlockPos pos);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "findBlockLightSources(Ljava/util/function/BiConsumer;)V")
    @Override public native void findBlockLightSources(BiConsumer<BlockPos, BlockState> output);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "findBlocks(Ljava/util/function/Predicate;Ljava/util/function/BiConsumer;)V")
    @Override public native void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output);

    @Override public void findBlocks(BiPredicate<BlockState, BlockPos> predicate, BiConsumer<BlockPos, BlockState> output) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int y = 0; y < CubicConstants.DIAMETER_IN_SECTIONS; y++) {
            for (int z = 0; z < CubicConstants.DIAMETER_IN_SECTIONS; z++) {
                for (int x = 0; x < CubicConstants.DIAMETER_IN_SECTIONS; x++) {
                    LevelChunkSection levelchunksection = this.getSection(Coords.sectionToIndex(x, y, z));
                    if (levelchunksection.maybeHas((state) -> predicate.test(state, BlockPos.ZERO))) {
                        BlockPos blockpos = this.cloPos.cubePos().asSectionPos().offset(x, y, z).origin();

                        for(int sectionLocalY = 0; sectionLocalY < SectionPos.SECTION_SIZE; ++sectionLocalY) {
                            for(int sectionLocalZ = 0; sectionLocalZ < SectionPos.SECTION_SIZE; ++sectionLocalZ) {
                                for(int sectionLocalX = 0; sectionLocalX < SectionPos.SECTION_SIZE; ++sectionLocalX) {
                                    BlockState blockstate = levelchunksection.getBlockState(sectionLocalX, sectionLocalY, sectionLocalZ);
                                    mutableBlockPos.setWithOffset(blockpos, sectionLocalX, sectionLocalY, sectionLocalZ);
                                    if (predicate.test(blockstate, mutableBlockPos.immutable())) {
                                        output.accept(mutableBlockPos, blockstate);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override public abstract TickContainerAccess<Block> getBlockTicks();

    @Override public abstract TickContainerAccess<Fluid> getFluidTicks();

    @Override public abstract ChunkAccess.TicksToSave getTicksForSerialization();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getUpgradeData()Lnet/minecraft/world/level/chunk/UpgradeData;")
    @Override public native UpgradeData getUpgradeData();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "isOldNoiseGeneration()Z")
    @Override public native boolean isOldNoiseGeneration();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getBlendingData()Lnet/minecraft/world/level/levelgen/blending/BlendingData;")
    @Override @Nullable public native BlendingData getBlendingData();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setBlendingData(Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V")
    @Override public native void setBlendingData(BlendingData blendingData);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getInhabitedTime()J")
    @Override public native long getInhabitedTime();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "incrementInhabitedTime(J)V")
    @Override public native void incrementInhabitedTime(long amount);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setInhabitedTime(J)V")
    @Override public native void setInhabitedTime(long inhabitedTime);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "isLightCorrect()Z")
    @Override public native boolean isLightCorrect();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "setLightCorrect(Z)V")
    @Override public native void setLightCorrect(boolean lightCorrect);

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getMinBuildHeight()I")
    @Override public native int getMinBuildHeight();

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getHeight()I")
    @Override public native int getHeight();

    @Override public NoiseChunk getOrCreateNoiseChunk(Function<CloAccess, NoiseChunk> noiseChunkCreator) {
        throw new UnsupportedOperationException(); // TODO P3
    }

    @Override public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> carverBiomeSettingsProvider) {
        throw new UnsupportedOperationException(); // TODO P3
    }

    @Override public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        throw new UnsupportedOperationException(); // TODO P3
    }

    @Override public void fillBiomesFromNoise(BiomeResolver resolver, Climate.Sampler sampler) {
        throw new UnsupportedOperationException(); // TODO P3
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "hasAnyStructureReferences()Z")
    @Override public native boolean hasAnyStructureReferences();

    @Override @Nullable public BelowZeroRetrogen getBelowZeroRetrogen() {
        return null; // No below-zero retrogen in cubic worlds :)
    }

    @Override public boolean isUpgrading() {
        return false; // Used for below-zero retrogen; not applicable to cubes
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = ChunkAccess.class), value = "getHeightAccessorForGeneration()Lnet/minecraft/world/level/LevelHeightAccessor;")
    @Override public native LevelHeightAccessor getHeightAccessorForGeneration();

    @Override public void initializeLightSources() {
        // TODO P2
    }

    @Override public ChunkSkyLightSources getSkyLightSources() {
        throw new UnsupportedOperationException(); // TODO P2
    }
}
