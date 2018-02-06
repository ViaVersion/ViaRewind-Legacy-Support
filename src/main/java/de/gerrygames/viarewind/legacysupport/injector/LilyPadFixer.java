package de.gerrygames.viarewind.legacysupport.injector;

import de.gerrygames.viarewind.legacysupport.reflection.ReflectionAPI;

import java.lang.reflect.Constructor;

public class LilyPadFixer {
	public static void fix() {
		try {
			Class blockWaterLilyClass = NMSReflection.getNMSClass("BlockWaterLily");
			Class boundingBoxClass = NMSReflection.getNMSClass("AxisAlignedBB");
			Constructor boundingBoxConstructor = boundingBoxClass.getConstructor(double.class, double.class, double.class, double.class, double.class, double.class);
			Object boundingBox = boundingBoxConstructor.newInstance(0.0625, 0.0, 0.0625, 0.9375, 0.015625, 0.9375);
			ReflectionAPI.setFinalValue(blockWaterLilyClass, "a", boundingBox);
		} catch (Exception ignored) {
			System.out.println("Could not fix Lily Pads bounding box.");
		}
	}
}
