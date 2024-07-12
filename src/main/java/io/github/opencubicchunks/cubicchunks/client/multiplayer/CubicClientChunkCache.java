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

    void cc_drop(CubePos chunkPos);

    void cc_replaceBiomes(int x, int y, int z, FriendlyByteBuf buffer);

    @Nullable LevelCube cc_replaceWithPacketData(
        int x,
        int y,
        int z,
        FriendlyByteBuf buffer,
        CompoundTag tag,
        Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer
    );

    void cc_updateViewCenter(int x, int y, int z);

    void cc_updateViewRadius(int viewDistance);

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

        public Storage(int chunkRadius, ClientLevel clientLevel) {
            this.cubeRadius = chunkRadius;
            this.viewRange = chunkRadius * 2 + 1;
            this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange * this.viewRange);
            this.level = clientLevel;
        }

        public int getIndex(int x, int y, int z) {
            return Math.floorMod(z, this.viewRange) * this.viewRange * this.viewRange + Math.floorMod(y, this.viewRange) * this.viewRange + Math.floorMod(x, this.viewRange);
        }

        public void replace(int chunkIndex, @Nullable LevelCube chunk) {
            LevelCube levelchunk = this.chunks.getAndSet(chunkIndex, chunk);
            if (levelchunk != null) {
                --this.chunkCount;
//                this.level.unload(levelchunk); // TODO P2
            }

            if (chunk != null) {
                ++this.chunkCount;
            }
        }

        public LevelCube replace(int chunkIndex, LevelCube chunk, @Nullable LevelCube replaceWith) {
            if (this.chunks.compareAndSet(chunkIndex, chunk, replaceWith) && replaceWith == null) {
                --this.chunkCount;
            }

//            this.level.unload(chunk); // TODO P2
            return chunk;
        }

        public boolean inRange(int x, int y, int z) {
            return Math.abs(x - this.viewCenterX) <= this.cubeRadius
                && Math.abs(y - this.viewCenterY) <= this.cubeRadius
                && Math.abs(z - this.viewCenterZ) <= this.cubeRadius;
        }

        @Nullable
        @TransformFromMethod(copyFrom = @Ref(ClientChunkCache.Storage.class), value = @MethodSig("getChunk(I)Lnet/minecraft/world/level/chunk/LevelChunk;"))
        public native LevelCube getChunk(int chunkIndex);

        // TODO dasm copying getChunk currently changes the access modifier from public to protected, so we need a dummy public method
        @Nullable public LevelCube temp_getChunk(int index) {
            return getChunk(index);
        }

        public void dumpChunks(String filePath) {
            // TODO reimplement debug code
        }
    }
}
