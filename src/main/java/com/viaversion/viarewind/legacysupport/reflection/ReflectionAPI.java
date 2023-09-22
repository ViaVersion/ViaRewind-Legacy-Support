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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReflectionAPI {
    private static final Map<String, Field> fields = new HashMap<>();
    private static boolean staticFinalModificationBlocked;

    static {
        try {
            Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException ex) {
            staticFinalModificationBlocked = true;
        }
    }

    /**
     * Recursively searches for (declared) methods at a specific class and all it's superclasses
     *
     * @param holder     The base class where to start searching
     * @param signatures Possible method signatures consisting of method name and parameters
     * @return The found {@link Method} or {@code null}
     * @throws RuntimeException If no method was found
     */
    public static Method pickMethod(Class<?> holder, MethodSignature... signatures) {
        Class<?> depth = holder;
        do {
            for (MethodSignature signature : signatures) {
                try {
                    Method method = depth.getDeclaredMethod(signature.name(), signature.parameterTypes());
                    if (signature.returnType() != null && !Objects.equals(method.getReturnType(), signature.returnType())) {
                        continue;
                    }
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    return method;
                } catch (NoSuchMethodException ignored) {
                }
            }
        } while ((depth = depth.getSuperclass()) != null);
        throw new RuntimeException("Failed to resolve method in " + holder + " using " + Arrays.toString(signatures));
    }

    public static Field getField(Class clazz, String fieldname) {
        String key = clazz.getName() + ":" + fieldname;
        Field field = null;
        if (fields.containsKey(key)) {
            field = fields.get(key);
        } else {
            try {
                field = clazz.getDeclaredField(fieldname);
            } catch (NoSuchFieldException ignored) {
            }
            fields.put(key, field);
        }
        return field;
    }

    public static Field getFieldAccessible(Class clazz, String fieldname) {
        Field field = getField(clazz, fieldname);
        if (field != null) field.setAccessible(true);
        return field;
    }

    public static void setFieldNotFinal(Field field) {
        int modifiers = field.getModifiers();
        if (!Modifier.isFinal(modifiers)) return;

        if (staticFinalModificationBlocked) {
            try {
                Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
                getDeclaredFields0.setAccessible(true);
                Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
                for (Field classField : fields) {
                    if ("modifiers".equals(classField.getName())) {
                        classField.setAccessible(true);
                        classField.set(field, modifiers & ~Modifier.FINAL);
                        break;
                    }
                }
            } catch (ReflectiveOperationException ex) {
                ex.printStackTrace();
            }
        } else {
            setValuePrintException(Field.class, field, "modifiers", modifiers & ~Modifier.FINAL);
        }
    }

    public static void setValue(Class clazz, Object object, String fieldname, Object value, boolean isFinal) throws IllegalAccessException {
        Field field = getFieldAccessible(clazz, fieldname);
        if (isFinal) setFieldNotFinal(field);
        field.set(object, value);
    }

    public static void setValue(Class clazz, Object object, String fieldname, Object value) throws NoSuchFieldException, IllegalAccessException {
        setValue(clazz, object, fieldname, value, false);
    }

    public static void setValuePrintException(Class clazz, Object object, String fieldname, Object value) {
        try {
            setValue(clazz, object, fieldname, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
