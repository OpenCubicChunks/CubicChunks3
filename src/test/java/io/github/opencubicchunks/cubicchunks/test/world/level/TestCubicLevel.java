package io.github.opencubicchunks.cubicchunks.test.world.level;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeSource;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Tests for {@link CubicLevel}.
 *
 * Currently only tests that the methods exist and don't throw exceptions or hang.
 *
 * The unit tests will not be further developed. We are just going to integration test this class once we have enough working functionality elsewhere.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicLevel extends BaseTest {
    public static class TestLevel extends Level {
        ChunkSource mockChunkSource = mock(ChunkSource.class, RETURNS_DEEP_STUBS);

        public TestLevel(
            WritableLevelData levelData,
            ResourceKey<Level> dimension,
            RegistryAccess registryAccess,
            Holder<DimensionType> dimensionTypeRegistration,
            boolean isClientSide,
            boolean isDebug,
            long biomeZoomSeed,
            int maxChainedNeighborUpdates
        ) {
            super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
            when(((CubeSource)mockChunkSource).cc_getCube(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(mock(LevelCube.class));
            when(((CubeSource)mockChunkSource).cc_getCube(anyInt(), anyInt(), anyInt(), any(), anyBoolean())).thenReturn(mock(LevelCube.class));
        }

        @Override public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

        }

        @Override public void playSeededSound(@Nullable Entity entity, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {

        }

        @Override public void playSeededSound(@Nullable Entity entity, Entity sourceEntity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {

        }

        @Override
        public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius,
                            boolean fire, ExplosionInteraction explosionInteraction, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles,
                            Holder<SoundEvent> explosionSound) {

        }

        @Override public String gatherChunkSourceStats() {
            return "";
        }

        @Override public @Nullable Entity getEntity(int id) {
            return null;
        }

        @Override public Collection<PartEntity<?>> dragonParts() {
            return List.of();
        }

        @Override public TickRateManager tickRateManager() {
            return null;
        }

        @Override public @Nullable MapItemSavedData getMapData(MapId mapId) {
            return null;
        }

        @Override public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {

        }

        @Override public Scoreboard getScoreboard() {
            return null;
        }

        @Override public RecipeAccess recipeAccess() {
            return null;
        }

        @Override protected LevelEntityGetter<Entity> getEntities() {
            return null;
        }

        @Override public PotionBrewing potionBrewing() {
            return null;
        }

        @Override public FuelValues fuelValues() {
            return null;
        }

        @Override public void setDayTimeFraction(float dayTimeFraction) {

        }

        @Override public float getDayTimeFraction() {
            return 0;
        }

        @Override public float getDayTimePerTick() {
            return 0;
        }

        @Override public void setDayTimePerTick(float dayTimePerTick) {

        }

        @Override public ChunkSource getChunkSource() {
            return mockChunkSource;
        }

        @Override public void levelEvent(@Nullable Entity entity, int type, BlockPos pos, int data) {

        }

        @Override public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {

        }

        @Override public List<? extends Player> players() {
            return List.of();
        }

        @Override public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
            return null;
        }

        @Override public int getSeaLevel() {
            return 0;
        }

        @Override public FeatureFlagSet enabledFeatures() {
            return null;
        }

        @Override public float getShade(Direction direction, boolean shade) {
            return 0;
        }

        @Override public LevelTickAccess<Block> getBlockTicks() {
            return null;
        }

        @Override public LevelTickAccess<Fluid> getFluidTicks() {
            return null;
        }

        // This overrides CubicLevel.hasCube
        @SuppressWarnings("unused") public boolean cc_hasCube(int x, int y, int z) {
            return true;
        }
    }

    private CloseableReference<TestLevel> setupTestLevel() {
        MockedStatic<RandomState> randomStateMockedStatic = Mockito.mockStatic(RandomState.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        ChunkGenerator noiseBasedChunkGeneratorMock = mock(ChunkGenerator.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        when(noiseBasedChunkGeneratorMock.createBiomes(any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[3]));
        when(noiseBasedChunkGeneratorMock.fillFromNoise(any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[3]));
        LevelStem levelStemMock = mock(RETURNS_DEEP_STUBS);
        when(levelStemMock.type().value().height()).thenReturn(384);
        LevelStorageSource.LevelStorageAccess levelStorageAccessMock = mock(RETURNS_DEEP_STUBS);
        try {
            when(levelStorageAccessMock.getDimensionPath(any())).thenReturn(Files.createTempDirectory("cc_test"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Holder holderMock = mock(Holder.class, RETURNS_DEEP_STUBS);
        when(holderMock.unwrapKey()).thenReturn(Optional.of(ResourceKey.create(mock(), mock())));
        when(holderMock.value()).thenReturn(mock(DimensionType.class));
        return new CloseableReference<>(
            new TestLevel(mock(RETURNS_DEEP_STUBS),
                mock(RETURNS_DEEP_STUBS),
                mock(RETURNS_DEEP_STUBS),
                holderMock,
                false,
                false,
                0,
                0),
            randomStateMockedStatic);
    }

    @Test public void testGetCubeAt() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic) testLevelReference.value()).cc_setCubic();
            ((CubicLevel) testLevelReference.value()).cc_getCubeAt(new BlockPos(0, 0, 0));
        }
    }

    @Test public void testGetCube() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic) testLevelReference.value()).cc_setCubic();
            ((CubicLevel) testLevelReference.value()).cc_getCube(0, 0, 0);
        }
    }

    @Test public void testGetCubeCubeAccess() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic) testLevelReference.value()).cc_setCubic();
            ((CubicLevel) testLevelReference.value()).cc_getCube(0, 0, 0, ChunkStatus.FULL, true);
        }
    }

    @Test public void testSetBlock() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).setBlock(new BlockPos(0, 0, 0), mock(BlockState.class), 0);
        }
    }

    @Test public void testGetBlockState() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).getBlockState(new BlockPos(0, 0, 0));
        }
    }

    @Test public void testGetBlockEntity() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).getBlockEntity(new BlockPos(0, 0, 0));
        }
    }

    @Test public void testGetFluidState() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).getFluidState(new BlockPos(0, 0, 0));
        }
    }

    @Test public void testSetBlockEntity() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).setBlockEntity(mock(BlockEntity.class, RETURNS_DEEP_STUBS));
        }
    }

    @Test public void testRemoveBlockEntity() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).removeBlockEntity(new BlockPos(0, 0, 0));
        }
    }

    @Test public void testIsLoaded() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic)testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).isLoaded(new BlockPos(0, 0, 0));
        }
    }

    @Test public void testLoadedAndEntityCanStandOnFace() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic) testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).loadedAndEntityCanStandOnFace(new BlockPos(0, 0, 0), mock(Entity.class), Direction.UP);
        }
    }

    @Test public void testBlockEntityChanged() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic) testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).blockEntityChanged(new BlockPos(0, 0, 0));
        }
    }

    @Test public void getCurrentDifficultyAt() throws Exception {
        try (CloseableReference<TestLevel> testLevelReference = setupTestLevel()) {
            ((MarkableAsCubic) testLevelReference.value()).cc_setCubic();
            (testLevelReference.value()).getCurrentDifficultyAt(new BlockPos(0, 0, 0));
        }
    }
}
