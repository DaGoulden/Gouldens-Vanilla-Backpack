package net.goulden.gouldensvanillabackpack.networking;

import net.goulden.gouldensvanillabackpack.common.entity.BackpackWearer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.goulden.gouldensvanillabackpack.registry.BPAttachments.BACKPACK_SLOT;

public class BackpackPayloadHandler {

    public static void HandleEquip(final BackpackEquipPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity instanceof Player player) {
                player.setData(BACKPACK_SLOT, payload.stack());
            }
        });
    }

    public static void HandleClientData(final BackpackOpenPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.id());
            if (entity instanceof BackpackWearer backpackWearer) {
                if (payload.isOpen()) {
                    backpackWearer.onBackpackOpen();
                } else {
                    backpackWearer.onBackpackClose();
                }
            }
        });
    }
}
