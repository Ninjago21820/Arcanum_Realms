#!/usr/bin/env python3
"""Genere les fichiers de langue (en_us / fr_fr) a partir du moteur de sorts.

Les noms et descriptions des sorts vivent dans le code Java (SpellBook + SpellDescriber)
pour rester synchronises avec les valeurs de jeu. Ce script les exporte vers les
fichiers de traduction Minecraft : il compile le moteur avec le JDK fourni, execute
un exporteur, puis ecrit les deux fichiers JSON.

Usage : python3 tools/generate-lang.py /chemin/vers/jdk17
"""
import json
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
SRC = os.path.join(ROOT, 'src', 'main', 'java')
LANG_DIR = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'arcanum_realms', 'lang')

DUMPER = '''
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.spell.SpellDescriber;
import com.arcanumrealms.core.world.GearDef;
import com.arcanumrealms.core.world.Gears;
import com.arcanumrealms.core.world.NpcDef;
import com.arcanumrealms.core.world.Npcs;
import com.arcanumrealms.core.world.QuestDef;
import com.arcanumrealms.core.world.Quests;
import com.arcanumrealms.core.world.WorldCreature;
import com.arcanumrealms.core.world.Bestiary;
import com.arcanumrealms.core.world.ZoneDef;
import com.arcanumrealms.core.world.Zones;

public class DumpLang {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (School school : School.values()) {
            sb.append("school.arcanum_realms.").append(school.id()).append('\\t')
              .append(school.display(false)).append('\\t').append(school.display(true)).append('\\n');
        }
        for (Spell spell : SpellBook.all()) {
            sb.append("spell.arcanum_realms.").append(spell.id()).append(".name").append('\\t')
              .append(spell.name(false)).append('\\t').append(spell.name(true)).append('\\n');
            sb.append("spell.arcanum_realms.").append(spell.id()).append(".desc").append('\\t')
              .append(SpellDescriber.describe(spell, false)).append('\\t')
              .append(SpellDescriber.describe(spell, true)).append('\\n');
        }
        for (WorldCreature creature : Bestiary.all()) {
            sb.append("creature.arcanum_realms.").append(creature.id()).append('\\t')
              .append(creature.nameEn()).append('\\t').append(creature.nameFr()).append('\\n');
        }
        for (NpcDef npc : Npcs.all()) {
            sb.append("npc.arcanum_realms.").append(npc.id()).append('\\t')
              .append(npc.nameEn()).append('\\t').append(npc.nameFr()).append('\\n');
            sb.append("npc.arcanum_realms.").append(npc.id()).append(".title").append('\\t')
              .append(npc.titleEn()).append('\\t').append(npc.titleFr()).append('\\n');
        }
        for (GearDef gear : Gears.all()) {
            sb.append("gear.arcanum_realms.").append(gear.id()).append('\\t')
              .append(gear.nameEn()).append('\\t').append(gear.nameFr()).append('\\n');
        }
        for (ZoneDef zone : Zones.all()) {
            sb.append("zone.arcanum_realms.").append(zone.id()).append('\\t')
              .append(zone.nameEn()).append('\\t').append(zone.nameFr()).append('\\n');
        }
        for (QuestDef quest : Quests.all()) {
            sb.append("quest.arcanum_realms.").append(quest.id()).append('\\t')
              .append(quest.nameEn()).append('\\t').append(quest.nameFr()).append('\\n');
            sb.append("quest.arcanum_realms.").append(quest.id()).append(".summary").append('\\t')
              .append(quest.summaryEn()).append('\\t').append(quest.summaryFr()).append('\\n');
        }
        System.out.print(sb);
    }
}
'''

