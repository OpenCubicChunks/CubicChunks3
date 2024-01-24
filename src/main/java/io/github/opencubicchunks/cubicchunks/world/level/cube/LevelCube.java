package io.github.opencubicchunks.cubicchunks.world.level.cube;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.CubeAccessAndDescendantsSet;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.dasm.api.Ref;
import io.github.opencubicchunks.dasm.api.transform.DasmRedirect;
import io.github.opencubicchunks.dasm.api.transform.TransformFrom;
import io.github.opencubicchunks.dasm.api.transform.TransformFromClass;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@DasmRedirect(CubeAccessAndDescendantsSet.class)
public class LevelCube extends CubeAccess implements LevelClo {
    // Fields matching LevelChunk
    static final Logger LOGGER = LogUtils.getLogger();
    private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity() {
        @Override public void tick() {
        }

        @Override public boolean isRemoved() {
            return true;
        }

        @Override public BlockPos getPos() {
            return BlockPos.ZERO;
        }

        @Override public String getType() {
            return "<null>";
        }
    };
    private final Map<BlockPos, RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
    private boolean loaded;
    final Level level;
    @Nullable private Supplier<FullChunkStatus> fullStatus;
    @Nullable private PostLoadProcessor postLoad;
    private final Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;
    private final LevelChunkTicks<Block> blockTicks;
    private final LevelChunkTicks<Fluid> fluidTicks;

    // Constructors mirroring vanilla signatures
    public LevelCube(Level level, CloPos pos) {
        this(level, pos, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, null, null, null);
    }

    public LevelCube(
        Level level,
        CloPos pos,
        UpgradeData data,
        LevelChunkTicks<Block> blockTicks,
        LevelChunkTicks<Fluid> fluidTicks,
        long inhabitedTime,
        @Nullable LevelChunkSection[] sections,
        @Nullable PostLoadProcessor postLoad,
        @Nullable BlendingData blendingData
    ) {
        super(pos, data, level, level.registryAccess().registryOrThrow(Registries.BIOME), inhabitedTime, sections, blendingData);
        this.level = level;
        this.gameEventListenerRegistrySections = new Int2ObjectOpenHashMap<>();

        for(Heightmap.Types heightmap$types : Heightmap.Types.values()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(heightmap$types)) {
                // TODO (P2) heightmaps
//                this.heightmaps.put(heightmap$types, new Heightmap(this, heightmap$types));
            }
        }

