# 1.21.1 NeoForge port notes

Notes from porting Lycanites Mobs (schism fork) from Forge 1.20.1 to NeoForge 1.21.1. These are differences or open questions to know about, not bugs that have been fixed.

## Rendering

- **Creature shading is matte by design.** Without a shader pack, creatures are drawn with the mod's own `pos_tex_normal` shader. This shader only uses the world light level and deliberately does no directional (sun/side) shading, so models look evenly lit. That's unchanged from the 1.20.1 code. With Iris and a shader pack active, creatures go through vanilla entity render types and the pack does the shading. Adding vanilla-style two-light shading to `pos_tex_normal.fsh` would give darker sides and undersides without a pack.
- **Fear mesh shader:** 1.21 removed the `position_tex_color_normal` shader, so `POSITION_TEX_COLOR_NORMAL` VBO draws (the fear meshes) use the clouds shader, which has the same vertex layout. Worth a visual check.
- **Name plates:** creatures show a name only if they have a custom name, or if they're an uncommon/rare variant with the variant name tag config on. The health bar seen in some modpacks comes from the Neat mod.

## Gameplay or behaviour differences

- **Dungeon spawners:** NeoForge removed `DungeonHooks`, so `dungeonWeight` in creature spawn configs is ignored. Vanilla dungeon spawners can't be extended from code anymore.
- **Tick handlers:** a few that had no phase check ran twice per tick on Forge (start and end). They now run once, at the end of the tick.
- **Leashing:** "only the owner can leash a tamed creature" is enforced in `GameEventListener#onEntityInteract`, because 1.21's `canBeLeashed()` no longer receives the player.
- **Biome debug commands** (`/lm debug biomesfromtag`, `listbiometags`) now use biome tags such as `c:is_cold`, because Forge's biome types no longer exist.
- **Creature debug overlay** lines now appear in the F3 screen (`CustomizeGuiOverlayEvent.DebugText`). The old stand-in event was never fired.
- **Mouse button handler:** fires once per click (`MouseButton.Post`). Forge fired it for both Pre and Post.

## Kept as-is from 1.20.1 (existing behaviour)

- **World data:** `ExtendedWorld` never reads its saved data back from disk (the `SavedData` deserializer ignores the tag), so mob-event and boss state resets after a restart.
- **Spawn placement check:** `Spawner#checkForgeSpawnPlacement` builds a spawn-placement event but never posts it, so it always returns "default".

## Data format changes made during the port

- **Recipes:** results use `"id"` instead of `"item"`, cooking and stonecutting results are objects, and stonecutting `count` moved into the result.
- **Biome tags:** `forge:is_*` became `c:is_*` (`is_coniferous` → `c:is_tree/coniferous`, `is_dense` → `c:is_dense_vegetation`).
- **Renamed vanilla IDs:** `minecraft:grass` → `short_grass`, `minecraft:scute` → `turtle_scute`.
- **Folders:** data folders use the 1.21 singular names (`recipe`, `loot_table`, `tags/item`, and so on), and `assets/.../modelParts` was renamed to `modelparts` because resource paths must be lowercase.

## Open questions

- **Nether spawns:** reported as not seen in-game. The configs look right: the demons (Apollyon, Behemophet, Belphegor, Grell, Grigori, Malwrath, Vorach) spawn through the Nether/End `land_deadly` / `sky_deadly` world spawners, which fire every 800 ticks with a 50% chance, 24–48 blocks from a player. They're limited to specific Nether biomes. To investigate, run `/lm debug log jsonspawner` and `/lm debug log mobspawns` in the Nether and watch `run/logs/latest.log`.
