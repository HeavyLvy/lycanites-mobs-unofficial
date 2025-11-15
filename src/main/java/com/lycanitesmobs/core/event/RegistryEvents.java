package com.lycanitesmobs.core.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.blockentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.blockentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.blockentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.command.*;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.item.ItemInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.effect.EffectBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.fluid.CustomFluid;
import com.lycanitesmobs.core.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Map;
import java.util.function.Supplier;

import static com.lycanitesmobs.core.manager.ObjectManager.*;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID)
public class RegistryEvents {
    public static RegistryEvents INSTANCE;

    public static RegistryEvents getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RegistryEvents();
        return INSTANCE;
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
        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            for (Supplier<? extends Block> block : blocks.values()) {
                BlockTypeGetter getter = (BlockTypeGetter) block.get();
                helper.register(getter.getRegistryName(), block.get());
            }
            for (Lazy<? extends LiquidBlock> lazy : liquidBlocks.values()) {
                LiquidBlock lb = lazy.get();
                BlockTypeGetter getter = (BlockTypeGetter) lb;
                helper.register(getter.getRegistryName(), lb);
            }
        });
        event.register(ForgeRegistries.Keys.ITEMS,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends BlockItem>> entry : ObjectManager.blockItems.entrySet()) {
                        String name = entry.getKey();
                        Supplier<? extends BlockItem> blockItem = entry.getValue();
                        LMHelperClass.logDebug("Item", "Registering item block: " + name);
                        if (name == null) {
                            LMHelperClass.logWarning("", "Block Item: " + name + " has no Registry Name!");
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

                        LMHelperClass.logDebug("Item", "Registering general item: " + itemInfo.getName());
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, itemInfo.getItem().itemName), itemInfo.getItem());
                    }
                    //Special Items
                    for (Supplier<? extends Item> item : items.values()) {
                        for (String name : items.keySet()) {
                            if (ItemManager.items.containsKey(name)) {
                                LycanitesMobs.LOGGER.info("Tried registering item in Item Manager's item list, skipping: " + name);
                                return;
                            }
                            LMHelperClass.logDebug("Item", "Registering special item: " + name);
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
        event.register(ForgeRegistries.Keys.FLUIDS, helper -> {
            FluidManager.getInstance().defineFluids();
            for (Fluid fluid : fluids.values()) {
                if (fluid instanceof CustomFluid customFluid) {
                    helper.register(customFluid.getRegistryName(), fluid);
                }
            }
        });
        event.register(ForgeRegistries.Keys.FLUID_TYPES, helper -> {
            java.util.Set<ResourceLocation> seen = new java.util.HashSet<>();
            for (Fluid fluid : fluids.values()) {
                FluidType ft = fluid.getFluidType();
                if (ft instanceof BaseFluidType bft) {
                    ResourceLocation id = bft.getRegistryName();
                    if (seen.add(id)) {
                        helper.register(id, bft);
                    }
                }
            }
        });
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

    @SubscribeEvent
    public void registerEntityAttributes(EntityAttributeCreationEvent event) {
        for (CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
            event.put(creatureInfo.getEntityType(), BaseCreatureEntity.registerCustomAttributes().build());
        }
    }

    @SubscribeEvent
    public void registerAttributes(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ATTRIBUTES,
                helper -> {
                    helper.register(new ResourceLocation(LycanitesMobs.MODID, "defense"), BaseCreatureEntity.DEFENSE);
                    helper.register(new ResourceLocation(LycanitesMobs.MODID, "ranged_speed"), BaseCreatureEntity.RANGED_SPEED);
                }
        );
    }

    @SubscribeEvent
    public void registerStats(RegisterEvent event) {
        StatManager.getInstance().createStatTypes();
        for (var entry : StatManager.getInstance().statTypes.entrySet()) {
            event.register(ForgeRegistries.Keys.STAT_TYPES,
                    helper -> {
                        helper.register(new ResourceLocation(LycanitesMobs.MODID, entry.getKey()), entry.getValue());
                    }
            );
        }
    }

    @SubscribeEvent
    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("lm")
                        .then(CreaturesCommand.register())
                        .then(BeastiaryCommand.register())
                        .then(SpawnersCommand.register())
                        .then(SpawnerCommand.register())
                        .then(MobEventsCommand.register())
                        .then(MobEventCommand.register())
                        .then(EquipmentCommand.register())
                        .then(DungeonsCommand.register())
                        .then(DebugCommand.register())
        );
    }
}
