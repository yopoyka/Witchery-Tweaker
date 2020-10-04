package muwa.witcherytweaker.server;

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

public class ServerCoreMod {
    public static IClassTransformer transformer = (name, transformedName, basicClass) -> {
        if (name.equals("com.emoniph.witchery.blocks.BlockWitchesOven$TileEntityWitchesOven")) {
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
                                "muwa/witcherytweaker/server/WitchOvenHook",
                                "updateOven",
                                "(Lcom/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven;)V",
                                false
                        ));
                        inst.add(new InsnNode(Opcodes.RETURN));
                        CorePlugin.log.info("Successfully patched tile Witch's Oven updateEntity method");
                    });

            classNode.visitField(Opcodes.ACC_PUBLIC, "wtw_cookTime", "I", null, null)
                    .visitEnd();

            classNode.interfaces.add("muwa/witcherytweaker/server/IWitchOvenTile");

            MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, "setTotalCookTime", "(I)V", null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "wtw_cookTime", "I");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();

            mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, "getTotalCookTime", "()I", null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "com/emoniph/witchery/blocks/BlockWitchesOven$TileEntityWitchesOven", "wtw_cookTime", "I");
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitEnd();

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("getFumeFunnels")
                            || m.name.equals("generateByProduct")
                            || m.name.equals("getFumeFunnelsChance")
                    )
                    .forEach(m -> m.access = Opcodes.ACC_PUBLIC);

            return write(classNode);
        }
        else if (name.equals("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven")) {
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
        }
        else
            return basicClass;
    };

    public static ClassNode read(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        return classNode;
    }

    public static byte[] write(ClassNode classNode) {
        ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }
}
