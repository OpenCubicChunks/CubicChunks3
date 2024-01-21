package io.github.opencubicchunks.cubicchunks.test.misc;

import static io.github.opencubicchunks.cubicchunks.testutils.Setup.setupTests;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.opencubicchunks.cubicchunks.world.level.chunklike.CloAccess;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ImposterProtoClo;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.LevelClo;
import io.github.opencubicchunks.cubicchunks.world.level.chunklike.ProtoClo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestInterfacesMatchVanillaClasses {
    @BeforeAll
    public static void setup() {
        setupTests();
    }

    private static String stringifyMethod(Method method) {
        return method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")) + ") -> " + method.getReturnType().getName();
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
            ChunkAccess.class.getMethod("getPos"),
            ChunkAccess.class.getMethod("getWorldForge") // TODO need to check existence; this would fail on Fabric
        );
    }

    @Test public void testLevelChunkLevelCloParity() throws NoSuchMethodException {
        testParityIncludingAncestors(
            LevelChunk.class,
            LevelClo.class,
            ChunkAccess.class.getMethod("getPos"),
            // TODO need to check existence; these would fail on Fabric
            ChunkAccess.class.getMethod("getWorldForge"),
            LevelChunk.class.getMethod("getWorldForge"),
            LevelChunk.class.getMethod("readAttachmentsFromNBT", CompoundTag.class),
            LevelChunk.class.getMethod("hasData", AttachmentType.class),
            LevelChunk.class.getMethod("getData", AttachmentType.class),
            LevelChunk.class.getMethod("writeAttachmentsToNBT"),
            LevelChunk.class.getMethod("setData", AttachmentType.class, Object.class),
            LevelChunk.class.getMethod("hasData", Supplier.class),
            LevelChunk.class.getMethod("getData", Supplier.class),
            LevelChunk.class.getMethod("setData", Supplier.class, Object.class),
            LevelChunk.class.getMethod("getAuxLightManager", ChunkPos.class)
        );
    }

    @Test public void testProtoChunkProtoCloParity() throws NoSuchMethodException {
        testParityIncludingAncestors(
            ProtoChunk.class,
            ProtoClo.class,
            ChunkAccess.class.getMethod("getPos"),
            ChunkAccess.class.getMethod("getWorldForge") // TODO need to check existence; this would fail on Fabric
        );
    }

    @Test public void testImposterProtoChunkImposterProtoCloParity() throws NoSuchMethodException {
        testParityIncludingAncestors(
            ImposterProtoChunk.class,
            ImposterProtoClo.class,
            ChunkAccess.class.getMethod("getPos"),
            ImposterProtoChunk.class.getMethod("getWrapped"),
            ChunkAccess.class.getMethod("getWorldForge") // TODO need to check existence; this would fail on Fabric
        );
    }
}
