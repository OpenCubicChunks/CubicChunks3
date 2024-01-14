package io.github.opencubicchunks.cubicchunks.test.misc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestInterfacesMatchVanillaClasses {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SharedConstants.IS_RUNNING_IN_IDE = true;
    }

    private static String stringifyMethod(Method method) {
        return method.getName() + "(" + Arrays.stream(method.getGenericParameterTypes()).map(Type::getTypeName).collect(Collectors.joining(", ")) + ") -> " + method.getGenericReturnType().getTypeName();
    }

    private static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    private void testParityIncludingAncestors(Class<?> vanillaClass, Class<?> cubicClass, Method... excludes) {
        var excludesSet = Arrays.stream(excludes)
            .map(TestInterfacesMatchVanillaClasses::stringifyMethod)
            .collect(Collectors.toSet());
        var vanillaMethods = Arrays.stream(vanillaClass.getMethods())
            .filter(method -> method.getDeclaringClass() != Object.class && !isStatic(method))
            .map(TestInterfacesMatchVanillaClasses::stringifyMethod)
            .filter(s -> !excludesSet.contains(s))
            .collect(Collectors.toSet());
        var cubicMethods = Arrays.stream(cubicClass.getMethods())
            .filter(method -> method.getDeclaringClass() != Object.class && !isStatic(method))
            .map(TestInterfacesMatchVanillaClasses::stringifyMethod)
            .filter(methodString -> !vanillaMethods.remove(methodString)) // Filter methodStrings that are NOT in vanillaMethods
            .toList();
        assertTrue(vanillaMethods.isEmpty() && cubicMethods.isEmpty(), () -> String.format("""
            Expected parity between %s %s and %s %s.
            Extra methods in %s:
                %s
            Extra methods in %s:
                %s
            
            """,
            vanillaClass.isInterface() ? "interface" : "class",
            vanillaClass.getName(),
            cubicClass.isInterface() ? "interface" : "class",
            cubicClass.getName(),
            vanillaClass.getSimpleName(),
            vanillaMethods.isEmpty() ? "[none]" : String.join("\n    ", vanillaMethods),
            cubicClass.getSimpleName(),
            cubicMethods.isEmpty() ? "[none]" : String.join("\n    ", cubicMethods)));
    }

    @Test public void testChunkAccessCloAccessParity() throws NoSuchMethodException {
        testParityIncludingAncestors(
            ChunkAccess.class,
            CloAccess.class,
            ChunkAccess.class.getMethod("getWorldForge"), // TODO need to check existence; this would fail on Fabric
            ChunkAccess.class.getMethod("getPos")
        );
    }
}
