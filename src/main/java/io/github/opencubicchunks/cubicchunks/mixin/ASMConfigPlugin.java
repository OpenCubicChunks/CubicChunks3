package io.github.opencubicchunks.cubicchunks.mixin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.transformer.Transformer;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

public class ASMConfigPlugin implements IMixinConfigPlugin {
    private final Map<String, Boolean> dasmTransformedInPreApply = new ConcurrentHashMap<>();
    private final Transformer transformer;
    private final AnnotationParser annotationParser;

    private final Map<String, TargetClass> preApplyTargets = new HashMap<>();
    private final Map<String, TargetClass> postApplyTargets = new HashMap<>();

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
        ClassProvider classProvider = new CachingClassProvider(s -> {
            try (var classStream = ASMConfigPlugin.class.getClassLoader().getResourceAsStream(s.replace(".", "/") + ".class")) {
                return classStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.transformer = new Transformer(mappings, classProvider, developmentEnvironment);
        this.annotationParser = new AnnotationParser(classProvider, GeneralSet.class);
    }

    @Override public void onLoad(String mixinPackage) {
    }

    @Override public String getRefMapperConfig() {
        return null;
    }

    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            ClassNode targetClass = MixinService.getService().getBytecodeProvider().getClassNode(targetClassName);
            ClassNode mixinClass = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);

            // PRE_APPLY
            var targetPreApply = new TargetClass(targetClassName);
            Set<RedirectSet> redirectSetsPreApply = new HashSet<>();
            this.annotationParser.findRedirectSets(targetClassName, mixinClass, redirectSetsPreApply);
            this.annotationParser.buildClassTarget(mixinClass, targetPreApply, TransformFrom.ApplicationStage.PRE_APPLY, "cc_dasm$");
            this.annotationParser.findRedirectSets(targetClassName, targetClass, redirectSetsPreApply);
            this.annotationParser.buildClassTarget(targetClass, targetPreApply, TransformFrom.ApplicationStage.PRE_APPLY, "cc_dasm$");
            redirectSetsPreApply.forEach(targetPreApply::addRedirectSet);
            this.preApplyTargets.put(mixinClassName + "|" + targetClassName, targetPreApply);

            // POST_APPLY
            var targetPostApply = new TargetClass(targetClassName);
            Set<RedirectSet> redirectSetsPostApply = new HashSet<>();
            this.annotationParser.findRedirectSets(targetClassName, mixinClass, redirectSetsPostApply);
            this.annotationParser.buildClassTarget(mixinClass, targetPostApply, TransformFrom.ApplicationStage.POST_APPLY, "cc_dasm$");
            this.annotationParser.findRedirectSets(targetClassName, targetClass, redirectSetsPostApply);
            this.annotationParser.buildClassTarget(targetClass, targetPostApply, TransformFrom.ApplicationStage.POST_APPLY, "cc_dasm$");
            redirectSetsPostApply.forEach(targetPostApply::addRedirectSet);
            this.postApplyTargets.put(mixinClassName + "|" + targetClassName, targetPostApply);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
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
        TargetClass target = null;
        switch (stage) {
            case PRE_APPLY -> target = preApplyTargets.get(mixinClassName + "|" + targetClassName);
            case POST_APPLY -> target = postApplyTargets.get(mixinClassName + "|" + targetClassName);
        }

        if (target.targetMethods().isEmpty() && target.wholeClass() == null) {
            return false;
        }

        if (target.wholeClass() != null) {
            this.transformer.transformClass(targetClass, target);
        } else {
            this.transformer.transformClass(targetClass, target);
        }
        return true;
    }
}