package com.lycanitesmobs.client.renderer.renderapi.client.render.shader;


import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

public class Shader {

    private int id = 0;


    public Shader(String name) {
        try {
            int v = ShaderManager.loadShaderProgram(name, ShaderManager.ShaderType.VERTEX);
            int f = ShaderManager.loadShaderProgram(name, ShaderManager.ShaderType.FRAGMENT);
            this.id = glCreateProgram();
            glAttachShader(id, v);
            glAttachShader(id, f);
            glLinkProgram(id);
        } catch (Exception e) {
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

}
