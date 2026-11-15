package com.lycanitesmobs.client.manager;

import net.neoforged.bus.api.SubscribeEvent;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.fluid.type.BaseFluidType;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.lycanitesmobs.client.event.ClientEventListener;
import com.lycanitesmobs.client.gui.screen.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen;
import com.lycanitesmobs.core.manager.ItemManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;

public class ClientManager {
    public static int GL_TEXTURE0 = 33984;
    public static int GL_TEXTURE1 = 33986;
    public static int GL_FULL_BRIGHT = 15728880;

    public static int FULL_BRIGHT = 240;

    protected static ClientManager INSTANCE;

    public static ClientManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientManager();
        }
        return INSTANCE;
    }

    protected Font fontRenderer;

    /**
     * Sets up the Language Manager used for additional language files.
     */
    public void initLanguageManager() {
        LanguageManager.getInstance();
    }

    /**
     * Registers all client side events listeners.
     */
    public void registerEvents() {
        // Event Listeners:
        NeoForge.EVENT_BUS.register(new ClientEventListener());
    }


    /**
     * Returns the client player entity.
     *
     * @return Client player entity.
     */
    public Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * Displays a Screen GUI on the client.
     */
    public void displayGuiScreen(String screenName, Player player) {
        if ("beastiary".equals(screenName)) {
            Minecraft.getInstance().setScreen(new SummoningBeastiaryScreen(player));
        }
    }

    /**
     * Returns the Font Renderer used by Lycanites Mobs.
     *
     * @return A sexy Font Renderer, thanks for the heads up CedKilleur!
     */
    public Font getFontRenderer() {
        return Minecraft.getInstance().font;
		/*if(this.fontRenderer == null) {
			ResourceLocation fontResource = ResourceLocation.fromNamespaceAndPath(LycanitesMobs.MODID, "fonts/diavlo_light.otf");
			this.fontRenderer = new FontRenderer(Minecraft.getInstance().getTextureManager(), new Font(Minecraft.getInstance().getTextureManager(), fontResource));
		}
		return this.fontRenderer;*/
    }


    public void initBlockRenderTypes() {
        for (Block block : ItemManager.getInstance().getCutoutBlocks()) {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
        }
    }

    /** Replaces the deprecated Item/FluidType#initializeClient hooks. Fired on the client mod bus after registration. */
    @SubscribeEvent
    public void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemEquipment) {
                event.registerItem(LycanitesMobs.PROXY.createEquipmentItemExtensions(), item);
            } else if (item instanceof ItemEquipmentPart) {
                event.registerItem(LycanitesMobs.PROXY.createEquipmentPartItemExtensions(), item);
            }
        }
        for (FluidType fluidType : NeoForgeRegistries.FLUID_TYPES) {
            if (fluidType instanceof BaseFluidType baseFluidType) {
                event.registerFluidType(baseFluidType.createClientExtensions(), baseFluidType);
            }
        }
    }
}
