package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
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
 * <p>La couche exterieure du modele (chapeau, veste, manches) est dessinee par
 * le meme modele : les chapeaux pointus, capuches et casques n'ont donc besoin
 * d'aucun modele supplementaire.
 */
public class WizardNpcRenderer extends HumanoidMobRenderer<WizardNpcEntity,
        HumanoidModel<WizardNpcEntity>> {

    private static final ResourceLocation NEUTRAL =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/npc/neutral.png");

    public WizardNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(WizardNpcEntity entity) {
        String id = entity.npcId();
        if (id == null || id.isBlank()) return NEUTRAL;
        return new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/npc/" + id + ".png");
    }
}
