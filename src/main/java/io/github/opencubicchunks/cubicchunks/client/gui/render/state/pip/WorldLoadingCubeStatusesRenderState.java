package io.github.opencubicchunks.cubicchunks.client.gui.render.state.pip;

import javax.annotation.Nullable;

import io.github.opencubicchunks.cubicchunks.client.gui.render.pip.WorldLoadingCubeStatusesRenderer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.server.level.progress.StoringChunkProgressListener;

/**
 * PiP render state for {@link WorldLoadingCubeStatusesRenderer}.
 * Stores a reference to the {@link StoringChunkProgressListener} used for tracking world load progress.
 */
public record WorldLoadingCubeStatusesRenderState(
        StoringChunkProgressListener chunkProgressListener, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public WorldLoadingCubeStatusesRenderState(
            StoringChunkProgressListener chunkProgressListener, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea
    ) {
        this(chunkProgressListener, x0, y0, x1, y1, scale, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