        this.postLoad = postLoad;
        this.blockTicks = blockTicks;
        this.fluidTicks = fluidTicks;
    }

    public LevelCube(ServerLevel level, ProtoCube cube, @Nullable PostLoadProcessor postLoad) {
        this(
            level,
            cube.cc_getCloPos(),
            cube.getUpgradeData(),
            cube.unpackBlockTicks(),
            cube.unpackFluidTicks(),
            cube.getInhabitedTime(),
            cube.getSections(),
            postLoad,
            cube.getBlendingData()
        );

        for(BlockEntity blockentity : cube.getBlockEntities().values()) {
            this.setBlockEntity(blockentity);
        }

        this.pendingBlockEntities.putAll(cube.getBlockEntityNbts());

        for(int i = 0; i < cube.getPostProcessing().length; ++i) {
            this.postProcessing[i] = cube.getPostProcessing()[i];
        }

        this.setAllStarts(cube.getAllStarts());
        this.setAllReferences(cube.getAllReferences());

        for(Map.Entry<Heightmap.Types, Heightmap> entry : cube.getHeightmaps()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
                // TODO (P2) heightmaps
//                this.setHeightmap(entry.getKey(), entry.getValue().getRawData());
            }
        }

        this.skyLightSources = cube.skyLightSources;
        this.setLightCorrect(cube.isLightCorrect());
        this.unsaved = true;
    }

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getBlockTicks()Lnet/minecraft/world/ticks/TickContainerAccess;")
    @Override public native TickContainerAccess<Block> getBlockTicks();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getFluidTicks()Lnet/minecraft/world/ticks/TickContainerAccess;")
    @Override public native TickContainerAccess<Fluid> getFluidTicks();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getTicksForSerialization()Lnet/minecraft/world/level/chunk/ChunkAccess$TicksToSave;")
    @Override public native ChunkAccess.TicksToSave getTicksForSerialization();

    // TODO should this actually be dasm'd?
    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getListenerRegistry(I)Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;")
    @Override public native GameEventListenerRegistry getListenerRegistry(int sectionY);

    // dasm + mixin
    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Override public native @NotNull BlockState getBlockState(BlockPos pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;")
    @Override public native @NotNull FluidState getFluidState(BlockPos pos);

    // dasm + mixin
    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;")
    @Override public native FluidState getFluidState(int x, int y, int z);

    // TODO might be dasm-able eventually, if we get more powerful mixin tools
    @Nullable @Override public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        var chunkSection = this.getSection(Coords.blockToIndex(pos));
        boolean isOnlyAir = chunkSection.hasOnlyAir();
        if (isOnlyAir && state.isAir()) {
            return null;
        } else {
            int sectionLocalX = pos.getX() & 15;
            int sectionLocalY = pos.getY() & 15;
            int sectionLocalZ = pos.getZ() & 15;
            var previousState = chunkSection.setBlockState(sectionLocalX, sectionLocalY, sectionLocalZ, state);
            if (previousState == state) {
                return null;
            } else {
                var block = state.getBlock();
                // TODO (P2) heightmaps + lighting - see vanilla equivalent to this method

                boolean flag2 = previousState.hasBlockEntity();
                if (!this.level.isClientSide) {
                    previousState.onRemove(this.level, pos, state, isMoving);
                } else if ((!previousState.is(block) || !state.hasBlockEntity()) && flag2) {
                    this.removeBlockEntity(pos);
                }

                if (!chunkSection.getBlockState(sectionLocalX, sectionLocalY, sectionLocalZ).is(block)) {
                    return null;
                } else {
                    if (!this.level.isClientSide && !this.level.captureBlockSnapshots) {
                        state.onPlace(this.level, pos, previousState, isMoving);
                    }

                    if (state.hasBlockEntity()) {
                        BlockEntity blockentity = this.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
                        if (blockentity == null) {
                            blockentity = ((EntityBlock)block).newBlockEntity(pos, state);
                            if (blockentity != null) {
                                this.addAndRegisterBlockEntity(blockentity);
                            }
                        } else {
                            blockentity.setBlockState(state);
                            this.updateBlockEntityTicker(blockentity);
                        }
                    }

                    this.unsaved = true;
                    return previousState;
                }
            }
        }
    }

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "addEntity(Lnet/minecraft/world/entity/Entity;)V")
    @Deprecated @Override public native void addEntity(Entity entity);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "createBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Nullable private native BlockEntity createBlockEntity(BlockPos pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Override @Nullable public native BlockEntity getBlockEntity(BlockPos pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)"
        + "Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Nullable public native BlockEntity getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType creationType);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "addAndRegisterBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    public native void addAndRegisterBlockEntity(BlockEntity blockEntity);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "isInLevel()Z")
    private native boolean isInLevel();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "isTicking(Lnet/minecraft/core/BlockPos;)Z")
    public native boolean isTicking(BlockPos pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    @Override public native void setBlockEntity(BlockEntity blockEntity);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;")
    @Override @Nullable public native CompoundTag getBlockEntityNbtForSaving(BlockPos pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "removeBlockEntity(Lnet/minecraft/core/BlockPos;)V")
    @Override public native void removeBlockEntity(BlockPos pos);

    // TODO maybe shouldn't be dasm
    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "removeGameEventListener(Lnet/minecraft/world/level/block/entity/BlockEntity;"
        + "Lnet/minecraft/server/level/ServerLevel;"
        + ")V")
    private native <T extends BlockEntity> void removeGameEventListener(T blockEntity, ServerLevel level);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "removeGameEventListenerRegistry(I)V")
    private native void removeGameEventListenerRegistry(int p_283355_);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "removeBlockEntityTicker(Lnet/minecraft/core/BlockPos;)V")
    private native void removeBlockEntityTicker(BlockPos pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "runPostLoad()V")
    public native void runPostLoad();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "isEmpty()Z")
    public native boolean isEmpty();

    public void replaceWithPacketData(
        FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> outputTagConsumer
    ) {
        this.clearAllBlockEntities();

        for(LevelChunkSection levelchunksection : this.sections) {
            levelchunksection.read(buffer);
        }

        // TODO (P2) heightmaps - see vanilla equivalent

        // TODO (P2) lighting
//        this.initializeLightSources();
        outputTagConsumer.accept((p_187968_, p_187969_, p_187970_) -> {
            BlockEntity blockentity = this.getBlockEntity(p_187968_, LevelChunk.EntityCreationType.IMMEDIATE);
            if (blockentity != null && p_187970_ != null && blockentity.getType() == p_187969_) {
                blockentity.handleUpdateTag(p_187970_);
            }
        });
    }

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "replaceBiomes(Lnet/minecraft/network/FriendlyByteBuf;)V")
    public native void replaceBiomes(FriendlyByteBuf buffer);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "setLoaded(Z)V")
    public native void setLoaded(boolean loaded);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getLevel()Lnet/minecraft/world/level/Level;")
    public native Level getLevel();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getBlockEntities()Ljava/util/Map;")
    public native Map<BlockPos, BlockEntity> getBlockEntities();

    // TODO P2 or P3 figure this out later - stub method for now
    public void postProcessGeneration() {
        for (int i = 0; i < this.postProcessing.length; ++i) {
            if (this.postProcessing[i] != null) {
                this.postProcessing[i].clear();
            }
        }

        for(BlockPos blockpos1 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
            this.getBlockEntity(blockpos1);
        }

        this.pendingBlockEntities.clear();
    }

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "promotePendingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/nbt/CompoundTag;)"
        + "Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Nullable private native BlockEntity promotePendingBlockEntity(BlockPos pos, CompoundTag tag);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "unpackTicks(J)V")
    public native void unpackTicks(long pos);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "registerTickContainerInLevel(Lnet/minecraft/server/level/ServerLevel;)V")
    public native void registerTickContainerInLevel(ServerLevel level);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "unregisterTickContainerFromLevel(Lnet/minecraft/server/level/ServerLevel;)V")
    public native void unregisterTickContainerFromLevel(ServerLevel level);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getStatus()Lnet/minecraft/world/level/chunk/ChunkStatus;")
    @Override public native ChunkStatus getStatus();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "getFullStatus()Lnet/minecraft/server/level/FullChunkStatus;")
    public native FullChunkStatus getFullStatus();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "setFullStatus(Ljava/util/function/Supplier;)V")
    public native void setFullStatus(Supplier<FullChunkStatus> fullStatus);

    // TODO a bit concerning
    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "clearAllBlockEntities()V")
    public native void clearAllBlockEntities();

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "registerAllBlockEntitiesAfterLevelLoad()V")
    public native void registerAllBlockEntitiesAfterLevelLoad();

    // TODO (P3): GameEvent stuff is a bit concerning
    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "addGameEventListener(Lnet/minecraft/world/level/block/entity/BlockEntity;"
        + "Lnet/minecraft/server/level/ServerLevel;)V")
    private native <T extends BlockEntity> void addGameEventListener(T blockEntity, ServerLevel level);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    private native <T extends BlockEntity> void updateBlockEntityTicker(T blockEntity);

    @TransformFrom(copyFrom = @Ref(LevelChunk.class), value = "createTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;"
        + "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;)"
        + "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;")
    private native <T extends BlockEntity> TickingBlockEntity createTicker(T blockEntity, BlockEntityTicker<T> ticker);

    // TODO forge stuff
    // FORGE START
