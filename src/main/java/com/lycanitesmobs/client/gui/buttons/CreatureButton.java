package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.opengl.GL11;

public class CreatureButton extends ButtonBase {
    public CreatureInfo creatureInfo;
    public int summonSetId = 0;

    public CreatureButton(int buttonID, int x, int y, int w, int h, MutableComponent text, int summonSetId, CreatureInfo creatureInfo, Button.OnPress pressable) {
        super(buttonID, x, y, w, h, text, pressable);
        this.summonSetId = summonSetId;
        this.creatureInfo = creatureInfo;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        super.renderWidget(g, mouseX, mouseY, partialTicks);

        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        int buttonX = this.getX();
        int buttonY = this.getY();

        net.minecraft.resources.ResourceLocation bg = com.lycanitesmobs.client.TextureManager.getTexture("GUIInventoryCreature");
        int stateVOffset = 0;
        if (this.isHovered()) stateVOffset = 64;
        com.lycanitesmobs.core.entity.ExtendedPlayer playerExt = com.lycanitesmobs.core.entity.ExtendedPlayer.getForPlayer(net.minecraft.client.Minecraft.getInstance().player);
        if (playerExt != null && playerExt.selectedSummonSet == this.summonSetId) stateVOffset = 32;

        g.blit(bg, buttonX, buttonY, 193, 187 - stateVOffset, this.width, this.height);

        if (this.creatureInfo != null) {
            net.minecraft.resources.ResourceLocation icon = this.creatureInfo.getIcon();
            g.blit(icon, buttonX + 8, buttonY + 8, 0, 0, 16, 16, 16, 16);
        }

        int textColor = 14737632;
        if (!this.active) textColor = -6250336;
        else if (this.isHovered()) textColor = 16777120;

        this.drawHelper.drawCenteredString(g, this.drawHelper.getFontRenderer(), this.getMessage(), buttonX + 5, buttonY + 2, textColor);
    }

}