# Textes ecrits a la main : interface, messages, commandes, objets.
MANUAL = {
    # --- equipement
    'item.arcanum_realms.wizard_gear': ['Wizard gear', 'Equipement de sorcier'],
    'item.arcanum_realms.wizard_gear.empty': [
        'Gear token without an item: use /arcanum equipement <id>',
        'Jeton d\'equipement sans piece : utilisez /arcanum equipement <id>'],
    'item.arcanum_realms.wizard_gear.equip': [
        'Right-click: equip - Shift right-click: unequip',
        'Clic droit : porter - Clic droit accroupi : retirer'],
    'gear.arcanum_realms.slot.hat': ['Hat', 'Chapeau'],
    'gear.arcanum_realms.slot.robe': ['Robe', 'Robe'],
    'gear.arcanum_realms.slot.boots': ['Boots', 'Bottes'],
    'gear.arcanum_realms.slot.wand': ['Wand', 'Baguette'],
    'gear.arcanum_realms.slot.athame': ['Athame', 'Athame'],
    'gear.arcanum_realms.slot.ring': ['Ring', 'Anneau'],
    'gear.arcanum_realms.slot.amulet': ['Amulet', 'Amulette'],
    'gear.arcanum_realms.slot.deck': ['Deck', 'Deck'],
    'gear.arcanum_realms.level': ['level %s', 'niveau %s'],
    'arcanum_realms.message.gear_equipped': ['Equipped: %s', 'Porte : %s'],
    'arcanum_realms.message.gear_removed': ['Unequipped: %s', 'Retire : %s'],

    # --- PNJ et dialogues
    'gui.arcanum_realms.dialogue.close': ['Goodbye', 'Au revoir'],
    'gui.arcanum_realms.dialogue.goals': ['Goals', 'Objectifs'],
    'gui.arcanum_realms.dialogue.rewards': ['Rewards', 'Recompenses'],
    'gui.arcanum_realms.dialogue.buy': ['Click to buy', 'Cliquez pour acheter'],
    'arcanum_realms.dialogue.handin_ready': ['You have something to hand in.', 'Vous avez une quete a remettre.'],
    'arcanum_realms.dialogue.quest_available': ['A quest is available.', 'Une quete est disponible.'],
    'arcanum_realms.npc.dont_hit': ['%s: "Please keep your spells to yourself."',
                                    '%s : "Gardez vos sorts pour vous, je vous prie."'],

    # --- journal de quetes
    'gui.arcanum_realms.journal.title': ['Quest journal', 'Journal de quetes'],
    'gui.arcanum_realms.journal.close': ['Close', 'Fermer'],
    'gui.arcanum_realms.journal.scroll': ['Scroll with the mouse wheel', 'Faites defiler a la molette'],
    'arcanum_realms.quest.journal_empty': ['No quest in progress. Talk to the headmaster.',
                                           'Aucune quete en cours. Parlez au directeur.'],
    'arcanum_realms.quest.journal_ready': ['Ready to hand in to %s', 'A remettre a %s'],

    # --- quetes
    'arcanum_realms.quest.accepted': ['Quest accepted: %s', 'Quete acceptee : %s'],
    'arcanum_realms.quest.abandoned': ['Quest abandoned: %s', 'Quete abandonnee : %s'],
    'arcanum_realms.quest.completed': ['Quest completed: %s', 'Quete terminee : %s'],
    'arcanum_realms.quest.already_done': ['You have already completed this quest.',
                                          'Vous avez deja termine cette quete.'],
    'arcanum_realms.quest.already_active': ['This quest is already in your journal.',
                                            'Cette quete est deja dans votre journal.'],
    'arcanum_realms.quest.level_required': ['You need level %s for "%s".',
                                            'Il vous faut le niveau %s pour "%s".'],
    'arcanum_realms.quest.prequest': ['First complete "%s".', 'Terminez d\'abord "%s".'],
    'arcanum_realms.quest.not_ready': ['Your goals are not complete yet.', 'Vos objectifs ne sont pas encore remplis.'],
    'arcanum_realms.quest.goals_done': ['Goals complete: %s', 'Objectifs remplis : %s'],
    'arcanum_realms.quest.talk_to': ['Talk to %s to hand in.', 'Parlez a %s pour remettre.'],
    'arcanum_realms.quest.reward_xp': ['+%s XP', '+%s XP'],
    'arcanum_realms.quest.reward_gold': ['+%s gold', '+%s or'],
    'arcanum_realms.quest.reward_spell': ['New spell learned: %s', 'Nouveau sort appris : %s'],
    'arcanum_realms.quest.reward_item': ['Received: %s', 'Recu : %s'],
    'arcanum_realms.quest.choice_available': ['Also available as a reward: %s',
                                              'Egalement disponible en recompense : %s'],
    'arcanum_realms.quest.level_up': ['Level %s!', 'Niveau %s !'],
    'arcanum_realms.quest.next': ['Next quest: "%s" from %s', 'Quete suivante : "%s" aupres de %s'],
    'arcanum_realms.quest.next_later': ['Next quest: "%s" (level %s)',
                                        'Quete suivante : "%s" (niveau %s)'],
    'arcanum_realms.quest.zone_unlocked': ['New zone unlocked: %s - %s',
                                           'Nouvelle zone ouverte : %s - %s'],

    # --- boutique
    'arcanum_realms.shop.too_low': ['This item requires level %s.', 'Cet objet demande le niveau %s.'],
    'arcanum_realms.shop.no_gold': ['You need %s gold (you have %s).',
                                    'Il vous faut %s or (vous en avez %s).'],
    'arcanum_realms.shop.bought': ['Bought: %s for %s gold', 'Achete : %s pour %s or'],

    # --- zones
    'arcanum_realms.zone.locked': ['%s is sealed until level %s.', '%s est scelle jusqu\'au niveau %s.'],
    'arcanum_realms.zone.dungeon': ['Dungeon zone: bring friends.', 'Zone de donjon : venez accompagne.'],

    # --- commandes du monde
    'arcanum_realms.command.help.zones': ['/arcanum zones [set|del] - list, place or remove zones',
                                          '/arcanum zones [set|del] - lister, poser ou retirer les zones'],
    'arcanum_realms.command.help.pnj': ['/arcanum pnj [id|ici] - list NPCs or place them',
                                        '/arcanum pnj [id|ici] - lister les PNJ ou les placer'],
    'arcanum_realms.command.help.quetes': ['/arcanum quetes [id] - quest journal or details',
                                           '/arcanum quetes [id] - journal de quetes ou details'],
    'arcanum_realms.command.help.quete': ['/arcanum quete <accepter|abandonner|remettre> <id>',
                                          '/arcanum quete <accepter|abandonner|remettre> <id>'],
    'arcanum_realms.command.help.or': ['/arcanum or <amount> - add or remove gold',
                                       '/arcanum or <montant> - ajouter ou retirer de l\'or'],
    'arcanum_realms.command.help.equipement': ['/arcanum equipement [id|palier|retirer] - gear',
                                               '/arcanum equipement [id|palier|retirer] - equipement'],
    'arcanum_realms.command.zones.header': ['Arcanum Realms zones (%s):', 'Zones d\'Arcanum Realms (%s) :'],
    'arcanum_realms.command.zones.missing': ['Not placed yet: %s', 'Pas encore posees : %s'],
    'arcanum_realms.command.zone_unknown': ['Unknown zone: %s', 'Zone inconnue : %s'],
    'arcanum_realms.command.zone_absent': ['Zone %s has no anchor.', 'La zone %s n\'a pas d\'ancre.'],
    'arcanum_realms.command.zone_set': ['Zone %s (%s) anchored here, radius %s blocks.',
                                        'Zone %s (%s) posee ici, rayon %s blocs.'],
    'arcanum_realms.command.zone_deleted': ['Anchor of %s removed.', 'Ancre de %s retiree.'],
    'arcanum_realms.command.npcs.header': ['Arcanum Realms NPCs (%s):', 'PNJ d\'Arcanum Realms (%s) :'],
    'arcanum_realms.command.npc_unknown': ['Unknown NPC: %s', 'PNJ inconnu : %s'],
    'arcanum_realms.command.npc_spawned': ['%s is waiting for you.', '%s vous attend.'],
    'arcanum_realms.command.npc_failed': ['Could not place the NPC here.', 'Impossible de poser le PNJ ici.'],
    'arcanum_realms.command.npcs_placed': ['%s NPC(s) placed in %s.', '%s PNJ pose(s) dans %s.'],
    'arcanum_realms.command.no_zone': ['No zone here: place one with /arcanum zones set.',
                                       'Aucune zone ici : posez-en une avec /arcanum zones set.'],
    'arcanum_realms.command.zone_no_npc': ['No NPC lives in %s.', 'Aucun PNJ n\'habite %s.'],
    'arcanum_realms.command.no_creature': ['No creature of this zone could be spawned.',
                                           'Aucune creature de cette zone n\'a pu apparaitre.'],
    'arcanum_realms.command.creatures_spawned': ['%s creature(s) appeared.', '%s creature(s) apparue(s).'],
    'arcanum_realms.command.quest_unknown': ['Unknown quest: %s', 'Quete inconnue : %s'],
    'arcanum_realms.command.gold': ['You now have %s gold.', 'Vous avez maintenant %s or.'],
    'arcanum_realms.command.gear.header': ['Arcanum Realms gear (%s):', 'Equipement d\'Arcanum Realms (%s) :'],
    'arcanum_realms.command.gear.gold': ['Gold: %s', 'Or : %s'],
    'arcanum_realms.command.gear_unknown': ['Unknown item: %s', 'Objet inconnu : %s'],
    'arcanum_realms.command.gear_given': ['Received: %s', 'Recu : %s'],
    'arcanum_realms.command.gear_tier': ['Full tier %s set received.', 'Set complet du palier %s recu.'],
    'arcanum_realms.command.slot_unknown': ['Unknown slot: %s', 'Emplacement inconnu : %s'],
    'arcanum_realms.command.slot_empty': ['Nothing is equipped there.', 'Rien n\'est porte ici.'],
    'arcanum_realms.command.gear_removed': ['Unequipped: %s', 'Retire : %s'],

    'item.arcanum_realms.duel_wand': ['Duel Wand', 'Baguette de Duel'],
    'item.arcanum_realms.duel_wand.hint': [
        'Right-click a creature to start a duel, or a magic circle to call an encounter.',
        'Clic droit sur une creature pour lancer un duel, ou sur un cercle arcanique pour appeler une rencontre.'],
    'item.arcanum_realms.duel_wand.tooltip1': [
        'Right-click: challenge a creature or a magic circle',
        'Clic droit : defier une creature ou un cercle arcanique'],
    'item.arcanum_realms.duel_wand.tooltip2': [
        'Right-click in the air: open your spellbook',
        'Clic droit dans le vide : ouvrir votre grimoire'],
    'item.arcanum_realms.duel_wand.tooltip3': [
        'Your deck is drawn during duels: 7 cards in hand',
        'Votre deck est pioche pendant les duels : 7 cartes en main'],
    'block.arcanum_realms.arcane_circle': ['Arcane Circle', 'Cercle Arcanique'],
    'block.arcanum_realms.arcane_circle.hint': [
        'Right-click the circle to call an encounter and start a duel.',
        'Clic droit sur le cercle pour appeler une rencontre et lancer un duel.'],
    'itemGroup.arcanum_realms.main': ['Arcanum Realms', 'Arcanum Realms'],

    'key.categories.arcanum_realms': ['Arcanum Realms', 'Arcanum Realms'],
    'key.arcanum_realms.open_duel': ['Reopen the duel board', 'Rouvrir le plateau de duel'],
    'key.arcanum_realms.open_grimoire': ['Open the spellbook', 'Ouvrir le grimoire'],

    'gui.arcanum_realms.duel.title': ['Duel', 'Duel'],
    'gui.arcanum_realms.duel.round': ['Round', 'Round'],
    'gui.arcanum_realms.duel.log': ['Battle log', 'Journal de combat'],
    'gui.arcanum_realms.duel.waiting': ['Waiting for the server...', 'En attente du serveur...'],
    'gui.arcanum_realms.grimoire.title': ['Spellbook', 'Grimoire'],
    'gui.arcanum_realms.grimoire.wizard': ['Wizard', 'Sorcier'],
    'gui.arcanum_realms.grimoire.level': ['Level', 'Niveau'],
    'gui.arcanum_realms.grimoire.powerpips': ['Power pip chance', 'Chance de pip de puissance'],
    'gui.arcanum_realms.grimoire.crit': ['Critical rating', 'Score critique'],
    'gui.arcanum_realms.grimoire.learned': ['Spells learned', 'Sorts appris'],
    'gui.arcanum_realms.grimoire.deck': ['Deck', 'Deck'],
    'gui.arcanum_realms.grimoire.hint': [
        'Left-click: add a copy - Right-click: remove a copy - Save to apply (min. 1 card)',
        'Clic gauche : ajouter une copie - Clic droit : retirer une copie - Enregistrez pour appliquer (1 carte minimum)'],
    'gui.arcanum_realms.grimoire.minlevel': ['min. level', 'niveau min.'],
    'gui.arcanum_realms.grimoire.add_hint': ['Left-click to add to the deck', 'Clic gauche pour ajouter au deck'],
    'gui.arcanum_realms.grimoire.waiting': ['Loading your wizard data...', 'Chargement de votre fiche de sorcier...'],
    'gui.arcanum_realms.button.pass': ['Pass', 'Passer'],
    'gui.arcanum_realms.button.flee': ['Flee', 'Fuir'],
    'gui.arcanum_realms.button.save': ['Save deck', 'Enregistrer le deck'],
    'gui.arcanum_realms.card.pips': ['pips', 'pips'],
    'gui.arcanum_realms.card.variable': ['X', 'X'],
    'gui.arcanum_realms.card.too_expensive': ['Not enough pips', 'Pips insuffisants'],
    'gui.arcanum_realms.card.hint': [
        'Left-click the card, then click a target. Right-click to discard.',
        'Clic gauche sur la carte, puis clic sur une cible. Clic droit pour defausser.'],
    'gui.arcanum_realms.phase.planning': ['Choose your cards', 'Choix des cartes'],
    'gui.arcanum_realms.phase.resolving': ['Resolving', 'Resolution'],
    'gui.arcanum_realms.state.defeated': ['defeated', 'vaincu'],
    'gui.arcanum_realms.hud.round': ['Round', 'Round'],
    'gui.arcanum_realms.hud.aura': ['Global aura', 'Aura globale'],
    'gui.arcanum_realms.hud.hint': ['Press R to open the duel board', 'Touche R pour ouvrir le plateau de duel'],
    'gui.arcanum_realms.result.victory': ['Victory!', 'Victoire !'],
    'gui.arcanum_realms.result.defeat': ['Defeat...', 'Defaite...'],
    'gui.arcanum_realms.result.draw': ['Draw', 'Egalite'],

    'arcanum_realms.message.already_in_duel': ['You are already in a duel.', 'Vous etes deja en duel.'],
    'arcanum_realms.message.no_creature': ['No creature to challenge nearby.', 'Aucune creature a defier a proximite.'],
    'arcanum_realms.message.no_encounter': ['No encounter available here.', 'Aucune rencontre disponible ici.'],
    'arcanum_realms.message.no_duel': ['No duel in progress.', 'Aucun duel en cours.'],
    'arcanum_realms.message.duel_started': ['The duel begins!', 'Le duel commence !'],
    'arcanum_realms.message.fled_too_far': ['You fled too far from the duel!', 'Vous avez trop fui du duel !'],
    'arcanum_realms.message.you_fled': ['You fled the duel.', 'Vous avez fui le duel.'],
    'arcanum_realms.message.victory': ['Victory! You gain %s XP.', 'Victoire ! Vous gagnez %s XP.'],
    'arcanum_realms.message.level_up': ['You reached level %s!', 'Vous atteignez le niveau %s !'],
    'arcanum_realms.message.defeat': ['You were defeated...', 'Vous avez ete vaincu...'],
    'arcanum_realms.message.deck_saved': ['Deck saved: %s/%s cards.', 'Deck enregistre : %s/%s cartes.'],
    'arcanum_realms.message.victory_short': ['Victory!', 'Victoire !'],
    'arcanum_realms.message.defeat_short': ['Defeat...', 'Defaite...'],
    'arcanum_realms.message.draw': ['The duel ends in a draw.', 'Le duel se termine en egalite.'],

    'arcanum_realms.command.help_header': ['--- Arcanum Realms ---', '--- Arcanum Realms ---'],
    'arcanum_realms.command.help.duel': ['/arcanum duel - challenge nearby creatures',
                                         '/arcanum duel - defier les creatures proches'],
    'arcanum_realms.command.help.arena': ['/arcanum arena - call an encounter at your position',
                                          '/arcanum arena - appeler une rencontre a votre position'],
    'arcanum_realms.command.help.resolve': ['/arcanum resolve - resolve the round now',
                                            '/arcanum resolve - resoudre le round immediatement'],
    'arcanum_realms.command.help.leave': ['/arcanum leave - leave the duel', '/arcanum leave - quitter le duel'],
    'arcanum_realms.command.help.noauto': ['/arcanum noauto <true|false> - disable auto duels',
                                           '/arcanum noauto <true|false> - desactiver les duels automatiques'],
    'arcanum_realms.command.help.school': ['/arcanum school <school> - change school (resets deck)',
                                           '/arcanum school <ecole> - changer d\'ecole (reinitialise le deck)'],
    'arcanum_realms.command.help.level': ['/arcanum level <n> - set level and learn spells',
                                          '/arcanum level <n> - definir le niveau et apprendre les sorts'],
    'arcanum_realms.command.help.powerpips': ['/arcanum powerpips <0-100> - power pip chance',
                                              '/arcanum powerpips <0-100> - chance de pip de puissance'],
    'arcanum_realms.command.help.crit': ['/arcanum crit <score> - critical rating',
                                         '/arcanum crit <score> - score critique'],
    'arcanum_realms.command.help.grimoire': ['/arcanum grimoire - open your spellbook and deck',
                                             '/arcanum grimoire - ouvrir votre grimoire et votre deck'],
    'arcanum_realms.command.help.deck': ['/arcanum deck <reset|info> - manage your deck',
                                         '/arcanum deck <reset|info> - gerer votre deck'],
    'arcanum_realms.command.help.spells': ['/arcanum spells [school] - list spells',
                                           '/arcanum spells [ecole] - lister les sorts'],
    'arcanum_realms.command.help.creatures': ['/arcanum creatures - list creatures',
                                              '/arcanum creatures - lister les creatures'],
    'arcanum_realms.command.help.info': ['/arcanum info - mod statistics', '/arcanum info - statistiques du mod'],
    'arcanum_realms.command.info.help': ['Use /arcanum help for the command list.',
                                         'Utilisez /arcanum help pour la liste des commandes.'],
    'arcanum_realms.command.resolve.done': ['Round resolved.', 'Round resolu.'],
    'arcanum_realms.command.noauto.on': ['Auto duels disabled.', 'Duels automatiques desactives.'],
    'arcanum_realms.command.noauto.off': ['Auto duels enabled.', 'Duels automatiques actives.'],
    'arcanum_realms.command.school.done': ['School set to %s (%s spells learned).',
                                           'Ecole definie : %s (%s sorts appris).'],
    'arcanum_realms.command.school.unknown': ['Unknown school: %s', 'Ecole inconnue : %s'],
    'arcanum_realms.command.level.done': ['Level set to %s (%s spells learned).',
                                          'Niveau defini : %s (%s sorts appris).'],
    'arcanum_realms.command.powerpips.done': ['Power pip chance set to %s%%.',
                                              'Chance de pip de puissance definie a %s%%.'],
    'arcanum_realms.command.crit.done': ['Critical rating set to %s.', 'Score critique defini a %s.'],
    'arcanum_realms.command.deck.reset': ['Deck reset with %s cards.', 'Deck reinitialise avec %s cartes.'],
    'arcanum_realms.command.deck.info': ['Deck: %s/%s cards, %s spells learned.',
                                         'Deck : %s/%s cartes, %s sorts appris.'],
}


