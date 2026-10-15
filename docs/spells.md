# Catalogue des sorts et des creatures

> Ce fichier est **genere automatiquement** par `tools/generate-docs.py` a partir du code du moteur : les valeurs affichees sont exactement celles utilisees en jeu.

Les degats sont donnes pour un lanceur sans bonus (ni lame, ni piege, ni coup critique).
La colonne *pips* indique le cout en pips ; `X+n` signifie un sort a pips variables.

## Les sept ecoles

| Ecole | Nom (EN) | Precision de base | Degats par pip | Lame | Piege | PV (x base) |
| --- | --- | --- | --- | --- | --- | --- |
| Pyromancie | Pyromancy | 75 % | 100 | +35 % | +25 % | 0.95 |
| Tempete | Tempest | 70 % | 125 | +35 % | +25 % | 0.85 |
| Givre | Frost | 80 % | 85 | +35 % | +30 % | 1.25 |
| Runomancie | Runomancy | 80 % | 90 | +35 % | +25 % | 1.00 |
| Sylve | Sylvan | 90 % | 85 | +35 % | +25 % | 1.10 |
| Necromancie | Necromancy | 85 % | 85 | +35 % | +30 % | 1.00 |
| Equilibre | Equilibrium | 85 % | 85 | +25 % | +30 % | 1.05 |

## Pyromancie (Pyromancy)

14 sorts. Precision de base : 75 %, degats par pip : 100, lame : +35 %, piege : +25 %, PV : x0.95.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Chaton de Braise** | Ember Kitten | 1 | 75 % | 1 | Degats | une cible | 5 | Inflige 80-120 degats de Pyromancie. |
| **Feu Follet de Braise** | Cinder Wisp | 2 | 75 % | 2 | Degats sur la duree | une cible | 4 | Inflige 100-100 degats de Pyromancie. 70-70 degats de Pyromancie par round pendant 3 rounds. |
| **Marque du Bucher** | Pyre Mark | 0 | 100 % | 3 | Piege | une cible | 4 | Piege de +30% (Pyromancie) sur la cible (1 sort). |
| **Tranchant de Braise** | Emberedge | 0 | 100 % | 4 | Lame | soi-meme | 4 | Lame de +35% (Pyromancie) sur vos 1 prochains sorts offensifs. |
| **Faucon Solaire** | Sunhawk | 3 | 75 % | 6 | Degats | une cible | 4 | Inflige 280-340 degats de Pyromancie. |
| **Garde Glaciaire** | Glacial Ward | 0 | 100 % | 8 | Bouclier | soi-meme | 4 | Reduit de 70% les prochains degats (Givre, Tempete) subis (1 attaque). |
| **Invoquer un Braisillon** | Summon Cinderling | 3 | 100 % | 10 | Invocation | soi-meme | 4 | Invoque Braisillon pour combattre a vos cotes. |
| **Pluie de Cendres** | Ashfall | 4 | 75 % | 12 | Degats de zone | tous les ennemis | 3 | Inflige 265-345 degats de Pyromancie. |
| **Voile de Cendres** | Ashen Veil | 1 | 100 % | 14 | Malus de precision | tous les ennemis | 4 | Retire 40% de precision a la cible (1 sort). |
| **Brume Etouffante** | Smothering Haze | 2 | 100 % | 16 | Etourdissement | tous les ennemis | 4 | Etourdit la cible pendant 1 round(s). |
| **Drakeling** | Drakeling | 5 | 75 % | 18 | Degats sur la duree | une cible | 3 | Inflige 355-425 degats de Pyromancie. 110-110 degats de Pyromancie par round pendant 3 rounds. |
| **Attiser les Flammes** | Fan the Flames | 2 | 100 % | 20 | Piege | une cible | 4 | Piege de +30% (Pyromancie) sur la cible (3 sort). |
| **Wyrm des Enfers** | Inferno Wyrm | 6 | 75 % | 26 | Degats | une cible | 3 | Inflige 570-670 degats de Pyromancie. |
| **Frappe de Baguette Pyromancie** | Pyromancy Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Pyromancie. |

## Tempete (Tempest)

