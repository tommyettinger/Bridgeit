package emu.com.artemis.utils.reflect;

import java.lang.reflect.Modifier;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ClassReflection {

    static public Class forName(String name) throws ReflectionException {
        try {
            return com.badlogic.gdx.utils.reflect.ClassReflection.forName(name);
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    static public String getSimpleName(Class c) {
        return com.badlogic.gdx.utils.reflect.ClassReflection.getSimpleName(c);
    }

    static public boolean isInstance(Class c, Object obj) {
        return com.badlogic.gdx.utils.reflect.ClassReflection.isInstance(c, obj);
    }

    static public boolean isAssignableFrom(Class c1, Class c2) {
        return com.badlogic.gdx.utils.reflect.ClassReflection.isAssignableFrom(c1, c2);
    }

    static public boolean isMemberClass(Class c) {
        return com.badlogic.gdx.utils.reflect.ClassReflection.isMemberClass(c);
    }

    static public boolean isStaticClass(Class c) {
        return com.badlogic.gdx.utils.reflect.ClassReflection.isStaticClass(c);
    }

    static public boolean isAbstractClass(Class c) {
        return com.badlogic.gdx.utils.reflect.ClassReflection.isAbstract(c);
    }

    static public <T> T newInstance(Class<T> c) throws ReflectionException {
        Object instance = newBridgeitComponent(c);
        if(instance != null) {
            return (T)instance;
        }

        try {
            return com.badlogic.gdx.utils.reflect.ClassReflection.newInstance(c);
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    static private Object newBridgeitComponent(Class c) {
        String name = c.getName();
        if("com.dbcgames.bridgeit.AiCmp".equals(name)) return new com.dbcgames.bridgeit.AiCmp();
        if("com.dbcgames.bridgeit.BBoxCmp".equals(name)) return new com.dbcgames.bridgeit.BBoxCmp();
        if("com.dbcgames.bridgeit.MobCmp".equals(name)) return new com.dbcgames.bridgeit.MobCmp();
        if("com.dbcgames.bridgeit.PlayerCmp".equals(name)) return new com.dbcgames.bridgeit.PlayerCmp();
        if("com.dbcgames.bridgeit.SmileyCmp".equals(name)) return new com.dbcgames.bridgeit.SmileyCmp();
        if("com.dbcgames.bridgeit.TexaCmp".equals(name)) return new com.dbcgames.bridgeit.TexaCmp();
        return null;
    }

    static public Constructor[] getConstructors(Class c) {
        return new Constructor[] {new Constructor(c, Modifier.PUBLIC)};
    }

    static public Constructor getConstructor(Class c, Class... parameterTypes) throws ReflectionException {
        return new Constructor(c, Modifier.PUBLIC);
    }

    static public Constructor getDeclaredConstructor(Class c, Class... parameterTypes) throws ReflectionException {
        return new Constructor(c, 0);
    }

    static public Method[] getMethods(Class c) {
        com.badlogic.gdx.utils.reflect.Method[] methods = com.badlogic.gdx.utils.reflect.ClassReflection.getMethods(c);
        Method[] result = new Method[methods.length];
        for(int i = 0, j = methods.length; i < j; i++) {
            result[i] = new Method(methods[i]);
        }
        return result;
    }

    static public Method getMethod(Class c, String name, Class... parameterTypes) throws ReflectionException {
        try {
            return new Method(com.badlogic.gdx.utils.reflect.ClassReflection.getMethod(c, name, parameterTypes));
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    static public Method[] getDeclaredMethods(Class c) {
        com.badlogic.gdx.utils.reflect.Method[] methods = com.badlogic.gdx.utils.reflect.ClassReflection.getDeclaredMethods(c);
        Method[] result = new Method[methods.length];
        for(int i = 0, j = methods.length; i < j; i++) {
            result[i] = new Method(methods[i]);
        }
        return result;
    }

    static public Method getDeclaredMethod(Class c, String name, Class... parameterTypes) throws ReflectionException {
        try {
            return new Method(com.badlogic.gdx.utils.reflect.ClassReflection.getDeclaredMethod(c, name, parameterTypes));
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    static public Field[] getFields(Class c) {
        com.badlogic.gdx.utils.reflect.Field[] fields = com.badlogic.gdx.utils.reflect.ClassReflection.getFields(c);
        Field[] result = new Field[fields.length];
        for(int i = 0, j = fields.length; i < j; i++) {
            result[i] = new Field(fields[i]);
        }
        return result;
    }

    static public Field getField(Class c, String name) throws ReflectionException {
        try {
            return new Field(com.badlogic.gdx.utils.reflect.ClassReflection.getField(c, name));
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    static public Field[] getDeclaredFields(Class c) {
        com.badlogic.gdx.utils.reflect.Field[] fields = com.badlogic.gdx.utils.reflect.ClassReflection.getDeclaredFields(c);
        Field[] result = new Field[fields.length];
        for(int i = 0, j = fields.length; i < j; i++) {
            result[i] = new Field(fields[i]);
        }
        return result;
    }

    static public <T extends java.lang.annotation.Annotation> T getAnnotation(Class c, Class<T> annotationClass) {
        return null;
    }

    static public Field getDeclaredField(Class c, String name) throws ReflectionException {
        try {
            return new Field(com.badlogic.gdx.utils.reflect.ClassReflection.getDeclaredField(c, name));
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    static public boolean isAnnotationPresent(Class c, Class<? extends java.lang.annotation.Annotation> annotationType) {
        return false;
    }

    static public Annotation[] getDeclaredAnnotations(Class c) {
        return new Annotation[0];
    }

    static public Annotation getDeclaredAnnotation(Class c, Class<? extends java.lang.annotation.Annotation> annotationType) {
        return null;
    }
}
