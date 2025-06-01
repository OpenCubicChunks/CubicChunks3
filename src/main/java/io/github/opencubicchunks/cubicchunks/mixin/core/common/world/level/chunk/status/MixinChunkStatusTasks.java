package io.github.opencubicchunks.cubicchunks.mixin.core.common.world.level.chunk.status;

import java.util.concurrent.CompletableFuture;

import io.github.opencubicchunks.cubicchunks.CanBeCubic;
import io.github.opencubicchunks.cubicchunks.world.level.chunk.status.CCChunkStatusTasks;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkStatusTasks.class)
public class MixinChunkStatusTasks {
    @Inject(method = "generateStructureStarts", at = @At("HEAD"), cancellable = true)
    private static void cc_generateStructureStarts(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateStructureStarts(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "loadStructureStarts", at = @At("HEAD"), cancellable = true)
    private static void cc_loadStructureStarts(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.loadStructureStarts(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateStructureReferences", at = @At("HEAD"), cancellable = true)
    private static void cc_generateStructureReferences(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateStructureReferences(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateBiomes", at = @At("HEAD"), cancellable = true)
    private static void cc_generateBiomes(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateBiomes(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateNoise", at = @At("HEAD"), cancellable = true)
    private static void cc_generateNoise(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateNoise(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateSurface", at = @At("HEAD"), cancellable = true)
    private static void cc_generateSurface(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateSurface(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateCarvers", at = @At("HEAD"), cancellable = true)
    private static void cc_generateCarvers(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateCarvers(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateFeatures", at = @At("HEAD"), cancellable = true)
    private static void cc_generateFeatures(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateFeatures(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "initializeLight", at = @At("HEAD"), cancellable = true)
    private static void cc_initializeLight(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.initializeLight(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "light", at = @At("HEAD"), cancellable = true)
    private static void cc_light(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.light(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "generateSpawn", at = @At("HEAD"), cancellable = true)
    private static void cc_generateSpawn(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.generateSpawn(worldGenContext, step, cache, chunk));
        }
    }

    @Inject(method = "full", at = @At("HEAD"), cancellable = true)
    private static void cc_full(
        WorldGenContext worldGenContext, ChunkStep step, StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
    ) {
        if (((CanBeCubic) worldGenContext.level()).cc_isCubic()) {
            cir.setReturnValue(CCChunkStatusTasks.full(worldGenContext, step, cache, chunk));
        }
    }
}
