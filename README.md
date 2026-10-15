# Arcanum Realms

Un mod **Minecraft Forge 1.20.1** qui transforme les combats en **duels par cartes a pips**,
inspires de Wizard101 : sept ecoles de magie, deck construit par le joueur, main de 7 cartes,
lames et pieges, fizzle, critiques, degats sur la duree, serviteurs et combats jusqu'a
4 sorciers contre un groupe de creatures.

Le mod est prevu pour un **serveur prive entre amis**. Les noms (ecoles, sorts, creatures) sont
originaux, mais les **mecaniques et les nombres** reproduisent fideles ceux du jeu d'origine.

---

## Sommaire

- [Ce que fait la version actuelle](#ce-que-fait-la-version-actuelle)
- [Installation](#installation)
- [Premiers pas en jeu](#premiers-pas-en-jeu)
- [Commandes](#commandes)
- [Ecrire vos propres textes](#ecrire-vos-propres-textes)
- [Apparence des personnages](#apparence-des-personnages)
- [Touches](#touches)
- [Reglages](#reglages)
- [Documentation](#documentation)
- [Compiler depuis les sources](#compiler-depuis-les-sources)
- [Structure du projet](#structure-du-projet)
- [Feuille de route](#feuille-de-route)
- [Licence](#licence)

---

## Ce que fait la version actuelle

**Etape 1 - le combat complet** (livree ici) :

- **7 ecoles** avec leurs vraies statistiques : Tempete (125 degats/pip, 70 % de precision),
  Pyromancie (100/75 %), Runomancie (90/80 %), Givre (85/80 %, beaucoup de PV),
  Sylve (90 %), Necromancie (85 %), Equilibre (lames et pieges pour tous).
- **112 sorts** dans le catalogue : attaques simples et de zone, drains, degats sur la duree,
  soins et soins sur la duree, lames, pieges, boucliers, faiblesses, charmes, etourdissements,
  percee, purges, invocations, auras globales et sorts a pips variables.
- **Pips fideles** : 7 emplacements, 1 pip par round, pips de puissance (valeur 2, uniquement
  dans votre ecole), paiement au moindre gaspillage, sorts a `X` pips.
- **Deck et main** : deck de 1 a 40 cartes construit dans le grimoire, main de 7 cartes,
  defausse au clic droit, melange automatique de la defausse (et sort *Remelange*).
- **Precision et fizzle**, **coups critiques et blocage** (formules de Wizard101), **lames**,
  **pieges**, **boucliers**, **DoT/HoT**, **absorption**, **etourdissements**, **serviteurs**
  (3 maximum par camp) et **auras globales** (*Deuil et Tenebres*, *Equilibre parfait*).
- **Duel de groupe** : jusqu'a 4 sorciers contre 1 a 4 creatures, ordre de tour alterne.
- **Creatures intelligentes** avec leur propre deck et trois comportements (prudente, normale,
  agressive), plus deux boss.
- **Declencheurs de duel** : creatures errantes (clic droit avec la baguette ou approche
  automatique), **Cercle Arcanique** activable, et commandes de test.
- **Progression** : XP, niveaux, apprentissage automatique des sorts, PV et critique qui
  evoluent, degats de defaite.
- **Interface de duel** : cartes colorees par ecole, ciblage a la souris, journal de combat,
  pips en losanges, barre de vie des adversaires.
- **Grimoire** : liste des sorts appris, construction du deck, enregistrement cote serveur.

**Etape 2 - le monde** (livree ici) :

- **7 zones** de la Cite des Arcanes, ouvertes par la progression : la Grand-Place, le Bosquet
  des Sept (une clairiere par ecole), le Sentier des Lucioles, l'Allee des Braises, l'Avenue des
  Marees, le Chemin des Colosses et le donjon de la Grotte Hantee.
- **35 creatures** reprises des fiches de la wiki Wizard101 Central : rang, points de vie,
  pips de depart, resistances et faiblesses par ecole, classe (Esprit, Mort-vivant, Fee, Elfe...),
  etourdissable, envoutable, liste de sorts et table de butin.
- **19 PNJ** : directeur, sept professeurs d'ecole, gardes, marchand, villageois et esprits,
  chacun avec ses repliques d'accueil, d'attente, de remise de quete et d'adieu, en francais et
  en anglais.
- **50 quetes** : une chaine principale de 36 quetes (l'Allee des Braises suit ses **douze
  etapes scenarisees** d'origine), des quetes annexes et les sept epreuves d'ecole, avec des
  objectifs *vaincre / recolter / parler / se rendre a*.
- **Journal de quetes** persistant, progression partagee dans un rayon de 40 blocs, recompenses
  en XP, or, sorts, equipement et choix de recompense.
- **Personnages humanoïdes et tenues 3D** : 19 PNJ avec visage, coiffure et tenue, robe de
  sorcier en volume (chapeau, cape animée, manches, ceinture, jupe à ourlet) teintée par
  l'école pour les joueurs, comportement de villageois.
- **Textes entierement a vous** : dialogues, noms de PNJ et noms de sorts sont exposes en
  **cles de traduction** — un pack de ressources suffit pour les reecrire (et jusqu'au journal
  de combat), sans recompiler. Voir [Ecrire vos propres textes](#ecrire-vos-propres-textes).
- **Equipement** : 47 pieces en 5 paliers et 8 emplacements (chapeau, robe, bottes, baguette,
  athame, anneau, amulette, deck) avec PV, pips de puissance, critique, blocage, precision,
  percee, resists et bonus de degats ; portees a la souris, statistiques appliquees en duel.
- **Butin** : 10 tables (par rang de creature et par boss) avec or et objets.
- **Boutique** : le marchand vend le palier correspondant au niveau du joueur.
- **Integration au monde** : discussion avec un PNJ, ramassage d'objets, mort de creature et entree dans une
  zone font progresser les quetes, et une zone se pose dans le monde avec `/arcanum zones set`.

L'etape 3 (familiers, jardinage, peche, PvP) est planifiee : voir la
[feuille de route](#feuille-de-route).

---

## Installation

1. Installez **Minecraft Forge 47.x pour Minecraft 1.20.1** sur votre PC et sur le serveur.
2. Placez le fichier `arcanum_realms-0.1.0.jar` dans le dossier `mods/` **du client et du serveur**
   (le mod doit etre present des deux cotes : le serveur gere les duels, le client affiche
   l'interface).
3. Lancez le jeu. Le fichier de reglages apparait dans `config/arcanum_realms-common.toml`.

Aucune dependance supplementaire n'est necessaire.

---

## Premiers pas en jeu

1. **Fabriquez la Baguette de Duel** : une amethyste en haut, deux batons en diagonale
   (recette en forme de baguette), ou tapez `/arcanum duel` directement.
2. **Choisissez votre ecole** : `/arcanum school pyromancy` (ou `tempest`, `frost`, `runomancy`,
   `sylvan`, `necromancy`, `equilibrium`). Le deck de depart est cree automatiquement.
3. **Regardez votre grimoire** : touche `G`, clic droit dans le vide avec la baguette, ou
   `/arcanum grimoire`. Clic gauche pour ajouter une carte, clic droit pour en retirer,
   puis **Enregistrer le deck**.
4. **Lancez un duel** : clic droit avec la baguette sur n'importe quelle creature hostile
   (zombie, squelette, araignee, pillard...). Les creatures proches rejoignent le combat.
5. **Jouez le round** : cliquez une carte, puis une cible (ou directement un allie pour les
   sorts de soin). Clic droit sur une carte pour la defausser, bouton **Passer** pour garder
   vos pips, bouton **Fuir** pour sortir du duel.
6. **Construisez un Cercle Arcanique** (8 pierres taillees + 1 amethyste) pour faire apparaitre
   une rencontre complete a l'endroit de votre choix - pratique pour les duels de groupe.

7. **Posez le monde** : placez-vous a l'endroit qui doit devenir la Grand-Place et tapez
   `/arcanum zones set grand_place`, puis `/arcanum pnj ici` pour y installer le directeur, le
   marchand et le garde. Repetez pour les autres zones (`/arcanum zones` liste ce qui manque).
8. **Suivez les quetes** : parlez au directeur (clic droit sur le PNJ). Les objectifs se
   valident en jouant normalement : tuer des creatures, ramasser les reagents, parler aux PNJ et
   entrer dans les zones. `/arcanum quetes` affiche le journal.

Astuce de test : `/arcanum level 50` pour debloquer presque tous les sorts et
`/arcanum resolve` pour resoudre le round immediatement. Pensez aussi a baisser
`planning_seconds` dans la config si vous jouez seul.

---

## Commandes

Toutes les commandes sont accessibles a tous les joueurs (usage entre amis).

| Commande | Effet |
| --- | --- |
| `/arcanum help` | Liste des commandes |
| `/arcanum info` | Statistiques du mod et duels en cours |
| `/arcanum duel` | Defie les creatures proches |
| `/arcanum arena` | Fait apparaitre une rencontre et lance le duel |
| `/arcanum resolve` | Resout le round immediatement |
| `/arcanum leave` | Quitte le duel en cours |
| `/arcanum noauto <true\|false>` | Active/desactive les duels automatiques |
| `/arcanum school <ecole>` | Change d'ecole (reinitialise le deck) |
| `/arcanum level <n>` | Regle le niveau et apprend les sorts |
| `/arcanum powerpips <0-100>` | Chance de pip de puissance |
| `/arcanum crit <score>` | Score critique |
| `/arcanum grimoire` | Ouvre le grimoire |
| `/arcanum deck <reset\|info>` | Reinitialise ou affiche le deck |
| `/arcanum spells [ecole]` | Liste les sorts (nom, pips, precision, niveau) |
| `/arcanum creatures` | Liste les creatures (niveau, PV, ecole) |
| `/arcanum creatures sauvages [id]` | Fait apparaitre les creatures de la zone |
| `/arcanum zones` | Liste les zones, leur plage de niveaux et leur quete d'ouverture |
| `/arcanum zones set <zone> [rayon]` | Pose (ou deplace) une zone ici, rayon en blocs |
| `/arcanum zones del <zone>` | Retire l'ancre d'une zone |
| `/arcanum pnj` | Liste les PNJ, leur zone et les quetes qu'ils donnent |
| `/arcanum pnj <id>` | Fait apparaitre ce PNJ a votre position |
| `/arcanum pnj ici` | Fait apparaitre en cercle tous les PNJ de la zone |
| `/arcanum quetes [id]` | Journal de quetes, ou detail d'une quete |
| `/arcanum quete <accepter\|abandonner\|remettre> <id>` | Agit sur une quete |
| `/arcanum or <montant>` | Ajoute (ou retire) de l'or |
| `/arcanum equipement [id\|palier <0-4>\|retirer <slot>]` | Donne ou retire de l'equipement |

---

## Touches

| Touche | Action |
| --- | --- |
| `R` | Rouvrir l'interface de duel si elle a ete fermee |
| `G` | Ouvrir le grimoire |

Les deux touches sont reconfigurables dans Options > Commandes > Arcanum Realms.

---

## Reglages

Tout est reglable dans `config/arcanum_realms-common.toml` : declenchement automatique des
duels et rayon, taille des groupes, activation des fizzle et des critiques, multiplicateurs
globaux de degats et de soins, duree de la phase de choix, degats de defaite, limite de deck,
courbe d'XP et rencontres du Cercle Arcanique. Le detail de chaque cle est dans
[docs/mechanics.md](docs/mechanics.md#11-reglages-configarcanum_realms-commontoml).

---

## Apparence des personnages

**Tous les personnages sont des humanoïdes habillés dans le style de Minecraft**, et leur
tenue est un **vrai modèle 3D** : robe longue, cape qui s'envole à la marche, manches évasées,
ceinture dorée, jupe à ourlet et coiffe en volume. Les 19 PNJ ont chacun leur peau et leur coiffure (le
directeur et son grand chapeau, les gardes et leur casque à nasal, les professeurs et le
chapeau pointu de leur école, le pêcheur et son chapeau de paille, les esprits et leur
capuche), et **les joueurs portent la tenue de sorcier teintée aux couleurs de leur école** —
par-dessus leur propre peau, dont ils gardent le visage, les cheveux et les mains.

```bash
python3 tools/preview-skins.py --check    # format des apparences et de la tenue 3D
```

Les personnages se comportent comme des villageois : ils suivent le joueur du regard, font
quelques pas autour de leur poste et saluent d'un geste de la main au passage.

```bash
python3 tools/preview-skins.py            # planches d'aperçu : docs/skins*.png
python3 tools/preview-skins.py --check    # vérifie chaque apparence et la tenue 3D
```

La tenue 3D se décrit comme un modèle Blockbench, sous forme de table dans
`client/model/WizardRobeModel.java` :

```text
piece  <partie> <nom> <matière> <u> <v> <x> <y> <z> <largeur> <hauteur> <profondeur>
pivot  <partie> <x> <y> <z>
```

La matière `cloth` est **teintée** (couleur de l'école du joueur, ou de la tenue du PNJ) et
`detail` garde ses couleurs (métal, cuir, galons). La cape, la jupe et les manches ont leurs
propres pivots et **s'animent** : la cape s'envole à la marche, la jupe ondule, les manches
claquent quand on frappe. Le générateur de textures, les aperçus et le contrôle du format
relisent cette table, donc tout suit. Les apparences sont des images de
`textures/entity/npc/<pnj>.png` et `textures/entity/wizard/{cloth,detail}.png` :
**un pack de ressources peut les remplacer** sans recompiler le mod. Tout est détaillé dans
[docs/apparences.md](docs/apparences.md).

---

## Ecrire vos propres textes

**Dialogues, noms de PNJ et noms de sorts sont entierement a vous** : ils passent par
des cles de traduction, donc rien n'est fige dans le code.

```bash
python3 tools/make-content-pack.py /chemin/vers/jdk17
```

Cree `content/arcanum-realms-textes/` : un **pack de ressources** avec tous les textes
du mod (859 cles, dont 91 repliques de dialogue, 112 noms de sorts et 19 PNJ). Copiez-le
dans `resourcepacks`, activez-le, puis **F3 + T** pour recharger : vos textes remplacent
ceux d'origine, sans recompiler le mod. Vous ne gardez que les cles qui vous interessent.

```json
{
  "npc.arcanum_realms.lea_nightchant": "Lea Chantenuit",
  "dialogue.arcanum_realms.lea_nightchant.greeting.0": "Tiens, un nouveau visage.",
  "spell.arcanum_realms.ember_kitten.name": "Chaton de Braise"
}
```

Verification de vos fichiers, et rendu en jeu sans lancer Minecraft :

```bash
python3 tools/make-content-pack.py /chemin/vers/jdk17 --check content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json
python3 tools/make-content-pack.py /chemin/vers/jdk17 --simulate content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json
```

Tout est detaille dans [docs/content.md](docs/content.md) (conventions des cles, les quatre
moments d'un dialogue, comment ecrire les textes directement dans le code source).

---

## Documentation

- [docs/mechanics.md](docs/mechanics.md) : toutes les mecaniques de duel (pips, main, ordre de
  calcul des degats, fizzle, critiques, etats, IA, XP, reglages).
- [docs/spells.md](docs/spells.md) : catalogue complet des 112 sorts et des 15 creatures,
  **genere automatiquement** depuis le moteur de jeu.
- [docs/apparences.md](docs/apparences.md) : peaux des PNJ, modele 3D des tenues (chapeau,
  cape, manches, jupe), planches d'apercu, controle du format, comportement des personnages.
- [docs/content.md](docs/content.md) : ecrire vos dialogues, noms de PNJ et noms de sorts
  (pack de ressources, conventions des cles, verification).
- [docs/world.md](docs/world.md) : contenu du monde — zones, bestiaire avec resistances et
  butin, PNJ, chaine de quetes, equipement et tables de butin, **genere automatiquement**.

---

## Compiler depuis les sources

Prerequis : **JDK 17** (ou 21) et une connexion internet pour le premier build (ForgeGradle
telecharge Minecraft et Forge).

```bash
./gradlew build          # produit build/libs/arcanum_realms-0.1.0.jar
./gradlew runClient      # lance un client de test
./gradlew runServer      # lance un serveur de test
```

### Tests du moteur

Le coeur du jeu (ecoles, sorts, cartes, pips, combat, IA) est **independant de Minecraft** : il
se compile et se teste en quelques secondes, sans lancer le jeu.

```bash
./tools/run-core-tests.sh              # detecte un JDK 17 et lance les 27 tests
./tools/run-core-tests.sh /chemin/jdk  # ou en indiquant le JDK
```

### Regenerer les ressources et la documentation

```bash
python3 tools/generate-resources.py              # modeles, textures, recettes, butin
python3 tools/generate-lang.py /chemin/jdk17     # fichiers de langue a partir du moteur
python3 tools/generate-docs.py /chemin/jdk17       # docs/spells.md
python3 tools/generate-world-docs.py /chemin/jdk17 # docs/world.md
python3 tools/make-content-pack.py /chemin/jdk17   # content/ : vos textes a editer
python3 tools/character_skins.py                   # apparences des PNJ et robes des joueurs
python3 tools/preview-skins.py                     # planches d'apercu des apparences
```

---

## Structure du projet

```
src/main/java/com/arcanumrealms/
  core/          moteur de duel pur Java (aucune dependance Minecraft)
    school/      les 7 ecoles et leurs statistiques
    spell/       sorts, catalogue, effets, description lisible
    status/      lames, pieges, boucliers, DoT/HoT, absorption...
    card/        pioche, main, defausse, melange
    battle/      Combatant, Battle, pips, evenements, configuration
    ai/          intelligence des creatures
    minion/      creatures et serviteurs
    world/       zones, bestiaire, PNJ, quetes, journal, equipement, butin
  network/       paquets Forge (duel, quetes, dialogues, fiche du sorcier)
  command/       commandes /arcanum
  client/        interface de duel, grimoire, journal de quetes, dialogue, HUD, touches
  entity/        entite PNJ (wizard_npc) et son rendu
  server/        services serveur : quetes, dialogues, apparition des creatures,
                 evenements du monde (progression en jeu)
  world/         ZoneMap : position des zones dans le monde (donnee sauvegardee)
  item/ block/   baguette, cercle arcanique et objet d'equipement
  player/        fiche du sorcier persistante (NBT : niveau, or, quetes, equipement)
  battle/        ServerBattleManager : orchestration des duels cote serveur
src/main/resources/   mods.toml, traductions, modeles, textures, recettes
src/test/java/        52 tests du moteur
```

Le moteur ne connait pas Minecraft : le serveur lui parle par l'interface `BattleHost`
(evenements de combat) et le client ne fait qu'**afficher** l'etat envoye par le serveur, ce qui
evite toute desynchronisation.

---

## Feuille de route

| Etape | Contenu | Etat |
| --- | --- | --- |
| 1 | Combat complet : ecoles, sorts, deck, pips, duels, IA, XP, interface | **livree** |
| 2 | Monde : 7 zones, 35 creatures, 19 PNJ, 50 quetes, dialogues, journal, equipement, butin | **livree** |
| 3 | Familiers, jardinage, peche, duels entre joueurs (PvP) | a venir |

Les **apparences** (personnages humanoïdes, robe des joueurs) et les **textes surchargeables**
(dialogues, noms de PNJ, noms de sorts) sont livrés en plus des étapes 1 et 2.

---

## Licence

Mod prive, non publie, a usage personnel entre amis. Aucune ressource, texture, nom ou code de
Wizard101 n'est utilise : seules les **mecaniques** du jeu d'origine ont inspire ce projet.
Wizard101 est une marque de KingsIsle Entertainment, sans lien avec ce projet.
