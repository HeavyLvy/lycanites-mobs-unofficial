package com.lycanitesmobs;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.client.ClientProxy;
import com.lycanitesmobs.client.ModelManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.*;
import com.lycanitesmobs.core.capabilities.*;
import com.lycanitesmobs.core.command.CommandManager;
import com.lycanitesmobs.core.compatibility.DLDungeons;
import com.lycanitesmobs.core.config.ConfigDebug;
import com.lycanitesmobs.core.config.CoreConfig;
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.entity.ExtendedEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.helpers.LMReflectionHelper;
import com.lycanitesmobs.core.info.*;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.item.consumable.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.ItemWinterGift;
import com.lycanitesmobs.core.item.equipment.EquipmentPartManager;
import com.lycanitesmobs.core.mobevent.MobEventListener;
import com.lycanitesmobs.core.mobevent.MobEventManager;
import com.lycanitesmobs.core.network.PacketHandler;
import com.lycanitesmobs.core.spawner.SpawnerEventListener;
import com.lycanitesmobs.core.spawner.SpawnerManager;
import com.lycanitesmobs.core.worldgen.BiomeModifierProvider;
import com.lycanitesmobs.core.worldgen.WorldGenManager;
import com.lycanitesmobs.core.worldgen.WorldGenerator;
import com.lycanitesmobs.core.worldgen.WorldGeneratorBiomeModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LycanitesMobs.MODID)
public class LycanitesMobs {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String MODID = "lycanitesmobs";
	public static final String name = "Lycanites Mobs";
	public static final String versionNumber = "2.3.3.4";
	public static final String versionMC = "1.16.5";
	public static final String version = versionNumber + " - MC " + versionMC;
	public static final String website = "https://lycanitesmobs.com";
	public static final String serviceAPI = "https://service.lycanitesmobs.com/api/v1";
	public static final String twitter = "https://twitter.com/Lycanite05";
	public static final String patreon = "https://www.patreon.com/lycanite";
	public static final String guilded = "https://www.guilded.gg/i/jpLvd6J2";
	public static final String discord = "https://discord.gg/bFpV3z4";

	public static final PacketHandler packetHandler = new PacketHandler();

	public static ModInfo modInfo;

	public static boolean configReady = false;
	public static boolean earlyDebug = false;
	// Biome Management:
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);

	public static final RegistryObject<Codec<WorldGeneratorBiomeModifier>> WORLD_GENERATOR_CODEC = BIOME_MODIFIER_SERIALIZERS.register("world_generator", () ->
			RecordCodecBuilder.create(instance -> instance.group(
					Biome.LIST_CODEC.fieldOf("biomes").forGetter(WorldGeneratorBiomeModifier::biomes),
					PlacedFeature.CODEC.fieldOf("chunk_spawn_feature").forGetter(WorldGeneratorBiomeModifier::chunkSpawnFeature),
					PlacedFeature.CODEC.fieldOf("dungeon_feature").forGetter(WorldGeneratorBiomeModifier::dungeonFeature),
					PlacedFeature.CODEC.fieldOf("poison_lake_feature").forGetter(WorldGeneratorBiomeModifier::poisonLakeFeature),
					PlacedFeature.CODEC.fieldOf("poison_spring_feature").forGetter(WorldGeneratorBiomeModifier::poisonSpringFeature),
					PlacedFeature.CODEC.fieldOf("acid_lake_feature").forGetter(WorldGeneratorBiomeModifier::acidLakeFeature),
					PlacedFeature.CODEC.fieldOf("acid_spring_feature").forGetter(WorldGeneratorBiomeModifier::acidSpringFeature),
					PlacedFeature.CODEC.fieldOf("ooze_lake_feature").forGetter(WorldGeneratorBiomeModifier::oozeLakeFeature),
					PlacedFeature.CODEC.fieldOf("ooze_spring_feature").forGetter(WorldGeneratorBiomeModifier::oozeSpringFeature),
					PlacedFeature.CODEC.fieldOf("moglava_lake_feature").forGetter(WorldGeneratorBiomeModifier::moglavaLakeFeature),
					PlacedFeature.CODEC.fieldOf("moglava_spring_feature").forGetter(WorldGeneratorBiomeModifier::moglavaSpringFeature)
			).apply(instance, WorldGeneratorBiomeModifier::new))
	);


	// Proxy:
	public static IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

	// Capabilities:
	public static final Capability<ExtendedEntity> EXTENDED_ENTITY = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<ExtendedPlayer> EXTENDED_PLAYER = CapabilityManager.get(new CapabilityToken<>(){});


	// Potion Effects:
	public static Effects EFFECTS;

	/** Constructor **/
	public LycanitesMobs() {
		modInfo = new ModInfo(this, name, 1000);
		FileLoader.initAll(modInfo.modid);
		StreamLoader.initAll(modInfo.modid);

		//Biome Management
		BIOME_MODIFIER_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());

		// Config:
		CoreConfig.buildSpec();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);

		// Setup Event Listeners:
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
//		modEventBus.addListener(this::serverStarting);
		modEventBus.addListener(this::enqueueIMC);
		modEventBus.addListener(this::processIMC);
		modEventBus.register(ObjectManager.getInstance());
		modEventBus.register(ItemManager.getInstance());
		modEventBus.register(CreatureManager.getInstance());
		modEventBus.register(ProjectileManager.getInstance());
		modEventBus.register(WorldGenManager.getInstance());
