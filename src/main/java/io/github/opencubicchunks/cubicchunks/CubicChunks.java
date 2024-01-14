package io.github.opencubicchunks.cubicchunks;

import java.lang.reflect.InvocationTargetException;

import io.github.opencubicchunks.cc_core.CubicChunksBase;
import io.github.opencubicchunks.cc_core.config.EarlyConfig;
import io.github.opencubicchunks.cc_core.utils.Coords;
import io.github.opencubicchunks.cubicchunks.config.CommonConfig;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkMap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Requires Mixin BootStrap in order to use in forge.
 */
@Mod("cubicchunks")
public class CubicChunks extends CubicChunksBase {
    protected static CommonConfig config = null;
    // For hardcoding height in P1
    public static final int SUPERFLAT_HEIGHT = 5;

    public CubicChunks(IEventBus modEventBus) {
        ChunkMap.class.getName();
        // if (!(CubeMap.class.isAssignableFrom(ChunkMap.class))) {
        //     throw new IllegalStateException("Mixin not applied!");
        // }
        EarlyConfig.getDiameterInSections();

        Coords.blockToIndex(new BlockPos(0, 0, 0));
        // ClassDuplicator.init();
        if (System.getProperty("cubicchunks.debug", "false").equalsIgnoreCase("true")) {
            try {
                Class.forName("io.github.opencubicchunks.cubicchunks.debug.DebugVisualization").getMethod("enable").invoke(null);
                SharedConstants.IS_RUNNING_IN_IDE = true;
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                LOGGER.catching(e);
            }
        }

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // PacketDispatcher.register();
    }

    public static CommonConfig config() {
        if (config == null) {
            config = CommonConfig.getConfig();
        }
        return config;
    }
}