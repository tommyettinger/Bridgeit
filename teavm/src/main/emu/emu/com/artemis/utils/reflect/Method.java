package emu.com.artemis.utils.reflect;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Method {

    private final com.badlogic.gdx.utils.reflect.Method method;

    Method(com.badlogic.gdx.utils.reflect.Method method) {
        this.method = method;
    }

    public String getName() {
        return method.getName();
    }

    public Class getReturnType() {
        return method.getReturnType();
    }

    public Class[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class getDeclaringClass() {
        return method.getDeclaringClass();
    }

    public boolean isAccessible() {
        return method.isAccessible();
    }

    public void setAccessible(boolean accessible) {
        method.setAccessible(accessible);
    }

    public boolean isAbstract() {
        return method.isAbstract();
    }

    public boolean isDefaultAccess() {
        return method.isDefaultAccess();
    }

    public boolean isFinal() {
        return method.isFinal();
    }

    public boolean isPrivate() {
        return method.isPrivate();
    }

    public boolean isProtected() {
        return method.isProtected();
    }

    public boolean isPublic() {
        return method.isPublic();
    }

    public boolean isNative() {
        return method.isNative();
    }

    public boolean isStatic() {
        return method.isStatic();
    }

    public boolean isVarArgs() {
        return method.isVarArgs();
    }

    public Object invoke(Object obj, Object... args) throws ReflectionException {
        try {
            return method.invoke(obj, args);
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    public boolean isAnnotationPresent(Class<? extends java.lang.annotation.Annotation> annotationType) {
        return false;
    }

    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }

    public Annotation getDeclaredAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) {
        return null;
    }
}