//		modEventBus.register(StatManager.getInstance()); TODO Stats
		PROXY.registerEvents();

		// Game Event Listeners:
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(CommandManager.getInstance());
		MinecraftForge.EVENT_BUS.register(new GameEventListener());
		MinecraftForge.EVENT_BUS.register(SpawnerEventListener.getInstance());
		MinecraftForge.EVENT_BUS.register(MobEventManager.getInstance());
		MinecraftForge.EVENT_BUS.register(MobEventListener.getInstance());
		MinecraftForge.EVENT_BUS.register(new WorldGenerator());

		// Network:
		packetHandler.register();

		// Load Content:
		this.loadContent();
	}

	// Content Loading:
	public void loadContent() {
		// Elements:
		ElementManager.getInstance().loadAllFromJson(modInfo);

		// Vanilla Item Lists:
		ObjectLists.createVanillaLists();

		// Potion Effects:
		EFFECTS = new Effects();

		// Blocks and Items:
		ItemManager.getInstance().startup(modInfo);

		// Equipment Parts:
		EquipmentPartManager.getInstance().loadAllFromJson(modInfo);

		// Creatures:
		CreatureManager.getInstance().startup(modInfo);

		// Projectiles:
		ProjectileManager.getInstance().startup(modInfo);

		// Spawners:
		SpawnerManager.getInstance().loadAllFromJson(modInfo);

		// Altars:
		AltarInfo.createAltars();

		// Mob Events:
		MobEventManager.getInstance().loadAllFromJson(modInfo);

		// Dungeons:
		DungeonManager.getInstance().loadAllFromJson(modInfo);

		// Treat Lists:
		ItemHalloweenTreat.createObjectLists();
		ItemWinterGift.createObjectLists();

		// World Gen:
		WorldGenManager.getInstance().startup();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		configReady = true;
		ObjectManager.setCurrentModInfo(modInfo);
		this.loadConfigs();
		// Fix Health Limit:
		LMReflectionHelper.fixMaxHealth();

		// Mod Support:
		DLDungeons.init();
	}

	@SubscribeEvent
	public void clientSetup(final FMLClientSetupEvent event) {
		ClientManager.getInstance().initLanguageManager();
		ClientManager.getInstance().registerScreens();
		ClientManager.getInstance().registerEvents();
		TextureManager.getInstance().createTextures(modInfo);
		ModelManager.getInstance().createModels();
		ClientManager.getInstance().initRenderRegister();
	}

	@SubscribeEvent
	public void serverStarting(final ServerStartingEvent event) {

	}

	public void loadConfigs() {
		ItemManager.getInstance().loadConfig();
		CreatureManager.getInstance().loadConfig();
		MobEventManager.getInstance().loadConfig();
		AltarInfo.loadGlobalSettings();
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
		// Example code to dispatch IMC to another mod
		//InterModComms.sendTo("modif", "methodname", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
	}

	private void processIMC(final InterModProcessEvent event) {
		// Example code to receive and process InterModComms from other mods
		//LOGGER.info("Got IMC {}", event.getIMCStream().map(m->m.getMessageSupplier().get()).collect(Collectors.toList()));
	}


	/**
	 * Prints an info message into the console.
	 * @param key The debug config key to use, if empty, the message is always printed.
	 * @param message The message to print.
	 */
	public static void logInfo(String key, String message) {
		if("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
			LOGGER.info("[LycanitesMobs] [Info] [" + key + "] " + message);
		}
	}


	/**
	 * Prints an info debug into the console.
	 * @param key The debug config key to use, if empty, the message is always printed.
	 * @param message The message to print.
	 */
	public static void logDebug(String key, String message) {
		if("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
			LOGGER.debug("[LycanitesMobs] [Debug] [" + key + "] " + message);
		}
	}


	/**
	 * Prints an info warning into the console.
	 * @param key The debug config key to use, if empty, the message is always printed.
	 * @param message The message to print.
	 */
	public static void logWarning(String key, String message) {
		if("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
			LOGGER.warn("[LycanitesMobs] [WARNING] [" + key + "] " + message);
		}
	}


	/**
	 * Prints an error message into the console.
	 * @param message The error message to print.
	 */
	public static void logError(String message) {
		LOGGER.error("[LycanitesMobs] " + message);
	}
}
