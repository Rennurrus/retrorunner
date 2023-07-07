package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.particles.values.MeshSpawnShapeValue;
import com.badlogic.gdx.math.CumulativeDistribution;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public final class WeightMeshSpawnShapeValue extends MeshSpawnShapeValue {
    private CumulativeDistribution<MeshSpawnShapeValue.Triangle> distribution = new CumulativeDistribution<>();

    public WeightMeshSpawnShapeValue(WeightMeshSpawnShapeValue value) {
        super(value);
        load(value);
    }

    public WeightMeshSpawnShapeValue() {
    }

    public void init() {
        calculateWeights();
    }

    public void calculateWeights() {
        this.distribution.clear();
        VertexAttributes attributes = this.mesh.getVertexAttributes();
        int indicesCount = this.mesh.getNumIndices();
        int vertexCount = this.mesh.getNumVertices();
        int vertexSize = (short) (attributes.vertexSize / 4);
        int positionOffset = (short) (attributes.findByUsage(1).offset / 4);
        float[] vertices = new float[(vertexCount * vertexSize)];
        this.mesh.getVertices(vertices);
        float f = 2.0f;
        if (indicesCount > 0) {
            short[] indices = new short[indicesCount];
            this.mesh.getIndices(indices);
            int i = 0;
            while (i < indicesCount) {
                int p1Offset = (indices[i] * vertexSize) + positionOffset;
                int p2Offset = (indices[i + 1] * vertexSize) + positionOffset;
                int p3Offset = (indices[i + 2] * vertexSize) + positionOffset;
                float x1 = vertices[p1Offset];
                float y1 = vertices[p1Offset + 1];
                float z1 = vertices[p1Offset + 2];
                float x2 = vertices[p2Offset];
                float y2 = vertices[p2Offset + 1];
                float z2 = vertices[p2Offset + 2];
                float x3 = vertices[p3Offset];
                float y3 = vertices[p3Offset + 1];
                float z3 = vertices[p3Offset + 2];
                float area = Math.abs(((((y2 - y3) * x1) + ((y3 - y1) * x2)) + ((y1 - y2) * x3)) / f);
                MeshSpawnShapeValue.Triangle triangle = r13;
                VertexAttributes attributes2 = attributes;
                MeshSpawnShapeValue.Triangle triangle2 = new MeshSpawnShapeValue.Triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
                this.distribution.add(triangle2, area);
                i += 3;
                attributes = attributes2;
                f = 2.0f;
            }
            int i2 = indicesCount;
            int i3 = vertexCount;
            short s = positionOffset;
        } else {
            int i4 = 0;
            while (i4 < vertexCount) {
                int p1Offset2 = i4 + positionOffset;
                int p2Offset2 = p1Offset2 + vertexSize;
                int p3Offset2 = p2Offset2 + vertexSize;
                float x12 = vertices[p1Offset2];
                float y12 = vertices[p1Offset2 + 1];
                float z12 = vertices[p1Offset2 + 2];
                float x22 = vertices[p2Offset2];
                float y22 = vertices[p2Offset2 + 1];
                float z22 = vertices[p2Offset2 + 2];
                float x32 = vertices[p3Offset2];
                float y32 = vertices[p3Offset2 + 1];
                float z32 = vertices[p3Offset2 + 2];
                float area2 = Math.abs(((((y22 - y32) * x12) + ((y32 - y12) * x22)) + ((y12 - y22) * x32)) / 2.0f);
                int indicesCount2 = indicesCount;
                int vertexCount2 = vertexCount;
                CumulativeDistribution<MeshSpawnShapeValue.Triangle> cumulativeDistribution = this.distribution;
                int positionOffset2 = positionOffset;
                MeshSpawnShapeValue.Triangle triangle3 = new MeshSpawnShapeValue.Triangle(x12, y12, z12, x22, y22, z22, x32, y32, z32);
                cumulativeDistribution.add(triangle3, area2);
                i4 += vertexSize;
                indicesCount = indicesCount2;
                vertexCount = vertexCount2;
                positionOffset = positionOffset2;
            }
            int i5 = vertexCount;
            int i6 = positionOffset;
        }
        this.distribution.generateNormalized();
    }

    public void spawnAux(Vector3 vector, float percent) {
        MeshSpawnShapeValue.Triangle t = this.distribution.value();
        float a = MathUtils.random();
        float b = MathUtils.random();
        vector.set(t.x1 + ((t.x2 - t.x1) * a) + ((t.x3 - t.x1) * b), t.y1 + ((t.y2 - t.y1) * a) + ((t.y3 - t.y1) * b), t.z1 + ((t.z2 - t.z1) * a) + ((t.z3 - t.z1) * b));
    }

    public SpawnShapeValue copy() {
        return new WeightMeshSpawnShapeValue(this);
    }
}
