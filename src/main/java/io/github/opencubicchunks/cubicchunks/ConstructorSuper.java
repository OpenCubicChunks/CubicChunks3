package io.github.opencubicchunks.cubicchunks;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.InjectionPoint.AtCode;
import org.spongepowered.asm.mixin.injection.selectors.ISelectorContext;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.util.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This injection point targets a delegating {@code super()} call <b><u>OR</u></b> a delegating {@code this()} call
 * <p>
 * This is a temporary hack until we update to a mixin version that allows you to target CTOR_SUPER.
 * <p>
 * To use this injection point specify its fully qualified name in the @At annotation.
 * <h1>Example</h1>
 * <pre>{@code @Inject(method = "<init>", target = @At("io.github.opencubicchunks.cubicchunks.ConstructorSuper"))}</pre>
 */
@AtCode("CC:SUPER")
public class ConstructorSuper extends InjectionPoint {
    protected final String superName;
    protected final String ownerName;

    protected final List<MethodNode> methodNodes;

    public ConstructorSuper(InjectionPointData data) {
        super(data);
        if (!data.getMixin().getMixin().getConfig().getEnvironment().getVersion().equals("0.8.5"))
            throw new NotImplementedException("We have updated mixin, please use CTOR_HEAD instead of this disaster.");

        ISelectorContext parent = data.getContext().getParent();
        ClassNode classNode = ((MixinTargetContext) parent.getMixin()).getTargetClassNode();
        this.superName = classNode.superName;
        this.ownerName = classNode.name;

        this.methodNodes = new ArrayList<>();

        try {
            Field targetsField = parent.getClass().getSuperclass().getDeclaredField("targets");
            targetsField.setAccessible(true);
            List<?> targets = (List<?>) targetsField.get(parent);

            if (targets.isEmpty()) {
                return;
            }

            Field methodField = targets.get(0).getClass().getDeclaredField("method");
            methodField.setAccessible(true);

            for (Object target : targets) {
                MethodNode method = (MethodNode) methodField.get(target);
                if (!method.name.equals("<init>")) {
                    throw new IllegalArgumentException("ConstructorSuper inject must target a constructor");
                }
                this.methodNodes.add(method);
            }
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(String.format("Failed to create super injection point for method %s %s", data.getMethod().name, data.getMixin().getClassName()), e);
        }
    }

    @Override
    public boolean checkPriority(int targetPriority, int ownerPriority) {
        return true;
    }

    @Override
    public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
        for (MethodNode node : this.methodNodes) {
            if (instructionsMatch(node.instructions, insns)) {
                MethodInsnNode delegateInit = findDelegateInit(insns, this.superName, this.ownerName);
                if (delegateInit == null) {
                    continue;
                }
                nodes.add(delegateInit.getNext()); // Assume there is always an instruction node after the super call
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static MethodInsnNode findDelegateInit(InsnList insns, String superName, String ownerName) {
        // Looking for the invocation of the super class constructor without a new before it
        int news = 0;
        for (AbstractInsnNode insn : insns) {
            if (insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.NEW) {
                news++;
            } else if (insn instanceof MethodInsnNode methodInsn && insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                if (Constants.CTOR.equals(methodInsn.name)) {
                    if (news > 0) {
                        news--;
                    } else {
                        boolean isSuper = methodInsn.owner.equals(superName);
                        if (isSuper || methodInsn.owner.equals(ownerName)) {
                            return methodInsn;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean instructionsMatch(InsnList a, InsnList b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }
}