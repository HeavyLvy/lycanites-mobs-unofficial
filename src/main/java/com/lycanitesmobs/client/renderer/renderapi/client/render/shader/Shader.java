package com.lycanitesmobs.client.renderer.renderapi.client.render.shader;



import com.lycanitesmobs.LycanitesMobs;

import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;

import static org.lwjgl.opengl.GL33.*;

public class Shader {

    private int id = 0;


    public Shader(String name) {
        try {
            int v = ShaderManager.loadShaderProgram(name, ShaderManager.ShaderType.VERTEX);
            int f = ShaderManager.loadShaderProgram(name, ShaderManager.ShaderType.FRAGMENT);
            LycanitesMobs.logInfo("", "Shader Vertex: " + v);
            LycanitesMobs.logInfo("", "Shader Vertex: " + f);
            this.id = glCreateProgram();
            glAttachShader(id, v);
            glAttachShader(id, f);
            glLinkProgram(id);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bind() {
        glUseProgram(id);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void uniformMatrix4f(String name, FloatBuffer matrix) {
        bind();
        glUniformMatrix4fv(glGetUniformLocation(id, name), false, matrix);
        unbind();
    }

    public void uniformValue2f(String name, float value1, float value2) {
        bind();
        glUniform2f(glGetUniformLocation(id, name), value1, value2);
        unbind();
    }
}
