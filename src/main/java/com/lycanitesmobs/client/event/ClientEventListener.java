package com.lycanitesmobs.client.event;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.creature.MinionSelectionScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.CreaturesBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.IndexBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.PetsBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.manager.KeyManager;
import com.lycanitesmobs.client.manager.TabManager;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.manager.ItemManager;
import com.lycanitesmobs.core.item.consumable.entity.ItemColorCustomSpawnEgg;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import com.lycanitesmobs.core.network.message.MessagePlayerAttack;
import com.lycanitesmobs.core.network.message.MessagePlayerControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT)
public class ClientEventListener {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        int sharpness = ItemManager.getInstance().getEquipmentSharpnessRepair(event.getItemStack());
        int mana = ItemManager.getInstance().getEquipmentManaRepair(event.getItemStack());
        if (sharpness > 0 || mana > 0) {
            event.getToolTip().add(Component.translatable("equipment.item.repair").withStyle(ChatFormatting.BLUE));
            if (sharpness > 0) {
                event.getToolTip().add(Component.translatable("equipment.sharpness").append(" " + sharpness).withStyle(ChatFormatting.BLUE));
            }
            if (mana > 0) {
                event.getToolTip().add(Component.translatable("equipment.mana").append(" " + mana).withStyle(ChatFormatting.BLUE));
            }
        }

        if (event.getItemStack().getItem() instanceof ItemSoulgazer) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.getForPlayer(event.getEntity());
            if (extendedPlayer != null && extendedPlayer.creatureStudyCooldown > 0) {
                event.getToolTip().add(Component.translatable("message.beastiary.study.cooldown").append(" " + String.format("%.0f", (float) extendedPlayer.creatureStudyCooldown / 20) + "s").withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFogDensity(ViewportEvent.RenderFog event) {
        GameRenderer fogRenderer = event.getRenderer();
        LivingEntity entityLiving = Minecraft.getInstance().player;
        if (entityLiving == null) {
            return;
        }
        if (entityLiving.isInLava() && (!entityLiving.isOnFire() || entityLiving.hasEffect(MobEffects.FIRE_RESISTANCE))) {
            event.scaleNearPlaneDistance(0.5F);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onBlockOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getBlockState().getBlock() == Blocks.FIRE && (!event.getPlayer().isOnFire() || event.getPlayer().hasEffect(MobEffects.FIRE_RESISTANCE))) {
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(KeyManager.getInstance().mc.player);
        if (playerExt == null)
            return;
        byte controlStates = 0;
        var mc = KeyManager.getInstance().mc;
        // ========== GUI Keys ==========
        // Player Inventory: Adds extra buttons to the GUI.
        if (!KeyManager.getInstance().inventoryOpen && mc.screen != null && mc.screen.getClass() == Screen.class) {
            TabManager.addTabsToInventory(mc.screen);
            KeyManager.getInstance().inventoryOpen = true;
        }
        if (KeyManager.getInstance().inventoryOpen && (mc.screen == null || mc.screen.getClass() != Screen.class)) {
            KeyManager.getInstance().inventoryOpen = false;
        }

        // Mount Inventory: Adds to control states.
        if (KeyManager.mountInventory.consumeClick()) {
            controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_INVENTORY.id;
        }

        // Beastiary Index:
        if (KeyManager.index.consumeClick()) {
            mc.setScreen(new IndexBeastiaryScreen(mc.player));
        }

        // Beastiary Creatures:
        if (KeyManager.beastiary.consumeClick()) {
            mc.setScreen(new CreaturesBeastiaryScreen(mc.player));
        }

        // Beastiary Pets:
        if (KeyManager.pets.consumeClick()) {
            mc.setScreen(new PetsBeastiaryScreen(mc.player));
        }

        // Beastiary Summoning:
        if (KeyManager.summoning.consumeClick()) {
            mc.setScreen(new SummoningBeastiaryScreen(mc.player));
        }


        if (mc.isWindowActive()) {
            // ========== HUD Controls ==========
            // Minion Selection:
            if (KeyManager.minionSelection.consumeClick()) {
                mc.setScreen(new MinionSelectionScreen(mc.player));
            }

            // ========== Action Controls ==========
            // Vanilla Jump: Adds to control states.
            if (mc.options.keyJump.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.JUMP.id;

            // Descend: Adds to control states.
            if (KeyManager.descend.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.DESCEND.id;

            // Mount Ability: Adds to control states.
            if (KeyManager.dismount.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT.id;

            // Mount Ability: Adds to control states.
            if (KeyManager.mountAbility.isDown())
                controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_ABILITY.id;

            // Attack Key Pressed:
            if (Minecraft.getInstance().options.keyAttack.isDown()) {
                controlStates += ExtendedPlayer.CONTROL_ID.ATTACK.id;
            }
        }


        // ========== Sync Controls To Server ==========
        if (controlStates == playerExt.controlStates)
            return;
        MessagePlayerControl message = new MessagePlayerControl(controlStates);
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
        playerExt.controlStates = controlStates;
    }


    /**
     * Player keyboard events.
     **/
    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.Key event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        // Minion Selection - Closes If Not Holding:
        if (event.getKey() == KeyManager.minionSelection.getKey().getValue() && mc.screen instanceof MinionSelectionScreen) {
            if (event.getAction() == GLFW.GLFW_RELEASE) {
                mc.player.closeContainer();
            }
        }
    }


    /**
     * Player 'mouse' events, these are actually events based on attack or item use actions and are still triggered if the key binding is no longer a mouse click.
     **/
    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseButton event) {
        var mc = Minecraft.getInstance();
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(mc.player);
        if (mc.player == null || playerExt == null || mc.hitResult == null)
            return;

        // Left (Attack):
        if (event.getButton() == 0) {
            // Disable attack for large entity reach override:
            if (!mc.player.isSpectator() && !mc.player.isHandsBusy() && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entityHit = ((EntityHitResult) mc.hitResult).getEntity();
                if (playerExt.canMeleeBigEntity(entityHit)) {
                    MessagePlayerAttack message = new MessagePlayerAttack(entityHit);
                    LycanitesMobs.PACKET_MANAGER.sendToServer(message);
                    //event.setCanceled(true);
                }
            }
        }
    }
}