//    private final net.neoforged.neoforge.attachment.AttachmentHolder.AsField attachmentHolder = new net.neoforged.neoforge.attachment.AttachmentHolder.AsField();
//
//    @Override
//    public boolean hasData(net.neoforged.neoforge.attachment.AttachmentType<?> type) {
//        return attachmentHolder.hasData(type);
//    }
//
//    @Override
//    public <T> T getData(net.neoforged.neoforge.attachment.AttachmentType<T> type) {
//        return attachmentHolder.getData(type);
//    }
//
//    @Override
//    @Nullable
//    public <T> T setData(net.neoforged.neoforge.attachment.AttachmentType<T> type, T data) {
//        setUnsaved(true);
//        return attachmentHolder.setData(type, data);
//    }
    // FORGE END

    @DasmRedirect(CubeAccessAndDescendantsSet.class)
    @TransformFromClass(@Ref(string = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity"))
    class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean loggedInvalidBlockState;

        BoundTickingBlockEntity(T blockEntity, BlockEntityTicker<T> ticker) {
            throw new IllegalStateException("DASM failed to apply");
        }

        @Override public native void tick();

        @Override public native boolean isRemoved();

        @Override public native BlockPos getPos();

        @Override public native String getType();

        @Override public native String toString();
    }

    @FunctionalInterface
    public interface PostLoadProcessor {
        void run(LevelCube cube);
    }

    @DasmRedirect(CubeAccessAndDescendantsSet.class)
    @TransformFromClass(@Ref(string = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper"))
    public class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
        private TickingBlockEntity ticker;

        RebindableTickingBlockEntityWrapper(TickingBlockEntity ticker) {
            throw new IllegalStateException("DASM failed to apply");
        }

        native void rebind(TickingBlockEntity ticker);

        @Override public native void tick();

        @Override public native boolean isRemoved();

        @Override public native BlockPos getPos();

        @Override public native String getType();

        @Override public native String toString();
    }
}
