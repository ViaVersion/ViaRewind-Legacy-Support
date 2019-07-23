package de.gerrygames.viarewind.legacysupport.injector;

import de.gerrygames.viarewind.legacysupport.reflection.ReflectionAPI;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BoundingBoxFixer {

	public static void fixLilyPad() {
		try {
			Class blockWaterLilyClass = NMSReflection.getNMSClass("BlockWaterLily");
			Field boundingBoxField = ReflectionAPI.getFieldAccessible(blockWaterLilyClass, "a");
			setBoundingBox(boundingBoxField, 0.0625, 0.0, 0.0625, 0.9375, 0.015625, 0.9375);
		} catch (Exception ex) {
			System.out.println("Could not fix lily pad bounding box.");
			ex.printStackTrace();
		}
	}

	public static void fixLadder() {
		try {
			Class blockLadderClass = NMSReflection.getNMSClass("BlockLadder");

			Field boundingBoxNorthField, boundingBoxSouthField, boundingBoxWestField, boundingBoxEastField;

			if (ProtocolRegistry.SERVER_PROTOCOL < 107) {
				return;
			} else if (ProtocolRegistry.SERVER_PROTOCOL <= 340) {
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "b");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "c");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "d");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "e");
			} else if (ProtocolRegistry.SERVER_PROTOCOL <= 404) {
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "c");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "o");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "p");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "q");
			} else {
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "c");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "d");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "e");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "f");
			}

			setBoundingBox(boundingBoxEastField, 0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
			setBoundingBox(boundingBoxWestField, 0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
			setBoundingBox(boundingBoxSouthField, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
			setBoundingBox(boundingBoxNorthField, 0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
		} catch (Exception ex) {
			System.out.println("Could not fix ladder bounding box.");
			ex.printStackTrace();
		}
	}

	private static void setBoundingBox(Field field, double x1, double y1, double z1, double x2, double y2, double z2) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if (field.getType().getSimpleName().equals("AxisAlignedBB")) {
			Class boundingBoxClass = field.getType();
			Constructor boundingBoxConstructor = boundingBoxClass.getConstructor(double.class, double.class, double.class, double.class, double.class, double.class);
			Object boundingBox = boundingBoxConstructor.newInstance(x1, y1, z1, x2, y2, z2);
			ReflectionAPI.setFinalValue(field, boundingBox);
		} else if (field.getType().getSimpleName().equals("VoxelShape")) {
			Method createVoxelShape = ReflectionAPI.getMethod(NMSReflection.getNMSClass("VoxelShapes"), "create", double.class, double.class, double.class, double.class, double.class, double.class);
			Object boundingBox = ReflectionAPI.invokeMethod(createVoxelShape, x1, y1, z1, x2, y2, z2);
			ReflectionAPI.setFinalValue(field, boundingBox);
		} else {
			throw new IllegalStateException();
		}
	}
}
