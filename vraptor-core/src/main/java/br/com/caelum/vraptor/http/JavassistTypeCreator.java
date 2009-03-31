package br.com.caelum.vraptor.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import br.com.caelum.vraptor.resource.ResourceMethod;

public class JavassistTypeCreator implements TypeCreator {

    /*
     * we require the class loading counter in order to reload method params
     * when reloading classes in the same classloader
     */
    private static int classLoadCounter = 0;

    public Class<?> typeFor(ResourceMethod method) {
        Method reflectionMethod = method.getMethod();
        ClassPool pool = new ClassPool();
        pool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
        CtClass ctType = pool
                .makeClass(reflectionMethod.getDeclaringClass().getName() + "$" + reflectionMethod.getName() + "$"
                        + Math.abs(reflectionMethod.hashCode()) + "$" + (++classLoadCounter));
        String valueLists = "";
        for (Class type : reflectionMethod.getParameterTypes()) {
            try {
                String fieldName = extractName(type);
                CtField field = CtField.make("private " + extractTypeDefinition(type) + " " + fieldName + "_;", ctType);
                ctType.addField(field);
                ctType.addMethod(CtNewMethod.getter("get" + fieldName, field));
                ctType.addMethod(CtNewMethod.setter("set" + fieldName, field));
                if (!valueLists.equals("")) {
                    valueLists += ",";
                }
                if (type.isPrimitive()) {
                    valueLists += wrapperCodeFor(type, fieldName + "_");
                } else {
                    valueLists += fieldName + "_";
                }
            } catch (CannotCompileException e) {
                // TODO validation exception?
                throw new IllegalArgumentException("unable to compile expression", e);
            }
        }
        try {
            ctType.addMethod(CtNewMethod.make("public Object[] gimmeMyValues() { return new Object[]{" + valueLists
                    + "};}", ctType));
        } catch (CannotCompileException e) {
            // TODO validation exception?
            throw new IllegalArgumentException("unable to compile expression", e);
        }
        try {
            return ctType.toClass();
        } catch (CannotCompileException e) {
            // TODO validation exception?
            throw new IllegalArgumentException("unable to compile expression", e);
        }
    }

    private String extractTypeDefinition(Class type) {
        if (type.isArray()) {
            return type.getComponentType().getName() + "[] ";
        }
        return type.getName();
    }

    private String extractName(Class type) {
        if (type.isArray()) {
            return type.getComponentType().getSimpleName();
        }
        return type.getSimpleName();
    }

    private static final Map<Class<?>, String> wrapper = new HashMap<Class<?>, String>();
    static {
        wrapper.put(int.class, "Integer.valueOf(");
        wrapper.put(long.class, "Long.valueOf(");
        wrapper.put(double.class, "Double.valueOf(");
        wrapper.put(float.class, "Float.valueOf(");
        wrapper.put(byte.class, "Byte.valueOf(");
        wrapper.put(char.class, "Character.valueOf(");
        wrapper.put(short.class, "Short.valueOf(");
        wrapper.put(boolean.class, "Boolean.valueOf(");
    }

    private String wrapperCodeFor(Class<?> type, String fieldName) {
        return wrapper.get(type) + fieldName + ")";
    }

}