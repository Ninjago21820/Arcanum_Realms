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
        System.out.print(sb);
    }
}
'''

# Textes ecrits a la main : interface, messages, commandes, objets.
MANUAL = {
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
        subprocess.run([javac, '-encoding', 'UTF-8', '-d', work] + core_files + [dumper], check=True)
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
