package com.lycanitesmobs.client.renderer.renderapi.client.render.shader;

import com.lycanitesmobs.core.util.CustomGLImportProcessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.lwjgl.opengl.GL33.*;


public class ShaderManager {
    private static Shader positionColorShader;

    public static Shader getPositionColorShader() {
        return positionColorShader;
    }




    public static void loadShaders() {
        positionColorShader = new Shader("position_color");

    }

    public static int loadShaderProgram(String name, ShaderType type) {
        try {
            boolean file_present = true;
            ResourceManager resourceFactory = Minecraft.getInstance().getResourceManager();
            Optional<Resource> resource = resourceFactory.getResource(new ResourceLocation("renderer", "shader/" + name + type.fileExtension));
            int i = glCreateShader(type.glType);
            if (resource.isPresent()) {
                GlStateManager.glShaderSource(i, new CustomGLImportProcessor() {

                    @Nullable
                    public String loadImport(boolean inline, String name) throws IOException {
                        return IOUtils.toString(resource.get().open(), StandardCharsets.UTF_8);
                    }
                }.readSource(readResourceAsString(resource.get().open())));
            } else file_present = false;
            glCompileShader(i);
            if (glGetShaderi(i, GL_COMPILE_STATUS) == 0 || !file_present) {
                String shaderInfo = StringUtils.trim(glGetShaderInfoLog(i, 32768));
                throw new IOException("Couldn't compile " + type.name + " program (" + name + ") : " + shaderInfo);
            }
            return i;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }




    private static String readResourceAsString(InputStream inputStream) {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = TextureUtil.readResource(inputStream);
            int i = byteBuffer.position();
            byteBuffer.rewind();
            return MemoryUtil.memASCII(byteBuffer, i);
        } catch (IOException ignored) {
        } finally {
            if (byteBuffer != null) {
                MemoryUtil.memFree(byteBuffer);
            }
        }
        return null;
    }

public enum ShaderType {
    VERTEX("vertex", ".vsh", GL_VERTEX_SHADER),
    FRAGMENT("fragment", ".fsh", GL_FRAGMENT_SHADER);
    private final String name;
    private final String fileExtension;
    private final int glType;

    ShaderType(String name, String fileExtension, int glType) {
        this.name = name;
        this.fileExtension = fileExtension;
        this.glType = glType;
    }
}
}
