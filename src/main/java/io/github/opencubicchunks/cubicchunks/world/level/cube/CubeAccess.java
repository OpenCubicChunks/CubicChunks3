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
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.CubeAccessAndDescendantsSet;
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
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
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

@Dasm(CubeAccessAndDescendantsSet.class)
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
        for (int i = 0; i < sections.length; ++i) {
            if (sections[i] == null) {
                sections[i] = new LevelChunkSection(biomeRegistry);
            }
        }
    }

    @TransformFromMethod(
        value = @MethodSig("getListenerRegistry(I)Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native GameEventListenerRegistry getListenerRegistry(int sectionY);

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

    @TransformFromMethod(
        value = @MethodSig("getBlockEntitiesPos()Ljava/util/Set;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native Set<BlockPos> getBlockEntitiesPos();

    @TransformFromMethod(
        value = @MethodSig("getSections()[Lnet/minecraft/world/level/chunk/LevelChunkSection;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native LevelChunkSection[] getSections();

    @TransformFromMethod(
        value = @MethodSig("getSection(I)Lnet/minecraft/world/level/chunk/LevelChunkSection;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
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

    @TransformFromMethod(
        value = @MethodSig("getStartForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override @Nullable public native StructureStart getStartForStructure(Structure structure);

    @TransformFromMethod(
        value = @MethodSig("setStartForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setStartForStructure(Structure structure, StructureStart structureStart);

    @TransformFromMethod(
        value = @MethodSig("getAllStarts()Ljava/util/Map;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native Map<Structure, StructureStart> getAllStarts();

    @TransformFromMethod(
        value = @MethodSig("setAllStarts(Ljava/util/Map;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setAllStarts(Map<Structure, StructureStart> structureStarts);

    @TransformFromMethod(
        value = @MethodSig("getReferencesForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;)Lit/unimi/dsi/fastutil/longs/LongSet;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native LongSet getReferencesForStructure(Structure structure);

    @TransformFromMethod(
        value = @MethodSig("addReferenceForStructure(Lnet/minecraft/world/level/levelgen/structure/Structure;J)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void addReferenceForStructure(Structure structure, long reference);

    @TransformFromMethod(
        value = @MethodSig("getAllReferences()Ljava/util/Map;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native Map<Structure, LongSet> getAllReferences();

    @TransformFromMethod(
        value = @MethodSig("setAllReferences(Ljava/util/Map;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setAllReferences(Map<Structure, LongSet> structureReferencesMap);

    @Override public boolean isYSpaceEmpty(int startY, int endY) {
        // TODO
        return false;
    }

    @TransformFromMethod(
        value = @MethodSig("setUnsaved(Z)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setUnsaved(boolean unsaved);

    @TransformFromMethod(
        value = @MethodSig("isUnsaved()Z"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native boolean isUnsaved();

    @Override public abstract ChunkStatus getStatus();

    @Override public ChunkStatus getHighestGeneratedStatus() {
        // In ChunkAccess this method is only used for below-zero retrogen; with no retrogen it does this
        return this.getStatus();
    }

    @Override public abstract void removeBlockEntity(BlockPos pos);

    @TransformFromMethod(
        value = @MethodSig("markPosForPostprocessing(Lnet/minecraft/core/BlockPos;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void markPosForPostprocessing(BlockPos pos);

    @TransformFromMethod(
        value = @MethodSig("getPostProcessing()[Lit/unimi/dsi/fastutil/shorts/ShortList;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native ShortList[] getPostProcessing();

    @TransformFromMethod(
        value = @MethodSig("addPackedPostProcess(SI)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void addPackedPostProcess(short packedPosition, int index);

    @TransformFromMethod(
        value = @MethodSig("setBlockEntityNbt(Lnet/minecraft/nbt/CompoundTag;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setBlockEntityNbt(CompoundTag tag);

    @TransformFromMethod(
        value = @MethodSig("getBlockEntityNbt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override @Nullable public native CompoundTag getBlockEntityNbt(BlockPos pos);

    @TransformFromMethod(
        value = @MethodSig("getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override @Nullable public native CompoundTag getBlockEntityNbtForSaving(BlockPos pos);

    @TransformFromMethod(
        value = @MethodSig("findBlockLightSources(Ljava/util/function/BiConsumer;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void findBlockLightSources(BiConsumer<BlockPos, BlockState> output);

    @TransformFromMethod(
        value = @MethodSig("findBlocks(Ljava/util/function/Predicate;Ljava/util/function/BiConsumer;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void findBlocks(Predicate<BlockState> predicate, BiConsumer<BlockPos, BlockState> output);

    @Override public void findBlocks(BiPredicate<BlockState, BlockPos> predicate, BiConsumer<BlockPos, BlockState> output) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int y = 0; y < CubicConstants.DIAMETER_IN_SECTIONS; y++) {
            for (int z = 0; z < CubicConstants.DIAMETER_IN_SECTIONS; z++) {
                for (int x = 0; x < CubicConstants.DIAMETER_IN_SECTIONS; x++) {
                    LevelChunkSection levelchunksection = this.getSection(Coords.sectionToIndex(x, y, z));
                    if (levelchunksection.maybeHas((state) -> predicate.test(state, BlockPos.ZERO))) {
                        BlockPos blockpos = this.cloPos.cubePos().asSectionPos().offset(x, y, z).origin();

                        for (int sectionLocalY = 0; sectionLocalY < SectionPos.SECTION_SIZE; ++sectionLocalY) {
                            for (int sectionLocalZ = 0; sectionLocalZ < SectionPos.SECTION_SIZE; ++sectionLocalZ) {
                                for (int sectionLocalX = 0; sectionLocalX < SectionPos.SECTION_SIZE; ++sectionLocalX) {
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

    @TransformFromMethod(
        value = @MethodSig("getUpgradeData()Lnet/minecraft/world/level/chunk/UpgradeData;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native UpgradeData getUpgradeData();

    @TransformFromMethod(
        value = @MethodSig("isOldNoiseGeneration()Z"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native boolean isOldNoiseGeneration();

    @TransformFromMethod(
        value = @MethodSig("getBlendingData()Lnet/minecraft/world/level/levelgen/blending/BlendingData;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override @Nullable public native BlendingData getBlendingData();

    @TransformFromMethod(
        value = @MethodSig("setBlendingData(Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setBlendingData(BlendingData blendingData);

    @TransformFromMethod(
        value = @MethodSig("getInhabitedTime()J"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native long getInhabitedTime();

    @TransformFromMethod(
        value = @MethodSig("incrementInhabitedTime(J)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void incrementInhabitedTime(long amount);

    @TransformFromMethod(
        value = @MethodSig("setInhabitedTime(J)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setInhabitedTime(long inhabitedTime);

    @TransformFromMethod(
        value = @MethodSig("getOrCreateOffsetList([Lit/unimi/dsi/fastutil/shorts/ShortList;I)Lit/unimi/dsi/fastutil/shorts/ShortList;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    public static native ShortList getOrCreateOffsetList(ShortList[] packedPositions, int index);

    @TransformFromMethod(
        value = @MethodSig("isLightCorrect()Z"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native boolean isLightCorrect();

    @TransformFromMethod(
        value = @MethodSig("setLightCorrect(Z)V"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native void setLightCorrect(boolean lightCorrect);

    @TransformFromMethod(
        value = @MethodSig("getMinBuildHeight()I"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native int getMinBuildHeight();

    @TransformFromMethod(
        value = @MethodSig("getHeight()I"),
        copyFrom = @Ref(ChunkAccess.class)
    )
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

    @TransformFromMethod(
        value = @MethodSig("hasAnyStructureReferences()Z"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native boolean hasAnyStructureReferences();

    @Override @Nullable public BelowZeroRetrogen getBelowZeroRetrogen() {
        return null; // No below-zero retrogen in cubic worlds :)
    }

    @Override public boolean isUpgrading() {
        return false; // Used for below-zero retrogen; not applicable to cubes
    }

    @TransformFromMethod(
        value = @MethodSig("getHeightAccessorForGeneration()Lnet/minecraft/world/level/LevelHeightAccessor;"),
        copyFrom = @Ref(ChunkAccess.class)
    )
    @Override public native LevelHeightAccessor getHeightAccessorForGeneration();

    @Override public void initializeLightSources() {
        // TODO P2
    }

    @Override public ChunkSkyLightSources getSkyLightSources() {
        throw new UnsupportedOperationException(); // TODO P2
    }
}
