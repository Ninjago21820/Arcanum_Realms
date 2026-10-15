# Apparence des personnages

Les personnages sont des humanoïdes dans le style de Minecraft, **et leur tenue est un vrai
modèle 3D** : chapeau en cône, cape d'épaules, manches plus larges que les bras, ceinture et
jupe évasée, en cubes volumétriques posés autour du corps.

## Le personnage

| Personnage | Apparence |
| --- | --- |
| **PNJ** (19) | une peau (visage, cheveux, couleurs) **et** une tenue 3D : le directeur porte le grand chapeau pointu et la barbe, les gardes un casque à nasal, les professeurs le chapeau pointu de leur école, le pêcheur un chapeau de paille à large bord, les esprits une capuche. |
| **Joueur** | sa propre peau, **plus la tenue 3D de sorcier** par-dessus, teintée aux couleurs de son école. Son visage, ses cheveux et ses mains restent visibles. |

La tenue est **teintée à l'affichage** (§ suivant) : une seule texture sert donc pour toutes
les robes, et chaque personnage reçoit sa couleur — celle de son école pour un joueur, celle
de sa tenue pour un PNJ.

## Le modèle 3D : comment il est construit

Tout est dans un seul fichier, `client/model/WizardRobeModel.java`. Chaque pièce tient sur une
ligne :

```java
box(head, "hat_cone_1", 44, 0, -4F, -11.5F, -4F, 8F, 2F, 8F);
```

c'est-à-dire : sur quelle partie du corps elle est attachée (`head`, `body`, `rightArm`,
`leftArm`), son nom, sa place dans la texture (`44, 0`), son coin et ses dimensions. Les
pièces attachées à la tête suivent la tête (un personnage qui se retourne tourne son chapeau),
celles attachées aux bras suivent les bras, et la jupe suit le torse.

Le repère est celui des modèles de Minecraft : les pieds à `y = 24`, les épaules à `y = 0`,
le sommet du crâne à `y = -8`, et l'axe vertical descend.

Ajouter une pièce est donc une seule ligne à écrire — **et le générateur d'apparences, les
aperçus et le contrôle du format se mettent à jour tout seuls** : ils relisent ce fichier
(voir plus bas).

## La texture de la tenue

`textures/entity/wizard/robe.png` (128 × 128) est dessinée **en niveaux de gris** puis
teintée en jeu : les plis, les ombres et les revers viennent de la texture, la couleur vient
du jeu. Les faces sont ombrées (dessus plus clair, dessous plus sombre), ce qui donne le
volume.

```bash
python3 tools/character_skins.py     # (re)dessine la texture à partir du modèle Java
```

Un **pack de ressources** peut la remplacer, comme n'importe quelle texture : déposez votre
`robe.png` dans `assets/arcanum_realms/textures/entity/wizard/`. Restez dans les gris clairs :
la texture est multipliée par la couleur du personnage.

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
wizard/robe.png                  la texture de la tenue 3D (niveaux de gris, teintée en jeu)
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
