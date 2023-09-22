/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2016-2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viarewind.legacysupport.reflection;

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
