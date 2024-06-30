package io.github.opencubicchunks.cubicchunks.client.multiplayer;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.CubeAccessAndDescendantsSet;
import io.github.opencubicchunks.cubicchunks.world.level.cube.CubicChunkSource;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;

public interface CubicClientChunkCache extends CubicChunkSource {
    // TODO (P2) we might want a version of the vanilla replaceWithPacketData with a different signature for handling chunks, since we only need heightmap data with CC

    void cc_drop(CubePos pChunkPos);

    void cc_replaceBiomes(int pX, int pY, int pZ, FriendlyByteBuf pBuffer);

    @Nullable LevelCube cc_replaceWithPacketData(
        int pX,
        int pY,
        int pZ,
        FriendlyByteBuf pBuffer,
        CompoundTag pTag,
        Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer
    );

    void cc_updateViewCenter(int pX, int pY, int pZ);

    void cc_updateViewRadius(int pViewDistance);

    // Fields and methods on this are public so they can be accessed from MixinClientChunkCache and tests; they should not be used anywhere else
    // (This has to be here since we can't add inner classes with mixin)
    @Dasm(CubeAccessAndDescendantsSet.class)
    final class Storage {
        public final AtomicReferenceArray<LevelCube> chunks;
        public final int cubeRadius;
        private final int viewRange;
        public volatile int viewCenterX;
        public volatile int viewCenterY;
        public volatile int viewCenterZ;
        public int chunkCount;
        // Field added since we can't get it off ClientChunkCache since this is no longer an inner class
        final ClientLevel level;

        public Storage(int pChunkRadius, ClientLevel clientLevel) {
            this.cubeRadius = pChunkRadius;
            this.viewRange = pChunkRadius * 2 + 1;
            this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange * this.viewRange);
            this.level = clientLevel;
        }

        public int getIndex(int pX, int pY, int pZ) {
            return Math.floorMod(pZ, this.viewRange) * this.viewRange * this.viewRange + Math.floorMod(pY, this.viewRange) * this.viewRange + Math.floorMod(pX, this.viewRange);
        }

        public void replace(int pChunkIndex, @Nullable LevelCube pChunk) {
            LevelCube levelchunk = this.chunks.getAndSet(pChunkIndex, pChunk);
            if (levelchunk != null) {
                --this.chunkCount;
//                this.level.unload(levelchunk); // TODO P2
            }

            if (pChunk != null) {
                ++this.chunkCount;
            }
        }

        public LevelCube replace(int pChunkIndex, LevelCube pChunk, @Nullable LevelCube pReplaceWith) {
            if (this.chunks.compareAndSet(pChunkIndex, pChunk, pReplaceWith) && pReplaceWith == null) {
                --this.chunkCount;
            }

//            this.level.unload(pChunk); // TODO P2
            return pChunk;
        }

        public boolean inRange(int pX, int pY, int pZ) {
            return Math.abs(pX - this.viewCenterX) <= this.cubeRadius
                && Math.abs(pY - this.viewCenterY) <= this.cubeRadius
                && Math.abs(pZ - this.viewCenterZ) <= this.cubeRadius;
        }

        @Nullable
        @TransformFromMethod(copyFrom = @Ref(ClientChunkCache.Storage.class), value = @MethodSig("getChunk(I)Lnet/minecraft/world/level/chunk/LevelChunk;"))
        public native LevelCube getChunk(int pChunkIndex);

        // TODO dasm copying getChunk currently changes the access modifier from public to protected, so we need a dummy public method
        @Nullable public LevelCube temp_getChunk(int index) {
            return getChunk(index);
        }

        public void dumpChunks(String pFilePath) {
            // TODO reimplement debug code
        }
    }
}
