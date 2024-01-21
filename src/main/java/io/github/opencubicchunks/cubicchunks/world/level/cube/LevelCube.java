package io.github.opencubicchunks.cubicchunks.world.level.cube;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.mixin.CopyFrom;
import io.github.opencubicchunks.cubicchunks.mixin.DasmRedirect;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFrom;
import io.github.opencubicchunks.cubicchunks.mixin.TransformFromClass;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
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

@DasmRedirect({ "cubeAccessAndDescendants" })
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
    public LevelCube(Level p_187945_, CloPos p_187946_) {
        this(p_187945_, p_187946_, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, null, null, null);
    }

    public LevelCube(
        Level p_196854_,
        CloPos p_196855_,
        UpgradeData p_196856_,
        LevelChunkTicks<Block> p_196857_,
        LevelChunkTicks<Fluid> p_196858_,
        long p_196859_,
        @Nullable LevelChunkSection[] p_196860_,
        @Nullable PostLoadProcessor p_196861_,
        @Nullable BlendingData p_196862_
    ) {
        super(p_196855_, p_196856_, p_196854_, p_196854_.registryAccess().registryOrThrow(Registries.BIOME), p_196859_, p_196860_, p_196862_);
        this.level = p_196854_;
        this.gameEventListenerRegistrySections = new Int2ObjectOpenHashMap<>();

        for(Heightmap.Types heightmap$types : Heightmap.Types.values()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(heightmap$types)) {
                // TODO (P2) heightmaps
//                this.heightmaps.put(heightmap$types, new Heightmap(this, heightmap$types));
            }
        }

        this.postLoad = p_196861_;
        this.blockTicks = p_196857_;
        this.fluidTicks = p_196858_;
    }

    public LevelCube(ServerLevel p_196850_, ProtoCube p_196851_, @Nullable PostLoadProcessor p_196852_) {
        this(
            p_196850_,
            p_196851_.cc_getCloPos(),
            p_196851_.getUpgradeData(),
            p_196851_.unpackBlockTicks(),
            p_196851_.unpackFluidTicks(),
            p_196851_.getInhabitedTime(),
            p_196851_.getSections(),
            p_196852_,
            p_196851_.getBlendingData()
        );

        for(BlockEntity blockentity : p_196851_.getBlockEntities().values()) {
            this.setBlockEntity(blockentity);
        }

        this.pendingBlockEntities.putAll(p_196851_.getBlockEntityNbts());

        for(int i = 0; i < p_196851_.getPostProcessing().length; ++i) {
            this.postProcessing[i] = p_196851_.getPostProcessing()[i];
        }

        this.setAllStarts(p_196851_.getAllStarts());
        this.setAllReferences(p_196851_.getAllReferences());

        for(Map.Entry<Heightmap.Types, Heightmap> entry : p_196851_.getHeightmaps()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
                // TODO (P2) heightmaps
//                this.setHeightmap(entry.getKey(), entry.getValue().getRawData());
            }
        }

        this.skyLightSources = p_196851_.skyLightSources;
        this.setLightCorrect(p_196851_.isLightCorrect());
        this.unsaved = true;
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getBlockTicks()Lnet/minecraft/world/ticks/TickContainerAccess;")
    @Override public native TickContainerAccess<Block> getBlockTicks();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getFluidTicks()Lnet/minecraft/world/ticks/TickContainerAccess;")
    @Override public native TickContainerAccess<Fluid> getFluidTicks();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getTicksForSerialization()Lnet/minecraft/world/level/chunk/ChunkAccess$TicksToSave;")
    @Override public native ChunkAccess.TicksToSave getTicksForSerialization();

    // TODO should this actually be dasm'd?
    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getListenerRegistry(I)Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;")
    @Override public native GameEventListenerRegistry getListenerRegistry(int p_251193_);

    // dasm + mixin
    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Override public native @NotNull BlockState getBlockState(BlockPos p_62923_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;")
    @Override public native @NotNull FluidState getFluidState(BlockPos p_62895_);

    // dasm + mixin
    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getFluidState(III)Lnet/minecraft/world/level/material/FluidState;")
    @Override public native FluidState getFluidState(int p_62815_, int p_62816_, int p_62817_);

    // TODO might be dasm-able eventually, if we get more powerful mixin tools
    @Nullable @Override public BlockState setBlockState(BlockPos pos, BlockState state, boolean p_62867_) {
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
                    previousState.onRemove(this.level, pos, state, p_62867_);
                } else if ((!previousState.is(block) || !state.hasBlockEntity()) && flag2) {
                    this.removeBlockEntity(pos);
                }

                if (!chunkSection.getBlockState(sectionLocalX, sectionLocalY, sectionLocalZ).is(block)) {
                    return null;
                } else {
                    if (!this.level.isClientSide && !this.level.captureBlockSnapshots) {
                        state.onPlace(this.level, pos, previousState, p_62867_);
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

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "addEntity(Lnet/minecraft/world/entity/Entity;)V")
    @Deprecated @Override public native void addEntity(Entity p_62826_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "createBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Nullable private native BlockEntity createBlockEntity(BlockPos p_62935_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Override @Nullable public native BlockEntity getBlockEntity(BlockPos p_62912_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)"
        + "Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Nullable public native BlockEntity getBlockEntity(BlockPos p_62868_, LevelChunk.EntityCreationType p_62869_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "addAndRegisterBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    public native void addAndRegisterBlockEntity(BlockEntity p_156391_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "isInLevel()Z")
    private native boolean isInLevel();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "isTicking(Lnet/minecraft/core/BlockPos;)Z")
    public native boolean isTicking(BlockPos p_156411_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    @Override public native void setBlockEntity(BlockEntity p_156374_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getBlockEntityNbtForSaving(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;")
    @Override @Nullable public native CompoundTag getBlockEntityNbtForSaving(BlockPos p_62932_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "removeBlockEntity(Lnet/minecraft/core/BlockPos;)V")
    @Override public native void removeBlockEntity(BlockPos p_62919_);

    // TODO maybe shouldn't be dasm
    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "removeGameEventListener(Lnet/minecraft/world/level/block/entity/BlockEntity;"
        + "Lnet/minecraft/server/level/ServerLevel;"
        + ")V")
    private native <T extends BlockEntity> void removeGameEventListener(T p_223413_, ServerLevel p_223414_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "removeGameEventListenerRegistry(I)V")
    private native void removeGameEventListenerRegistry(int p_283355_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "removeBlockEntityTicker(Lnet/minecraft/core/BlockPos;)V")
    private native void removeBlockEntityTicker(BlockPos p_156413_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "runPostLoad()V")
    public native void runPostLoad();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "isEmpty()Z")
    public native boolean isEmpty();

    public void replaceWithPacketData(
        FriendlyByteBuf p_187972_, CompoundTag p_187973_, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> p_187974_
    ) {
        this.clearAllBlockEntities();

        for(LevelChunkSection levelchunksection : this.sections) {
            levelchunksection.read(p_187972_);
        }

        // TODO (P2) heightmaps - see vanilla equivalent

        // TODO (P2) lighting
//        this.initializeLightSources();
        p_187974_.accept((p_187968_, p_187969_, p_187970_) -> {
            BlockEntity blockentity = this.getBlockEntity(p_187968_, LevelChunk.EntityCreationType.IMMEDIATE);
            if (blockentity != null && p_187970_ != null && blockentity.getType() == p_187969_) {
                blockentity.handleUpdateTag(p_187970_);
            }
        });
    }

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "replaceBiomes(Lnet/minecraft/network/FriendlyByteBuf;)V")
    public native void replaceBiomes(FriendlyByteBuf p_275574_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "setLoaded(Z)V")
    public native void setLoaded(boolean p_62914_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getLevel()Lnet/minecraft/world/level/Level;")
    public native Level getLevel();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getBlockEntities()Ljava/util/Map;")
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

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "promotePendingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/nbt/CompoundTag;)"
        + "Lnet/minecraft/world/level/block/entity/BlockEntity;")
    @Nullable private native BlockEntity promotePendingBlockEntity(BlockPos p_62871_, CompoundTag p_62872_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "unpackTicks(J)V")
    public native void unpackTicks(long p_187986_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "registerTickContainerInLevel(Lnet/minecraft/server/level/ServerLevel;)V")
    public native void registerTickContainerInLevel(ServerLevel p_187959_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "unregisterTickContainerFromLevel(Lnet/minecraft/server/level/ServerLevel;)V")
    public native void unregisterTickContainerFromLevel(ServerLevel p_187980_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getStatus()Lnet/minecraft/world/level/chunk/ChunkStatus;")
    @Override public native ChunkStatus getStatus();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "getFullStatus()Lnet/minecraft/server/level/FullChunkStatus;")
    public native FullChunkStatus getFullStatus();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "setFullStatus(Ljava/util/function/Supplier;)V")
    public native void setFullStatus(Supplier<FullChunkStatus> p_62880_);

    // TODO a bit concerning
    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "clearAllBlockEntities()V")
    public native void clearAllBlockEntities();

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "registerAllBlockEntitiesAfterLevelLoad()V")
    public native void registerAllBlockEntitiesAfterLevelLoad();

    // TODO (P3): GameEvent stuff is a bit concerning
    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "addGameEventListener(Lnet/minecraft/world/level/block/entity/BlockEntity;"
        + "Lnet/minecraft/server/level/ServerLevel;)V")
    private native <T extends BlockEntity> void addGameEventListener(T p_223416_, ServerLevel p_223417_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "updateBlockEntityTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
    private native <T extends BlockEntity> void updateBlockEntityTicker(T p_156407_);

    @TransformFrom(copyFrom = @CopyFrom(clazz = LevelChunk.class), value = "createTicker(Lnet/minecraft/world/level/block/entity/BlockEntity;"
        + "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;)"
        + "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;")
    private native <T extends BlockEntity> TickingBlockEntity createTicker(T p_156376_, BlockEntityTicker<T> p_156377_);

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

    @DasmRedirect({ "cubeAccessAndDescendants" })
    @TransformFromClass(@CopyFrom(string = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity"))
    class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean loggedInvalidBlockState;

        BoundTickingBlockEntity(T p_156433_, BlockEntityTicker<T> p_156434_) {
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
        void run(LevelCube p_196867_);
    }

    @DasmRedirect({ "cubeAccessAndDescendants" })
    @TransformFromClass(@CopyFrom(string = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper"))
    public class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
        private TickingBlockEntity ticker;

        RebindableTickingBlockEntityWrapper(TickingBlockEntity p_156447_) {
            throw new IllegalStateException("DASM failed to apply");
        }

        native void rebind(TickingBlockEntity p_156450_);

        @Override public native void tick();

        @Override public native boolean isRemoved();

        @Override public native BlockPos getPos();

        @Override public native String getType();

        @Override public native String toString();
    }
}
