package io.github.opencubicchunks.gradle;

import static io.github.opencubicchunks.dasm.util.ResolutionUtils.applyImportsToMethodSignature;
import static io.github.opencubicchunks.dasm.util.ResolutionUtils.resolveType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.github.opencubicchunks.dasm.RedirectsParseException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public class DasmPlugin implements Plugin<Project> {
    private static final boolean TO_NAMED = false;

    private class Mappings {

        public Mappings.ClassMapping getClass(String name) {
            return null;
        }

        private class ClassMapping {
            private final String srcName;
            private final String dstName;

            public ClassMapping(String srcName, String dstName) {
                this.srcName = srcName;
                this.dstName = dstName;
            }

            public String getSrcName() {
                return srcName;
            }

            public String getDstName() {
                return dstName;
            }

            public MethodMapping getMethod(String name, String descriptor) {
                return null;
            }

            public FieldMapping getField(String name, String descriptor) {
                return null;
            }
        }

        public class MethodMapping {
            private final String srcName;
            private final String srcDesc;
            private final String dstName;
            private final String sdtDesc;

            public MethodMapping(String srcName, String srcDesc, String dstName, String sdtDesc) {

                this.srcName = srcName;
                this.srcDesc = srcDesc;
                this.dstName = dstName;
                this.sdtDesc = sdtDesc;
            }

            public String getSrcName() {
                return srcName;
            }

            public String getSrcDesc() {
                return srcDesc;
            }

            public String getDstName() {
                return dstName;
            }

            public String getSdtDesc() {
                return sdtDesc;
            }
        }

        public class FieldMapping {
            private final String srcName;
            private final String srcDesc;
            private final String dstName;
            private final String sdtDesc;

            public FieldMapping(String srcName, String srcDesc, String dstName, String sdtDesc) {

                this.srcName = srcName;
                this.srcDesc = srcDesc;
                this.dstName = dstName;
                this.sdtDesc = sdtDesc;
            }

            public String getSrcName() {
                return srcName;
            }

            public String getSrcDesc() {
                return srcDesc;
            }

            public String getDstName() {
                return dstName;
            }

            public String getSdtDesc() {
                return sdtDesc;
            }
        }
    }
    @Override public void apply(Project project) {
        project.afterEvaluate(proj -> {
            ProcessResources processResources = ((ProcessResources) project.getTasks().getByName("processResources"));
            Mappings mappings = new Mappings();

            File destinationDir = processResources.getDestinationDir();
            processResources.filesMatching("dasm/**/*.dasm", copySpec -> {
                copySpec.exclude();
                File file = copySpec.getFile();
                File output = copySpec.getRelativePath().getFile(destinationDir);
                processFile(file, output, mappings);
            });

        });
    }

    private void processFile(File file, File output, Mappings mappings) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            JsonObject parsed = new JsonParser().parse(bufferedReader).getAsJsonObject();
            if (file.getName().equals("targets.dasm")) {
                parsed = processTargets(parsed, mappings);
            } else {
                parsed = processSets(parsed, mappings);
            }
            Files.createDirectories(output.toPath().getParent());
            Files.writeString(output.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(parsed), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private JsonObject processSets(JsonObject parsed, Mappings mappings) throws RedirectsParseException {

        JsonObject output = new JsonObject();

        Map<String, String> globalImports = parseImports(parsed.get("imports").getAsJsonArray());

        JsonObject outputSets = new JsonObject();
        JsonElement sets = parsed.getAsJsonObject().get("sets");
        for (Map.Entry<String, JsonElement> set : sets.getAsJsonObject().entrySet()) {
            HashMap<String, String> globalImportsCopy = new HashMap<>(globalImports); // copied as local imports may modify it

            outputSets.add(set.getKey(), remapSet(mappings, set.getValue().getAsJsonObject(), globalImportsCopy));
        }
        output.add("sets", outputSets);

        return output;
    }

    @NotNull private Map<String, String> parseImports(JsonArray imports) {
        Map<String, String> outputImports = new HashMap<>();
        for (JsonElement importElement : imports) {
            String importString = importElement.getAsString();
            int lastDot = importString.lastIndexOf('.');

            outputImports.put(importString.substring(lastDot + 1), importString);
        }
        return outputImports;
    }

    private JsonObject remapSet(Mappings mappings, JsonObject value, Map<String, String> imports) throws RedirectsParseException {
        if (value.has("imports") && value.get("imports").isJsonArray()) {
            JsonArray localImports = value.get("imports").getAsJsonArray();
            imports.putAll(parseImports(localImports));
        }

        JsonObject output = new JsonObject();
        for (Map.Entry<String, JsonElement> setPart : value.entrySet()) {
            if (setPart.getKey().equals("typeRedirects")) {
                output.add(setPart.getKey(), remapTypeRedirects(mappings, setPart.getValue().getAsJsonObject(), imports));
            } else if (setPart.getKey().equals("fieldRedirects")) {
                output.add(setPart.getKey(), remapFieldRedirects(mappings, setPart.getValue().getAsJsonObject(), imports));
            } else if (setPart.getKey().equals("methodRedirects")) {
                output.add(setPart.getKey(), remapMethodRedirects(mappings, setPart.getValue().getAsJsonObject(), imports));
            } else {
                output.add(setPart.getKey(), setPart.getValue());
            }
        }
        return output;
    }

    private JsonObject remapTypeRedirects(Mappings mappings, JsonObject redirects, Map<String, String> imports) throws RedirectsParseException {
        JsonObject output = new JsonObject();
        for (Map.Entry<String, JsonElement> typeEntry : redirects.entrySet()) {
            String srcTypeName = resolveType(typeEntry.getKey(), imports);
            String dstTypeName = resolveType(typeEntry.getValue().getAsString(), imports);

            output.add(remapClassName(mappings, srcTypeName), new JsonPrimitive(dstTypeName));
        }
        return output;
    }

    private JsonObject remapFieldRedirects(Mappings mappings, JsonObject redirects, Map<String, String> imports) throws RedirectsParseException {
        JsonObject output = new JsonObject();
        for (Map.Entry<String, JsonElement> fieldJsonEntry : redirects.entrySet()) {
            String key = fieldJsonEntry.getKey();
            JsonElement fieldVal = fieldJsonEntry.getValue();
            String[] parts = key.split("\s*\\|\s*");
            String rawOwner = resolveType(parts[0], imports);
            String mappingsOwner = rawOwner;
            if (fieldJsonEntry.getValue().isJsonObject() && fieldJsonEntry.getValue().getAsJsonObject().get("mappingsOwner") != null) {
                mappingsOwner = resolveType(fieldJsonEntry.getValue().getAsJsonObject().get("mappingsOwner").getAsString(), imports);
                String newMappingsOwner = remapClassName(mappings, mappingsOwner);
                fieldVal.getAsJsonObject().add("mappingsOwner", new JsonPrimitive(newMappingsOwner));
            }
            String[] fieldParts = parts[1].split("\s+");
            String fieldDecl = resolveType(fieldParts[0], imports) + " " + fieldParts[1];
            Method fakeMethod = Method.getMethod(fieldDecl + "()");
            Type type = fakeMethod.getReturnType();
            String name = fakeMethod.getName();

            String newOwner = remapClassName(mappings, rawOwner);
            Type newType = remapType(mappings, type);
            String newName = remapFieldName(mappings, mappingsOwner, type, name);

            String newKey = newOwner + " | " + newType.getClassName() + " " + newName;

            output.add(newKey, fieldVal);
        }
        return output;
    }

    private JsonObject remapMethodRedirects(Mappings mappings, JsonObject redirects, Map<String, String> imports) throws RedirectsParseException {
        JsonObject output = new JsonObject();
        for (Map.Entry<String, JsonElement> methodJsonEntry : redirects.entrySet()) {
            String key = methodJsonEntry.getKey();
            JsonElement methodVal = methodJsonEntry.getValue();
            String[] parts = key.split("\s*\\|\s*");
            String rawOwner = resolveType(parts[0], imports);
            String mappingsOwner = rawOwner;
            if (methodJsonEntry.getValue().isJsonObject() && methodJsonEntry.getValue().getAsJsonObject().get("mappingsOwner") != null) {
                mappingsOwner = resolveType(methodJsonEntry.getValue().getAsJsonObject().get("mappingsOwner").getAsString(), imports);
                String newMappingsOwner = remapClassName(mappings, mappingsOwner);
                methodVal.getAsJsonObject().add("mappingsOwner", new JsonPrimitive(newMappingsOwner));
            }
            String methodDecl = applyImportsToMethodSignature(parts[1], imports);
            Method asmMethod = Method.getMethod(methodDecl);
            Type returnType = asmMethod.getReturnType();
            Type[] argumentTypes = asmMethod.getArgumentTypes();
            String name = asmMethod.getName();

            for (int i = 0; i < argumentTypes.length; i++) {
                argumentTypes[i] = remapType(mappings, argumentTypes[i]);
            }
            String newOwner = remapClassName(mappings, rawOwner);
            Type newReturnType = remapType(mappings, returnType);
            String newName = remapMethodName(mappings, mappingsOwner, asmMethod);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(newOwner).append(" | ");
            stringBuilder.append(newReturnType.getClassName()).append(" ").append(newName).append("(");
            stringBuilder.append(Arrays.stream(argumentTypes).map(arg -> arg.getClassName()).collect(Collectors.joining(", ")));
            stringBuilder.append(")");
            String newKey = stringBuilder.toString();

            output.add(newKey, methodVal);
        }
        return output;
    }

    private JsonObject processTargets(JsonElement parsed, Mappings mappings) throws RedirectsParseException {
        JsonObject out = new JsonObject();
        JsonObject json = parsed.getAsJsonObject();

        Map<String, String> imports;
        if (json.has("imports") && json.get("imports").isJsonArray()) {
            imports = parseImports(json.get("imports").getAsJsonArray());
        } else {
            imports = new HashMap<>();
        }

        JsonElement targets = json.get("targets");
        JsonObject targetsOutput = new JsonObject();
        Set<Map.Entry<String, JsonElement>> targetEntries = new HashSet<>(targets.getAsJsonObject().entrySet());

        // remap targets object
        for (Map.Entry<String, JsonElement> targetEntry : targetEntries) {
            String className = resolveType(targetEntry.getKey(), imports);
            String dstName = remapClassName(mappings, className);

            JsonObject newClassEntry = remapClassValue(className, mappings, targetEntry, imports);
            targetsOutput.add(dstName, newClassEntry);
        }

        // copy json entries to output
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (entry.getKey().equals("targets")) {
                out.add("targets", targetsOutput); // use our remapped targets object
            } else { //otherwise just copy everything else unmodified
                out.add(entry.getKey(), entry.getValue());
            }
        }
        return out;
    }

    private static JsonObject remapClassValue(String ownerName, Mappings mappings, Map.Entry<String, JsonElement> jsonEntry, Map<String, String> imports)
        throws RedirectsParseException {
        JsonObject classEntry = jsonEntry.getValue().getAsJsonObject();
        JsonObject newClassEntry = new JsonObject();
        for (Map.Entry<String, JsonElement> classEntryJsonEntry : classEntry.entrySet()) {
            if (classEntryJsonEntry.getKey().equals("targetMethods")) {
                JsonObject newTargetMethods = new JsonObject();
                for (Map.Entry<String, JsonElement> methodJsonEntry : classEntryJsonEntry.getValue().getAsJsonObject().entrySet()) {
                    JsonElement methodVal = methodJsonEntry.getValue();
                    String methodOwner;
                    if (methodVal.isJsonObject() && methodVal.getAsJsonObject().get("mappingsOwner") != null) {
                        String mappingsOwner = resolveType(methodVal.getAsJsonObject().get("mappingsOwner").getAsString(), imports);
                        String newMappingsOwner = remapClassName(mappings, mappingsOwner);
                        methodVal.getAsJsonObject().add("mappingsOwner", new JsonPrimitive(newMappingsOwner));
                        methodOwner = mappingsOwner;
                    } else {
                        methodOwner = resolveType(ownerName, imports);
                    }
                    String methodKey = applyImportsToMethodSignature(methodJsonEntry.getKey(), imports);
                    Method asmMethod = Method.getMethod(methodKey);
                    Type returnType = asmMethod.getReturnType();
                    Type newReturnType = remapType(mappings, returnType);

                    Type[] argumentTypes = asmMethod.getArgumentTypes();
                    for (int i = 0; i < argumentTypes.length; i++) {
                        argumentTypes[i] = remapType(mappings, argumentTypes[i]);
                    }

                    String mappedName = remapMethodName(mappings, methodOwner, asmMethod);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(newReturnType.getClassName()).append(" ").append(mappedName).append("(");
                    stringBuilder.append(Arrays.stream(argumentTypes).map(arg -> arg.getClassName()).collect(Collectors.joining(", ")));
                    stringBuilder.append(")");
                    String newKey = stringBuilder.toString();
                    newTargetMethods.add(newKey, methodVal);
                }
                newClassEntry.add(classEntryJsonEntry.getKey(), newTargetMethods);
            } else {
                newClassEntry.add(classEntryJsonEntry.getKey(), classEntryJsonEntry.getValue());
            }
        }
        return newClassEntry;
    }

    private static String remapMethodName(Mappings mappings, String methodOwner, Method asmMethod) {
        Mappings.ClassMapping mapEntry = mappings.getClass(methodOwner.replace('.', '/'));
        if (mapEntry == null) {
            return asmMethod.getName();
        }
        Mappings.MethodMapping method = mapEntry.getMethod(asmMethod.getName(), asmMethod.getDescriptor());
        if (method == null) {
            return asmMethod.getName();
        }
        return method.getDstName();
    }

    private static String remapFieldName(Mappings mappings, String fieldOwner, Type type, String name) {
        Mappings.ClassMapping mapEntry = mappings.getClass(fieldOwner.replace('.', '/'));
        if (mapEntry == null) {
            return name;
        }
        Mappings.FieldMapping field = mapEntry.getField(name, type.getDescriptor());
        if (field == null) {
            return name;
        }
        return field.getDstName();
    }

    private static String remapClassName(Mappings mappings, String className) {
        Mappings.ClassMapping mapEntry = mappings.getClass(className.replace('.', '/'));
        String dstName = mapEntry == null ? className : mapEntry.getDstName().replace('/', '.');
        return dstName;
    }

    private static Type remapType(Mappings mappings, Type type) {
        switch (type.getSort()) {
            case Type.VOID:
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.FLOAT:
            case Type.LONG:
            case Type.DOUBLE:
                return type;
            case Type.ARRAY:
                return Type.getType('[' + remapType(mappings, type.getElementType()).getDescriptor());
            case Type.OBJECT:
                Mappings.ClassMapping mapEntry = mappings.getClass(type.getInternalName());
                if (mapEntry == null) {
                    return type;
                }
                return Type.getObjectType(mapEntry.getDstName());
            default:
                throw new IllegalArgumentException("Invalid type sort: " + type.getSort());
        }
    }
}
