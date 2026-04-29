package net.goulden.gouldensvanillabackpack;

import net.goulden.gouldensvanillabackpack.networking.*;
import net.goulden.gouldensvanillabackpack.registry.*;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(GouldensVanillaBackpack.MODID)
public class GouldensVanillaBackpack {
    public static final String MODID = "gouldensvanillabackpack";

    public GouldensVanillaBackpack(IEventBus modEventBus) {
        modEventBus.register(GouldensVanillaBackpack.class);

        BPBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        BPBlocks.BLOCKS.register(modEventBus);
        BPDataAttachments.ATTACHMENT_TYPES.register(modEventBus);
        BPDataAttachments.COMPONENTS.register(modEventBus);
        BPItems.ITEMS.register(modEventBus);
        BPRecipes.SERIALIZERS.register(modEventBus);
        BPSounds.SOUND_EVENTS.register(modEventBus);
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                BackpackEquipPayload.TYPE,
                BackpackEquipPayload.STREAM_CODEC,
                BackpackPayloadHandler::HandleEquip
        );
        registrar.playToClient(
                BackpackOpenPayload.TYPE,
                BackpackOpenPayload.STREAM_CODEC,
                BackpackPayloadHandler::HandleClientData
        );
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            return switch (tintIndex) {
                case 0 -> {
                    Integer color = stack.get(BPDataAttachments.BASE_COLOR.get());
                    yield color != null ? FastColor.ARGB32.opaque(color) : -1;
                }
                case 1 -> {
                    Integer color = stack.get(BPDataAttachments.LID_COLOR.get());
                    yield color != null ? FastColor.ARGB32.opaque(color) : -1;
                }
                default -> -1;
            };
        }, BPItems.BACKPACK.value());
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(BPItems.BACKPACK);
    }
}
