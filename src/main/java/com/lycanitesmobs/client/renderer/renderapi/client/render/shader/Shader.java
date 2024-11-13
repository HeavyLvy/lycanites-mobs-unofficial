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
            this.id = glCreateProgram();
            glAttachShader(id, v);
            glAttachShader(id, f);
            glLinkProgram(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uniform1i(String name, int value) {
        bind();
        int location = glGetUniformLocation(id, name);
        if (location != -1) { // Check if the uniform location is valid
            glUniform1i(location, value);
        } else {
            System.err.println("Could not find uniform variable '" + name + "' in shader program.");
        }
        unbind(); // Unbind after setting the uniform
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
