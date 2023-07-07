package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import java.nio.Buffer;
import java.util.Iterator;

public class Model implements Disposable {
    public final Array<Animation> animations;
    protected final Array<Disposable> disposables;
    public final Array<Material> materials;
    public final Array<MeshPart> meshParts;
    public final Array<Mesh> meshes;
    private ObjectMap<NodePart, ArrayMap<String, Matrix4>> nodePartBones;
    public final Array<Node> nodes;

    public Model() {
        this.materials = new Array<>();
        this.nodes = new Array<>();
        this.animations = new Array<>();
        this.meshes = new Array<>();
        this.meshParts = new Array<>();
        this.disposables = new Array<>();
        this.nodePartBones = new ObjectMap<>();
    }

    public Model(ModelData modelData) {
        this(modelData, new TextureProvider.FileTextureProvider());
    }

    public Model(ModelData modelData, TextureProvider textureProvider) {
        this.materials = new Array<>();
        this.nodes = new Array<>();
        this.animations = new Array<>();
        this.meshes = new Array<>();
        this.meshParts = new Array<>();
        this.disposables = new Array<>();
        this.nodePartBones = new ObjectMap<>();
        load(modelData, textureProvider);
    }

    /* access modifiers changed from: protected */
    public void load(ModelData modelData, TextureProvider textureProvider) {
        loadMeshes(modelData.meshes);
        loadMaterials(modelData.materials, textureProvider);
        loadNodes(modelData.nodes);
        loadAnimations(modelData.animations);
        calculateTransforms();
    }

