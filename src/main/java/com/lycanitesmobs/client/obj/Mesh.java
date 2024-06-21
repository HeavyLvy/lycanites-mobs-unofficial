package com.lycanitesmobs.client.obj;

import com.lycanitesmobs.LycanitesMobs;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL21;

import com.lycanitesmobs.client.renderer.CustomRenderStates;

import java.nio.ByteBuffer;


public class Mesh {

	public int[] indices;
	public Vertex[] vertices;
	public Vector3f[] normals;
	private int vbo = -1;

	public int getVbo() {
		if (this.vbo == -1) {
			if (this.normals == null) {
				this.normals = new Vector3f[this.indices.length];
				for (int i = 0; i < this.normals.length / 3; i++) {
					Vector3f v1 = this.vertices[this.indices[i * 3]].getPos();
					Vector3f v2 = this.vertices[this.indices[i * 3 + 1]].getPos();
					Vector3f v3 = this.vertices[this.indices[i * 3 + 2]].getPos();
					Vector3f normal = calcNormal(v1, v2, v3);
					this.normals[i * 3] = normal;
					this.normals[i * 3 + 1] = normal;
					this.normals[i * 3 + 2] = normal;
				}
			}

			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();

			bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);
			for (int i = 0; i < this.indices.length; i++) {
				Vertex vertex = this.vertices[this.indices[i]];
				Vector3f normal = this.normals[i];
				bufferBuilder.vertex(vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z());
				bufferBuilder.uv(vertex.getTexCoords().x, 1.0F - vertex.getTexCoords().y);
				bufferBuilder.normal(normal.x(), normal.y(), normal.z());
				bufferBuilder.endVertex();
			}
			bufferBuilder.end();

			this.vbo = GL21.glGenBuffers();
			GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, vbo);
			GL21.glBufferData(GL21.GL_ARRAY_BUFFER, popNextBuffer(bufferBuilder), GL21.GL_STATIC_DRAW);
			GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, 0);
		}

		return this.vbo;
	}
	public ByteBuffer popNextBuffer(BufferBuilder bufferBuilder) {
		ByteBuffer bytebuffer = bufferBuilder.buffer.slice();
		bytebuffer.order(bufferBuilder.buffer.order());
		bufferBuilder.buffer.clear();
		return bytebuffer;
	}

	private static Vector3f calcNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f output = new Vector3f();

		// Calculate Edges:
		Vector3f calU = new Vector3f(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
		Vector3f calV = new Vector3f(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());

		// Cross Edges
		output.rotateX(calU.y() * calV.z() - calU.z() * calV.y());
		output.rotateY(calU.z() * calV.x() - calU.x() * calV.z());
		output.rotateZ(calU.x() * calV.y() - calU.y() * calV.x());

		output.normalize();
		return output;
	}

	public void delete() {
		if (vbo != -1) {
			GL21.glDeleteBuffers(vbo);
			vbo = -1;
		}
	}

}
