package com.turbominer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * Ghost / NoClip : le joueur traverse les blocs. Aucun paquet de mouvement n'est envoye au serveur
 * pendant le mode (voir ClientPlayerEntityMixin).
 *  - Ghost  : vol libre dans la direction du regard (vitesse "ghost").
 *  - NoClip : deplacement a plat selon ton orientation + saut / sneak pour monter / descendre (vitesse "noclip").
 * Au 2e appui, tu restes la ou tu es. En solo, le serveur integre te teleporte exactement la (meme dans des blocs).
 */
public final class Ghost {
    public static boolean active = false;
    /** false = ghost, true = noclip. */
    public static boolean noclipMode = false;

    private static ClientPlayerEntity owner;

    private Ghost() {}

    public static void toggle(boolean noclip) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p == null) return;
        if (!active) {
            owner = p;
            noclipMode = noclip;
            active = true;
        } else if (noclipMode != noclip) {
            noclipMode = noclip; // bascule ghost <-> noclip sans sortir
        } else {
            exit(p);
        }
    }

    private static void exit(ClientPlayerEntity p) {
        final double x = p.getX();
        final double y = p.getY();
        final double z = p.getZ();
        final float yaw = p.getYaw();
        final float pitch = p.getPitch();
        final UUID id = p.getUuid();

        active = false;
        noclipMode = false;
        p.noClip = false;
        p.setVelocity(Vec3d.ZERO);
        p.fallDistance = 0;
        owner = null;

        // Solo : le serveur integre te place exactement ici (pas de verification de collision).
        final IntegratedServer srv = MinecraftClient.getInstance().getServer();
        if (srv != null) {
            srv.execute(() -> {
                ServerPlayerEntity sp = srv.getPlayerManager().getPlayer(id);
                if (sp != null) {
                    sp.fallDistance = 0;
                    sp.networkHandler.requestTeleport(x, y, z, yaw, pitch);
                }
            });
        }
    }

    public static void reset() {
        active = false;
        noclipMode = false;
        owner = null;
    }

    /** Appele a la fin de chaque tick client. */
    public static void tick(ClientPlayerEntity p) {
        if (!active) return;
        if (owner != p || p.isDead()) {
            reset();
            return;
        }

        p.noClip = true;
        p.fallDistance = 0;
        PlayerAbilities ab = p.getAbilities();
        ab.allowFlying = true;
        ab.flying = true;

        Input in = p.input;
        double f = (in.pressingForward ? 1 : 0) - (in.pressingBack ? 1 : 0);
        double s = (in.pressingLeft ? 1 : 0) - (in.pressingRight ? 1 : 0);
        double vy = (in.jumping ? 1 : 0) - (in.sneaking ? 1 : 0);

        double yaw = Math.toRadians(p.getYaw());
        Vec3d left = new Vec3d(Math.cos(yaw), 0, Math.sin(yaw));
        Vec3d forward = noclipMode ? new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw)) : p.getRotationVec(1.0F);
        double mult = noclipMode ? ModConfig.noclipMultiplier : ModConfig.ghostMultiplier;

        Vec3d v = forward.multiply(f).add(left.multiply(s)).add(0, vy, 0);
        if (v.lengthSquared() > 1.0E-6) {
            if (v.lengthSquared() > 1.0) v = v.normalize();
            v = v.multiply(0.55 * mult);
        } else {
            v = Vec3d.ZERO;
        }
        p.setVelocity(v);
    }
}
