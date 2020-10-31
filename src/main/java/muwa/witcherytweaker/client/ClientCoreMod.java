package muwa.witcherytweaker.client;

import muwa.witcherytweaker.coremod.CorePlugin;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ClientCoreMod {
    public static IClassTransformer transformer = (name, transformedName, basicClass) -> {
        if (name.equals("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven")) {
            ClassNode classNode = read(basicClass);

            classNode.visitField(Opcodes.ACC_PUBLIC, "wtw_totalCookTime", "I", null, null)
                    .visitEnd();

            classNode.fields
                    .stream()
                    .filter(f -> f.name.equals("furnace"))
                    .findFirst()
                    .ifPresent(fieldNode -> fieldNode.access = Opcodes.ACC_PUBLIC);

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_75137_b") || m.name.equals("updateProgressBar"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ILOAD, 1));
                        inst.add(new InsnNode(Opcodes.ICONST_3));
                        Label ifNot = new Label();
                        inst.add(new JumpInsnNode(Opcodes.IF_ICMPNE, new LabelNode(ifNot)));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new VarInsnNode(Opcodes.ILOAD, 2));
                        inst.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "wtw_totalCookTime", "I"));
                        inst.add(new LabelNode(ifNot));

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

                        CorePlugin.log.info("Successfully patched Witch's Oven Container's updateProgressBar method");
                    });

            return write(classNode);
        }
        else if (name.equals("com.emoniph.witchery.blocks.BlockWitchesOvenGUI")) {
            ClassNode classNode = read(basicClass);
            boolean srgNames = classNode.methods.stream().anyMatch(m -> m.name.equals("func_146976_a"));

            MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, "wtw_getCookProgressScaled", "(I)I", null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOvenGUI", srgNames ? "field_147002_h" : "inventorySlots", "Lnet/minecraft/inventory/Container;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven");
            mv.visitInsn(Opcodes.DUP);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "wtw_totalCookTime", "I");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
            mv.visitInsn(Opcodes.SWAP);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven", "furnace", "Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;");
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "furnaceCookTime", "I");
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.SWAP);
            mv.visitInsn(Opcodes.IDIV);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitEnd();

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_146976_a") || m.name.equals("drawGuiContainerBackgroundLayer"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(methodNode.instructions.iterator(), Spliterator.ORDERED), false)
                                .filter(node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals("getCookProgressScaled"))
                                .findFirst()
                                .ifPresent(node -> {
                                    InsnList inst = new InsnList();
                                    inst.add(new InsnNode(Opcodes.SWAP));
                                    inst.add(new InsnNode(Opcodes.POP));
                                    inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    inst.add(new InsnNode(Opcodes.SWAP));
                                    inst.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/emoniph/witchery/blocks/BlockWitchesOvenGUI", "wtw_getCookProgressScaled", "(I)I", false));
                                    methodNode.instructions.insertBefore(node, inst);
                                    methodNode.instructions.remove(node);

                                    CorePlugin.log.info("Successfully patched Witch's Oven Gui's drawGuiContainerBackgroundLayer method");
                                });
                    });

            return write(classNode);
        }
        else if (name.equals("com.emoniph.witchery.blocks.BlockDistillery$ContainerDistillery")) {
            ClassNode classNode = read(basicClass);

            classNode.visitField(Opcodes.ACC_PUBLIC, "wtw_totalCookTime", "I", null, null)
                    .visitEnd();

            classNode.fields
                    .stream()
                    .filter(f -> f.name.equals("furnace"))
                    .findFirst()
                    .ifPresent(fieldNode -> fieldNode.access = Opcodes.ACC_PUBLIC);

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_75137_b") || m.name.equals("updateProgressBar"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ILOAD, 1));
                        inst.add(new InsnNode(Opcodes.ICONST_3));
                        Label ifNot = new Label();
                        inst.add(new JumpInsnNode(Opcodes.IF_ICMPNE, new LabelNode(ifNot)));
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new VarInsnNode(Opcodes.ILOAD, 2));
                        inst.add(new FieldInsnNode(Opcodes.PUTFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "wtw_totalCookTime", "I"));
                        inst.add(new LabelNode(ifNot));

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

                        CorePlugin.log.info("Successfully patched Distillery Container's updateProgressBar method");
                    });

            return write(classNode);
        }
        else if (name.equals("com.emoniph.witchery.blocks.BlockDistilleryGUI")) {
            ClassNode classNode = read(basicClass);

            boolean srgNames = classNode.methods.stream().anyMatch(m -> m.name.equals("func_146976_a"));

            MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, "wtw_getCookProgressScaled", "(I)I", null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistilleryGUI", srgNames ? "field_147002_h" : "inventorySlots", "Lnet/minecraft/inventory/Container;");
            mv.visitTypeInsn(Opcodes.CHECKCAST, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery");
            mv.visitInsn(Opcodes.DUP);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "wtw_totalCookTime", "I");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false);
            mv.visitInsn(Opcodes.SWAP);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "furnace", "Lcom/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery;");
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$TileEntityDistillery", "furnaceCookTime", "I");
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.SWAP);
            mv.visitInsn(Opcodes.IDIV);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitEnd();

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_146976_a") || m.name.equals("drawGuiContainerBackgroundLayer"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        AbstractInsnNode sipush = null;
                        ListIterator<AbstractInsnNode> i = methodNode.instructions.iterator();
                        while (i.hasNext()) {
                            AbstractInsnNode next = i.next();
                            if (next.getOpcode() == Opcodes.SIPUSH && ((IntInsnNode) next).operand == 800) {
                                sipush = next;
                                break;
                            }
                        }

                        InsnList inst = new InsnList();
                        inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistilleryGUI", srgNames ? "field_147002_h" : "inventorySlots", "Lnet/minecraft/inventory/Container;"));
                        inst.add(new TypeInsnNode(Opcodes.CHECKCAST, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery"));
                        inst.add(new FieldInsnNode(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockDistillery$ContainerDistillery", "wtw_totalCookTime", "I"));

                        methodNode.instructions.insertBefore(sipush, inst);
                        methodNode.instructions.remove(sipush);
                    });

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("func_146976_a") || m.name.equals("drawGuiContainerBackgroundLayer"))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(methodNode.instructions.iterator(), Spliterator.ORDERED), false)
                            .filter(node -> node.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals("getCookProgressScaled"))
                            .findFirst()
                            .ifPresent(node -> {
                                InsnList inst = new InsnList();
                                inst.add(new InsnNode(Opcodes.SWAP));
                                inst.add(new InsnNode(Opcodes.POP));
                                inst.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                inst.add(new InsnNode(Opcodes.SWAP));
                                inst.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/emoniph/witchery/blocks/BlockDistilleryGUI", "wtw_getCookProgressScaled", "(I)I", false));
                                methodNode.instructions.insertBefore(node, inst);
                                methodNode.instructions.remove(node);

                                CorePlugin.log.info("Successfully patched Distillery Gui's drawGuiContainerBackgroundLayer method");
                            });
                    });

            return write(classNode);
        }
        else if (name.equals("com.emoniph.witchery.integration.NEIWitcheryConfig")) {
            ClassNode classNode = read(basicClass);

            classNode.methods
                .stream()
                .filter(m -> m.name.equals("loadConfig"))
                .findFirst()
                .ifPresent(methodNode -> {
                    methodNode.instructions.iterator().forEachRemaining(node -> {
                        if (node.getOpcode() == Opcodes.NEW && node instanceof TypeInsnNode) {
                            if (((TypeInsnNode) node).desc.equals("com/emoniph/witchery/integration/NEIWitchesOvenRecipeHandler"))
                                ((TypeInsnNode) node).desc = "muwa/witcherytweaker/common/nei/NeiWitchOvenHandler";
                        }
                        else if (node.getOpcode() == Opcodes.INVOKESPECIAL && node instanceof MethodInsnNode) {
                            if (((MethodInsnNode) node).owner.equals("com/emoniph/witchery/integration/NEIWitchesOvenRecipeHandler"))
                                ((MethodInsnNode) node).owner = "muwa/witcherytweaker/common/nei/NeiWitchOvenHandler";
                        }
                    });

                    CorePlugin.log.info("Successfully patched NEIWitcheryConfig");
                });

            return write(classNode);
        }
        else
            return basicClass;
    };

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
