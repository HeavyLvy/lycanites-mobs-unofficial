package com.lycanitesmobs;

import com.lycanitesmobs.client.gui.screen.block.EquipmentForgeScreen;
import com.lycanitesmobs.client.gui.screen.block.EquipmentInfuserScreen;
import com.lycanitesmobs.client.gui.screen.block.EquipmentStationScreen;
import com.lycanitesmobs.client.gui.screen.block.SummoningPedestalScreen;
import com.lycanitesmobs.client.gui.screen.creature.CreatureInventoryScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.event.GameEventListener;
import com.lycanitesmobs.core.data.info.altar.AltarInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.core.network.proxy.ClientProxy;
import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.manager.CommandManager;
import com.lycanitesmobs.core.compatibility.DLDungeons;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import com.lycanitesmobs.core.data.config.CoreConfig;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.block.EquipmentInfuserContainer;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.block.SummoningPedestalContainer;
import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.effect.EffectManager;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.item.consumable.holiday.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGift;
import com.lycanitesmobs.core.item.equipment.EquipmentPartManager;
import com.lycanitesmobs.core.event.MobEventListener;
import com.lycanitesmobs.core.network.proxy.IProxy;
import com.lycanitesmobs.core.network.proxy.ServerProxy;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.worldgen.WorldGenerator;
import com.lycanitesmobs.core.worldgen.WorldGeneratorBiomeModifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
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

import static com.lycanitesmobs.core.util.helpers.LMHelperClass.fixMaxHealth;


