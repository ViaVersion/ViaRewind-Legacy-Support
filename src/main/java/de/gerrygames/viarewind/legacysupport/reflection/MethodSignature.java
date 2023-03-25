package de.gerrygames.viarewind.legacysupport.reflection;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class MethodSignature {

    private final String name;
    private final Class<?>[] parameterTypes;

    private Class<?> returnType;

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

    public Class<?> returnType() {
        return returnType;
    }

    public MethodSignature withReturnType(Class<?> returnType) {
        Objects.requireNonNull(returnType);
        this.returnType = returnType;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MethodSignature.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("parameterTypes=" + Arrays.toString(parameterTypes))
                .toString();
    }
}
