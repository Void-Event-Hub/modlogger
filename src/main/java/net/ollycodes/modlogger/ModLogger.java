package net.ollycodes.modlogger;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.ProfileBanEntry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Mod("modlogger")
public class ModLogger {
    public static final Logger logger = LogManager.getLogger();
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
    public void serverStarting(FMLServerStartingEvent event) {
        server = event.getServer();
        logger.info("Registered server: {}", server);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        MLCommand.register(event.getDispatcher());
        logger.info("Registered commands");
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) throws IOException {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ImmutableList<String> mods = Objects.requireNonNull(
            NetworkHooks.getConnectionData(player.connection.getConnection())
        ).getModList();

        PlayerRecord.PlayerConnection playerConnection = new PlayerRecord.PlayerConnection();
        playerConnection.username = player.getName().getString();
        playerConnection.mods = mods;
        playerConnection.timestamp = new Date();
        fileHandler.logConnection(player.getUUID().toString(), playerConnection);

        List<String> requiredMods = new ArrayList<>(fileHandler.config.requiredMods);
        List<String> bannedMods = new ArrayList<>();
        List<String> addedMods = new ArrayList<>();
        List<String> defaultMods = new ArrayList<>();
        List<String> ignoredMods = new ArrayList<>();
        boolean playerWhitelisted = (
                fileHandler.config.playerWhitelist.contains(player.getName().getString()) || (
                    fileHandler.config.bypassKickPermissionLevel != -1
                    && server.getProfilePermissions(player.getGameProfile()) >= fileHandler.config.bypassKickPermissionLevel
                )
        );

        mods.forEach(mod -> {
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
                    player.connection.disconnect(
                        new StringTextComponent(fileHandler.config.kick.requiredMessageWithMods.replace(
                            "%s", webhook.formatModsList(requiredMods, false)
                        ))
                    );
                } else {
                    player.connection.disconnect(new StringTextComponent(fileHandler.config.kick.requiredMessage));
                }
            }
            if (fileHandler.config.webhook.onRequired) {
                webhook.sendRequiredMessage(
                    player.getStringUUID(), player.getName().getString(), playerConnection.timestamp,
                    playerWhitelisted, defaultMods, ignoredMods, requiredMods
                );
            }

        } else if (!bannedMods.isEmpty()) {
            if (!playerWhitelisted && fileHandler.config.ban.onBanned) {
                    BanList banList = server.getPlayerList().getBans();
                    String banMessage = fileHandler.config.ban.banMessage.replace(
                        "%s", webhook.formatModsList(bannedMods, false)
                    );
                    ProfileBanEntry banEntry = new ProfileBanEntry(
                        player.getGameProfile(), new Date(), "ModLogger", null, banMessage
                    );
                    banList.add(banEntry);
                    player.connection.disconnect(new StringTextComponent(banMessage));
            } else if (!playerWhitelisted && fileHandler.config.kick.onBanned) {
                if (fileHandler.config.kick.showBannedMods) {
                    player.connection.disconnect(
                        new StringTextComponent(fileHandler.config.kick.bannedMessageWithMods.replace(
                            "%s", webhook.formatModsList(bannedMods, false)
                        ))
                    );
                } else {
                    player.connection.disconnect(new StringTextComponent(fileHandler.config.kick.bannedMessage));
                }
            }
            if (fileHandler.config.webhook.onBanned) {
                webhook.sendBannedMessage(
                    player.getStringUUID(), player.getName().getString(), playerConnection.timestamp,
                    playerWhitelisted, defaultMods, ignoredMods, addedMods, bannedMods
                );
            }

        } else if (!addedMods.isEmpty()) {
            if (fileHandler.config.kick.onAdded && !playerWhitelisted) {
                if (fileHandler.config.kick.showAddedMods) {
                    player.connection.disconnect(
                        new StringTextComponent(fileHandler.config.kick.addedMessageWithMods.replace(
                            "%s", webhook.formatModsList(addedMods, false)
                        ))
                    );
                } else {
                    player.connection.disconnect(new StringTextComponent(fileHandler.config.kick.addedMessage));
                }
            }
            if (fileHandler.config.webhook.onAdded) {
                webhook.sendAddedMessage(
                    player.getStringUUID(), player.getName().getString(), playerConnection.timestamp,
                    playerWhitelisted, defaultMods, ignoredMods, addedMods
                );
            }

        } else if (fileHandler.config.webhook.onDefault) {
            webhook.sendDefaultMessage(
                player.getStringUUID(), player.getName().getString(), playerConnection.timestamp,
                defaultMods, ignoredMods
            );
        }

        ModLogger.logger.debug("Handled connection for {}", player.getName().getString());
    }

    public static boolean checkModList(List<String> modList, String mod) {
        if (fileHandler.config.matchExactModName) return modList.contains(mod);
        for (String modName : modList) {
            if (mod.contains(modName)) return true;
        }
        return false;
    }
}
