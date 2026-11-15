package com.lycanitesmobs.client.gui.widgets;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.util.helpers.DrawHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public abstract class BaseList<S> extends AbstractSelectionList<BaseListEntry> {
    public DrawHelper drawHelper;
    public S screen;

    public BaseList(S screen, int width, int height, int top, int bottom, int left, int slotHeight) {
        // 1.21 lists are positioned widgets: (minecraft, width, listHeight, y, itemHeight).
        super(Minecraft.getInstance(), width, bottom - top, top, slotHeight);
        Minecraft minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
        this.setX(left);
        this.screen = screen;
        this.createEntries();
    }

    public BaseList(S screen, int width, int height, int top, int bottom, int left) {
        this(screen, width, height, top, bottom, left, 28);
    }

    @Override
    public int getRowWidth() {
        return this.getWidth();
    }

    protected int getScrollbarWidth() {
        return 6;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRight() - this.getScrollbarWidth();
    }

    /**
     * Creates all List Entries for this List Widget.
     */
    public void createEntries() {
    }

    /**
     * Returns the index of the selected entry.
     *
     * @return The selected entry index, defaults to 0 if none are selected.
     */
    public int getSelectedIndex() {
        if (this.getSelected() != null)
            return this.getSelected().index;
        return 0;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = guiGraphics.pose();

        try {
            this.renderListBackground(guiGraphics);

            double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
            int windowHeight = Minecraft.getInstance().getWindow().getScreenHeight();
            int scissorLeft = (int) Math.floor(this.getX() * scaleFactor);
            int scissorRight = (int) Math.ceil(this.getRight() * scaleFactor);
            int scissorTop = (int) Math.floor(this.getY() * scaleFactor);
            int scissorBottom = (int) Math.ceil(this.getBottom() * scaleFactor);
            int scissorWidth = scissorRight - scissorLeft;
            int scissorHeight = scissorBottom - scissorTop;
            if (scissorWidth <= 0 || scissorHeight <= 0) {
                return;
            }

            RenderSystem.enableScissor(scissorLeft, windowHeight - scissorBottom, scissorWidth, scissorHeight);

            RenderSystem.disableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.resetTextureMatrix();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE
            );
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            Tesselator tessellator = Tesselator.getInstance();
            Matrix4f matrix = matrixStack.last().pose();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.addVertex(matrix, (float) this.getX(), (float) this.getBottom(), 0.0F).setColor(0, 0, 0, 64);
            bufferbuilder.addVertex(matrix, (float) this.getRight(), (float) this.getBottom(), 0.0F).setColor(0, 0, 0, 64);
            bufferbuilder.addVertex(matrix, (float) this.getRight(), (float) this.getY(), 0.0F).setColor(0, 0, 0, 64);
            bufferbuilder.addVertex(matrix, (float) this.getX(), (float) this.getY(), 0.0F).setColor(0, 0, 0, 64);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

            int listLeft = this.getRowLeft();
            int listTop = this.getY() + 4 - (int) this.getScrollAmount();
            if (this.isFocused()) {
                this.renderHeader(guiGraphics, listLeft, listTop);
            }

            try {
                this.renderListItems(guiGraphics, mouseX, mouseY, partialTicks);
            } catch (Exception e) {
                LMHelperClass.logError("BaseList renderList error: " + e.getMessage());
            }

            int maxScroll = this.getMaxScroll();
            if (maxScroll > 0) {
                int trackHeight = this.getBottom() - this.getY();
                int handleHeight = (int) ((float) trackHeight * trackHeight / (float) this.getMaxPosition());
                handleHeight = LMHelperClass.convertToInteger(LMHelperClass.clamp(handleHeight, 32, trackHeight - 8));
                int handleY = (int) this.getScrollAmount() * (trackHeight - handleHeight) / maxScroll + this.getY();
                if (handleY < this.getY()) {
                    handleY = this.getY();
                }

                int scrollbarLeft = this.getScrollbarPosition();
                int scrollbarRight = scrollbarLeft + this.getScrollbarWidth();

                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.addVertex(matrix, (float) scrollbarLeft, (float) this.getBottom(), 0.0F).setColor(0, 0, 0, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarRight, (float) this.getBottom(), 0.0F).setColor(0, 0, 0, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarRight, (float) this.getY(), 0.0F).setColor(0, 0, 0, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarLeft, (float) this.getY(), 0.0F).setColor(0, 0, 0, 255);
                BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

                bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.addVertex(matrix, (float) scrollbarLeft, (float) (handleY + handleHeight), 0.0F).setColor(128, 128, 128, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarRight, (float) (handleY + handleHeight), 0.0F).setColor(128, 128, 128, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarRight, (float) handleY, 0.0F).setColor(128, 128, 128, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarLeft, (float) handleY, 0.0F).setColor(128, 128, 128, 255);
                BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

                bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.addVertex(matrix, (float) scrollbarLeft, (float) (handleY + handleHeight - 1), 0.0F).setColor(192, 192, 192, 255);
                bufferbuilder.addVertex(matrix, (float) (scrollbarRight - 1), (float) (handleY + handleHeight - 1), 0.0F).setColor(192, 192, 192, 255);
                bufferbuilder.addVertex(matrix, (float) (scrollbarRight - 1), (float) handleY, 0.0F).setColor(192, 192, 192, 255);
                bufferbuilder.addVertex(matrix, (float) scrollbarLeft, (float) handleY, 0.0F).setColor(192, 192, 192, 255);
                BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            }

            this.renderDecorations(guiGraphics, mouseX, mouseY);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.disableScissor();
        }
    }


    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    /** The 1.20.1 list background hook was empty by default; vanilla's 1.21 default draws a dark panel. */
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }
}