14 sorts. Precision de base : 70 %, degats par pip : 125, lame : +35 %, piege : +25 %, PV : x0.85.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Serpent d'Etincelles** | Spark Serpent | 1 | 70 % | 1 | Degats | une cible | 5 | Inflige 125-165 degats de Tempete. |
| **Chauves-souris de Bourrasque** | Gale Bats | 2 | 70 % | 4 | Degats | une cible | 4 | Inflige 245-285 degats de Tempete. |
| **Tranchant de Rafale** | Galeedge | 0 | 100 % | 5 | Lame | soi-meme | 4 | Lame de +35% (Tempete) sur vos 1 prochains sorts offensifs. |
| **Garde Thermique** | Thermic Ward | 0 | 100 % | 6 | Bouclier | soi-meme | 4 | Reduit de 70% les prochains degats (Pyromancie, Givre) subis (1 attaque). |
| **Marque de Tempete** | Tempest Mark | 0 | 100 % | 7 | Piege | une cible | 4 | Piege de +30% (Tempete) sur la cible (1 sort). |
| **Requin de Ressac** | Riptide Shark | 3 | 70 % | 10 | Degats | une cible | 4 | Inflige 375-435 degats de Tempete. |
| **Invoquer une Sentinelle des Flots** | Summon Wave Sentinel | 3 | 100 % | 12 | Invocation | soi-meme | 4 | Invoque Sentinelle des Flots pour combattre a vos cotes. |
| **Eclair Fracture** | Fractured Bolt | 2 | 70 % | 12 | Degats | une cible | 4 | Inflige soit 10, soit 100, soit 1000 degats de Tempete : coup de chance ! |
| **Deferlante Tempetueuse** | Tempest Surge | X+4-7 | 70 % | 14 | Degats de zone | tous les ennemis | 3 | Inflige 80 degats de Tempete a tous les ennemis par pip depense. |
| **Kraken Abyssal** | Abyssal Kraken | 4 | 70 % | 14 | Degats | une cible | 3 | Inflige 520-580 degats de Tempete. |
| **Vent Noir** | Darkwind | 3 | 100 % | 20 | UTILITY | soi-meme | 4 | Aura globale : +25% de degats ( (Tempete)) pendant 4 rounds. |
| **Heraut du Tonnerre** | Thunder Herald | 6 | 70 % | 22 | Degats | une cible | 3 | Inflige 690-770 degats de Tempete. |
| **Surcarge** | Supercharge | 2 | 100 % | 26 | UTILITY | soi-meme | 4 | Consomme tous vos pips : ajoute une lame Tempete de +5% par pip depense. |
| **Frappe de Baguette Tempete** | Tempest Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Tempete. |

## Givre (Frost)

14 sorts. Precision de base : 80 %, degats par pip : 85, lame : +35 %, piege : +30 %, PV : x1.25.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Scarabee de Givre** | Frost Beetle | 1 | 80 % | 1 | Degats | une cible | 5 | Inflige 65-105 degats de Givre. |
| **Tranchant de Givre** | Rimeedge | 0 | 100 % | 4 | Lame | soi-meme | 4 | Lame de +35% (Givre) sur vos 1 prochains sorts offensifs. |
| **Serpent des Neiges** | Snow Serpent | 2 | 80 % | 5 | Degats | une cible | 4 | Inflige 155-195 degats de Givre. |
| **Marque de Gel** | Frost Mark | 0 | 100 % | 6 | Piege | une cible | 4 | Piege de +30% (Givre) sur la cible (1 sort). |
| **Garde Volcanique** | Volcanic Ward | 0 | 100 % | 7 | Bouclier | soi-meme | 4 | Reduit de 70% les prochains degats (Pyromancie, Tempete) subis (1 attaque). |
| **Bouclier Protecteur** | Warding Shield | 0 | 100 % | 8 | Bouclier | soi-meme | 4 | Reduit de 50% les prochains degats subis, quelle que soit leur ecole. |
| **Egide Glaciaire** | Glacial Aegis | 2 | 100 % | 10 | Absorption | soi-meme | 4 | Absorbe jusqu'a 300 degats. |
| **Chaînes de Gel** | Rime Chains | 2 | 80 % | 12 | Etourdissement | une cible | 4 | Etourdit la cible pendant 1 round(s). |
| **Blizzard** | Blizzard | 4 | 80 % | 16 | Degats de zone | tous les ennemis | 3 | Inflige 205-265 degats de Givre. |
| **Morsure du Gel** | Frostbite | 4 | 80 % | 18 | Degats sur la duree | une cible | 3 | 90-90 degats de Givre par round pendant 4 rounds. |
| **Invoquer une Sentinelle de Givre** | Summon Rime Sentinel | 3 | 100 % | 18 | Invocation | soi-meme | 4 | Invoque Sentinelle de Givre pour combattre a vos cotes. |
| **Wyverne de Glace** | Ice Wyvern | 4 | 80 % | 22 | Degats | une cible | 3 | Inflige 335-395 degats de Givre. |
| **Colosse** | Colossus | 6 | 80 % | 26 | Degats | une cible | 3 | Inflige 545-635 degats de Givre. |
| **Frappe de Baguette Givre** | Frost Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Givre. |

