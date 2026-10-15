package com.arcanumrealms.server;

import com.arcanumrealms.core.world.Dialogue;
import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.core.world.Npcs;
import com.arcanumrealms.core.world.QuestDef;
import com.arcanumrealms.core.world.QuestGoal;
import com.arcanumrealms.core.world.QuestText;
import com.arcanumrealms.core.world.Quests;
import com.arcanumrealms.item.GearItem;
import com.arcanumrealms.core.world.GearDef;
import com.arcanumrealms.core.world.Gears;
import com.arcanumrealms.network.NetworkHandler;
import com.arcanumrealms.network.packet.NpcInteractPacket;
import com.arcanumrealms.player.WizardData;
import com.arcanumrealms.registry.ModItems;
import com.arcanumrealms.util.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialogues de PNJ.
 *
 * <p>Le serveur decide de ce que dit un PNJ et de ce que le joueur peut faire,
 * puis envoie au client une replique deja mise en forme. Toute la logique de
 * quete reste donc cote serveur, le client ne fait qu'afficher des lignes de
 * texte et des boutons.
 */
public final class DialogueService {

    private DialogueService() {}

    /** Le joueur clique sur un PNJ : on lui joue le dialogue d'accueil. */
    public static void open(ServerPlayer player, String npcId) {
        NpcDef npc = Npcs.get(npcId);
        WizardData data = WizardData.load(player);
        boolean french = Language.isFrench();
        List<QuestDef> handIns = handInReady(data, npc);
        List<QuestDef> offers = offers(data, npc);

        Dialogue dialogue = npc.greeting();
        if (!handIns.isEmpty()) dialogue = npc.completion();
        else if (!offers.isEmpty()) dialogue = npc.inProgress();
        if (dialogue.isEmpty()) dialogue = npc.greeting();

        List<String> lines = new ArrayList<>();
        lines.add(npc.title(french).isBlank()
                ? npc.name(french)
                : npc.name(french) + ", " + npc.title(french));
        lines.addAll(dialogue.texts(french));
        if (!handIns.isEmpty()) {
            lines.add(Component.translatable("arcanum_realms.dialogue.handin_ready").getString());
        } else if (!offers.isEmpty()) {
            lines.add(Component.translatable("arcanum_realms.dialogue.quest_available").getString());
        }

        List<QuestAction> actions = new ArrayList<>();
        for (QuestDef quest : handIns) {
            actions.add(new QuestAction(quest.id(), QuestText.line(quest, french),
                    QuestText.goals(quest, french), QuestText.rewards(quest, french), KIND_HAND_IN));
        }
        for (QuestDef quest : offers) {
            actions.add(new QuestAction(quest.id(), QuestText.line(quest, french),
                    QuestText.goals(quest, french), QuestText.rewards(quest, french), KIND_QUEST));
        }
        // Le marchand vend l'equipement correspondant au niveau du joueur.
        if (npc.role() == com.arcanumrealms.core.world.NpcRole.SHOPKEEPER) {
            for (GearDef gear : shopStock(data)) {
                actions.add(new QuestAction(gear.id(),
                        gear.name(french) + " - " + GearItem.buyPrice(gear) + (french ? " or" : " gold"),
                        List.of(gear.stats(french)), "", KIND_BUY));
            }
        }

        NetworkHandler.sendNpcDialogue(player, npc.id(), lines, actions);
    }

    /** Le joueur clique sur un bouton du dialogue. */
    public static void handle(ServerPlayer player, String npcId, int action, String arg) {
        NpcDef npc = Npcs.get(npcId);
        switch (action) {
            case NpcInteractPacket.ACCEPT -> QuestService.accept(player, arg);
            case NpcInteractPacket.HAND_IN -> QuestService.handIn(player, arg);
            case NpcInteractPacket.BUY -> buy(player, npc, arg);
            case NpcInteractPacket.CLOSE -> {
                boolean french = Language.isFrench();
                List<String> lines = new ArrayList<>(npc.farewell().texts(french));
                NetworkHandler.sendNpcDialogue(player, npc.id(), lines, List.of());
            }
            default -> {
                // action inconnue : on ignore
            }
        }
    }

    /** Achats chez un marchand. */
    private static void buy(ServerPlayer player, NpcDef npc, String gearId) {
        if (npc.role() != com.arcanumrealms.core.world.NpcRole.SHOPKEEPER) return;
        GearDef gear = Gears.find(gearId).orElse(null);
        if (gear == null) return;
        WizardData data = WizardData.load(player);
        int price = GearItem.buyPrice(gear);
        if (data.level() < gear.level()) {
            player.displayClientMessage(Component.translatable("arcanum_realms.shop.too_low",
                    gear.level()), false);
            return;
        }
        if (!data.spendGold(price)) {
            player.displayClientMessage(Component.translatable("arcanum_realms.shop.no_gold",
                    price, data.gold()), false);
            return;
        }
        data.save(player);
        player.getInventory().placeItemBackInInventory(GearItem.create(gear, 1));
        player.displayClientMessage(Component.translatable("arcanum_realms.shop.bought",
                Component.translatable("gear.arcanum_realms." + gear.id()), price), false);
        open(player, npc.id());
    }

    /** Quetes de ce PNJ que le joueur peut rendre tout de suite. */
    static List<QuestDef> handInReady(WizardData data, NpcDef npc) {
        List<QuestDef> out = new ArrayList<>();
        for (String questId : npc.endsQuests()) {
            if (data.quests().isActive(questId) && data.quests().readyToHandIn(questId)) {
                Quests.find(questId).ifPresent(out::add);
            }
        }
        return out;
    }

    /** Quetes de ce PNJ que le joueur peut accepter maintenant. */
    static List<QuestDef> offers(WizardData data, NpcDef npc) {
        List<QuestDef> out = new ArrayList<>();
        for (QuestDef quest : Quests.byGiver(npc.id())) {
            if (data.quests().canStart(quest.id(), data.level())) out.add(quest);
        }
        return out;
    }

    /** Equipement en vente : le palier correspondant au niveau du joueur, et les precedents. */
    static List<GearDef> shopStock(WizardData data) {
        int tier = Math.min(4, Math.max(0, (data.level() - 1) / 6));
        List<GearDef> out = new ArrayList<>(Gears.ofTier(tier));
        if (tier > 0) out.addAll(Gears.ofTier(tier - 1));
        return out;
    }

    /** Petite aide d'affichage : effet d'une recompense de sort. */
    static String spellLine(String spellId, boolean french) {
        return com.arcanumrealms.core.spell.SpellBook.find(spellId)
                .map(spell -> Component.translatable(spell.langKey()).getString())
                .orElse(spellId);
    }

    /** Types de bouton affiches sous un dialogue. */
    public static final int KIND_QUEST = 0;
    public static final int KIND_HAND_IN = 1;
    public static final int KIND_BUY = 2;

    /** Bouton de dialogue envoye au client. */
    public record QuestAction(String id, String label, List<String> goals, String rewards, int kind) {}

    /** Resume lisible d'un objectif, pour les messages de progression. */
    public static String goalLabel(QuestGoal goal, boolean french) {
        return QuestText.goal(goal, french);
    }

    /** Message envoye au joueur quand une creature de quete tombe. */
    static void announce(ChatFormatting colour, ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(colour), true);
    }

    static ItemStack gearStack(String gearId) {
        GearDef def = Gears.find(gearId).orElse(null);
        if (def == null) return new ItemStack(ModItems.WIZARD_GEAR.get());
        return GearItem.create(def, 1);
    }
}
