#!/usr/bin/env python3
"""Genere docs/spells.md : le catalogue complet des sorts et des creatures.

Le document est produit a partir du code du moteur, donc il ne peut jamais
diverger des valeurs reellement utilisees en jeu.

Usage : python3 tools/generate-docs.py /chemin/vers/jdk17
"""
import os
import shutil
import subprocess
import sys
import tempfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
SRC = os.path.join(ROOT, 'src', 'main', 'java')
DOC = os.path.join(ROOT, 'docs', 'spells.md')

DUMPER = '''
import com.arcanumrealms.core.minion.CreatureDef;
import com.arcanumrealms.core.minion.Creatures;
import com.arcanumrealms.core.school.School;
import com.arcanumrealms.core.spell.Spell;
import com.arcanumrealms.core.spell.SpellBook;
import com.arcanumrealms.core.spell.SpellDescriber;
import com.arcanumrealms.core.status.Status;

public class DumpDocs {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (Spell spell : SpellBook.all()) {
            sb.append("SPELL\\t").append(spell.id()).append('\\t').append(spell.school().id()).append('\\t')
              .append(spell.variablePips() ? ("X+" + spell.minPips() + "-" + spell.maxPips()) : String.valueOf(spell.pipCost())).append('\\t')
              .append(spell.accuracy()).append('\\t').append(spell.minLevel()).append('\\t')
              .append(spell.kind().name()).append('\\t').append(spell.targetMode().name()).append('\\t')
              .append(spell.maxCopies()).append('\\t')
              .append(spell.name(true)).append('\\t').append(spell.name(false)).append('\\t')
              .append(SpellDescriber.describe(spell, true)).append('\\t')
              .append(SpellDescriber.describe(spell, false)).append('\\n');
        }
        for (School school : School.values()) {
            sb.append("SCHOOL\\t").append(school.id()).append('\\t').append(school.displayFr()).append('\\t')
              .append(school.displayEn()).append('\\t').append(school.baseAccuracy()).append('\\t')
              .append(school.damagePerPip()).append('\\t').append(school.bladePercent()).append('\\t')
              .append(school.trapPercent()).append('\\t').append(school.healthMultiplier()).append('\\n');
        }
        for (CreatureDef def : Creatures.all()) {
            StringBuilder resist = new StringBuilder();
            for (School school : School.values()) {
                int value = def.resist(school);
                if (value != 0) resist.append(school.id()).append(':').append(value).append(' ');
            }
            sb.append("CREATURE\\t").append(def.id()).append('\\t').append(def.school().id()).append('\\t')
              .append(def.level()).append('\\t').append(def.health()).append('\\t')
              .append(def.entityId()).append('\\t').append(def.critRating()).append('\\t')
              .append(def.powerPipChance()).append('\\t').append(def.deck().size()).append('\\t')
              .append(def.boss() ? "boss" : "normal").append('\\t')
              .append(def.nameFr()).append('\\t').append(def.nameEn()).append('\\t')
              .append(resist.toString().trim()).append('\\n');
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

    work = tempfile.mkdtemp(prefix='arcanum-docs-')
    try:
        core_files = []
        for root, _dirs, files in os.walk(os.path.join(SRC, 'com', 'arcanumrealms', 'core')):
            core_files += [os.path.join(root, f) for f in files if f.endswith('.java')]
        dumper = os.path.join(work, 'DumpDocs.java')
        with open(dumper, 'w', encoding='utf-8') as f:
            f.write(DUMPER)
        subprocess.run([javac, '-encoding', 'UTF-8', '-d', work] + core_files + [dumper], check=True)
        out = subprocess.run([java, '-Dfile.encoding=UTF-8', '-cp', work, 'DumpDocs'],
                             check=True, capture_output=True, text=True).stdout
    finally:
        shutil.rmtree(work, ignore_errors=True)

    spells, schools, creatures = [], [], []
    for line in out.splitlines():
        if not line.strip():
            continue
        parts = line.split('\t')
        if parts[0] == 'SPELL':
            spells.append(parts[1:])
        elif parts[0] == 'SCHOOL':
            schools.append(parts[1:])
        elif parts[0] == 'CREATURE':
            creatures.append(parts[1:])

    lines = []
    lines.append('# Catalogue des sorts et des creatures')
    lines.append('')
    lines.append('> Ce fichier est **genere automatiquement** par `tools/generate-docs.py` '
                 'a partir du code du moteur : les valeurs affichees sont exactement celles utilisees en jeu.')
    lines.append('')
    lines.append('Les degats sont donnes pour un lanceur sans bonus (ni lame, ni piege, ni coup critique).')
    lines.append('La colonne *pips* indique le cout en pips ; `X+n` signifie un sort a pips variables.')
    lines.append('')
    lines.append('## Les sept ecoles')
    lines.append('')
    lines.append('| Ecole | Nom (EN) | Precision de base | Degats par pip | Lame | Piege | PV (x base) |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- |')
    for sid, fr, en, acc, dpp, blade, trap, hp in schools:
        lines.append(f'| {fr} | {en} | {acc} % | {dpp} | +{blade} % | +{trap} % | {float(hp):.2f} |')
    lines.append('')

    labels = {
        'WAND': 'Baguette', 'DAMAGE': 'Degats', 'DAMAGE_ALL': 'Degats de zone',
        'DRAIN': 'Drain', 'DAMAGE_OVER_TIME': 'Degats sur la duree', 'HEAL': 'Soin',
        'HEAL_ALL': 'Soin de zone', 'HEAL_OVER_TIME': 'Soin sur la duree',
        'ABSORB': 'Absorption', 'BLADE': 'Lame', 'TRAP': 'Piege', 'SHIELD': 'Bouclier',
        'WEAKNESS': 'Faiblesse', 'ACCURACY_DEBUFF': 'Malus de precision', 'STUN': 'Etourdissement',
        'SUMMON': 'Invocation', 'DECK': 'Deck', 'UNKNOWN': 'Divers',
    }

    for sid, fr, en, acc, dpp, blade, trap, hp in schools:
        school_spells = [s for s in spells if s[1] == sid]
        lines.append(f'## {fr} ({en})')
        lines.append('')
        lines.append(f'{len(school_spells)} sorts. Precision de base : {acc} %, degats par pip : {dpp}, '
                     f'lame : +{blade} %, piege : +{trap} %, PV : x{float(hp):.2f}.')
        lines.append('')
        lines.append('| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |')
        lines.append('| --- | --- | --- | --- | --- | --- | --- | --- | --- |')
        for (sid_, school, cost, accuracy, level, kind, target, copies, name_fr, name_en, desc_fr, desc_en) in school_spells:
            kind_label = labels.get(kind, kind)
            target_label = {'SINGLE': 'une cible', 'ENEMY': 'un ennemi', 'ALL_ENEMIES': 'tous les ennemis',
                            'ALLY': 'un allie', 'ALL_ALLIES': 'tous les allies', 'SELF': 'soi-meme',
                            'AURA': 'aura globale'}.get(target, target)
            lines.append(f'| **{name_fr}** | {name_en} | {cost} | {accuracy} % | {level} | '
                         f'{kind_label} | {target_label} | {copies} | {desc_fr} |')
        lines.append('')

    lines.append('## Creatures')
    lines.append('')
    lines.append('| Creature (FR) | Creature (EN) | Ecole | Niveau | PV | Entite Minecraft | Critique | Pips de puissance | Cartes | Type | Resistances |')
    lines.append('| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |')
    school_names = {s[0]: s[1] for s in schools}
    for (cid, school, level, health, entity, crit, power, deck, kind, name_fr, name_en, resist) in creatures:
        entity_label = entity.split(':')[-1] if entity else '-'
        lines.append(f'| **{name_fr}** | {name_en} | {school_names.get(school, school)} | {level} | {health} | '
                     f'{entity_label} | {crit} | {power} % | {deck} | {kind} | {resist or "aucune"} |')
    lines.append('')
    lines.append(f'Total : {len(spells)} sorts et {len(creatures)} creatures.')
    lines.append('')

    os.makedirs(os.path.dirname(DOC), exist_ok=True)
    with open(DOC, 'w', encoding='utf-8') as f:
        f.write('\n'.join(lines))
    print(f'docs/spells.md : {len(spells)} sorts, {len(creatures)} creatures')


if __name__ == '__main__':
    main()
