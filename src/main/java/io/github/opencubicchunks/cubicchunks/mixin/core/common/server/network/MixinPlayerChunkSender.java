package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.network;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.world.level.CloPos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCloSet;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundForgetLevelCloPacket;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelChunkPacket;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelCubeWithLightPacket;
import io.github.opencubicchunks.cubicchunks.world.entity.EntityCubePosGetter;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Dasm(ChunkToCloSet.class)
@Mixin(PlayerChunkSender.class)
public class MixinPlayerChunkSender {
    @Shadow private int unacknowledgedBatches;

    @Shadow private int maxUnacknowledgedBatches;

    @Shadow private float desiredChunksPerTick;

    @Shadow private float batchQuota;

    @Shadow @Final private LongSet pendingChunks;

    @AddTransformToSets(ChunkToCloSet.class) @TransformFromMethod(value = @MethodSig("markChunkPendingToSend(Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    public native void cc_markCloPendingToSend(LevelClo clo);

    @AddMethodToSets(sets = ChunkToCloSet.class, owner = @Ref(PlayerChunkSender.class), method = @MethodSig("dropChunk(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/ChunkPos;)V"))
    public void cc_dropClo(ServerPlayer player, CloPos cloPos) {
        if (!this.pendingChunks.remove(cloPos.toLong()) && player.isAlive()) {
            PacketDistributor.sendToPlayer(player, new CCClientboundForgetLevelCloPacket(cloPos));
        }
    }

    @Inject(method = "sendNextChunks", at = @At(value = "HEAD"), cancellable = true)
    private void cc_onSendNextChunks(ServerPlayer player, CallbackInfo ci) {
        if (!((CanBeCubic) player.level()).cc_isCubic()) {
            return;
        }

        ci.cancel();

        if (this.unacknowledgedBatches < this.maxUnacknowledgedBatches) {
            float f = Math.max(1.0F, this.desiredChunksPerTick);
            this.batchQuota = Math.min(this.batchQuota + this.desiredChunksPerTick, f);
            if (!(this.batchQuota < 1.0F)) {
                if (!this.pendingChunks.isEmpty()) {
                    ServerLevel serverlevel = player.serverLevel();
                    ChunkMap chunkmap = serverlevel.getChunkSource().chunkMap;
                    List<LevelClo> list = this.cc_collectChunksToSend(chunkmap, CloPos.cube(((EntityCubePosGetter)player).cc_cubePosition()));
                    if (!list.isEmpty()) {
                        ServerGamePacketListenerImpl servergamepacketlistenerimpl = player.connection;
                        ++this.unacknowledgedBatches;

                        // This packet can remain the same because it is just for timing purposes in order to determine how many chunks (or cubes) the client should request
                        servergamepacketlistenerimpl.send(ClientboundChunkBatchStartPacket.INSTANCE);

                        // TODO P2 :: We need to send heightmap and lighting data, which would be contained in the Column

                        // We need to send the chunks first before cubes, to ensure that load order invariants are preserved
                        for (LevelClo levelClo : list) {
                            if(levelClo instanceof LevelChunk) {
                                cc_sendChunk(servergamepacketlistenerimpl, serverlevel, (LevelChunk) levelClo);
                            }
                        }

                        for (LevelClo levelClo : list) {
                            if(levelClo instanceof LevelCube) {
                                cc_sendCube(servergamepacketlistenerimpl, serverlevel, (LevelCube) levelClo);
                            }
                        }

                        // This packet can remain the same because it is just for timing purposes in order to determine how many chunks (or cubes) the client should request
                        servergamepacketlistenerimpl.send(new ClientboundChunkBatchFinishedPacket(list.size()));
                        this.batchQuota -= (float)list.size();
                    }
                }
            }
        }
    }

    @Unique
    private static void cc_sendCube(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelCube cube) {
        PacketDistributor.sendToPlayer(packetListener.player, new CCClientboundLevelCubeWithLightPacket(cube));

        // ChunkPos chunkpos = chunk.getPos();

        // TODO :: Probably never (its for vanilla debug tools)
        // DebugPackets.sendPoiPacketsForChunk(level, chunkpos);

        // TODO P3 :: We need our own fireCubeSent event for this
        // net.neoforged.neoforge.event.EventHooks.fireChunkSent(packetListener.player, chunk, level);
    }

    @Unique
    private static void cc_sendChunk(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk) {
        PacketDistributor.sendToPlayer(packetListener.player, new CCClientboundLevelChunkPacket(chunk.getPos()));

        // ChunkPos chunkpos = chunk.getPos();

        // TODO :: Probably never (its for vanilla debug tools)
        // DebugPackets.sendPoiPacketsForChunk(level, chunkpos);

        // TODO P3 :: We need our own fireChunkSent event for this
        // net.neoforged.neoforge.event.EventHooks.fireChunkSent(packetListener.player, chunk, level);
    }

    @TransformFromMethod(value = @MethodSig("collectChunksToSend(Lnet/minecraft/server/level/ChunkMap;Lnet/minecraft/world/level/ChunkPos;)Ljava/util/List;"))
    private native List<LevelClo> cc_collectChunksToSend(ChunkMap chunkMap, CloPos cloPos);

    // FIXME these should probably have some kind of reasonable sort order - at the very least, chunks before cubes
    @Dynamic @Redirect(method = "cc_dasm$cc_collectChunksToSend", at = @At(ordinal = 0, value = "INVOKE", target = "Ljava/util/Comparator;comparingInt(Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;"))
    private Comparator<Long> cc_onCollectChunksToSend_comparator1(ToIntFunction<Long> keyExtractor) {
        return (a, b) -> 0;
    }

    @Dynamic @Redirect(method = "cc_dasm$cc_collectChunksToSend", at = @At(ordinal = 1, value = "INVOKE", target = "Ljava/util/Comparator;comparingInt(Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;"))
    private Comparator<LevelClo> cc_onCollectChunksToSend_comparator2(ToIntFunction<LevelClo> keyExtractor) {
        return (a, b) -> 0;
    }
}
