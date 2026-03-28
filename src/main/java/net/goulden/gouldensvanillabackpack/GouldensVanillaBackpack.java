package net.goulden.gouldensvanillabackpack;

import net.goulden.gouldensvanillabackpack.networking.BackpackOpenPayload;
import net.goulden.gouldensvanillabackpack.networking.BackpackPayloadHandler;
import net.goulden.gouldensvanillabackpack.registry.*;
import net.goulden.gouldensvanillabackpack.registry.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(GouldensVanillaBackpack.MODID)
public class GouldensVanillaBackpack {
    public static final String MODID = "gouldensvanillabackpack";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GouldensVanillaBackpack(IEventBus modEventBus) {
        modEventBus.register(GouldensVanillaBackpack.class);

        BPDataAttatchments.ATTACHMENT_TYPES.register(modEventBus);
        BPBlocks.BLOCKS.register(modEventBus);
        BPItems.ITEMS.register(modEventBus);
        BPBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        BPSounds.SOUND_EVENTS.register(modEventBus);
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                BackpackOpenPayload.TYPE,
                BackpackOpenPayload.STREAM_CODEC,
                BackpackPayloadHandler::HandleClientData
        );
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? -1 :DyedItemColor.getOrDefault(stack, -1),
                BPItems.BACKPACK.value());
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) event.accept(BPItems.BACKPACK);
    }
}
