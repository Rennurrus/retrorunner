package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.NumberUtils;
import java.io.Serializable;

public class Quaternion implements Serializable {
    private static final long serialVersionUID = -7661875440774897168L;
    private static Quaternion tmp1 = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    private static Quaternion tmp2 = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    public float w;
    public float x;
    public float y;
    public float z;

    public Quaternion(float x2, float y2, float z2, float w2) {
        set(x2, y2, z2, w2);
    }

    public Quaternion() {
        idt();
    }

    public Quaternion(Quaternion quaternion) {
        set(quaternion);
    }

    public Quaternion(Vector3 axis, float angle) {
        set(axis, angle);
    }

    public Quaternion set(float x2, float y2, float z2, float w2) {
        this.x = x2;
        this.y = y2;
        this.z = z2;
        this.w = w2;
        return this;
    }

    public Quaternion set(Quaternion quaternion) {
        return set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public Quaternion set(Vector3 axis, float angle) {
        return setFromAxis(axis.x, axis.y, axis.z, angle);
    }

    public Quaternion cpy() {
        return new Quaternion(this);
    }

    public static final float len(float x2, float y2, float z2, float w2) {
        return (float) Math.sqrt((double) ((x2 * x2) + (y2 * y2) + (z2 * z2) + (w2 * w2)));
    }

    public float len() {
        float f = this.x;
        float f2 = this.y;
        float f3 = (f * f) + (f2 * f2);
        float f4 = this.z;
        float f5 = f3 + (f4 * f4);
        float f6 = this.w;
        return (float) Math.sqrt((double) (f5 + (f6 * f6)));
    }

    public String toString() {
        return "[" + this.x + "|" + this.y + "|" + this.z + "|" + this.w + "]";
    }

    public Quaternion setEulerAngles(float yaw, float pitch, float roll) {
        return setEulerAnglesRad(yaw * 0.017453292f, pitch * 0.017453292f, 0.017453292f * roll);
    }

    public Quaternion setEulerAnglesRad(float yaw, float pitch, float roll) {
        float hr = roll * 0.5f;
        float shr = (float) Math.sin((double) hr);
        float chr = (float) Math.cos((double) hr);
        float hp = pitch * 0.5f;
        float shp = (float) Math.sin((double) hp);
        float chp = (float) Math.cos((double) hp);
        float hy = 0.5f * yaw;
        float shy = (float) Math.sin((double) hy);
        float chy = (float) Math.cos((double) hy);
        float chy_shp = chy * shp;
        float shy_chp = shy * chp;
        float chy_chp = chy * chp;
        float shy_shp = shy * shp;
        this.x = (chy_shp * chr) + (shy_chp * shr);
        this.y = (shy_chp * chr) - (chy_shp * shr);
        this.z = (chy_chp * shr) - (shy_shp * chr);
        this.w = (chy_chp * chr) + (shy_shp * shr);
        return this;
    }

    public int getGimbalPole() {
        float t = (this.y * this.x) + (this.z * this.w);
        if (t > 0.499f) {
            return 1;
        }
        return t < -0.499f ? -1 : 0;
    }

    public float getRollRad() {
        int pole = getGimbalPole();
        if (pole == 0) {
            float f = this.w;
            float f2 = this.z;
            float f3 = this.y;
            float f4 = this.x;
            return MathUtils.atan2(((f * f2) + (f3 * f4)) * 2.0f, 1.0f - (((f4 * f4) + (f2 * f2)) * 2.0f));
        }
        return MathUtils.atan2(this.y, this.w) * ((float) pole) * 2.0f;
    }

    public float getRoll() {
        return getRollRad() * 57.295776f;
    }

    public float getPitchRad() {
        int pole = getGimbalPole();
        return pole == 0 ? (float) Math.asin((double) MathUtils.clamp(((this.w * this.x) - (this.z * this.y)) * 2.0f, -1.0f, 1.0f)) : ((float) pole) * 3.1415927f * 0.5f;
    }

    public float getPitch() {
        return getPitchRad() * 57.295776f;
    }

    public float getYawRad() {
        if (getGimbalPole() != 0) {
            return 0.0f;
        }
        float f = this.y;
        float f2 = this.x;
        return MathUtils.atan2(((this.w * f) + (this.z * f2)) * 2.0f, 1.0f - (((f * f) + (f2 * f2)) * 2.0f));
    }

    public float getYaw() {
        return getYawRad() * 57.295776f;
    }

    public static final float len2(float x2, float y2, float z2, float w2) {
        return (x2 * x2) + (y2 * y2) + (z2 * z2) + (w2 * w2);
    }

    public float len2() {
        float f = this.x;
        float f2 = this.y;
        float f3 = (f * f) + (f2 * f2);
        float f4 = this.z;
        float f5 = f3 + (f4 * f4);
        float f6 = this.w;
        return f5 + (f6 * f6);
    }

    public Quaternion nor() {
        float len = len2();
        if (len != 0.0f && !MathUtils.isEqual(len, 1.0f)) {
            float len2 = (float) Math.sqrt((double) len);
            this.w /= len2;
            this.x /= len2;
            this.y /= len2;
            this.z /= len2;
        }
        return this;
    }

    public Quaternion conjugate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vector3 transform(Vector3 v) {
        tmp2.set(this);
        tmp2.conjugate();
        tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0.0f)).mulLeft(this);
        Quaternion quaternion = tmp2;
        v.x = quaternion.x;
        v.y = quaternion.y;
        v.z = quaternion.z;
        return v;
    }

    public Quaternion mul(Quaternion other) {
        float f = this.w;
        float f2 = other.x;
        float f3 = this.x;
        float f4 = other.w;
        float f5 = this.y;
        float f6 = other.z;
        float f7 = this.z;
        float f8 = other.y;
        this.x = (((f * f2) + (f3 * f4)) + (f5 * f6)) - (f7 * f8);
        this.y = (((f * f8) + (f5 * f4)) + (f7 * f2)) - (f3 * f6);
        this.z = (((f * f6) + (f7 * f4)) + (f3 * f8)) - (f5 * f2);
        this.w = (((f * f4) - (f3 * f2)) - (f5 * f8)) - (f7 * f6);
        return this;
    }

    public Quaternion mul(float x2, float y2, float z2, float w2) {
        float f = this.w;
        float f2 = this.x;
        float f3 = this.y;
        float f4 = this.z;
        this.x = (((f * x2) + (f2 * w2)) + (f3 * z2)) - (f4 * y2);
        this.y = (((f * y2) + (f3 * w2)) + (f4 * x2)) - (f2 * z2);
        this.z = (((f * z2) + (f4 * w2)) + (f2 * y2)) - (f3 * x2);
        this.w = (((f * w2) - (f2 * x2)) - (f3 * y2)) - (f4 * z2);
        return this;
    }

    public Quaternion mulLeft(Quaternion other) {
        float f = other.w;
        float f2 = this.x;
        float f3 = other.x;
        float f4 = this.w;
        float f5 = other.y;
        float f6 = this.z;
        float f7 = other.z;
        float f8 = this.y;
        this.x = (((f * f2) + (f3 * f4)) + (f5 * f6)) - (f7 * f8);
        this.y = (((f * f8) + (f5 * f4)) + (f7 * f2)) - (f3 * f6);
        this.z = (((f * f6) + (f7 * f4)) + (f3 * f8)) - (f5 * f2);
        this.w = (((f * f4) - (f3 * f2)) - (f5 * f8)) - (f7 * f6);
        return this;
    }

    public Quaternion mulLeft(float x2, float y2, float z2, float w2) {
        float f = this.x;
        float f2 = this.w;
        float f3 = this.z;
        float f4 = this.y;
        this.x = (((w2 * f) + (x2 * f2)) + (y2 * f3)) - (z2 * f4);
        this.y = (((w2 * f4) + (y2 * f2)) + (z2 * f)) - (x2 * f3);
        this.z = (((w2 * f3) + (z2 * f2)) + (x2 * f4)) - (y2 * f);
        this.w = (((f2 * w2) - (f * x2)) - (f4 * y2)) - (f3 * z2);
        return this;
    }

    public Quaternion add(Quaternion quaternion) {
        this.x += quaternion.x;
        this.y += quaternion.y;
        this.z += quaternion.z;
        this.w += quaternion.w;
        return this;
    }

    public Quaternion add(float qx, float qy, float qz, float qw) {
        this.x += qx;
        this.y += qy;
        this.z += qz;
        this.w += qw;
        return this;
    }

    public void toMatrix(float[] matrix) {
        float f = this.x;
        float xx = f * f;
        float f2 = this.y;
        float xy = f * f2;
        float f3 = this.z;
        float xz = f * f3;
        float f4 = this.w;
        float xw = f * f4;
        float yy = f2 * f2;
        float yz = f2 * f3;
        float yw = f2 * f4;
        float zz = f3 * f3;
        float zw = f3 * f4;
        matrix[0] = 1.0f - ((yy + zz) * 2.0f);
        matrix[4] = (xy - zw) * 2.0f;
        matrix[8] = (xz + yw) * 2.0f;
        matrix[12] = 0.0f;
        matrix[1] = (xy + zw) * 2.0f;
        matrix[5] = 1.0f - ((xx + zz) * 2.0f);
        matrix[9] = (yz - xw) * 2.0f;
        matrix[13] = 0.0f;
        matrix[2] = (xz - yw) * 2.0f;
        matrix[6] = (yz + xw) * 2.0f;
        matrix[10] = 1.0f - ((xx + yy) * 2.0f);
        matrix[14] = 0.0f;
        matrix[3] = 0.0f;
        matrix[7] = 0.0f;
        matrix[11] = 0.0f;
        matrix[15] = 1.0f;
    }

    public Quaternion idt() {
        return set(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public boolean isIdentity() {
        return MathUtils.isZero(this.x) && MathUtils.isZero(this.y) && MathUtils.isZero(this.z) && MathUtils.isEqual(this.w, 1.0f);
    }

    public boolean isIdentity(float tolerance) {
        return MathUtils.isZero(this.x, tolerance) && MathUtils.isZero(this.y, tolerance) && MathUtils.isZero(this.z, tolerance) && MathUtils.isEqual(this.w, 1.0f, tolerance);
    }

    public Quaternion setFromAxis(Vector3 axis, float degrees) {
        return setFromAxis(axis.x, axis.y, axis.z, degrees);
    }

    public Quaternion setFromAxisRad(Vector3 axis, float radians) {
        return setFromAxisRad(axis.x, axis.y, axis.z, radians);
    }

    public Quaternion setFromAxis(float x2, float y2, float z2, float degrees) {
        return setFromAxisRad(x2, y2, z2, 0.017453292f * degrees);
    }

    public Quaternion setFromAxisRad(float x2, float y2, float z2, float radians) {
        float d = Vector3.len(x2, y2, z2);
        if (d == 0.0f) {
            return idt();
        }
        float d2 = 1.0f / d;
        float l_ang = radians < 0.0f ? 6.2831855f - ((-radians) % 6.2831855f) : radians % 6.2831855f;
        float l_sin = (float) Math.sin((double) (l_ang / 2.0f));
        return set(d2 * x2 * l_sin, d2 * y2 * l_sin, d2 * z2 * l_sin, (float) Math.cos((double) (l_ang / 2.0f))).nor();
    }

    public Quaternion setFromMatrix(boolean normalizeAxes, Matrix4 matrix) {
        return setFromAxes(normalizeAxes, matrix.val[0], matrix.val[4], matrix.val[8], matrix.val[1], matrix.val[5], matrix.val[9], matrix.val[2], matrix.val[6], matrix.val[10]);
    }

    public Quaternion setFromMatrix(Matrix4 matrix) {
        return setFromMatrix(false, matrix);
    }

    public Quaternion setFromMatrix(boolean normalizeAxes, Matrix3 matrix) {
        return setFromAxes(normalizeAxes, matrix.val[0], matrix.val[3], matrix.val[6], matrix.val[1], matrix.val[4], matrix.val[7], matrix.val[2], matrix.val[5], matrix.val[8]);
    }

    public Quaternion setFromMatrix(Matrix3 matrix) {
        return setFromMatrix(false, matrix);
    }

    public Quaternion setFromAxes(float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        return setFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz);
    }

    public Quaternion setFromAxes(boolean normalizeAxes, float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        float zz2;
        float zy2;
        float zx2;
        float yz2;
        float yy2;
        float yx2;
        float xz2;
        float xy2;
        float xx2;
        if (normalizeAxes) {
            float lx = 1.0f / Vector3.len(xx, xy, xz);
            float ly = 1.0f / Vector3.len(yx, yy, yz);
            float lz = 1.0f / Vector3.len(zx, zy, zz);
            xx2 = xx * lx;
            xy2 = xy * lx;
            xz2 = xz * lx;
            yx2 = yx * ly;
            yy2 = yy * ly;
            yz2 = yz * ly;
            zx2 = zx * lz;
            zy2 = zy * lz;
            zz2 = zz * lz;
        } else {
            xx2 = xx;
            xy2 = xy;
            xz2 = xz;
            yx2 = yx;
            yy2 = yy;
            yz2 = yz;
            zx2 = zx;
            zy2 = zy;
            zz2 = zz;
        }
        float t = xx2 + yy2 + zz2;
        if (t >= 0.0f) {
            float s = (float) Math.sqrt((double) (1.0f + t));
            this.w = s * 0.5f;
            float s2 = 0.5f / s;
            this.x = (zy2 - yz2) * s2;
            this.y = (xz2 - zx2) * s2;
            this.z = (yx2 - xy2) * s2;
            float f = t;
        } else if (xx2 <= yy2 || xx2 <= zz2) {
            if (yy2 > zz2) {
                double d = (double) yy2;
                Double.isNaN(d);
                double d2 = (double) xx2;
                Double.isNaN(d2);
                double d3 = (d + 1.0d) - d2;
                double d4 = (double) zz2;
                Double.isNaN(d4);
                float s3 = (float) Math.sqrt(d3 - d4);
                this.y = s3 * 0.5f;
                float s4 = 0.5f / s3;
                this.x = (yx2 + xy2) * s4;
                this.z = (zy2 + yz2) * s4;
                this.w = (xz2 - zx2) * s4;
            } else {
                double d5 = (double) zz2;
                Double.isNaN(d5);
                double d6 = (double) xx2;
                Double.isNaN(d6);
                double d7 = (d5 + 1.0d) - d6;
                double d8 = (double) yy2;
                Double.isNaN(d8);
                float s5 = (float) Math.sqrt(d7 - d8);
                this.z = s5 * 0.5f;
                float s6 = 0.5f / s5;
                this.x = (xz2 + zx2) * s6;
                this.y = (zy2 + yz2) * s6;
                this.w = (yx2 - xy2) * s6;
            }
        } else {
            float f2 = t;
            double d9 = (double) xx2;
            Double.isNaN(d9);
            double d10 = (double) yy2;
            Double.isNaN(d10);
            double d11 = (d9 + 1.0d) - d10;
            double d12 = (double) zz2;
            Double.isNaN(d12);
            float s7 = (float) Math.sqrt(d11 - d12);
            this.x = s7 * 0.5f;
            float s8 = 0.5f / s7;
            this.y = (yx2 + xy2) * s8;
            this.z = (xz2 + zx2) * s8;
            this.w = (zy2 - yz2) * s8;
        }
        return this;
    }

    public Quaternion setFromCross(Vector3 v1, Vector3 v2) {
        return setFromAxisRad((v1.y * v2.z) - (v1.z * v2.y), (v1.z * v2.x) - (v1.x * v2.z), (v1.x * v2.y) - (v1.y * v2.x), (float) Math.acos((double) MathUtils.clamp(v1.dot(v2), -1.0f, 1.0f)));
    }

    public Quaternion setFromCross(float x1, float y1, float z1, float x2, float y2, float z2) {
        return setFromAxisRad((y1 * z2) - (z1 * y2), (z1 * x2) - (x1 * z2), (x1 * y2) - (y1 * x2), (float) Math.acos((double) MathUtils.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1.0f, 1.0f)));
    }

    public Quaternion slerp(Quaternion end, float alpha) {
        float d = (this.x * end.x) + (this.y * end.y) + (this.z * end.z) + (this.w * end.w);
        float absDot = d < 0.0f ? -d : d;
        float scale0 = 1.0f - alpha;
        float scale1 = alpha;
        if (((double) (1.0f - absDot)) > 0.1d) {
            float angle = (float) Math.acos((double) absDot);
            float invSinTheta = 1.0f / ((float) Math.sin((double) angle));
            scale0 = ((float) Math.sin((double) ((1.0f - alpha) * angle))) * invSinTheta;
            scale1 = ((float) Math.sin((double) (alpha * angle))) * invSinTheta;
        }
        if (d < 0.0f) {
            scale1 = -scale1;
        }
        this.x = (this.x * scale0) + (end.x * scale1);
        this.y = (this.y * scale0) + (end.y * scale1);
        this.z = (this.z * scale0) + (end.z * scale1);
        this.w = (this.w * scale0) + (end.w * scale1);
        return this;
    }

    public Quaternion slerp(Quaternion[] q) {
        float w2 = 1.0f / ((float) q.length);
        set(q[0]).exp(w2);
        for (int i = 1; i < q.length; i++) {
            mul(tmp1.set(q[i]).exp(w2));
        }
        nor();
        return this;
    }

    public Quaternion slerp(Quaternion[] q, float[] w2) {
        set(q[0]).exp(w2[0]);
        for (int i = 1; i < q.length; i++) {
            mul(tmp1.set(q[i]).exp(w2[i]));
        }
        nor();
        return this;
    }

    public Quaternion exp(float alpha) {
        float coeff;
        float norm = len();
        float normExp = (float) Math.pow((double) norm, (double) alpha);
        float theta = (float) Math.acos((double) (this.w / norm));
        if (((double) Math.abs(theta)) < 0.001d) {
            coeff = (normExp * alpha) / norm;
        } else {
            double d = (double) normExp;
            double sin = Math.sin((double) (alpha * theta));
            Double.isNaN(d);
            double d2 = d * sin;
            double d3 = (double) norm;
            double sin2 = Math.sin((double) theta);
            Double.isNaN(d3);
            coeff = (float) (d2 / (d3 * sin2));
        }
        double d4 = (double) normExp;
        double cos = Math.cos((double) (alpha * theta));
        Double.isNaN(d4);
        this.w = (float) (d4 * cos);
        this.x *= coeff;
        this.y *= coeff;
        this.z *= coeff;
        nor();
        return this;
    }

    public int hashCode() {
        return (((((((1 * 31) + NumberUtils.floatToRawIntBits(this.w)) * 31) + NumberUtils.floatToRawIntBits(this.x)) * 31) + NumberUtils.floatToRawIntBits(this.y)) * 31) + NumberUtils.floatToRawIntBits(this.z);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Quaternion)) {
            return false;
        }
        Quaternion other = (Quaternion) obj;
        if (NumberUtils.floatToRawIntBits(this.w) == NumberUtils.floatToRawIntBits(other.w) && NumberUtils.floatToRawIntBits(this.x) == NumberUtils.floatToRawIntBits(other.x) && NumberUtils.floatToRawIntBits(this.y) == NumberUtils.floatToRawIntBits(other.y) && NumberUtils.floatToRawIntBits(this.z) == NumberUtils.floatToRawIntBits(other.z)) {
            return true;
        }
        return false;
    }

    public static final float dot(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        return (x1 * x2) + (y1 * y2) + (z1 * z2) + (w1 * w2);
    }

    public float dot(Quaternion other) {
        return (this.x * other.x) + (this.y * other.y) + (this.z * other.z) + (this.w * other.w);
    }

    public float dot(float x2, float y2, float z2, float w2) {
        return (this.x * x2) + (this.y * y2) + (this.z * z2) + (this.w * w2);
    }

    public Quaternion mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        this.w *= scalar;
        return this;
    }

    public float getAxisAngle(Vector3 axis) {
        return getAxisAngleRad(axis) * 57.295776f;
    }

    public float getAxisAngleRad(Vector3 axis) {
        if (this.w > 1.0f) {
            nor();
        }
        float angle = (float) (Math.acos((double) this.w) * 2.0d);
        float f = this.w;
        double s = Math.sqrt((double) (1.0f - (f * f)));
        if (s < 9.999999974752427E-7d) {
            axis.x = this.x;
            axis.y = this.y;
            axis.z = this.z;
        } else {
            double d = (double) this.x;
            Double.isNaN(d);
            axis.x = (float) (d / s);
            double d2 = (double) this.y;
            Double.isNaN(d2);
            axis.y = (float) (d2 / s);
            double d3 = (double) this.z;
            Double.isNaN(d3);
            axis.z = (float) (d3 / s);
        }
        return angle;
    }

    public float getAngleRad() {
        float f = this.w;
        if (f > 1.0f) {
            f /= len();
        }
        return (float) (Math.acos((double) f) * 2.0d);
    }

    public float getAngle() {
        return getAngleRad() * 57.295776f;
    }

    public void getSwingTwist(float axisX, float axisY, float axisZ, Quaternion swing, Quaternion twist) {
        float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        twist.set(axisX * d, axisY * d, axisZ * d, this.w).nor();
        if (d < 0.0f) {
            twist.mul(-1.0f);
        }
        swing.set(twist).conjugate().mulLeft(this);
    }

    public void getSwingTwist(Vector3 axis, Quaternion swing, Quaternion twist) {
        getSwingTwist(axis.x, axis.y, axis.z, swing, twist);
    }

    public float getAngleAroundRad(float axisX, float axisY, float axisZ) {
        float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        float l2 = len2(axisX * d, axisY * d, axisZ * d, this.w);
        if (MathUtils.isZero(l2)) {
            return 0.0f;
        }
        double d2 = (double) (d < 0.0f ? -this.w : this.w);
        double sqrt = Math.sqrt((double) l2);
        Double.isNaN(d2);
        return (float) (Math.acos((double) MathUtils.clamp((float) (d2 / sqrt), -1.0f, 1.0f)) * 2.0d);
    }

    public float getAngleAroundRad(Vector3 axis) {
        return getAngleAroundRad(axis.x, axis.y, axis.z);
    }

    public float getAngleAround(float axisX, float axisY, float axisZ) {
        return getAngleAroundRad(axisX, axisY, axisZ) * 57.295776f;
    }

    public float getAngleAround(Vector3 axis) {
        return getAngleAround(axis.x, axis.y, axis.z);
    }
}
