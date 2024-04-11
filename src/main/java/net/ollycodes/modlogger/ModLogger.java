package net.ollycodes.modlogger;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ConnectionData;
import org.slf4j.Logger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.List;

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

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        MLCommand.register(event.getDispatcher());
        logger.info("Registered commands");
    }

    public static void handleConnection(ConnectionData connection, GameProfile profile, CallbackInfoReturnable<Component> info) throws IOException {
        PlayerRecord.PlayerConnection playerConnection = new PlayerRecord.PlayerConnection();
        playerConnection.username = profile.getName();
        playerConnection.mods = connection.getModList();
        playerConnection.timestamp = new Date();
        fileHandler.logConnection(profile.getId().toString(), playerConnection);

        List<String> bannedMods = new ArrayList<>();
        List<String> addedMods = new ArrayList<>();
        List<String> defaultMods = new ArrayList<>();

        connection.getModData().forEach((mod, data) -> {
            if (checkModList(fileHandler.config.bannedMods, mod)) {
                bannedMods.add(mod);
            } else if (checkModList(fileHandler.config.defaultMods, mod)) {
                defaultMods.add(mod);
            } else {
                addedMods.add(mod);
            }
        });

        if (!bannedMods.isEmpty()) {
            if (fileHandler.config.kick.onBanned) {
                if (fileHandler.config.kick.showBannedMods) {
                    info.setReturnValue(
                        new TextComponent(fileHandler.config.kick.bannedMessageWithMods.replace(
                            "%s", webhook.formatModsList(bannedMods, false)
                        ))
                    );
                } else {
                    info.setReturnValue(new TextComponent(fileHandler.config.kick.bannedMessage));
                }
            }
            if (fileHandler.config.webhook.onBanned) {
                webhook.sendBannedMessage(
                    profile.getId().toString(), profile.getName(), playerConnection.timestamp,
                    defaultMods, addedMods, bannedMods
                );
            }

        } else if (!addedMods.isEmpty()) {
            if (fileHandler.config.kick.onAdded) {
                if (fileHandler.config.kick.showAddedMods) {
                    info.setReturnValue(
                            new TextComponent(fileHandler.config.kick.addedMessageWithMods.replace(
                                "%s", webhook.formatModsList(addedMods, false)
                            ))
                    );
                } else {
                    info.setReturnValue(new TextComponent(fileHandler.config.kick.addedMessage));
                }
            }
            if (fileHandler.config.webhook.onAdded) {
                webhook.sendAddedMessage(
                    profile.getId().toString(), profile.getName(), playerConnection.timestamp, defaultMods, addedMods
                );
            }

        } else if (fileHandler.config.webhook.onDefault) {
            webhook.sendDefaultMessage(
                profile.getId().toString(), profile.getName(), playerConnection.timestamp, defaultMods
            );
        }

        ModLogger.logger.debug("Handled connection for {}", profile.getName());
    }

    public static boolean checkModList(List<String> modList, String mod) {
        if (fileHandler.config.matchExactModName) return modList.contains(mod);
        for (String modName : modList) {
            if (mod.contains(modName)) return true;
        }
        return false;
    }
}
