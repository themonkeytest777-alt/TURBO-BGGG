package com.turbominer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.Set;

/** Cherche le biome enneige le plus proche dans les chunks actuellement charges autour du joueur. */
public final class BiomeFinder {
    private static final Set<String> SNOWY = Set.of(
            "snowy_plains", "ice_spikes", "snowy_taiga", "snowy_slopes", "grove",
            "frozen_peaks", "jagged_peaks", "frozen_river", "snowy_beach",
            "frozen_ocean", "deep_frozen_ocean");

    /** Biomes de la surface (id, nom FR). Les biomes de grottes ne sont pas detectables depuis la surface. */
    private static final String[][] BIOMES = {
            {"plains", "Plaines"}, {"sunflower_plains", "Plaines de tournesols"}, {"forest", "Foret"},
            {"flower_forest", "Foret fleurie"}, {"birch_forest", "Foret de bouleaux"},
            {"old_growth_birch_forest", "Vieille foret de bouleaux"}, {"dark_forest", "Foret sombre"},
            {"taiga", "Taiga"}, {"old_growth_pine_taiga", "Taiga de pins anciens"},
            {"old_growth_spruce_taiga", "Taiga d'epiceas anciens"}, {"savanna", "Savane"},
            {"savanna_plateau", "Plateau de savane"}, {"windswept_savanna", "Savane ventee"},
            {"desert", "Desert"}, {"badlands", "Badlands"}, {"eroded_badlands", "Badlands erodees"},
            {"wooded_badlands", "Badlands boisees"}, {"jungle", "Jungle"}, {"sparse_jungle", "Jungle clairsemee"},
            {"bamboo_jungle", "Jungle de bambous"}, {"swamp", "Marais"}, {"mangrove_swamp", "Mangrove"},
            {"cherry_grove", "Cerisaie"}, {"meadow", "Prairie"}, {"stony_shore", "Cote rocheuse"},
            {"windswept_hills", "Collines ventees"}, {"windswept_forest", "Foret ventee"},
            {"windswept_gravelly_hills", "Collines de gravier ventees"}, {"stony_peaks", "Pics rocheux"},
            {"mushroom_fields", "Champignons"}, {"beach", "Plage"}, {"river", "Riviere"},
            {"ocean", "Ocean"}, {"deep_ocean", "Ocean profond"}, {"lukewarm_ocean", "Ocean tiede"},
            {"deep_lukewarm_ocean", "Ocean tiede profond"}, {"warm_ocean", "Ocean chaud"},
            {"cold_ocean", "Ocean froid"}, {"deep_cold_ocean", "Ocean froid profond"}
    };
    private static int selected = 0;

    private static final String[] DIRS = {"Est", "Sud-Est", "Sud", "Sud-Ouest", "Ouest", "Nord-Ouest", "Nord", "Nord-Est"};

    private BiomeFinder() {}

    public static String selectedName() {
        return BIOMES[selected][1];
    }

    public static void cycleSelected() {
        selected = (selected + 1) % BIOMES.length;
    }

    /** Touche 3 : uniquement les biomes enneiges / geles. */
    public static void searchSnowy() {
        search(SNOWY, "enneige");
    }

    /** Bouton du menu : le biome choisi dans la liste. */
    public static void searchSelected() {
        search(Set.of(BIOMES[selected][0]), BIOMES[selected][1]);
    }

    private static void search(Set<String> targets, String label) {
        MinecraftClient c = MinecraftClient.getInstance();
        ClientPlayerEntity p = c.player;
        ClientWorld w = c.world;
        if (p == null || w == null) return;

        int px = p.getBlockX();
        int pz = p.getBlockZ();
        int pcx = px >> 4;
        int pcz = pz >> 4;
        int radius = c.options.getViewDistance().getValue() + 2;

        double best = Double.MAX_VALUE;
        int bx = 0;
        int bz = 0;
        String bName = "";
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                if (w.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, false) == null) continue;
                for (int ox = 2; ox < 16; ox += 4) {
                    for (int oz = 2; oz < 16; oz += 4) {
                        int x = (cx << 4) + ox;
                        int z = (cz << 4) + oz;
                        double dx = x - px;
                        double dz = z - pz;
                        double d = dx * dx + dz * dz;
                        if (d >= best) continue;
                        int y = w.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
                        pos.set(x, y, z);
                        String name = w.getBiome(pos).getKey().map(k -> k.getValue().getPath()).orElse("");
                        if (targets.contains(name)) {
                            best = d;
                            bx = x;
                            bz = z;
                            bName = name;
                        }
                    }
                }
            }
        }

        if (best == Double.MAX_VALUE) {
            p.sendMessage(Text.literal("[TurboMiner] Aucun biome " + label + " dans la zone chargee ("
                    + (radius - 2) + " chunks). Deplace-toi puis reessaie."), false);
            return;
        }
        double ang = Math.toDegrees(Math.atan2(bz - pz, bx - px)); // 0 = Est, 90 = Sud
        int idx = ((int) Math.round(ang / 45.0) % 8 + 8) % 8;
        p.sendMessage(Text.literal("[TurboMiner] Biome " + label + " le plus proche : " + bName
                + " | X=" + bx + " Z=" + bz
                + " | " + (int) Math.sqrt(best) + " blocs vers le " + DIRS[idx]), false);
    }
}
