package net.goulden.gouldensvanillabackpack.common.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.networking.BackpackOpenPayload;
import net.goulden.gouldensvanillabackpack.registry.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.goulden.gouldensvanillabackpack.registry.BPAttachments.IS_OPEN;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID)
public class EntityInteractionEvents {

    public static final Map<UUID, UUID> openingBackpack = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        ItemStack item = target.getData(BPAttachments.BACKPACK_SLOT);
        if (!item.is(BPItems.BACKPACK)) return;
        if (Boolean.TRUE.equals(item.get(BPDataComponents.LOCKED.get()))) return;
        if (!isBehind(player, target) || player.distanceTo(target) > 3.0F) return;

        if (!player.level().isClientSide()) {

            openingBackpack.put(player.getUUID(), target.getUUID());
            if (target instanceof ServerPlayer serverTarget) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new BackpackOpenPayload(true, target.getId()));
                target.level().playSound(null, target.blockPosition(), BPSounds.BACKPACK_OPEN.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            int slots = BPEnchantments.getSlotCount(item, player.level().registryAccess());
            SimpleContainer container = new SimpleContainer(slots);
            if (item.has(DataComponents.CONTAINER)) {
                NonNullList<ItemStack> list = NonNullList.withSize(slots, ItemStack.EMPTY);
                item.get(DataComponents.CONTAINER).copyInto(list);
                for (int i = 0; i < slots; i++) container.setItem(i, list.get(i));
            }

            SimpleContainer syncedContainer = new SimpleContainer(slots) {
                {
                    for (int i = 0; i < container.getContainerSize(); i++) setItem(i, container.getItem(i));
                }
                @Override
                public void setChanged() {
                    super.setChanged();
                    NonNullList<ItemStack> list = NonNullList.withSize(slots, ItemStack.EMPTY);
                    for (int i = 0; i < slots; i++) list.set(i, getItem(i));
                    item.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
                }
            };

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> {
                        int rows = slots / 9;
                        MenuType<?> type = switch (rows) {
                            case 4 -> MenuType.GENERIC_9x4;
                            case 5 -> MenuType.GENERIC_9x5;
                            case 6 -> MenuType.GENERIC_9x6;
                            default -> MenuType.GENERIC_9x3;
                        };
                        return new ChestMenu(type, id, inv, syncedContainer, rows);
                    },
                    Component.translatable("container.backpack")
            ));
        }

        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (!openingBackpack.containsKey(player.getUUID())) {
            boolean shouldBeOpen = openingBackpack.containsValue(player.getUUID());
            boolean isOpen = player.getData(IS_OPEN);
            if (shouldBeOpen != isOpen) {
                player.setData(IS_OPEN, shouldBeOpen);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new BackpackOpenPayload(shouldBeOpen, player.getId()));
                if (!shouldBeOpen) {
                    player.level().playSound(null, player.blockPosition(), BPSounds.BACKPACK_CLOSE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            return;
        }

        Player target = player.level().getPlayerByUUID(openingBackpack.get(player.getUUID()));
        if (!(player.containerMenu instanceof ChestMenu)) {
            openingBackpack.remove(player.getUUID());
            player.level().playSound(null, target.blockPosition(), BPSounds.BACKPACK_CLOSE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        if (target == null || !isBehind(player, target) || player.distanceTo(target) > 3.0F) {
            player.closeContainer();
        }
    }

    public static boolean isBehind(Player player, LivingEntity target) {
        Vec3 forwardTarget = Vec3.directionFromRotation(0, target.yBodyRot).normalize();
        Vec3 toPlayer = player.position().subtract(target.position());
        toPlayer = new Vec3(toPlayer.x, 0, toPlayer.z).normalize();
        double dot = forwardTarget.dot(toPlayer);
        return dot < -0.5D;
    }
}