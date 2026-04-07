package net.goulden.gouldensvanillabackpack.common.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.goulden.gouldensvanillabackpack.networking.BackpackEquipPayload;
import net.goulden.gouldensvanillabackpack.registry.BPBlocks;
import net.goulden.gouldensvanillabackpack.registry.BPDataAttachments;
import net.goulden.gouldensvanillabackpack.registry.BPItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID)
public class BackpackOnDeathDropEvents {

    // Al morir: limpiar attachment y dropear como ítem
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack equipped = player.getData(BPDataAttachments.BACKPACK_SLOT);
        if (equipped.isEmpty()) return;

        player.setData(BPDataAttachments.BACKPACK_SLOT, ItemStack.EMPTY);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new BackpackEquipPayload(player.getId(), ItemStack.EMPTY));

        player.drop(equipped, false);
    }

    // Cada tick: si el ítem de mochila toca el suelo o flota en un fluido → convertir en bloque
    @SubscribeEvent
    public static void onItemEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        ItemStack stack = itemEntity.getItem();
        if (!stack.is(BPItems.BACKPACK)) return;

        boolean hasContainer = stack.has(DataComponents.CONTAINER);
        boolean isEmpty = Objects.equals(stack.get(DataComponents.CONTAINER), ItemContainerContents.EMPTY);
        if (!hasContainer || isEmpty) return;

        Level level = itemEntity.level();
        if (level.isClientSide) return;

        boolean inFluid = !level.getFluidState(itemEntity.blockPosition()).isEmpty();
        boolean onFluidSurface = inFluid && level.getFluidState(itemEntity.blockPosition().above()).isEmpty();
        boolean onGround = itemEntity.onGround() && !inFluid;

        // Flotar más rápido en cualquier fluido
        if (inFluid) {
            Vec3 velocity = itemEntity.getDeltaMovement();
            itemEntity.setDeltaMovement(velocity.x, velocity.y + 0.01, velocity.z);
        }

        if (!onGround && !onFluidSurface) return;

        BlockPos placePos = inFluid ? itemEntity.blockPosition().above() : itemEntity.blockPosition();
        if (!level.getBlockState(placePos).canBeReplaced()) return;

        BlockState state = BPBlocks.BACKPACK.get().defaultBlockState()
                .setValue(BackpackBlock.FACING, Direction.NORTH)
                .setValue(BackpackBlock.FLOATING, inFluid);

        BackpackBlockEntity blockEntity = new BackpackBlockEntity(placePos, state);
        blockEntity.applyComponentsFromItemStack(stack);
        level.setBlockAndUpdate(placePos, state);
        level.setBlockEntity(blockEntity);

        itemEntity.discard();
    }

    // Auto-equipar al recoger
    @SubscribeEvent
    public static void onItemEntityPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack stack = event.getItemEntity().getItem();
        if (!stack.is(BPItems.BACKPACK)) return;

        boolean hasContainer = stack.has(DataComponents.CONTAINER);
        boolean isEmpty = Objects.equals(stack.get(DataComponents.CONTAINER), ItemContainerContents.EMPTY);

        // Sin contenido → agarrar normalmente
        if (!hasContainer || isEmpty) return;

        Player player = event.getPlayer();
        ItemStack equipped = player.getData(BPDataAttachments.BACKPACK_SLOT);
        if (!equipped.isEmpty()) return;

        if (!event.getItemEntity().hasPickUpDelay() && !player.level().isClientSide()) {
            player.setData(BPDataAttachments.BACKPACK_SLOT, stack.copy());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new BackpackEquipPayload(player.getId(), stack.copy()));
            player.take(event.getItemEntity(), 1);
            event.getItemEntity().discard();
            player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), 1);
            player.onItemPickup(event.getItemEntity());
        }

        event.setCanPickup(TriState.FALSE);
    }
}
