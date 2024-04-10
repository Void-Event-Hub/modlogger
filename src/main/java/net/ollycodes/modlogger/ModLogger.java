package net.ollycodes.modlogger;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ConnectionData;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;

@Mod("modlogger")
public class ModLogger {
    public static final Logger logger = LogUtils.getLogger();
    public static Map<SocketAddress, Connection> profiles = new HashMap<>();
    public static FileHandler fileHandler = new FileHandler();
    public static Webhook webhook = new Webhook();

    public ModLogger() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        logger.info("ModLogger Enabled");
    }

    public static void handleConnection(ConnectionData connection, GameProfile profile) throws IOException {
        PlayerRecord.PlayerConnection playerConnection = new PlayerRecord.PlayerConnection();
        playerConnection.username = profile.getName();
        playerConnection.mods = connection.getModList();
        playerConnection.timestamp = new Date();
        fileHandler.logConnection(profile.getId().toString(), playerConnection);

        List<String> bannedMods = new ArrayList<>();
        List<String> addedMods = new ArrayList<>();
        List<String> defaultMods = new ArrayList<>();

        connection.getModData().forEach((mod, data) -> {
            if (fileHandler.config.bannedMods.contains(mod)) {
                bannedMods.add(mod);
            } else if (fileHandler.config.defaultMods.contains(mod)) {
                defaultMods.add(mod);
            } else {
                addedMods.add(mod);
            }
        });

        if (!bannedMods.isEmpty() && fileHandler.config.webhookOnBanned) {
            webhook.sendBannedMessage(
                profile.getId().toString(), profile.getName(), playerConnection.timestamp,
                defaultMods, addedMods, bannedMods
            );
        } else if (!addedMods.isEmpty() && fileHandler.config.webhookOnAdded) {
            webhook.sendAddedMessage(
                profile.getId().toString(), profile.getName(), playerConnection.timestamp, defaultMods, addedMods
            );
        } else if (fileHandler.config.webhookOnDefault) {
            webhook.sendDefaultMessage(
                profile.getId().toString(), profile.getName(), playerConnection.timestamp, defaultMods
            );
        }
    }
}
