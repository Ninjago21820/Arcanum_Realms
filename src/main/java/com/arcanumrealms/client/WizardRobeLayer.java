package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.client.model.WizardRobeModel;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.world.Attire;
import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.entity.WizardNpcEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Pose la tenue 3D sur un personnage : robe, cape d'epaules, manches, ceinture,
 * jupe et coiffe.
 *
 * <p>La meme couche sert aux joueurs et aux PNJ : seule change la maniere de
 * choisir la couleur (ecole du joueur, couleur de tenue du PNJ) et la coiffe
 * (chapeau pointu pour un sorcier, coiffe declaree pour un PNJ).
 *
 * <p>La tenue suit le corps : le torse, les bras et la tete de la couche
 * reprennent la position de ceux du personnage a chaque image, donc la jupe
 * tourne avec le corps et les manches avec les bras.
 */
public class WizardRobeLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/wizard/robe.png");

    private final WizardRobeModel model;

    public WizardRobeLayer(RenderLayerParent<T, M> parent, WizardRobeModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || entity.isSpectator()) return;

        Style style = styleOf(entity);
        if (style == null) return;

        // la tenue reprend la pose du personnage : torse, bras et tete
        M body = getParentModel();
        this.model.head.copyFrom(body.head);
        this.model.body.copyFrom(body.body);
        this.model.rightArm.copyFrom(body.rightArm);
        this.model.leftArm.copyFrom(body.leftArm);

        // une seule coiffe a la fois
        showHat(style.attire);

        // leger balancement de la jupe quand le personnage marche, pour que la robe
        // ne paraisse pas figee
        this.model.body.xRot += (float) Math.sin(ageInTicks * 0.06f) * 0.02f
                + limbSwingAmount * 0.05f;

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        float red = style.tinted ? style.red : 1f;
        float green = style.tinted ? style.green : 1f;
        float blue = style.tinted ? style.blue : 1f;
        this.model.renderToBuffer(pose, consumer, light, OverlayTexture.NO_OVERLAY,
                red, green, blue, 1f);
    }

    private void showHat(Attire attire) {
        boolean pointed = attire == Attire.POINTED;
        boolean hood = attire == Attire.HOOD;
        boolean helm = attire == Attire.HELM;
        boolean cap = attire == Attire.CAP;
        boolean straw = attire == Attire.STRAW;
        for (String piece : new String[]{"hat_brim", "hat_cone_1", "hat_cone_2", "hat_cone_3",
                "hat_tip"}) {
            this.model.setVisible(piece, pointed);
        }
        this.model.setVisible("hood_back", hood);
        this.model.setVisible("hood_cowl", hood);
        for (String piece : new String[]{"helm_cap", "helm_nose", "helm_cheek", "helm_cheek_2"}) {
            this.model.setVisible(piece, helm);
        }
        this.model.setVisible("cap_crown", cap || straw);
        this.model.setVisible("cap_brim", cap);
        this.model.setVisible("straw_crown", false);
        this.model.setVisible("straw_brim", straw);
        this.model.setVisible("mantle", !hood);
        this.model.setVisible("mantle_trim", !hood);
    }

    /** Couleur et coiffe a porter sur ce personnage, ou {@code null} pour ne rien dessiner. */
    private static Style styleOf(LivingEntity entity) {
        if (entity instanceof WizardNpcEntity npc) {
            NpcDef def = npc.def();
            if (def == null) return null;
            float[] colour = def.outfitColour();
            return new Style(true, colour[0], colour[1], colour[2], def.attire());
        }
        if (entity instanceof net.minecraft.client.player.AbstractClientPlayer) {
            if (!com.arcanumrealms.ArcanumConfig.wizardRobeEnabled()) return null;
            var data = ClientBattleState.wizard();
            School school = data == null ? null : data.school();
            Attire attire = com.arcanumrealms.ArcanumConfig.playerHatEnabled()
                    ? Attire.POINTED : Attire.NONE;
            if (school == null) {
                // pas encore d'ecole : une robe neutre, sans teinte
                return new Style(false, 1f, 1f, 1f, attire);
            }
            int rgb = school.color();
            return new Style(true, ((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f,
                    (rgb & 0xff) / 255f, attire);
        }
        return null;
    }

    /** Ce qu'il faut dessiner sur un personnage : une couleur et une coiffe. */
    private record Style(boolean tinted, float red, float green, float blue, Attire attire) {}
}
