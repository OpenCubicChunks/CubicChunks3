package io.github.opencubicchunks.cubicchunks.test.server.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinDistanceManager;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level.MixinPlayerTicketTracker;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.CubicDistanceManagerTestAccess;
import io.github.opencubicchunks.cubicchunks.testutils.BaseTest;
import io.github.opencubicchunks.cubicchunks.world.level.CubicTicketStorage;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.TriState;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MixinDistanceManager} and {@link MixinPlayerTicketTracker}
 */
public class TestCubicDistanceManager extends BaseTest {
    static class TestDistanceManager extends DistanceManager {
        TestDistanceManager(TicketStorage ticketStorage, Executor executor, Executor executor2) {
            super(ticketStorage, executor, executor2);
        }

        @Override protected boolean isChunkToRemove(long chunkPos) {
            return false;
        }

        @Override protected @Nullable ChunkHolder getChunk(long chunkPos) {
            return null;
        }

        @Override protected @Nullable ChunkHolder updateChunkScheduling(long chunkPos, int newLevel, @Nullable ChunkHolder holder, int oldLevel) {
            return null;
        }
    }

    static class ServerPlayerAndPosition {
        ServerPlayer player;
        SectionPos pos;

        ServerPlayerAndPosition(SectionPos pos, ServerPlayer player) {
            this.pos = pos;
            this.player = player;
        }
    }

    private DistanceManager setupDistanceManager(TicketStorage ticketStorage) {
        var mainThread = Thread.currentThread();
        var mainThreadExecutor = // Based on ServerChunkCache.MainThreadExecutor
                new BlockableEventLoop<>("test_event_loop") {
                    @Override public Runnable wrapRunnable(Runnable runnable) {
                        return runnable;
                    }

                    @Override protected boolean shouldRun(Runnable runnable) {
                        return true;
                    }

                    @Override protected boolean scheduleExecutables() {
                        return true;
                    }

                    @Override protected Thread getRunningThread() {
                        return mainThread;
                    }
                };
        // We run everything on the main thread as Mockito has race conditions when multiple threads call into it
        // (which occurs when using RETURNS_DEEP_STUBS)
        DistanceManager distanceManager = new TestDistanceManager(ticketStorage, Runnable::run, mainThreadExecutor);
        return distanceManager;
    }

    @Test
    public void testUpdateChunkForcedVanilla() {
        var ticketStorage = new TicketStorage();
        DistanceManager distanceManager = setupDistanceManager(ticketStorage);
        ticketStorage.updateChunkForced(ChunkPos.ZERO, true);
        distanceManager.runAllUpdates(mock(ChunkMap.class));
        assertTrue(distanceManager.inEntityTickingRange(ChunkPos.ZERO.toLong()), "ChunkPos.ZERO is not in entity ticking range");
    }

