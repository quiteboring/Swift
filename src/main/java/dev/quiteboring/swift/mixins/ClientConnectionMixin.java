package dev.quiteboring.swift.mixins;

import dev.quiteboring.swift.event.PacketEvent;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

  @Shadow
  private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
  }

  @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
  private static void onPacketReceived(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
    if (packet instanceof BundleS2CPacket bundlePacket) {
      ci.cancel();

      for (Packet<?> subPacket : bundlePacket.getPackets()) {
        handlePacket(subPacket, listener);
      }

      return;
    }

    PacketEvent.RECEIVE.invoker().trigger(packet);
  }

}