## Runomancie (Runomancy)

14 sorts. Precision de base : 80 %, degats par pip : 90, lame : +35 %, piege : +25 %, PV : x1.00.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Chauve-souris Runique** | Rune Bat | 1 | 80 % | 1 | Degats | une cible | 5 | Inflige 70-110 degats de Runomancie. |
| **Troll de Pierre** | Stone Troll | 2 | 80 % | 5 | Degats | une cible | 4 | Inflige 170-210 degats de Runomancie. |
| **Tranchant Glyphique** | Glyph Edge | 0 | 100 % | 5 | Lame | soi-meme | 4 | Lame de +35% (Runomancie) sur vos 1 prochains sorts offensifs. |
| **Marque Runique** | Rune Mark | 0 | 100 % | 6 | Piege | une cible | 4 | Piege de +30% (Runomancie) sur la cible (1 sort). |
| **Invoquer un Serviteur Runique** | Summon Runestone Minion | 3 | 100 % | 8 | Invocation | soi-meme | 4 | Invoque Serviteur Runique pour combattre a vos cotes. |
| **Cyclope Runique** | Rune Cyclops | 3 | 80 % | 10 | Degats | une cible | 4 | Inflige 265-325 degats de Runomancie. |
| **Vigueur du Serviteur** | Minion Might | 0 | 100 % | 12 | Lame | un allie | 4 | Donne a un allie ou a un serviteur une lame de +40% de degats. |
| **Bouclier du Serviteur** | Minion Shield | 0 | 100 % | 12 | Bouclier | un allie | 4 | Donne a un allie ou a un serviteur un bouclier de -70% de degats. |
| **Idole Coassante** | Croaking Idol | 4 | 80 % | 16 | Degats de zone | tous les ennemis | 3 | Inflige 265-325 degats de Runomancie. |
| **Rune Lamentable** | Wailing Rune | 3 | 80 % | 20 | Degats | une cible | 3 | Inflige 240-300 degats de Runomancie. Reduit de 25% les degats infliges par la cible (1 sort). |
| **Fureur Runique** | Runic Fury | 3 | 100 % | 22 | UTILITY | soi-meme | 4 | Aura globale : +25% de degats ( (Runomancie)) pendant 4 rounds. |
| **Invoquer un Colosse Runique** | Summon Rune Colossus | 6 | 100 % | 26 | Invocation | soi-meme | 4 | Invoque Colosse Runique pour combattre a vos cotes. |
| **Regard Petrifiant** | Petrifying Gaze | 5 | 80 % | 28 | Degats | une cible | 3 | Inflige 480-560 degats de Runomancie. Etourdit la cible pendant 1 round(s). |
| **Frappe de Baguette Runomancie** | Runomancy Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Runomancie. |

## Sylve (Sylvan)

