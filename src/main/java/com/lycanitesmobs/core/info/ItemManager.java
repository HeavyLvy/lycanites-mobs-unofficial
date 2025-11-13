package com.lycanitesmobs.core.info;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.FileLoader;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.StreamLoader;
import com.lycanitesmobs.core.block.*;
import com.lycanitesmobs.core.block.building.HiveBlock;
import com.lycanitesmobs.core.block.effect.*;
import com.lycanitesmobs.core.block.fluid.*;
import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.item.*;
import com.lycanitesmobs.core.item.consumable.*;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.special.ItemSoulContract;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import com.lycanitesmobs.core.item.special.ItemSoulkey;
import com.lycanitesmobs.core.item.special.ItemSoulstone;
import com.lycanitesmobs.core.item.summoningstaff.*;
import com.lycanitesmobs.core.util.LMHelperClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class ItemManager extends JSONLoader {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LycanitesMobs.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LycanitesMobs.MODID);
    // Creative Tabs:
    public static final CreativeModeTab.Builder itemsGroup = LMItemsGroup.getBuilder();
    public static final RegistryObject<CreativeModeTab> itemTab = TABS.register(LycanitesMobs.MODID + ".items", itemsGroup::build);
    public static final CreativeModeTab.Builder blocksGroup = LMBlocksGroup.getBuilder();
    public static final RegistryObject<CreativeModeTab> blockTab = TABS.register(LycanitesMobs.MODID + ".blocks", blocksGroup::build);
    public static final CreativeModeTab.Builder creaturesGroups = LMCreaturesGroup.getBuilder();
    public static final RegistryObject<CreativeModeTab> creaturesTab = TABS.register(LycanitesMobs.MODID + ".creatures", creaturesGroups::build);
    public static final CreativeModeTab.Builder chargesGroup = LMChargesGroup.getBuilder();
    public static final RegistryObject<CreativeModeTab> chargesTab = TABS.register(LycanitesMobs.MODID + ".charges", chargesGroup::build);
    public static final CreativeModeTab.Builder equipmentPartsGroup = LMEquipmentPartsGroup.getBuilder();
    public static final RegistryObject<CreativeModeTab> equipmentPartsTab = TABS.register(LycanitesMobs.MODID + ".equipmentparts", equipmentPartsGroup::build);
    public static final Map<String, Item.Properties> registryItems = new HashMap<>();
    public static ItemManager INSTANCE;
    public static Map<String, ItemInfo> items = new HashMap<>();

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }

    /**
     * A list of blocks that need to use the cutout renderer.
     **/
    public List<Block> cutoutBlocks = new ArrayList<>();
    /**
     * A list of fluids blocks for reference (the worldgen manager uses this to automatically create lakes, etc).
     **/
    public Map<String, BaseFluidBlock> worldgenFluidBlocks = new HashMap<>();
    /**
     * A list of mod groups that have loaded with this manager.
     **/
    public List<ModInfo> loadedGroups = new ArrayList<>();
    /**
     * Handles all global item general config settings.
     **/
    public ItemConfig config;

    /**
     * Returns the main Item Manager instance or creates it and returns it.
     **/
    public static ItemManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemManager();
        }
        return INSTANCE;
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        loadItems();
        loadAllFromJson(modInfo);
    }

    /**
     * Loads all JSON Items.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        if (!this.loadedGroups.contains(modInfo)) {
            this.loadedGroups.add(modInfo);
        }
        this.loadAllJson(modInfo, "Items", "items", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
        LycanitesMobs.logDebug("Items", "Complete! " + this.items.size() + " JSON Items Loaded In Total.");
    }

    /*private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, LycanitesMobs.MODID);


    public static void registerItem(IEventBus event) {
        ITEM_DEFERRED_REGISTER.register(event);
        ITEM_DEFERRED_REGISTER.register("soulgazer", () -> new ItemSoulgazer(new Item.Properties()));
    }*/


    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        ItemInfo itemInfo = new ItemInfo(modInfo);
        itemInfo.loadFromJSON(json);
        if (Objects.equals(itemInfo.name, "unamed_item")) return;
        this.items.put(itemInfo.name, itemInfo);
    }

    /**
     * Called during early start up, loads all global configs into this manager.
     **/
    public void loadConfig() {
        ItemConfig.loadGlobalSettings();
    }


    /**
     * Called during early start up, loads all non-json items.
     **/
    public void loadItems() {
        Item.Properties itemProperties = new Item.Properties();
        Item.Properties itemPropertiesNoStack = new Item.Properties().stacksTo(1);

        ObjectManager.addItem("soulgazer", () -> new ItemSoulgazer(itemPropertiesNoStack));
        ObjectManager.addItem("soul_contract", () -> new ItemSoulContract(itemPropertiesNoStack));
        ObjectManager.addItem("mobtoken", () -> new ItemMobToken(new Item.Properties()));
        ObjectManager.addItem("soulstone", () -> new ItemSoulstone(itemProperties, null));
        // Equipment Pieces:
        Item.Properties equipmentProperties = new Item.Properties().stacksTo(1).setNoRepair();
        ObjectManager.addItem("equipment", () -> new ItemEquipment(equipmentProperties));

        // Keys:
        ObjectManager.addItem("soulkey", () -> new ItemSoulkey(itemProperties, "soulkey", 0));
        ObjectManager.addItem("soulkeydiamond", () -> new ItemSoulkey(itemProperties, "soulkeydiamond", 1));
        ObjectManager.addItem("soulkeyemerald", () -> new ItemSoulkey(itemProperties, "soulkeyemerald", 2));


        // Buff Items:
        ObjectManager.addItem("immunizer", () -> new ItemImmunizer(itemProperties));
        ObjectManager.addItem("cleansingcrystal", () -> new ItemCleansingCrystal(itemProperties));


        // Seasonal Items:
        ObjectManager.addItem("halloweentreat", () -> new ItemHalloweenTreat(itemProperties));
        ObjectManager.addItem("wintergift", () -> new ItemWinterGift(itemProperties));
        ObjectManager.addItem("wintergiftlarge", () -> new ItemWinterGiftLarge(itemProperties));


        // Special:
        ObjectManager.addItem("frostyfur", () -> new ItemBlockPlacer(itemProperties, "frostyfur", "frostcloud"));
        ObjectManager.addItem("poisongland", () -> new ItemBlockPlacer(itemProperties, "poisongland", "poisoncloud"));
        ObjectManager.addItem("geistliver", () -> new ItemBlockPlacer(itemProperties, "geistliver", "shadowfire"));

        BlockMaker.addDungeonBlocks("lush");
        //if (true) return;
        //.tab(this.itemsGroup)
        // Summoning Staves:
        Item.Properties summoningStaffProperties = new Item.Properties().stacksTo(1).durability(500);
        ObjectManager.addItem("summoningstaff", () -> new ItemStaffSummoning(summoningStaffProperties, "summoningstaff", "summoningstaff"));
        ObjectManager.addItem("stablesummoningstaff", () -> new ItemStaffStable(summoningStaffProperties, "stablesummoningstaff", "staffstable"));
        ObjectManager.addItem("bloodsummoningstaff", () -> new ItemStaffBlood(summoningStaffProperties, "bloodsummoningstaff", "staffblood"));
        ObjectManager.addItem("sturdysummoningstaff", () -> new ItemStaffSturdy(summoningStaffProperties, "sturdysummoningstaff", "staffsturdy"));
        ObjectManager.addItem("savagesummoningstaff", () -> new ItemStaffSavage(summoningStaffProperties, "savagesummoningstaff", "staffsavage"));
        // Utilities:
        ObjectManager.addBlock("summoningpedestal", () -> new BlockSummoningPedestal(Block.Properties.of().sound(SoundType.METAL).strength(5, 10)), false);
        ObjectManager.addBlock("equipmentforge_lesser", () -> new BlockEquipmentForge(Block.Properties.of().sound(SoundType.WOOD).strength(5, 10), 1), false);
        ObjectManager.addBlock("equipmentforge_greater", () -> new BlockEquipmentForge(Block.Properties.of().sound(SoundType.STONE).strength(5, 20), 2), false);
        ObjectManager.addBlock("equipmentforge_master", () -> new BlockEquipmentForge(Block.Properties.of().sound(SoundType.METAL).strength(5, 1000), 3), false);
        ObjectManager.addBlock("equipment_infuser", () -> new EquipmentInfuserBlock(Block.Properties.of().sound(SoundType.METAL).strength(5, 1000)), false);
        ObjectManager.addBlock("equipment_station", () -> new EquipmentStationBlock(Block.Properties.of().sound(SoundType.METAL).strength(5, 1000)), false);

        // Building Blocks:

        BlockMaker.addDungeonBlocks("desert");
        BlockMaker.addDungeonBlocks("shadow");
        BlockMaker.addDungeonBlocks("demon");
        BlockMaker.addDungeonBlocks("aberrant");
        BlockMaker.addDungeonBlocks("ashen");
        BlockMaker.addDungeonBlocks("stream");
        ObjectManager.addBlock("soulcubedemonic", () -> new BlockBase(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubedemonic"), false);
        ObjectManager.addBlock("soulcubeundead", () -> new BlockBase(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubeundead"), false);
        ObjectManager.addBlock("soulcubeaberrant", () -> new BlockBase(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubeaberrant"), false);
        ObjectManager.addBlock("propolis", () -> new HiveBlock(Block.Properties.of().mapColor(MapColor.CLAY).sound(SoundType.WET_GRASS).strength(0.6F).randomTicks(), "propolis"), false);
        ObjectManager.addBlock("veswax", () -> new HiveBlock(Block.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0.6F).randomTicks(), "veswax"), false);


        // Effect Blocks:
        Block.Properties fireProperties = Block.Properties.of().mapColor(MapColor.FIRE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        Block.Properties brightFireProperties = Block.Properties.of().mapColor(MapColor.FIRE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion().lightLevel((BlockState blockState) -> {
            return 15;
        });
        ObjectManager.addSound("frostfire", "block.frostfire");
        ObjectManager.addBlock("frostfire", () -> new BlockFrostfire(fireProperties), false);
        ObjectManager.addSound("icefire", "block.icefire");
        ObjectManager.addBlock("icefire", () -> new BlockIcefire(fireProperties), false);
        ObjectManager.addSound("hellfire", "block.hellfire");
        ObjectManager.addBlock("hellfire", () -> new BlockHellfire(brightFireProperties), false);
        ObjectManager.addSound("doomfire", "block.doomfire");
        ObjectManager.addBlock("doomfire", () -> new BlockDoomfire(brightFireProperties), false);
        ObjectManager.addSound("primefire", "block.primefire");
        ObjectManager.addBlock("primefire", () -> new BlockPrimefire(brightFireProperties), false);
        ObjectManager.addSound("scorchfire", "block.scorchfire");
        ObjectManager.addBlock("scorchfire", () -> new BlockScorchfire(brightFireProperties), false);
        ObjectManager.addSound("shadowfire", "block.shadowfire");
        ObjectManager.addBlock("shadowfire", () -> new BlockShadowfire(fireProperties), false);
        ObjectManager.addSound("smitefire", "block.smitefire");
        ObjectManager.addBlock("smitefire", () -> new BlockSmitefire(brightFireProperties), false);

        Block.Properties cloudProperties = Block.Properties.of().mapColor(MapColor.NONE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        ObjectManager.addSound("frostcloud", "block.frostcloud");
        ObjectManager.addBlock("frostcloud", () -> new BlockFrostCloud(cloudProperties), false);
        ObjectManager.addSound("poisoncloud", "block.poisoncloud");
        ObjectManager.addBlock("poisoncloud", () -> new BlockPoisonCloud(cloudProperties), false);
        ObjectManager.addSound("poopcloud", "block.poopcloud");
        ObjectManager.addBlock("poopcloud", () -> new BlockPoopCloud(cloudProperties), false);

        Block.Properties webProperties = Block.Properties.of().mapColor(MapColor.WOOL).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        ObjectManager.addBlock("quickweb", () -> new BlockQuickWeb(webProperties), false);
        ObjectManager.addBlock("frostweb", () -> new BlockFrostweb(webProperties), false);


        // Fluids:
        Block.Properties waterBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable();
        Block.Properties waterBrightBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable().lightLevel((BlockState blockState) -> {
            return 10;
        });
        Block.Properties lavaBlockProperties = Block.Properties.of().mapColor(MapColor.FIRE).noCollission().randomTicks().strength(100).noLootTable().lightLevel((BlockState blockState) -> {
            return 15;
        });
       /* this.addFluid("ooze", 0x009F9F, 3000, 3000, 0, 10, false, OozeFluidBlock.class, waterBrightBlockProperties, "frost", false, true);
        this.addFluid("rabbitooze", 0x00AFAF, 3000, 3000, 0, 10, true, OozeFluidBlock.class, waterBrightBlockProperties, "frost", false, false);
        this.addFluid("moglava", 0xFF5722, 3000, 5000, 1100, 15, true, MoglavaFluidBlock.class, lavaBlockProperties, "lava", false, true);
        this.addFluid("acid", 0x8BC34A, 1000, 10, 40, 10, false, AcidFluidBlock.class, waterBrightBlockProperties, "acid", true, true);
        this.addFluid("sharacid", 0x8BB35A, 1000, 10, 40, 10, true, AcidFluidBlock.class, waterBrightBlockProperties, "acid", false, false);
        this.addFluid("poison", 0x9C27B0, 1000, 8, 20, 0, false, PoisonFluidBlock.class, waterBlockProperties, "poison", false, true);
        this.addFluid("vesspoison", 0xAC27A0, 1000, 8, 20, 0, true, PoisonFluidBlock.class, waterBlockProperties, "poison", false, false);
        this.addFluid("veshoney", 0xCEBC39, 4000, 4000, 0, 0, false, VeshoneyFluidBlock.class, waterBlockProperties, "fae", false, false);
*/
        ObjectManager.addDamageType("ooze");
        ObjectManager.addDamageType("acid");
    }

    public void defineFluids() {
        Block.Properties waterBrightBlockProperties = Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable().lightLevel(s -> 10);
        Block.Properties lavaBlockProperties = Block.Properties.of().mapColor(MapColor.FIRE).noCollission().randomTicks().strength(100).noLootTable().lightLevel(s -> 15);

        this.addFluid("ooze", 0x009F9F, 3000, 3000, 0, 10, false, OozeFluidBlock.class, waterBrightBlockProperties, "frost", false, true);
        this.addFluid("rabbitooze", 0x00AFAF, 3000, 3000, 0, 10, true, OozeFluidBlock.class, waterBrightBlockProperties, "frost", false, false);
        this.addFluid("moglava", 0xFF5722, 3000, 5000, 1100, 15, true, MoglavaFluidBlock.class, lavaBlockProperties, "lava", false, true);
        this.addFluid("acid", 0x8BC34A, 1000, 10, 40, 10, false, AcidFluidBlock.class, waterBrightBlockProperties, "acid", true, true);
        this.addFluid("sharacid", 0x8BB35A, 1000, 10, 40, 10, true, AcidFluidBlock.class, waterBrightBlockProperties, "acid", false, false);
        this.addFluid("poison", 0x9C27B0, 1000, 8, 20, 0, false, PoisonFluidBlock.class, Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable(), "poison", false, true);
        this.addFluid("vesspoison", 0xAC27A0, 1000, 8, 20, 0, true, PoisonFluidBlock.class, Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable(), "poison", false, false);
        this.addFluid("veshoney", 0xCEBC39, 4000, 4000, 0, 0, false, VeshoneyFluidBlock.class, Block.Properties.of().mapColor(MapColor.WATER).noCollission().randomTicks().strength(100).noLootTable(), "fae", false, false);
    }

    public void addFluid(String fluidName, int fluidColor, int density, int viscosity, int temperature, int luminosity, boolean multiply, Class<? extends BaseFluidBlock> blockClass, BlockBehaviour.Properties blockProperties, String elementName, boolean destroyItems, boolean worldgen) {
        ElementInfo element = ElementManager.getInstance().getElement(elementName);
        Vector3f fogColor = new Vector3f(((fluidColor >> 16) & 0xFF) / 255f, ((fluidColor >> 8) & 0xFF) / 255f, (fluidColor & 0xFF) / 255f);
        int tickRate = (viscosity >= 4000) ? 30 : 5;

        FluidBuilder builder = new FluidBuilder(fluidName, new ResourceLocation(LycanitesMobs.MODID, fluidName));
        FluidType fluidType = builder.createFluidType(
                new ResourceLocation(LycanitesMobs.MODID, "block/" + fluidName + "_still"),
                new ResourceLocation(LycanitesMobs.MODID, "block/" + fluidName + "_flowing"),
                null,
                fluidColor, fogColor, density, viscosity, temperature, luminosity, multiply
        );
        ForgeFlowingFluid.Properties fluidProps = builder.fluidProperties(fluidType, tickRate, () -> ObjectManager.getFluidBlock(fluidName));

        ObjectManager.addFluid(fluidName, new CustomFluid.Still(fluidProps, fluidName));
        ObjectManager.addFluid(fluidName + "_flowing", new CustomFluid.Flowing(fluidProps, fluidName + "_flowing"));
        ObjectManager.addSound(fluidName, "block." + fluidName);

        Item.Properties bucketProps = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);
        ObjectManager.addItem(fluidName + "_bucket", () -> new BucketItem(builder::getStillFluid, bucketProps));

        ObjectManager.addBlock(fluidName, () -> {
            try {
                Supplier<ForgeFlowingFluid> still = builder::getStillFluid;
                Constructor<? extends BaseFluidBlock> ctor = blockClass.getConstructor(Supplier.class, BlockBehaviour.Properties.class, String.class, ElementInfo.class, boolean.class);
                BaseFluidBlock block = ctor.newInstance(still, blockProperties, fluidName, element, destroyItems);
                if (worldgen) this.worldgenFluidBlocks.put(fluidName, block);
                return block;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, true);
    }

    /**
     * Determines the Equipment Sharpness repair amount of the provided itemstack. Stack size is not taken into account.
     *
     * @param itemStack The itemstack to check the item and nbt data of.
     * @return The amount of Sharpness the provided itemstack restores.
     */
    public int getEquipmentSharpnessRepair(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        Item item = itemStack.getItem();
        Object locationObj = LMHelperClass.convertObjectToDesired(item, "resourcelocation");
        if (locationObj instanceof ResourceLocation location) {
            if (ItemConfig.lowEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.lowEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.lowEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.mediumEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.mediumEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.mediumEquipmentRepairAmount;
                    }
                }
            }

            if (ItemConfig.highEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.highEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.highEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.maxEquipmentSharpnessItems != null) {
                for (String itemId : ItemConfig.maxEquipmentSharpnessItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemEquipment.SHARPNESS_MAX;
                    }
                }
            }

        }
        return 0;
    }

    /**
     * Determines the Equipment Mana repair amount of the provided itemstack. Stack size is not taken into account.
     *
     * @param itemStack The itemstack to check the item and nbt data of.
     * @return The amount of Mana the provided itemstack restores.
     */
    public int getEquipmentManaRepair(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        Item item = itemStack.getItem();
        Object locationObj = LMHelperClass.convertObjectToDesired(item, "resourcelocation");
        if (locationObj instanceof ResourceLocation location) {
            if (item instanceof ChargeItem) {
                return ItemConfig.highEquipmentRepairAmount;
            }
            if (ItemConfig.lowEquipmentManaItems != null) {
                for (String itemId : ItemConfig.lowEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.lowEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.mediumEquipmentManaItems != null) {
                for (String itemId : ItemConfig.mediumEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.mediumEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.highEquipmentManaItems != null) {
                for (String itemId : ItemConfig.highEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemConfig.highEquipmentRepairAmount;
                    }
                }
            }
            if (ItemConfig.maxEquipmentManaItems != null) {
                for (String itemId : ItemConfig.maxEquipmentManaItems) {
                    if (new ResourceLocation(itemId).equals(location)) {
                        return ItemEquipment.MANA_MAX;
                    }
                }
            }

        }
        return 0;
    }

    public static class FluidBuilder {
        private final String baseName;
        private final ResourceLocation registryName;
        private FluidType fluidType;
        private ForgeFlowingFluid.Properties fluidProperties;

        public FluidBuilder(String fluidName, ResourceLocation registryLocation) {
            this.baseName = fluidName;
            this.registryName = registryLocation;
        }

        public FluidType createFluidType(final ResourceLocation stillTexture, final ResourceLocation flowingTexture, final ResourceLocation overlayTexture,
                                         final int tintColor, final Vector3f fogColor, int fluidDensity, int fluidViscosity, int fluidTemperature, int fluidLuminosity,
                                         boolean fluidMultiply) {
            FluidType.Properties props = FluidType.Properties.create();
            props.density(fluidDensity);
            props.viscosity(fluidViscosity);
            props.temperature(fluidTemperature);
            props.lightLevel(fluidLuminosity);
            if (fluidMultiply) props.canConvertToSource(true);
            fluidType = new BaseFluidType(registryName, stillTexture, flowingTexture, overlayTexture, tintColor, fogColor, props);
            return fluidType;
        }

        public ForgeFlowingFluid.Properties fluidProperties(FluidType fT, int fluidTickRate, Supplier<? extends LiquidBlock> blockSupplier) {
            ForgeFlowingFluid.Properties p = new ForgeFlowingFluid.Properties(() -> fT, this::getStillFluid, this::getFlowingFluid);
            p.tickRate(fluidTickRate);
            p.bucket(this::getBucketItem);
            p.block(blockSupplier);
            this.fluidProperties = p;
            return p;
        }

        public ForgeFlowingFluid getStillFluid() {
            return ObjectManager.getFluid(baseName);
        }

        public ForgeFlowingFluid getFlowingFluid() {
            return ObjectManager.getFluid(baseName + "_flowing");
        }

        public BucketItem getBucketItem() {
            return (BucketItem) ObjectManager.getItem(baseName + "_bucket");
        }

        public ForgeFlowingFluid.Properties getFluidProperties() {
            return fluidProperties;
        }

        public FluidType getFluidType() {
            return fluidType;
        }
    }

}
