package de.gerrygames.viarewind.legacysupport.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionAPI {
	private static Map<String, Field> fields = new HashMap<>();

	public static Field getField(Class clazz, String fieldname) throws NoSuchFieldException {
		String c = clazz.getName() + ":" + fieldname;
		Field f = fields.get(c);
		if (f == null) {
			f = clazz.getDeclaredField(fieldname);
			fields.put(c, f);
		}
		return f;
	}

	public static void setFieldNotFinal(Field field) {
		try {
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			if (Modifier.isFinal(field.getModifiers())) {
				modifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
			}
		} catch (Exception ignored) {}
	}

	public static Field getFieldAccessible(Class clazz, String fieldname) throws NoSuchFieldException {
		Field field = getField(clazz, fieldname);
		field.setAccessible(true);
		return field;
	}

	public static Object getValue(Class clazz, Object object, String fieldname) throws Exception {
		return getFieldAccessible(clazz, fieldname).get(object);
	}

	public static Object getValue(Object object, String fieldname) throws Exception {
		return getValue(object.getClass(), object, fieldname);
	}

	public static Object getValue(Class clazz, String fieldname) throws Exception {
		return getValue(clazz, null, fieldname);
	}

	public static Object getValuePrintException(Object object, String fieldname) {
		try {
			return getValue(object, fieldname);
		} catch (Exception ex) {ex.printStackTrace();}
		return null;
	}

	public static Object getValuePrintException(Class clazz, String fieldname) {
		try {
			return getValue(clazz, fieldname);
		} catch (Exception ex) {ex.printStackTrace();}
		return null;
	}

	public static Object getValueIgnoreException(Object object, String fieldname) {
		try {
			return getValue(object, fieldname);
		} catch (Exception ignored) {}
		return null;
	}

	public static Object getValueIgnoreException(Class clazz, String fieldname) {
		try {
			return getValue(clazz, fieldname);
		} catch (Exception ignored) {}
		return null;
	}

	private static void setValue(Class clazz, Object object, String fieldname, Object value, boolean isFinal) throws Exception {
		Field field = getFieldAccessible(clazz, fieldname);
		if (isFinal) setFieldNotFinal(field);
		field.set(object, value);
	}

	public static void setValue(Object object, String fieldname, Object value) throws Exception {
		setValue(object.getClass(), object, fieldname, value, false);
	}

	public static void setValue(Class clazz, String fieldname, Object value) throws Exception {
		setValue(clazz, null, fieldname, value, false);
	}

	public static void setFinalValue(Object object, String fieldname, Object value) throws Exception {
		setValue(object.getClass(), object, fieldname, value, true);
	}

	public static void setFinalValue(Class clazz, String fieldname, Object value) throws Exception {
		setValue(clazz, null, fieldname, value, true);
	}

	public static void setValuePrintException(Object object, String fieldname, Object value) {
		try {
			setValue(object, fieldname, value);
		} catch (Exception ex) {ex.printStackTrace();}
	}

	public static void setValuePrintException(Class clazz, String fieldname, Object value) {
		try {
			setValue(clazz, fieldname, value);
		} catch (Exception ex) {ex.printStackTrace();}
	}

	public static void setFinalValuePrintException(Object object, String fieldname, Object value) {
		try {
			setFinalValue(object, fieldname, value);
		} catch (Exception ex) {ex.printStackTrace();}
	}

	public static void setFinalValuePrintException(Class clazz, String fieldname, Object value) {
		try {
			setFinalValue(clazz, fieldname, value);
		} catch (Exception ex) {ex.printStackTrace();}
	}

	public static void setValueIgnoreException(Object object, String fieldname, Object value) {
		try {
			setValue(object, fieldname, value);
		} catch (Exception ignored) {}
	}

	public static void setValueIgnoreException(Class clazz, String fieldname, Object value) {
		try {
			setValue(clazz, fieldname, value);
		} catch (Exception ignored) {}
	}

	public static void setFinalValueIgnoreException(Object object, String fieldname, Object value) {
		try {
			setFinalValue(object, fieldname, value);
		} catch (Exception ignored) {}
	}

	public static void setFinalValueIgnoreException(Class clazz, String fieldname, Object value) {
		try {
			setFinalValue(clazz, fieldname, value);
		} catch (Exception ignored) {}
	}
}
