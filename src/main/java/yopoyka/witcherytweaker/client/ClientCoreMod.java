package yopoyka.witcherytweaker.client;

import yopoyka.witcherytweaker.coremod.BaseClassTransformer;
import yopoyka.witcherytweaker.coremod.CorePlugin;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ClientCoreMod extends BaseClassTransformer {
    {
        transformers.put("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven", (name, transformedName, basicClass) -> {
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
        });
        transformers.put("com.emoniph.witchery.blocks.BlockWitchesOvenGUI", (name, transformedName, basicClass) -> {
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
        });
        transformers.put("com.emoniph.witchery.blocks.BlockDistillery$ContainerDistillery", (name, transformedName, basicClass) -> {
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
        });
        transformers.put("com.emoniph.witchery.blocks.BlockDistilleryGUI", (name, transformedName, basicClass) -> {
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
        });
        transformers.put("com.emoniph.witchery.integration.NEIWitchesOvenRecipeHandler", (name, transformedName, basicClass) -> {
            final ClassNode classNode = read(basicClass);

            classNode.methods
                    .stream()
                    .filter(forMethod("loadCraftingRecipes").and(forMethodDesc("(Ljava/lang/String;[Ljava/lang/Object;)V")))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        final JumpInsnNode jump = find(methodNode.instructions, opcode(Opcodes.IF_ACMPNE));

                        final InsnList code = new InsnList();
                        code.add(new FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "instance",
                                "Lyopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler;"
                        ));
                        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        code.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        code.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "loadCraftingRecipes",
                                "(Ljava/lang/String;[Ljava/lang/Object;)V",
                                false
                        ));
                        code.add(new FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "yopoyka/witcherytweaker/common/WitchOvenRecipes",
                                "defaultEnabled",
                                "Z"
                        ));
                        final Label target = new Label();
                        code.add(new JumpInsnNode(Opcodes.IFNE, new LabelNode(target)));
                        code.add(new InsnNode(Opcodes.RETURN));
                        code.add(new LabelNode(target));

                        methodNode.instructions.insert(jump, code);
                    });

            classNode.methods
                    .stream()
                    .filter(forMethod("loadCraftingRecipes").and(forMethodDesc("(Lnet/minecraft/item/ItemStack;)V")))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        final InsnList code = new InsnList();
                        code.add(new FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "instance",
                                "Lyopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler;"
                        ));
                        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        code.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "loadCraftingRecipes",
                                "(Lnet/minecraft/item/ItemStack;)V",
                                false
                        ));
                        code.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "loadCraftingRecipes",
                                "(Ljava/lang/String;[Ljava/lang/Object;)V",
                                false
                        ));
                        code.add(new FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "yopoyka/witcherytweaker/common/WitchOvenRecipes",
                                "defaultEnabled",
                                "Z"
                        ));
                        final Label target = new Label();
                        code.add(new JumpInsnNode(Opcodes.IFNE, new LabelNode(target)));
                        code.add(new InsnNode(Opcodes.RETURN));
                        code.add(new LabelNode(target));
                    });

            classNode.methods
                    .stream()
                    .filter(forMethod("loadUsageRecipes").and(forMethodDesc("(Lnet/minecraft/item/ItemStack;)V")))
                    .findFirst()
                    .ifPresent(methodNode -> {
                        final InsnList code = new InsnList();
                        code.add(new FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "instance",
                                "Lyopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler;"
                        ));
                        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        code.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "loadUsageRecipes",
                                "(Lnet/minecraft/item/ItemStack;)V",
                                false
                        ));
                        code.add(new MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "yopoyka/witcherytweaker/common/nei/NeiWitchOvenHandler",
                                "loadCraftingRecipes",
                                "(Ljava/lang/String;[Ljava/lang/Object;)V",
                                false
                        ));
                        code.add(new FieldInsnNode(
                                Opcodes.GETSTATIC,
                                "yopoyka/witcherytweaker/common/WitchOvenRecipes",
                                "defaultEnabled",
                                "Z"
                        ));
                        final Label target = new Label();
                        code.add(new JumpInsnNode(Opcodes.IFNE, new LabelNode(target)));
                        code.add(new InsnNode(Opcodes.RETURN));
                        code.add(new LabelNode(target));
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

    public static <T extends AbstractInsnNode> T find(InsnList list, Predicate<AbstractInsnNode> filter) {
        AbstractInsnNode node = list.getFirst();
        while (node != null) {
            if (filter.test(node))
                return (T) node;
            node = node.getNext();
        }
        return null;
    }

    public static <T extends AbstractInsnNode> T find(AbstractInsnNode node, Predicate<AbstractInsnNode> filter) {
        while (node != null) {
            if (filter.test(node))
                return (T) node;
            node = node.getNext();
        }
        return null;
    }

    public static <T extends AbstractInsnNode> T findBack(AbstractInsnNode node, Predicate<AbstractInsnNode> filter) {
        while (node != null) {
            if (filter.test(node))
                return (T) node;
            node = node.getPrevious();
        }
        return null;
    }

    public static <T extends AbstractInsnNode> void forEach(InsnList list, Predicate<AbstractInsnNode> filter, BiConsumer<InsnList, T> action) {
        AbstractInsnNode node = list.getFirst();
        while (node != null) {
            if (filter.test(node))
                action.accept(list, (T) node);
            node = node.getNext();
        }
    }

    public static Predicate<AbstractInsnNode> opcode(int opcode) {
        return n -> n.getOpcode() == opcode;
    }

    public static Predicate<AbstractInsnNode> type(String type) {
        return n -> n instanceof TypeInsnNode && type.equals(((TypeInsnNode) n).desc);
    }

    public static Predicate<MethodNode> forMethod(String name) {
        return m -> name.equals(m.name);
    }

    public static Predicate<MethodNode> forMethodDesc(String desc) {
        return m -> desc.equals(m.desc);
    }

    public static Predicate<AbstractInsnNode> method(String name) {
        return m -> m instanceof MethodInsnNode && name.equals(((MethodInsnNode) m).name);
    }

    public static Predicate<AbstractInsnNode> methodDesc(String desc) {
        return m -> m instanceof MethodInsnNode && desc.equals(((MethodInsnNode) m).desc);
    }

    public static Predicate<AbstractInsnNode> methodOwner(String owner) {
        return m -> m instanceof MethodInsnNode && owner.equals(((MethodInsnNode) m).owner);
    }

    public static Predicate<AbstractInsnNode> label(Label label) {
        return m -> m instanceof LabelNode && label.equals(((LabelNode) m).getLabel());
    }

    public static Predicate<AbstractInsnNode> jump(Label label) {
        return m -> m instanceof JumpInsnNode && label.equals(((JumpInsnNode) m).label.getLabel());
    }
}
