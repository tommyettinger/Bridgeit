package emu.com.artemis.utils.reflect;

@SuppressWarnings("rawtypes")
public final class Constructor {

    private final com.badlogic.gdx.utils.reflect.Constructor constructor;
    private final Class declaringClass;
    private final int modifiers;

    Constructor(com.badlogic.gdx.utils.reflect.Constructor constructor, int modifiers) {
        this.constructor = constructor;
        this.declaringClass = constructor.getDeclaringClass();
        this.modifiers = modifiers;
    }

    Constructor(Class declaringClass, int modifiers) {
        this.constructor = null;
        this.declaringClass = declaringClass;
        this.modifiers = modifiers;
    }

    public Class[] getParameterTypes() {
        return constructor != null ? constructor.getParameterTypes() : new Class[0];
    }

    public Class getDeclaringClass() {
        return declaringClass;
    }

    public boolean isAccessible() {
        return constructor == null || constructor.isAccessible();
    }

    public void setAccessible(boolean accessible) {
        if(constructor != null) {
            constructor.setAccessible(accessible);
        }
    }

    public int getModifiers() {
        return modifiers;
    }

    public Object newInstance(Object... args) throws ReflectionException {
        try {
            if(constructor != null) {
                return constructor.newInstance(args);
            }
            return ClassReflection.newInstance(declaringClass);
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }
}