15 sorts. Precision de base : 90 %, degats par pip : 85, lame : +35 %, piege : +25 %, PV : x1.10.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Lutin Germe** | Sprout Imp | 1 | 90 % | 1 | Degats | une cible | 5 | Inflige 65-105 degats de Sylve. |
| **Benediction Mineure** | Minor Blessing | 0 | 100 % | 2 | Soin | soi-meme | 4 | Soigne 65-65 points de vie. |
| **Trèfle Vivant** | Cloverkin | 2 | 90 % | 5 | Degats | une cible | 4 | Inflige 155-195 degats de Sylve. |
| **Tranchant Verdoyant** | Verdant Edge | 0 | 100 % | 5 | Lame | soi-meme | 4 | Lame de +35% (Sylve) sur vos 1 prochains sorts offensifs. |
| **Marque d'Epines** | Thorn Mark | 0 | 100 % | 6 | Piege | une cible | 4 | Piege de +30% (Sylve) sur la cible (1 sort). |
| **Don de la Fee** | Faerie Gift | 2 | 100 % | 8 | Soin | un allie | 4 | Soigne 420-420 points de vie. |
| **Garde Legendarie** | Legend Ward | 0 | 100 % | 8 | Bouclier | soi-meme | 4 | Reduit de 70% les prochains degats (Necromancie, Runomancie) subis (1 attaque). |
| **Floraison de Lutins** | Sprite Bloom | 1 | 100 % | 10 | Soin sur la duree | un allie | 4 | Soigne 30-30 points de vie. Soigne 90-90 points de vie par round pendant 3 rounds. |
| **Benediction de la Licorne** | Unicorn Blessing | 3 | 100 % | 12 | Soin de zone | tous les allies | 3 | Soigne 275-275 points de vie. |
| **Lumiere Guide** | Guiding Light | 1 | 100 % | 20 | UTILITY | soi-meme | 4 | Ajoute +30% de soins sur votre prochain sort de soin. |
| **Seraphin Radieux** | Radiant Seraph | 4 | 90 % | 22 | Degats | une cible | 3 | Inflige 335-395 degats de Sylve. |
| **Sanctuaire** | Sanctuary | 4 | 100 % | 24 | UTILITY | soi-meme | 4 | Aura globale : +40% de soins pendant 4 rounds. |
| **Renouveau** | Renewal | 5 | 100 % | 26 | Soin sur la duree | un allie | 3 | Soigne 300-300 points de vie. Soigne 150-150 points de vie par round pendant 3 rounds. |
| **Charge du Centaure** | Centaur Charge | 6 | 90 % | 28 | Degats | une cible | 3 | Inflige 500-580 degats de Sylve. |
| **Frappe de Baguette Sylve** | Sylvan Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Sylve. |

## Necromancie (Necromancy)

14 sorts. Precision de base : 85 %, degats par pip : 85, lame : +35 %, piege : +30 %, PV : x1.00.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Spectre Follet** | Wisp Wraith | 1 | 85 % | 1 | Degats | une cible | 5 | Inflige 65-105 degats de Necromancie. |
| **Goule** | Ghoul | 2 | 85 % | 5 | Drain | une cible | 4 | Inflige 160-160 degats de Necromancie et vous soigne de 50% des degats infliges. |
| **Tranchant Sepulcral** | Grave Edge | 0 | 100 % | 5 | Lame | soi-meme | 4 | Lame de +35% (Necromancie) sur vos 1 prochains sorts offensifs. |
| **Marque Funebre** | Grave Mark | 0 | 100 % | 6 | Piege | une cible | 4 | Piege de +30% (Necromancie) sur la cible (1 sort). |
| **Garde Onirique** | Dream Ward | 0 | 100 % | 8 | Bouclier | soi-meme | 4 | Reduit de 70% les prochains degats (Sylve, Runomancie) subis (1 attaque). |
| **Corsaire d'Os** | Bone Corsair | 3 | 85 % | 10 | Degats | une cible | 4 | Inflige 275-345 degats de Necromancie. |
| **Papillon de Sang** | Bloodmoth | 4 | 85 % | 16 | Drain | une cible | 3 | Inflige 300-360 degats de Necromancie et vous soigne de 50% des degats infliges. |
| **Peste** | Plague | 2 | 100 % | 18 | Faiblesse | tous les ennemis | 4 | Reduit de 20% les degats infliges par la cible (1 sort). |
| **Spectre** | Wraith | 5 | 85 % | 20 | Drain | une cible | 3 | Inflige 400-470 degats de Necromancie et vous soigne de 50% des degats infliges. |
| **Offrande de Sang** | Blood Offering | 3 | 100 % | 22 | Soin | un allie | 4 | Sacrifie 250 de vos points de vie pour soigner un allie de 500. |
| **Moisson d'Os** | Harvest of Bones | 4 | 85 % | 24 | Drain | tous les ennemis | 3 | Inflige 250-310 degats de Necromancie a tous les ennemis et vous soigne de la moitie des degats infliges. |
| **Feinte** | Feint | 0 | 100 % | 26 | Piege | une cible | 2 | Pose un piege de +70% sur l'ennemi et un piege de +30% sur vous-meme. Un classique a double tranchant ! |
| **Ruine et Tenebres** | Doom and Gloom | 3 | 100 % | 30 | UTILITY | soi-meme | 4 | Aura globale : -25% de soins pour tout le monde pendant 4 rounds. |
| **Frappe de Baguette Necromancie** | Necromancy Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Necromancie. |

