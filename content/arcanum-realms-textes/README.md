# Pack de textes Arcanum Realms

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
