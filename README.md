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

Les etapes 2 (monde, quetes, PNJ, dialogue) et 3 (familiers, jardinage, peche, PvP) sont
planifiees : voir la [feuille de route](#feuille-de-route).

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

## Documentation

- [docs/mechanics.md](docs/mechanics.md) : toutes les mecaniques de duel (pips, main, ordre de
  calcul des degats, fizzle, critiques, etats, IA, XP, reglages).
- [docs/spells.md](docs/spells.md) : catalogue complet des 112 sorts et des 15 creatures,
  **genere automatiquement** depuis le moteur de jeu.

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
python3 tools/generate-docs.py /chemin/jdk17     # docs/spells.md
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
  network/       paquets Forge (etat du duel, actions du joueur, fiche du sorcier)
  command/       commandes /arcanum
  client/        interface de duel, grimoire, HUD, touches
  item/ block/   baguette et cercle arcanique
  player/        fiche du sorcier persistante (NBT)
  battle/        ServerBattleManager : orchestration des duels cote serveur
src/main/resources/   mods.toml, traductions, modeles, textures, recettes
src/test/java/        27 tests du moteur
```

Le moteur ne connait pas Minecraft : le serveur lui parle par l'interface `BattleHost`
(evenements de combat) et le client ne fait qu'**afficher** l'etat envoye par le serveur, ce qui
evite toute desynchronisation.

---

## Feuille de route

| Etape | Contenu | Etat |
| --- | --- | --- |
| 1 | Combat complet : ecoles, sorts, deck, pips, duels, IA, XP, interface | **livree** |
| 2 | Monde : zones de quete, PNJ et dialogues, quetes de type "vaincs X creatures", recompenses, equipement a statistiques | a venir |
| 3 | Familiers, jardinage, peche, duels entre joueurs (PvP) | a venir |

---

## Licence

Mod prive, non publie, a usage personnel entre amis. Aucune ressource, texture, nom ou code de
Wizard101 n'est utilise : seules les **mecaniques** du jeu d'origine ont inspire ce projet.
Wizard101 est une marque de KingsIsle Entertainment, sans lien avec ce projet.
