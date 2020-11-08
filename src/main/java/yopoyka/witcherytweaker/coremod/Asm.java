package yopoyka.witcherytweaker.coremod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Asm {
    public static ClassNode read(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        return classNode;
    }

    public static byte[] write(ClassNode classNode, ClassWriter cw) {
        classNode.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] write(ClassNode classNode) {
        return write(classNode, new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS));
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

    public static <T extends AbstractInsnNode> void forEach(AbstractInsnNode from, Predicate<AbstractInsnNode> filter, Consumer<T> action) {
        AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept((T) node);
            node = node.getNext();
        }
    }

    public static <T extends AbstractInsnNode> void forEachBack(AbstractInsnNode from, Predicate<AbstractInsnNode> filter, Consumer<T> action) {
        AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept((T) node);
            node = node.getPrevious();
        }
    }

    public static <T extends AbstractInsnNode> void forEach(AbstractInsnNode from, InsnList list, Predicate<AbstractInsnNode> filter, BiConsumer<InsnList, T> action) {
        AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept(list, (T) node);
            node = node.getNext();
        }
    }

    public static <T extends AbstractInsnNode> void forEachBack(AbstractInsnNode from, InsnList list, Predicate<AbstractInsnNode> filter, BiConsumer<InsnList, T> action) {
        AbstractInsnNode node = from;
        while (node != null) {
            if (filter.test(node))
                action.accept(list, (T) node);
            node = node.getPrevious();
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
