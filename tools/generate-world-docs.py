#!/usr/bin/env python3
"""Genere docs/world.md : le contenu du monde (zones, creatures, PNJ, quetes, equipement).

Comme docs/spells.md, ce document est produit en executant le moteur : les noms,
les objectifs et les valeurs affichees sont exactement ceux du jeu.

Usage : python3 tools/generate-world-docs.py /chemin/vers/jdk17
"""
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
SRC = os.path.join(ROOT, 'src', 'main', 'java')
DOC = os.path.join(ROOT, 'docs', 'world.md')

DUMPER = r'''
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.world.*;
import java.util.List;

public class DumpWorld {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();

        for (ZoneDef zone : Zones.all()) {
            sb.append("ZONE\t").append(zone.id()).append('\t')
              .append(zone.nameFr()).append('\t').append(zone.nameEn()).append('\t')
              .append(zone.minLevel()).append('\t').append(zone.maxLevel()).append('\t')
              .append(zone.dungeon() ? "donjon" : "exterieur").append('\t')
              .append(zone.unlockQuest() == null ? "-" : zone.unlockQuest()).append('\t')
              .append(zone.unlockLevel()).append('\t')
              .append(zone.creatures().size()).append('\t').append(zone.npcs().size()).append('\t')
              .append(zone.quests().size()).append('\t')
              .append(zone.descriptionFr()).append('\n');
        }

        for (WorldCreature creature : Bestiary.all()) {
            StringBuilder resist = new StringBuilder();
            for (School school : School.values()) {
                if (creature.resist(school) != 0) resist.append(school.displayFr()).append(' ').append(creature.resist(school)).append("% ");
            }
            StringBuilder boost = new StringBuilder();
            for (School school : School.values()) {
                if (creature.boost(school) != 0) boost.append(school.displayFr()).append(' ').append(creature.boost(school)).append("% ");
            }
            sb.append("CREATURE\t").append(creature.id()).append('\t')
              .append(creature.nameFr()).append('\t').append(creature.nameEn()).append('\t')
              .append(creature.school().displayFr()).append('\t').append(creature.rank()).append('\t')
              .append(creature.level()).append('\t').append(creature.health()).append('\t')
              .append(creature.startingPips()).append('\t').append(creature.classification()).append('\t')
              .append(creature.stunnable() ? "oui" : "non").append('\t')
              .append(creature.beguilable() ? "oui" : "non").append('\t')
              .append(String.join(", ", creature.zoneIds())).append('\t')
              .append(creature.entity().split(":")[1]).append('\t')
              .append(creature.deck().size()).append('\t')
              .append(creature.lootTable()).append('\t')
              .append(creature.boss() ? "boss" : "normal").append('\t')
              .append(resist.toString().trim()).append('\t')
              .append(boost.toString().trim()).append('\n');
        }

        for (NpcDef npc : Npcs.all()) {
            sb.append("NPC\t").append(npc.id()).append('\t')
              .append(npc.nameFr()).append('\t').append(npc.nameEn()).append('\t')
              .append(npc.titleFr()).append('\t').append(npc.zoneId()).append('\t')
              .append(npc.role().name()).append('\t')
              .append(npc.school() == null ? "-" : npc.school().displayFr()).append('\t')
              .append(npc.level()).append('\t')
              .append(npc.givesQuests().isEmpty() ? "-" : String.join(", ", npc.givesQuests())).append('\t')
              .append(npc.endsQuests().isEmpty() ? "-" : String.join(", ", npc.endsQuests())).append('\t')
              .append(npc.greeting().size()).append('\n');
        }

        for (QuestDef quest : Quests.all()) {
            sb.append("QUEST\t").append(quest.id()).append('\t')
              .append(quest.nameFr()).append('\t').append(quest.nameEn()).append('\t')
              .append(quest.side() ? "annexe" : "principale").append('\t')
              .append(quest.levelRequired()).append('\t')
              .append(quest.prequest() == null ? "-" : quest.prequest()).append('\t')
              .append(quest.giverNpc()).append('\t').append(quest.handInNpc()).append('\t')
              .append(quest.leadsTo() == null ? "-" : quest.leadsTo() + " (nv " + quest.leadsToLevel() + ")").append('\t')
              .append(quest.rewardXp()).append('\t').append(quest.rewardGold()).append('\t')
              .append(quest.rewardSpell() == null ? "-" : quest.rewardSpell()).append('\t')
              .append(quest.rewardItem() == null ? "-" : quest.rewardItem()).append('\t')
              .append(quest.rewardChoices().isEmpty() ? "-" : String.join(", ", quest.rewardChoices())).append('\t')
              .append(String.join(" / ", QuestText.goals(quest, true))).append('\t')
              .append(QuestText.rewards(quest, true)).append('\n');
        }

        for (GearDef gear : Gears.all()) {
            sb.append("GEAR\t").append(gear.id()).append('\t')
              .append(gear.nameFr()).append('\t').append(gear.nameEn()).append('\t')
              .append(gear.slot().name().toLowerCase()).append('\t').append(gear.tier()).append('\t')
              .append(gear.level()).append('\t')
              .append(gear.school() == null ? "-" : gear.school().displayFr()).append('\t')
              .append(gear.stats(true)).append('\n');
        }

        for (LootTable table : LootTables.all()) {
            StringBuilder drops = new StringBuilder();
            for (LootTable.Entry entry : table.entries()) {
                if (drops.length() > 0) drops.append(" / ");
                drops.append(entry.id()).append(' ').append(entry.chance()).append("% \u00d7")
                     .append(entry.min()).append('-').append(entry.max());
            }
            sb.append("LOOT\t").append(table.id()).append('\t')
              .append(table.goldMin()).append('\t').append(table.goldMax()).append('\t')
              .append(drops).append('\n');
        }

        System.out.print(sb);
    }
}
'''


