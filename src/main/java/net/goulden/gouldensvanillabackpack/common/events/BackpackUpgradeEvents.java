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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
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

        if (blockEntity instanceof BackpackBlockEntity be) {

            // DYE BACKPACK
            if (event.getItemStack().getItem() instanceof DyeItem dyeItem) {
                if (!level.isClientSide) {
                    int color = dyeItem.getDyeColor().getTextureDiffuseColor();
                    double hitY = event.getHitVec().getLocation().y - blockPos.getY();
                    boolean isFloating = level.getBlockState(blockPos).getValue(BackpackBlock.FLOATING);
                    if (hitY > (isFloating ? 0.35 : 0.5)) {
                        be.setLidColor(color);
                    } else {
                        be.setBaseColor(color);
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
            if (event.getItemStack().is(Items.NETHERITE_SCRAP) && !be.isReinforced()) {
                if (!level.isClientSide) {
                    be.setReinforced(true);
                    level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS);
                    if (!player.isCreative()) event.getItemStack().shrink(1);
                    level.playSound(null, blockPos, BPSounds.BACKPACK_REINFORCE.value(), SoundSource.BLOCKS);
                } else {
                    player.swing(InteractionHand.MAIN_HAND);
                }
                event.setCanceled(true);
            }
        }
    }
}