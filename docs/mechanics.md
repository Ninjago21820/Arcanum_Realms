# Mecaniques de duel

Ce document decrit **exactement** ce que fait le moteur de duel d'Arcanum Realms.
Toutes les valeurs citees sont celles du code (`src/main/java/com/arcanumrealms/core`) et sont
regroupables via `config/arcanum_realms-common.toml`.

Le catalogue complet des sorts et des creatures (noms, pips, degats, precisions) se trouve
dans [spells.md](spells.md), genere automatiquement a partir du moteur.

---

## 1. Declencher un duel

| Moyen | Comment |
| --- | --- |
| Creature errante | Clic droit avec la **Baguette de Duel** sur une creature, ou approche automatique (rayon 12 blocs, une tentative toutes les 20 s) |
| Cercle arcanique | Clic droit sur le bloc **Cercle Arcanique** : fait apparaitre une rencontre complete et lance le duel |
| Commande | `/arcanum duel` (creatures proches) et `/arcanum arena` (rencontre a votre position) |

Un duel peut opposer jusqu'a **4 sorciers** (joueurs proches, rayon 12 blocs) a un groupe de
**1 a 4 creatures** (`max_creatures`, `max_players`). Les joueurs proches sont invites
automatiquement, comme dans un duel de groupe.

Regles de securite :

- un joueur deja en duel ne peut pas en lancer un autre ;
- `/arcanum noauto true` desactive les duels automatiques pour un joueur ;
- quitter un duel se fait avec le bouton **Fuir**, `/arcanum leave`, ou en s'eloignant de plus de
  **32 blocs** du centre (`leash_radius`) ;
- les joueurs sont **invulnerables** aux degats du monde pendant un duel (`invulnerable_during_duel`),
  et leurs PV sont restaures selon le reglage `restore_health_after_duel` (les degats de defaite
  sont appliques avant, voir section 9).

---

## 2. Deroulement d'un round

```
Round n
  +1 pip pour chaque participant vivant (pip de puissance selon la chance)
  Chaque main est completee a 7 cartes
  PHASE DE PLANIFICATION (60 s)  -> joueurs et creatures choisissent en secret
  PHASE DE RESOLUTION (~2 s)     -> les sorts partent dans l'ordre de tour
  Fin de round (DoT / HoT / durees / auras / serviteurs)
```

- La **planification** se termine des que tout le monde a choisi, ou au bout de
  `planning_seconds` secondes (defaut 60) : les retardataires passent leur tour.
- La **resolution** est une simple pause de `resolve_delay_ticks` (defaut 40 = 2 s) pendant
  laquelle le journal de combat defile.
- Ordre de tour : **ALTERNATING** (joueur 1, creature 1, joueur 2, creature 2...). C'est l'ordre
  utilise par Wizard101 et cela evite qu'une equipe entiere frappe avant l'autre.
- Un duel s'arrete au plus tard apres `max_rounds` (defaut 60) : l'equipe avec le meilleur
  pourcentage de PV restants gagne.

---

## 3. Pips

Les pips sont la ressource des sorts.

| Regle | Valeur |
| --- | --- |
| Emplacements de pips | **7** |
| Gain par round | **1 pip** par participant vivant |
| Pip ordinaire | vaut 1 |
| Pip de puissance | vaut **2**, uniquement pour les sorts **de votre ecole** (ou si vous possedez la maitrise) |
| Pip de puissance partiel | impossible : un pip de puissance ne peut pas payer 1 seul pip |
| Paiement | toujours au **moindre gaspillage** : le moteur utilise d'abord les pips de puissance inutiles, puis les pips ordinaires |
| Chance de pip de puissance | `power_pip_chance` (25 % par defaut, comme un sorcier sans equipement) |

Exemples de paiement pour un sort a 4 pips :

