package net.ollycodes.modlogger;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
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
    public static MinecraftServer server;

    public ModLogger() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        logger.info("ModLogger Enabled");
    }

    @SubscribeEvent
    public void serverStarting(ServerStartingEvent event) {
        server = event.getServer();
        logger.info("Registered server: {}", server);
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

        List<String> requiredMods = new ArrayList<>(fileHandler.config.requiredMods);
        List<String> bannedMods = new ArrayList<>();
        List<String> addedMods = new ArrayList<>();
        List<String> defaultMods = new ArrayList<>();
        List<String> ignoredMods = new ArrayList<>();
        boolean playerWhitelisted = (
            fileHandler.config.playerWhitelist.contains(profile.getName()) || (
                fileHandler.config.bypassKickPermissionLevel != -1
                && server.getProfilePermissions(profile) >= fileHandler.config.bypassKickPermissionLevel
            )
        );

        connection.getModData().forEach((mod, data) -> {
            if (requiredMods.contains(mod)) {
                requiredMods.remove(mod);
            } else if (checkModList(fileHandler.config.bannedMods, mod)) {
                bannedMods.add(mod);
            } else if (checkModList(fileHandler.config.defaultMods, mod)) {
                defaultMods.add(mod);
            } else if (checkModList(fileHandler.config.ignoredMods, mod)) {
                ignoredMods.add(mod);
            } else {
                addedMods.add(mod);
            }
        });

        if (!requiredMods.isEmpty()) {
            if (fileHandler.config.kick.onRequired && !playerWhitelisted) {
                if (fileHandler.config.kick.showRequiredMods) {
                    info.setReturnValue(
                            new TextComponent(fileHandler.config.kick.requiredMessageWithMods.replace(
                                    "%s", webhook.formatModsList(requiredMods, false)
                            ))
                    );
                } else {
                    info.setReturnValue(new TextComponent(fileHandler.config.kick.requiredMessage));
                }
            }
            if (fileHandler.config.webhook.onRequired) {
                webhook.sendRequiredMessage(
                        profile.getId().toString(), profile.getName(), playerConnection.timestamp,
                        playerWhitelisted, defaultMods, ignoredMods, requiredMods
                );
            }

        } else if (!bannedMods.isEmpty()) {
            if (!playerWhitelisted && fileHandler.config.ban.onBanned) {
                    UserBanList banList = server.getPlayerList().getBans();
                    String banMessage = fileHandler.config.ban.banMessage.replace(
                        "%s", webhook.formatModsList(bannedMods, false)
                    );
                    UserBanListEntry banEntry = new UserBanListEntry(
                        profile, new Date(), "ModLogger", null, banMessage
                    );
                    banList.add(banEntry);
                    info.setReturnValue(new TextComponent(banMessage));
            } else if (!playerWhitelisted && fileHandler.config.kick.onBanned) {
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
                    playerWhitelisted, defaultMods, ignoredMods, addedMods, bannedMods
                );
            }

        } else if (!addedMods.isEmpty()) {
            if (fileHandler.config.kick.onAdded && !playerWhitelisted) {
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
                    profile.getId().toString(), profile.getName(), playerConnection.timestamp,
                    playerWhitelisted, defaultMods, ignoredMods, addedMods
                );
            }

        } else if (fileHandler.config.webhook.onDefault) {
            webhook.sendDefaultMessage(
                profile.getId().toString(), profile.getName(), playerConnection.timestamp, defaultMods, ignoredMods
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
