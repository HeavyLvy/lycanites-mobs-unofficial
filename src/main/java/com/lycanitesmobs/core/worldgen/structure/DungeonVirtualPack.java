package com.lycanitesmobs.core.worldgen.structure;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.locating.IModFile;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * A virtual PackResources that dynamically generates worldgen structure, structure_set,
 * and biome tag JSON files from dungeon schematic configs at startup.
 *
 * This eliminates the need for hard-coded JSON files in the resources folder and allows
 * full customization of dungeon biome placement through the schematic config JSONs.
 */
public class DungeonVirtualPack implements PackResources {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String packId;
    private final Map<ResourceLocation, byte[]> resources = new HashMap<>();

    public DungeonVirtualPack(String packId) {
        this.packId = packId;
        generateResources();
    }

    private void generateResources() {
        try {
            Map<String, JsonObject> defaultSchematics = new LinkedHashMap<>();
            Map<String, JsonObject> customSchematics = new LinkedHashMap<>();

            IModFile modFile = ModList.get().getModFileById(LycanitesMobs.MODID).getFile();
            Path jarSchematicsDir = modFile.findResource("common", "lycanitesmobs", "dungeons", "schematics");
            if (Files.isDirectory(jarSchematicsDir)) {
                loadSchematicsFromDir(jarSchematicsDir, defaultSchematics);
            }

            Path configDir = Paths.get(".", "config", LycanitesMobs.MODID, "dungeons", "schematics");
            if (Files.isDirectory(configDir)) {
                loadSchematicsFromDir(configDir, customSchematics);
            }

            Map<String, JsonObject> mergedSchematics = new LinkedHashMap<>(defaultSchematics);
            for (Map.Entry<String, JsonObject> entry : customSchematics.entrySet()) {
                String name = entry.getKey();
                JsonObject customJson = entry.getValue();
                if (defaultSchematics.containsKey(name)) {
                    boolean loadDefault = !customJson.has("loadDefault") || customJson.get("loadDefault").getAsBoolean();
                    if (!loadDefault) {
                        mergedSchematics.put(name, customJson);
                    }
                } else {
                    mergedSchematics.put(name, customJson);
                }
            }

            for (JsonObject json : mergedSchematics.values()) {
                processSchematic(json);
            }

            LMHelperClass.logDebug("DungeonVirtualPack",
                    "Generated " + resources.size() + " virtual worldgen resources from dungeon schematics.");

        } catch (Exception e) {
            LMHelperClass.logWarning("DungeonVirtualPack",
                    "Failed to generate virtual dungeon resources: " + e.getMessage());
        }
    }

