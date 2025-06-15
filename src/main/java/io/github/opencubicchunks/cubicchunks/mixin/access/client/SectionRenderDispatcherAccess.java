package io.github.opencubicchunks.cubicchunks.mixin.access.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionRenderDispatcher.class)
public interface SectionRenderDispatcherAccess {
    @Accessor("level")
    ClientLevel cc_getLevel();
}
