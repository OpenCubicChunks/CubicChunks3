package io.github.opencubicchunks.cubicchunks.config;

import static io.github.opencubicchunks.cc_core.world.CubicLevelHeightAccessor.WorldStyle;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ServerConfig extends BaseConfig {
    private static final String FILE_PATH = "serverconfig/cubicchunks.toml";

    private static final String KEY_WORLDSTYLES = "worldStyles";
    private static final String KEY_DEFAULT_WORLD_STYLE = KEY_WORLDSTYLES + ".defaultWorldStyle";
    private static final String KEY_CUBIC_DIMENSIONS = KEY_WORLDSTYLES + ".cubicDimensions";
    private static final String KEY_HYBRID_DIMENSIONS = KEY_WORLDSTYLES + ".hybridDimensions";
    private static final String KEY_CHUNK_DIMENSIONS = KEY_WORLDSTYLES + ".vanillaDimensions";

    private final WorldStyle defaultWorldStyle;
    // TODO should this be a Map<ResourceLocation, ...> instead?
    private final Map<String, WorldStyle> overrides = new HashMap<>();

    private ServerConfig(CommentedConfig config) {
        // TODO actually give proper user feedback if this crashes due to incorrect values
        defaultWorldStyle = worldStyleFromString(config.get(KEY_DEFAULT_WORLD_STYLE));

        config.<List<String>>get(KEY_CUBIC_DIMENSIONS).forEach((s) -> overrides.put(s, WorldStyle.CUBIC));
        config.<List<String>>get(KEY_HYBRID_DIMENSIONS).forEach((s) -> overrides.put(s, WorldStyle.HYBRID));
        config.<List<String>>get(KEY_CHUNK_DIMENSIONS).forEach((s) -> overrides.put(s, WorldStyle.CHUNK));
    }

    private static WorldStyle worldStyleFromString(String string) {
        if (string.equals("VANILLA"))
            return WorldStyle.CHUNK;
        return WorldStyle.valueOf(string);
    }

    public WorldStyle getWorldStyle(ResourceKey<Level> dimension) {
        return overrides.getOrDefault(dimension.location().toString(), defaultWorldStyle);
    }

    private static CommentedConfig createDefaultConfig() {
        // TODO some way of setting different defaults for specific modids? e.g. for things like RFTools and Mystcraft - maybe things like
        // "mystcraft:*"
        Config.setInsertionOrderPreserved(true);
        var config = CommentedConfig.inMemory();
        config.set(KEY_DEFAULT_WORLD_STYLE, WorldStyle.CUBIC);
        config.setComment(KEY_DEFAULT_WORLD_STYLE,
                """
                         The default world style used for dimensions that are not explicitly defined in one of the three lists below.
                         Possible values:
                             "CUBIC" - the dimension uses cubic chunks.
                             "HYBRID" - the dimension has cubic chunks enabled, but uses vanilla world generation. This may improve mod compatibility in some cases.
                             "VANILLA" - the dimension does NOT use cubic chunks; it behaves the same as in vanilla, with limited height, etc.\
                        """);
        config.set(KEY_CUBIC_DIMENSIONS, List.of());
        config.set(KEY_HYBRID_DIMENSIONS, List.of());
        config.set(KEY_CHUNK_DIMENSIONS, List.of(Level.END.location().toString()));
        config.setComment(KEY_CUBIC_DIMENSIONS, """
                 Explicitly sets the world style for each dimension. Overrides the default in defaultWorldStyle.
                 Note that this only affects dimensions that have not yet been generated.
                 If you want to change the world style of an existing dimension, you will need to delete it manually to make it regenerate.

                 By default, the only dimension listed here is the End, which is set to Vanilla so that you don't fall forever if you fall off.\
                """);
        return config;
    }

    private static File getConfigPath(Path worldFolder) {
        return new File(worldFolder.toFile(), FILE_PATH);
    }

    private static void createConfig(Path worldFolder) {
        File configPath = getConfigPath(worldFolder);
        if (configPath.exists())
            return;
        configPath.getParentFile().mkdirs();
        write(configPath, createDefaultConfig());
    }
    /*
     * @Nullable public static ServerConfig getConfig(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
     * File configPath = getConfigPath(((LevelStorageAccessAccess) levelStorageAccess).getLevelPath());
     * if (configPath.exists()) {
     * var config = createDefaultConfig();
     * read(configPath, config);
     * var serverConfig = new ServerConfig(config);
     * // Write the config again in case any keys were missing or invalid
     * write(configPath, config);
     * return serverConfig;
     * }
     * return null;
     * }
     * public static void generateConfigIfNecessary(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
     * if (CubicChunks.config().shouldGenerateNewWorldsAsCC()) {
     * CubicChunks.LOGGER.info("New worlds are configured to generate as CC; creating CC config file");
     * var rootFolderPath = ((LevelStorageAccessAccess) levelStorageAccess).getLevelPath();
     * ServerConfig.createConfig(rootFolderPath);
     * } else {
     * CubicChunks.LOGGER.info("New worlds are configured to NOT generate as CC; no Cubic Chunks data will be created");
     * }
     * }
     */
}
