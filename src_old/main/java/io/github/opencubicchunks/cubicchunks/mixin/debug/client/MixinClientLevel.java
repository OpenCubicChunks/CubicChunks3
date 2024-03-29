package io.github.opencubicchunks.cubicchunks.mixin.debug.client;

import java.util.function.Supplier;

import io.github.opencubicchunks.cc_core.world.CubicLevelHeightAccessor;
import io.github.opencubicchunks.cubicchunks.debug.DebugVisualization;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level {

    protected MixinClientLevel(WritableLevelData writableLevelData,
                               ResourceKey<Level> resourceKey, Holder<DimensionType> dimensionType,
                               Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
        super(writableLevelData, resourceKey, dimensionType, supplier, bl, bl2, l);
        throw new Error("Mixin failed to apply!");
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onClientWorldConstruct(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey resourceKey,
                                        Holder holder, int i, int j, Supplier supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci) {

        if (((CubicLevelHeightAccessor) this).isCubic()) {
            DebugVisualization.onWorldLoad(this);
        }
    }
}
