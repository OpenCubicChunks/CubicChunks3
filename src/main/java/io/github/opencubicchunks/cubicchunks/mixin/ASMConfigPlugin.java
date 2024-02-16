package io.github.opencubicchunks.cubicchunks.mixin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.transformer.ClassTransform;
import io.github.notstirred.dasm.transformer.MethodTransform;
import io.github.notstirred.dasm.transformer.Transformer;
import io.github.notstirred.dasm.util.CachingClassProvider;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.Either;
import io.github.opencubicchunks.cubicchunks.CubicChunks;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.service.MixinService;

public class ASMConfigPlugin implements IMixinConfigPlugin {
    private final Map<String, Boolean> dasmTransformedInPreApply = new ConcurrentHashMap<>();
    private final Transformer transformer;
    private final AnnotationParser annotationParser;

    private final Map<String, Either<ClassTransform, Collection<MethodTransform>>> preApplyTargets = new HashMap<>();
    private final Map<String, Either<ClassTransform, Collection<MethodTransform>>> postApplyTargets = new HashMap<>();

    public ASMConfigPlugin() {
        boolean developmentEnvironment = false;
        try {
            developmentEnvironment = !FMLEnvironment.production;
        } catch (Throwable ignored) {
        }
        MappingsProvider mappings = MappingsProvider.IDENTITY;

        // TODO: breaks on fabric (remapped at runtime)
        ClassNodeProvider classProvider = new CachingClassProvider(s -> {
            try (var classStream = ASMConfigPlugin.class.getClassLoader().getResourceAsStream(s.replace(".", "/") + ".class")) {
                return Optional.ofNullable(classStream.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.transformer = new Transformer(classProvider, mappings);
        this.annotationParser = new AnnotationParser(classProvider);
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
            // rename the mixin class to get dasm to generate owners correctly
            mixinClass.name = targetClass.name;

            // PRE_APPLY
            this.annotationParser.findRedirectSets(mixinClass);
            var methodTransformsMixin = this.annotationParser.buildMethodTargets(mixinClass, "cc_dasm$");
            this.annotationParser.findRedirectSets(targetClass);
            var classTransform = this.annotationParser.buildClassTarget(targetClass);
            var methodTransformsTarget = this.annotationParser.buildMethodTargets(targetClass, "cc_dasm$");

            var methodTransforms = Stream.of(methodTransformsTarget, methodTransformsMixin)
                .filter(Optional::isPresent).map(Optional::get)
                .flatMap(Collection::stream).toList();

            String key = mixinClassName + "|" + targetClassName;
            if (classTransform.isPresent())  {
                // TODO: nice error
                assert methodTransformsMixin.isEmpty() && methodTransforms.isEmpty() : "Whole class transform WITH method transforms?";
                ClassTransform transform = classTransform.get();
                if (transform.stage() == ApplicationStage.PRE_APPLY) {
                    this.preApplyTargets.put(key, Either.left(transform));
                } else {
                    this.postApplyTargets.put(key, Either.left(transform));
                }
            } else {
                Collection<MethodTransform> preTransforms = this.preApplyTargets.computeIfAbsent(key, k -> Either.right(new ArrayList<>())).right().get();
                Collection<MethodTransform> postTransforms = this.postApplyTargets.computeIfAbsent(key, k -> Either.right(new ArrayList<>())).right().get();
                methodTransforms.forEach(transform -> {
                    if (transform.stage() == ApplicationStage.PRE_APPLY) {
                        preTransforms.add(transform);
                    } else {
                        postTransforms.add(transform);
                    }
                });
            }
        } catch (ClassNotFoundException | IOException | DasmWrappedExceptions e) {
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
        boolean wasTransformed;
        try {
            wasTransformed = transformClass(targetClassName, targetClass, mixinClassName, ApplicationStage.PRE_APPLY);
        } catch (DasmException e) {
            throw new RuntimeException(e);
        }
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
        boolean wasTransformed;
        try {
            wasTransformed = transformClass(targetClassName, targetClass, mixinClassName, ApplicationStage.POST_APPLY);
        } catch (DasmException e) {
            throw new RuntimeException(e);
        }
        for (MethodNode methodNode : targetClass.methods) {
            if (methodNode.name.contains("cc_dasm$")) {
                int asd = 0;
            }
        }
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

            prefixMethodPair.dasmAddedMethod.name = prefixMethodPair.dasmAddedMethod.name
                .replace("__init__", "<init>")
                .replace("__clinit__", "<clinit>");

            // remove the prefix
            prefixMethodPair.dasmAddedMethod.name = prefixMethodPair.dasmAddedMethod.name.substring("cc_dasm$".length());
        });

        ClassWriter classWriter = new ClassWriter(0);
        targetClass.accept(classWriter);
        try {
            Path path = Path.of(".dasm.out/" + "POSTIER_APPLY" + "/" + targetClassName.replace('.', '/') + ".class").toAbsolutePath();
            Files.createDirectories(path.getParent());
            Files.write(path, classWriter.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Whether any transformation was done to the targetClass
     */
    private boolean transformClass(String targetClassName, ClassNode targetClass, String mixinClassName, ApplicationStage stage) throws DasmException {
        Either<ClassTransform, Collection<MethodTransform>> target = null;
        switch (stage) {
            case PRE_APPLY -> target = preApplyTargets.get(mixinClassName + "|" + targetClassName);
            case POST_APPLY -> target = postApplyTargets.get(mixinClassName + "|" + targetClassName);
        }
        if (target == null) {
            return false;
        }

        if (target.isLeft()) {
            this.transformer.transform(targetClass, target.left().get());
        } else {
            this.transformer.transform(targetClass, target.right().get());
        }
        ClassWriter classWriter = new ClassWriter(0);
        targetClass.accept(classWriter);
        try {
            Path path = Path.of(".dasm.out/" + stage + "/" + targetClassName.replace('.', '/') + ".class").toAbsolutePath();
            Files.createDirectories(path.getParent());
            Files.write(path, classWriter.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}