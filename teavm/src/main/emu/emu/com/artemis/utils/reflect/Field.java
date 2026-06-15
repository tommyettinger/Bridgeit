package emu.com.artemis.utils.reflect;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class Field {

    private final com.badlogic.gdx.utils.reflect.Field field;

    Field(com.badlogic.gdx.utils.reflect.Field field) {
        this.field = field;
    }

    public String getName() {
        return field.getName();
    }

    public Class getType() {
        return field.getType();
    }

    public Class getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public boolean isAccessible() {
        return field.isAccessible();
    }

    public void setAccessible(boolean accessible) {
        field.setAccessible(accessible);
    }

    public boolean isDefaultAccess() {
        return field.isDefaultAccess();
    }

    public boolean isFinal() {
        return field.isFinal();
    }

    public boolean isPrivate() {
        return field.isPrivate();
    }

    public boolean isProtected() {
        return field.isProtected();
    }

    public boolean isPublic() {
        return field.isPublic();
    }

    public boolean isStatic() {
        return field.isStatic();
    }

    public boolean isTransient() {
        return field.isTransient();
    }

    public boolean isVolatile() {
        return field.isVolatile();
    }

    public boolean isSynthetic() {
        return field.isSynthetic();
    }

    public Class getElementType(int index) {
        return field.getElementType(index);
    }

    public Object get(Object obj) throws ReflectionException {
        try {
            return field.get(obj);
        }
        catch(com.badlogic.gdx.utils.reflect.ReflectionException e) {
            throw new ReflectionException(e.getMessage(), e);
        }
    }

    public void set(Object obj, Object value) throws ReflectionException {
        try {
            field.set(obj, value);
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

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Field)) {
            return false;
        }

        Field field1 = (Field)o;
        return field.equals(field1.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }
}