    @Test
    public void testUpdateChunkForced() {
        var ticketStorage = new TicketStorage();
        DistanceManager distanceManager = setupDistanceManager(ticketStorage);
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicTicketStorage) ticketStorage).cc_updateChunkForced(CloPos.cube(0, 0, 0), true);
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
            assertEquals(TriState.TRUE, distanceManager.hasPlayersNearby(player.pos.chunk().toLong()),
                    player.pos.chunk().toLong() + " has no players nearby");
        }
    }

    private void removeAndTestPlayersVanilla(
            DistanceManager distanceManager, List<ServerPlayerAndPosition> players, float chanceToRemove, Random rand
    ) {
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
            assertEquals(TriState.TRUE, distanceManager.hasPlayersNearby(player.pos.chunk().toLong()),
                    player.pos.chunk().toLong() + " has no players nearby");
        }
    }

    private void testAddAndRemoveVanilla(DistanceManager distanceManager, int iterations, int numToAdd, float chanceToRemove) {
        Random rand = new Random(727);
        List<ServerPlayerAndPosition> players = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            if (i % 2 == 0) {
                addAndTestPlayersVanilla(distanceManager, players, numToAdd, rand);
            } else {
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
            assertEquals(TriState.TRUE, distanceManager.hasPlayersNearby(CloPos.section(player.pos).toLong()),
                    CloPos.section(player.pos).toLong() + " has no players nearby");
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
            assertEquals(TriState.TRUE, distanceManager.hasPlayersNearby(CloPos.section(player.pos).toLong()),
                    CloPos.section(player.pos).toLong() + " has no players nearby");
        }
    }

    private void testAddAndRemove(DistanceManager distanceManager, int iterations, int numToAdd, float chanceToRemove) {
        Random rand = new Random(727);
        List<ServerPlayerAndPosition> players = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            if (i % 2 == 0) {
                addAndTestPlayers(distanceManager, players, numToAdd, rand);
            } else {
                removeAndTestPlayers(distanceManager, players, chanceToRemove, rand);
            }
        }
    }

    @Test
    public void testSinglePlayerVanilla() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        testAddAndRemoveVanilla(distanceManager, 2, 1, 0.0f);
    }

    @Test
    public void testMultiplePlayersVanilla() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        testAddAndRemoveVanilla(distanceManager, 100, 200, 0.5f);
    }

    @Test
    public void testSinglePlayer() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        testAddAndRemove(distanceManager, 2, 1, 0.0f);
    }

    @Test
    public void testMultiplePlayers() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        testAddAndRemove(distanceManager, 50, 50, 0.5f);
    }

    @Test
    public void testAddRemoveTicketsVanilla() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ticketStorage.addTicket(new Ticket(TicketType.START, 0), new ChunkPos(0, 0));
        assertTrue(distanceManager.hasTickets());
        ticketStorage.removeTicket(new Ticket(TicketType.START, 0), new ChunkPos(0, 0));
        assertFalse(distanceManager.hasTickets());
        ticketStorage.addTicketWithRadius(TicketType.START, new ChunkPos(0, 0), 5);
        assertTrue(distanceManager.hasTickets());
        ticketStorage.removeTicketWithRadius(TicketType.START, new ChunkPos(0, 0), 5);
        assertFalse(distanceManager.hasTickets());
    }

    @Test
    public void testAddRemoveTickets() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicTicketStorage) ticketStorage).cc_addTicket(new Ticket(TicketType.START, 0), CloPos.cube(0, 0, 0));
        assertTrue(distanceManager.hasTickets());
        ((CubicTicketStorage) ticketStorage).cc_removeTicket(new Ticket(TicketType.START, 0), CloPos.cube(0, 0, 0));
        assertFalse(distanceManager.hasTickets());
        ((CubicTicketStorage) ticketStorage).cc_addTicketWithRadius(TicketType.START, CloPos.cube(0, 0, 0), 5);
        assertTrue(distanceManager.hasTickets());
        ((CubicTicketStorage) ticketStorage).cc_removeTicketWithRadius(TicketType.START, CloPos.cube(0, 0, 0), 5);
        assertFalse(distanceManager.hasTickets());
    }

    // Tests for PlayerTicketTracker
    // TODO: The two functions being tested are complex and confusing, and I don't know how to test them at this moment beyond verifying they don't
    // crash anything.

    @Test
    public void testOnLevelChangeVanilla() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        // Update view distance calls onLevelChange
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().updateViewDistance(10);
    }

    @Test
    public void testRunAllUpdatesVanilla() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().runAllUpdates();
    }

    @Test
    public void testOnLevelChange() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        // Update view distance calls onLevelChange
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().updateViewDistance(10);
    }

    @Test
    public void testRunAllUpdates() {
        var ticketStorage = new TicketStorage();
        var distanceManager = setupDistanceManager(ticketStorage);
        ((MarkableAsCubic) distanceManager).cc_setCubic();
        ((CubicDistanceManagerTestAccess) distanceManager).get_playerTicketManager().runAllUpdates();
    }

}
