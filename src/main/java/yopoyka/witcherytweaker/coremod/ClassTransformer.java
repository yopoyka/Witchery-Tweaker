package yopoyka.witcherytweaker.coremod;

import yopoyka.witcherytweaker.common.IDistilleryRecipe;
import yopoyka.witcherytweaker.common.Inject;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Predicate;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ClassTransformer implements IClassTransformer {
    private static IClassTransformer client = (name, transformedName, basicClass) -> basicClass;
    private static IClassTransformer server = (name, transformedName, basicClass) -> basicClass;
    private static IClassTransformer common = (name, transformedName, basicClass) -> {
        if (name.equals("com.emoniph.witchery.WitcheryRecipes")) {
            ClassNode classNode = read(basicClass);
            classNode.methods
                .stream()
                .filter(m -> m.name.equals("preInit"))
                .findFirst()
                .ifPresent(methodNode -> {
                    InsnList kettle = new InsnList();

                    AbstractInsnNode lastDist = null;
                    AbstractInsnNode lastKettle = null;
                    AbstractInsnNode lastSmelting = null;

                    ListIterator<AbstractInsnNode> i = methodNode.instructions.iterator();
                    while (i.hasNext()) {
                        AbstractInsnNode next = i.next();

                        if (next.getOpcode() == Opcodes.INVOKEVIRTUAL && next instanceof MethodInsnNode) {
                            if (((MethodInsnNode) next).owner.equals("com/emoniph/witchery/crafting/DistilleryRecipes")
                                    && ((MethodInsnNode) next).name.equals("addRecipe")) {
                                lastDist = next;
                            }
                            else if (((MethodInsnNode) next).owner.equals("com/emoniph/witchery/crafting/KettleRecipes")
                                    && ((MethodInsnNode) next).name.equals("addRecipe")) {
                                lastKettle = next;
                            }
                        }
                        else if (lastDist == null && next.getOpcode() == Opcodes.INVOKESTATIC && next instanceof MethodInsnNode) {
                            if (((MethodInsnNode) next).owner.equals("cpw/mods/fml/common/registry/GameRegistry")
                                    && ((MethodInsnNode) next).name.equals("addSmelting")
                            ) {
                                lastSmelting = next;
                            }
                        }
                    }

                    AbstractInsnNode start = lastDist.getNext();
                    AbstractInsnNode finish = lastKettle;

                    Map<LabelNode, LabelNode> map = new HashMap<LabelNode, LabelNode>() {
                        @Override
                        public LabelNode get(Object key) {
                            return new LabelNode(((LabelNode) key).getLabel());
                        }
                    };

                    do {
                        start = start.getNext();
                        kettle.add(start.clone(map));
                    } while (start != finish);
                    kettle.add(new InsnNode(Opcodes.RETURN));

                    MethodNode wtw_kettle = new MethodNode(Opcodes.ACC_PUBLIC, "wtw_kettle", "()V", null, null);
                    wtw_kettle.instructions.add(kettle);

                    classNode.methods.add(wtw_kettle);

                    CorePlugin.log.info("Successfully added wtw_kettle method!");

                    start = lastSmelting.getNext();
                    finish = lastDist;

                    final InsnList dist = new InsnList();
                    while (start != finish) {
                        dist.add(start.clone(map));
                        start = start.getNext();
                    }
                    dist.add(new InsnNode(Opcodes.RETURN));

                    MethodNode wtw_distillery = new MethodNode(Opcodes.ACC_PUBLIC, "wtw_distillery", "()V", null, null);
                    wtw_distillery.instructions.add(dist);

                    classNode.methods.add(wtw_distillery);

                    CorePlugin.log.info("Successfully added wtw_distillery method!");
                });
            return write(classNode);
        }
        else if (name.equals("com.emoniph.witchery.brewing.WitcheryBrewRegistry")) {
            ClassNode classNode = read(basicClass);

            classNode.methods
                .stream()
                .filter(m -> m.name.equals("<init>"))
                .findFirst()
                .ifPresent(methodNode -> {
                    InsnList cauldron = new InsnList();
                    AbstractInsnNode lastRegister = null;

                    ListIterator<AbstractInsnNode> i = methodNode.instructions.iterator();
                    while (i.hasNext()) {
                        AbstractInsnNode next = i.next();

                        if (next.getOpcode() == Opcodes.INVOKESPECIAL && next instanceof MethodInsnNode
                                && ((MethodInsnNode) next).owner.equals("com/emoniph/witchery/brewing/WitcheryBrewRegistry")
                                && ((MethodInsnNode) next).name.equals("register")) {
                            lastRegister = next;
                        }
                        else if (next.getOpcode() == Opcodes.NEW && next instanceof TypeInsnNode
                                && ((TypeInsnNode) next).desc.equals("com/emoniph/witchery/brewing/action/BrewActionRitualRecipe")
                                && lastRegister != null)
                            break;
                    }

                    Map<LabelNode, LabelNode> map = new HashMap<LabelNode, LabelNode>() {
                        @Override
                        public LabelNode get(Object key) {
                            return new LabelNode(((LabelNode) key).getLabel());
                        }
                    };

                    AbstractInsnNode next = lastRegister.getNext();
                    while (next.getNext() != null) {
                        cauldron.add(next.getNext().clone(map));
                        next = next.getNext();
                    }

                    MethodNode wtw_cauldron = new MethodNode(Opcodes.ACC_PUBLIC, "wtw_cauldron", "()V", null, null);
                    wtw_cauldron.instructions.add(cauldron);
                    classNode.methods.add(wtw_cauldron);

                    CorePlugin.log.info("Successfully added wtw_cauldron method!");
                });

            return write(classNode);
        }
        else if (transformedName.equals("com.emoniph.witchery.crafting.DistilleryRecipes$DistilleryRecipe")) {
            final ClassNode classNode = read(basicClass);

            Inject.inject(classNode, IDistilleryRecipe.class);

            classNode.methods
                    .stream()
                    .filter(m -> m.name.equals("<init>"))
                    .forEach(methodNode -> {
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        list.add(new IntInsnNode(Opcodes.SIPUSH, 800));
                        list.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, "wtw_cookTime", "I"));

                        AbstractInsnNode last = methodNode.instructions.getLast();
                        while (last != null) {
                            if (last.getOpcode() == Opcodes.RETURN)
                                break;

                            last = last.getPrevious();
                        }

                        methodNode.instructions.insertBefore(last, list);

                        CorePlugin.log.info("Successfully patched DistilleryRecipe's constructor!");
                    });

            return write(classNode);
        }
        else
            return basicClass;
    };

    private static <T extends AbstractInsnNode> T find(InsnList instructions, Predicate<AbstractInsnNode> filter) {
        ListIterator<AbstractInsnNode> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();
            if (filter.test(next))
                return (T) next;
        }
        return null;
    }

    static {
        try {
            Class<?> clientClass = Class.forName("yopoyka.witcherytweaker.client.ClientCoreMod");
            client = (IClassTransformer) clientClass.newInstance();
            CorePlugin.log.info("Client coremod part loaded");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            CorePlugin.log.warn("Client coremod part not found");
            CorePlugin.log.catching(Level.WARN, e);
        }
        try {
            Class<?> serverClass = Class.forName("yopoyka.witcherytweaker.server.ServerCoreMod");
            server = (IClassTransformer) serverClass.newInstance();
            CorePlugin.log.info("Server coremod part loaded");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            CorePlugin.log.warn("Server coremod part not found");
            CorePlugin.log.catching(Level.WARN, e);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return client.transform(name, transformedName, server.transform(name, transformedName, common.transform(name, transformedName, basicClass)));
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
