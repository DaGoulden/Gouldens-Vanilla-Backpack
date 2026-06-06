package net.goulden.gouldensvanillabackpack.client.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.registry.BPAttachments;
import net.goulden.gouldensvanillabackpack.registry.BPDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID, value = Dist.CLIENT)
public class BackpackGuiEvents {

    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/gui/backpack_slot_base.png");
    private static final ResourceLocation LID_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/gui/backpack_slot_lid.png");
    private static final ResourceLocation REINFORCED_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/gui/backpack_slot_reinforced.png");
    private static final ResourceLocation LOCKED_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/gui/backpack_slot_locked.png");

    /*private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/item/backpack_base.png");
    private static final ResourceLocation LID_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/item/backpack_lid.png");
    private static final ResourceLocation REINFORCED_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/item/backpack_reinforced.png");
    private static final ResourceLocation LOCKED_TEXTURE = ResourceLocation.fromNamespaceAndPath(GouldensVanillaBackpack.MODID,
            "textures/item/backpack_locked.png");*/

    @SubscribeEvent
    public static void onRenderGui(ContainerScreenEvent.Render.Background event) {
        if (!(event.getContainerScreen() instanceof InventoryScreen screen)) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack equipped = Minecraft.getInstance().player.getData(BPAttachments.BACKPACK_SLOT);
        if (equipped.isEmpty()) return;

        Slot chestSlot = screen.getMenu().slots.stream()
                .filter(s -> s.container instanceof Inventory && s.getSlotIndex() == 38)
                .findFirst().orElse(null);

        if (chestSlot == null) return;

        int x = screen.getGuiLeft() + chestSlot.x + 18;
        int y = screen.getGuiTop() + chestSlot.y;

        Integer baseColor = equipped.get(BPDataComponents.BASE_COLOR.get());
        Integer lidColor = equipped.get(BPDataComponents.LID_COLOR.get());

        GuiGraphics graphics = event.getGuiGraphics();

        if (baseColor != null) {
            graphics.setColor(
                    FastColor.ARGB32.red(baseColor) / 255f,
                    FastColor.ARGB32.green(baseColor) / 255f,
                    FastColor.ARGB32.blue(baseColor) / 255f,
                    1.0f
            );
            graphics.blit(BASE_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f); // resetear color
        }

        if (lidColor != null) {
            graphics.setColor(
                    FastColor.ARGB32.red(lidColor) / 255f,
                    FastColor.ARGB32.green(lidColor) / 255f,
                    FastColor.ARGB32.blue(lidColor) / 255f,
                    1.0f
            );
            graphics.blit(LID_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f); // resetear color
        }

        Boolean reinforced = equipped.get(BPDataComponents.REINFORCED.get());
        if (reinforced != null && reinforced) {
            graphics.blit(REINFORCED_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        }

        Boolean locked = equipped.get(BPDataComponents.LOCKED.get());
        if (locked != null && locked) {
            graphics.blit(LOCKED_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
        }
    }
}