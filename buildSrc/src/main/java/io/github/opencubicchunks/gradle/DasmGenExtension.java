package io.github.opencubicchunks.gradle;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.apache.tools.ant.util.StringUtils.removePrefix;
import static org.apache.tools.ant.util.StringUtils.removeSuffix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.stream.JsonWriter;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

@SuppressWarnings("unused")
public class DasmGenExtension {

    Pattern pattern = Pattern.compile("(?<indent> *)@(Dasm|TransformFromClass).+? (interface|class|record) (?<name>\\w+)?", Pattern.DOTALL);

    private final Map<SourceSet, Map<String, Action<DasmConfig>>> configsBySourceSet = new HashMap<>();

    private String filePattern = "dasm.json";

    private String defaultMinVersion;

    DasmGenExtension() {}

    public void setFilePattern(String pattern) {
        this.filePattern = pattern;
    }

    public String getFilePattern() {
        return this.filePattern;
    }

    public String getDefaultMinVersion() {
        return defaultMinVersion;
    }

    public void setDefaultMinVersion(String defaultMinVersion) {
        this.defaultMinVersion = defaultMinVersion;
    }

    public void config(SourceSet sourceSet, String name, Action<DasmConfig> configure) {
        configsBySourceSet.computeIfAbsent(sourceSet, s -> new HashMap<>()).put(name, configure);
    }

    public static class DasmConfig {
        private String packageName;
        private String minVersion;

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(String minVersion) {
            this.minVersion = minVersion;
        }
    }

    void generateFiles(JavaPluginConvention convention) throws IOException {
        convention.getSourceSets().forEach(sourceSet -> {
            Map<String, Action<DasmConfig>> configs = configsBySourceSet.get(sourceSet);
            if (configs == null) {
                return;
            }

            Set<File> resourcesSet = sourceSet.getResources().getSrcDirs();
            Path resources;
            try {
                resources = resourcesSet.iterator().next().getCanonicalFile().toPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (String name : configs.keySet()) {
                DasmConfig config = new DasmConfig();
                Action<DasmConfig> configure = configs.get(name);
                if (defaultMinVersion != null) {
                    config.minVersion = defaultMinVersion;
                }
                configure.execute(config);
                String fileName = String.format(filePattern, name);

                Path path = resources.resolve(fileName);
                try {
                    Files.createDirectories(resources);
                    try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(path, CREATE, TRUNCATE_EXISTING))) {
                        writer.setIndent("    ");
                        writer.beginObject();
                        if (config.packageName != null) {
                            writer.name("package").value(config.packageName);
                        }
                        if (config.minVersion != null) {
                            writer.name("minVersion").value(config.minVersion);
                        }

                        writeDasm(convention, sourceSet, name, config, writer);

                        writer.endObject();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void writeDasm(JavaPluginConvention convention, SourceSet sourceSet, String name, DasmConfig config, JsonWriter writer)
            throws IOException {
        Set<String> dasmTypes = getDasmClasses(config, sourceSet.getAllJava());

        Set<Path> classesSet = new HashSet<>();
        Set<Path> interfacesSet = new HashSet<>();

        Path prefixPath = Paths.get(config.packageName.replace('.', '/'));

        Function<String, String> transform = path -> removePrefix(path.replace(File.separatorChar, '.'), name + ".");

        List<String> types = dasmTypes.stream().map(transform).sorted(Comparator.comparing(a -> a.toLowerCase(Locale.ROOT))).toList();

        writer.name("dasm").beginArray();
        for (String path : types) {
            writer.value(path);
        }
        writer.endArray();
    }

    private Set<String> getDasmClasses(DasmConfig config, SourceDirectorySet allJava) throws IOException {
        System.out.println("GetDasm Classes");
        Set<Path> srcPaths = new HashSet<>();
        for (File file : allJava.getSrcDirs()) {
            Path toPath = file.getCanonicalFile().toPath();
            System.out.println("GetDasm " + toPath);

            srcPaths.add(toPath);
        }

        Set<String> dasmTypes = new HashSet<>();
        for (File it : allJava) {
            Path javaClass = it.getCanonicalFile().toPath();
            System.out.println("Class:  " + javaClass);
            for (Path srcPath : srcPaths) {
                if (javaClass.startsWith(srcPath)) {
                    String relative = srcPath.relativize(javaClass).toString();
                    if (relative.replace(File.separatorChar, '.').startsWith(config.packageName) && !relative.endsWith("package-info.java")) {
                        // This is an abomination. Ideally we'd parse .java files with a library
                        // This approach works fine for dasm on the outer class and its direct children, any other @Dasm annotation will be
                        // improperly parsed. (Also assumes our indentation is correct in all java files.)
                        Matcher matcher = pattern.matcher(Files.readString(javaClass));

                        String clazz = removeSuffix(relative, ".java");
                        while (matcher.find()) {
                            String indent = matcher.group("indent");
                            String name = matcher.group("name");

                            String path = indent.isEmpty() ? clazz : clazz + "$" + name;

                            dasmTypes.add(path);
                        }
                    }
                }
            }
        }
        return dasmTypes;
    }
}
