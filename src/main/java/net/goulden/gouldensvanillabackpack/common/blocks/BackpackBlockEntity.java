package net.goulden.gouldensvanillabackpack.common.blocks;

import net.goulden.gouldensvanillabackpack.registry.BPBlockEntities;
import net.goulden.gouldensvanillabackpack.registry.BPDataComponents;
import net.goulden.gouldensvanillabackpack.registry.BPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class BackpackBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> itemStacks;
    public int placeTicks;
    public boolean open;
    private int openCount;
    public int openTicks;
    public int floatTicks;
    private int baseColor;
    private int lidColor;
    private boolean reinforced = false;
    private boolean locked = false;

    public BackpackBlockEntity(BlockPos pos, BlockState blockState) {
        super(BPBlockEntities.BACKPACK.get(), pos, blockState);
        this.itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public int getBaseColor() { return baseColor; }
    public int getLidColor() { return lidColor; }
    public boolean isReinforced() { return reinforced; }
    public boolean isLocked() { return locked; }

    public void setBaseColor(int color) { this.baseColor = color; setChanged(); }
    public void setLidColor(int color) { this.lidColor = color; setChanged(); }
    public void setReinforced(boolean reinforced) { this.reinforced = reinforced; setChanged(); }
    public void setLocked(boolean locked) { this.locked = locked; setChanged(); }

    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            openCount = type;
            if (openCount == 0) { openTicks = 10; }
            if (openCount == 1) { openTicks = 0; }
            open = openCount > 0;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BackpackBlockEntity blockEntity) {
        if (blockEntity.open && blockEntity.openTicks < 10) { ++blockEntity.openTicks; }
        if (!blockEntity.open && blockEntity.openTicks > 0) { --blockEntity.openTicks; }

        if (blockEntity.placeTicks < 20) { ++blockEntity.placeTicks; }

        if (blockEntity.floatTicks < 90) { ++blockEntity.floatTicks; }
        if (blockEntity.floatTicks == 90) { blockEntity.floatTicks = 0; }
    }

    public void onOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            ++openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, openCount);
            if (this.openCount == 1) {
                this.level.gameEvent(player, GameEvent.CONTAINER_OPEN, this.worldPosition);
                this.level.playSound(null, this.getBlockPos(), BPSounds.BACKPACK_OPEN.value(), SoundSource.BLOCKS);
            }
        }
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            --openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, openCount);
            if (this.openCount <= 0) {
                this.level.gameEvent(player, GameEvent.CONTAINER_CLOSE, this.worldPosition);
                this.level.playSound(null, this.getBlockPos(), BPSounds.BACKPACK_CLOSE.value(), SoundSource.BLOCKS);
            }
        }
    }

    protected Component getDefaultName() {
        return Component.translatable("container.backpack");
    }

    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.itemStacks = items;
    }

    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return new ShulkerBoxMenu(id, player, this);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.loadFromTag(tag, registries);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.itemStacks, false, registries);
        }
        tag.putInt("FloatTicks", this.floatTicks);
        tag.putInt("BaseColor", this.baseColor);
        tag.putInt("LidColor", this.lidColor);
        tag.putBoolean("Reinforced", this.reinforced);
        tag.putBoolean("Locked", this.locked);
        setChanged();
    }

    public void loadFromTag(CompoundTag tag, HolderLookup.Provider levelRegistry) {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag) && tag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(tag, this.itemStacks, levelRegistry);
        }
        this.floatTicks = tag.getInt("FloatTicks");
        this.baseColor = tag.getInt("BaseColor");
        this.lidColor = tag.getInt("LidColor");
        this.reinforced = tag.getBoolean("Reinforced");
        this.locked = tag.getBoolean("Locked");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();

        saveAdditional(tag, registries);
        return tag;
    }

    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        Integer base = input.get(BPDataComponents.BASE_COLOR.get());
        this.baseColor = base != null ? base : 0;
        Integer lid = input.get(BPDataComponents.LID_COLOR.get());
        this.lidColor = lid != null ? lid : 0;
        Boolean reinforced = input.get(BPDataComponents.REINFORCED.get());
        this.reinforced = reinforced != null && reinforced;
        Boolean locked = input.get(BPDataComponents.LOCKED.get());
        this.locked = locked != null && locked;
    }

    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (baseColor != 0) components.set(BPDataComponents.BASE_COLOR.get(), baseColor);
        if (lidColor != 0) components.set(BPDataComponents.LID_COLOR.get(), lidColor);
        if (reinforced) components.set(BPDataComponents.REINFORCED.get(), true);
        if (locked) components.set(BPDataComponents.LOCKED.get(), true);
    }

    public int getContainerSize() {
        return this.itemStacks.size();
    }
}
