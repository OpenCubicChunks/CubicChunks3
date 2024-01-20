package io.github.opencubicchunks.cubicchunks.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import io.github.opencubicchunks.dasm.MappingsProvider;
import io.github.opencubicchunks.dasm.RedirectsParseException;
import io.github.opencubicchunks.dasm.RedirectsParser;
import io.github.opencubicchunks.dasm.Transformer;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.MixinService;

public class ASMConfigPlugin implements IMixinConfigPlugin {
    private final Map<String, Boolean> dasmTransformedInPreApply = new ConcurrentHashMap<>();
    private final Map<String, RedirectsParser.RedirectSet> redirectSetByName = new HashMap<>();
    private final Throwable constructException;

    private final Transformer transformer;

    public ASMConfigPlugin() {
        boolean developmentEnvironment = false;
        try {
            developmentEnvironment = !FMLEnvironment.production;
        } catch (Throwable ignored) {
        }
        MappingsProvider mappings = new MappingsProvider() {

            @Override public String mapFieldName(String owner, String fieldName, String descriptor) {
                return fieldName;
            }

            @Override public String mapMethodName(String owner, String methodName, String descriptor) {
                return methodName;
            }

            @Override public String mapClassName(String className) {
                return className;
            }
        };

        // TODO: breaks on fabric (remapped at runtime)
        this.transformer = new Transformer(mappings, s -> {
            try (var classStream = ASMConfigPlugin.class.getClassLoader().getResourceAsStream(s.replace(".", "/") + ".class")){
                return classStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, developmentEnvironment);

        List<RedirectsParser.RedirectSet> redirectSets;
        try {
            //TODO: add easy use of multiple set and target json files
            redirectSets = loadSetsFile("dasm/sets/sets.dasm");

            for (RedirectsParser.RedirectSet redirectSet : redirectSets) {
                redirectSetByName.put(redirectSet.getName(), redirectSet);
            }
        } catch (Throwable e) {
            constructException = e; // Annoying because mixin catches Throwable for creating a config plugin >:(
            return;
        }
        constructException = null;
    }

    @Override public void onLoad(String mixinPackage) {
        if (this.constructException != null) {
            throw new Error(this.constructException); // throw error because Mixin catches Exception for onLoad
        }
    }

    @Override public String getRefMapperConfig() {
        return null;
    }

    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Nullable
    @Override public List<String> getMixins() {
        return null;
    }

    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        var wasTransformed = transformClass(targetClassName, targetClass, mixinClassName, TransformFrom.ApplicationStage.PRE_APPLY);
        dasmTransformedInPreApply.put(mixinClassName + "|" + targetClassName, wasTransformed);

        try {
            // ugly hack to add class metadata to mixin
            // based on https://github.com/Chocohead/OptiFabric/blob/54fc2ef7533e43d1982e14bc3302bcf156f590d8/src/main/java/me/modmuss50/optifabric/compat/fabricrendererapi
            // /RendererMixinPlugin.java#L25:L44
            Method addMethod = ClassInfo.class.getDeclaredMethod("addMethod", MethodNode.class, boolean.class);
            addMethod.setAccessible(true);

            ClassInfo ci = ClassInfo.forName(targetClassName);
            Set<String> existingMethods = ci.getMethods().stream().map(x -> x.getName() + x.getDesc()).collect(Collectors.toSet());
            for (MethodNode method : targetClass.methods) {
                if (!existingMethods.contains(method.name + method.desc)) {
                    addMethod.invoke(ci, method, false);
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Apply POST_APPLY dasm transforms
        boolean wasTransformed = transformClass(targetClassName, targetClass, mixinClassName, TransformFrom.ApplicationStage.POST_APPLY);

        // If no DASM transformation happened to this class, we can skip removing the prefixed methods
        if (!(wasTransformed | dasmTransformedInPreApply.get(mixinClassName + "|" + targetClassName)))
            return;

        // Find all DASM-added method nodes and their corresponding MixinMerged method nodes
        record PrefixMethodPair(MethodNode dasmAddedMethod, MethodNode mixinAddedMethod) { }
        List<PrefixMethodPair> methodPairs = new ArrayList<>();
        for (MethodNode methodNode : targetClass.methods) {
            if (methodNode.name.contains("cc_dasm$")) {
                var methodNameWithoutPrefix = methodNode.name.substring(methodNode.name.indexOf("$") + 1);
                var mixinAddedMethod = targetClass.methods.stream()
                    .filter(m -> m.name.equals(methodNameWithoutPrefix) && m.desc.equals(methodNode.desc))
                    .findFirst();

                if (mixinAddedMethod.isEmpty()) {
                    CubicChunks.LOGGER.info(String.format("Found DASM added method `%s` without a corresponding MixinMerged method", methodNameWithoutPrefix));
                }
                methodPairs.add(new PrefixMethodPair(methodNode, mixinAddedMethod.orElse(null)));
            }
        }

        // Remove the mixin-added methods and set the dasm-added names
        methodPairs.forEach(prefixMethodPair -> {
            if (prefixMethodPair.mixinAddedMethod != null) {
                targetClass.methods.remove(prefixMethodPair.mixinAddedMethod);
            }
            prefixMethodPair.dasmAddedMethod.name = prefixMethodPair.dasmAddedMethod.name.substring(
                prefixMethodPair.dasmAddedMethod.name.indexOf("$") + 1
            );
        });
    }

    /**
     * @return Whether any transformation was done to the targetClass
     */
    private boolean transformClass(String targetClassName, ClassNode targetClass, String mixinClassName, TransformFrom.ApplicationStage stage) {
        ClassNode mixinClass;
        try {
            mixinClass = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        var target = new RedirectsParser.ClassTarget(targetClassName);
        Set<RedirectsParser.RedirectSet> redirectSets = new HashSet<>();

        findRedirectSets(targetClassName, mixinClass, redirectSets);
        buildClassTarget(mixinClass, target, stage, "cc_dasm$");
        findRedirectSets(targetClassName, targetClass, redirectSets);
        buildClassTarget(targetClass, target, stage, "cc_dasm$");

        if (target.getTargetMethods().isEmpty() && target.wholeClass() == null) {
            return false;
        }

        if (target.wholeClass() != null) {
            this.transformer.transformClass(targetClass, target, redirectSets.stream().toList());
        } else {
            this.transformer.transformClass(targetClass, target, redirectSets.stream().toList());
        }
        return true;
    }

    private void findRedirectSets(String targetClassName, ClassNode targetClass, Set<RedirectsParser.RedirectSet> redirectSets) {
        if (targetClass.invisibleAnnotations == null) {
            return;
        }
        for (AnnotationNode ann : targetClass.invisibleAnnotations) {
            if (!ann.desc.equals("Lio/github/opencubicchunks/cubicchunks/mixin/DasmRedirect;")) {
                continue;
            }
            // The name value pairs of this annotation. Each name value pair is stored as two consecutive
            // elements in the list. The name is a String, and the value may be a
            // Byte, Boolean, Character, Short, Integer, Long, Float, Double, String or org.objectweb.asm.Type,
            // or a two elements String array (for enumeration values), an AnnotationNode,
            // or a List of values of one of the preceding types. The list may be null if there is no name value pair.
            List<Object> values = ann.values;
            if (values == null) {
                redirectSets.add(redirectSetByName.get("general"));
                continue;
            }
            List<String> useSets = null;
            for (int i = 0, valuesSize = values.size(); i < valuesSize; i += 2) {
                String name = (String) values.get(i);
                Object value = values.get(i + 1);
                if (name.equals("value")) {
                    useSets = (List<String>) value;
                }
            }
            if (useSets == null) {
                redirectSets.add(redirectSetByName.get("general"));
                continue;
            }
            for (String useSet : useSets) {
                RedirectsParser.RedirectSet redirectSet = redirectSetByName.get(useSet);
                if (redirectSet == null) {
                    throw new IllegalArgumentException("No redirect set " + useSet + ", targetClass=" + targetClassName);
                }
                redirectSets.add(redirectSet);
            }
        }
    }

    private static void buildClassTarget(ClassNode targetClass, RedirectsParser.ClassTarget classTarget, TransformFrom.ApplicationStage stage, String methodPrefix) {
        if (targetClass.invisibleAnnotations == null) {
            return;
        }
        for (AnnotationNode ann : targetClass.invisibleAnnotations) {
            if (!ann.desc.equals("Lio/github/opencubicchunks/cubicchunks/mixin/TransformFromClass;") || ann.values == null) {
                continue;
            }

            List<Object> values = ann.values;
            Type srcClass = null;
            TransformFrom.ApplicationStage requestedStage = TransformFrom.ApplicationStage.PRE_APPLY;
            for (int i = 0, valuesSize = values.size(); i < valuesSize; i += 2) {
                String name = (String) values.get(i);
                Object value = values.get(i + 1);
                if (name.equals("value")) {
                    Type val = (Type) value;
                    if (!Objects.equals(val, Type.getObjectType(Object.class.getName()))) { // Special case the default
                        srcClass = val;
                    }
                } else if (name.equals("stage")) {
                    var parts = ((String[]) value);
                    requestedStage = TransformFrom.ApplicationStage.valueOf(parts[1]);
                }
            }
            if (stage != requestedStage) {
                continue;
            }
            classTarget.targetWholeClass(srcClass);
        }

        for (Iterator<MethodNode> iterator = targetClass.methods.iterator(); iterator.hasNext(); ) {
            MethodNode method = iterator.next();
            if (method.invisibleAnnotations == null) {
                continue;
            }

            for (AnnotationNode ann : method.invisibleAnnotations) {
                if (!ann.desc.equals("Lio/github/opencubicchunks/cubicchunks/mixin/TransformFrom;")) {
                    continue;
                }
                iterator.remove();

                // The name value pairs of this annotation. Each name value pair is stored as two consecutive
                // elements in the list. The name is a String, and the value may be a
                // Byte, Boolean, Character, Short, Integer, Long, Float, Double, String or org.objectweb.asm.Type,
                // or a two elements String array (for enumeration values), an AnnotationNode,
                // or a List of values of one of the preceding types. The list may be null if there is no name value pair.
                List<Object> values = ann.values;
                String targetName = null;
                boolean makeSyntheticAccessor = false;
                String desc = null;
                TransformFrom.ApplicationStage requestedStage = TransformFrom.ApplicationStage.PRE_APPLY;
                Type srcOwner = null;
                for (int i = 0, valuesSize = values.size(); i < valuesSize; i += 2) {
                    String name = (String) values.get(i);
                    Object value = values.get(i + 1);
                    switch (name) {
                        case "value" -> targetName = (String) value;
                        case "makeSyntheticAccessor" -> makeSyntheticAccessor = (Boolean) value;
                        case "signature" -> desc = parseMethodDescriptor((AnnotationNode) value);
                        case "stage" -> {
                            var parts = ((String[]) value);
                            requestedStage = TransformFrom.ApplicationStage.valueOf(parts[1]);
                        }
                        case "copyFrom" -> {
                            var val = (Type) value;
                            if (!Objects.equals(val, Type.getObjectType(Object.class.getName()))) { // Special case the default
                                srcOwner = val;
                            }
                        }
                    }
                }
                if (stage != requestedStage) {
                    continue;
                }

                if (desc == null) {
                    int split = targetName.indexOf('(');
                    desc = targetName.substring(split);
                    targetName = targetName.substring(0, split);
                }
                RedirectsParser.ClassTarget.TargetMethod targetMethod;
                if (srcOwner == null) {
                     targetMethod = new RedirectsParser.ClassTarget.TargetMethod(
                        new Transformer.ClassMethod(Type.getObjectType(targetClass.name), new org.objectweb.asm.commons.Method(targetName, desc)),
                         methodPrefix + method.name, // Name is modified here to prevent mixin from overwriting it. We remove this prefix in postApply.
                        true, makeSyntheticAccessor
                    );
                } else {
                    targetMethod = new RedirectsParser.ClassTarget.TargetMethod(
                        srcOwner,
                        new Transformer.ClassMethod(Type.getObjectType(targetClass.name), new org.objectweb.asm.commons.Method(targetName, desc)),
                        methodPrefix + method.name, // Name is modified here to prevent mixin from overwriting it. We remove this prefix in postApply.
                        true, makeSyntheticAccessor
                    );
                }
                if (classTarget.getTargetMethods().stream().anyMatch(t -> t.method().method.equals(targetMethod.method().method))) {
                    throw new RuntimeException(String.format("Trying to add duplicate TargetMethod to %s:\n\t\t\t\t%s | %s", classTarget.getClassName(), targetMethod.method().owner,
                        targetMethod.method().method));
                }
                classTarget.addTarget(targetMethod);
            }
        }
    }

    private static String parseMethodDescriptor(AnnotationNode ann) {
        if (ann == null) {
            return null;
        }
        List<Object> values = ann.values;

        Type ret = null;
        List<Type> args = null;
        boolean useFromString = false;
        for (int i = 0, valuesSize = values.size(); i < valuesSize; i += 2) {
            String name = (String) values.get(i);
            Object value = values.get(i + 1);
            switch (name) {
                case "ret" -> ret = (Type) value;
                case "args" -> args = (List<Type>) value;
                case "useFromString" -> useFromString = (Boolean) value;
            }
        }
        if (useFromString) {
            return null;
        }
        return Type.getMethodDescriptor(ret, args.toArray(new Type[0]));
    }

    private JsonElement parseFileAsJson(String fileName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream(fileName)) {
            return new JsonParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<RedirectsParser.ClassTarget> loadTargetsFile(String fileName) throws RedirectsParseException {
        RedirectsParser redirectsParser = new RedirectsParser();

        JsonElement targetsJson = parseFileAsJson(fileName);
        return redirectsParser.parseClassTargets(targetsJson.getAsJsonObject());
    }

    private List<RedirectsParser.RedirectSet> loadSetsFile(String fileName) throws RedirectsParseException {
        RedirectsParser redirectsParser = new RedirectsParser();

        JsonObject setsJson = parseFileAsJson(fileName).getAsJsonObject();
        JsonElement sets = setsJson.get("sets");
        JsonElement globalImports = setsJson.get("imports");

        if (globalImports == null) {
            return redirectsParser.parseRedirectSet(sets.getAsJsonObject());
        } else {
            return redirectsParser.parseRedirectSet(sets.getAsJsonObject(), globalImports);
        }
    }
}