package com.lycanitesmobs.client.obj;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
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

			bufferBuilder.begin(GL21.GL_TRIANGLES, CustomRenderStates.POS_TEX_NORMAL);
			for (int i = 0; i < this.indices.length; i++) {
				Vertex vertex = this.vertices[this.indices[i]];
				Vector3f normal = this.normals[i];
				bufferBuilder.vertex(vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z())
						.uv(vertex.getTexCoords().x, 1.0F - vertex.getTexCoords().y)
						.normal(normal.x(), normal.y(), normal.z())
						.endVertex();
			}
			bufferBuilder.end();

			this.vbo = GL21.glGenBuffers();
			GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, vbo);
			GL21.glBufferData(GL21.GL_ARRAY_BUFFER, bufferBuilder.end().vertexBuffer().slice(), GL21.GL_STATIC_DRAW);
			GL21.glBindBuffer(GL21.GL_ARRAY_BUFFER, 0);
		}

		return this.vbo;
	}
	/**
	 * Extracts and returns the ByteBuffer slice for the next buffer.
	 * @return ByteBuffer slice containing the vertex data.
	 */
	public ByteBuffer getNextBufferSlice(BufferBuilder bufferBuilder) {
		if (bufferBuilder.currentIndex >= vertexStates.size()) {
			return null; // Or throw an exception depending on your needs.
		}

		BufferBuilder.DrawState drawState = this.vertexStates.get(this.currentIndex++);
		this.byteBuffer.position(this.uploadedBytes);
		this.uploadedBytes += drawState.getVertexCount() * drawState.getFormat().getVertexSize();
		this.byteBuffer.limit(this.uploadedBytes);
		if (this.currentIndex == this.vertexStates.size() && this.vertexCount == 0) {
			this.clearBuffer();
		}

		ByteBuffer slicedBuffer = this.byteBuffer.slice();
		slicedBuffer.order(this.byteBuffer.order());
		this.byteBuffer.clear();
		return slicedBuffer;
	}
	public Pair<BufferBuilder.DrawState, ByteBuffer> popNextBuffer(BufferBuilder bufferBuilder, BufferBuilder.DrawState dS) {
		BufferBuilder.DrawState drawState = dS.vertexCount().get(this.lastRenderedCountIndex++);
		this.buffer.position(this.totalUploadedBytes);
		this.totalUploadedBytes += drawState.vertexCount() * drawState.format().getVertexSize();
		this.buffer.limit(this.totalUploadedBytes);
		if (this.lastRenderedCountIndex == this.vertexCounts.size() && this.vertices == 0) {
			this.clear();
		}

		ByteBuffer byteBuffer = this.buffer.slice();
		byteBuffer.order(this.buffer.order());
		this.buffer.clear();
		return Pair.of(drawState, byteBuffer);
	}

	private static Vector3f calcNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f output = new Vector3f();

		// Calculate Edges:
		Vector3f calU = new Vector3f(v2.x() - v1.x(), v2.y() - v1.y(), v2.z() - v1.z());
		Vector3f calV = new Vector3f(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());

		// Cross Edges
		output.setX(calU.y() * calV.z() - calU.z() * calV.y());
		output.setY(calU.z() * calV.x() - calU.x() * calV.z());
		output.setZ(calU.x() * calV.y() - calU.y() * calV.x());

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
