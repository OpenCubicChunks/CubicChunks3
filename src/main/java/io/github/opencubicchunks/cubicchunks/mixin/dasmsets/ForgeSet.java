package io.github.opencubicchunks.cubicchunks.mixin.dasmsets;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.opencubicchunks.cubicchunks.movetoforgesourcesetlater.CCEventHooks;
import net.neoforged.neoforge.event.EventHooks;

// TODO once redirect sets can be applied conditionally, this should be in the forge sourceset and GlobalSet should no longer extend it
@RedirectSet
public interface ForgeSet {
    @InterOwnerContainer(from = @Ref(EventHooks.class), to = @Ref(CCEventHooks.class))
    class EventHooks_to_CCEventHooks_redirects {
    }
}
