package io.github.opencubicchunks.cubicchunks.test.world.level.cube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubeAccess;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubeAccess {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    static class CubeAccessTestImpl extends CubeAccess {
        public CubeAccessTestImpl(CloPos cloPos, UpgradeData upgradeData,
                                  LevelHeightAccessor levelHeightAccessor, Registry<Biome> biomeRegistry,
                                  long inhabitedTime, @Nullable LevelChunkSection[] chunkSections,
                                  @Nullable BlendingData blendingData) {
            super(cloPos, upgradeData, levelHeightAccessor, biomeRegistry, inhabitedTime, chunkSections, blendingData);
        }

        @Override public GameEventListenerRegistry getListenerRegistry(int p_251437_) {
            return null;
        }

        @Override public @Nullable BlockState setBlockState(BlockPos blockPos, BlockState state, boolean unused) {
            int sectionIndex = Coords.blockToIndex(blockPos);
            int localX = Coords.blockToSectionLocal(blockPos.getX());
            int localY = Coords.blockToSectionLocal(blockPos.getY());
            int localZ = Coords.blockToSectionLocal(blockPos.getZ());
            return this.sections[sectionIndex].setBlockState(localX, localY, localZ, state);
        }

        @Override public void setBlockEntity(BlockEntity p_156114_) {

        }

        @Override public void addEntity(Entity p_62078_) {

        }

        @Override public ChunkStatus getStatus() {
            return null;
        }

        @Override public void removeBlockEntity(BlockPos p_62101_) {

        }

        @Override public TickContainerAccess<Block> getBlockTicks() {
            return null;
        }

        @Override public TickContainerAccess<Fluid> getFluidTicks() {
            return null;
        }

        @Override public ChunkAccess.TicksToSave getTicksForSerialization() {
            return null;
        }

        @Nullable @Override public BlockEntity getBlockEntity(BlockPos p_45570_) {
            return null;
        }

        @Override public BlockState getBlockState(BlockPos p_45571_) {
            return null;
        }

        @Override public FluidState getFluidState(BlockPos p_45569_) {
            return null;
        }
    }

    private void findBlocks(Random random) {
        CloPos cubePos = CloPos.cube(random.nextInt(20000)-10000, random.nextInt(20000)-10000, random.nextInt(20000)-10000);
        var cubeAccess = new CubeAccessTestImpl(cubePos, mock(), mock(), mock(), 0L, new LevelChunkSection[CubicConstants.SECTION_COUNT], mock());
        Set<BlockPos> visitedPositions = new HashSet<>();
        Set<BlockPos> expectedPositions = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            BlockPos pos = cubePos.cubePos().asBlockPos(random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS), random.nextInt(CubicConstants.DIAMETER_IN_BLOCKS));
            if (!visitedPositions.add(pos)) continue;
            if (random.nextBoolean()) {
                cubeAccess.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                expectedPositions.add(pos);
            } else {
                cubeAccess.setBlockState(pos, Blocks.DIRT.defaultBlockState(), false);
            }
        }
        Set<BlockPos> foundPositions = new HashSet<>();
        cubeAccess.findBlocks((state, pos) -> state == Blocks.STONE.defaultBlockState(), (pos, state) -> foundPositions.add(new BlockPos(pos)));
        assertEquals(expectedPositions, foundPositions);
    }

    @Test public void testFindBlocks() {
        var random = new Random(-99);
        for (int i = 0; i < 100; i++) {
            findBlocks(random);
        }
    }
}