## Equilibre (Equilibrium)

27 sorts. Precision de base : 85 %, degats par pip : 85, lame : +25 %, piege : +30 %, PV : x1.05.

| Sort (FR) | Sort (EN) | Pips | Precision | Niv. | Type | Cible | Copies | Effet |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Scarabee** | Scarab | 1 | 85 % | 1 | Degats | une cible | 5 | Inflige 65-105 degats de Equilibre. |
| **Scorpion** | Scorpion | 2 | 85 % | 5 | Degats | une cible | 4 | Inflige 160-200 degats de Equilibre. |
| **Tranchant d'Equilibre** | Equilibrium Edge | 0 | 100 % | 5 | Lame | soi-meme | 4 | Ajoute une lame de +25% a votre prochain sort, quelle que soit son ecole. |
| **Faiblesse** | Weakness | 0 | 100 % | 8 | Faiblesse | une cible | 4 | Reduit de 25% les degats infliges par la cible (1 sort). |
| **Malefice** | Hex | 1 | 100 % | 16 | Piege | une cible | 4 | Pose un piege de +30% sur la cible, quelle que soit l'ecole du prochain sort. |
| **Tempete de Sable** | Sandstorm | 4 | 85 % | 16 | Degats de zone | tous les ennemis | 3 | Inflige 240-300 degats de Equilibre. |
| **Tranchant Elementaire** | Elemental Edge | 0 | 100 % | 18 | Lame | soi-meme | 4 | Ajoute une lame de +25% pour le prochain sort de Pyromancie, Tempete ou Givre. |
| **Tranchant Spirituel** | Spirit Edge | 0 | 100 % | 18 | Lame | soi-meme | 4 | Ajoute une lame de +25% pour le prochain sort de Sylve, Necromancie ou Runomancie. |
| **Mains Secourables** | Helping Hands | 3 | 100 % | 20 | Soin | un allie | 3 | Soigne 400-400 points de vie. |
| **Jugement** | Judgement | X+1-7 | 85 % | 22 | Degats | une cible | 3 | Inflige 100 degats d'Equilibre par pip depense. |
| **Decharge Spectrale** | Spectral Blast | 3 | 85 % | 24 | Degats | une cible | 3 | Inflige 300-400 degats d'une ecole elementaire aleatoire (Pyromancie, Tempete ou Givre). |
| **Rebattre** | Reshuffle | 4 | 100 % | 24 | Deck | soi-meme | 4 | Remelange votre defausse dans votre deck et pioche une nouvelle main. |
| **Aura d'Equilibre** | Equilibrium Aura | 3 | 100 % | 28 | UTILITY | soi-meme | 4 | Aura globale : +20% de degats (toutes les ecoles) pendant 4 rounds. |
| **Hydre** | Hydra | 6 | 85 % | 30 | Degats | une cible | 2 | Frappe trois fois : 130-150 degats de Pyromancie, de Givre et de Tempete. |
| **Garde de Braise** | Ember Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Pyromancie. |
| **Garde de Tempete** | Tempest Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Tempete. |
| **Garde de Givre** | Frost Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Givre. |
| **Garde Runique** | Rune Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Runomancie. |
| **Garde Verdoyante** | Verdant Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Sylve. |
| **Garde Sepulcrale** | Grave Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Necromancie. |
| **Garde Karmique** | Karmic Ward | 0 | 100 % | 12 | Bouclier | soi-meme | 4 | Reduit de 80% les prochains degats de Equilibre. |
| **Purifier le Sort** | Cleanse Charm | 0 | 100 % | 10 | UTILITY | soi-meme | 4 | Retire un charme nefaste (Faiblesse, malus de precision) qui vous affecte. |
| **Desarmer** | Disarm | 1 | 100 % | 16 | UTILITY | une cible | 4 | Retire une lame de la cible. |
| **Voler le Sort** | Steal Charm | 1 | 100 % | 20 | UTILITY | une cible | 4 | Vole une lame de la cible et la garde pour vous. |
| **Triage** | Triage | 1 | 100 % | 22 | UTILITY | un allie | 4 | Retire tous les effets de degats sur la duree qui affectent un allie. |
| **Egide Perforante** | Piercing Aegis | 2 | 100 % | 26 | UTILITY | soi-meme | 4 | Votre prochain sort ignore 20% de la resistance de la cible. |
| **Frappe de Baguette Equilibre** | Equilibrium Wand Strike | 0 | 100 % | 1 | Baguette | une cible | 4 | Inflige 40-60 degats de Equilibre. |

