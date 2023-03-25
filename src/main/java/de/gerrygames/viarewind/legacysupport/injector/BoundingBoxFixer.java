package de.gerrygames.viarewind.legacysupport.injector;

import com.viaversion.viaversion.api.Via;
import de.gerrygames.viarewind.legacysupport.BukkitPlugin;
import de.gerrygames.viarewind.legacysupport.reflection.ReflectionAPI;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoundingBoxFixer {

	public static void fixLilyPad() {
		try {
			Class<?> blockWaterLilyClass = NMSReflection.getNMSBlock("BlockWaterLily");

			Field boundingBoxField = ReflectionAPI.getFieldAccessible(blockWaterLilyClass, "a");
			Object boundingBox = boundingBoxField.get(null);

			setBoundingBox(boundingBox, 0.0625, 0.0, 0.0625, 0.9375, 0.015625, 0.9375);
		} catch (Exception ex) {
			BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not fix lily pad bounding box.", ex);
		}
	}

	public static void fixLadder() {
		try {
			Class<?> blockLadderClass = NMSReflection.getNMSBlock("BlockLadder");

			Field boundingBoxNorthField, boundingBoxSouthField, boundingBoxWestField, boundingBoxEastField;

			int serverProtocol = Via.getAPI().getServerVersion().lowestSupportedVersion();
			if (serverProtocol <= 340) {
				System.out.println(Arrays.stream(blockLadderClass.getFields()).map(c -> c+"").collect(Collectors.joining(", ")));
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "b");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "c");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "d");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "e");
			} else if (serverProtocol <= 404) {
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "c");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "o");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "p");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "q");
			} else if (serverProtocol <= 754) {
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "c");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "d");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "e");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "f");
			} else {
				boundingBoxEastField = ReflectionAPI.getFieldAccessible(blockLadderClass, "d");
				boundingBoxWestField = ReflectionAPI.getFieldAccessible(blockLadderClass, "e");
				boundingBoxSouthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "f");
				boundingBoxNorthField = ReflectionAPI.getFieldAccessible(blockLadderClass, "g");
			}

			setBoundingBox(boundingBoxEastField.get(null), 0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);
			setBoundingBox(boundingBoxWestField.get(null), 0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
			setBoundingBox(boundingBoxSouthField.get(null), 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
			setBoundingBox(boundingBoxNorthField.get(null), 0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
		} catch (Exception ex) {
			BukkitPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not fix ladder bounding box.", ex);
		}
	}

	private static void setBoundingBox(Object boundingBox, double... values) throws ReflectiveOperationException {
		if (boundingBox.getClass().getSimpleName().equals("VoxelShapeArray")) {
			setVoxelShapeArray(boundingBox, values);
			return;
		}

		if (boundingBox.getClass().getSimpleName().equals("AxisAlignedBB")) {
			setAxisAlignedBB(boundingBox, values);
			return;
		}

		// Tuinity support
		if (boundingBox.getClass().getSimpleName().equals("AABBVoxelShape")) {
			setAABBVoxelShape(boundingBox, values);
			return;
		}

		throw new IllegalStateException("Unknown bounding box type: " + boundingBox.getClass().getName());
	}

	private static void setAABBVoxelShape(Object boundingBox, double[] values) throws ReflectiveOperationException {
		for (Field field : boundingBox.getClass().getFields()) {
			// Set data for internally used AxisAlignedBB
			if (field.getType().getSimpleName().equals("AxisAlignedBB")) {
				setBoundingBox(field.get(boundingBox), values);
			}
			// Clear the cache
			if (field.getType().getSimpleName().equals("DoubleList")) {
				Object doubleList = field.get(boundingBox);
				doubleList.getClass().getMethod("clear").invoke(doubleList);
			}
		}
	}

	private static void setAxisAlignedBB(Object boundingBox, double[] values) throws ReflectiveOperationException {
		Field[] doubleFields = Arrays.stream(boundingBox.getClass().getDeclaredFields())
				.filter(f -> f.getType() == double.class && !Modifier.isStatic(f.getModifiers()))
				.toArray(Field[]::new);

		if (doubleFields.length < 6) {
			throw new IllegalStateException("Invalid field count for " + boundingBox.getClass().getName() + ": " + doubleFields.length);
		}

		for (int i = 0; i < 6; i++) {
			Field currentField = doubleFields[i];
			currentField.setAccessible(true);
			currentField.setDouble(boundingBox, values[i]);
		}
	}

	private static void setVoxelShapeArray(Object voxelShapeArray, double[] values) throws ReflectiveOperationException {
		Field[] doubleListFields = Arrays.stream(voxelShapeArray.getClass().getDeclaredFields())
				.filter(f -> f.getType().getSimpleName().equals("DoubleList"))
				.toArray(Field[]::new);

		if (doubleListFields.length < 3) {
			throw new IllegalStateException("Invalid field count for " + voxelShapeArray.getClass().getName() + ": " + doubleListFields.length);
		}

		// fastutil is relocated on Spigot but not on Paper
		String doubleArrayListClass = doubleListFields[0].getType().getName().replace("DoubleList", "DoubleArrayList");
		Method wrapMethod = Class.forName(doubleArrayListClass).getMethod("wrap", double[].class);

		for (int i = 0; i < 3; i++) {
			double[] array = {values[i], values[i + 3]};
			Field field = doubleListFields[i];
			field.setAccessible(true);
			field.set(voxelShapeArray, wrapMethod.invoke(null, (Object) array));
		}
	}
}
