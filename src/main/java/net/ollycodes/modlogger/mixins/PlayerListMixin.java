package net.ollycodes.modlogger.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;
import net.ollycodes.modlogger.ModLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.SocketAddress;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(at = { @At("HEAD") }, method = { "canPlayerLogin" }, cancellable = true)
    public void canPlayerLogin(SocketAddress socket, GameProfile profile, CallbackInfoReturnable<Component> info) {
        Connection con = ModLogger.profiles.get(socket);
        ConnectionData connection = NetworkHooks.getConnectionData(con);
        if (connection == null) return;
        try {
            ModLogger.handleConnection(connection, profile, info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
