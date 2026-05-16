package net.goulden.gouldensvanillabackpack.mixins;

import net.goulden.gouldensvanillabackpack.networking.BackpackEquipPayload;
import net.goulden.gouldensvanillabackpack.registry.BPAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Inject(method = "clearOrCountMatchingItems", at = @At("HEAD"))
    private void onClearOrCount(Predicate<ItemStack> stackPredicate, int maxCount, Container inventory, CallbackInfoReturnable<Integer> cir) {
        Inventory inv = (Inventory)(Object)this;
        Player player = inv.player;
        ItemStack equipped = player.getData(BPAttachments.BACKPACK_SLOT);
        if (equipped.isEmpty()) return;

        if (maxCount != 0 && stackPredicate.test(equipped)) {
            player.setData(BPAttachments.BACKPACK_SLOT, ItemStack.EMPTY);
            if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new BackpackEquipPayload(player.getId(), ItemStack.EMPTY));
            }
        }
    }
}