    private void loadSchematicsFromDir(Path dir, Map<String, JsonObject> target) {
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("name")) {
                        target.put(json.get("name").getAsString().toLowerCase(), json);
                    }
                } catch (Exception e) {
                    LMHelperClass.logWarning("DungeonVirtualPack",
                            "Failed to read schematic: " + path.getFileName() + ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LMHelperClass.logWarning("DungeonVirtualPack",
                    "Failed to list schematics in: " + dir + ": " + e.getMessage());
        }
    }

    private void processSchematic(JsonObject json) {
        try {
            String name = json.get("name").getAsString().toLowerCase();

            if (json.has("enabled") && !json.get("enabled").getAsBoolean()) {
                return;
            }

            int spacing = 32;
            int separation = 8;
            int salt = 0;
            List<String> biomeTags = new ArrayList<>();
            List<String> biomeIds = new ArrayList<>();

            if (json.has("conditions")) {
                for (JsonElement condEl : json.getAsJsonArray("conditions")) {
                    JsonObject cond = condEl.getAsJsonObject();
                    String type = cond.has("type") ? cond.get("type").getAsString() : "";
                    if (!"world".equals(type)) continue;

                    if (cond.has("spacing")) spacing = cond.get("spacing").getAsInt();
                    if (cond.has("separation")) {
                        separation = cond.get("separation").getAsInt();
                    } else {
                        separation = Math.max(1, spacing / 4);
                    }
                    if (cond.has("salt")) salt = cond.get("salt").getAsInt();

                    if (cond.has("biomeTags")) {
                        for (JsonElement e : cond.getAsJsonArray("biomeTags")) {
                            String tag = e.getAsString();
                            if (!tag.isEmpty()) biomeTags.add(tag);
                        }
                    }
                    if (cond.has("biomeIds")) {
                        for (JsonElement e : cond.getAsJsonArray("biomeIds")) {
                            String id = e.getAsString();
                            if (!id.isEmpty()) biomeIds.add(id);
                        }
                    }
                    break;
                }
            }

            if (biomeTags.isEmpty() && biomeIds.isEmpty()) {
                LMHelperClass.logWarning("DungeonVirtualPack",
                        "Schematic '" + name + "' has no biome tags or IDs, skipping worldgen resource generation.");
                return;
            }

            generateBiomeTag(name, biomeTags, biomeIds);
            generateStructure(name);
            generateStructureSet(name, spacing, separation, salt);

            LMHelperClass.logDebug("DungeonVirtualPack",
                    "Generated virtual worldgen resources for dungeon: " + name);

        } catch (Exception e) {
            LMHelperClass.logWarning("DungeonVirtualPack",
                    "Failed to process schematic: " + e.getMessage());
        }
    }

    /**
     * Generates a biome tag JSON from the schematic's condition biomeTags and biomeIds.
     * biomeTags entries are written as tag references (prefixed with #).
     * biomeIds entries are written as direct biome resource locations.
     */
    private void generateBiomeTag(String name, List<String> biomeTags, List<String> biomeIds) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);
        JsonArray values = new JsonArray();

        for (String bt : biomeTags) {
            if (bt.contains(":")) {
                values.add("#" + bt);
            }
        }
        for (String bi : biomeIds) {
            if (bi.contains(":")) {
                values.add(bi);
            }
        }

        tag.add("values", values);

        resources.put(
                new ResourceLocation("lycanitesmobs", "tags/worldgen/biome/has_structure/" + name + ".json"),
                toBytes(tag)
        );
    }

    /**
     * Generates a structure JSON that references our custom lm_dungeon type,
     * the schematic name, and the dynamically created biome tag.
     */
    private void generateStructure(String name) {
        JsonObject structure = new JsonObject();
        structure.addProperty("type", "lycanitesmobs:lm_dungeon");
        structure.addProperty("schematic_name", name);
        structure.addProperty("biomes", "#lycanitesmobs:has_structure/" + name);
        structure.addProperty("step", "underground_structures");
        structure.addProperty("terrain_adaptation", "none");
        structure.add("spawn_overrides", new JsonObject());

        resources.put(
                new ResourceLocation("lycanitesmobs", "worldgen/structure/" + name + ".json"),
                toBytes(structure)
        );
    }

    /**
     * Generates a structure_set JSON with random_spread placement using
     * the spacing, separation, and salt from the schematic's condition.
     */
    private void generateStructureSet(String name, int spacing, int separation, int salt) {
        JsonObject set = new JsonObject();

        JsonArray structures = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("structure", "lycanitesmobs:" + name);
        entry.addProperty("weight", 1);
        structures.add(entry);
        set.add("structures", structures);

        JsonObject placement = new JsonObject();
        placement.addProperty("type", "minecraft:random_spread");
        placement.addProperty("spacing", spacing);
        placement.addProperty("separation", separation);
        placement.addProperty("salt", salt);
        set.add("placement", placement);

        resources.put(
                new ResourceLocation("lycanitesmobs", "worldgen/structure_set/" + name + ".json"),
                toBytes(set)
        );
    }

    private byte[] toBytes(JsonObject json) {
        return GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String packId() {
        return packId;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        if (path.length == 1 && "pack.mcmeta".equals(path[0])) {
            String meta = "{\"pack\":{\"description\":\"Lycanites Mobs Dynamic Dungeons\",\"pack_format\":15}}";
            return () -> new ByteArrayInputStream(meta.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.SERVER_DATA) return null;
        byte[] data = resources.get(location);
        if (data == null) return null;
        return () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.SERVER_DATA) return;
        String prefix = path.endsWith("/") ? path : path + "/";
        for (Map.Entry<ResourceLocation, byte[]> entry : resources.entrySet()) {
            ResourceLocation loc = entry.getKey();
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(prefix)) {
                byte[] data = entry.getValue();
                output.accept(loc, () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.SERVER_DATA) return Set.of();
        return Set.of("lycanitesmobs");
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
        IoSupplier<InputStream> root = getRootResource("pack.mcmeta");
        if (root == null) return null;
        try (InputStream in = root.get()) {
            JsonObject meta = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            String sectionName = deserializer.getMetadataSectionName();
            if (meta.has(sectionName)) {
                return deserializer.fromJson(meta.getAsJsonObject(sectionName));
            }
        }
        return null;
    }

    @Override
    public void close() {
    }
}
