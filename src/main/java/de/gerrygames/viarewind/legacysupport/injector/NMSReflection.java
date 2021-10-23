package de.gerrygames.viarewind.legacysupport.injector;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class NMSReflection {

    private static final String BASE = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS = BASE.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    private static Field playerConnectionField;

    private static Method getHandleMethod;
    private static Method sendPacketMethod;

    private static void resolve() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        Class<?> craftPlayerClass = obc("entity.CraftPlayer");
        Class<?> nmsPlayerClass = nms("EntityPlayer", "net.minecraft.server.level.EntityPlayer");
        Class<?> playerConnectionClass = nms("PlayerConnection", "net.minecraft.server.network.PlayerConnection");
        Class<?> packetClass = nms("Packet", "net.minecraft.network.protocol.Packet");

        getHandleMethod = craftPlayerClass.getMethod("getHandle");
        playerConnectionField = Arrays.stream(nmsPlayerClass.getFields())
                .filter(field -> field.getType() == playerConnectionClass).findFirst()
                .orElseThrow(() -> new NoSuchFieldException("Failed to find PlayerConnection field in EntityPlayer"));
        sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);
    }

    public static Class<?> getNMSBlock(String name) {
        try {
            return nms(name, "net.minecraft.world.level.block." + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Method getBukkitSoundMethod() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> nmsSoundEffect = nms("SoundEffect", "net.minecraft.sounds.SoundEffect");
        Class<?> craftSoundClass = obc("CraftSound");
        for (Method method : craftSoundClass.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 1) {
                if (method.getReturnType() == Sound.class && method.getParameterTypes()[0] == nmsSoundEffect) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException("Failed to find getSoundByEffect Method in EntityPlayer");
    }

    public static Class<?> getGamePacketClass(String packetType) {
        try {
            return nms(packetType, "net.minecraft.network.protocol.game." + packetType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void sendPacket(Player player, Object packet) {
        if (getHandleMethod == null) {
            try {
                resolve();
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            Object nmsPlayer = getHandleMethod.invoke(player);
            Object playerConnection = playerConnectionField.get(nmsPlayer);
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Class<?> nms(String className, String fallbackFullClassName) throws ClassNotFoundException {
        try {
            return Class.forName(NMS + "." + className);
        } catch (ClassNotFoundException ignored) {
            return Class.forName(fallbackFullClassName);
        }
    }

    public static Class<?> obc(String className) throws ClassNotFoundException {
        return Class.forName(BASE + "." + className);
    }
}
