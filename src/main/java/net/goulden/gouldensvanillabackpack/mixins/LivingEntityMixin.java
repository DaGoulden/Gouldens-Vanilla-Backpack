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
        float current = getData(OPEN_TICKS) / 10f;
        float target = getData(IS_OPEN) ? 1.0f : 0.0f;

        float speed = 0.15f;
        float next = current + (target - current) * speed;

        setData(OPEN_TICKS, (int)(next * 10));
    }

    public void onBackpackOpen() {
        this.setData(IS_OPEN, true);
    }

    public void onBackpackClose() {
        this.setData(IS_OPEN, false);
    }
}