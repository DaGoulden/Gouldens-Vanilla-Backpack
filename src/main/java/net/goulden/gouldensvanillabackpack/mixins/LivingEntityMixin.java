package net.goulden.gouldensvanillabackpack.mixins;

import net.goulden.gouldensvanillabackpack.common.entity.BackpackWearer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.goulden.gouldensvanillabackpack.registry.BPAttachments.IS_OPEN;
import static net.goulden.gouldensvanillabackpack.registry.BPAttachments.OPEN_TICKS;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements BackpackWearer {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    public void baseTick(CallbackInfo ci) {
        if (getData(IS_OPEN) && getData(OPEN_TICKS) < 10) { setData(OPEN_TICKS, getData(OPEN_TICKS) + 1); }
        if (!getData(IS_OPEN) && getData(OPEN_TICKS) > 0) { setData(OPEN_TICKS, getData(OPEN_TICKS) - 1); }
    }

    public void onBackpackOpen() {
        this.setData(IS_OPEN, true);
    }

    public void onBackpackClose() {
        this.setData(IS_OPEN, false);
    }
}