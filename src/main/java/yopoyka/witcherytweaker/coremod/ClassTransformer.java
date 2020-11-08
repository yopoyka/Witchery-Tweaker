package yopoyka.witcherytweaker.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import yopoyka.witcherytweaker.client.ClientCoreMod;
import yopoyka.witcherytweaker.common.IDistilleryRecipe;
import yopoyka.witcherytweaker.common.Inject;
import yopoyka.witcherytweaker.server.ServerCoreMod;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import static yopoyka.witcherytweaker.coremod.Asm.read;
import static yopoyka.witcherytweaker.coremod.Asm.write;

public class ClassTransformer extends BaseClassTransformer {
    private static IClassTransformer client = new ClientCoreMod();
    private static IClassTransformer server = new ServerCoreMod();
    {
        transformers.put("com.emoniph.witchery.WitcheryRecipes", (name, transformedName, basicClass) -> {
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
        });
        transformers.put("com.emoniph.witchery.brewing.WitcheryBrewRegistry", (name, transformedName, basicClass) -> {
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
        });
        transformers.put("com.emoniph.witchery.crafting.DistilleryRecipes$DistilleryRecipe", (name, transformedName, basicClass) -> {
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
        });
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return client.transform(name, transformedName, server.transform(name, transformedName, super.transform(name, transformedName, basicClass)));
    }
}
