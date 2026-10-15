package com.arcanumrealms.server;

import com.arcanumrealms.core.world.GearDef;
import com.arcanumrealms.core.world.Gears;
import com.arcanumrealms.core.world.Npcs;
import com.arcanumrealms.core.world.QuestDef;
import com.arcanumrealms.core.world.QuestGoal;
import com.arcanumrealms.core.world.QuestText;
import com.arcanumrealms.core.world.Quests;
import com.arcanumrealms.core.world.Zones;
import com.arcanumrealms.network.packet.QuestActionPacket;
import com.arcanumrealms.player.WizardData;
import com.arcanumrealms.util.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Quetes : accepter, suivre la progression, remettre.
 *
 * <p>La progression est enregistree dans les donnees du joueur (voir
 * {@link WizardData}), elle survit donc a la deconnexion. Chaque recompense
 * (experience, or, sort, equipement) est appliquee ici, cote serveur.
 */
public final class QuestService {

    private QuestService() {}

    /** Le joueur clique sur un bouton de quete. */
    public static void handleAction(ServerPlayer player, int action, String questId) {
        switch (action) {
            case QuestActionPacket.ACCEPT -> accept(player, questId);
            case QuestActionPacket.ABANDON -> abandon(player, questId);
            case QuestActionPacket.HAND_IN -> handIn(player, questId);
            default -> {
                // action inconnue : on ignore
            }
        }
    }

    /** Accepte une quete si tous les prealables sont remplis. */
    public static void accept(ServerPlayer player, String questId) {
        QuestDef quest = Quests.find(questId).orElse(null);
        if (quest == null) return;
        WizardData data = WizardData.load(player);
        boolean french = Language.isFrench();

        if (data.quests().isCompleted(questId)) {
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.already_done"), false);
            return;
        }
        if (data.quests().isActive(questId)) {
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.already_active"), false);
            return;
        }
        if (!data.quests().canStart(questId, data.level())) {
            if (data.level() < quest.levelRequired()) {
                player.displayClientMessage(Component.translatable("arcanum_realms.quest.level_required",
                        quest.levelRequired(), quest.name(french)).withStyle(ChatFormatting.RED), false);
            } else {
                QuestDef prequest = Quests.find(quest.prequest()).orElse(null);
                String name = prequest == null ? quest.prequest() : prequest.name(french);
                player.displayClientMessage(Component.translatable("arcanum_realms.quest.prequest",
                        name).withStyle(ChatFormatting.RED), false);
            }
            return;
        }

        data.quests().start(questId);
        data.save(player);
        player.displayClientMessage(Component.translatable("arcanum_realms.quest.accepted",
                quest.name(french)).withStyle(ChatFormatting.GREEN), false);
        for (String goal : QuestText.goals(quest, french)) {
            player.displayClientMessage(Component.literal(" - " + goal).withStyle(ChatFormatting.GRAY), false);
        }
    }

