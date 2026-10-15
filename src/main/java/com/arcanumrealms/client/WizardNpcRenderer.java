package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.client.model.WizardRobeModel;
import com.arcanumrealms.entity.WizardNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Rendu des PNJ : un personnage humanoide par PNJ.
 *
 * <p>Chaque personnage a sa propre apparence (visage, cheveux, coiffe, tenue),
 * generee par {@code tools/character_skins.py} dans
 * {@code textures/entity/npc/<identifiant>.png}. Comme le modele est celui du
 * joueur, un pack de ressources peut remplacer une apparence : il suffit de
 * fournir un fichier du meme nom.
 *
 * <p>La tenue elle-meme est un modele 3D pose par-dessus (voir
 * {@link WizardRobeLayer}) : chapeau pointu, capuche ou casque en volume, cape
 * d'epaules, manches larges, ceinture et jupe evasee.
 */
public class WizardNpcRenderer extends HumanoidMobRenderer<WizardNpcEntity,
        HumanoidModel<WizardNpcEntity>> {

    private static final ResourceLocation NEUTRAL =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/npc/neutral.png");

    public WizardNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
        // la tenue 3D : chapeau, cape d'epaules, manches, ceinture et jupe
        this.addLayer(new WizardRobeLayer<>(this,
                new WizardRobeModel(context.bakeLayer(WizardRobeModel.LAYER))));
    }

    @Override
    public ResourceLocation getTextureLocation(WizardNpcEntity entity) {
        String id = entity.npcId();
        if (id == null || id.isBlank()) return NEUTRAL;
        return new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/npc/" + id + ".png");
    }
}
