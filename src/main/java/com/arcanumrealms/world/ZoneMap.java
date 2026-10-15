package com.arcanumrealms.world;

import com.arcanumrealms.core.world.ZoneDef;
import com.arcanumrealms.core.world.Zones;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Position des zones dans le monde.
 *
 * <p>Le mod ne genere pas de terrain : les zones de la Cite des Arcanes sont
 * donc posees par le serveur sous forme de points d'ancrage
 * ({@code /arcanum zone set <id>}), et tout ce qui se trouve dans leur rayon
 * appartient a la zone (quetes de visite, rencontres, apparitions).
 */
public final class ZoneMap extends SavedData {

    private static final String DATA_NAME = "arcanum_realms_zones";

    /** Rayon par defaut d'une zone, en blocs. */
    public static final int DEFAULT_RADIUS = 96;

    private final Map<String, Entry> anchors = new LinkedHashMap<>();

    /** Ancre d'une zone : un centre et un rayon. */
    public record Entry(String zoneId, BlockPos center, int radius) {}

    public static ZoneMap get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(ZoneMap::load, ZoneMap::new, DATA_NAME);
    }

    private static ZoneMap load(CompoundTag tag) {
        ZoneMap map = new ZoneMap();
        CompoundTag zones = tag.getCompound("zones");
        for (String zoneId : zones.getAllKeys()) {
            CompoundTag entry = zones.getCompound(zoneId);
            map.anchors.put(zoneId, new Entry(zoneId,
                    new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")),
                    entry.contains("radius") ? entry.getInt("radius") : DEFAULT_RADIUS));
        }
        return map;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag zones = new CompoundTag();
        anchors.forEach((zoneId, entry) -> {
            CompoundTag data = new CompoundTag();
            data.putInt("x", entry.center().getX());
            data.putInt("y", entry.center().getY());
            data.putInt("z", entry.center().getZ());
            data.putInt("radius", entry.radius());
            zones.put(zoneId, data);
        });
        tag.put("zones", zones);
        return tag;
    }

    /** Pose (ou deplace) l'ancre d'une zone. */
    public void set(String zoneId, BlockPos center, int radius) {
        anchors.put(zoneId, new Entry(zoneId, center, radius));
        setDirty();
    }

    public boolean clear(String zoneId) {
        boolean removed = anchors.remove(zoneId) != null;
        if (removed) setDirty();
        return removed;
    }

    public Optional<Entry> anchor(String zoneId) {
        return Optional.ofNullable(anchors.get(zoneId));
    }

    public Map<String, Entry> anchors() {
        return Collections.unmodifiableMap(anchors);
    }

    /** Zone a laquelle appartient une position (l'ancre la plus proche dans son rayon). */
    public Optional<String> zoneAt(ServerLevel level, BlockPos pos) {
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return Optional.empty();
        String best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entry entry : anchors.values()) {
            double distance = entry.center().distSqr(pos);
            if (distance <= (double) entry.radius() * entry.radius() && distance < bestDistance) {
                best = entry.zoneId();
                bestDistance = distance;
            }
        }
        return Optional.ofNullable(best);
    }

    /** Zone posee la plus proche, meme hors rayon : sert au choix des rencontres. */
    public Optional<String> nearestZone(ServerLevel level, BlockPos pos) {
        String best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entry entry : anchors.values()) {
            double distance = entry.center().distSqr(pos);
            if (distance < bestDistance) {
                best = entry.zoneId();
                bestDistance = distance;
            }
        }
        return Optional.ofNullable(best);
    }

    /** Les zones definies dans le moteur mais pas encore posees dans le monde. */
    public java.util.List<String> missing() {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (ZoneDef zone : Zones.all()) {
            if (!anchors.containsKey(zone.id())) out.add(zone.id());
        }
        return out;
    }

    /** Cherche une zone par identifiant, nom francais ou nom anglais. */
    public static Optional<ZoneDef> resolve(String name) {
        if (name == null) return Optional.empty();
        Optional<ZoneDef> direct = Zones.find(name);
        if (direct.isPresent()) return direct;
        String needle = strip(name);
        for (ZoneDef zone : Zones.all()) {
            if (strip(zone.nameFr()).equals(needle) || strip(zone.nameEn()).equals(needle)
                    || strip(zone.id()).equals(needle)) {
                return Optional.of(zone);
            }
        }
        return Optional.empty();
    }

    /** Minuscules sans accents, pour comparer "l'allee des braises" et "Allee des Braises". */
    public static String strip(String text) {
        String lower = text.toLowerCase(Locale.ROOT).replace('\'', ' ').replace('-', ' ');
        StringBuilder out = new StringBuilder(lower.length());
        for (char c : lower.toCharArray()) {
            out.append(switch (c) {
                case 'a', 'à', 'â', 'ä' -> 'a';
                case 'e', 'é', 'è', 'ê', 'ë' -> 'e';
                case 'i', 'î', 'ï' -> 'i';
                case 'o', 'ô', 'ö' -> 'o';
                case 'u', 'ù', 'û', 'ü' -> 'u';
                case 'c', 'ç' -> 'c';
                default -> c;
            });
        }
        return out.toString().trim().replaceAll("\\s+", " ");
    }
}
