package com.turbominer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ModConfig {
    public static boolean fastMine = false;
    public static boolean speedHack = false;
    public static boolean fly = false;
    public static boolean autoMine = false;
    public static boolean noMineDelay = true;
    public static boolean showHud = true;
    public static boolean step = false;
    public static boolean onlineMiningCap = false; // true = plafonne le minage a x1.4 en ligne

    public static double miningMultiplier = 10.0; // 1 - 100
    public static double speedMultiplier = 3.0;   // 1 - 100
    public static double flyMultiplier = 3.0;     // 1 - 20
    public static double stepHeight = 3.0;        // 1 - 10 blocs
    public static double ghostMultiplier = 3.0;   // 1 - 100
    public static double noclipMultiplier = 3.0;  // 1 - 100

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("turbominer.json");

    private ModConfig() {}

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            fastMine = bool(o, "fastMine", fastMine);
            speedHack = bool(o, "speedHack", speedHack);
            fly = bool(o, "fly", fly);
            noMineDelay = bool(o, "noMineDelay", noMineDelay);
            showHud = bool(o, "showHud", showHud);
            step = bool(o, "step", step);
            onlineMiningCap = bool(o, "onlineMiningCap", onlineMiningCap);
            miningMultiplier = clamp(num(o, "miningMultiplier", miningMultiplier), 1, 100);
            speedMultiplier = clamp(num(o, "speedMultiplier", speedMultiplier), 1, 100);
            flyMultiplier = clamp(num(o, "flyMultiplier", flyMultiplier), 1, 20);
            stepHeight = clamp(num(o, "stepHeight", stepHeight), 1, 10);
            ghostMultiplier = clamp(num(o, "ghostMultiplier", ghostMultiplier), 1, 100);
            noclipMultiplier = clamp(num(o, "noclipMultiplier", noclipMultiplier), 1, 100);
            // autoMine n'est volontairement pas restaure au demarrage
        } catch (Exception e) {
            System.err.println("[TurboMiner] Config illisible, valeurs par defaut : " + e);
        }
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("fastMine", fastMine);
            o.addProperty("speedHack", speedHack);
            o.addProperty("fly", fly);
            o.addProperty("noMineDelay", noMineDelay);
            o.addProperty("showHud", showHud);
            o.addProperty("step", step);
            o.addProperty("onlineMiningCap", onlineMiningCap);
            o.addProperty("miningMultiplier", miningMultiplier);
            o.addProperty("speedMultiplier", speedMultiplier);
            o.addProperty("flyMultiplier", flyMultiplier);
            o.addProperty("stepHeight", stepHeight);
            o.addProperty("ghostMultiplier", ghostMultiplier);
            o.addProperty("noclipMultiplier", noclipMultiplier);
            Files.writeString(FILE, o.toString());
        } catch (Exception e) {
            System.err.println("[TurboMiner] Impossible de sauvegarder la config : " + e);
        }
    }

    public static String fmt(double v) {
        if (v == Math.floor(v)) return Integer.toString((int) v);
        return String.format(Locale.ROOT, "%.1f", v);
    }

    private static boolean bool(JsonObject o, String k, boolean d) {
        return o.has(k) ? o.get(k).getAsBoolean() : d;
    }

    private static double num(JsonObject o, String k, double d) {
        return o.has(k) ? o.get(k).getAsDouble() : d;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
