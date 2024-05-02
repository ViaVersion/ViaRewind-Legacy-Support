/*
 * This file is part of ViaRewind-Legacy-Support - https://github.com/ViaVersion/ViaRewind-Legacy-Support
 * Copyright (C) 2018-2024 ViaVersion and contributors
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

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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

        final Method initCache = voxelShape.getDeclaredMethod("initCache");
        initCache.invoke(voxelShapeArray);
    }
}
