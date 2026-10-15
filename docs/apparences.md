# Apparence des personnages

Les personnages sont des humanoïdes dans le style de Minecraft, **et leur tenue est un vrai
modèle 3D** : robe longue, cape qui bouge, manches évasées, ceinture, jupe à ourlet doré et
coiffe en volume — construits comme un modèle Blockbench, avec des parties articulées et
animées.

## Le personnage

| Personnage | Apparence |
| --- | --- |
| **PNJ** (19) | une peau (visage, cheveux, couleurs) **et** une tenue 3D : le directeur porte le grand chapeau pointu et la barbe, les gardes un casque à nasal, les professeurs le chapeau pointu de leur école, le pêcheur un chapeau de paille à large bord, les esprits une capuche. |
| **Joueur** | sa propre peau, **plus la tenue 3D de sorcier** par-dessus, teintée aux couleurs de son école. Son visage, ses cheveux et ses mains restent visibles. |

La tenue est **teintée à l'affichage** (§ suivant) : une seule texture sert donc pour toutes
les robes, et chaque personnage reçoit sa couleur — celle de son école pour un joueur, celle
de sa tenue pour un PNJ.

## Le modèle 3D : une table de pièces

Tout est dans un seul fichier, `client/model/WizardRobeModel.java`, sous forme de **table** —
comme un modèle Blockbench, mais lisible dans le code :

```text
piece  <partie> <nom> <matière> <u> <v> <x> <y> <z> <largeur> <hauteur> <profondeur>
pivot  <partie> <x> <y> <z>
```

Par exemple, les pièces de la jupe et la cape de dos :

```text
pivot  cape 0 0 -2
piece  cape cape_main cloth 0 144 -4.6 0 -1.6 9.2 15 1.5
piece  cape cape_hem detail 32 144 -4.8 14.6 -2.1 9.6 1.4 2.6
pivot  skirt 0 11 0
piece  skirt skirt_low cloth 76 168 -6.2 8 -3.6 12.4 4.6 7.2
```

- **partie** : à quoi la pièce est attachée (`head`, `body`, `right_arm`, `left_arm`, `cape`,
  `skirt`). Une pièce attachée à la tête tourne avec la tête ; la cape et la jupe ont leur
  propre `pivot` (le haut de la cape, la taille) et peuvent donc bouger seules.
