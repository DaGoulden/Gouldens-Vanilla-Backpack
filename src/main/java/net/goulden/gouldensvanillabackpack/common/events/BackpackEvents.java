package net.goulden.gouldensvanillabackpack.common.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.goulden.gouldensvanillabackpack.networking.BackpackEquipPayload;
import net.goulden.gouldensvanillabackpack.registry.BPBlocks;
import net.goulden.gouldensvanillabackpack.registry.BPDataAttachments;
import net.goulden.gouldensvanillabackpack.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock.FACING;
import static net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock.WATERLOGGED;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID)
public class BackpackEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock (PlayerInteractEvent.RightClickBlock event) {

        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        if (!player.isCrouching()) return;
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return;
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack equippedBackpackSlot = player.getData(BPDataAttachments.BACKPACK_SLOT);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        boolean hasBackpack = !equippedBackpackSlot.isEmpty();

        // PICKUP BACKPACK
        if (blockState.is(BPBlocks.BACKPACK) && !hasBackpack) {

            if (!level.isClientSide) {
                ItemStack itemStack = new ItemStack(BPBlocks.BACKPACK);
                itemStack.applyComponents(blockEntity != null ? blockEntity.collectComponents() : null);
                player.setData(BPDataAttachments.BACKPACK_SLOT, itemStack);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new BackpackEquipPayload(player.getId(), itemStack));
                level.removeBlockEntity(blockPos);
                level.removeBlock(blockPos, false);
                level.playSound(null, blockPos.above(), BPSounds.BACKPACK_EQUIP.value(), SoundSource.BLOCKS);
                addParticles(level, blockPos);
            } else {
                player.swing(InteractionHand.MAIN_HAND);
            }

            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }

        // PLACE BACKPACK
        if (event.getHitVec().getDirection() == Direction.UP || level.getBlockState(blockPos).canBeReplaced()) {

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
                    blockEntity.applyComponentsFromItemStack(equippedBackpackSlot);
                    player.setData(BPDataAttachments.BACKPACK_SLOT, ItemStack.EMPTY);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new BackpackEquipPayload(player.getId(), ItemStack.EMPTY));
                    level.setBlockAndUpdate(placePos, state);
                    level.setBlockEntity(blockEntity);
                    level.playSound(null, placePos, BPSounds.BACKPACK_PLACE.value(), SoundSource.BLOCKS);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }

                event.setCancellationResult(InteractionResult.FAIL);
                event.setCanceled(true);
            }
        }
    }

    private static void addParticles(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        for (int i = 0; i < 4; i++) {
            serverLevel.sendParticles(
                    ParticleTypes.DUST_PLUME,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }
}
