# Apparence des personnages

Tous les personnages — **les joueurs et les PNJ** — sont des humanoïdes habillés, dans le
style de Minecraft : une tête, deux bras, deux jambes, une peau au format habituel
(64 × 64). Aucun modèle 3D supplémentaire n'est nécessaire, donc tout reste remplaçable par
un pack de ressources.

## Ce qui est dessiné

| Personnage | Apparence |
| --- | --- |
| **PNJ** (19) | un visage, une coiffure et une tenue par PNJ : le directeur a la barbe et le grand chapeau, les gardes un casque, les professeurs le chapeau pointu aux couleurs de leur école, le pêcheur un chapeau de paille, les esprits une capuche. |
| **Joueur** | sa propre peau, **plus une robe de sorcier** posée par-dessus (chapeau pointu, robe, manches, ceinture dorée, jupe) aux couleurs de son école. Sa peau, ses cheveux et ses mains restent visibles. |

La robe du joueur est une **couche extérieure** : le mod ne remplace jamais l'apparence
d'un joueur, il l'habille. Un joueur qui n'a pas encore choisi d'école porte la robe neutre ;
dès qu'il choisit son école, la robe change de couleur.

## Voir le résultat sans lancer le jeu

```bash
python3 tools/preview-skins.py            # écrit docs/skins.png, docs/skins-visages.png, docs/skins-robes.png
python3 tools/preview-skins.py --check    # vérifie le format des apparences
python3 tools/preview-skins.py --only lea # une seule apparence
```

Le script lit les vraies images du mod, les dessine sur un modèle humanoïde et en fait une
planche : c'est exactement ce que le jeu affichera, aux mêmes régions de texture près.

`--check` vérifie, pour chaque apparence :

- la taille (64 × 64) et l'absence de pixels hors des régions du modèle ;
- qu'aucune case du corps (couche intérieure) n'est transparente — un trou se verrait à
  travers le personnage en jeu ;
- que **les yeux restent visibles** : la coiffe ne doit pas descendre sur la rangée des yeux ;
- que le visage garde assez de détails, et que quelque chose coiffe le haut du crâne.

## Remplacer une apparence

Les apparences sont des fichiers de `src/main/resources/assets/arcanum_realms/textures/entity/` :

```text
npc/<identifiant du PNJ>.png     un personnage par PNJ (lea_nightchant.png, guard_quentin.png, ...)
npc/neutral.png                  apparence de secours
wizard/<ecole>.png               robe de sorcier (pyromancy.png, tempest.png, ...)
wizard/neutral.png               robe de secours (avant le choix de l'école)
```

Un **pack de ressources** peut les remplacer sans recompiler le mod : créez le dossier
`assets/arcanum_realms/textures/entity/npc/` dans votre pack et déposez-y vos propres
images, avec exactement le même nom de fichier. Pour habiller les joueurs autrement,
remplacez `wizard/<ecole>.png` par votre robe : c'est une couche extérieure, donc dessinez
uniquement le chapeau, la veste, les manches et le haut des bottes, et laissez le reste
transparent.

Vous pouvez partir d'une peau de joueur existante : il suffit qu'elle respecte la mise en
page habituelle (tête en haut à gauche, corps à partir de (16,16), bras et jambes à leurs
places). `--check` vous dira si votre fichier est utilisable.

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
| `python3 tools/character_skins.py` | redessine les apparences des PNJ et les robes |
| `python3 tools/preview-skins.py` | planches d'aperçu et contrôle du format |
| `python3 tools/generate-resources.py` | objets, blocs, modèles, **et apparences** |
