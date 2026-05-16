package net.goulden.gouldensvanillabackpack.registry;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.items.BackpackItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BPItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GouldensVanillaBackpack.MODID);

    public static final DeferredItem<BackpackItem> BACKPACK = ITEMS.register("backpack",
            () -> new BackpackItem(BPBlocks.BACKPACK.get(), new Item.Properties()
                    .stacksTo(1)
                    .component(BPDataComponents.BASE_COLOR.get(), DyeColor.BROWN.getTextureDiffuseColor())
                    .component(BPDataComponents.LID_COLOR.get(), DyeColor.BROWN.getTextureDiffuseColor())
            ));
}