def main():
    jdk = sys.argv[1] if len(sys.argv) > 1 else '/tmp/jdk17'
    javac = os.path.join(jdk, 'bin', 'javac')
    java = os.path.join(jdk, 'bin', 'java')
    if not os.path.exists(javac):
        sys.exit(f'javac introuvable : {javac}')

    work = tempfile.mkdtemp(prefix='arcanum-lang-')
    try:
        core_files = []
        for root, _dirs, files in os.walk(os.path.join(SRC, 'com', 'arcanumrealms', 'core')):
            core_files += [os.path.join(root, f) for f in files if f.endswith('.java')]
        dumper = os.path.join(work, 'DumpLang.java')
        with open(dumper, 'w', encoding='utf-8') as f:
            f.write(DUMPER)
        subprocess.run([javac, '-encoding', 'UTF-8', '-nowarn', '-d', work] + core_files + [dumper], check=True)
        out = subprocess.run([java, '-Dfile.encoding=UTF-8', '-cp', work, 'DumpLang'],
                             check=True, capture_output=True, text=True).stdout
    finally:
        shutil.rmtree(work, ignore_errors=True)

    english = {}
    french = {}
    for key, values in MANUAL.items():
        english[key] = values[0]
        french[key] = values[1]
    for line in out.splitlines():
        if not line.strip():
            continue
        key, en, fr = line.split('\t')
        english[key] = en
        french[key] = fr

    os.makedirs(LANG_DIR, exist_ok=True)
    for name, table in (('en_us', english), ('fr_fr', french)):
        path = os.path.join(LANG_DIR, name + '.json')
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(dict(sorted(table.items())), f, indent=2, ensure_ascii=False)
            f.write('\n')
        print(f'{name}.json : {len(table)} entrees')


if __name__ == '__main__':
    main()
