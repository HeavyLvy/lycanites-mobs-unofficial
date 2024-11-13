package com.lycanitesmobs;

import com.lycanitesmobs.core.EffectBase;
import com.lycanitesmobs.core.block.CustomFluid;
import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.container.*;
import com.lycanitesmobs.core.info.ItemInfo;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.tileentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.util.LMHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.lycanitesmobs.core.item.LMItemsGroup.itemNames;


@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class ObjectManager {
    // ========== Items/Blocks ==========
    public static final Map<String, Supplier<? extends Item>> items = new HashMap<>();
    public static final Map<String, Lazy<? extends BlockItem>> blockItems = new HashMap<>();
    public static final Map<String, Lazy<? extends Block>> blocks = new HashMap<>();
    public static final Map<String, Lazy<? extends LiquidBlock>> liquidBlocks = new HashMap<>();
    // ========== Entities ==========
    public static final Map<String, Lazy<? extends EntityType<?>>> entityTypes = new HashMap<>();
    // ========== Containers ==========
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, LycanitesMobs.MODID);
    public static ObjectManager INSTANCE;
    public static ModInfo currentModInfo;
    public static Map<String, Class> tileEntities = new HashMap<>();
    public static Map<Class<? extends BlockEntity>, BlockEntityType<? extends BlockEntity>> tileEntityTypes = new HashMap<>();
    public static Map<String, ForgeFlowingFluid> fluids = new HashMap<>();
    public static Map<Block, Item> buckets = new HashMap<>();
    // ========== Creative Mode Tabs ==========
    public static Map<Item, ModInfo> itemGroups = new HashMap<>();
    // ========== Damage Sources/Effects ==========
    public static Map<String, DamageSource> damageSources = new HashMap<>();
    public static Map<String, EffectBase> effects = new HashMap<>();
    // ========== Sounds ==========
    public static Map<String, ResourceLocation> soundNames = new HashMap<>();
    public static Map<String, Lazy<? extends SoundEvent>> sounds = new HashMap<>();
    public static Map<String, Class<? extends Entity>> specialEntities = new HashMap<>();
    public static Map<Class<? extends Entity>, Constructor<? extends Entity>> specialEntityConstructors = new HashMap<>();
    public static Map<Class<? extends Entity>, EntityType<? extends Entity>> specialEntityTypes = new HashMap<>();

    /**
     * The next available network id for special entities to register by.
     **/
    protected static int nextSpecialEntityNetworkId = 0;

    public static ObjectManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ObjectManager();
        return INSTANCE;
    }

    // ==================================================
    //                        Setup
    // ==================================================
    public static void setCurrentModInfo(ModInfo group) {
        currentModInfo = group;
    }

    /**
     * Generates the next available special entity network id to register with.
     *
     * @return The next special entity network id.
     */
    public static int getNextSpecialEntityNetworkId() {
        return nextSpecialEntityNetworkId++;
    }

    // ==================================================
    //                        Add
    // ==================================================
    // ========== Block ==========
    public static void addBlock(String name, Supplier<? extends Block> block, boolean isLiquid) {
        if (isLiquid) {
            liquidBlocks.put(name, Lazy.of(() -> (LiquidBlock) block.get()));
        } else {
            Item.Properties blockItemProperties = new Item.Properties();
            blocks.put(name, Lazy.of(block));
            blockItems.put(name, () -> new BlockItem(blocks.get(name).get(), blockItemProperties));
        }
    }

    // ========== Fluid ==========
    public static Fluid addFluid(String name, ForgeFlowingFluid fluid) {
        fluids.put(name, fluid);
        return fluid;
    }

    // ========== Item ==========
    public static void addItem(String name, Supplier<? extends Item> itemSupplier) {
        RegistryObject<Item> item = LycanitesMobs.ITEMS.register(name, itemSupplier);
        //LycanitesMobs.ITEMS.register(name,item);
        items.put(name, item);
        itemNames.add(name);
    }

    // ========== Tile Entity ==========
    public static Class addTileEntity(String name, Class tileEntityClass) {
        name = name.toLowerCase();
        tileEntities.put(name, tileEntityClass);
        return tileEntityClass;
    }

    // ========== Potion Effect ==========
    public static EffectBase addPotionEffect(String name, boolean isBad, int color, boolean goodEffect) {
        EffectBase effect = new EffectBase(name, isBad, color);
        effects.put(name, effect);
        ObjectLists.addEffect(goodEffect ? "buffs" : "debuffs", effect, name);

        return effect;
    }

    // ========== Special Entity ==========
    public static void addSpecialEntity(String name, Class<? extends Entity> entityClass, Constructor<? extends Entity> specialEntityConstructor) {
        specialEntities.put(name, entityClass);
        specialEntityConstructors.put(entityClass, specialEntityConstructor);
    }

    // ========== Damage Source ==========
    public static void addDamageSource(String name, DamageSource damageSource) {
        name = name.toLowerCase();
        damageSources.put(name, damageSource);
    }

    // ========== Sound ==========
    public static void addSound(String name, String path) {
        name = name.toLowerCase();
        ResourceLocation resourceLocation = new ResourceLocation(LycanitesMobs.MODID, path);
        soundNames.put(name, resourceLocation);
        sounds.put(name, Lazy.of(() -> SoundEvent.createVariableRangeEvent(resourceLocation)));
    }

    // ==================================================
    //                        Get
    // ==================================================
    // ========== Block ==========
    public static Block getBlock(String name) {
        name = name.toLowerCase();
        if (!blocks.containsKey(name))
            return null;
        return blocks.get(name).get();
    }

    public static LiquidBlock getFluidBlock(String name) {
        name = name.toLowerCase();
        if (!liquidBlocks.containsKey(name))
            return null;
        return liquidBlocks.get(name).get();
    }

    // ========== Item ==========
    public static Item getItem(String name) {
        name = name.toLowerCase();
        if (!items.containsKey(name))
            return null;
        return items.get(name).get();
    }

    // ========== Fluid ==========
    public static ForgeFlowingFluid getFluid(String name) {
        name = name.toLowerCase();
        if (!fluids.containsKey(name))
            return null;
        return fluids.get(name);
    }

    // ========== Tile Entity ==========
    public static Class getTileEntity(String name) {
        name = name.toLowerCase();
        if (!tileEntities.containsKey(name)) return null;
        return tileEntities.get(name);
    }

    // ========== Potion Effect ==========
    public static EffectBase getEffect(String name) {
        name = name.toLowerCase();
        if (!effects.containsKey(name)) return null;
        return effects.get(name);
    }

    // ========== Damage Source ==========
    public static DamageSource getDamageSource(String name) {
        name = name.toLowerCase();
        if (!damageSources.containsKey(name)) return null;
        return damageSources.get(name);
    }

    // ========== Sound ==========
    public static SoundEvent getSound(String name) {
        name = name.toLowerCase();
        if (!sounds.containsKey(name))
            return null;
        return sounds.get(name).get();
    }


    @Nonnull
    public static EntityType<? extends LivingEntity> getEntityType(String name) {
        LMHelperClass.environmentRunnable(false, () -> {
            LycanitesMobs.logInfo("", "EntityType: " + name);
        });
        return (EntityType<? extends LivingEntity>) entityTypes.get(name).get();
    }

    public static void addEntityType(String name, EntityType.Builder<? extends LivingEntity> builder) {
        entityTypes.put(name, () -> builder.build(name));

    }

    public static void registerContainers(IEventBus event) {

        MENUS.register(event);
        MENUS.register("creature", () -> CreatureContainer.TYPE);
        MENUS.register("summoning_pedestal", () -> SummoningPedestalContainer.TYPE);
        MENUS.register("equipment_forge", () -> EquipmentForgeContainer.TYPE);
        MENUS.register("equipment_infuser", () -> EquipmentInfuserContainer.TYPE);
        MENUS.register("equipment_station", () -> EquipmentStationContainer.TYPE);
    }

    // ==================================================
    //                  Registry Events
    // ==================================================
    // ========== Entities ==========
    @SubscribeEvent
    public void registerEntities(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_TYPES,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends EntityType<?>>> entityType : entityTypes.entrySet()) {
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, entityType.getKey()), entityType.getValue().get());
                    }
                }
        );

    }
    // ========== Blocks ==========

    /**
     * Registers blocks and their corresponding block items.
     * <p>
     * Block items must be registered in the same Registry Event after blocks
     * to ensure that a non-null block is passed to the BlockItem constructor.
     * This sequence guarantees that the block references used in block items
     * are fully initialized and registered, avoiding any potential issues
     * with null references or unregistered blocks.
     * </p>
     *
     * @param event The register event for blocks and items.
     */
    @SubscribeEvent
    public void registerBlocks(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS,
                helper -> {
                    for (Supplier<? extends Block> block : blocks.values()) {
                        BlockTypeGetter getter = (BlockTypeGetter) block.get();
                        LycanitesMobs.LOGGER.info("Registering block: " + getter.getRegistryName());
                        helper.register(getter.getRegistryName(), block.get());
                    }
                }
        );
        event.register(ForgeRegistries.Keys.ITEMS,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends BlockItem>> entry : ObjectManager.blockItems.entrySet()) {
                        String name = entry.getKey();
                        Supplier<? extends BlockItem> blockItem = entry.getValue();
                        LycanitesMobs.logDebug("Item", "Registering item block: " + name);
                        if (name == null) {
                            LycanitesMobs.logWarning("", "Block Item: " + name + " has no Registry Name!");
                            continue;
                        }
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, name), blockItem.get());
                    }
                }
        );
    }

    // ========== Items ==========
    @SubscribeEvent
    public void registerItems(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS,
                helper -> {
                    //General Items
                    for (ItemInfo itemInfo : ItemManager.items.values()) {
                        if (ObjectManager.items.containsKey(itemInfo.getName())) {
                            LycanitesMobs.LOGGER.info("Tried registering item in Object Manager's item list, skipping: " + itemInfo.getName());
                            return;
                        }

                        LycanitesMobs.logDebug("Item", "Registering general item: " + itemInfo.getName());
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, itemInfo.getItem().itemName), itemInfo.getItem());
                    }
                    //Special Items
                    for (Supplier<? extends Item> item : items.values()) {
                        for (String name : items.keySet()) {
                            if (ItemManager.items.containsKey(name)) {
                                LycanitesMobs.LOGGER.info("Tried registering item in Item Manager's item list, skipping: " + name);
                                return;
                            }
                            LycanitesMobs.logDebug("Item", "Registering special item: " + name);
                            helper.register(new ResourceLocation(LycanitesMobs.MODID, name), item.get());
                        }
                    }
                }
        );

    }

    // ========== Sounds ==========

    /**
     * Sounds are for some reason being registered elsewhere(assuming during the process of
     * adding them to the sounds map with SoundEvent.createVariableRangeEvent(resourceLocation)
     * so this is not needed. Kept for future organization.
     *
     * @param event
     */
    /*@SubscribeEvent
    public void registerSounds(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.SOUND_EVENTS,
                helper -> {
                    for (Lazy<? extends SoundEvent> soundEvent : sounds.values()) {
                        for (ResourceLocation registryName : soundNames.values()) {
                            if (registryName == null) {
                                LycanitesMobs.logWarning("", "Sound: " + soundEvent + " has no Registry Name!");
                            }
                            helper.register(registryName, soundEvent.get());
                        }
                    }
                }
        );
    }*/

    // ========== Potions ==========
    @SubscribeEvent
    public void registerEffects(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.MOB_EFFECTS,
                helper -> {
                    for (EffectBase effect : effects.values()) {
                        helper.register(effect.getRegistryName(), effect);
                    }
                }
        );

    }

    // ========== Fluids ==========
    @SubscribeEvent
    public void registerFluids(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.FLUIDS,
                helper -> {
                    for (Fluid fluid : fluids.values()) {
                        if (fluid instanceof CustomFluid customFluid) {
                            helper.register(customFluid.getRegistryName(), fluid);
                        }

                    }
                }
        );
        event.register(ForgeRegistries.Keys.FLUID_TYPES,
                helper -> {
                    for (Fluid fluid : fluids.values()) {
                        if (fluid.getFluidType() instanceof BaseFluidType fluidType) {
                            helper.register(fluidType.getRegistryName(), fluidType);
                        }
                    }
                }
        );

    }

    // ========== Tile Entities ==========
    //@SubscribeEvent
    public void registerTileEntities(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES,
                helper -> {
                    BlockEntityType<TileEntitySummoningPedestal> summoningPedestalType = BlockEntityType.Builder.of(TileEntitySummoningPedestal::new,
                            getBlock("summoningpedestal")
                    ).build(null);
                    ResourceLocation summoningpedestalLocation = new ResourceLocation(LycanitesMobs.MODID, "summoningpedestal");
                    helper.register(summoningpedestalLocation, summoningPedestalType);
                    tileEntityTypes.put(TileEntitySummoningPedestal.class, summoningPedestalType);

                    BlockEntityType<TileEntityEquipmentForge> equipmentForgeType = BlockEntityType.Builder.of(TileEntityEquipmentForge::new,
                            getBlock("equipmentforge_lesser"),
                            getBlock("equipmentforge_greater"),
                            getBlock("equipmentforge_master")
                    ).build(null);
                    ResourceLocation equipmentforgeLocation = new ResourceLocation(LycanitesMobs.MODID, "equipmentforge");
                    helper.register(equipmentforgeLocation, equipmentForgeType);
                    tileEntityTypes.put(TileEntityEquipmentForge.class, equipmentForgeType);

                    BlockEntityType<EquipmentInfuserTileEntity> equipmentInfuserType = BlockEntityType.Builder.of(EquipmentInfuserTileEntity::new,
                            getBlock("equipment_infuser")
                    ).build(null);
                    ResourceLocation equipment_infuserLocation = new ResourceLocation(LycanitesMobs.MODID, "equipment_infuser");
                    helper.register(equipment_infuserLocation, equipmentInfuserType);
                    tileEntityTypes.put(EquipmentInfuserTileEntity.class, equipmentInfuserType);

                    BlockEntityType<EquipmentStationTileEntity> equipmentStationType = BlockEntityType.Builder.of(EquipmentStationTileEntity::new,
                            getBlock("equipment_station")
                    ).build(null);
                    ResourceLocation equipment_stationLocation = new ResourceLocation(LycanitesMobs.MODID, "equipment_station");
                    helper.register(equipment_stationLocation, equipmentStationType);
                    tileEntityTypes.put(EquipmentStationTileEntity.class, equipmentStationType);
                });
    }
}
