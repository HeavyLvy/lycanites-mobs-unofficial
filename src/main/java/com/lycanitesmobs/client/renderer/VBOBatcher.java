package com.lycanitesmobs.client.renderer;

import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lycanitesmobs.core.util.LMHelperClass;
import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

public class VBOBatcher {

	public static class VBODrawCommand {

		private final int vbo;
		private final int vertexCount;
		private final VertexFormat format;
		private final Matrix4f matrix;
		private final ResourceLocation texLocation;
		private float red = 1.0F;
		private float green = 1.0F;
		private float blue = 1.0F;
		private float alpha = 1.0F;
		private float textureOffsetX;
		private float textureOffsetY;
		private float overlayOffsetX;
		private float overlayOffsetY;
		private float lightOffsetX;
		private float lightOffsetY;

		public VBODrawCommand(int vbo, int vertexCount, VertexFormat format, Matrix4f matrix, ResourceLocation texLocation) {
			this.vbo = vbo;
			this.vertexCount = vertexCount;
			this.format = format;
			this.matrix = matrix;
			this.texLocation = texLocation;
		}

		public VBODrawCommand setColor(Vector4f color) {
			return setColor(color.x(), color.y(), color.z(), color.w());
		}

		public VBODrawCommand setColor(float red, float green, float blue, float alpha) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.alpha = alpha;
			return this;
		}

		public VBODrawCommand setTextureOffset(Vector2f textureOffset) {
			return setTextureOffset(textureOffset.x * 0.01F, -textureOffset.y * 0.01F);
		}

		public VBODrawCommand setTextureOffset(float textureOffsetX, float textureOffsetY) {
			this.textureOffsetX = textureOffsetX;
			this.textureOffsetY = textureOffsetY;
			return this;
		}

		public VBODrawCommand setOverlayOffset(float white, boolean red) {
			return setOverlayOffset(OverlayTexture.u(white), OverlayTexture.v(red));
		}

		public VBODrawCommand setOverlayOffset(float overlayOffsetX, float overlayOffsetY) {
			this.overlayOffsetX = overlayOffsetX;
			this.overlayOffsetY = overlayOffsetY;
			return this;
		}

		public VBODrawCommand setLightOffset(int packed) {
			return setLightOffset(LightTexture.block(packed) << 4, LightTexture.sky(packed) << 4);
		}

		public VBODrawCommand setLightOffset(float lightOffsetX, float lightOffsetY) {
			this.lightOffsetX = lightOffsetX;
			this.lightOffsetY = lightOffsetY;
			return this;
		}
		private static final FloatBuffer MATRIX_BUFFER = GLX.make(MemoryUtil.memAllocFloat(16), (p_209238_0_) -> {
			DebugMemoryUntracker.untrack(MemoryUtil.memAddress(p_209238_0_));
		});
		public void draw() {
			Minecraft.getInstance().getTextureManager().bindForSetup(texLocation);

			GL11.glColor4f(correctedColor(red), correctedColor(green), correctedColor(blue), correctedColor(alpha));
			if (textureOffsetX != 0.0F || textureOffsetY != 0.0F) {
				GL11.glMatrixMode(GL21.GL_TEXTURE);
				GL11.glPushMatrix();
				GL11.glTranslatef(textureOffsetX, textureOffsetY, 0.0F);
				GL11.glMatrixMode(GL21.GL_MODELVIEW);
			}
			RenderSystem.texParameter(GL21.GL_TEXTURE1, LMHelperClass.convertToInteger(overlayOffsetX), LMHelperClass.convertToInteger(overlayOffsetY));
			RenderSystem.texParameter(GL21.GL_TEXTURE2, LMHelperClass.convertToInteger(lightOffsetX), LMHelperClass.convertToInteger(lightOffsetY));

			GL11.glPushMatrix();
			GL11.glMultMatrixf(MATRIX_BUFFER);
			RenderSystem.glBindBuffer(GL21.GL_ARRAY_BUFFER, () -> vbo);
			format.setupBufferState();

			GL11.glDrawArrays(GL21.GL_TRIANGLES, 0, vertexCount);

			format.clearBufferState();
			RenderSystem.glBindBuffer(GL21.GL_ARRAY_BUFFER, () -> 0);
			GL11.glPopMatrix();

			if (textureOffsetX != 0.0F || textureOffsetY != 0.0F) {
				GL11.glMatrixMode(GL21.GL_TEXTURE);
				GL11.glPopMatrix();
				GL11.glMatrixMode(GL21.GL_MODELVIEW);
			}
		}

		private static float correctedColor(float f) {
			return ((int) (f * 255.0F) & 0xFF) / 255.0F;
		}

	}

	private static final VBOBatcher INSTANCE = new VBOBatcher();
	private final Map<RenderType, List<VBODrawCommand>> batchedDrawCommands = new LinkedHashMap<>();

	public static VBOBatcher getInstance() {
		return INSTANCE;
	}

	public void queue(RenderType renderType, VBODrawCommand drawCommand) {
		batchedDrawCommands.computeIfAbsent(renderType, k -> new LinkedList<>()).add(drawCommand);
	}

	public void endBatches() {
		batchedDrawCommands.forEach((renderType, drawCommands) -> {
			if (drawCommands.isEmpty()) {
				return;
			}

			renderType.setupRenderState();
			//RenderSystem.enableTexture();

			for (VBODrawCommand drawCommand : drawCommands) {
				drawCommand.draw();
			}

			renderType.clearRenderState();
			drawCommands.clear();
		});

		batchedDrawCommands.clear();
	}

}