- **matière** : `cloth` (tissu, **teinté** par la couleur de l'école ou du PNJ) ou `detail`
  (métal, cuir, galons, gemme — qui gardent leurs couleurs).
- **u, v** : où la pièce se trouve dans la texture ; **x, y, z** son coin et ses dimensions.

Repère : celui des modèles de Minecraft — épaules à `y = 0`, taille à `y = 12`, pieds à
`y = 24`, et l'axe vertical descend.

Ajouter une pièce, élargir une manche ou allonger la jupe se fait donc **en changeant un
nombre**, et le générateur de textures, les planches d'aperçu et le contrôle du format suivent
tout seuls : ils relisent cette table (voir plus bas).

## Les animations

`animate(...)` fait vivre la tenue par-dessus la pose du personnage :

| Ce qui bouge | Comment |
| --- | --- |
| **Cape** | elle pend, puis s'envole avec la marche, et se balance latéralement au rythme des pas |
| **Jupe** | elle ondule au rythme des pas et respire très légèrement à l'arrêt |
| **Manches** | léger retard sur le bras, et claquement quand le personnage frappe |
| **Tissu** | la respiration soulève le torse de quelques centièmes d'unité |
| **Coiffe** | elle suit l'inclinaison de la tête |

Tout part de trois valeurs que Minecraft donne à chaque image : la phase de marche
(`limbSwing`), son amplitude (`limbSwingAmount`) et le temps écoulé (`ageInTicks`).

## Les textures (deux matières)

| Fichier | Rôle |
| --- | --- |
| `textures/entity/wizard/cloth.png` (256 × 256) | le tissu, dessiné **en niveaux de gris** puis teinté en jeu (couleur de l'école pour un joueur, couleur de tenue pour un PNJ) |
| `textures/entity/wizard/detail.png` (256 × 256) | le métal, le cuir, les galons, la gemme — jamais teintés |

Les faces sont ombrées (dessus plus clair, dessous plus sombre) et un pli marque le milieu du
tissu : c'est ce qui donne du volume, même à distance. Le modèle est dessiné en deux passes,
une par matière, ce qui permet au tissu d'être coloré sans que le métal le soit.

```bash
python3 tools/character_skins.py     # (re)dessine les deux textures à partir du modèle Java
```

Un **pack de ressources** peut les remplacer : déposez vos `cloth.png` et `detail.png` dans
`assets/arcanum_realms/textures/entity/wizard/`. Pour le tissu, restez dans les gris clairs :
il est multiplié par la couleur du personnage.

## Voir le résultat sans lancer le jeu

```bash
python3 tools/preview-skins.py            # docs/skins.png, docs/skins-visages.png, docs/skins-tenues.png
python3 tools/preview-skins.py --check    # vérifie le format des apparences et de la tenue
python3 tools/preview-skins.py --only lea # une seule apparence
```

Le script lit les vraies images du mod, les dessine sur un modèle humanoïde et en fait une
planche : c'est exactement ce que le jeu affichera, aux mêmes régions de texture près.

`--check` vérifie, pour chaque apparence :

- la taille (64 × 64) et l'absence de pixels hors des régions du modèle ;
- qu'aucune case du corps (couche intérieure) n'est transparente — un trou se verrait à
  travers le personnage en jeu ;
- que **les yeux restent visibles** : la coiffe ne doit pas descendre sur la rangée des yeux ;
- que le visage garde assez de détails, et que quelque chose coiffe le haut du crâne ;
- pour la tenue 3D : que chaque pièce du modèle a bien sa place peinte dans la texture, et que
  la texture reste assez claire et contrastée pour être teintée.

## Remplacer une apparence

Les apparences sont des fichiers de `src/main/resources/assets/arcanum_realms/textures/entity/` :

```text
npc/<identifiant du PNJ>.png     la peau d'un PNJ (lea_nightchant.png, guard_quentin.png, ...)
npc/neutral.png                  peau de secours
wizard/cloth.png                 le tissu de la tenue 3D (niveaux de gris, teinté en jeu)
wizard/detail.png                les détails de la tenue 3D (métal, cuir, galons)
```

Un **pack de ressources** peut les remplacer sans recompiler le mod : créez le dossier
`assets/arcanum_realms/textures/entity/npc/` dans votre pack et déposez-y vos propres images,
avec exactement le même nom de fichier. La forme du personnage (visage, cheveux, mains) vient
de cette peau ; la tenue par-dessus vient du modèle 3D.

Vous pouvez partir d'une peau de joueur existante : il suffit qu'elle respecte la mise en page
habituelle (tête en haut à gauche, corps à partir de (16, 16), bras et jambes à leurs places).
`--check` vous dira si votre fichier est utilisable.

## Changer la coiffe d'un PNJ

La coiffe est choisie dans le catalogue du moteur (`core/world/Npcs.java`) :

```java
add(NpcDef.builder("fisherman_pike")
        ...
        .outfit(0x66BB6A).attire(Attire.STRAW)
        ...
```

Les coiffes possibles sont dans `core/world/Attire.java` : `NONE`, `POINTED` (chapeau pointu),
`HOOD` (capuche), `HELM` (casque), `CAP` (petit chapeau), `STRAW` (chapeau de paille). Le
modèle 3D et le générateur lisent tous les deux ce choix : une seule ligne à changer.

## Redessiner toutes les apparences

Les apparences sont **générées** par `tools/character_skins.py`. Les traits de chaque PNJ
(teint, cheveux, coiffe, barbe, yeux) sont déclarés au début du fichier :

```python
LOOKS = {
    'headmaster_alderwise': dict(skin='fair', hair_colour='white', hair_style='long',
                                 eyes='grey', beard='full', hat='tall', mouth='grim'),
    ...
}
```

Les **couleurs de tenue ne sont pas écrites ici** : elles sont lues dans le catalogue du
moteur (`Npcs.java`), donc changer une robe se fait à un seul endroit, et la peau suit. Les
PNJ sans traits déclarés reçoivent une apparence tirée de leur identifiant, ce qui garantit
que deux personnages ne se ressemblent pas.

Après une modification :

```bash
python3 tools/character_skins.py      # redessine les apparences
python3 tools/preview-skins.py        # régénère les planches d'aperçu
```

## Comportement des personnages

Les PNJ se comportent comme des villageois : ils **suivent le joueur du regard** quand il
approche, se retournent de temps en temps, font quelques pas **autour de leur poste**, et
saluent d'un geste de la main au passage. Toucher un PNJ lui fait dire une réplique.

Tout se règle dans la configuration (`config/arcanum_realms-common.toml`) :

```toml
[npc]
    # Les PNJ font quelques pas autour de leur poste (comme des villageois).
    wander = true
    # Distance maximale, en blocs, à laquelle un PNJ s'éloigne de son poste.
    wanderRadius = 6
```

Mettez `wander = false` pour qu'ils restent strictement immobiles, debout à leur poste.

## Outils

| Commande | Rôle |
| --- | --- |
| `./tools/install-jdk.sh` | installe un JDK 17 local (`tools/.jdk`) pour lancer tests et générateurs |
| `python3 tools/character_skins.py` | redessine les peaux des PNJ et la texture de la tenue 3D |
| `python3 tools/preview-skins.py` | planches d'aperçu (personnages, visages, tenues) et contrôle du format |
| `python3 tools/generate-resources.py` | objets, blocs, modèles, **et apparences** |
