package de.gerrygames.viarewind.legacysupport.reflection;

import java.util.Arrays;
import java.util.StringJoiner;

public class MethodSignature {

    private final String name;
    private final Class<?>[] parameterTypes;

    public MethodSignature(String name, Class<?>... parameterTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    public String name() {
        return name;
    }

    public Class<?>[] parameterTypes() {
        return parameterTypes;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MethodSignature.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("parameterTypes=" + Arrays.toString(parameterTypes))
                .toString();
    }
}
