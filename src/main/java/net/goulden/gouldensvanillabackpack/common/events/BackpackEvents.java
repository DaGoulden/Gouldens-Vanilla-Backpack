package net.goulden.gouldensvanillabackpack.common.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.goulden.gouldensvanillabackpack.networking.BackpackEquipPayload;
import net.goulden.gouldensvanillabackpack.registry.BPBlocks;
import net.goulden.gouldensvanillabackpack.registry.BPEnchantments;
import net.goulden.gouldensvanillabackpack.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock.*;
import static net.goulden.gouldensvanillabackpack.registry.BPAttachments.BACKPACK_SLOT;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID)
public class BackpackEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock (PlayerInteractEvent.RightClickBlock event) {

        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        if (!player.isCrouching()) return;
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        ItemStack equippedBackpackSlot = player.getData(BACKPACK_SLOT);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        boolean hasBackpack = !equippedBackpackSlot.isEmpty();

        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {

            // PICKUP BACKPACK
            if (blockEntity instanceof BackpackBlockEntity && !hasBackpack) {

                if (!level.isClientSide) {
                    ItemStack itemStack = new ItemStack(BPBlocks.BACKPACK);
                    itemStack.applyComponents(blockEntity.collectComponents());
                    player.setData(BACKPACK_SLOT, itemStack);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new BackpackEquipPayload(player.getId(), itemStack));
                    level.removeBlockEntity(blockPos);
                    level.removeBlock(blockPos, false);
                    level.playSound(null, blockPos.above(), BPSounds.BACKPACK_EQUIP.value(), SoundSource.BLOCKS);
                    addParticles(level, blockPos, blockEntity);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                event.setCanceled(true);
            }

            // PLACE BACKPACK
            if (event.getHitVec().getDirection() == Direction.UP
                    || (level.getBlockState(blockPos).canBeReplaced() && !level.getBlockState(blockPos).is(BlockTags.CLIMBABLE))) {

                while (level.getBlockState(blockPos).canBeReplaced()) {
                    blockPos = blockPos.below();
                }
                BlockPos placePos = blockPos.above();
                if (hasBackpack && level.getBlockState(placePos).canBeReplaced()
                        && level.isUnobstructed(BPBlocks.BACKPACK.get().defaultBlockState(), placePos, CollisionContext.of(player))) {

                    if (!level.isClientSide) {
                        BlockState state = BPBlocks.BACKPACK.get().defaultBlockState()
                                .setValue(FACING, player.getDirection())
                                .setValue(WATERLOGGED, level.getFluidState(placePos).getType() == Fluids.WATER);
                        blockEntity = new BackpackBlockEntity(placePos, state);
                        int slotCount = BPEnchantments.getSlotCount(equippedBackpackSlot, level.registryAccess());
                        ((BackpackBlockEntity) blockEntity).setSlotCount(slotCount);
                        blockEntity.applyComponentsFromItemStack(equippedBackpackSlot);
                        player.setData(BACKPACK_SLOT, ItemStack.EMPTY);
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new BackpackEquipPayload(player.getId(), ItemStack.EMPTY));
                        level.setBlockAndUpdate(placePos, state);
                        level.setBlockEntity(blockEntity);
                        level.playSound(null, placePos, BPSounds.BACKPACK_PLACE.value(), SoundSource.BLOCKS);
                    } else {
                        player.swing(InteractionHand.MAIN_HAND);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void addParticles(Level level, BlockPos pos, BlockEntity blockEntity) {
        ServerLevel serverLevel = (ServerLevel) level;
        boolean floating = blockEntity.getBlockState().getValue(FLOATING);
        Direction facing = blockEntity.getBlockState().getValue(FACING);
        boolean facingZAxis = facing == Direction.NORTH || facing == Direction.SOUTH;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    serverLevel.sendParticles(
                            new DustParticleOptions(
                                    Vec3.fromRGB24(0xCCCCCC).toVector3f(),
                                    0.6F
                            ),
                            pos.getX() + 0.5 + ((facingZAxis ? 0.25 : 0.1875) * i),
                            pos.getY() + (floating ? 0.15625 : 0.34375) + (0.34375 * j),
                            pos.getZ() + 0.5 + ((facingZAxis ? 0.1875 : 0.25) * k),
                            1,
                            0, 0, 0,
                            0
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide) return;
        event.getAffectedBlocks().removeIf(pos -> {
            BlockEntity be = event.getLevel().getBlockEntity(pos);
            return be instanceof BackpackBlockEntity backpackBE && backpackBE.isReinforced();
        });
    }
}
