package net.goulden.gouldensvanillabackpack.common.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.networking.BackpackEquipPayload;
import net.goulden.gouldensvanillabackpack.registry.BPAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID)
public class BackpackRenderingEvents {

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player targetPlayer) {
            ItemStack equipped = targetPlayer.getData(BPAttachments.BACKPACK_SLOT);
            if (!equipped.isEmpty()) {
                PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new BackpackEquipPayload(targetPlayer.getId(), equipped));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack equipped = player.getData(BPAttachments.BACKPACK_SLOT);
            if (!equipped.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new BackpackEquipPayload(player.getId(), equipped));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack equipped = player.getData(BPAttachments.BACKPACK_SLOT);
            if (!equipped.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new BackpackEquipPayload(player.getId(), equipped));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.isEndConquered()) return;
            ItemStack equipped = player.getData(BPAttachments.BACKPACK_SLOT);
            if (!equipped.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new BackpackEquipPayload(player.getId(), equipped));
            }
        }
    }
}
