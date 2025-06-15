package io.github.opencubicchunks.cubicchunks.config;

import java.io.File;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.neoforged.fml.loading.FMLPaths;

public class CommonConfig extends BaseConfig {
    private static final String FILE_NAME = "cubicchunks_common.toml";
    // TODO forge/fabric-agnostic method for getting config directory
    // Note that this relies on IS_IN_TEST being set before this class is classloaded
    private static final File FILE_PATH = CubicChunks.IS_IN_TEST ? null : new File(FMLPaths.CONFIGDIR.get().toFile(), FILE_NAME);

    private static final String KEY_GENERAL = "general";
    private static final String KEY_VERTICAL_VIEW_DISTANCE = KEY_GENERAL + ".verticalViewDistance";
    private static final String KEY_GENERATE_NEW_WORLDS_AS_CC = KEY_GENERAL + ".generateNewWorldsAsCC";

    private final CommentedConfig config;

    private CommonConfig(CommentedConfig config) {
        this.config = config;
    }

    private static CommentedConfig createDefaultConfig() {
        Config.setInsertionOrderPreserved(true);
        var config = CommentedConfig.inMemory();
        config.set(KEY_VERTICAL_VIEW_DISTANCE, 8);
        // TODO more detailed config comment?
        config.setComment(KEY_VERTICAL_VIEW_DISTANCE, """
                 The vertical view distance for players in Cubic Chunks dimensions (similar to vanilla render distance for the horizontal axes).\
                """);
        config.set(KEY_GENERATE_NEW_WORLDS_AS_CC, true);
        config.setComment(KEY_GENERATE_NEW_WORLDS_AS_CC,
                """
                         Whether or not newly-created worlds generate using Cubic Chunks. (On the client, this is toggled by the button in the world creation GUI.)\
                        """);
        return config;
    }

    // TODO save on game exit, etc, instead of every time config is marked dirty
    public void markDirty() {
        write(FILE_PATH, config);
    }

    // TODO do we want config values in fields on this class, instead of doing a get() each time?
    public int getVerticalViewDistance() {
        return config.getInt(KEY_VERTICAL_VIEW_DISTANCE);
    }

    public boolean shouldGenerateNewWorldsAsCC() {
        return config.get(KEY_GENERATE_NEW_WORLDS_AS_CC);
    }

    public void setVerticalViewDistance(int verticalViewDistance) {
        config.set(KEY_VERTICAL_VIEW_DISTANCE, verticalViewDistance);
    }

    public void setGenerateNewWorldsAsCC(boolean generateNewWorldsAsCC) {
        config.set(KEY_GENERATE_NEW_WORLDS_AS_CC, generateNewWorldsAsCC);
    }

    public static CommonConfig getConfig() {
        var config = createDefaultConfig();
        if (CubicChunks.IS_IN_TEST) {
            // Skip file access when running in a test environment; tests should manually update relevant config values before running game code.
            return new CommonConfig(config);
        }
        // Read existing values to the config
        if (FILE_PATH.exists()) {
            read(FILE_PATH, config);
        }
        var commonConfig = new CommonConfig(config);
        // Write the config again even if we loaded an existing file, in case any keys were missing or invalid
        write(FILE_PATH, config);
        return commonConfig;
    }
}
