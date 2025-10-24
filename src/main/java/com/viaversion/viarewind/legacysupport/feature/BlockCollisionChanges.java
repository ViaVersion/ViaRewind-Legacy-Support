/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2025 ViaVersion and contributors
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

package com.viaversion.viarewind.legacysupport.feature;

import com.viaversion.viarewind.legacysupport.util.ReflectionUtil;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.viaversion.viarewind.legacysupport.util.ReflectionUtil.*;
import static com.viaversion.viarewind.legacysupport.util.NMSUtil.*;

public class BlockCollisionChanges {

    public static void fixLilyPad(final Logger logger, final ProtocolVersion serverVersion) {
        try {
            final Field boundingBoxField = getFieldAccessible(getNMSBlockClass("BlockWaterLily"), serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_20_2) ? "a" : "b");

            setBoundingBox(boundingBoxField.get(null), 0.0625, 0.0, 0.0625, 0.9375, 0.015625, 0.9375);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not fix lily pad bounding box.", ex);
        }
    }

    public static void fixCarpet(final Logger logger, final ProtocolVersion serverVersion) {
        try {
            final Class<?> blockCarpetClass = serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_16_4) ? getNMSBlockClass("BlockCarpet") : getNMSBlockClass("CarpetBlock");

            final Field boundingBoxField = getFieldAccessible(blockCarpetClass, serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_20_2) ? "a" : "b");
            setBoundingBox(boundingBoxField.get(0), 0.0D, -0.0000001D, 0.0D, 1.0D, 0.0000001D, 1.0D);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not fix carpet bounding box.", ex);
        }
    }

    public static void fixLadder(final Logger logger, final ProtocolVersion serverVersion) {
        try {
            if (serverVersion.newerThanOrEqualTo(ProtocolVersion.v1_20_5)) {
                final Class<?> blockLadderClass = getNMSBlockClass("BlockLadder");

                final Map<String, double[]> overrides = new HashMap<String, double[]>();
                overrides.put("EAST", new double[]{0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D});
                overrides.put("WEST", new double[]{0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D});
                overrides.put("SOUTH", new double[]{0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D});
                overrides.put("NORTH", new double[]{0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D});

                Field shapesField = null;
                for (Field field : blockLadderClass.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (!Map.class.isAssignableFrom(field.getType())) {
                        continue;
                    }
                    field.setAccessible(true);
                    shapesField = field;
                    break;
                }

                if (shapesField != null) {
                    final Object rawShapes = shapesField.get(null);
                    ReflectionUtil.removeFinal(shapesField);

                    final Class<?> directionClass = Class.forName("net.minecraft.core.Direction");
                    final Class<?> shapesClass = Class.forName("net.minecraft.world.phys.shapes.Shapes");
                    final Class<?> blockClass = Class.forName("net.minecraft.world.level.block.Block");

                    Method shapesBoxMethod = ReflectionUtil.findMethod(shapesClass, new String[]{"box", "a"}, double.class, double.class, double.class, double.class, double.class, double.class);
                    if (shapesBoxMethod != null) {
                        shapesBoxMethod.setAccessible(true);
                    }

                    Method blockBoxMethod = ReflectionUtil.findMethod(blockClass, new String[]{"box", "a"}, double.class, double.class, double.class, double.class, double.class, double.class);
                    if (blockBoxMethod != null) {
                        blockBoxMethod.setAccessible(true);
                    }

                    Method shapesCreateMethod = null;
                    Constructor<?> aabbConstructor = null;
                    if (shapesBoxMethod == null) {
                        final Class<?> aabbClass = Class.forName("net.minecraft.world.phys.AABB");
                        shapesCreateMethod = ReflectionUtil.findMethod(shapesClass, new String[]{"create", "a"}, aabbClass);
                        if (shapesCreateMethod != null) {
                            shapesCreateMethod.setAccessible(true);
                            aabbConstructor = aabbClass.getDeclaredConstructor(double.class, double.class, double.class, double.class, double.class, double.class);
                            aabbConstructor.setAccessible(true);
                        }
                    }

                    final Class<?> directionClassFinal = directionClass;
                    Map<Object, Object> shapes;
                    try {
                        final Constructor<?> enumMapConstructor = EnumMap.class.getDeclaredConstructor(Class.class);
                        enumMapConstructor.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        final Map<Object, Object> enumMap = (Map<Object, Object>) enumMapConstructor.newInstance(directionClassFinal);
                        shapes = enumMap;
                    } catch (ReflectiveOperationException ignored) {
                        shapes = new HashMap<>();
                    }

                    if (rawShapes instanceof Map<?, ?>) {
                        shapes.putAll((Map<?, ?>) rawShapes);
                    }

                    final Method directionValueOf = directionClass.getMethod("valueOf", String.class);
                    directionValueOf.setAccessible(true);

                    for (Map.Entry<String, double[]> entry : overrides.entrySet()) {
                        final Object direction = directionValueOf.invoke(null, entry.getKey());
                        final Object voxelShape = createVoxelShape(shapesBoxMethod, blockBoxMethod, shapesCreateMethod, aabbConstructor, entry.getValue());
                        shapes.put(direction, voxelShape);
                    }

                    shapesField.set(null, shapes);
                } else {
                    final Class<?> voxelShapeInterface = Class.forName("net.minecraft.world.phys.shapes.VoxelShape");

                    boolean updated = false;
                    for (Field field : blockLadderClass.getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        field.setAccessible(true);
                        final Object shapeObject = field.get(null);
                        if (shapeObject == null) {
                            continue;
                        }
                        if (!voxelShapeInterface.isAssignableFrom(shapeObject.getClass())
                                && !shapeObject.getClass().getSimpleName().contains("VoxelShape")) {
                            continue;
                        }

                        final double[] currentBounds = getBoundingBoxValues(shapeObject);
                        if (currentBounds == null) {
                            continue;
                        }

                        final String orientation = detectOrientation(currentBounds);
                        if (orientation == null) {
                            continue;
                        }

                        final double[] override = overrides.get(orientation);
                        if (override == null) {
                            continue;
                        }

                        setBoundingBox(shapeObject, override);
                        updated = true;
                    }

                    if (!updated) {
                        throw new IllegalStateException("Could not adjust ladder shapes using fallback path for modern versions");
                    }
                }
            } else
            {
                final boolean pre1_12_2 = serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_12_2);
                final boolean pre1_13_2 = serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_13_2);
                final boolean pre1_16_4 = serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_16_4);
                final boolean pre1_20_2 = serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_20_2);

                final Class<?> blockLadderClass = getNMSBlockClass("BlockLadder");

                final Field boundingBoxEastField = getFieldAccessible(blockLadderClass, pre1_12_2 ? "b" : pre1_13_2 ? "c" : pre1_16_4 ? "c" : pre1_20_2 ? "d" : "e");
                final Field boundingBoxWestField = getFieldAccessible(blockLadderClass, pre1_12_2 ? "c" : pre1_13_2 ? "o" : pre1_16_4 ? "d" : pre1_20_2 ? "e" : "f");
                final Field boundingBoxSouthField = getFieldAccessible(blockLadderClass, pre1_12_2 ? "d" : pre1_13_2 ? "p" : pre1_16_4 ? "e" : pre1_20_2 ? "f" : "g");
                final Field boundingBoxNorthField = getFieldAccessible(blockLadderClass, pre1_12_2 ? "e" : pre1_13_2 ? "q" : pre1_16_4 ? "f" : pre1_20_2 ? "g" : "h");

                setBoundingBox(boundingBoxEastField.get(null), 0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
                setBoundingBox(boundingBoxWestField.get(null), 0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
                setBoundingBox(boundingBoxSouthField.get(null), 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
                setBoundingBox(boundingBoxNorthField.get(null), 0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not fix ladder bounding box.", ex);
        }
    }

    private static void setBoundingBox(Object boundingBox, double... values) throws ReflectiveOperationException {
        switch (boundingBox.getClass().getSimpleName()) {
            case "AxisAlignedBB": // Legacy NMS
                setAxisAlignedBB(boundingBox, values);
                break;
            case "VoxelShapeArray": // Paper 1.20.4-
            case "ArrayVoxelShape": // Paper 1.20.5+
                setVoxelShapeArray(boundingBox, values);
                break;
            case "AABBVoxelShape": // Tuinity
                setAABBVoxelShape(boundingBox, values);
                break;
            default:
                throw new IllegalStateException("Unknown bounding box type: " + boundingBox.getClass().getName());
        }
    }

    private static void setAABBVoxelShape(Object boundingBox, double[] values) throws ReflectiveOperationException {
        for (Field field : boundingBox.getClass().getFields()) {
            // Set data for internally used AxisAlignedBB
            if (field.getType().getSimpleName().equals("AxisAlignedBB")) {
                setBoundingBox(field.get(boundingBox), values);
            }
            // Clear the cache
            if (field.getType().getSimpleName().equals("DoubleList")) {
                final Object doubleList = field.get(boundingBox);

                doubleList.getClass().getMethod("clear").invoke(doubleList);
            }
        }
    }

    private static void setAxisAlignedBB(final Object boundingBox, final double[] values) throws ReflectiveOperationException {
        final Field[] doubleFields = Arrays.stream(boundingBox.getClass().getDeclaredFields()).filter(f -> f.getType() == double.class && !Modifier.isStatic(f.getModifiers())).toArray(Field[]::new);

        if (doubleFields.length < 6) {
            throw new IllegalStateException("Invalid field count for " + boundingBox.getClass().getName() + ": " + doubleFields.length);
        }

        for (int i = 0; i < 6; i++) {
            Field currentField = doubleFields[i];
            currentField.setAccessible(true);
            currentField.setDouble(boundingBox, values[i]);
        }
    }

    private static void setVoxelShapeArray(final Object voxelShapeArray, final double[] values) throws ReflectiveOperationException {
        final Field[] doubleListFields = Arrays.stream(voxelShapeArray.getClass().getDeclaredFields()).filter(f -> f.getType().getSimpleName().equals("DoubleList")).toArray(Field[]::new);

        if (doubleListFields.length < 3) {
            throw new IllegalStateException("Invalid field count for " + voxelShapeArray.getClass().getName() + ": " + doubleListFields.length);
        }

        // FastUtil is relocated on Spigot but not on Paper
        final String doubleArrayListClass = doubleListFields[0].getType().getName().replace("DoubleList", "DoubleArrayList");
        final Method wrapMethod = Class.forName(doubleArrayListClass).getMethod("wrap", double[].class);

        for (int i = 0; i < 3; i++) {
            final double[] array = {values[i], values[i + 3]};

            final Field field = doubleListFields[i];
            field.setAccessible(true);
            field.set(voxelShapeArray, wrapMethod.invoke(null, (Object) array));
        }

        // Handle Paper voxel shape caching by clearing the cache
        final Class<?> voxelShape = voxelShapeArray.getClass().getSuperclass();

        final Field shape = getFieldAccessible(voxelShape, "a");
        final Field cachedShapeData = getFieldAccessible(shape.getType(), "cachedShapeData");
        if (cachedShapeData == null) { // No Paper or too old version
            return;
        }
        cachedShapeData.set(shape.get(voxelShapeArray), null);

        final Field isEmpty = getFieldAccessible(voxelShape, "isEmpty");
        isEmpty.setBoolean(voxelShapeArray, true);

        final Method initCache = ReflectionUtil.findMethod(voxelShape, new String[]{ "initCache", "moonrise$initCache" });
        if (initCache == null) {
            throw new IllegalStateException("Could not find initCache method in " + voxelShape.getName());
        }
        initCache.invoke(voxelShapeArray);
    }

    private static double[] getBoundingBoxValues(final Object boundingBox) throws ReflectiveOperationException {
        switch (boundingBox.getClass().getSimpleName()) {
            case "AxisAlignedBB":
                return getAxisAlignedBBValues(boundingBox);
            case "VoxelShapeArray":
            case "ArrayVoxelShape":
                return getVoxelShapeArrayValues(boundingBox);
            case "AABBVoxelShape":
                return getAABBVoxelShapeValues(boundingBox);
            default:
                return null;
        }
    }

    private static double[] getAABBVoxelShapeValues(final Object boundingBox) throws ReflectiveOperationException {
        for (Field field : boundingBox.getClass().getFields()) {
            if (field.getType().getSimpleName().equals("AxisAlignedBB")) {
                return getBoundingBoxValues(field.get(boundingBox));
            }
        }
        return null;
    }

    private static double[] getAxisAlignedBBValues(final Object boundingBox) throws ReflectiveOperationException {
        final Field[] doubleFields = Arrays.stream(boundingBox.getClass().getDeclaredFields()).filter(f -> f.getType() == double.class && !Modifier.isStatic(f.getModifiers())).toArray(Field[]::new);

        if (doubleFields.length < 6) {
            throw new IllegalStateException("Invalid field count for " + boundingBox.getClass().getName() + ": " + doubleFields.length);
        }

        final double[] values = new double[6];
        for (int i = 0; i < 6; i++) {
            final Field currentField = doubleFields[i];
            currentField.setAccessible(true);
            values[i] = currentField.getDouble(boundingBox);
        }
        return values;
    }

    private static double[] getVoxelShapeArrayValues(final Object voxelShapeArray) throws ReflectiveOperationException {
        final Field[] doubleListFields = Arrays.stream(voxelShapeArray.getClass().getDeclaredFields()).filter(f -> f.getType().getSimpleName().equals("DoubleList")).toArray(Field[]::new);

        if (doubleListFields.length < 3) {
            throw new IllegalStateException("Invalid field count for " + voxelShapeArray.getClass().getName() + ": " + doubleListFields.length);
        }

        final double[] values = new double[6];

        for (int i = 0; i < 3; i++) {
            final Field field = doubleListFields[i];
            field.setAccessible(true);
            final Object doubleList = field.get(voxelShapeArray);
            if (doubleList == null) {
                return null;
            }

            final Method getDouble = ReflectionUtil.findMethod(doubleList.getClass(), new String[]{"getDouble", "get"}, int.class);
            if (getDouble == null) {
                return null;
            }
            getDouble.setAccessible(true);

            values[i] = ((Number) getDouble.invoke(doubleList, 0)).doubleValue();
            values[i + 3] = ((Number) getDouble.invoke(doubleList, 1)).doubleValue();
        }

        return values;
    }

    private static String detectOrientation(final double[] bounds) {
        final double epsilon = 1.0E-3;
        final double minX = bounds[0];
        final double minZ = bounds[2];
        final double maxX = bounds[3];
        final double maxZ = bounds[5];

        if (minX <= epsilon && maxX <= 0.5D) {
            return "EAST";
        }
        if (maxX >= 1.0D - epsilon && minX >= 0.5D) {
            return "WEST";
        }
        if (minZ <= epsilon && maxZ <= 0.5D) {
            return "SOUTH";
        }
        if (maxZ >= 1.0D - epsilon && minZ >= 0.5D) {
            return "NORTH";
        }
        return null;
    }

    private static Object createVoxelShape(final Method shapesBoxMethod, final Method blockBoxMethod,
                                           final Method shapesCreateMethod, final Constructor<?> aabbConstructor,
                                           final double[] values) throws ReflectiveOperationException {
        if (shapesBoxMethod != null) {
            return shapesBoxMethod.invoke(null, values[0], values[1], values[2], values[3], values[4], values[5]);
        }
        if (shapesCreateMethod != null && aabbConstructor != null) {
            final Object aabb = aabbConstructor.newInstance(values[0], values[1], values[2], values[3], values[4], values[5]);
            return shapesCreateMethod.invoke(null, aabb);
        }
        if (blockBoxMethod != null) {
            final double scale = 16.0D;
            return blockBoxMethod.invoke(null,
                    values[0] * scale, values[1] * scale, values[2] * scale,
                    values[3] * scale, values[4] * scale, values[5] * scale);
        }
        throw new IllegalStateException("Unable to create voxel shape for ladder bounding box.");
    }
}
