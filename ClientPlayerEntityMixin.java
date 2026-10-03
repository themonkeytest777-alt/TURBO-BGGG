package com.turbominer.mixin;

import com.turbominer.Ghost;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    /** PlayerEntity.tick remet noClip a false chaque tick : on le force avant le mouvement. */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void turbominer$ghostNoClip(CallbackInfo ci) {
        if (Ghost.active) {
            ((Entity) (Object) this).noClip = true;
        }
    }

    /**
     * Pendant le ghost, aucun paquet de position n'est envoye. A la sortie du ghost, le jeu
     * envoie normalement la position actuelle (la ou tu es) au serveur.
     */
    @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
    private void turbominer$freezeServerPosition(CallbackInfo ci) {
        if (Ghost.active) {
            ci.cancel();
        }
    }
}
