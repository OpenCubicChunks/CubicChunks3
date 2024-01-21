package io.github.opencubicchunks.cubicchunks.testutils;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class Setup {
    public static void setupTests() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }
}