    /** Abandonne une quete active. */
    public static void abandon(ServerPlayer player, String questId) {
        WizardData data = WizardData.load(player);
        if (!data.quests().isActive(questId)) return;
        QuestDef quest = Quests.find(questId).orElse(null);
        data.quests().abandon(questId);
        data.save(player);
        if (quest != null) {
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.abandoned",
                    quest.name(Language.isFrench())).withStyle(ChatFormatting.GRAY), false);
        }
    }

    /** Remet une quete terminee et distribue les recompenses. */
    public static void handIn(ServerPlayer player, String questId) {
        QuestDef quest = Quests.find(questId).orElse(null);
        if (quest == null) return;
        WizardData data = WizardData.load(player);
        boolean french = Language.isFrench();

        if (!data.quests().isActive(questId) || !data.quests().readyToHandIn(questId)) {
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.not_ready")
                    .withStyle(ChatFormatting.RED), false);
            return;
        }

        data.quests().complete(questId);

        // --- recompenses
        if (quest.rewardXp() > 0) {
            int gained = data.grantXp(quest.rewardXp());
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.reward_xp",
                    quest.rewardXp()).withStyle(ChatFormatting.AQUA), false);
            if (gained > 0) {
                player.displayClientMessage(Component.translatable("arcanum_realms.quest.level_up",
                        data.level()).withStyle(ChatFormatting.GOLD), false);
            }
        }
        if (quest.rewardGold() > 0) {
            data.addGold(quest.rewardGold());
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.reward_gold",
                    quest.rewardGold()).withStyle(ChatFormatting.GOLD), false);
        }
        if (quest.rewardSpell() != null && data.learn(quest.rewardSpell())) {
            player.displayClientMessage(Component.translatable("arcanum_realms.quest.reward_spell",
                    DialogueService.spellLine(quest.rewardSpell(), french)).withStyle(ChatFormatting.LIGHT_PURPLE),
                    false);
        }
        if (quest.rewardItem() != null) {
            giveGear(player, quest.rewardItem(), french);
        }
        // Les quetes a choix donnent la premiere piece disponible : le joueur
        // peut acheter les autres chez le marchand.
        if (quest.rewardChoices().size() > 0) {
            giveGear(player, quest.rewardChoices().get(0), french);
            for (int i = 1; i < quest.rewardChoices().size(); i++) {
                GearDef other = Gears.find(quest.rewardChoices().get(i)).orElse(null);
                if (other != null) {
                    player.displayClientMessage(Component.translatable("arcanum_realms.quest.choice_available",
                            other.name(french)).withStyle(ChatFormatting.GRAY), false);
                }
            }
        }

        data.save(player);
        player.displayClientMessage(Component.translatable("arcanum_realms.quest.completed",
                quest.name(french)).withStyle(ChatFormatting.GREEN), false);

        // --- suite de la chaine
        if (quest.leadsTo() != null) {
            Quests.find(quest.leadsTo()).ifPresent(next -> {
                if (data.quests().canStart(next.id(), data.level())) {
                    player.displayClientMessage(Component.translatable("arcanum_realms.quest.next",
                            next.name(french), npcName(next.giverNpc(), french))
                            .withStyle(ChatFormatting.YELLOW), false);
                } else {
                    player.displayClientMessage(Component.translatable("arcanum_realms.quest.next_later",
                            next.name(french), next.levelRequired()).withStyle(ChatFormatting.YELLOW), false);
                }
            });
        }

        // --- zones ouvertes
        for (com.arcanumrealms.core.world.ZoneDef zone : Zones.all()) {
            if (questId.equals(zone.unlockQuest())) {
                player.displayClientMessage(Component.translatable("arcanum_realms.quest.zone_unlocked",
                        zone.name(french), zone.unlockLevel()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
        }
    }

    private static String npcName(String npcId, boolean french) {
        return Npcs.find(npcId).map(npc -> npc.name(french)).orElse(npcId);
    }

    private static void giveGear(ServerPlayer player, String gearId, boolean french) {
        GearDef gear = Gears.find(gearId).orElse(null);
        if (gear == null) return;
        player.getInventory().placeItemBackInInventory(DialogueService.gearStack(gearId));
        player.displayClientMessage(Component.translatable("arcanum_realms.quest.reward_item",
                gear.name(french)).withStyle(ChatFormatting.GOLD), false);
    }

    // ------------------------------------------------- progression en jeu

    /** Une creature de quete vient de tomber : met a jour les objectifs de tous les joueurs proches. */
    public static void onCreatureDefeated(ServerPlayer player, String creatureId, String classification) {
        WizardData data = WizardData.load(player);
        List<String> finished = data.quests().reportDefeat(creatureId, classification, 1);
        if (finished.isEmpty()) return;
        data.save(player);
        announceCompletion(player, finished);
    }

    /** Le joueur ramasse un objet : met a jour les objectifs de recolte. */
    public static void onItemCollected(ServerPlayer player, String itemId, int count) {
        WizardData data = WizardData.load(player);
        List<String> finished = data.quests().reportCollect(itemId, count);
        if (finished.isEmpty()) return;
        data.save(player);
        announceCompletion(player, finished);
    }

    /** Le joueur parle a un PNJ : valide les objectifs de discussion. */
    public static void onTalked(ServerPlayer player, String npcId) {
        WizardData data = WizardData.load(player);
        List<String> finished = data.quests().reportTalk(npcId);
        if (finished.isEmpty()) return;
        data.save(player);
        announceCompletion(player, finished);
    }

    /** Le joueur entre dans une zone : valide les objectifs de visite. */
    public static void onZoneEntered(ServerPlayer player, String zoneId) {
        WizardData data = WizardData.load(player);
        List<String> finished = data.quests().reportVisit(zoneId);
        if (finished.isEmpty()) return;
        data.save(player);
        announceCompletion(player, finished);
    }

    private static void announceCompletion(ServerPlayer player, List<String> finishedQuestIds) {
        boolean french = Language.isFrench();
        for (String questId : finishedQuestIds) {
            Quests.find(questId).ifPresent(quest -> {
                player.displayClientMessage(Component.translatable("arcanum_realms.quest.goals_done",
                        quest.name(french)).withStyle(ChatFormatting.GREEN), true);
                player.displayClientMessage(Component.translatable("arcanum_realms.quest.talk_to",
                        npcName(quest.handInNpc(), french)).withStyle(ChatFormatting.YELLOW), false);
            });
        }
    }

    // ---------------------------------------------------------- journal

    /** Journal de quetes affiche par {@code /arcanum quetes}. */
    public static List<String> journal(ServerPlayer player) {
        boolean french = Language.isFrench();
        WizardData data = WizardData.load(player);
        List<String> lines = new ArrayList<>();
        List<String> active = data.quests().active();
        if (active.isEmpty()) {
            lines.add(Component.translatable("arcanum_realms.quest.journal_empty").getString());
            return lines;
        }
        for (String questId : active) {
            QuestDef quest = Quests.find(questId).orElse(null);
            if (quest == null) continue;
            boolean ready = data.quests().readyToHandIn(questId);
            lines.add((ready ? "[+] " : "[-] ") + QuestText.line(quest, french));
            for (QuestGoal goal : quest.goals()) {
                int progress = data.quests().progressOf(questId, goal.key());
                boolean done = data.quests().goalDone(questId, goal);
                lines.add("    " + (done ? "[x] " : "[ ] ") + QuestText.goal(goal, french)
                        + (done ? "" : " (" + progress + "/" + required(goal) + ")"));
            }
            if (ready) {
                lines.add("    " + Component.translatable("arcanum_realms.quest.journal_ready",
                        npcName(quest.handInNpc(), french)).getString());
            }
        }
        return lines;
    }

    private static int required(QuestGoal goal) {
        if (goal instanceof QuestGoal.Defeat d) return d.count();
        if (goal instanceof QuestGoal.Collect c) return c.count();
        return 1;
    }
}
