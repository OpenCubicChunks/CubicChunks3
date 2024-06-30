package io.github.opencubicchunks.cubicchunks.mixin.core.common.client.multiplayer;

import static io.github.opencubicchunks.cc_core.CubicChunksBase.LOGGER;
import static io.github.opencubicchunks.cc_core.utils.Coords.cubeToSection;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cc_core.api.CubePos;
import io.github.opencubicchunks.cc_core.api.CubicConstants;
import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.client.multiplayer.CubicClientChunkCache;
import io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.MixinChunkSource;
import io.github.opencubicchunks.cubicchunks.mixin.dasmsets.CubeAccessAndDescendantsSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.cube.EmptyLevelCube;
import io.github.opencubicchunks.cubicchunks.world.level.cube.LevelCube;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The vanilla {@link ClientChunkCache} class stores all loaded chunks on the client and has methods to update and unload them, as well as change the center and range of the chunk storage.
 * This mixin adds versions of these methods for cubes, meaning that this class now stores both cubes and chunks.
 */
@Dasm(CubeAccessAndDescendantsSet.class)
@Mixin(ClientChunkCache.class)
public abstract class MixinClientChunkCache extends MixinChunkSource implements CubicClientChunkCache {
    @AddFieldToSets(sets = CubeAccessAndDescendantsSet.class, owner = @Ref(ClientChunkCache.class),
        field = @FieldSig(type = @Ref(ClientChunkCache.Storage.class), name = "storage"))
    volatile CubicClientChunkCache.Storage cc_cubeStorage;

    private LevelCube cc_emptyCube;

    @Shadow @Final ClientLevel level;

