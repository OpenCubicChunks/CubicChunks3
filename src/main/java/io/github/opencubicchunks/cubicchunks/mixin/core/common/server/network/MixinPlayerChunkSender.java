package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.network;

import java.util.List;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.ChunkToCubeSet;
import io.github.opencubicchunks.cubicchunks.network.CCClientboundLevelCubeWithLightPacket;
import io.github.opencubicchunks.cubicchunks.world.entity.EntityCubePosGetter;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Dasm(ChunkToCubeSet.class)
@Mixin(PlayerChunkSender.class)
public class MixinPlayerChunkSender {
    @Shadow private int unacknowledgedBatches;

    @Shadow private int maxUnacknowledgedBatches;

    @Shadow private float desiredChunksPerTick;

    @Shadow private float batchQuota;

    @Shadow @Final private LongSet pendingChunks;

    @Inject(method = "sendNextChunks", at = @At(value = "HEAD"), cancellable = true)
    private void cc_sendNextChunks(ServerPlayer player, CallbackInfo ci) {
        if(!((CanBeCubic)player).cc_isCubic()) {
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
                    List<LevelCube> list = this.cc_collectChunksToSend(chunkmap, ((EntityCubePosGetter)player).cc_cubePosition());
                    if (!list.isEmpty()) {
                        ServerGamePacketListenerImpl servergamepacketlistenerimpl = player.connection;
                        ++this.unacknowledgedBatches;

                        // This packet can remain the same because it is just for timing purposes in order to determine how many chunks (or cubes) the client should request
                        servergamepacketlistenerimpl.send(new ClientboundChunkBatchStartPacket());

                        // TODO P2 :: We need to send heightmap and lighting data, which would be contained in the Column, but we only send Cubes for now
                        // TODO P2 :: We will need to send fireChunkSent when we send a column
                        // Also we need to send the columns before the cubes to do this correctly

                        for(LevelCube levelCube : list) {
                            cc_sendCube(servergamepacketlistenerimpl, serverlevel, levelCube);
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
        PacketDistributor.PLAYER.with(packetListener.player).send(new CCClientboundLevelCubeWithLightPacket(cube));

        // ChunkPos chunkpos = chunk.getPos();

        // TODO :: Probably never (its for vanilla debug tools)
        // DebugPackets.sendPoiPacketsForChunk(level, chunkpos);

        // TODO P2 :: We need our own fireCubeSent event for this
        // net.neoforged.neoforge.event.EventHooks.fireChunkSent(packetListener.player, chunk, level);
    }

    @TransformFromMethod(value = @MethodSig("collectChunksToSend(Lnet/minecraft/server/level/ChunkMap;Lnet/minecraft/world/level/ChunkPos;)Ljava/util/List;"))
    private native List<LevelCube> cc_collectChunksToSend(ChunkMap chunkMap, CubePos cubePos);
}
