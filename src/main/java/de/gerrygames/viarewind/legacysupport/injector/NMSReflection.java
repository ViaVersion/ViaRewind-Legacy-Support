package de.gerrygames.viarewind.legacysupport.injector;

import com.viaversion.viaversion.api.Via;
import de.gerrygames.viarewind.legacysupport.reflection.MethodSignature;
import de.gerrygames.viarewind.legacysupport.reflection.ReflectionAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;

public class NMSReflection {

    private static int protocolVersion = -1;
    private static final int PROTOCOL_1_17 = 755;

    private static String version;
    private static Field playerConnectionField;

    public static String getVersion() {
        return version == null ? version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] : version;
    }

    public static int getProtocolVersion() {
        return protocolVersion == -1 ?
                protocolVersion = Via.getAPI().getServerVersion().lowestSupportedVersion() :
                protocolVersion;
    }

    public static Class<?> getBlockPositionClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.core.BlockPosition");
            }
            return getLegacyNMSClass("BlockPosition");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getNMSBlock(String name) {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.world.level.block." + name);
            }
            return getLegacyNMSClass(name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Class getSoundCategoryClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.sounds.SoundCategory");
            }
            return getLegacyNMSClass("SoundCategory");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getPacketClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.network.protocol.Packet");
            }
            return getLegacyNMSClass("Packet");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getGamePacketClass(String packetType) {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.network.protocol.game." + packetType);
            }
            return getLegacyNMSClass(packetType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getPlayerConnectionClass() {
        try {
            if (getProtocolVersion() >= PROTOCOL_1_17) {
                return Class.forName("net.minecraft.server.network.PlayerConnection");
            }
            return getLegacyNMSClass("PlayerConnection");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Class<?> getLegacyNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getVersion() + "." + name);
    }

    public static Class<?> getCraftBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            if (playerConnectionField == null) {
                playerConnectionField = Arrays.stream(nmsPlayer.getClass().getFields())
                        .filter(field -> field.getType() == getPlayerConnectionClass()).findFirst()
                        .orElseThrow(() -> new ReflectiveOperationException("Failed to find PlayerConnection field in EntityPlayer"));
            }
            Object playerConnection = playerConnectionField.get(nmsPlayer);
            ReflectionAPI.pickMethod(
                    playerConnection.getClass(),
                    new MethodSignature("sendPacket", getPacketClass()),
                    new MethodSignature("a", getPacketClass())
            ).invoke(playerConnection, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
