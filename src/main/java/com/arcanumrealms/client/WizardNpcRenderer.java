package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.entity.WizardNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Rendu des PNJ.
 *
 * <p>Chaque PNJ utilise le modele humanoide standard et une tenue choisie selon
 * son ecole : les professeurs portent les couleurs de leur ecole, les habitants
 * une tenue neutre. Une seule texture par ecole, generee par
 * {@code tools/generate-resources.py}.
 */
public class WizardNpcRenderer extends HumanoidMobRenderer<WizardNpcEntity, HumanoidModel<WizardNpcEntity>> {

    private static final ResourceLocation NEUTRAL =
            new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/npc/neutral.png");

    public WizardNpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new net.minecraft.client.renderer.entity.layers.CustomHeadLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(WizardNpcEntity entity) {
        NpcDef def = entity.def();
        if (def == null || def.school() == null) return NEUTRAL;
        School school = def.school();
        return new ResourceLocation(ArcanumRealms.MOD_ID, "textures/entity/npc/" + school.id() + ".png");
    }
}
