package com.arcanumrealms.client;

import com.arcanumrealms.ArcanumRealms;
import com.arcanumrealms.registry.ModEntities;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Rendus du mod.
 *
 * <p>Deux choses s'enregistrent ici :
 *
 * <ul>
 *   <li>le rendu des PNJ, qui choisit l'apparence du personnage a partir de son
 *       identifiant ;</li>
 *   <li>la <b>robe de sorcier</b> posee par-dessus les joueurs, sur la couche
 *       exterieure du modele : le joueur garde son apparence Minecraft, mais
 *       s'habille en sorcier de son ecole.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = ArcanumRealms.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ClientRenderers {

    private ClientRenderers() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WIZARD_NPC.get(), WizardNpcRenderer::new);
    }

    /**
     * Ajoute la robe aux deux variantes du modele de joueur (classique et fine).
     * C'est un ajout de couche : rien n'est remplace, donc la peau du joueur et
     * les packs de ressources continuent de fonctionner normalement.
     */
    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new WizardRobeLayer(renderer));
        }
    }
}
