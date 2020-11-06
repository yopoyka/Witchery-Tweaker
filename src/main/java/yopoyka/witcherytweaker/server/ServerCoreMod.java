package yopoyka.witcherytweaker.server;

import yopoyka.witcherytweaker.common.Inject;
import yopoyka.witcherytweaker.coremod.BaseClassTransformer;
import yopoyka.witcherytweaker.coremod.CorePlugin;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ServerCoreMod extends BaseClassTransformer {
    {
        transformers.put("com.emoniph.witchery.blocks.BlockWitchesOven$TileEntityWitchesOven", (name, transformedName, basicClass) -> {
            ClassNode classNode = read(basicClass);

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_145845_h") || m.name.equals("updateEntity"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        InsnList inst = methodNode.instructions = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "yopoyka/witcherytweaker/server/WitchOvenHook",
                                "updateOven",
                                "(Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;)V",
                                false
                        ));
                        inst.add(new InsnNode(Opcodes.RETURN));
                        CorePlugin.log.info("Successfully patched tile Witch's Oven updateEntity method");
                    });

            Inject.inject(classNode, IWitchOvenTile.class);
            return write(classNode);
        });
        transformers.put("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven", (name, transformedName, basicClass) -> {
            ClassNode classNode = read(basicClass);

            classNode.visitField(Opcodes.ACC_PUBLIC, "wtw_lastTotalCookTime", "I", null, null)
                    .visitEnd();

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_75142_b ") || m.name.equals("detectAndSendChanges"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(methodNode.instructions.iterator(), Spliterator.ORDERED), false)
                                .filter(node -> node.getOpcode() == Opcodes.INVOKEINTERFACE && (((MethodInsnNode) node).name.equals("func_71112_a") || ((MethodInsnNode) node).name.equals("sendProgressBarUpdate")))
                                .reduce((a, b) -> b) // get last
                                .ifPresent(node -> {
                                    AbstractInsnNode aqua = node.getNext();
                                    InsnList inst = new InsnList();
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "wtw_lastTotalCookTime", "I"));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "furnace", "Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;"));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "wtw_cookTime", "I"));
                                    Label end = new Label();
                                    inst.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, new LabelNode(end)));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 2));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new InsnNode(Opcodes.ICONST_3));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "furnace", "Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;"));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "wtw_cookTime", "I"));
                                    inst.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, ((MethodInsnNode) node).owner, ((MethodInsnNode) node).name, ((MethodInsnNode) node).desc, true));
                                    inst.add(new LabelNode(end));
                                    methodNode.instructions.insert(aqua, inst);

                                    CorePlugin.log.info("Successfully patched tile Witch's Oven Container's cycle");
                                });

                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "furnace", "Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;"));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "wtw_cookTime", "I"));
                        inst.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "wtw_lastTotalCookTime", "I"));

                        AbstractInsnNode ret = null;
                        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                        while (iterator.hasNext()) {
                            AbstractInsnNode next = iterator.next();
                            if (next.getOpcode() == Opcodes.RETURN) {
                                ret = next;
                                break;
                            }
                        }

                        methodNode.instructions.insertBefore(ret, inst);

                        CorePlugin.log.info("Successfully patched tile Witch's Oven Container's detectAndSendChanges method");
                    });

            boolean srgNames = classNode.methods.stream().anyMatch(m -> m.name.equals("func_75132_a"));

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_75132_a") || m.name.equals("onCraftGuiOpened"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new InsnNode(Opcodes.ICONST_3));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "furnace", "Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;"));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "wtw_cookTime", "I"));
                        inst.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/inventory/ICrafting", srgNames ? "func_71112_a" : "sendProgressBarUpdate", "(Lnet/minecraft/inventory/Container;II)V", true));

                        AbstractInsnNode ret = null;
                        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                        while (iterator.hasNext()) {
                            AbstractInsnNode next = iterator.next();
                            if (next.getOpcode() == Opcodes.RETURN) {
                                ret = next;
                                break;
                            }
                        }

                        methodNode.instructions.insertBefore(ret, inst);

                        CorePlugin.log.info("Successfully patched tile Witch's Oven Container's onCraftGuiOpened method");
                    });

            return write(classNode);
        });
        transformers.put("com.emoniph.witchery.blocks.BlockDistillery$TileEntityDistillery", (name, transformedName, basicClass) -> {
            ClassNode classNode = read(basicClass);

            final MethodNode toRemove = classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_145845_h") || m.name.equals("updateEntity"))
                    .findFirst()
                    .get();

            classNode.methods.remove(toRemove);
            final MethodVisitor mv = classNode.visitMethod(toRemove.access, toRemove.name, toRemove.desc, null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "com/emoniph/witchery/blocks/TileEntityBase",
                    toRemove.name,
                    "()V",
                    false
            );
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "yopoyka/witcherytweaker/server/DistilleryHook",
                    "updateDistillery",
                    "(Lcom/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery;)V",
                    false
            );
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();
            CorePlugin.log.info("Successfully patched tile Distillery updateEntity method");

            Inject.inject(classNode, IDistilleryTile.class);

            return write(classNode);
        });
        transformers.put("com.emoniph.witchery.blocks.BlockDistillery$ContainerDistillery", (name, transformedName, basicClass) -> {
            ClassNode classNode = read(basicClass);

            classNode.visitField(Opcodes.ACC_PUBLIC, "wtw_lastTotalCookTime", "I", null, null)
                    .visitEnd();

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_75142_b ") || m.name.equals("detectAndSendChanges"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(methodNode.instructions.iterator(), Spliterator.ORDERED), false)
                                .filter(node -> node.getOpcode() == Opcodes.INVOKEINTERFACE && (((MethodInsnNode) node).name.equals("func_71112_a") || ((MethodInsnNode) node).name.equals("sendProgressBarUpdate")))
                                .reduce((a, b) -> b) // get last
                                .ifPresent(node -> {
                                    AbstractInsnNode aqua = node.getNext();
                                    InsnList inst = new InsnList();
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "wtw_lastTotalCookTime", "I"));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "furnace", "Lcom/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery;"));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery", "wtw_cookTime", "I"));
                                    Label end = new Label();
                                    inst.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, new LabelNode(end)));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 2));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new InsnNode(Opcodes.ICONST_3));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "furnace", "Lcom/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery;"));
                                    inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery", "wtw_cookTime", "I"));
                                    inst.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, ((MethodInsnNode) node).owner, ((MethodInsnNode) node).name, ((MethodInsnNode) node).desc, true));
                                    inst.add(new LabelNode(end));
                                    methodNode.instructions.insert(aqua, inst);

                                    CorePlugin.log.info("Successfully patched Distillery Container's cycle");
                                });

                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "furnace", "Lcom/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery;"));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery", "wtw_cookTime", "I"));
                        inst.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "wtw_lastTotalCookTime", "I"));

                        AbstractInsnNode ret = null;
                        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                        while (iterator.hasNext()) {
                            AbstractInsnNode next = iterator.next();
                            if (next.getOpcode() == Opcodes.RETURN) {
                                ret = next;
                                break;
                            }
                        }

                        methodNode.instructions.insertBefore(ret, inst);

                        CorePlugin.log.info("Successfully patched Distillery Container's detectAndSendChanges method");
                    });

            boolean srgNames = classNode.methods.stream().anyMatch(m -> m.name.equals("func_75132_a"));

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_75132_a") || m.name.equals("onCraftGuiOpened"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new InsnNode(Opcodes.ICONST_3));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "furnace", "Lcom/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery;"));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery", "wtw_cookTime", "I"));
                        inst.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/inventory/ICrafting", srgNames ? "func_71112_a" : "sendProgressBarUpdate", "(Lnet/minecraft/inventory/Container;II)V", true));

                        AbstractInsnNode ret = null;
                        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                        while (iterator.hasNext()) {
                            AbstractInsnNode next = iterator.next();
                            if (next.getOpcode() == Opcodes.RETURN) {
                                ret = next;
                                break;
                            }
                        }

                        methodNode.instructions.insertBefore(ret, inst);

                        CorePlugin.log.info("Successfully patched Distillery Container's onCraftGuiOpened method");
                    });

            return write(classNode);
        });
    }

    private static ClassNode read(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        return classNode;
    }

    private static byte[] write(ClassNode classNode) {
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
