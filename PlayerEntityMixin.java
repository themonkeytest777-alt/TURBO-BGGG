package com.turbominer.mixin;

import com.turbominer.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    /** Multiplie la vitesse de destruction des blocs (cote client et serveur integre en solo). */
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void turbominer$boostBreaking(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (!ModConfig.fastMine) return;
        PlayerEntity self = (PlayerEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && self.getUuid().equals(mc.player.getUuid())) {
            float mult = (float) ModConfig.miningMultiplier;
            // Option (menu) : en ligne, un serveur strict n'accepte le bloc qu'a ~70 % du temps normal.
            // Si activee, on plafonne a x1.4 pour eviter les blocs qui reviennent. Desactivee par defaut.
            if (ModConfig.onlineMiningCap && !mc.isInSingleplayer()) mult = Math.min(mult, 1.4F);
            cir.setReturnValue(cir.getReturnValueF() * mult);
        }
    }
}
