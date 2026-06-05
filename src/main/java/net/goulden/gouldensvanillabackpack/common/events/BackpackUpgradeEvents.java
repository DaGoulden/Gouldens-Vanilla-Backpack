package net.goulden.gouldensvanillabackpack.common.events;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlockEntity;
import net.goulden.gouldensvanillabackpack.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = GouldensVanillaBackpack.MODID)
public class BackpackUpgradeEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        if (!player.isCrouching()) return;
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        BlockState blockState = level.getBlockState(blockPos);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if (blockEntity instanceof BackpackBlockEntity backpackBlockEntity) {

            // DYE BACKPACK
            if (event.getItemStack().getItem() instanceof DyeItem dyeItem) {
                if (!level.isClientSide) {
                    int color = dyeItem.getDyeColor().getTextureDiffuseColor();
                    double hitY = event.getHitVec().getLocation().y - blockPos.getY();
                    boolean isFloating = level.getBlockState(blockPos).getValue(BackpackBlock.FLOATING);
                    if (hitY > (isFloating ? 0.3124 : 0.4687)) {
                        backpackBlockEntity.setLidColor(color);
                    } else {
                        backpackBlockEntity.setBaseColor(color);
                    }
                    level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
                    if (!player.isCreative()) event.getItemStack().shrink(1);
                    level.playSound(null, blockPos, BPSounds.BACKPACK_DYE.value(), SoundSource.BLOCKS);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                event.setCanceled(true);
            }

            // REINFORCE BACKPACK
            if (event.getItemStack().is(Items.NETHERITE_SCRAP) && !backpackBlockEntity.isReinforced()) {
                if (!level.isClientSide) {
                    backpackBlockEntity.setReinforced(true);
                    level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
                    if (!player.isCreative()) event.getItemStack().shrink(1);
                    level.playSound(null, blockPos, BPSounds.BACKPACK_REINFORCE.value(), SoundSource.BLOCKS);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                event.setCanceled(true);
            }

            // LOCK BACKPACK
            if (event.getItemStack().is(Items.TRIAL_KEY) && !backpackBlockEntity.isLocked()) {
                if (!level.isClientSide) {
                    backpackBlockEntity.setLocked(true);
                    level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
                    if (!player.isCreative()) event.getItemStack().shrink(1);
                    level.playSound(null, blockPos, BPSounds.BACKPACK_LOCK.value(), SoundSource.BLOCKS);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                event.setCanceled(true);
            }

            // UNLOCK BACKPACK
            if (event.getItemStack().is(Items.SHEARS) && backpackBlockEntity.isLocked()) {
                if (!level.isClientSide) {
                    backpackBlockEntity.setLocked(false);
                    level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
                    level.addFreshEntity(new ItemEntity(level, blockPos.getX() + 0.5, blockPos.getY() + 0.8, blockPos.getZ() + 0.5,
                            new ItemStack(Items.TRIAL_KEY)
                    ));
                    level.playSound(null, blockPos, BPSounds.BACKPACK_UNLOCK.value(), SoundSource.BLOCKS);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                event.setCanceled(true);
            }
        }
    }
}