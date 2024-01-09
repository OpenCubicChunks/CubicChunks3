package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.CubicDistanceManagerTestAccess;
import io.github.opencubicchunks.cubicchunks.server.level.CubicDistanceManager;
import io.github.opencubicchunks.cubicchunks.server.level.CubicTicketType;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinDistanceManager} and {@link io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinPlayerTicketTracker}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCubicDistanceManager {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    static class TestDistanceManager extends DistanceManager {
        public TestDistanceManager(Executor executor, Executor executor2) {
            super(executor, executor2);
        }

        @Override
        protected boolean isChunkToRemove(long p_140462_) {
            return false;
        }

        @Nullable
        @Override
        protected ChunkHolder getChunk(long p_140469_) { return null; }
        @Nullable
        @Override
        protected ChunkHolder updateChunkScheduling(long p_140464_, int p_140465_, @Nullable ChunkHolder p_140466_, int p_140467_) { return null; }
    }

    static class ServerPlayerAndPosition {
        ServerPlayer player;
        SectionPos pos;

        ServerPlayerAndPosition(SectionPos pos, ServerPlayer player) {
            this.pos = pos;
            this.player = player;
        }
    }

    private DistanceManager setupDistanceManager() {
        var executor = Util.backgroundExecutor();
        var mainThread = Thread.currentThread();
        var mainThreadExecutor = // Based on ServerChunkCache.MainThreadExecutor
            new BlockableEventLoop<>("test_event_loop") {
                @Override
                protected Runnable wrapRunnable(Runnable p_8506_) {
                    return p_8506_;
                }

                @Override
                protected boolean shouldRun(Runnable p_8504_) {
                    return true;
                }

                @Override
                protected boolean scheduleExecutables() {
                    return true;
                }

                @Override
                protected Thread getRunningThread() {
                    return mainThread;
                }
            };
        DistanceManager distanceManager = new TestDistanceManager(executor, mainThreadExecutor);
        return distanceManager;
    }

    @Test public void testUpdateChunkForcedVanilla() {
        DistanceManager distanceManager = setupDistanceManager();
        ((CubicDistanceManagerTestAccess) distanceManager).invoke_updateChunkForced(ChunkPos.ZERO, true);
        distanceManager.runAllUpdates(mock(ChunkMap.class));
        assertTrue(distanceManager.inEntityTickingRange(ChunkPos.ZERO.toLong()), "ChunkPos.ZERO is not in entity ticking range");
    }

    @Test public void testUpdateChunkForced() {
        DistanceManager distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicDistanceManagerTestAccess)distanceManager).invoke_updateCubeForced(CloPos.cube(0, 0, 0), true);
        distanceManager.runAllUpdates(mock(ChunkMap.class));
        assertTrue(distanceManager.inEntityTickingRange(CloPos.cube(0, 0, 0).toLong()), "CloPos.ZERO is not in entity ticking range");
    }

    private void addAndTestPlayersVanilla(DistanceManager distanceManager, List<ServerPlayerAndPosition> players, int count, Random rand) {
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                ServerPlayer player = mock(ServerPlayer.class);
                SectionPos pos = SectionPos.of(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
                distanceManager.addPlayer(pos, player);
                players.add(new ServerPlayerAndPosition(pos, player));
            }
        }

        for (ServerPlayerAndPosition player : players) {
            assertTrue(distanceManager.hasPlayersNearby(player.pos.chunk().toLong()), player.pos.chunk().toLong() + " has no players nearby");
        }
    }

    private void removeAndTestPlayersVanilla(DistanceManager distanceManager, List<ServerPlayerAndPosition> players, float chanceToRemove, Random rand) {
        if (chanceToRemove != 0) {
            List<ServerPlayerAndPosition> playersToRemove = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                if (Math.random() < chanceToRemove) {
                    distanceManager.removePlayer(players.get(i).pos, players.get(i).player);
                    playersToRemove.add(players.get(i));
                }
            }

            players.removeAll(playersToRemove);
        }

        for (ServerPlayerAndPosition player : players) {
            assertTrue(distanceManager.hasPlayersNearby(player.pos.chunk().toLong()), player.pos.chunk().toLong() + " has no players nearby");
        }
    }

    private void testAddAndRemoveVanilla(DistanceManager distanceManager, int iterations, int numToAdd, float chanceToRemove) {
        Random rand = new Random(727);
        List<ServerPlayerAndPosition> players = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            if (i % 2 == 0) {
                addAndTestPlayersVanilla(distanceManager, players, numToAdd, rand);
            }
            else {
                removeAndTestPlayersVanilla(distanceManager, players, chanceToRemove, rand);
            }
        }
    }

    private void addAndTestPlayers(DistanceManager distanceManager, List<ServerPlayerAndPosition> players, int count, Random rand) {
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                ServerPlayer player = mock(ServerPlayer.class);
                SectionPos pos = SectionPos.of(rand.nextInt(100), rand.nextInt(100), rand.nextInt(100));
                distanceManager.addPlayer(pos, player);
                players.add(new ServerPlayerAndPosition(pos, player));
            }
        }

        for (ServerPlayerAndPosition player : players) {
            assertTrue(distanceManager.hasPlayersNearby(CloPos.section(player.pos).toLong()), CloPos.section(player.pos).toLong() + " has no players nearby");
        }
    }

    private void removeAndTestPlayers(DistanceManager distanceManager, List<ServerPlayerAndPosition> players, float chanceToRemove, Random rand) {
        if (chanceToRemove != 0) {
            List<ServerPlayerAndPosition> playersToRemove = new ArrayList<>();
            for (int i = 0; i < players.size(); i++) {
                if (Math.random() < chanceToRemove) {
                    distanceManager.removePlayer(players.get(i).pos, players.get(i).player);
                    playersToRemove.add(players.get(i));
                }
            }

            players.removeAll(playersToRemove);
        }

        for (ServerPlayerAndPosition player : players) {
            assertTrue(distanceManager.hasPlayersNearby(CloPos.section(player.pos).toLong()), CloPos.section(player.pos).toLong() + " has no players nearby");
        }
    }

    private void testAddAndRemove(DistanceManager distanceManager, int iterations, int numToAdd, float chanceToRemove) {
        Random rand = new Random(727);
        List<ServerPlayerAndPosition> players = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            if (i % 2 == 0) {
                addAndTestPlayers(distanceManager, players, numToAdd, rand);
            }
            else {
                removeAndTestPlayers(distanceManager, players, chanceToRemove, rand);
            }
        }
    }

    @Test public void testSinglePlayerVanilla() {
        testAddAndRemoveVanilla(setupDistanceManager(), 2, 1, 0.0f);
    }

    @Test public void testMultiplePlayersVanilla() {
        testAddAndRemoveVanilla(setupDistanceManager(), 100, 200, 0.5f);
    }

    @Test public void testSinglePlayer() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        testAddAndRemove(distanceManager, 2, 1, 0.0f);
    }

    @Test public void testMultiplePlayers() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        testAddAndRemove(distanceManager, 50, 50, 0.5f);
    }

    @Test public void testRemoveTicketsOnClosingVanilla() {
        var distanceManager = setupDistanceManager();
        distanceManager.addTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE);
        assertTrue(distanceManager.hasTickets());
        distanceManager.removeTicketsOnClosing();
        assertFalse(distanceManager.hasTickets());
    }

    @Test public void testRemoveTicketsOnClosing() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicDistanceManager)distanceManager).addTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE);
        assertTrue(distanceManager.hasTickets());
        distanceManager.removeTicketsOnClosing();
        assertFalse(distanceManager.hasTickets());
    }

    @Test public void testAddRemoveTicketsVanilla() {
        var distanceManager = setupDistanceManager();
        distanceManager.addTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE);
        assertTrue(distanceManager.hasTickets());
        distanceManager.removeTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE);
        assertFalse(distanceManager.hasTickets());
        distanceManager.addRegionTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE);
        assertTrue(distanceManager.hasTickets());
        distanceManager.removeRegionTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE);
        assertFalse(distanceManager.hasTickets());
    }

    @Test public void testAddRemoveTickets() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicDistanceManager)distanceManager).addTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE);
        assertTrue(distanceManager.hasTickets());
        ((CubicDistanceManager)distanceManager).removeTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE);
        assertFalse(distanceManager.hasTickets());
        ((CubicDistanceManager)distanceManager).addRegionTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE);
        assertTrue(distanceManager.hasTickets());
        ((CubicDistanceManager)distanceManager).removeRegionTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE);
        assertFalse(distanceManager.hasTickets());
    }

    @Test public void testShouldForceTicksVanilla() {
        var distanceManager = setupDistanceManager();
        distanceManager.addRegionTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE, true);
        assertTrue(distanceManager.shouldForceTicks(0));
        distanceManager.removeRegionTicket(TicketType.START, new ChunkPos(0, 0), 0,  Unit.INSTANCE, true);
        assertFalse(distanceManager.shouldForceTicks(0));
    }

    @Test public void testShouldForceTicks() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicDistanceManager)distanceManager).addRegionTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE, true);
        assertTrue(distanceManager.shouldForceTicks(0));
        ((CubicDistanceManager)distanceManager).removeRegionTicket(CubicTicketType.START, CloPos.cube(0, 0, 0), 0,  Unit.INSTANCE, true);
        assertFalse(distanceManager.shouldForceTicks(0));
    }

    // Tests for PlayerTicketTracker
    // TODO: The two functions being tested are complex and confusing, and I don't know how to test them at this moment beyond verifying they don't crash anything.

    @Test public void testOnLevelChangeVanilla() {
        var distanceManager = setupDistanceManager();
        // Update view distance calls onLevelChange
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().updateViewDistance(10);
    }

    @Test public void testRunAllUpdatesVanilla() {
        var distanceManager = setupDistanceManager();
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().runAllUpdates();
    }

    @Test public void testOnLevelChange() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        // Update view distance calls onLevelChange
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().updateViewDistance(10);
    }

    @Test public void testRunAllUpdates() {
        var distanceManager = setupDistanceManager();
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().runAllUpdates();
    }



}
