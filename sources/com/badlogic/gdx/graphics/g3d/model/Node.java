package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.Iterator;

public class Node {
    private final Array<Node> children = new Array<>(2);
    public final Matrix4 globalTransform = new Matrix4();
    public String id;
    public boolean inheritTransform = true;
    public boolean isAnimated;
    public final Matrix4 localTransform = new Matrix4();
    protected Node parent;
    public Array<NodePart> parts = new Array<>(2);
    public final Quaternion rotation = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    public final Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);
    public final Vector3 translation = new Vector3();

    public Matrix4 calculateLocalTransform() {
        if (!this.isAnimated) {
            this.localTransform.set(this.translation, this.rotation, this.scale);
        }
        return this.localTransform;
    }

    public Matrix4 calculateWorldTransform() {
        Node node;
        if (!this.inheritTransform || (node = this.parent) == null) {
            this.globalTransform.set(this.localTransform);
        } else {
            this.globalTransform.set(node.globalTransform).mul(this.localTransform);
        }
        return this.globalTransform;
    }

    public void calculateTransforms(boolean recursive) {
        calculateLocalTransform();
        calculateWorldTransform();
        if (recursive) {
            Iterator<Node> it = this.children.iterator();
            while (it.hasNext()) {
                it.next().calculateTransforms(true);
            }
        }
    }

    public void calculateBoneTransforms(boolean recursive) {
        Iterator<NodePart> it = this.parts.iterator();
        while (it.hasNext()) {
            NodePart part = it.next();
            if (!(part.invBoneBindTransforms == null || part.bones == null || part.invBoneBindTransforms.size != part.bones.length)) {
                int n = part.invBoneBindTransforms.size;
                for (int i = 0; i < n; i++) {
                    part.bones[i].set(((Node[]) part.invBoneBindTransforms.keys)[i].globalTransform).mul(((Matrix4[]) part.invBoneBindTransforms.values)[i]);
                }
            }
        }
        if (recursive) {
            Iterator<Node> it2 = this.children.iterator();
            while (it2.hasNext()) {
                it2.next().calculateBoneTransforms(true);
            }
        }
    }

    public BoundingBox calculateBoundingBox(BoundingBox out) {
        out.inf();
        return extendBoundingBox(out);
    }

    public BoundingBox calculateBoundingBox(BoundingBox out, boolean transform) {
        out.inf();
        return extendBoundingBox(out, transform);
    }

    public BoundingBox extendBoundingBox(BoundingBox out) {
        return extendBoundingBox(out, true);
    }

    public BoundingBox extendBoundingBox(BoundingBox out, boolean transform) {
        int partCount = this.parts.size;
        for (int i = 0; i < partCount; i++) {
            NodePart part = this.parts.get(i);
            if (part.enabled) {
                MeshPart meshPart = part.meshPart;
                if (transform) {
                    meshPart.mesh.extendBoundingBox(out, meshPart.offset, meshPart.size, this.globalTransform);
                } else {
                    meshPart.mesh.extendBoundingBox(out, meshPart.offset, meshPart.size);
                }
            }
        }
        int childCount = this.children.size;
        for (int i2 = 0; i2 < childCount; i2++) {
            this.children.get(i2).extendBoundingBox(out);
        }
        return out;
    }

    public <T extends Node> void attachTo(T parent2) {
        parent2.addChild(this);
    }

    public void detach() {
        Node node = this.parent;
        if (node != null) {
            node.removeChild(this);
            this.parent = null;
        }
    }

    public boolean hasChildren() {
        Array<Node> array = this.children;
        return array != null && array.size > 0;
    }

    public int getChildCount() {
        return this.children.size;
    }

    public Node getChild(int index) {
        return this.children.get(index);
    }

    public Node getChild(String id2, boolean recursive, boolean ignoreCase) {
        return getNode(this.children, id2, recursive, ignoreCase);
    }

    public <T extends Node> int addChild(T child) {
        return insertChild(-1, child);
    }

    public <T extends Node> int addChildren(Iterable<T> nodes) {
        return insertChildren(-1, nodes);
    }

    public <T extends Node> int insertChild(int index, T child) {
        Node p = this;
        while (p != null) {
            if (p != child) {
                p = p.getParent();
            } else {
                throw new GdxRuntimeException("Cannot add a parent as a child");
            }
        }
        Node p2 = child.getParent();
        if (p2 == null || p2.removeChild(child)) {
            if (index < 0 || index >= this.children.size) {
                index = this.children.size;
                this.children.add(child);
            } else {
                this.children.insert(index, child);
            }
            child.parent = this;
            return index;
        }
        throw new GdxRuntimeException("Could not remove child from its current parent");
    }

    public <T extends Node> int insertChildren(int index, Iterable<T> nodes) {
        if (index < 0 || index > this.children.size) {
            index = this.children.size;
        }
        int i = index;
        for (T child : nodes) {
            insertChild(i, child);
            i++;
        }
        return index;
    }

    public <T extends Node> boolean removeChild(T child) {
        if (!this.children.removeValue(child, true)) {
            return false;
        }
        child.parent = null;
        return true;
    }

    public Iterable<Node> getChildren() {
        return this.children;
    }

    public Node getParent() {
        return this.parent;
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public Node copy() {
        return new Node().set(this);
    }

    /* access modifiers changed from: protected */
    public Node set(Node other) {
        detach();
        this.id = other.id;
        this.isAnimated = other.isAnimated;
        this.inheritTransform = other.inheritTransform;
        this.translation.set(other.translation);
        this.rotation.set(other.rotation);
        this.scale.set(other.scale);
        this.localTransform.set(other.localTransform);
        this.globalTransform.set(other.globalTransform);
        this.parts.clear();
        Iterator<NodePart> it = other.parts.iterator();
        while (it.hasNext()) {
            this.parts.add(it.next().copy());
        }
        this.children.clear();
        for (Node child : other.getChildren()) {
            addChild(child.copy());
        }
        return this;
    }

    public static Node getNode(Array<Node> nodes, String id2, boolean recursive, boolean ignoreCase) {
        int n = nodes.size;
        if (ignoreCase) {
            for (int i = 0; i < n; i++) {
                Node node = nodes.get(i);
                Node node2 = node;
                if (node.id.equalsIgnoreCase(id2)) {
                    return node2;
                }
            }
        } else {
            for (int i2 = 0; i2 < n; i2++) {
                Node node3 = nodes.get(i2);
                Node node4 = node3;
                if (node3.id.equals(id2)) {
                    return node4;
                }
            }
        }
        if (!recursive) {
            return null;
        }
        for (int i3 = 0; i3 < n; i3++) {
            Node node5 = getNode(nodes.get(i3).children, id2, true, ignoreCase);
            Node node6 = node5;
            if (node5 != null) {
                return node6;
            }
        }
        return null;
    }
}
