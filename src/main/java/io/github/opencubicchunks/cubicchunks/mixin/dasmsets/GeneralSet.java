package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloPos;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ImposterProtoClo;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;

/**
 * Should be used for most DASM transforms, except in cases where other specific transforms are required (e.g. ChunkAccess to CubeAccess for CubeAccess and subclasses)
 * <br/><br/>
 * Redirects should be added to {@link GlobalSet} rather than this set, except when they cause issues with other sets that inherit from {@link GlobalSet} - for example constructor to factory redirects on ChunkAccess subclasses.
 */
@RedirectSet
public interface GeneralSet extends GlobalSet {
    @TypeRedirect(from = @Ref(ChunkAccess.class), to = @Ref(CloAccess.class))
    interface ChunkAccess_to_CloAccess_redirects {
        @MethodRedirect(@MethodSig("getPos()Lnet/minecraft/world/level/ChunkPos;"))
        CloPos cc_getCloPos();

        @MethodRedirect(@MethodSig("getStatus()Lnet/minecraft/world/level/chunk/ChunkStatus;"))
        ChunkStatus getStatus();
    }

    @TypeRedirect(from = @Ref(LevelChunk.class), to = @Ref(LevelClo.class))
    interface LevelChunk_to_LevelClo_redirects {
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(Level.class), @Ref(CloPos.class) }))
        static LevelClo create(Level level, ChunkPos pos) {
            throw new IllegalStateException("this should never be called");
        }
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(Level.class),
            @Ref(ChunkPos.class),
            @Ref(UpgradeData.class),
            @Ref(LevelChunkTicks.class),
            @Ref(LevelChunkTicks.class),
            @Ref(long.class),
            @Ref(LevelChunkSection[].class),
            @Ref(LevelChunk.PostLoadProcessor.class),
            @Ref(BlendingData.class)
        }))
        static LevelClo create(Level level,
                               CloPos pos,
                               UpgradeData data,
                               LevelChunkTicks<Block> blockTicks,
                               LevelChunkTicks<Fluid> fluidTicks,
                               long inhabitedTime,
                               @Nullable LevelChunkSection[] sections,
                               @Nullable LevelClo.PostLoadProcessor postLoad,
                               @Nullable BlendingData blendingData) {
            throw new IllegalStateException("this should never be called");
        }
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(ServerLevel.class), @Ref(ProtoChunk.class), @Ref(LevelChunk.PostLoadProcessor.class)  }))
        static LevelClo create(ServerLevel level, ProtoClo clo, @Nullable LevelClo.PostLoadProcessor postLoad) {
            throw new IllegalStateException("this should never be called");
        }
    }

    @TypeRedirect(from = @Ref(LevelChunk.PostLoadProcessor.class), to = @Ref(LevelClo.PostLoadProcessor.class))
    interface LevelChunk$PostLoadProcessor_to_LevelClo$PostLoadProcessor_redirects { }

    @TypeRedirect(from = @Ref(ProtoChunk.class), to = @Ref(ProtoClo.class))
    interface ProtoChunk_to_ProtoClo_redirects {
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(ChunkPos.class), @Ref(UpgradeData.class), @Ref(LevelHeightAccessor.class), @Ref(Registry.class), @Ref(BlendingData.class) }))
        static ProtoClo create(CloPos cloPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, @Nullable BlendingData pBlendingData) {
            throw new IllegalStateException("this should never be called");
        }

        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {
            @Ref(ChunkPos.class),
            @Ref(UpgradeData.class),
            @Ref(LevelChunkSection[].class),
            @Ref(ProtoChunkTicks.class),
            @Ref(ProtoChunkTicks.class),
            @Ref(LevelHeightAccessor.class),
            @Ref(Registry.class),
            @Ref(BlendingData.class)}))
        static ProtoClo create(
            CloPos cloPos,
            UpgradeData pUpgradeData,
            @Nullable LevelChunkSection[] pSections,
            ProtoChunkTicks<Block> pBlockTicks,
            ProtoChunkTicks<Fluid> pLiquidTicks,
            LevelHeightAccessor pLevelHeightAccessor,
            Registry<Biome> pBiomeRegistry,
            @Nullable BlendingData pBlendingData
        ) {
            throw new IllegalStateException("this should never be called");
        }
    }

    @TypeRedirect(from = @Ref(ImposterProtoChunk.class), to = @Ref(ImposterProtoClo.class))
    interface ImposterProtoChunk_to_ImposterProtoClo_redirects {
        @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(LevelChunk.class), @Ref(boolean.class)}))
        static ImposterProtoClo create(LevelClo wrapped, boolean pAllowWrites) {
            throw new IllegalStateException("this should never be called");
        }

        @MethodRedirect(@MethodSig("getWrapped()Lnet/minecraft/world/level/chunk/LevelChunk;"))
        LevelClo cc_getWrappedClo();
    }
}
