package net.ollycodes.modlogger.mixins;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.ollycodes.modlogger.ModLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginHandlerMixin {
    @Shadow
    @Final
    public Connection connection;

    @Inject(at = { @At("HEAD") }, method = { "handleAcceptedLogin" })
    public void handleAcceptedLogin(CallbackInfo info) {
        ModLogger.profiles.put(this.connection.getRemoteAddress(), this.connection);
    }
}
