#!/usr/bin/env python3
"""Cree un pack de ressources contenant tous les textes du mod.

Le pack permet de reecrire **dialogues, noms de PNJ et noms de sorts** (et tout le
reste : quetes, creatures, equipement, zones, interface) sans recompiler le mod :
on modifie un fichier JSON, on recharge avec F3+T, et c'est applique.

Usage :
    python3 tools/make-content-pack.py /chemin/vers/jdk17          # ecrit content/
    python3 tools/make-content-pack.py /chemin/vers/jdk17 --check content/.../fr_fr.json
"""
import importlib.util
import json
import os
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
PACK_DIR = os.path.join(ROOT, 'content', 'arcanum-realms-textes')
LANG_DIR = os.path.join(PACK_DIR, 'assets', 'arcanum_realms', 'lang')


def load_generator():
    """Importe tools/generate-lang.py (le tiret empeche un import normal)."""
    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'generate-lang.py')
    spec = importlib.util.spec_from_file_location('generate_lang', path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


PACK_MCMETA = {
    "pack": {
        "pack_format": 15,
        "description": "Arcanum Realms - textes du contenu (dialogues, PNJ, sorts, quetes)"
    }
}

README = """# Pack de textes Arcanum Realms

Ce dossier est un **pack de ressources** : il contient tous les textes du mod, et
tout ce que vous y ecrivez remplace le texte d'origine. Vous pouvez donc choisir
les dialogues, les noms des PNJ et les noms des sorts vous-meme, sans recompiler
le mod ni toucher a une seule ligne de code.

## Installation

1. Copiez le dossier `arcanum-realms-textes` dans le dossier `resourcepacks` de
   votre instance Minecraft (a cote de `mods`).
2. Dans le jeu : Options > Ressources, activez le pack s'il ne l'est pas deja.
3. Relancez le chargement des ressources avec **F3 + T** apres chaque modification.

## Ou ecrire quoi

| Ce que vous voulez changer | Cles a modifier | Exemple |
| --- | --- | --- |
| Une replique de dialogue | `dialogue.arcanum_realms.<pnj>.<moment>.<numero>` | `dialogue.arcanum_realms.lea_nightchant.greeting.0` |
| Le nom d'un PNJ | `npc.arcanum_realms.<pnj>` | `npc.arcanum_realms.lea_nightchant` |
| Le titre d'un PNJ | `npc.arcanum_realms.<pnj>.title` | `npc.arcanum_realms.headmaster_alderwise.title` |
| Le nom d'un sort | `spell.arcanum_realms.<sort>.name` | `spell.arcanum_realms.ember_kitten.name` |
| La description d'un sort | `spell.arcanum_realms.<sort>.desc` | `spell.arcanum_realms.ember_kitten.desc` |
| Le nom d'une creature | `creature.arcanum_realms.<creature>` | `creature.arcanum_realms.lost_soul` |
| Le nom d'une quete | `quest.arcanum_realms.<quete>` | `quest.arcanum_realms.the_cure` |
| Le resume d'une quete | `quest.arcanum_realms.<quete>.summary` | `quest.arcanum_realms.the_cure.summary` |
| Un objectif de quete | `quest.arcanum_realms.<quete>.goal.<numero>` | `quest.arcanum_realms.the_cure.goal.0` |
| Les recompenses affichees | `quest.arcanum_realms.<quete>.rewards` | `quest.arcanum_realms.the_cure.rewards` |
| Le nom d'une zone | `zone.arcanum_realms.<zone>` | `zone.arcanum_realms.allee_braises` |
| Une piece d'equipement | `gear.arcanum_realms.<piece>` | `gear.arcanum_realms.ember_cowl` |
| L'interface, les messages, les commandes | cles commencant par `arcanum_realms.` ou `gui.arcanum_realms.` | `arcanum_realms.quest.completed` |

Les quatre **moments** d'un dialogue sont, dans l'ordre :

- `greeting` : le PNJ vous voit arriver ;
- `inProgress` : il vous rappelle ce qu'il attend ;
- `completion` : vous revenez avec la quete finie ;
- `farewell` : vous fermez la conversation.

Les numeros de repliques commencent a 0. Si vous en ajoutez, gardez la suite des
numeros (`...greeting.0`, `...greeting.1`, `...greeting.2`).

`fr_fr.json` sert quand le jeu est en francais, `en_us.json` quand il est en
anglais. Les deux fichiers contiennent les memes cles : modifiez celui de votre
langue, et laissez l'autre tel quel si vous ne jouez pas en anglais.

## Verifier votre fichier

```bash
# 1. coherence : cles inconnues, valeurs vides, ce que vous avez personnalise
python3 tools/make-content-pack.py /chemin/vers/jdk17 --check content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json

# 2. rendu en jeu (sans lancer Minecraft) : dialogues, noms de sorts, journal de combat
python3 tools/make-content-pack.py /chemin/vers/jdk17 --simulate content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json
```

La premiere commande signale les cles inconnues et les valeurs vides. La seconde
affiche ce que le jeu montrera : vos dialogues, vos noms de sorts dans le grimoire
et dans le **journal de combat** (ou les noms ecrits par le moteur sont remplaces
par les votres).

## Utiliser seulement une partie du pack

Vous pouvez supprimer toutes les cles que vous ne modifiez pas : seules celles
qui restent sont appliquees. Par exemple, pour ne changer que les trois premiers
dialogues de Lea, gardez uniquement :

```json
{
  "dialogue.arcanum_realms.lea_nightchant.greeting.0": "Tiens, un nouveau visage.",
  "dialogue.arcanum_realms.lea_nightchant.greeting.1": "Les ames perdues rodent encore sur le sentier.",
  "dialogue.arcanum_realms.lea_nightchant.farewell.0": "Revenez vite, on a besoin de vous."
}
```
"""


def write_pack(english, french):
    os.makedirs(LANG_DIR, exist_ok=True)
    with open(os.path.join(PACK_DIR, 'pack.mcmeta'), 'w', encoding='utf-8') as f:
        json.dump(PACK_MCMETA, f, indent=2, ensure_ascii=False)
        f.write('\n')
    for name, table in (('en_us', english), ('fr_fr', french)):
        with open(os.path.join(LANG_DIR, name + '.json'), 'w', encoding='utf-8') as f:
            json.dump(dict(sorted(table.items())), f, indent=2, ensure_ascii=False)
            f.write('\n')
    with open(os.path.join(PACK_DIR, 'README.md'), 'w', encoding='utf-8') as f:
        f.write(README)
    dialogues = sum(1 for key in french if key.startswith('dialogue.'))
    npcs = sum(1 for key in french if key.startswith('npc.'))
    spells = sum(1 for key in french if key.startswith('spell.') and key.endswith('.name'))
    print(f'content/arcanum-realms-textes : {len(french)} cles '
          f'({dialogues} repliques, {npcs} cles de PNJ, {spells} noms de sorts)')
    print('  -> a copier dans .minecraft/resourcepacks, puis F3+T en jeu')


# Familles de textes dont les noms sont reecrits dans les textes composes par le
# moteur (journal de combat, messages d'objectif). Miroir de ContentNames.java.
NAME_FAMILIES = ('spell.', 'creature.', 'npc.', 'gear.', 'zone.', 'school.')
NOT_SOURCES = ('.desc', '.description')


def is_source(key):
    """Vrai si cette cle porte un nom susceptible d'apparaitre dans un texte compose."""
    if not key.startswith(NAME_FAMILIES):
        return False
    return not key.endswith(NOT_SOURCES)


def simulate(path, triples, english, french):
    """Simule le jeu avec un pack de textes, pour verifier avant de lancer Minecraft.

    Reproduit ce que fait le client : le pack remplace les textes portant une cle,
    et les noms reecrits sont substitues dans les textes composes par le moteur.
    """
    with open(path, encoding='utf-8') as f:
        overrides = json.load(f)

    def shown(key, fallback):
        # pack de ressources, puis fichier de langue du mod, puis texte par defaut
        value = overrides.get(key) or french.get(key) or fallback
        return value

    # texte par defaut, dans les deux langues (le journal de combat melange les deux)
    defaults = {}
    for key, en, fr in triples:
        defaults.setdefault(key, (en, fr))

    table = {}
    for key, (en, fr) in defaults.items():
        if not is_source(key):
            continue
        for source in (en, fr):
            renamed = shown(key, source)
            if renamed != source:
                table[source] = renamed
    # les noms les plus longs d'abord, comme dans ContentNames
    ordered = sorted(table, key=len, reverse=True)

    def substitute(text):
        for source in ordered:
            if source in text:
                text = text.replace(source, table[source])
        return text

    print(f'Simulation avec {os.path.basename(path)} ({len(overrides)} cles ecrites)')
    print()
    changed = [key for key in overrides if key in french and overrides[key] != french[key]]
    print(f'Textes reellement remplaces : {len(changed)}')
    for key in sorted(changed)[:6]:
        print(f'  {key}')
        print(f'    avant : {french[key]}')
        print(f'    apres : {overrides[key]}')
    if len(changed) > 6:
        print(f'  ... et {len(changed) - 6} autre(s)')
    print()

    samples = [
        ('Dialogue affiche', overrides.get('dialogue.arcanum_realms.lea_nightchant.greeting.0')),
        ('Nom de sort dans le grimoire', overrides.get('spell.arcanum_realms.ember_kitten.name')),
        ('Journal de combat (texte du moteur)', None),
    ]
    print('Rendu en jeu :')
    for label, value in samples:
        if value is not None:
            print(f'  {label} : {value}')
    if table:
        print('  Journal de combat (noms de sorts du moteur, reecrits par le pack) :')
        spells = [en_fr for key, en_fr in defaults.items()
                  if key.startswith('spell.') and key.endswith('.name')]
        for caster, cast in zip(spells[:2], spells[1:3]):
            original = f'{caster[0]} casts {cast[0]} (2 pips)'
            rendered = substitute(original)
            marker = '  ' if rendered != original else '  (inchange) '
            print(f'    {marker}{original}')
            if rendered != original:
                print(f'      -> {rendered}')
    else:
        print('  Aucun nom renomme : la substitution ne change rien.')


def check(path, reference):
    """Verifie un fichier de langue modifie a la main par le joueur."""
    with open(path, encoding='utf-8') as f:
        data = json.load(f)
    problems = []
    unknown = [key for key in data if key not in reference]
    empty = [key for key, value in data.items() if not str(value).strip() or str(value).strip() == key]
    if unknown:
        problems.append(f'{len(unknown)} cle(s) inconnue(s) : ' + ', '.join(sorted(unknown)[:5]))
    if empty:
        problems.append(f'{len(empty)} valeur(s) vide(s) : ' + ', '.join(sorted(empty)[:5]))

    dialogues = [key for key in reference if key.startswith('dialogue.')]
    spells = [key for key in reference if key.startswith('spell.') and key.endswith('.name')]
    npcs = [key for key in reference if key.startswith('npc.') and key.count('.') == 2]
    print(f'{os.path.basename(path)} : {len(data)} cles ecrites')
    print(f'  dialogues du mod : {len(dialogues)} ; noms de sorts : {len(spells)} ; noms de PNJ : {len(npcs)}')
    print(f'  dialogues personnalises : {sum(1 for key in dialogues if key in data)}')
    print(f'  sorts renommes : {sum(1 for key in spells if key in data)}')
    print(f'  PNJ renommes : {sum(1 for key in npcs if key in data)}')
    if problems:
        print('PROBLEMES :')
        for problem in problems:
            print('  -', problem)
        return 1
    print('Aucun probleme : le pack est coherent.')
    return 0


def main():
    jdk = sys.argv[1] if len(sys.argv) > 1 else '/tmp/jdk17'
    generator = load_generator()
    english, french = generator.tables(jdk)

    if '--check' in sys.argv:
        path = sys.argv[sys.argv.index('--check') + 1]
        sys.exit(check(path, french))

    if '--simulate' in sys.argv:
        path = sys.argv[sys.argv.index('--simulate') + 1]
        simulate(path, [line.split('\t') for line in generator.dump(jdk).splitlines() if line.strip()],
                 english, french)
        return

    write_pack(english, french)


if __name__ == '__main__':
    main()
