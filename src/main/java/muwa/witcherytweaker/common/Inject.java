package muwa.witcherytweaker.common;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Optional;

public class Inject {
    /**
     * Denotes which class is owner of whatever target
     * is annotated by this annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Owner {
        public static class C {}
        /**
         * Should be the same as calling {@code Class#getName}.
         * @see Class#getName
         * @return the name of target class.
         */
        public String value() default "";

        /**
         * @return target class.
         */
        public Class<?> clazz() default C.class;
    }

    /**
     * Mark interface's method as accessor for field of target class.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Access {
        /**
         * @return name of target field.
         */
        public String value() default "";

        /**
         * Denotes that field should be created if it doesn't exist.
         * @return true if field should be created.
         */
        public boolean create() default false;
    }

    /**
     * Forces method of the target class to be public.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Public {}

    /**
     * Accepts a ClassNode and Class of interface which will be used as a model
     * to make modifications to ClassNode.
     *
     * The {@code inter} should be annotated with {@code Owner} annotation
     * to tell which class is owner of members targeted by the following modifications.
     * If {@code Owner} annotation is missing or if the {@code Owner#value()} is empty
     * or {@code Owner#clazz()} equals to {@code Object.class} the name of ClassNode will be used.
     *
     * Examples:
     * <blockquote><pre>
     * &#064;Access
     * public int getAnInt();
     * </pre></blockquote>
     * Creates a method to access field {@code anInt} of target class.
     * <blockquote><pre>
     * &#064;Access("integer")
     * public int getAnInt();
     * </pre></blockquote>
     * Creates a method to access field {@code integer} of target class.
     * <blockquote><pre>
     * &#064;Access(create = true)
     * public int getAnInt();
     * </pre></blockquote>
     * Creates a method to access field {@code anInt} of target class.
     * Also creates field {@code public int anInt} on a target class
     * if it doesn't exist.
     *
     * <blockquote><pre>
     * &#064;Access
     * public void setAnInt(int value);
     * </pre></blockquote>
     * Creates a method to set field {@code anInt} of target class.
     * <blockquote><pre>
     * &#064;Access("integer")
     * public void setAnInt(int value);
     * </pre></blockquote>
     * Creates a method to set field {@code integer} of target class.
     * <blockquote><pre>
     * &#064;Access(create = true)
     * public void setAnInt(int value);
     * </pre></blockquote>
     * Creates a method to set field {@code anInt} of target class.
     * If targeted field is final it will be made non-final.
     * Also creates field {@code public int anInt} on a target class
     * if it doesn't exist.
     *
     * @see ClassNode
     * @param classNode the class to be modified.
     * @param inter the model according to which the class will be modified.
     *              Target class will be implementing the interface.
     */
    public static void inject(ClassNode classNode, Class<?> inter) {
        final Owner classAnnotation = inter.getAnnotation(Owner.class);
        final String owner =
                classAnnotation == null
                        ? classNode.name
                        : classAnnotation.value().isEmpty()
                        ? classAnnotation.clazz() == Owner.C.class
                        ? classNode.name
                        : classAnnotation.clazz().getName().replace('.', '/')
                        : classAnnotation.value().replace('.', '/');

        final String interfaceName = inter.getName().replace('.', '/');

        for (Method method : inter.getMethods()) {
            final Public publicAnn = method.getAnnotation(Public.class);
            if (publicAnn != null) {
                if (!classNode.interfaces.contains(interfaceName))
                    classNode.interfaces.add(interfaceName);
                final String desc = getMethodDescriptor(method);
                classNode.methods
                        .stream()
                        .filter(m -> m.name.equals(method.getName()) && m.desc.equals(desc))
                        .forEach(m -> m.access = m.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)
                                | Opcodes.ACC_PUBLIC);
                continue;
            }
            final Access accessAnn = method.getAnnotation(Access.class);
            if (accessAnn != null) {
                if (!classNode.interfaces.contains(interfaceName))
                    classNode.interfaces.add(interfaceName);
                final String fieldOwner = Optional.ofNullable(method.getAnnotation(Owner.class))
                        .map(Owner::value)
                        .map(s -> s.replace('.', '/'))
                        .orElse(owner);

                final String fieldName;

                if (accessAnn.value().isEmpty()) {
                    if (method.getName().length() < 4)
                        throw new IllegalArgumentException("Accessor name must start with `get` or `set` got " + method.getName() + " in " + inter);
                    fieldName = (Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4));
                }
                else
                    fieldName = accessAnn.value();

                final String fieldDesc;

                if (method.getParameterCount() == 0) { // getter
                    if (method.getReturnType() == void.class)
                        throw new IllegalArgumentException("Getter returns void " + method + " in " + inter);

                    fieldDesc = getDescriptorForClass(method.getReturnType());
                    final MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), "()" + fieldDesc, null, null);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(Opcodes.GETFIELD, fieldOwner, fieldName, fieldDesc);
                    switch (fieldDesc.charAt(0)) {
                        case 'L': mv.visitInsn(Opcodes.ARETURN); break;
                        case 'F': mv.visitInsn(Opcodes.FRETURN); break;
                        case 'J': mv.visitInsn(Opcodes.LRETURN); break;
                        case 'D': mv.visitInsn(Opcodes.DRETURN); break;
                        default: mv.visitInsn(Opcodes.IRETURN); break;
                    }
                    mv.visitMaxs(1, 1);
                    mv.visitEnd();
                }
                else if (method.getParameterCount() == 1) { // setter
                    if (method.getReturnType() != void.class)
                        throw new IllegalArgumentException("Setter must return void! Cause: " + method + " at " + inter);
                    fieldDesc = getDescriptorForClass(method.getParameterTypes()[0]);
                    final MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), "(" + fieldDesc + ")V", null, null);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    switch (fieldDesc.charAt(0)) {
                        case 'L': mv.visitVarInsn(Opcodes.ALOAD, 1); break;
                        case 'F': mv.visitVarInsn(Opcodes.FLOAD, 1); break;
                        case 'J': mv.visitVarInsn(Opcodes.LLOAD, 1); break;
                        case 'D': mv.visitVarInsn(Opcodes.DLOAD, 1); break;
                        default: mv.visitVarInsn(Opcodes.ILOAD, 1); break;
                    }
                    mv.visitFieldInsn(Opcodes.PUTFIELD, fieldOwner, fieldName, fieldDesc);
                    mv.visitInsn(Opcodes.RETURN);
                    mv.visitMaxs(2, 2);
                    mv.visitEnd();
                    classNode.fields
                            .stream()
                            .filter(f -> f.name.equals(fieldName))
                            .findFirst()
                            .ifPresent(f -> f.access &= ~Opcodes.ACC_FINAL);
                }
                else
                    throw new IllegalArgumentException("Parameters count must be 1 (one) ore (zero) but got: " + method.getParameterCount() + " for " + method + " in " + inter);

                if (accessAnn.create() && classNode.fields.stream().noneMatch(f -> f.name.equals(fieldName))) {
                    classNode.visitField(Opcodes.ACC_PUBLIC, fieldName, fieldDesc, null, null)
                            .visitEnd();
                }
            }
        }
    }

    public static String getDescriptorForClass(final Class<?> c) {
        if(c.isPrimitive()) {
            if(c == byte.class)
                return "B";
            if(c == char.class)
                return "C";
            if(c == double.class)
                return "D";
            if(c == float.class)
                return "F";
            if(c == int.class)
                return "I";
            if(c == long.class)
                return "J";
            if(c == short.class)
                return "S";
            if(c == boolean.class)
                return "Z";
            if(c == void.class)
                return "V";
            throw new RuntimeException("Unrecognized primitive " + c);
        }
        if(c.isArray())
            return c.getName().replace('.', '/');
        return
                ('L' + c.getName() + ';').replace('.', '/');
    }

    static String getMethodDescriptor(Method m) {
        StringBuilder s = new StringBuilder("(");
        for(final Class<?> c : (m.getParameterTypes()))
            s.append(getDescriptorForClass(c));
        s.append(')').append(getDescriptorForClass(m.getReturnType()));
        return s.toString();
    }

}
