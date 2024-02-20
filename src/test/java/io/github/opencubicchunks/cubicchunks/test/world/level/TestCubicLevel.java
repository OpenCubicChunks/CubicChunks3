package io.github.opencubicchunks.cubicchunks.test.world.level;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import io.github.opencubicchunks.cubicchunks.world.level.CubicLevel;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubicChunkSource;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
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
            WritableLevelData p_270739_,
            ResourceKey<Level> p_270683_,
            RegistryAccess p_270200_,
            Holder<DimensionType> p_270240_,
            Supplier<ProfilerFiller> p_270692_,
            boolean p_270904_,
            boolean p_270470_,
            long p_270248_,
            int p_270466_
        ) {
            super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
            when(((CubicChunkSource)mockChunkSource).cc_getCube(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(mock(LevelCube.class));
            when(((CubicChunkSource)mockChunkSource).cc_getCube(anyInt(), anyInt(), anyInt(), any(), anyBoolean())).thenReturn(mock(LevelCube.class));
        }

        @Override public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_262953_, double p_263004_, double p_263398_, double p_263376_, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_,
                                    float p_262914_, long p_262991_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {

        }

        @Override public String gatherChunkSourceStats() {
            return null;
        }

        @Nullable @Override public Entity getEntity(int p_46492_) {
            return null;
        }

        @Override public TickRateManager tickRateManager() {
            return null;
        }

        @Nullable @Override public MapItemSavedData getMapData(String p_46650_) {
            return null;
        }

        @Override public void setMapData(String p_151533_, MapItemSavedData p_151534_) {

        }

        @Override public int getFreeMapId() {
            return 0;
        }

        @Override public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_) {

        }

        @Override public Scoreboard getScoreboard() {
            return null;
        }

        @Override public RecipeManager getRecipeManager() {
            return null;
        }

        @Override protected LevelEntityGetter<Entity> getEntities() {
            return null;
        }

        @Override public LevelTickAccess<Block> getBlockTicks() {
            return null;
        }

        @Override public LevelTickAccess<Fluid> getFluidTicks() {
            return null;
        }

        @Override public ChunkSource getChunkSource() {
            return mockChunkSource;
        }

        @Override public void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {

        }

        @Override public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_) {

        }

        @Override public float getShade(Direction p_45522_, boolean p_45523_) {
            return 0;
        }

        @Override public List<? extends Player> players() {
            return null;
        }

        @Override public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
            return null;
        }

        @Override public FeatureFlagSet enabledFeatures() {
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
        when(noiseBasedChunkGeneratorMock.createBiomes(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
        when(noiseBasedChunkGeneratorMock.fillFromNoise(any(),any(),any(),any(),any())).thenAnswer(i -> CompletableFuture.completedFuture(i.getArguments()[4]));
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
                mock(RETURNS_DEEP_STUBS),
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
