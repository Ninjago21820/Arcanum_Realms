package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumConfig;
import com.arcanumrealms.client.model.WizardRobeModel;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.world.Attire;
import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.entity.WizardNpcEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

/**
 * Pose la tenue de sorcier sur un personnage : robe longue, cape, manches,
 * ceinture, jupe et coiffe, avec ses animations.
 *
 * <p>La même couche sert aux joueurs et aux PNJ ; seuls changent la couleur
 * (école du joueur, tenue du PNJ) et la coiffe (chapeau pointu du sorcier,
 * coiffe déclarée du PNJ).
 *
 * <p>Le modèle est dessiné en deux passes :
 *
 * <ol>
 *   <li>le <b>tissu</b>, avec la texture en niveaux de gris multipliée par la
 *       couleur du personnage ;</li>
 *   <li>les <b>détails</b> (métal, cuir, galons, gemmes), avec leur propre
 *       texture, jamais teintée.</li>
 * </ol>
 */
public class WizardRobeLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private final WizardRobeModel model;

    public WizardRobeLayer(RenderLayerParent<T, M> parent, WizardRobeModel model) {
        super(parent);
        this.model = model;
    }

    // L'habillage (quelle matière, quelle coiffe) est décidé par le modèle :
    // WizardRobeModel.dress(layer, attire). Rien à répéter ici.

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || entity.isSpectator()) return;

        Style style = styleOf(entity);
        if (style == null) return;

        // la tenue suit le corps, puis s'anime par-dessus
        this.model.follow(getParentModel());
        this.model.animate(ageInTicks, limbSwing, limbSwingAmount,
                entity.getAttackAnim(partialTick));


        // 1. le tissu, teinté par la couleur du personnage
        this.model.dress("cloth", style.attire);
        VertexConsumer cloth = buffers.getBuffer(RenderType.entityCutoutNoCull(WizardRobeModel.CLOTH));
        this.model.renderToBuffer(pose, cloth, light, OverlayTexture.NO_OVERLAY,
                style.red, style.green, style.blue, 1f);

        // 2. les détails, avec leurs vraies couleurs
        this.model.dress("detail", style.attire);
        VertexConsumer detail = buffers.getBuffer(RenderType.entityCutoutNoCull(WizardRobeModel.DETAIL));
        this.model.renderToBuffer(pose, detail, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
    }

    /** Couleur de la tenue, coiffe portée et état de la capuche. */
    private static Style styleOf(LivingEntity entity) {
        if (entity instanceof WizardNpcEntity npc) {
            NpcDef def = npc.def();
            if (def == null) return null;
            float[] colour = def.outfitColour();
            // une capuche couvre les épaules : la cape d'épaules s'efface
            return new Style(colour[0], colour[1], colour[2], def.attire());
        }
        if (entity instanceof AbstractClientPlayer player) {
            if (!ArcanumConfig.wizardRobeEnabled()) return null;
            var data = ClientBattleState.wizard();
            School school = data == null ? null : data.school();
            Attire attire = ArcanumConfig.playerHatEnabled() ? Attire.POINTED : Attire.NONE;
            if (school == null) {
                // pas encore d'école : une robe neutre, sans teinte
                return new Style(1f, 1f, 1f, attire);
            }
            int rgb = school.color();
            return new Style(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f,
                    (rgb & 0xff) / 255f, attire);
        }
        return null;
    }

    /** @param red teinte du tissu (1 = neutre, avant le choix de l'ecole) */
    private record Style(float red, float green, float blue, Attire attire) {}
}