def main():
    jdk = sys.argv[1] if len(sys.argv) > 1 else '/tmp/jdk17'
    javac = os.path.join(jdk, 'bin', 'javac')
    java = os.path.join(jdk, 'bin', 'java')
    if not os.path.exists(javac):
        sys.exit(f'javac introuvable : {javac}')

    work = tempfile.mkdtemp(prefix='arcanum-world-')
    try:
        core_files = []
        for root, _dirs, files in os.walk(os.path.join(SRC, 'com', 'arcanumrealms', 'core')):
            core_files += [os.path.join(root, f) for f in files if f.endswith('.java')]
        dumper = os.path.join(work, 'DumpWorld.java')
        with open(dumper, 'w', encoding='utf-8') as f:
            f.write(DUMPER)
        subprocess.run([javac, '-encoding', 'UTF-8', '-nowarn', '-d', work] + core_files + [dumper],
                       check=True)
        out = subprocess.run([java, '-Dfile.encoding=UTF-8', '-cp', work, 'DumpWorld'],
                             check=True, capture_output=True, text=True).stdout
    finally:
        shutil.rmtree(work, ignore_errors=True)

    zones, creatures, npcs, quests, gears, loots = [], [], [], [], [], []
    for line in out.splitlines():
        if not line.strip():
            continue
        parts = line.split('\t')
        {
            'ZONE': zones, 'CREATURE': creatures, 'NPC': npcs,
            'QUEST': quests, 'GEAR': gears, 'LOOT': loots,
        }.get(parts[0], []).append(parts[1:])

    lines = []
    lines.append('# Contenu du monde')
    lines.append('')
    lines.append('> Fichier **genere automatiquement** par `tools/generate-world-docs.py` a partir du moteur : '
                 'les noms, objectifs et valeurs listes ici sont exactement ceux utilises en jeu.')
    lines.append('')
    lines.append('Le contenu suit les fiches de la wiki Wizard101 Central (statistiques, rangs, resistances, '
                 'sorts, butin, enchainement des quetes), avec des noms originaux.')
    lines.append('')

    lines.append('## Sommaire')
    lines.append('')
    lines.append(f'- {len(zones)} zones')
    lines.append(f'- {len(creatures)} creatures')
    lines.append(f'- {len(npcs)} PNJ')
    lines.append(f'- {len(quests)} quetes')
    lines.append(f'- {len(gears)} pieces d\'equipement')
    lines.append(f'- {len(loots)} tables de butin')
    lines.append('')

    lines.append('## Zones')
    lines.append('')
    lines.append('| Zone | Nom (EN) | Niveaux | Creatures | PNJ | Quetes | Ouverture | Type |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- | --- |')
    for zid, fr, en, mn, mx, kind, unlock, ulevel, nc, nn, nq, desc in zones:
        opening = '-' if unlock == '-' else f'`{unlock}` (nv {ulevel})'
        lines.append(f'| **{fr}** (`{zid}`) | {en} | {mn}-{mx} | {nc} | {nn} | {nq} | {opening} | {kind} |')
    lines.append('')
    for zid, fr, en, mn, mx, kind, unlock, ulevel, nc, nn, nq, desc in zones:
        lines.append(f'### {fr} ({en})')
        lines.append('')
        lines.append(f'{desc}')
        lines.append('')

    lines.append('## Bestiaire')
    lines.append('')
    lines.append('| Creature | Nom (EN) | Ecole | Rang | Niv. | PV | Pips | Classe | Zone(s) | Entite | Cartes | Butin | Type |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |')
    for (cid, fr, en, school, rank, level, health, pips, cls, stun, beguile, zs, entity,
         deck, loot, kind, resist, boost) in creatures:
        lines.append(f'| **{fr}** (`{cid}`) | {en} | {school} | {rank} | {level} | {health} | {pips} | '
                     f'{cls} | {zs} | `{entity}` | {deck} | {loot} | {kind} |')
    lines.append('')
    lines.append('Resistances et faiblesses relevees dans les fiches du bestiaire :')
    lines.append('')
    lines.append('| Creature | Resistances | Faiblesses | Etourdissable | Envoutable |')
    lines.append('| --- | --- | --- | --- | --- |')
    for (cid, fr, en, school, rank, level, health, pips, cls, stun, beguile, zs, entity,
         deck, loot, kind, resist, boost) in creatures:
        lines.append(f'| {fr} | {resist or "-"} | {boost or "-"} | {stun} | {beguile} |')
    lines.append('')

    lines.append('## PNJ')
    lines.append('')
    lines.append('| PNJ | Nom (EN) | Titre | Zone | Role | Ecole | Niv. | Donne | Remet | Repliques |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |')
    for (nid, fr, en, title, zone, role, school, level, gives, ends, greetings) in npcs:
        lines.append(f'| **{fr}** (`{nid}`) | {en} | {title or "-"} | {zone} | {role} | {school} | '
                     f'{level} | {gives} | {ends} | {greetings} |')
    lines.append('')

    lines.append('## Quetes')
    lines.append('')
    lines.append('### Chaine principale')
    lines.append('')
    lines.append('| Quete | Nom (EN) | Niv. | Donneur | Objectifs | Remettant | Recompenses | Suite |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- | --- |')
    for (qid, fr, en, kind, level, pre, giver, handin, leads, xp, gold, spell, item, choices,
         goals, rewards) in quests:
        if kind != 'principale':
            continue
        lines.append(f'| **{fr}** (`{qid}`) | {en} | {level} | {giver} | {goals} | {handin} | '
                     f'{rewards or "-"} | {leads} |')
    lines.append('')
    lines.append('### Quetes annexes')
    lines.append('')
    lines.append('| Quete | Nom (EN) | Niv. | Prealable | Donneur | Objectifs | Recompenses |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- |')
    for (qid, fr, en, kind, level, pre, giver, handin, leads, xp, gold, spell, item, choices,
         goals, rewards) in quests:
        if kind != 'annexe':
            continue
        lines.append(f'| **{fr}** (`{qid}`) | {en} | {level} | {pre} | {giver} | {goals} | {rewards or "-"} |')
    lines.append('')

    lines.append('## Equipement')
    lines.append('')
    names = {'hat': 'Chapeau', 'robe': 'Robe', 'boots': 'Bottes', 'wand': 'Baguette',
             'athame': 'Athame', 'ring': 'Anneau', 'amulet': 'Amulette', 'deck': 'Deck'}
    for tier in range(5):
        pieces = [g for g in gears if int(g[4]) == tier]
        if not pieces:
            continue
        lines.append(f'### Palier {tier} : {pieces[0][1].split()[0]}')
        lines.append('')
        lines.append('| Piece | Nom (EN) | Emplacement | Niv. | Ecole | Statistiques |')
        lines.append('| --- | --- | --- | --- | --- | --- |')
        for gid, fr, en, slot, t, level, school, stats in pieces:
            lines.append(f'| **{fr}** (`{gid}`) | {en} | {names.get(slot, slot)} | {level} | '
                         f'{school} | {stats or "-"} |')
        lines.append('')

    lines.append('## Tables de butin')
    lines.append('')
    lines.append('| Table | Or | Butin |')
    lines.append('| --- | --- | --- |')
    for lid, gmin, gmax, drops in loots:
        lines.append(f'| `{lid}` | {gmin}-{gmax} | {drops} |')
    lines.append('')

    os.makedirs(os.path.dirname(DOC), exist_ok=True)
    with open(DOC, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines))
    print(f'docs/world.md : {len(zones)} zones, {len(creatures)} creatures, {len(npcs)} PNJ, '
          f'{len(quests)} quetes, {len(gears)} pieces d\'equipement, {len(loots)} tables de butin')


if __name__ == '__main__':
    main()
