/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2026 ViaVersion and contributors
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
package com.viaversion.viarewind.legacysupport.util;

import com.viaversion.viarewind.legacysupport.BukkitPlugin;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Common reflection utilities for Java 8 and newer.
 */
public class ReflectionUtil {

    private static final Map<String, Field> fieldCache = new HashMap<>();
    private static boolean staticFinalModificationBlocked;

    static {
        try {
            Field.class.getDeclaredField("modifiers");
        } catch (NoSuchFieldException ex) {
            staticFinalModificationBlocked = true;
        }
    }

    public static Method findMethod(final Class<?> clazz, final String[] methodNames, final Class<?>... parameterTypes) {
        for (String methodName : methodNames) {
            final Method method = getMethod(clazz, methodName, parameterTypes);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    /**
     * Recursively search for a method in the class and its superclasses. Returns null if the method is not found.
     *
     * @param clazz          The class to search in
     * @param methodName     The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method if found, otherwise null
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            final Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            }
            return getMethod(superClass, methodName, parameterTypes);
        }
    }

    public static Field getFieldAndCache(final Class<?> clazz, final String fieldName) {
        final String key = clazz.getName() + ":" + fieldName;
        if (fieldCache.containsKey(key)) {
            return fieldCache.get(key);
        } else {
            Field field = null;
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            } // Cache non-existing field too
            fieldCache.put(key, field);
            return field;
        }
    }

    /**
     * Gets and field from the {@link #fieldCache} or directly and makes it accessible.
     *
     * @param clazz     The class
     * @param fieldName The field name
     * @return The field
     */
    public static Field getFieldAccessible(final Class<?> clazz, final String fieldName) {
        final Field field = getFieldAndCache(clazz, fieldName);
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * Get a field from a class, and print an error message if it fails.
     *
     * @param clazz     The class
     * @param fieldName The field name
     * @return The field, or null if it fails
     */
    public static Field failSafeGetField(final Class<?> clazz, final String fieldName) {
        try {
            return getFieldAccessible(clazz, fieldName);
        } catch (Exception e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to get field " + fieldName + " in class " + clazz.getName(), e);
            return null;
        }
    }

    public static void setValue(final Class<?> clazz, final Object object, final String name, final Object value, final boolean isFinal) throws IllegalAccessException {
        final Field field = getFieldAccessible(clazz, name);
        if (isFinal) {
            removeFinal(field);
        }
        field.set(object, value);
    }

    /**
     * Remove the final modifier from a field.
     *
     * @param field The field
     */
    public static void removeFinal(final Field field) {
        final int modifiers = field.getModifiers();
        if (!Modifier.isFinal(modifiers)) {
            // Non-finals don't need to be modified
            return;
        }
        if (!staticFinalModificationBlocked) {
            // Older Java versions allow us to modify the modifiers directly and remove
            // the final modifier
            failSafeSetValue(Field.class, field, "modifiers", modifiers & ~Modifier.FINAL);
            return;
        }

        // Modern Java bypass
        try {
            final Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);

            final Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
            for (Field classField : fields) {
                if ("modifiers".equals(classField.getName())) {
                    classField.setAccessible(true);
                    classField.set(field, modifiers & ~Modifier.FINAL);
                    break;
                }
            }
        } catch (ReflectiveOperationException e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to remove final modifier from field " + field.getName(), e);
        }
    }

    /**
     * Unsafe wrapper for value setting which bypasses final checks.
     *
     * @param clazz  The class
     * @param object The object
     * @param name   The field name
     * @param value  The value
     * @throws IllegalAccessException If the field cannot be accessed
     */
    public static void setValue(final Class<?> clazz, final Object object, final String name, final Object value) throws IllegalAccessException {
        setValue(clazz, object, name, value, false);
    }

    /**
     * Set a field value, and print an error message if it fails.
     *
     * @param clazz  The class
     * @param object The object
     * @param name   The field name
     * @param value  The value
     */
    public static void failSafeSetValue(final Class<?> clazz, final Object object, final String name, final Object value) {
        try {
            setValue(clazz, object, name, value);
        } catch (Exception e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to set value for field " + name + " in class " + clazz.getName(), e);
        }
    }

    public static Class<?> failSafeGetClass(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to get class " + name, e);
            return null;
        }
    }

}
