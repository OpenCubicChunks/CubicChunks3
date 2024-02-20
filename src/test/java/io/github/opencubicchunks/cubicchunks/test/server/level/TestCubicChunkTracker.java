package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.ChunkTrackerTestAccess;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.testutils.Misc;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.server.level.ChunkTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * This test class is for testing {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinChunkTracker}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicChunkTracker extends BaseTest {
    static class TestCubicTracker extends ChunkTracker {
        protected final int maxLevel;
        protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
        private final Long2ByteMap sources = new Long2ByteOpenHashMap();

        protected TestCubicTracker(int levelCount, int expectedUpdatesByLevel, int expectedPropagationLevels) {
            super(levelCount, expectedUpdatesByLevel, expectedPropagationLevels);
            this.maxLevel = levelCount-1;
            this.chunks.defaultReturnValue((byte) (levelCount-1));
        }

        @Override protected int getLevelFromSource(long pos) {
            return sources.containsKey(pos) ? sources.get(pos) : Integer.MAX_VALUE;
        }

        @Override protected int getLevel(long pos) {
            return chunks.get(pos);
        }

        @Override protected void setLevel(long pos, int level) {
            ((ChunkTrackerTestAccess) this).invoke_cc_onSetLevel(pos, level);
            if (level >= maxLevel) chunks.remove(pos);
            else chunks.put(pos, (byte) level);
        }

        public void runAllUpdates() {
            super.runUpdates(Integer.MAX_VALUE);
        }

        public void addSource(long pos, int level) {
            this.sources.put(pos, (byte) level);
            this.update(pos, level, true);
        }

        public void removeSource(long pos) {
            this.sources.remove(pos);
            this.update(pos, maxLevel+1, false);
        }
    }

    private void addAndTestCubeSources(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand, int numSourcesAdded) {
        if (numSourcesAdded == 0) {
            for (int i = 0; i < numSourcesAdded; i++) {
                srcPosClo.add(CloPos.cube(rand.nextInt(0, 20)-10, rand.nextInt(0, 20)-10, rand.nextInt(0, 20)-10));
                tracker.addSource(srcPosClo.get(srcPosClo.size() - 1).asLong(), 0);
            }
            tracker.runAllUpdates();
        }
        for (int i = 0; i < srcPosClo.size(); i++) {
            assertEquals(0, tracker.getLevel(srcPosClo.get(i).asLong()), "Level should be zero at source cube.");
        }
    }

    private void removeAndTestCubeSources(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand, float chanceToRemove) {
        if (chanceToRemove != 0.0f) {
            List<Integer> srcPosCloToRemove = new ArrayList<Integer>();
            for (int i = 0; i < srcPosClo.size(); i++) {
                if (rand.nextFloat() < chanceToRemove) {
                    srcPosCloToRemove.add(i);
                    tracker.removeSource(srcPosClo.get(i).asLong());
                }
            }
            for (int i = 0; i < srcPosCloToRemove.size(); i++) {
                srcPosClo.remove((int)srcPosCloToRemove.get(i));
            }
            tracker.runAllUpdates();
        }
        for (int i = 0; i < srcPosClo.size(); i++) {
            assertEquals(0, tracker.getLevel(srcPosClo.get(i).asLong()), "Level should be zero at source cube.");
        }
    }

    private void testSourceCubePropagation(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand) {
        for (int i = 0; i < 100; i++) {
            var testPos = CubePos.of(rand.nextInt(0, 20)-10, rand.nextInt(0, 20)-10, rand.nextInt(0, 20)-10);
            // Minimum distance from all sources should be the level
            var dist = Integer.MAX_VALUE;
            for (int j = 0; j < srcPosClo.size(); j++) {
                dist = Math.min(dist, Misc.chebyshevDistance(srcPosClo.get(j).cubePos(), testPos));
            }
            assertEquals(Math.min(7, dist), tracker.getLevel(CloPos.cube(testPos).asLong()),
                String.format("Level at cube %d %d %d", testPos.getX(), testPos.getY(), testPos.getZ()));
        }
    }

    private void testSourceColumnPropagation(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand) {
        var srcPosY0 = new CubePos[srcPosClo.size()];
        for (int i = 0; i < srcPosClo.size(); i++) {
            srcPosY0[i] = CubePos.of(srcPosClo.get(i).getX(), 0, srcPosClo.get(i).getZ());
        }
        for (int i = 0; i < 100; i++) {
            var testPos = CloPos.cube(rand.nextInt(0, 20) - 10, 0, rand.nextInt(0, 20) - 10);
            var testCol = testPos.correspondingChunkCloPos(rand.nextInt(0, CubicConstants.DIAMETER_IN_SECTIONS), rand.nextInt(0, CubicConstants.DIAMETER_IN_SECTIONS));
            // Minimum distance from all sources should be the level
            var dist = Integer.MAX_VALUE;
            for (int j = 0; j < srcPosClo.size(); j++) {
                dist = Math.min(dist, Misc.chebyshevDistance(srcPosY0[j], testPos.cubePos()));
            }
            assertEquals(Math.min(7, dist), tracker.getLevel(testCol.asLong()), String.format("Level at chunk %d %d.", testCol.getX(), testCol.getZ()));
        }
    }

    private void testSourceCubeIntersection(List<CloPos> srcPosClo, TestCubicTracker tracker) {
        for (int i = 0; i < srcPosClo.size(); i++) {
            for (int dx = 0; dx < CubicConstants.DIAMETER_IN_SECTIONS; dx++) {
                for (int dz = 0; dz < CubicConstants.DIAMETER_IN_SECTIONS; dz++) {
                    var srcChunk = srcPosClo.get(i).correspondingChunkCloPos(dx, dz);
                    assertEquals(0, tracker.getLevel(srcChunk.asLong()), "Level should be zero at chunks intersecting source cube.");
                }
            }
        }
    }

    public void testIntersectionAndPropagation(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand) {
        testSourceCubeIntersection(srcPosClo, tracker);
        testSourceCubePropagation(srcPosClo, tracker, rand);
        testSourceColumnPropagation(srcPosClo, tracker, rand);
    }

    public void testCubeSources(int initialSourceCount, int numPhases, int seed, float chanceToRemovePerPhase, int numSourcesToAddPerPhase) {
        var tracker = new TestCubicTracker(8, 16, 256);
        ((MarkableAsCubic) tracker).cc_setCubic();
        var rand = new Random(seed);
        var srcPosClo = new ArrayList<CloPos>();
        addAndTestCubeSources(srcPosClo, tracker, rand, initialSourceCount);

        for(int i = 0; i < numPhases; i++) {
            testIntersectionAndPropagation(srcPosClo, tracker, rand);
            if(numPhases % 2 == 0) {
                removeAndTestCubeSources(srcPosClo, tracker, rand, chanceToRemovePerPhase);
            } else {
                addAndTestCubeSources(srcPosClo, tracker, rand, numSourcesToAddPerPhase);
            }
        }

        for (int i = 0; i < srcPosClo.size(); i++) {
            tracker.removeSource(srcPosClo.get(i).asLong());
        }
        tracker.runAllUpdates();

        assertThat(tracker.chunks).withFailMessage("Tracker should be empty after removing all sources.").isEmpty();
    }

    @Test public void testSingleCubeSource() {
        testCubeSources(1, 1, 333, 0, 0);
    }

    @Test public void testMultipleCubeSources() {
        testCubeSources(10, 4, 666, 0.5f, 5);
    }

    /**
     * This scenario tests an implementation detail, but it is important
     * that {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinChunkTracker#cc_existingCubesForCubeColumns} is updated correctly
     * to prevent memory leaks.
     * <br><br>
     * May have to be updated if the implementation of MixinChunkTracker changes.
     */
    @Test public void testExistingCubesForCubeColumns() {
        var tracker = new TestCubicTracker(8, 16, 256);
        ((MarkableAsCubic) tracker).cc_setCubic();
        var existingCubesForCubeColumns = ((ChunkTrackerTestAccess) tracker).get_cc_existingCubesForCubeColumns();
        var srcPos = CloPos.cube(1, 0, 0);
        tracker.addSource(srcPos.asLong(), 0);
        tracker.runAllUpdates();
        assertThat(existingCubesForCubeColumns.size()).isEqualTo(13*13);
        var srcPos2 = CloPos.cube(1, 1, 0);
        tracker.addSource(srcPos2.asLong(), 0);
        tracker.runAllUpdates();
        assertThat(existingCubesForCubeColumns.size()).isEqualTo(13*13);
        var srcPos3 = CloPos.cube(1, 1, 0);
        var srcPos4 = CloPos.cube(2, 1, 0);
        tracker.addSource(srcPos3.asLong(), 0);
        tracker.addSource(srcPos4.asLong(), 0);
        tracker.runAllUpdates();
        assertThat(existingCubesForCubeColumns.size()).isEqualTo(14*13);
        tracker.removeSource(srcPos.asLong());
        tracker.removeSource(srcPos2.asLong());
        tracker.removeSource(srcPos3.asLong());
        tracker.runAllUpdates();
        assertThat(existingCubesForCubeColumns.size()).isEqualTo(13*13);
        tracker.removeSource(srcPos4.asLong());
        tracker.runAllUpdates();
        assertThat(existingCubesForCubeColumns).withFailMessage("Tracker should be empty after removing all sources.").isEmpty();
    }

    /**
     * This scenario tests a single chunk source. Chunk sources won't be allowed in future implementations, but it is good to make sure they don't break everything.
     * <br><br>
     * // TODO: A chunk should never be allowed to be a source. It should be an assert in the future.
     */
    @Test public void testSingleChunkSource() {
        var tracker = new TestCubicTracker(8, 16, 256);
        ((MarkableAsCubic) tracker).cc_setCubic();
        var srcPos = CloPos.chunk(-4, 3);
        tracker.addSource(srcPos.asLong(), 0);
        tracker.runAllUpdates();
        var rand = new Random(333);
        assertEquals(0, tracker.getLevel(srcPos.asLong()), "Level should be zero at source chunk.");
        assertThat(tracker.chunks.keySet()).withFailMessage("No cubes should be loaded by columns.").allSatisfy(CloPos::isChunk);
        for (int i = 0; i < 1000; i++) {
            var testPos = CloPos.chunk(srcPos.getX() + rand.nextInt(0, 20) - 10, srcPos.getZ() + rand.nextInt(0, 20) - 10);
            var dist = Misc.chebyshevDistance(srcPos.chunkPos(), testPos.chunkPos());
            assertEquals(Math.min(7, dist), tracker.getLevel(testPos.asLong()), String.format("Level at chunk %d %d.", testPos.getX(), testPos.getZ()));
        }
        tracker.removeSource(srcPos.asLong());
        tracker.runAllUpdates();
        assertThat(tracker.chunks.keySet()).withFailMessage("Tracker should be empty after removing all sources.").isEmpty();
    }

    private void addAndTestVanillaChunkSources(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand, int numSourcesAdded) {
        if (numSourcesAdded == 0) {
            for (int i = 0; i < numSourcesAdded; i++) {
                srcPosClo.add(CloPos.chunk(rand.nextInt(0, 20)-10, rand.nextInt(0, 20)-10));
                tracker.addSource(srcPosClo.get(srcPosClo.size() - 1).toLong(), 0);
            }
            tracker.runAllUpdates();
        }
        for (int i = 0; i < srcPosClo.size(); i++) {
            assertEquals(0, tracker.getLevel(srcPosClo.get(i).toLong()), "Level should be zero at source chunk.");
        }
    }

    private void removeAndTestVanillaChunkSources(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand, float chanceToRemove) {
        if (chanceToRemove == 0.0f) {
            List<Integer> srcPosToRemove = new ArrayList<Integer>();
            for (int i = 0; i < srcPosClo.size(); i++) {
                if (rand.nextFloat() < chanceToRemove) {
                    srcPosToRemove.add(i);
                    tracker.removeSource(srcPosClo.get(i).toLong());
                }
            }
            for (int i = 0; i < srcPosToRemove.size(); i++) {
                srcPosClo.remove((int)srcPosToRemove.get(i));
            }
            tracker.runAllUpdates();
        }
        for (int i = 0; i < srcPosClo.size(); i++) {
            assertEquals(0, tracker.getLevel(srcPosClo.get(i).toLong()), "Level should be zero at source chunk.");
        }
    }

    private void testVanillaChunkSourcePropagation(List<CloPos> srcPosClo, TestCubicTracker tracker, Random rand) {
        for (int i = 0; i < 1000; i++) {
            var testPos = CloPos.chunk(rand.nextInt(0, 20) - 10, rand.nextInt(0, 20) - 10).chunkPos();
            var dist = Integer.MAX_VALUE;
            for (int j = 0; j < srcPosClo.size(); j++) {
                dist = Math.min(dist, Misc.chebyshevDistance(srcPosClo.get(j).chunkPos(), testPos));
            }
            assertEquals(Math.min(7, dist), tracker.getLevel(testPos.toLong()), String.format("Level at chunk %d %d.", testPos.x, testPos.z));
        }
    }

    /**
     * We need this test to ensure we don't break vanilla functionality.
     */
    private void testChunkSourcesVanilla(int initialSourceCount, int numPhases, int seed, float chanceToRemovePerPhase, int numSourcesToAddPerPhase) {
        var tracker = new TestCubicTracker(8, 16, 256);
        var rand = new Random(seed);
        var srcPosClo = new ArrayList<CloPos>(initialSourceCount);

        for(int i = 0; i < numPhases; i++) {
            testVanillaChunkSourcePropagation(srcPosClo, tracker, rand);
            if(initialSourceCount == 1) {
                if(numPhases % 2 == 0) {
                    removeAndTestVanillaChunkSources(srcPosClo, tracker, rand, chanceToRemovePerPhase);
                } else {
                    addAndTestVanillaChunkSources(srcPosClo, tracker, rand, numSourcesToAddPerPhase);
                }
            }
        }
    }

    @Test public void testSingleChunkSourceVanilla() {
        testChunkSourcesVanilla(1, 1, 727, 0.5f, 5);
    }

    @Test public void testMultipleChunkSourcesVanilla() {
        testChunkSourcesVanilla(10, 4, 999, 0.5f, 5);
    }
}
