package io.github.opencubicchunks.cubicchunks.mixin.core.common.server.level;

import io.github.opencubicchunks.cubicchunks.server.level.CloTrackingView;
import org.spongepowered.asm.mixin.Mixin;

// Needed for DASM to apply
// TODO won't be necessary once we have dasm.json
@Mixin(CloTrackingView.class)
public interface MixinCloTrackingView {
}
