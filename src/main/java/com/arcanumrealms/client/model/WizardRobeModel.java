package com.arcanumrealms.client.model;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.core.world.Attire;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La tenue de sorcier : un modèle complet, décrit par une table de pièces.
 *
 * <p>Chaque ligne de {@link #PIECES} décrit une pièce, comme dans un modèle
 * Blockbench :
 *
 * <pre>
 * piece  &lt;partie&gt; &lt;nom&gt; &lt;matière&gt; &lt;u&gt; &lt;v&gt; &lt;x&gt; &lt;y&gt; &lt;z&gt; &lt;largeur&gt; &lt;hauteur&gt; &lt;profondeur&gt;
 * pivot  &lt;partie&gt; &lt;x&gt; &lt;y&gt; &lt;z&gt;
 * </pre>
 *
 * <p>La matière dit comment la pièce est teintée :
 *
 * <ul>
 *   <li>{@code cloth} : le tissu de la robe, <b>teinté</b> par la couleur de
 *       l'école du joueur ou de la tenue du PNJ ;</li>
 *   <li>{@code detail} : ce qui garde sa couleur propre — métal, cuir, galons,
 *       gemmes, bordure de capuche. Ces pièces sont dessinées avec leur propre
 *       texture, non teintée.</li>
 * </ul>
 *
 * <p>Un {@code pivot} donne à une partie son point de rotation (le haut de la
 * cape, la taille pour la jupe, le poignet pour les revers) : la partie peut
 * alors bouger toute seule, c'est ce qui permet les animations.
 *
 * <p>Repère : celui des modèles de Minecraft. Les épaules sont à {@code y = 0},
 * la taille à {@code y = 12}, les pieds à {@code y = 24}, et l'axe vertical
 * descend. Une partie qui n'est pas listée dans {@link #PIECES} n'est pas
 * dessinée.
 */
public class WizardRobeModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(ArcanumRealms.MOD_ID, "wizard_robe"), "main");

    /** Texture du tissu : en niveaux de gris, teintée à l'affichage. */
    public static final ResourceLocation CLOTH =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/wizard/cloth.png");

    /** Texture des détails : métal, cuir, galons. Jamais teintée. */
    public static final ResourceLocation DETAIL =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/wizard/detail.png");

    /** Taille des deux textures. */
    public static final int TEXTURE_SIZE = 256;

    /**
     * La table des pièces du modèle.
     *
     * <p>C'est ici que se règle l'allure : ajouter une pièce, élargir une manche
     * ou allonger une jupe se fait en changeant un nombre. Le générateur de
     * textures et les planches d'aperçu relisent cette table, donc rien ne peut
     * se désynchroniser.
     *
     * <p>Formes disponibles pour les coiffes : il n'y a qu'une coiffe portée à la
     * fois, choisie par {@code Attire} (voir {@code core/world/Attire.java}).
     */
    public static final String PIECES = """
            # --- chapeau pointu : un bord, puis six etages qui se resserrent
            piece head hat_brim cloth 0 0 -5.6 -9.4 -5.6 11.2 1 11.2
            piece head hat_a cloth 44 0 -4.8 -10.6 -4.8 9.6 1.2 9.6
            piece head hat_b cloth 72 0 -4 -11.8 -4 8 1.2 8
            piece head hat_c cloth 96 0 -3.2 -13 -3.2 6.4 1.2 6.4
            piece head hat_d cloth 116 0 -2.4 -14.2 -2.4 4.8 1.2 4.8
            piece head hat_e cloth 132 0 -1.6 -15.4 -1.6 3.2 1.2 3.2
            piece head hat_tip cloth 144 0 -1 -16.6 -1 2 1.4 2
            piece head hat_band detail 0 16 -5.7 -8.5 -5.7 11.4 0.6 11.4

            # --- capuche : l'arrière du crâne et les côtés, ouverte sur le visage
            piece head hood_back cloth 0 24 -5.6 -9.6 -6.2 11.2 10.4 6.4
            piece head hood_top cloth 44 24 -5.6 -10.4 -6.2 11.2 1.8 11.8
            piece head hood_edge detail 88 24 -5.8 -1 -1 11.6 1.2 1.4
            piece body hood_cowl cloth 108 24 -5.8 -1.4 -4 11.6 4.4 8

            # --- casque de garde : calotte, nasal et protège-joues
            piece head helm_cap detail 0 40 -5.2 -9.6 -5.2 10.4 4.4 10.4
            piece head helm_nose detail 44 40 -1 -8.6 4.2 2 4.8 1.6
            piece head helm_cheekL detail 56 40 -5.2 -6.4 -5.2 1.8 4.4 10.4
            piece head helm_cheekR detail 56 40 3.4 -6.4 -5.2 1.8 4.4 10.4
            piece head helm_ridge detail 76 40 -0.7 -10.4 -5.6 1.4 1 11.2

            # --- petit chapeau souple, bord rabattu devant
            piece head cap_crown cloth 0 56 -5.2 -11 -5.2 10.4 3 10.4
            piece head cap_brim cloth 44 56 -5.6 -9.4 -9.6 11.2 1 6.6
            piece head cap_band detail 84 56 -5.4 -8.6 -5.4 10.8 0.8 10.8

            # --- chapeau de paille, large bord
            piece head straw_crown cloth 0 72 -5.2 -11.6 -5.2 10.4 4 10.4
            piece head straw_brim cloth 44 72 -7.8 -9.4 -7.8 15.6 1 15.6
            piece head straw_band detail 0 88 -5.4 -8.8 -5.4 10.8 1.2 10.8

            # --- col haut, uniquement à l'arrière et sur les côtés
            piece body collar_back cloth 0 104 -3.6 -1.6 -3.4 7.2 2.8 1.8
            piece body collar_left cloth 20 104 -3.6 -1.6 -1.6 1.6 2.8 3.4
            piece body collar_right cloth 20 104 2 -1.6 -1.6 1.6 2.8 3.4

            # --- torse de la robe, ajusté au corps
            piece body robe_torso cloth 32 104 -4.4 -0.8 -2.4 8.8 11.8 4.8
            piece body robe_placket detail 72 104 -0.9 -0.4 2.5 1.8 10.6 0.5

            # --- cape d'épaules et plastron
            piece body mantle cloth 0 128 -5.2 -1.6 -3.2 10.4 2.8 6.4
            piece body mantle_trim detail 44 128 -4.8 1.2 -2.9 9.6 0.8 5.8

            # --- ceinture, boucle et gemme
            piece body belt detail 76 128 -4.7 9 -2.7 9.4 2.2 5.4
            piece body buckle detail 108 128 -1.2 9.4 2.7 2.4 1.4 0.5
            piece body gem detail 124 128 -1.1 9.6 3.2 2.2 1 0.4

            # --- cape : elle pend du haut des épaules et bouge avec la marche
            pivot cape 0 0 -2
            piece cape cape_main cloth 0 144 -4.6 0 -1.6 9.2 15 1.5
            piece cape cape_hem detail 32 144 -4.8 14.6 -2.1 9.6 1.4 2.6

            # --- jupe : trois étages qui s'évase, jusqu'aux chevilles
            pivot skirt 0 11 0
            piece skirt skirt_top cloth 0 168 -4.9 0 -2.9 9.8 4 5.8
            piece skirt skirt_mid cloth 36 168 -5.8 4 -3.4 11.6 4 6.8
            piece skirt skirt_low cloth 76 168 -6.2 8 -3.6 12.4 4.6 7.2
            piece skirt skirt_hem detail 112 168 -6 12.6 -3.5 12 1.6 7

            # --- manches : elles s'evasent vers le poignet, la main reste visible
            pivot right_arm 0 0 0
            pivot left_arm 0 0 0
            piece right_arm sleeve_right cloth 0 200 -3.5 -2.4 -2.4 5 8.6 5
            piece right_arm sleeve_right_bell cloth 32 200 -4.2 6.2 -3.1 6.4 3.6 6.4
            piece right_arm sleeve_right_trim detail 72 200 -4.4 9.6 -3.3 6.8 1.6 6.8
            piece left_arm sleeve_left cloth 0 200 -1.5 -2.4 -2.4 5 8.6 5
            piece left_arm sleeve_left_bell cloth 32 200 -2.2 6.2 -3.1 6.4 3.6 6.4
            piece left_arm sleeve_left_trim detail 72 200 -2.4 9.6 -3.3 6.8 1.6 6.8
            """;

    private static final String CLOTH_LAYER = "cloth";
    private static final String DETAIL_LAYER = "detail";

    /** Une pièce du modèle, telle qu'elle est écrite dans la table. */
    public record Piece(String part, String name, String layer, int u, int v,
                        float x, float y, float z, float width, float height, float depth) {
        /** Boîte de la pièce dans le repère de sa partie. */
        public float[] box() {
            return new float[]{x, y, z, x + width, y + height, z + depth};
        }
    }

    /** Pivot d'une partie (point de rotation). */
    public record Pivot(String part, float x, float y, float z) {}

    private static List<Piece> pieces;
    private static Map<String, Pivot> pivots;

    /** Les pièces du modèle (lecture de la table). */
    public static synchronized List<Piece> pieces() {
        if (pieces == null) parse();
        return pieces;
    }

    /** Les pivots des parties mobiles. */
    public static synchronized Map<String, Pivot> pivots() {
        if (pivots == null) parse();
        return pivots;
    }

    private static void parse() {
        List<Piece> found = new ArrayList<>();
        Map<String, Pivot> foundPivots = new HashMap<>();
        for (String raw : PIECES.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] words = line.split("\\s+");
            if (words[0].equals("pivot") && words.length >= 5) {
                foundPivots.put(words[1], new Pivot(words[1], Float.parseFloat(words[2]),
                        Float.parseFloat(words[3]), Float.parseFloat(words[4])));
            } else if (words[0].equals("piece") && words.length >= 12) {
                found.add(new Piece(words[1], words[2], words[3],
                        Integer.parseInt(words[4]), Integer.parseInt(words[5]),
                        Float.parseFloat(words[6]), Float.parseFloat(words[7]),
                        Float.parseFloat(words[8]), Float.parseFloat(words[9]),
                        Float.parseFloat(words[10]), Float.parseFloat(words[11])));
            } else {
                throw new IllegalStateException("ligne de modele illisible : " + line);
            }
        }
        pieces = List.copyOf(found);
        pivots = Map.copyOf(foundPivots);
    }

    // ------------------------------------------------------------ parties

    public final ModelPart head;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart cape;
    public final ModelPart skirt;

    /** Nom de chaque pièce, pour l'afficher ou la masquer (coiffes). */
    private final Map<String, ModelPart> byName = new HashMap<>();
    private final Map<String, String> layerOf = new HashMap<>();

    public WizardRobeModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.cape = root.getChild("cape");
        this.skirt = root.getChild("skirt");
        for (Piece piece : pieces()) {
            ModelPart part = partOf(piece.part());
            ModelPart child = part.getChild(piece.name());
            if (child != null) {
                byName.put(piece.name(), child);
                layerOf.put(piece.name(), piece.layer());
            }
        }
    }

    private ModelPart partOf(String part) {
        return switch (part) {
            case "body" -> this.body;
            case "right_arm" -> this.rightArm;
            case "left_arm" -> this.leftArm;
            case "cape" -> this.cape;
            case "skirt" -> this.skirt;
            default -> this.head;
        };
    }

    /** Construit le modèle à partir de la table : c'est le seul endroit qui la lit. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        Map<String, PartDefinition> parts = new HashMap<>();

        // chaque partie est créée à son pivot (ou à l'origine si elle n'en a pas)
        for (String part : List.of("head", "body", "right_arm", "left_arm", "cape", "skirt")) {
            Pivot pivot = pivots().get(part);
            PartPose pose = pivot == null ? PartPose.ZERO
                    : PartPose.offset(pivot.x(), pivot.y(), pivot.z());
            parts.put(part, root.addOrReplaceChild(part, CubeListBuilder.create(), pose));
        }

        for (Piece piece : pieces()) {
            PartDefinition part = parts.get(piece.part());
            if (part == null) throw new IllegalStateException("partie inconnue : " + piece.part());
            // les pièces sont écrites dans le repère de la partie : si la partie a un
            // pivot, les coordonnées données sont ramenées dans son repère local
            Pivot pivot = pivots().get(piece.part());
            float dx = pivot == null ? 0f : pivot.x();
            float dy = pivot == null ? 0f : pivot.y();
            float dz = pivot == null ? 0f : pivot.z();
            part.addOrReplaceChild(piece.name(), CubeListBuilder.create()
                            .texOffs(piece.u(), piece.v())
                            .addBox(piece.x() - dx, piece.y() - dy, piece.z() - dz,
                                    piece.width(), piece.height(), piece.depth()),
                    PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    // ------------------------------------------------------- affichage

    /**
     * Habille le modele : une matiere et une coiffe.
     *
     * <p>C'est la seule methode qui decide de ce qui se voit. Elle est appelee
     * deux fois par image — une fois pour le tissu (teinte), une fois pour les
     * details (couleurs propres). La coiffe portee limite toujours les pieces de
     * coiffe, sinon un personnage porterait tous ses chapeaux a la fois.
     *
     * @param layer  matiere a dessiner ({@code cloth} ou {@code detail})
     * @param attire coiffe portee
     */
    public void dress(String layer, Attire attire) {
        java.util.Set<String> worn = hatPieces(attire);
        boolean hooded = attire == Attire.HOOD;
        for (Map.Entry<String, String> entry : layerOf.entrySet()) {
            String name = entry.getKey();
            boolean visible = entry.getValue().equals(layer);
            if (isHat(name)) visible = visible && worn.contains(name);
            if (hooded && (name.equals("mantle") || name.equals("mantle_trim"))) visible = false;
            ModelPart part = byName.get(name);
            if (part != null) part.visible = visible;
        }
    }

    /** Vrai si cette piece fait partie d'une coiffe (elle n'est portee que si elle est choisie). */
    public static boolean isHat(String piece) {
        return piece.startsWith("hat_") || piece.startsWith("hood_") || piece.startsWith("helm_")
                || piece.startsWith("cap_") || piece.startsWith("straw_");
    }

    /**
     * Les pieces de coiffe reellement portees.
     *
     * <p>Plusieurs coiffes coexistent dans le modele : le chapeau pointu, la
     * capuche, le casque, le petit chapeau et le chapeau de paille. Le jeu n'en
     * affiche qu'une, celle que le personnage porte.
     */
    public static java.util.Set<String> hatPieces(Attire attire) {
        return switch (attire) {
            case POINTED -> java.util.Set.of("hat_brim", "hat_a", "hat_b", "hat_c", "hat_d",
                    "hat_e", "hat_tip", "hat_band");
            case CAP -> java.util.Set.of("cap_crown", "cap_brim", "cap_band");
            case STRAW -> java.util.Set.of("straw_crown", "straw_brim", "straw_band");
            case HELM -> java.util.Set.of("helm_cap", "helm_nose", "helm_cheekL", "helm_cheekR",
                    "helm_ridge");
            case HOOD -> java.util.Set.of("hood_back", "hood_top", "hood_edge", "hood_cowl");
            case NONE -> java.util.Set.of();
        };
    }

    /** Suit la pose du personnage : tête, torse et bras. */
    public void follow(net.minecraft.client.model.HumanoidModel<?> body) {
        this.head.copyFrom(body.head);
        this.body.copyFrom(body.body);
        this.rightArm.copyFrom(body.rightArm);
        this.leftArm.copyFrom(body.leftArm);
    }

    /**
     * Anime la tenue par-dessus la pose du personnage.
     *
     * <p>C'est ce qui distingue une tenue vivante d'une tenue figée : la cape
     * s'envole quand on marche, la jupe ondule, les cheveux et les manches suivent
     * le mouvement, et le tissu respire doucement à l'arrêt.
     *
     * @param ageInTicks       temps écoulé, en ticks
     * @param limbSwing        phase de la marche
     * @param limbSwingAmount  amplitude de la marche (0 à l'arrêt, 1 en courant)
     * @param attack           animation de coup (0 à 1), pour faire claquer les manches
     */
    public void animate(float ageInTicks, float limbSwing, float limbSwingAmount,
                        float attack) {
        // respiration : le tissu monte et descend très légèrement
        float breathe = Mth.sin(ageInTicks * 0.055f) * 0.018f;
        this.body.xRot += breathe;
        this.cape.xRot += breathe * 0.6f;

        // cape : elle pend, puis s'envole avec la marche
        this.cape.xRot = -0.05f - limbSwingAmount * 0.35f
                + Mth.sin(ageInTicks * 0.045f) * 0.035f;
        this.cape.zRot = Mth.cos(limbSwing * 0.5f) * limbSwingAmount * 0.06f;

        // jupe : elle ondule au rythme des pas
        this.skirt.xRot = limbSwingAmount * 0.09f * Mth.cos(limbSwing * 0.6662f)
                + Mth.sin(ageInTicks * 0.04f) * 0.02f;
        this.skirt.zRot = Mth.sin(limbSwing * 0.3331f) * limbSwingAmount * 0.05f;

        // manches : un léger retard sur le bras, et un claquement quand on frappe
        float swing = Mth.sin(limbSwing * 0.6662f);
        this.rightArm.xRot += -swing * limbSwingAmount * 0.08f - attack * 0.12f;
        this.leftArm.xRot += swing * limbSwingAmount * 0.08f - attack * 0.12f;

        // la coiffe suit la tête : elle est attachée à la partie « tete », qui vient
        // d'être recopiee depuis la pose du personnage ; il ne reste qu'un souffle
        this.head.xRot += breathe * 0.5f;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        this.head.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.body.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.rightArm.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.leftArm.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.cape.render(pose, consumer, light, overlay, red, green, blue, alpha);
        this.skirt.render(pose, consumer, light, overlay, red, green, blue, alpha);
    }
}
