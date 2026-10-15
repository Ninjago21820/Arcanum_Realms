package com.arcanumrealms.client.model;

import com.arcanumrealms.ArcanumRealms;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * La tenue de sorcier en volume : chapeau, cape d'epaules, manches, ceinture et
 * jupe evasee.
 *
 * <p>Le modele est construit en cubes supplementaires poses autour du
 * personnage, exactement comme les couches exterieures d'une peau, mais avec de
 * <b>vraies formes</b> : un chapeau pointu en cinq etages, une cape qui depasse
 * des epaules, une jupe qui s'evase vers le sol et des manches plus larges que
 * les bras. Rien n'est plat : chaque piece a une epaisseur.
 *
 * <p>Les pieces suivent le corps : la cape et la jupe sont attachees au torse,
 * les manches aux bras, les chapeaux a la tete. Un personnage qui marche, tourne
 * la tete ou agite les bras emmene donc sa tenue avec lui.
 *
 * <p>La texture est en niveaux de gris : elle est teintee a l'affichage par la
 * couleur de l'ecole du joueur ou par la couleur de tenue du PNJ. Une seule
 * image suffit donc pour toutes les robes, et un pack de ressources peut la
 * remplacer.
 *
 * <p>Repère du modele : les pieds sont a y = 24, la taille a y = 12, les epaules
 * a y = 0, le sommet du crane a y = -8 (l'axe vertical descend, comme dans tous
 * les modeles de Minecraft). Chaque piece est decrite par une ligne
 * {@code box(...)} : le generateur d'apparences relit ces lignes pour dessiner
 * la texture et les apercus, donc les deux ne peuvent pas diverger.
 */
