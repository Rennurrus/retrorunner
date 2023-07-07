package com.badlogic.gdx.math;

public class Polyline implements Shape2D {
    private boolean calculateLength;
    private boolean calculateScaledLength;
    private boolean dirty;
    private float length;
    private float[] localVertices;
    private float originX;
    private float originY;
    private float rotation;
    private float scaleX;
    private float scaleY;
    private float scaledLength;
    private float[] worldVertices;
    private float x;
    private float y;

    public Polyline() {
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.calculateScaledLength = true;
        this.calculateLength = true;
        this.dirty = true;
        this.localVertices = new float[0];
    }

    public Polyline(float[] vertices) {
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.calculateScaledLength = true;
        this.calculateLength = true;
        this.dirty = true;
        if (vertices.length >= 4) {
            this.localVertices = vertices;
            return;
        }
        throw new IllegalArgumentException("polylines must contain at least 2 points.");
    }

    public float[] getVertices() {
        return this.localVertices;
    }

    public float[] getTransformedVertices() {
        if (!this.dirty) {
            return this.worldVertices;
        }
        boolean scale = false;
        this.dirty = false;
        float[] localVertices2 = this.localVertices;
        float[] fArr = this.worldVertices;
        if (fArr == null || fArr.length < localVertices2.length) {
            this.worldVertices = new float[localVertices2.length];
        }
        float[] worldVertices2 = this.worldVertices;
        float positionX = this.x;
        float positionY = this.y;
        float originX2 = this.originX;
        float originY2 = this.originY;
        float scaleX2 = this.scaleX;
        float scaleY2 = this.scaleY;
        if (!(scaleX2 == 1.0f && scaleY2 == 1.0f)) {
            scale = true;
        }
        float rotation2 = this.rotation;
        float cos = MathUtils.cosDeg(rotation2);
        float sin = MathUtils.sinDeg(rotation2);
        int n = localVertices2.length;
        for (int i = 0; i < n; i += 2) {
            float x2 = localVertices2[i] - originX2;
            float y2 = localVertices2[i + 1] - originY2;
            if (scale) {
                x2 *= scaleX2;
                y2 *= scaleY2;
            }
            if (rotation2 != 0.0f) {
                float oldX = x2;
                x2 = (cos * x2) - (sin * y2);
                y2 = (sin * oldX) + (cos * y2);
            }
            worldVertices2[i] = positionX + x2 + originX2;
            worldVertices2[i + 1] = positionY + y2 + originY2;
        }
        return worldVertices2;
    }

    public float getLength() {
        if (!this.calculateLength) {
            return this.length;
        }
        this.calculateLength = false;
        this.length = 0.0f;
        int n = this.localVertices.length - 2;
        for (int i = 0; i < n; i += 2) {
            float[] fArr = this.localVertices;
            float x2 = fArr[i + 2] - fArr[i];
            float y2 = fArr[i + 1] - fArr[i + 3];
            this.length += (float) Math.sqrt((double) ((x2 * x2) + (y2 * y2)));
        }
        return this.length;
    }

    public float getScaledLength() {
        if (!this.calculateScaledLength) {
            return this.scaledLength;
        }
        this.calculateScaledLength = false;
        this.scaledLength = 0.0f;
        int n = this.localVertices.length - 2;
        for (int i = 0; i < n; i += 2) {
            float[] fArr = this.localVertices;
            float f = fArr[i + 2];
            float f2 = this.scaleX;
            float x2 = (f * f2) - (fArr[i] * f2);
            float f3 = fArr[i + 1];
            float f4 = this.scaleY;
            float y2 = (f3 * f4) - (fArr[i + 3] * f4);
            this.scaledLength += (float) Math.sqrt((double) ((x2 * x2) + (y2 * y2)));
        }
        return this.scaledLength;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getOriginX() {
        return this.originX;
    }

    public float getOriginY() {
        return this.originY;
    }

    public float getRotation() {
        return this.rotation;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public void setOrigin(float originX2, float originY2) {
        this.originX = originX2;
        this.originY = originY2;
        this.dirty = true;
    }

    public void setPosition(float x2, float y2) {
        this.x = x2;
        this.y = y2;
        this.dirty = true;
    }

    public void setVertices(float[] vertices) {
        if (vertices.length >= 4) {
            this.localVertices = vertices;
            this.dirty = true;
            return;
        }
        throw new IllegalArgumentException("polylines must contain at least 2 points.");
    }

    public void setRotation(float degrees) {
        this.rotation = degrees;
        this.dirty = true;
    }

    public void rotate(float degrees) {
        this.rotation += degrees;
        this.dirty = true;
    }

    public void setScale(float scaleX2, float scaleY2) {
        this.scaleX = scaleX2;
        this.scaleY = scaleY2;
        this.dirty = true;
        this.calculateScaledLength = true;
    }

    public void scale(float amount) {
        this.scaleX += amount;
        this.scaleY += amount;
        this.dirty = true;
        this.calculateScaledLength = true;
    }

    public void calculateLength() {
        this.calculateLength = true;
    }

    public void calculateScaledLength() {
        this.calculateScaledLength = true;
    }

    public void dirty() {
        this.dirty = true;
    }

    public void translate(float x2, float y2) {
        this.x += x2;
        this.y += y2;
        this.dirty = true;
    }

    public boolean contains(Vector2 point) {
        return false;
    }

    public boolean contains(float x2, float y2) {
        return false;
    }
}
