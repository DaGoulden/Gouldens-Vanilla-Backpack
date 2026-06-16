package net.goulden.gouldensvanillabackpack.mixins;

import net.goulden.gouldensvanillabackpack.registry.BPAttachments;
import net.goulden.gouldensvanillabackpack.registry.BPEnchantments;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onSave(CompoundTag tag, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        ItemStack backpack = player.getData(BPAttachments.BACKPACK_SLOT);
        int slots = BPEnchantments.getSlotCount(backpack, player.registryAccess());
        if (backpack.isEmpty()) return;

        ItemStack toSave = backpack.copy();
        toSave.remove(DataComponents.CONTAINER);
        tag.put("BackpackItem", toSave.save(player.registryAccess()));

        ItemContainerContents contents = backpack.get(DataComponents.CONTAINER);
        if (contents == null) return;

        NonNullList<ItemStack> list = NonNullList.withSize(slots, ItemStack.EMPTY);
        contents.copyInto(list);
        ListTag listTag = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                Tag savedItem = list.get(i).save(player.registryAccess());
                itemTag.put("Item", savedItem);
                listTag.add(itemTag);
            }
        }
        tag.put("BackpackContents", listTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onLoad(CompoundTag tag, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (!tag.contains("BackpackItem")) return;

        ItemStack backpack = ItemStack.parse(player.registryAccess(), tag.getCompound("BackpackItem")).orElse(ItemStack.EMPTY);
        int slots = BPEnchantments.getSlotCount(backpack, player.registryAccess());
        if (backpack.isEmpty()) return;

        if (tag.contains("BackpackContents")) {
            NonNullList<ItemStack> list = NonNullList.withSize(slots, ItemStack.EMPTY);
            ListTag listTag = tag.getList("BackpackContents", 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < slots && itemTag.contains("Item")) {
                    ItemStack.parse(player.registryAccess(), itemTag.getCompound("Item"))
                            .ifPresent(s -> list.set(slot, s));
                }
            }
            backpack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
        }

        player.setData(BPAttachments.BACKPACK_SLOT, backpack);
    }
}