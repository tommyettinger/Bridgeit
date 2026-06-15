package emu.com.artemis.utils.reflect;

public final class Annotation {

    private final com.badlogic.gdx.utils.reflect.Annotation annotation;

    Annotation(com.badlogic.gdx.utils.reflect.Annotation annotation) {
        this.annotation = annotation;
    }

    public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationType) {
        return annotation.getAnnotation(annotationType);
    }

    public Class<? extends java.lang.annotation.Annotation> getAnnotationType() {
        return annotation.getAnnotationType();
    }
}