@Mod(LycanitesMobs.MODID)
@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class LycanitesMobs {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "lycanitesmobs";
    public static final String name = "Lycanites Mobs";
    public static final String versionNumber = "2.3.3.4";
    public static final String versionMC = "1.20.1";
    public static final String version = versionNumber + " - MC " + versionMC;
    public static final String website = "https://lycanitesmobs.com";
    public static final String serviceAPI = "https://service.lycanitesmobs.com/api/v1";
    public static final String twitter = "https://twitter.com/Lycanite05";
    public static final String patreon = "https://www.patreon.com/lycanite";
    public static final String guilded = "https://www.guilded.gg/i/jpLvd6J2";
    public static final String discord = "https://discord.gg/bFpV3z4";

    public static final PacketManager PACKET_MANAGER = new PacketManager();
    // Biome Management:
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
    /*@SubscribeEvent
    public static void onServerLoaded(ServerStartingEvent event) {
        var creatureManager = CreatureManager.getInstance();
        for(CreatureInfo creatureInfo : creatureManager.creatures.values()) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(LycanitesMobs.MODID, creatureInfo.getName()));
            LycanitesMobs.logInfo("", "Registeredcreature: " + entityType);
            creatureInfo.entityType = (EntityType<? extends LivingEntity>) entityType;
            if (creatureInfo.entityType instanceof EntityTypeGetter getter) {
                getter.setRegistryName(new ResourceLocation(LycanitesMobs.MODID, creatureInfo.getName()));
            }
            EntityFactory.getInstance().addEntityType(creatureInfo.entityType, creatureInfo.entityConstructor, creatureInfo.getName());
            creatureInfo.init();
        }
    }*/
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
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.Keys.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.Keys.BLOCKS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, MODID);
    public static ModInfo modInfo;
    public static boolean configReady = false;
    public static boolean earlyDebug = false;
    // Proxy:
    public static IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    // Potion Effects:
    public static EffectManager EffectManager;

    /**
     * Constructor
     **/
    public LycanitesMobs() {
        modInfo = new ModInfo(this, name, 1000);
        // Config:
        CoreConfig.buildSpec();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);

        FileLoader.initAll(modInfo.modid);
        StreamLoader.initAll(modInfo.modid);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //Biome Management
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);

        // Setup Event Listeners:
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        WorldGenManager.registerPlacements(modEventBus);
        ObjectManager.registerContainers(modEventBus);
        //ItemManager.registerItem(modEventBus);
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
        ItemManager.register(modEventBus);

        // Game Event Listeners:
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(CommandManager.getInstance());
        MinecraftForge.EVENT_BUS.register(new GameEventListener());
        MinecraftForge.EVENT_BUS.register(SpawnerEventListener.getInstance());
        MinecraftForge.EVENT_BUS.register(MobEventManager.getInstance());
        MinecraftForge.EVENT_BUS.register(MobEventListener.getInstance());
        MinecraftForge.EVENT_BUS.register(new WorldGenerator());

        // Network:
        PACKET_MANAGER.register();
        // Load Content:
        this.loadContent();

    }

    /*public static final RegistryObject<EntityType> ABIA = ENTITY_TYPES.register("abia", LycanitesMobs::getAbia);
        public static EntityType<?> getAbia() {
            EntityType.EntityFactory factory = EntityAbaia::new;
            return EntityType.Builder.of(factory, MobCategory.MONSTER)
                    .setTrackingRange(10)
                    .setUpdateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(2f, 1f).build("abia");
        }*/
    public static void registryObjects() {
        // Blocks and Items:
        ItemManager.getInstance().startup(modInfo);
        // Equipment Parts:
        EquipmentPartManager.getInstance().loadAllFromJson(modInfo);
        // Creatures:
        CreatureManager.getInstance().startup(modInfo);
        // Projectiles:
        ProjectileManager.getInstance().startup(modInfo);
    }

    /**
     * Some initialization must be done after registries have been initialized so we do them on common setup
     */
    public static void loadValues() {
        var creatureManager = CreatureManager.getInstance();
        for (CreatureInfo creatureInfo : creatureManager.creatures.values()) {
            ResourceLocation id = new ResourceLocation(LycanitesMobs.MODID, creatureInfo.getName());
            EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(id);
            if (t == null || !id.equals(ForgeRegistries.ENTITY_TYPES.getKey(t))) continue;
            creatureInfo.entityType = (EntityType<? extends LivingEntity>) t;
            EntityFactory.getInstance().addEntityType(creatureInfo.entityType, creatureInfo.entityConstructor, creatureInfo.getName());
            creatureInfo.init();
        }
        ProjectileManager.getInstance().bindRegisteredTypes();
    }


    /**
     * Prints an info message into the console.
     *
     * @param key     The debug config key to use, if empty, the message is always printed.
     * @param message The message to print.
     */
    public static void logInfo(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.info("[LycanitesMobs] [Info] [" + key + "] " + message);
        }
    }

    /**
     * Prints an info debug into the console.
     *
     * @param key     The debug config key to use, if empty, the message is always printed.
     * @param message The message to print.
     */
    public static void logDebug(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.debug("[LycanitesMobs] [Debug] [" + key + "] " + message);
        }
    }

    /**
     * Prints an info warning into the console.
     *
     * @param key     The debug config key to use, if empty, the message is always printed.
     * @param message The message to print.
     */
    public static void logWarning(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.warn("[LycanitesMobs] [WARNING] [" + key + "] " + message);
        }
    }

    /**
     * Prints an error message into the console.
     *
     * @param message The error message to print.
     */
    public static void logError(String message) {
        LOGGER.error("[LycanitesMobs] " + message);
    }

    // Content Loading:
    public void loadContent() {
        // Elements:
        ElementManager.getInstance().loadAllFromJson(modInfo);

        // Vanilla Item Lists:
        ObjectLists.createVanillaLists();

        // Potion Effects:
        EffectManager = new EffectManager();

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


        //Registry Objects
        LycanitesMobs.registryObjects();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        configReady = true;
        ObjectManager.setCurrentModInfo(modInfo);
        this.loadConfigs();
        // Fix Health Limit:
        fixMaxHealth();
        // Mod Support:
        DLDungeons.init();
        loadValues();
    }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event) {

        ClientManager.getInstance().initLanguageManager();
        ClientManager.getInstance().registerEvents();
        TextureManager.getInstance().createTextures(modInfo);
        ModelManager.getInstance().createModels();
        ClientManager.getInstance().initRenderRegister();
        ClientManager.getInstance().initBlockRenderTypes();
        //New menu screen registration

        event.enqueueWork(
                () -> {
                    MenuScreens.register(CreatureContainer.TYPE, CreatureInventoryScreen::new);
                    MenuScreens.register(SummoningPedestalContainer.TYPE, SummoningPedestalScreen::new);
                    MenuScreens.register(EquipmentForgeContainer.TYPE, EquipmentForgeScreen::new);
                    MenuScreens.register(EquipmentInfuserContainer.TYPE, EquipmentInfuserScreen::new);
                    MenuScreens.register(EquipmentStationContainer.TYPE, EquipmentStationScreen::new);
                }
        );


    }

    @SubscribeEvent
    public void serverStarting(final ServerStartingEvent event) {

    }

    public void loadConfigs() {
        //ItemManager.getInstance().loadConfig();
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
}