| Pips en reserve | Cout reel |
| --- | --- |
| 4 pips ordinaires | 4/4 (tous consommes) |
| 2 pips de puissance | 2/4 (chaque pip vaut 2) |
| 1 pip de puissance + 3 ordinaires | 4/4 (1 puissance + 2 ordinaires) |
| 2 pips de puissance + 1 ordinaire | 4/4 (2 puissance) |

Sorts a **pips variables** (`X`) : vous choisissez le nombre de pips a investir (molette de la
souris sur la carte, entre le minimum et le maximum du sort). Les pips de puissance depenses
comptent pour 2, donc `Tempete montante` (X pips, 80 degats par pip) peut exploser avec 6 pips.

---

## 4. Cartes, main et deck

| Regle | Valeur |
| --- | --- |
| Main | **7 cartes** en permanence |
| Pioche | **1 carte par round** (recompletee automatiquement au debut du round) |
| Deck minimum | **1 carte** (le moteur refuse un deck vide) |
| Deck maximum | `deck_size_limit`, defaut **40 cartes** |
| Defausse | clic droit sur une carte pendant la planification |
| Pioche vide | melange automatique de la defausse dans le deck (**reshuffle**) |
| Sort `Remelange` | melange votre defausse et vous redonne une main complete |

Le deck est construit dans le **grimoire** (touche `G`, clic droit avec la baguette, ou
`/arcanum grimoire`). Il est stocke cote serveur dans les donnees du joueur : un deck survit
a la deconnexion et ne peut pas etre modifie par un client non autorise.

Repartition des cartes : chaque sort possede un nombre maximum de copies (`max_copies`, 3 a 5
selon le sort) : impossible de mettre 40 fois le meme sort.

---

## 5. Ordre de calcul des degats

Un coup qui touche passe par la chaine suivante, dans cet ordre exact :

```
1.  Base du sort                 (ex. 265-345)
2.  Bonus d'ecole                (+X % de degats de cette ecole)
3.  Aura globale                 (+X % au niveau du duel)
4.  Lames du lanceur             (+35 % par lame active)
5.  Faiblesse sur le lanceur     (-25 %)
6.  Pieges sur la cible          (+30 % par piege actif)
7.  damage_scale                 (reglage global, 1.0 par defaut)
8.  Coup critique                (x1.0 a x2.0 selon le score critique)
9.  Percee du lanceur            (ignore X % de resistance)
10. Resistance de la cible
11. Boucliers de la cible        (-X % de degats)
12. Absorption                   (reserve de degats qui encaisse a votre place)
13. Resistance plate             (valeurs fixes de degats retires)
```

Les etats **consommables** (lames, pieges, faiblesses, boucliers, charmes de precision) ne sont
consommes que lorsqu'un sort **touche** reellement : un fizzle ne gaspille donc aucune lame ni
aucun piege, et il ne consomme pas non plus les faiblesses du lanceur.

Ordre de tour et ciblage :

- la cible est choisie **par le joueur** : cliquez la carte, puis la creature ou l'allie vise
  (les sorts offensifs refusent un allie, les soins refusent un ennemi) ;
- si aucune cible n'est fournie (sort de zone, invocation, IA distraite), le moteur prend la
  premiere cible vivante du camp concerne : camp adverse pour une attaque, votre camp pour un
  soin ou un bonus ;
- les serviteurs agissent dans l'ordre de tour, juste apres leur maitre.

---

## 6. Precision et fizzle

Un sort qui rate (fizzle) ne produit **aucun effet** : la carte et les pips sont perdus, comme
dans Wizard101. En revanche les lames, pieges et charmes ne sont **pas** consommes : ils restent
en place pour le sort suivant.

```
precision finale = precision du sort                        (70 a 100 % selon l'ecole)
                 + precision de l'equipement du lanceur
                 + charmes de precision / malus de precision
                 - malus eventuels de la creature
```