    /**
     * Initialize cube storage and the empty cube if the level is cubic
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void cc_onConstruct(ClientLevel pLevel, int pViewDistance, CallbackInfo ci) {
        if (((CanBeCubic) pLevel).cc_isCubic()) {
            cc_emptyCube = new EmptyLevelCube(
                pLevel, CloPos.cube(0, 0, 0), pLevel.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS)
            );
            cc_cubeStorage = new CubicClientChunkCache.Storage(calculateStorageRange(pViewDistance), pLevel);
            // TODO we could redirect the initial construction instead of immediately resizing. doesn't really matter
            updateViewRadius(cc_calculateChunkViewDistance(pViewDistance));
        }
    }

    private static boolean cc_isValidCube(@Nullable LevelCube pChunk, int pX, int pY, int pZ) {
        if (pChunk == null) {
            return false;
        } else {
            CubePos cubePos = pChunk.cc_getCloPos().cubePos();
            return cubePos.getX() == pX && cubePos.getY() == pY && cubePos.getZ() == pZ;
        }
    }

    @Override
    public void cc_drop(CubePos pChunkPos) {
        if (this.cc_cubeStorage.inRange(pChunkPos.getX(), pChunkPos.getY(), pChunkPos.getZ())) {
            int i = this.cc_cubeStorage.getIndex(pChunkPos.getX(), pChunkPos.getY(), pChunkPos.getZ());
            LevelCube levelCube = this.cc_cubeStorage.temp_getChunk(i);
            if (cc_isValidCube(levelCube, pChunkPos.getX(), pChunkPos.getY(), pChunkPos.getZ())) {
                // TODO event hook
//                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.ChunkEvent.Unload(levelCube));
                this.cc_cubeStorage.replace(i, levelCube, null);
            }
        }
    }

    @Override
    public @Nullable LevelCube cc_getCube(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
        if (this.cc_cubeStorage.inRange(pChunkX, pChunkY,pChunkZ)) {
            LevelCube levelCube = this.cc_cubeStorage.temp_getChunk(this.cc_cubeStorage.getIndex(pChunkX, pChunkY,pChunkZ));
            if (cc_isValidCube(levelCube, pChunkX, pChunkY,pChunkZ)) {
                return levelCube;
            }
        }

        return pLoad ? this.cc_emptyCube : null;
    }

    @Override
    public void cc_replaceBiomes(int pX, int pY, int pZ, FriendlyByteBuf pBuffer) {
        if (true) throw new UnsupportedOperationException("don't remove this exception until packet integration tests are added for this method"); // TODO (P2)
        if (!this.cc_cubeStorage.inRange(pX, pY, pZ)) {
            LOGGER.warn("Ignoring cube since it's not in the view range: {}, {}, {}", pX, pY, pZ);
        } else {
            int i = this.cc_cubeStorage.getIndex(pX, pY, pZ);
            LevelCube levelCube = this.cc_cubeStorage.chunks.get(i);
            if (!cc_isValidCube(levelCube, pX, pY, pZ)) {
                LOGGER.warn("Ignoring cube since it's not present: {}, {}, {}", pX, pY, pZ);
            } else {
                levelCube.replaceBiomes(pBuffer);
            }
        }
    }

    @Override
    public @Nullable LevelCube cc_replaceWithPacketData(
        int pX,
        int pY,
        int pZ,
        FriendlyByteBuf pBuffer,
        CompoundTag pTag,
        Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer
    ) {
        if (!this.cc_cubeStorage.inRange(pX, pY, pZ)) {
            LOGGER.warn("Ignoring cube since it's not in the view range: {}, {}, {}", pX, pY, pZ);
            return null;
        } else {
            int i = this.cc_cubeStorage.getIndex(pX, pY, pZ);
            LevelCube levelCube = this.cc_cubeStorage.chunks.get(i);
            CubePos cubePos = CubePos.of(pX, pY, pZ);
            if (!cc_isValidCube(levelCube, pX, pY, pZ)) {
                levelCube = new LevelCube(this.level, CloPos.cube(cubePos));
                levelCube.replaceWithPacketData(pBuffer, pTag, pConsumer);
                this.cc_cubeStorage.replace(i, levelCube);
            } else {
                levelCube.replaceWithPacketData(pBuffer, pTag, pConsumer);
            }

//            ((CubicClientLevel) this.level).onCubeLoaded(cubePos); // TODO (P3) onCubeLoaded call
            // TODO event hook
//            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.ChunkEvent.Load(levelCube, false));
            return levelCube;
        }
    }

    @Shadow public abstract void updateViewCenter(int pX, int pZ);

    @Override
    public void cc_updateViewCenter(int pX, int pY, int pZ) {
        this.cc_cubeStorage.viewCenterX = pX;
        this.cc_cubeStorage.viewCenterY = pY;
        this.cc_cubeStorage.viewCenterZ = pZ;
        this.updateViewCenter(cubeToSection(pX, 0), cubeToSection(pZ, 0));
    }

    @Shadow public abstract void updateViewRadius(int pViewDistance);

    @Override
    public void cc_updateViewRadius(int pViewDistance) {
        int i = this.cc_cubeStorage.cubeRadius;
        int j = calculateStorageRange(pViewDistance);
        if (i != j) {
            CubicClientChunkCache.Storage storage = new CubicClientChunkCache.Storage(j, this.level);
            storage.viewCenterX = this.cc_cubeStorage.viewCenterX;
            storage.viewCenterY = this.cc_cubeStorage.viewCenterY;
            storage.viewCenterZ = this.cc_cubeStorage.viewCenterZ;

            for(int k = 0; k < this.cc_cubeStorage.chunks.length(); ++k) {
                LevelCube levelCube = this.cc_cubeStorage.chunks.get(k);
                if (levelCube != null) {
                    CubePos cubePos = levelCube.cc_getCloPos().cubePos();
                    if (storage.inRange(cubePos.getX(), cubePos.getY(), cubePos.getZ())) {
                        storage.replace(storage.getIndex(cubePos.getX(), cubePos.getY(), cubePos.getZ()), levelCube);
                    }
                }
            }
            this.cc_cubeStorage = storage;
        }
        updateViewRadius(cc_calculateChunkViewDistance(pViewDistance));
    }

    @Shadow
    private static int calculateStorageRange(int pViewDistance) {
        throw new IllegalStateException("mixin failed to apply");
    }

    private static int cc_calculateChunkViewDistance(int cubeViewDistance) {
        int cubeStorageRange = calculateStorageRange(cubeViewDistance);
        // TODO this radius might be larger than it needs to be? coordinate maths is difficult
        int chunkStorageRange = CubicConstants.DIAMETER_IN_SECTIONS * (cubeStorageRange + 1);
        return chunkStorageRange - 3; // This gives the view distance, which gets passed back into calculateStorageRange which will readd the 3
    }

    // TODO gatherStats (only used for debug)

    @Override
    public int cc_getLoadedCubeCount() {
        return this.cc_cubeStorage.chunkCount;
    }
}