    /* access modifiers changed from: protected */
    public void loadAnimations(Iterable<ModelAnimation> modelAnimations) {
        for (ModelAnimation anim : modelAnimations) {
            Animation animation = new Animation();
            animation.id = anim.id;
            Iterator<ModelNodeAnimation> it = anim.nodeAnimations.iterator();
            while (it.hasNext()) {
                ModelNodeAnimation nanim = it.next();
                Node node = getNode(nanim.nodeId);
                if (node != null) {
                    NodeAnimation nodeAnim = new NodeAnimation();
                    nodeAnim.node = node;
                    if (nanim.translation != null) {
                        nodeAnim.translation = new Array<>();
                        nodeAnim.translation.ensureCapacity(nanim.translation.size);
                        Iterator<ModelNodeKeyframe<Vector3>> it2 = nanim.translation.iterator();
                        while (it2.hasNext()) {
                            ModelNodeKeyframe<Vector3> kf = it2.next();
                            if (kf.keytime > animation.duration) {
                                animation.duration = kf.keytime;
                            }
                            nodeAnim.translation.add(new NodeKeyframe(kf.keytime, new Vector3(kf.value == null ? node.translation : (Vector3) kf.value)));
                        }
                    }
                    if (nanim.rotation != null) {
                        nodeAnim.rotation = new Array<>();
                        nodeAnim.rotation.ensureCapacity(nanim.rotation.size);
                        Iterator<ModelNodeKeyframe<Quaternion>> it3 = nanim.rotation.iterator();
                        while (it3.hasNext()) {
                            ModelNodeKeyframe<Quaternion> kf2 = it3.next();
                            if (kf2.keytime > animation.duration) {
                                animation.duration = kf2.keytime;
                            }
                            nodeAnim.rotation.add(new NodeKeyframe(kf2.keytime, new Quaternion(kf2.value == null ? node.rotation : (Quaternion) kf2.value)));
                        }
                    }
                    if (nanim.scaling != null) {
                        nodeAnim.scaling = new Array<>();
                        nodeAnim.scaling.ensureCapacity(nanim.scaling.size);
                        Iterator<ModelNodeKeyframe<Vector3>> it4 = nanim.scaling.iterator();
                        while (it4.hasNext()) {
                            ModelNodeKeyframe<Vector3> kf3 = it4.next();
                            if (kf3.keytime > animation.duration) {
                                animation.duration = kf3.keytime;
                            }
                            nodeAnim.scaling.add(new NodeKeyframe(kf3.keytime, new Vector3(kf3.value == null ? node.scale : (Vector3) kf3.value)));
                        }
                    }
                    if ((nodeAnim.translation != null && nodeAnim.translation.size > 0) || ((nodeAnim.rotation != null && nodeAnim.rotation.size > 0) || (nodeAnim.scaling != null && nodeAnim.scaling.size > 0))) {
                        animation.nodeAnimations.add(nodeAnim);
                    }
                }
            }
            if (animation.nodeAnimations.size > 0) {
                this.animations.add(animation);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void loadNodes(Iterable<ModelNode> modelNodes) {
        this.nodePartBones.clear();
        for (ModelNode node : modelNodes) {
            this.nodes.add(loadNode(node));
        }
        ObjectMap.Entries<NodePart, ArrayMap<String, Matrix4>> it = this.nodePartBones.entries().iterator();
        while (it.hasNext()) {
            ObjectMap.Entry<NodePart, ArrayMap<String, Matrix4>> e = (ObjectMap.Entry) it.next();
            if (((NodePart) e.key).invBoneBindTransforms == null) {
                ((NodePart) e.key).invBoneBindTransforms = new ArrayMap<>(Node.class, Matrix4.class);
            }
            ((NodePart) e.key).invBoneBindTransforms.clear();
            Iterator it2 = ((ArrayMap) e.value).entries().iterator();
            while (it2.hasNext()) {
                ObjectMap.Entry<String, Matrix4> b = (ObjectMap.Entry) it2.next();
                ((NodePart) e.key).invBoneBindTransforms.put(getNode((String) b.key), new Matrix4((Matrix4) b.value).inv());
            }
        }
    }

    /* access modifiers changed from: protected */
    public Node loadNode(ModelNode modelNode) {
        Node node = new Node();
        node.id = modelNode.id;
        if (modelNode.translation != null) {
            node.translation.set(modelNode.translation);
        }
        if (modelNode.rotation != null) {
            node.rotation.set(modelNode.rotation);
        }
        if (modelNode.scale != null) {
            node.scale.set(modelNode.scale);
        }
        if (modelNode.parts != null) {
            for (ModelNodePart modelNodePart : modelNode.parts) {
                MeshPart meshPart = null;
                Material meshMaterial = null;
                if (modelNodePart.meshPartId != null) {
                    Iterator<MeshPart> it = this.meshParts.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        MeshPart part = it.next();
                        if (modelNodePart.meshPartId.equals(part.id)) {
                            meshPart = part;
                            break;
                        }
                    }
                }
                if (modelNodePart.materialId != null) {
                    Iterator<Material> it2 = this.materials.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        Material material = it2.next();
                        if (modelNodePart.materialId.equals(material.id)) {
                            meshMaterial = material;
                            break;
                        }
                    }
                }
                if (meshPart == null || meshMaterial == null) {
                    throw new GdxRuntimeException("Invalid node: " + node.id);
                }
                if (!(meshPart == null || meshMaterial == null)) {
                    NodePart nodePart = new NodePart();
                    nodePart.meshPart = meshPart;
                    nodePart.material = meshMaterial;
                    node.parts.add(nodePart);
                    if (modelNodePart.bones != null) {
                        this.nodePartBones.put(nodePart, modelNodePart.bones);
                    }
                }
            }
        }
        if (modelNode.children != null) {
            for (ModelNode child : modelNode.children) {
                node.addChild(loadNode(child));
            }
        }
        return node;
    }

    /* access modifiers changed from: protected */
    public void loadMeshes(Iterable<ModelMesh> meshes2) {
        for (ModelMesh mesh : meshes2) {
            convertMesh(mesh);
        }
    }

    /* access modifiers changed from: protected */
    public void convertMesh(ModelMesh modelMesh) {
        int numIndices = 0;
        for (ModelMeshPart part : modelMesh.parts) {
            numIndices += part.indices.length;
        }
        boolean hasIndices = numIndices > 0;
        VertexAttributes attributes = new VertexAttributes(modelMesh.attributes);
        int numVertices = modelMesh.vertices.length / (attributes.vertexSize / 4);
        Mesh mesh = new Mesh(true, numVertices, numIndices, attributes);
        this.meshes.add(mesh);
        this.disposables.add(mesh);
        BufferUtils.copy(modelMesh.vertices, (Buffer) mesh.getVerticesBuffer(), modelMesh.vertices.length, 0);
        mesh.getIndicesBuffer().clear();
        int offset = 0;
        for (ModelMeshPart part2 : modelMesh.parts) {
            MeshPart meshPart = new MeshPart();
            meshPart.id = part2.id;
            meshPart.primitiveType = part2.primitiveType;
            meshPart.offset = offset;
            meshPart.size = hasIndices ? part2.indices.length : numVertices;
            meshPart.mesh = mesh;
            if (hasIndices) {
                mesh.getIndicesBuffer().put(part2.indices);
            }
            offset += meshPart.size;
            this.meshParts.add(meshPart);
        }
        mesh.getIndicesBuffer().position(0);
        Iterator<MeshPart> it = this.meshParts.iterator();
        while (it.hasNext()) {
            it.next().update();
        }
    }

    /* access modifiers changed from: protected */
    public void loadMaterials(Iterable<ModelMaterial> modelMaterials, TextureProvider textureProvider) {
        for (ModelMaterial mtl : modelMaterials) {
            this.materials.add(convertMaterial(mtl, textureProvider));
        }
    }

    /* access modifiers changed from: protected */
    public Material convertMaterial(ModelMaterial mtl, TextureProvider textureProvider) {
        Texture texture;
        ModelMaterial modelMaterial = mtl;
        Material result = new Material();
        result.id = modelMaterial.id;
        if (modelMaterial.ambient != null) {
            result.set((Attribute) new ColorAttribute(ColorAttribute.Ambient, modelMaterial.ambient));
        }
        if (modelMaterial.diffuse != null) {
            result.set((Attribute) new ColorAttribute(ColorAttribute.Diffuse, modelMaterial.diffuse));
        }
        if (modelMaterial.specular != null) {
            result.set((Attribute) new ColorAttribute(ColorAttribute.Specular, modelMaterial.specular));
        }
        if (modelMaterial.emissive != null) {
            result.set((Attribute) new ColorAttribute(ColorAttribute.Emissive, modelMaterial.emissive));
        }
        if (modelMaterial.reflection != null) {
            result.set((Attribute) new ColorAttribute(ColorAttribute.Reflection, modelMaterial.reflection));
        }
        if (modelMaterial.shininess > 0.0f) {
            result.set((Attribute) new FloatAttribute(FloatAttribute.Shininess, modelMaterial.shininess));
        }
        if (modelMaterial.opacity != 1.0f) {
            result.set((Attribute) new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, modelMaterial.opacity));
        }
        ObjectMap<String, Texture> textures = new ObjectMap<>();
        if (modelMaterial.textures != null) {
            Iterator<ModelTexture> it = modelMaterial.textures.iterator();
            while (it.hasNext()) {
                ModelTexture tex = it.next();
                if (textures.containsKey(tex.fileName)) {
                    texture = textures.get(tex.fileName);
                    TextureProvider textureProvider2 = textureProvider;
                } else {
                    texture = textureProvider.load(tex.fileName);
                    textures.put(tex.fileName, texture);
                    this.disposables.add(texture);
                }
                TextureDescriptor descriptor = new TextureDescriptor(texture);
                descriptor.minFilter = texture.getMinFilter();
                descriptor.magFilter = texture.getMagFilter();
                descriptor.uWrap = texture.getUWrap();
                descriptor.vWrap = texture.getVWrap();
                float offsetU = tex.uvTranslation == null ? 0.0f : tex.uvTranslation.x;
                float offsetV = tex.uvTranslation == null ? 0.0f : tex.uvTranslation.y;
                float scaleU = tex.uvScaling == null ? 1.0f : tex.uvScaling.x;
                float scaleV = tex.uvScaling == null ? 1.0f : tex.uvScaling.y;
                int i = tex.usage;
                if (i == 2) {
                    result.set((Attribute) new TextureAttribute(TextureAttribute.Diffuse, descriptor, offsetU, offsetV, scaleU, scaleV));
                } else if (i == 3) {
                    result.set((Attribute) new TextureAttribute(TextureAttribute.Emissive, descriptor, offsetU, offsetV, scaleU, scaleV));
                } else if (i == 4) {
                    result.set((Attribute) new TextureAttribute(TextureAttribute.Ambient, descriptor, offsetU, offsetV, scaleU, scaleV));
                } else if (i == 5) {
                    result.set((Attribute) new TextureAttribute(TextureAttribute.Specular, descriptor, offsetU, offsetV, scaleU, scaleV));
                } else if (i == 7) {
                    result.set((Attribute) new TextureAttribute(TextureAttribute.Normal, descriptor, offsetU, offsetV, scaleU, scaleV));
                } else if (i == 8) {
                    result.set((Attribute) new TextureAttribute(TextureAttribute.Bump, descriptor, offsetU, offsetV, scaleU, scaleV));
                } else if (i == 10) {
                    TextureAttribute textureAttribute = r11;
                    TextureAttribute textureAttribute2 = new TextureAttribute(TextureAttribute.Reflection, descriptor, offsetU, offsetV, scaleU, scaleV);
                    result.set((Attribute) textureAttribute);
                }
            }
            TextureProvider textureProvider3 = textureProvider;
        } else {
            TextureProvider textureProvider4 = textureProvider;
        }
        return result;
    }

