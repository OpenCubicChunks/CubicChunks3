package io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level;

import net.minecraft.server.level.DistanceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DistanceManager.class)
public interface CubicDistanceManagerTestAccess {
    @Accessor(value = "playerTicketManager")
    DistanceManager.PlayerTicketTracker get_playerTicketManager();
}
