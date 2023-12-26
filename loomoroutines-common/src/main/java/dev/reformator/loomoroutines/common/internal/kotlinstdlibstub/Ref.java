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
}
