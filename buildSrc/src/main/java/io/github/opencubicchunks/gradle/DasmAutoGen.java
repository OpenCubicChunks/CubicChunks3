package io.github.opencubicchunks.gradle;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.Nonnull;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;

public class DasmAutoGen implements Plugin<Project> {

    @Override public void apply(@Nonnull Project target) {
        DasmGenExtension dasmExtension = new DasmGenExtension();
        target.getExtensions().add("dasmGen", dasmExtension);
        Task generateDasmConfigs = target.getTasks().create("generateDasmConfigs");
        generateDasmConfigs.setGroup("filegen");
        generateDasmConfigs.doLast(task -> {
            JavaPluginConvention convention = Utils.getJavaPluginConvention(target);
            try {
                dasmExtension.generateFiles(convention);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