| Ecole | Precision de base |
| --- | --- |
| Tempete | 70 % |
| Pyromancie | 75 % |
| Givre | 80 % |
| Runomancie | 80 % |
| Necromancie | 85 % |
| Equilibre | 85 % |
| Sylve | 90 % |

La baguette (`wand_<ecole>`, 0 pip, 40-60 degats) a **100 %** de precision : elle sert de filet
de securite quand un sort rate.

---

## 7. Coups critiques

Deux modes, choisis par `critical_enabled` :

**Mode RATING (par defaut, formule de Wizard101)**

```
l = min(niveau / 100, 1)
chance  = 3 * l * c / (3 * c + b)
blocage = la cible bloque si elle reussit son propre jet (b = sa resistance critique)
degats  = x (2.0 * (1 - 1.5 * b / (c + 3 * b)))      borne a [1.0 ; 2.0]
```

avec `c` = score critique du lanceur, `b` = score de blocage de la cible.

Exemples (niveau 50, donc `l = 0.5`) :

| Critique | Blocage cible | Chance de critique | Multiplicateur |
| --- | --- | --- | --- |
| 100 | 0 | 50 % | x2.00 |
| 300 | 100 | 45 % | x1.80 |
| 300 | 300 | 30 % | x1.50 |
| 100 | 300 | 12 % | x1.33 |

**Mode FLAT** : chance fixe (`flat_crit_chance`), utile pour tester.

Le score critique d'un sorcier vaut `niveau x crit_rating_per_level` (2 par niveau) tant qu'il
n'est pas personnalise (`/arcanum crit <score>`).

---

## 8. Etats (lames, pieges, boucliers...)