    public void manageDisposable(Disposable disposable) {
        if (!this.disposables.contains(disposable, true)) {
            this.disposables.add(disposable);
        }
    }

    public Iterable<Disposable> getManagedDisposables() {
        return this.disposables;
    }

    public void dispose() {
        Iterator<Disposable> it = this.disposables.iterator();
        while (it.hasNext()) {
            it.next().dispose();
        }
    }

    public void calculateTransforms() {
        int n = this.nodes.size;
        for (int i = 0; i < n; i++) {
            this.nodes.get(i).calculateTransforms(true);
        }
        for (int i2 = 0; i2 < n; i2++) {
            this.nodes.get(i2).calculateBoneTransforms(true);
        }
    }

    public BoundingBox calculateBoundingBox(BoundingBox out) {
        out.inf();
        return extendBoundingBox(out);
    }

    public BoundingBox extendBoundingBox(BoundingBox out) {
        int n = this.nodes.size;
        for (int i = 0; i < n; i++) {
            this.nodes.get(i).extendBoundingBox(out);
        }
        return out;
    }

    public Animation getAnimation(String id) {
        return getAnimation(id, true);
    }

    public Animation getAnimation(String id, boolean ignoreCase) {
        int n = this.animations.size;
        if (ignoreCase) {
            for (int i = 0; i < n; i++) {
                Animation animation = this.animations.get(i);
                Animation animation2 = animation;
                if (animation.id.equalsIgnoreCase(id)) {
                    return animation2;
                }
            }
            return null;
        }
        for (int i2 = 0; i2 < n; i2++) {
            Animation animation3 = this.animations.get(i2);
            Animation animation4 = animation3;
            if (animation3.id.equals(id)) {
                return animation4;
            }
        }
        return null;
    }

    public Material getMaterial(String id) {
        return getMaterial(id, true);
    }

    public Material getMaterial(String id, boolean ignoreCase) {
        int n = this.materials.size;
        if (ignoreCase) {
            for (int i = 0; i < n; i++) {
                Material material = this.materials.get(i);
                Material material2 = material;
                if (material.id.equalsIgnoreCase(id)) {
                    return material2;
                }
            }
            return null;
        }
        for (int i2 = 0; i2 < n; i2++) {
            Material material3 = this.materials.get(i2);
            Material material4 = material3;
            if (material3.id.equals(id)) {
                return material4;
            }
        }
        return null;
    }

    public Node getNode(String id) {
        return getNode(id, true);
    }

    public Node getNode(String id, boolean recursive) {
        return getNode(id, recursive, false);
    }

    public Node getNode(String id, boolean recursive, boolean ignoreCase) {
        return Node.getNode(this.nodes, id, recursive, ignoreCase);
    }
}
