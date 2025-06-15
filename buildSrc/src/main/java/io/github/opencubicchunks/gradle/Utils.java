package io.github.opencubicchunks.gradle;

import javax.annotation.Nonnull;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;

public class Utils {

    @Nonnull
    public static JavaPluginConvention getJavaPluginConvention(@Nonnull Project target) {
        JavaPluginConvention convention = target.getConvention().findByType(JavaPluginConvention.class);
        if (convention == null) {
            convention = target.getConvention().findPlugin(JavaPluginConvention.class);
            if (convention == null) {
                convention = target.getConvention().getByType(JavaPluginConvention.class);
            }
        }
        return convention;
    }
}
