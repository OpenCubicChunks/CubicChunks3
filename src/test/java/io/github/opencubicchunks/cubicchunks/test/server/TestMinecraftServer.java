package io.github.opencubicchunks.cubicchunks.test.server;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;

import static io.github.opencubicchunks.cubicchunks.testutils.Misc.setupServerLevel;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import io.github.opencubicchunks.cubicchunks.MarkableAsCubic;
import io.github.opencubicchunks.cubicchunks.mixin.test.common.server.level.MinecraftServerTestAccess;
import io.github.opencubicchunks.cubicchunks.testutils.CloseableReference;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Answers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMinecraftServer {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    private MinecraftServer setupServer() {
        WorldStem worldStemMock = mock(WorldStem.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        when(worldStemMock.registries().compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)).thenReturn(true);
        return new IntegratedServer(mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), mock(RETURNS_DEEP_STUBS), worldStemMock, mock(RETURNS_DEEP_STUBS),
            mock(RETURNS_DEEP_STUBS));
    }

    //TODO: These tests are disabled for now since MinecraftServer creates a ServerFunctionManager which eventually tries to copy a null array. Not sure how to mock that right now.
    @Test @Disabled public void testSetInitialSpawnVanilla() throws Exception {
        try (CloseableReference<ServerLevel> serverLevelReference = setupServerLevel()) {
            ((MarkableAsCubic)serverLevelReference.value()).cc_setCubic();
            ((MinecraftServerTestAccess)setupServer()).invoke_setInitialSpawn(serverLevelReference.value(), mock(RETURNS_DEEP_STUBS), false, false);
        }
    }

    @Test @Disabled public void testPrepareLevelsVanilla() throws Exception {
        MinecraftServer server = setupServer();
        ((MarkableAsCubic)server.overworld()).cc_setCubic();
        ((MinecraftServerTestAccess)setupServer()).invoke_prepareLevels(mock());
    }
}
