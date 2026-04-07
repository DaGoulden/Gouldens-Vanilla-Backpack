package net.goulden.gouldensvanillabackpack.registry;

import net.goulden.gouldensvanillabackpack.GouldensVanillaBackpack;
import net.goulden.gouldensvanillabackpack.common.blocks.BackpackBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy;

public class BPBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GouldensVanillaBackpack.MODID);

    public static final DeferredBlock<BackpackBlock> BACKPACK = BLOCKS.register(
            "backpack", () -> new BackpackBlock(ofFullCopy(Blocks.WHITE_WOOL)
                    .sound(new SoundType(1.0F, 1.0F,
                            SoundEvents.WOOL_BREAK,
                            SoundEvents.WOOL_STEP,
                            BPSounds.BACKPACK_PLACE.value(),
                            SoundEvents.WOOL_HIT,
                            SoundEvents.WOOL_FALL))
                    .forceSolidOn()));
}
