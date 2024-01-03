package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub;

import java.io.Serializable;

public class Ref {
    private Ref() {}

    public static final class ObjectRef<T> implements Serializable {
        public T element;

        @Override
        public String toString() {
            return String.valueOf(element);
        }
    }

    public static final class IntRef implements Serializable {
        public int element;

        public String toString() {
            return String.valueOf(this.element);
        }
    }
}