| Etat | Effet | Duree |
| --- | --- | --- |
| **Lame** (blade) | +35 % sur vos prochains sorts offensifs (Equilibre : +25 % pour tous les degats) | 1 sort, ou 3 pour les lames de zone |
| **Piege** (trap) | +30 % sur les prochains sorts offensifs subis (Equilibre : +25 %) | 1 sort (3 pour *Attiser les flammes*) |
| **Bouclier** (shield) | -X % sur les degats subis | 1 attaque |
| **Faiblesse** (weakness) | -25 % sur les prochains sorts offensifs du porteur | 1 sort |
| **Charme de precision** | +/- X % de precision | 1 sort |
| **Lame de soin** | +X % sur vos prochains soins | 1 sort |
| **Percee** | ignore X % de resistance | 1 sort |
| **Absorption** | reserve de degats qui encaisse avant les PV | jusqu'a epuisement |
| **DoT** | X degats par round | N rounds |
| **HoT** | X PV rendus par round | N rounds |
| **Etourdissement** | le porteur passe son tour | 1 round (se brise s'il est frappe) |

Un sort comme *Feinte* pose un piege de **+70 %** sur l'ennemi **et** un piege de **+30 %** sur
vous-meme : c'est le meme mecanisme a double tranchant que dans Wizard101.

---

## 9. Fin du duel, defaites et recompenses

- Un duel se termine quand un camp n'a plus aucun participant vivant.
- Les joueurs **vaincus** perdent `defeat_damage / 2` coeurs (3 par defaut) et sortent du duel ;
  leurs PV sont restaures au maximum s'ils ne sont pas tombes a 0.
- Duel de groupe : tant qu'un allie est debout, les vaincus attendent la fin du duel.
- Les creatures vaincues disparaissent du monde (pas de butin, pas d'XP Minecraft : toute la
  progression passe par l'XP du mod).
- **Victoire** : chacun gagne `xp_per_creature_level x niveau de la creature` (10 x niveau).
- **Niveau** : il faut `xp_to_level_up x niveau courant` XP pour passer au niveau suivant
  (100 XP au niveau 1, 200 au niveau 2, ...).
- Chaque niveau appris automatiquement les sorts debloques (`auto_learn_spells`), et chaque
  niveau fait evoluer les PV (`400 + 45 x niveau`, module par l'ecole) ainsi que le score critique.
- Les PV maximum d'un sorcier suivent la table des ecoles : Givre x1.25, Sylve x1.10,
  Equilibre x1.05, Runomancie/Necromancie x1.00, Pyromancie x0.95, Tempete x0.85.

---

## 10. Intelligence des creatures

Chaque creature possede son propre deck et un comportement (`CAREFUL`, `NORMAL`, `AGGRESSIVE`) :

1. elle note chaque carte jouable : degats attendus, soins rendus, lame deja active, nombre
   d'ennemis restants, presence d'un serviteur... ;
2. les comportements modulent la note : une creature `CAREFUL` privilegie les soutiens et les
   gros sorts (x0.8 sur les attaques), une creature `AGGRESSIVE` frappe des que possible
   (x1.25 sur les attaques) ;
3. elle vise **l'adversaire le plus bas en PV** pour ses attaques, **l'allie le plus blesse**
   (en pourcentage) pour ses soins, et elle-meme pour ses lames et boucliers ;
4. si aucune carte jouable ne vaut le cout, elle passe son tour et garde ses pips.

Les boss (Tyran des Braises, Matriarche de Givre) ont un deck plus large, plus de pips de
puissance et plus de PV.

---

## 11. Reglages (`config/arcanum_realms-common.toml`)

| Section | Cle | Defaut | Role |
| --- | --- | --- | --- |
| duel_trigger | auto_trigger | true | duels automatiques avec les creatures errantes |
| duel_trigger | trigger_radius | 12 | rayon de declenchement |
| duel_trigger | trigger_cooldown_seconds | 20 | delai entre deux declenchements |
| duel_trigger | max_creatures / max_players | 4 / 4 | taille des groupes |
| duel_rules | fizzle_enabled | true | active les rates |
| duel_rules | critical_enabled | true | active les critiques |
| duel_rules | damage_scale / heal_scale | 1.0 / 1.0 | equilibrage global |
| duel_rules | planning_seconds | 60 | duree de la phase de choix |
| duel_rules | resolve_delay_ticks | 40 | pause de resolution |
| duel_rules | allow_flee | true | autorise la fuite |
| duel_rules | max_rounds | 60 | limite de securite |
| duel_rules | leash_radius | 32 | distance maximale avant abandon |
| wizard | base_power_pip_chance | 25 | chance de pip de puissance |
| wizard | crit_rating_per_level | 2 | critique par niveau |
| wizard | accuracy_bonus / base_resist / base_damage_bonus | 0 | bonus d'equipement par defaut |
| wizard | deck_size_limit | 40 | taille maximale du deck |
| wizard | xp_per_creature_level | 10 | XP gagnee par niveau de creature |
| wizard | xp_to_level_up | 100 | cout du niveau suivant |
| wizard | auto_learn_spells | true | apprentissage automatique |
| wizard | invulnerable_during_duel | true | protection contre les degats du monde |
| wizard | restore_health_after_duel | true | soins apres le duel |
| wizard | defeat_damage | 6.0 | degats (en demi-coeurs) apres une defaite |
| arena | encounters | liste | rencontres du Cercle Arcanique |

---

## 12. Ecart assume avec Wizard101

Le mod reprend les **mecaniques et les nombres** du jeu d'origine, mais pas ses noms : les
ecoles, sorts et creatures sont des creations originales pour rester dans un usage prive et
respecter les marques de KingsIsle. Quelques choix deliberes :

- le pas de temps est celui de Minecraft : un round = un cycle de planification de 60 s, pas un
  tour en temps reel ;
- aucun equipement a statistiques pour l'instant : `base_resist`, `accuracy_bonus`,
  `base_damage_bonus` existent dans la config et seront pilotes par du stuff (etape suivante) ;
- les degats sur la duree (DoT) touchent a la fin du round ou ils sont poses, ce qui peut donner
  un round d'avance par rapport au jeu d'origine.