public class WizardRobeModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(ArcanumRealms.MOD_ID, "wizard_robe"), "main");

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/wizard/robe.png");

    /** Pieces qui suivent la tete. */
    public final net.minecraft.client.model.geom.ModelPart head;
    /** Pieces qui suivent le torse : col, cape, ceinture et jupe. */
    public final net.minecraft.client.model.geom.ModelPart body;
    public final net.minecraft.client.model.geom.ModelPart rightArm;
    public final net.minecraft.client.model.geom.ModelPart leftArm;

    public WizardRobeModel(net.minecraft.client.model.geom.ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    /**
     * Les cubes du modele.
     *
     * <p>Chaque appel ajoute une piece : {@code box(partie, nom, u, v, x, y, z, largeur,
     * hauteur, profondeur)}, ou {@code u, v} est le coin de la piece dans la texture et
     * {@code x, y, z} le coin inferieur de la piece dans le repere du modele.
     */
    public static LayerDefinition createBodyLayer() {
        net.minecraft.client.model.geom.builders.MeshDefinition mesh =
                new net.minecraft.client.model.geom.builders.MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);

        // --- chapeau pointu : un bord, puis trois etages de plus en plus etroits.
        // Le chapeau fait environ la hauteur de la tete (8 unites) : plus grand, il
        // ecrase le personnage.
        box(head, "hat_brim", 0, 0, -5F, -9.5F, -5F, 10F, 1F, 10F);
        box(head, "hat_cone_1", 44, 0, -4F, -11.5F, -4F, 8F, 2F, 8F);
        box(head, "hat_cone_2", 76, 0, -3F, -13.5F, -3F, 6F, 2F, 6F);
        box(head, "hat_cone_3", 96, 0, -2F, -15.5F, -2F, 4F, 2F, 4F);
        box(head, "hat_tip", 112, 0, -1F, -17.5F, -1F, 2F, 2F, 2F);

        // --- capuche : elle coiffe l'arriere du crane et les cotes, et s'arrete au
        // niveau du visage (devant, le tissu ne depasse pas z = 0,5), sinon elle
        // avale la tete
        box(head, "hood_back", 0, 16, -5.2F, -9.5F, -6F, 10.4F, 10F, 6.5F);
        box(body, "hood_cowl", 92, 40, -5.5F, -1.5F, -3.5F, 11F, 4F, 7F);

        // --- casque de garde : calotte, nasal et protege-joues
        box(head, "helm_cap", 44, 16, -5F, -9.5F, -5F, 10F, 4F, 10F);
        box(head, "helm_nose", 88, 16, -1F, -8.6F, 4F, 2F, 5F, 2F);
        box(head, "helm_cheek", 100, 16, -5F, -7F, -5F, 2F, 5F, 10F);
        box(head, "helm_cheek_2", 100, 16, 3F, -7F, -5F, 2F, 5F, 10F);

        // --- petit chapeau souple : calotte et bord rabattu vers l'avant
        box(head, "cap_crown", 0, 40, -5F, -11F, -5F, 10F, 3F, 10F);
        box(head, "cap_brim", 44, 40, -5.5F, -9.6F, -10F, 11F, 1F, 7F);

        // --- chapeau de paille : large bord qui depasse sur les cotes
        box(head, "straw_crown", 0, 56, -5F, -11.5F, -5F, 10F, 4F, 10F);
        box(head, "straw_brim", 44, 56, -7.5F, -9.5F, -7.5F, 15F, 1F, 15F);

        // --- col : une petite collerette qui marque les epaules
        box(body, "collar", 0, 72, -3F, -1.5F, -3F, 6F, 3F, 6F);

        // --- torse de la robe : ajuste au corps (0,25 unite de marge, comme la veste
        // d'une peau), du col a la ceinture
        box(body, "torso", 28, 72, -4.25F, -0.5F, -2.25F, 8.5F, 11.5F, 4.5F);

        // --- cape d'epaules : elle depasse du torse et s'arrete vite, sinon la robe
        // devient un tonneau
        box(body, "mantle", 60, 72, -5F, -1.5F, -3.2F, 10F, 3F, 6.4F);
        box(body, "mantle_trim", 92, 72, -4.4F, 1.5F, -2.8F, 8.8F, 1F, 5.6F);

        // --- ceinture et boucle
        box(body, "belt", 0, 92, -4.9F, 9F, -3F, 9.8F, 2F, 6F);
        box(body, "buckle", 32, 92, -1.2F, 9.4F, 3F, 2.4F, 1.4F, 0.6F);

        // --- jupe : elle s'evase vers le sol en deux etages, sans couvrir les bottes
        box(body, "skirt_top", 44, 92, -4.6F, 11F, -2.7F, 9.2F, 5F, 5.4F);
        box(body, "skirt_bottom", 78, 92, -5.4F, 16F, -3.3F, 10.8F, 4.5F, 6.6F);

        // --- manches : plus larges que les bras, elles s'arretent avant les mains,
        // avec un revers au poignet
        box(rightArm, "sleeve_right", 0, 108, -3.3F, -2.3F, -2.3F, 4.6F, 9F, 4.6F);
        box(rightArm, "cuff_right", 24, 108, -3.6F, 5.5F, -2.6F, 5.2F, 3F, 5.2F);
        box(leftArm, "sleeve_left", 0, 108, -1.3F, -2.3F, -2.3F, 4.6F, 9F, 4.6F);
        box(leftArm, "cuff_left", 24, 108, -1.6F, 5.5F, -2.6F, 5.2F, 3F, 5.2F);

        return LayerDefinition.create(mesh, 128, 128);
    }

    /** Ajoute une piece au modele (voir la convention decrite plus haut). */
    private static void box(PartDefinition part, String name, int u, int v,
                            float x, float y, float z, float width, float height, float depth) {
        part.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth),
                PartPose.ZERO);
    }

    /** Affiche ou masque une piece (les chapeaux non portes ne doivent pas se voir). */
    public void setVisible(String piece, boolean visible) {
        net.minecraft.client.model.geom.ModelPart part = switch (piece) {
            case "right_arm" -> this.rightArm;
            case "left_arm" -> this.leftArm;
            case "body" -> this.body;
            default -> this.head;
        };
        net.minecraft.client.model.geom.ModelPart child = part.getChild(piece);
        if (child != null) child.visible = visible;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        this.head.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.body.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.rightArm.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.leftArm.render(pose, consumer, light, overlay, red, green, blue, alpha);
    }

    public static ResourceLocation texture() {
        return TEXTURE;
    }
}
