package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.core.school.School;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Habille les joueurs en sorciers, sans toucher a leur apparence.
 *
 * <p>La robe est une texture posee sur la <b>couche exterieure</b> du modele de
 * joueur : celle qui sert d'ordinaire au chapeau, a la veste et aux manches. Le
 * joueur garde donc totalement son apparence Minecraft (sa peau, ses cheveux,
 * ses vetements), avec par-dessus le chapeau pointu et la robe de son ecole.
 *
 * <p>La couche suit l'ecole du joueur : elle apparait des qu'un sorcier a choisi
 * son ecole, et change s'il en change.
 */
public class WizardRobeLayer extends RenderLayer<AbstractClientPlayer,
        PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation NEUTRAL =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/wizard/neutral.png");

    public WizardRobeLayer(RenderLayerParent<AbstractClientPlayer,
            PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isSpectator() || player.isInvisible()) return;

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(textureFor()));
        // Le modele entier est redessine avec la texture de la robe : les cases
        // transparentes de la couche interieure ne dessinent rien, seules les
        // parties de la couche exterieure (chapeau, veste, manches, bottes)
        // apparaissent.
        getParentModel().renderToBuffer(pose, consumer, light, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
    }

    /** Robe de l'ecole du joueur, ou robe neutre s'il n'en a pas encore choisi. */
    private static ResourceLocation textureFor() {
        String school = schoolId();
        if (school == null || school.isBlank()) return NEUTRAL;
        return new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/wizard/" + school + ".png");
    }

    private static String schoolId() {
        var data = ClientBattleState.wizard();
        if (data == null) return null;
        School school = data.school();
        return school == null ? null : school.id();
    }
}
