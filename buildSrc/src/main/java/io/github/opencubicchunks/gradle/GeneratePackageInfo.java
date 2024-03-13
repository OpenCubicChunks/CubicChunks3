package io.github.opencubicchunks.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSet;

public class GeneratePackageInfo {

    public static void generateFiles(SourceSet sourceSet) throws IOException {
        Map<Path, Path> packages = getPackages(sourceSet.getAllJava());
        String code = """
            @ParametersAreNonnullByDefault
            @MethodsReturnNonnullByDefault
            package __PACKAGE__;
                        
            import javax.annotation.ParametersAreNonnullByDefault;
                        
            import io.github.opencubicchunks.cc_core.annotation.MethodsReturnNonnullByDefault;""";
        for (Path pkg : packages.keySet()) {
            Path absolutePath = packages.get(pkg);
            Path file = absolutePath.resolve("package-info.java");
            if (!Files.exists(file)) {
                String packageName = code.replace("__PACKAGE__", pkg.toString().replaceAll("[/\\\\]", "."));
                Files.write(absolutePath.resolve("package-info.java"), List.of(packageName));
            }
        }
    }

    private static Map<Path, Path> getPackages(SourceDirectorySet allJava) throws IOException {
        Set<Path> srcPaths = new HashSet<>();
        for (File file : allJava.getSrcDirs()) {
            Path toPath = file.getCanonicalFile().toPath();
            srcPaths.add(toPath);
        }
        Map<Path, Path> packages = new HashMap<>();
        for (File it : allJava) {
            Path javaClass = it.getCanonicalFile().toPath();
            if (javaClass.toString().contains("mixin/test")) {
                // junit will try to load package-info files (when scanning for tests) causing mixin to throw as classes in mixin packages must not be loaded.
                continue;
            }
            for (Path srcPath : srcPaths) {
                if (javaClass.startsWith(srcPath) && javaClass.toString().endsWith(".java")) {
                    Path relative = srcPath.relativize(javaClass);
                    packages.put(relative.getParent(), javaClass.getParent());
                }
            }
        }
        return packages;
    }
}
