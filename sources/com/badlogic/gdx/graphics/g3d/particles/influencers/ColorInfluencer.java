package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.values.GradientColorValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class ColorInfluencer extends Influencer {
    ParallelArray.FloatChannel colorChannel;

    public static class Random extends ColorInfluencer {
        ParallelArray.FloatChannel colorChannel;

        public void allocateChannels() {
            this.colorChannel = (ParallelArray.FloatChannel) this.controller.particles.addChannel(ParticleChannels.Color);
        }

        public void activateParticles(int startIndex, int count) {
            int i = this.colorChannel.strideSize * startIndex;
            int c = (this.colorChannel.strideSize * count) + i;
            while (i < c) {
                this.colorChannel.data[i + 0] = MathUtils.random();
                this.colorChannel.data[i + 1] = MathUtils.random();
                this.colorChannel.data[i + 2] = MathUtils.random();
                this.colorChannel.data[i + 3] = MathUtils.random();
                i += this.colorChannel.strideSize;
            }
        }

        public Random copy() {
            return new Random();
        }
    }

    public static class Single extends ColorInfluencer {
        ParallelArray.FloatChannel alphaInterpolationChannel;
        public ScaledNumericValue alphaValue;
        public GradientColorValue colorValue;
        ParallelArray.FloatChannel lifeChannel;

        public Single() {
            this.colorValue = new GradientColorValue();
            this.alphaValue = new ScaledNumericValue();
            this.alphaValue.setHigh(1.0f);
        }

        public Single(Single billboardColorInfluencer) {
            this();
            set(billboardColorInfluencer);
        }

        public void set(Single colorInfluencer) {
            this.colorValue.load(colorInfluencer.colorValue);
            this.alphaValue.load(colorInfluencer.alphaValue);
        }

        public void allocateChannels() {
            ColorInfluencer.super.allocateChannels();
            ParticleChannels.Interpolation.id = this.controller.particleChannels.newId();
            this.alphaInterpolationChannel = (ParallelArray.FloatChannel) this.controller.particles.addChannel(ParticleChannels.Interpolation);
            this.lifeChannel = (ParallelArray.FloatChannel) this.controller.particles.addChannel(ParticleChannels.Life);
        }

        public void activateParticles(int startIndex, int count) {
            int i = this.colorChannel.strideSize * startIndex;
            int a = this.alphaInterpolationChannel.strideSize * startIndex;
            int l = (this.lifeChannel.strideSize * startIndex) + 2;
            int c = (this.colorChannel.strideSize * count) + i;
            while (i < c) {
                float alphaStart = this.alphaValue.newLowValue();
                float alphaDiff = this.alphaValue.newHighValue() - alphaStart;
                this.colorValue.getColor(0.0f, this.colorChannel.data, i);
                this.colorChannel.data[i + 3] = (this.alphaValue.getScale(this.lifeChannel.data[l]) * alphaDiff) + alphaStart;
                this.alphaInterpolationChannel.data[a + 0] = alphaStart;
                this.alphaInterpolationChannel.data[a + 1] = alphaDiff;
                i += this.colorChannel.strideSize;
                a += this.alphaInterpolationChannel.strideSize;
                l += this.lifeChannel.strideSize;
            }
        }

        public void update() {
            int i = 0;
            int a = 0;
            int l = 2;
            int c = (this.controller.particles.size * this.colorChannel.strideSize) + 0;
            while (i < c) {
                float lifePercent = this.lifeChannel.data[l];
                this.colorValue.getColor(lifePercent, this.colorChannel.data, i);
                this.colorChannel.data[i + 3] = this.alphaInterpolationChannel.data[a + 0] + (this.alphaInterpolationChannel.data[a + 1] * this.alphaValue.getScale(lifePercent));
                i += this.colorChannel.strideSize;
                a += this.alphaInterpolationChannel.strideSize;
                l += this.lifeChannel.strideSize;
            }
        }

        public Single copy() {
            return new Single(this);
        }

        public void write(Json json) {
            json.writeValue("alpha", (Object) this.alphaValue);
            json.writeValue("color", (Object) this.colorValue);
        }

        public void read(Json json, JsonValue jsonData) {
            this.alphaValue = (ScaledNumericValue) json.readValue("alpha", ScaledNumericValue.class, jsonData);
            this.colorValue = (GradientColorValue) json.readValue("color", GradientColorValue.class, jsonData);
        }
    }

    public void allocateChannels() {
        this.colorChannel = (ParallelArray.FloatChannel) this.controller.particles.addChannel(ParticleChannels.Color);
    }
}
