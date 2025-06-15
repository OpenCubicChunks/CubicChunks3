package io.github.opencubicchunks.gradle;

import java.io.File;

import com.diffplug.spotless.FormatterStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixAnnotationsFormatterStep implements FormatterStep {
    @Override public String getName() {
        return "cc_fix_annotations";
    }

    private static final String[] ANNOTATIONS = { "@Shadow", "@Final", "@Mutable", "@Public", "@Override", "@Nullable", "@NotNull", "@Invoker",
        "@Accessor", "@Dynamic" };

    @Override public @Nullable String format(String s, @NotNull File file) {
        var outputString = s;
        for (var annotation : ANNOTATIONS) {
            // Remove newlines after the listed annotations
            outputString = outputString.replaceAll("(" + annotation + "(:?\\([^()\\n]*\\))?)\\n\\h*", "$1 ");
        }
        return outputString;
    }

    @Override public void close() {

    }
}