## Creatures

| Creature (FR) | Creature (EN) | Ecole | Niveau | PV | Entite Minecraft | Critique | Pips de puissance | Cartes | Type | Resistances |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Braisillon** | Cinderling | Pyromancie | 4 | 250 | cinderling | 0 | 25 % | 5 | normal | aucune |
| **Sentinelle des Flots** | Wave Sentinel | Tempete | 5 | 220 | wave_sentinel | 0 | 25 % | 5 | normal | aucune |
| **Sentinelle de Givre** | Rime Sentinel | Givre | 6 | 420 | rime_sentinel | 0 | 25 % | 6 | normal | aucune |
| **Serviteur Runique** | Runestone Minion | Runomancie | 5 | 300 | runestone_minion | 0 | 25 % | 6 | normal | aucune |
| **Colosse Runique** | Rune Colossus | Runomancie | 12 | 700 | rune_colossus | 0 | 25 % | 7 | normal | aucune |
| **Diablotin de Braise** | Ember Imp | Pyromancie | 4 | 350 | ember_imp | 0 | 25 % | 6 | normal | aucune |
| **Follet Statique** | Static Wisp | Tempete | 6 | 320 | static_wisp | 0 | 40 % | 6 | normal | aucune |
| **Serviteur des Ronces** | Bramble Thrall | Sylve | 5 | 380 | bramble_thrall | 0 | 25 % | 6 | normal | aucune |
| **Gnome Runique** | Rune Gnome | Runomancie | 6 | 400 | rune_gnome | 0 | 30 % | 6 | normal | aucune |
| **Ombre Sepulcrale** | Grave Shade | Necromancie | 7 | 420 | grave_shade | 0 | 30 % | 6 | normal | aucune |
| **Molosse des Glaciers** | Glacier Hound | Givre | 8 | 520 | glacier_hound | 0 | 30 % | 7 | normal | frost:5 |
| **Scribe de l'Equilibre** | Balance Scribe | Equilibre | 9 | 450 | balance_scribe | 0 | 30 % | 7 | normal | aucune |
| **Spectre de Givre** | Frost Wraith | Givre | 12 | 700 | frost_wraith | 5 | 40 % | 7 | normal | frost:10 |
| **Tyran des Braises** | Cinder Tyrant | Pyromancie | 15 | 1500 | cinder_tyrant | 20 | 50 % | 8 | boss | pyromancy:15 |
| **Matriarche de Givre** | Frost Matriarch | Givre | 20 | 2200 | frost_matriarch | 20 | 60 % | 9 | boss | frost:25 |

Total : 112 sorts et 15 creatures.
