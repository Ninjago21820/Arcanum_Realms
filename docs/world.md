# Contenu du monde

> Fichier **genere automatiquement** par `tools/generate-world-docs.py` a partir du moteur : les noms, objectifs et valeurs listes ici sont exactement ceux utilises en jeu.

Le contenu suit les fiches de la wiki Wizard101 Central (statistiques, rangs, resistances, sorts, butin, enchainement des quetes), avec des noms originaux.

## Sommaire

- 7 zones
- 35 creatures
- 19 PNJ
- 50 quetes
- 47 pieces d'equipement
- 10 tables de butin

## Zones

| Zone | Nom (EN) | Niveaux | Creatures | PNJ | Quetes | Ouverture | Type |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **La Grand-Place** (`grand_place`) | The Great Square | 1-5 | 0 | 3 | 2 | - | exterieur |
| **Le Bosquet des Sept** (`bosquet`) | The Sevenfold Grove | 1-20 | 0 | 7 | 7 | - | exterieur |
| **Le Sentier des Lucioles** (`sentier_lucioles`) | Firefly Way | 1-6 | 6 | 2 | 8 | - | exterieur |
| **L'Allee des Braises** (`allee_braises`) | Ember Alley | 5-14 | 10 | 3 | 17 | `report_to_hanna` (nv 5) | exterieur |
| **L'Avenue des Marees** (`avenue_marees`) | Tide Avenue | 12-18 | 12 | 2 | 7 | `swiftarrow_ambush` (nv 12) | exterieur |
| **Le Chemin des Colosses** (`chemin_colosses`) | Colossus Road | 15-19 | 5 | 1 | 6 | `the_harvest_lord_quest` (nv 15) | exterieur |
| **La Grotte Hantee** (`grotte_hantee`) | Haunted Hollow | 17-22 | 4 | 1 | 4 | `ashes_of_the_road` (nv 17) | donjon |

### La Grand-Place (The Great Square)

Le coeur de la cite : la fontaine aux pips, le bureau du directeur et les marchands.

### Le Bosquet des Sept (The Sevenfold Grove)

Sept clairieres, une par ecole : c'est ici que les professeurs enseignent leurs sorts.

### Le Sentier des Lucioles (Firefly Way)

Un chemin de campagne ou les premiers esprits se sont mis a roder.

### L'Allee des Braises (Ember Alley)

Des elfes de feu ont investi les ruelles ; leurs flammes ont fait fondre les ponts.

### L'Avenue des Marees (Tide Avenue)

Le vieux port est sous l'eau, et ce qui s'y est noye n'a pas l'intention de rester tranquille.

### Le Chemin des Colosses (Colossus Road)

La grande avenue des statues : leurs gardiens de glace se sont reveilles.

### La Grotte Hantee (Haunted Hollow)

Un boyau de pierre noire ou le Grand Necromancien rassemble ses os.

## Bestiaire

| Creature | Nom (EN) | Ecole | Rang | Niv. | PV | Pips | Classe | Zone(s) | Entite | Cartes | Butin | Type |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Ame Perdue** (`lost_soul`) | Lost Soul | Necromancie | 1 | 2 | 55 | 0 | Spirit | sentier_lucioles | `skeleton` | 5 | rank1 | normal |
| **Serviteur Hante** (`haunted_minion`) | Haunted Minion | Necromancie | 1 | 3 | 90 | 1 | Spirit | sentier_lucioles, allee_braises, avenue_marees | `zombie` | 5 | rank1 | normal |
| **Guerrier Squelettique** (`skeletal_warrior`) | Skeletal Warrior | Necromancie | 2 | 4 | 115 | 1 | Undead | sentier_lucioles, allee_braises | `skeleton` | 6 | rank2 | normal |
| **Lutin Ennuyeux** (`pesky_pixie`) | Pesky Pixie | Tempete | 1 | 2 | 65 | 0 | Fairy | sentier_lucioles | `silverfish` | 5 | rank1 | normal |
| **Lutin des Ronces** (`thorn_sprite`) | Thorn Sprite | Sylve | 1 | 3 | 60 | 0 | Fairy | sentier_lucioles | `cave_spider` | 5 | rank1 | normal |
| **Essaim de Scarabees** (`scarab_swarm`) | Scarab Swarm | Equilibre | 1 | 3 | 70 | 0 | Insect | sentier_lucioles | `endermite` | 5 | rank1 | normal |
| **Chasseur Elfe de Braise** (`ember_elf_hunter`) | Ember Elf Hunter | Pyromancie | 2 | 9 | 150 | 1 | Elf | allee_braises | `pillager` | 7 | rank2 | normal |
| **Eclaireur Elfe de Braise** (`ember_elf_pathfinder`) | Ember Elf Pathfinder | Pyromancie | 2 | 8 | 135 | 1 | Elf | allee_braises | `vindicator` | 5 | rank2 | normal |
| **Homme de Magma** (`magma_man`) | Magma Man | Pyromancie | 3 | 11 | 210 | 2 | Elemental | allee_braises | `magma_cube` | 6 | rank3 | normal |
| **Gretta Chaudron-Noir** (`gretta_darkkettle`) | Gretta Darkkettle | Pyromancie | 3 | 10 | 300 | 2 | Witch | allee_braises | `witch` | 6 | rank3 | normal |
| **Vermigut** (`wormguts`) | Wormguts | Runomancie | 3 | 10 | 340 | 2 | Gobbler | allee_braises | `hoglin` | 6 | boss_alley | boss |
| **Melweena Chatiment** (`melweena_smite`) | Melweena Smite | Necromancie | 3 | 10 | 320 | 2 | Undead | allee_braises | `witch` | 6 | boss_alley | boss |
| **Sepulcra Ombrevent** (`bastilla_gravewynd`) | Sepulcra Gravewynd | Necromancie | 4 | 12 | 415 | 3 | Undead | allee_braises | `wither_skeleton` | 8 | boss_alley | boss |
| **Alcane Vive-Fleche** (`alicane_swiftarrow`) | Alcane Swiftarrow | Pyromancie | 4 | 12 | 400 | 3 | Elf | allee_braises | `pillager` | 8 | boss_alley | boss |
| **Chair Pourrissante** (`rotting_fodder`) | Rotting Fodder | Necromancie | 2 | 6 | 155 | 1 | Undead | avenue_marees | `zombie` | 5 | rank2 | normal |
| **Marcheur des Cryptes** (`crypt_walker`) | Crypt Walker | Necromancie | 2 | 7 | 175 | 1 | Undead | avenue_marees | `husk` | 5 | rank2 | normal |
| **Rampant Infect** (`loathsome_creeper`) | Loathsome Creeper | Necromancie | 2 | 7 | 165 | 1 | Undead | avenue_marees | `cave_spider` | 5 | rank2 | normal |
| **Rob le Pourri** (`rotten_rob`) | Rotten Rob | Necromancie | 3 | 8 | 240 | 2 | Undead | avenue_marees | `zombie` | 6 | rank3 | normal |
| **Anguille Electrique** (`electric_eel`) | Electric Eel | Tempete | 2 | 7 | 145 | 1 | Sea Creature | avenue_marees | `drowned` | 5 | rank2 | normal |
| **Hurleur Ecarlate** (`scarlet_screamer`) | Scarlet Screamer | Tempete | 3 | 8 | 205 | 2 | Sea Creature | avenue_marees | `phantom` | 5 | rank3 | normal |
| **Hurleur Griffe-Sort** (`spellwrit_screamer`) | Spellwrit Screamer | Tempete | 3 | 9 | 215 | 2 | Sea Creature | avenue_marees | `phantom` | 5 | rank3 | normal |
| **Peur de Se Noyer** (`fear_of_drowning`) | Fear of Drowning | Tempete | 3 | 9 | 260 | 2 | Spirit | avenue_marees | `drowned` | 5 | rank3 | normal |
| **Kestrel Orage-Noir** (`kestrel_blackstorm`) | Kestrel Blackstorm | Tempete | 3 | 9 | 250 | 2 | Human | avenue_marees | `witch` | 6 | rank3 | normal |
| **Sergent Fende-Crane** (`sergeant_skullsplitter`) | Sergeant Skullsplitter | Runomancie | 3 | 9 | 275 | 2 | Gobbler | avenue_marees | `vindicator` | 6 | rank3 | normal |
| **Kraken des Marees** (`tide_kraken`) | Tide Kraken | Tempete | 4 | 10 | 520 | 3 | Sea Creature | avenue_marees | `ravager` | 7 | boss_avenue | boss |
| **Le Seigneur des Moissons** (`the_harvest_lord`) | The Harvest Lord | Necromancie | 4 | 10 | 500 | 3 | Undead | avenue_marees | `wither_skeleton` | 8 | boss_avenue | boss |
| **Troll des Glaces** (`ice_troll`) | Ice Troll | Givre | 3 | 13 | 300 | 2 | Troll | chemin_colosses | `stray` | 6 | rank3 | normal |
| **Diablotin Maraudeur** (`marauder_imp`) | Marauder Imp | Pyromancie | 3 | 14 | 280 | 2 | Imp | chemin_colosses | `zombified_piglin` | 6 | rank3 | normal |
| **Sentinelle Colossale** (`colossus_sentinel`) | Colossus Sentinel | Givre | 4 | 15 | 400 | 3 | Golem | chemin_colosses | `stray` | 6 | rank4 | normal |
| **Ombre Nocturne** (`night_shade`) | Night Shade | Necromancie | 4 | 16 | 340 | 3 | Spirit | chemin_colosses | `wither_skeleton` | 6 | rank4 | normal |
| **Maraudeur Runique** (`rune_marauder`) | Rune Marauder | Runomancie | 4 | 16 | 360 | 3 | Gobbler | chemin_colosses | `pillager` | 6 | rank4 | normal |
| **Os de Corsaire** (`corsair_bones`) | Corsair Bones | Necromancie | 5 | 16 | 700 | 3 | Undead | grotte_hantee | `skeleton` | 6 | rank5 | normal |
| **Legion d'Os** (`bone_legion`) | Bone Legion | Necromancie | 4 | 15 | 420 | 2 | Undead | grotte_hantee | `husk` | 5 | rank4 | normal |
| **Le Seigneur du Creux** (`hollow_lord`) | The Hollow Lord | Necromancie | 6 | 18 | 1200 | 4 | Undead | grotte_hantee | `wither_skeleton` | 8 | boss_hollow | boss |
| **Le Grand Necromancien** (`grand_necromancer`) | The Grand Necromancer | Necromancie | 7 | 20 | 1800 | 5 | Undead | grotte_hantee | `evoker` | 8 | boss_hollow | boss |

Resistances et faiblesses relevees dans les fiches du bestiaire :

| Creature | Resistances | Faiblesses | Etourdissable | Envoutable |
| --- | --- | --- | --- | --- |
| Ame Perdue | Necromancie 10% | Sylve 15% | oui | oui |
| Serviteur Hante | Necromancie 10% | Sylve 15% | oui | oui |
| Guerrier Squelettique | Necromancie 15% | Sylve 20% | oui | oui |
| Lutin Ennuyeux | Tempete 10% | Runomancie 15% | oui | oui |
| Lutin des Ronces | Sylve 10% | Necromancie 15% | oui | oui |
| Essaim de Scarabees | Equilibre 10% | Tempete 15% | oui | oui |
| Chasseur Elfe de Braise | Pyromancie 10% | Givre 10% | oui | oui |
| Eclaireur Elfe de Braise | Pyromancie 10% | Givre 10% | oui | oui |
| Homme de Magma | Pyromancie 15% | Givre 15% | oui | oui |
| Gretta Chaudron-Noir | Pyromancie 15% | Givre 15% | oui | oui |
| Vermigut | Runomancie 15% | Tempete 15% | oui | oui |
| Melweena Chatiment | Necromancie 15% | Sylve 20% | oui | oui |
| Sepulcra Ombrevent | Necromancie 20% | Sylve 25% | oui | oui |
| Alcane Vive-Fleche | Pyromancie 20% | Givre 25% | oui | oui |
| Chair Pourrissante | Necromancie 10% | Sylve 15% | oui | oui |
| Marcheur des Cryptes | Necromancie 10% | Sylve 15% | oui | oui |
| Rampant Infect | Necromancie 10% | Sylve 15% | oui | oui |
| Rob le Pourri | Necromancie 15% | Sylve 20% | oui | oui |
| Anguille Electrique | Tempete 10% | Runomancie 15% | oui | oui |
| Hurleur Ecarlate | Tempete 15% | Runomancie 20% | oui | oui |
| Hurleur Griffe-Sort | Tempete 15% | Runomancie 20% | oui | oui |
| Peur de Se Noyer | Tempete 15% | Runomancie 20% | oui | oui |
| Kestrel Orage-Noir | Tempete 15% | Runomancie 20% | oui | oui |
| Sergent Fende-Crane | Runomancie 15% | Tempete 15% | oui | oui |
| Kraken des Marees | Tempete 20% | Runomancie 25% | oui | oui |
| Le Seigneur des Moissons | Necromancie 20% | Sylve 25% | oui | oui |
| Troll des Glaces | Givre 15% | Pyromancie 15% | oui | oui |
| Diablotin Maraudeur | Pyromancie 15% | Givre 15% | oui | oui |
| Sentinelle Colossale | Givre 20% | Pyromancie 20% | non | oui |
| Ombre Nocturne | Necromancie 20% | Sylve 25% | oui | oui |
| Maraudeur Runique | Runomancie 20% | Tempete 20% | oui | oui |
| Os de Corsaire | Necromancie 25% | Sylve 25% | oui | oui |
| Legion d'Os | Necromancie 20% | Sylve 20% | oui | oui |
| Le Seigneur du Creux | Necromancie 30% | Sylve 30% | oui | oui |
| Le Grand Necromancien | Necromancie 35% | Sylve 35% | non | oui |

## PNJ

| PNJ | Nom (EN) | Titre | Zone | Role | Ecole | Niv. | Donne | Remet | Repliques |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Directeur Sage-Aulne** (`headmaster_alderwise`) | Headmaster Alderwise | Directeur de la Cite des Arcanes | grand_place | QUEST_GIVER | - | 50 | welcome_to_the_city | welcome_to_the_city | 3 |
| **Beatrice** (`shopkeeper_beatrice`) | Beatrice | Tenanciere de la boutique | grand_place | SHOPKEEPER | - | 20 | - | - | 3 |
| **Sentinelle Quentin** (`guard_quentin`) | Sentinel Quentin | Garde de la porte | grand_place | GUARD | - | 15 | - | - | 2 |
| **Professeure Cendrelune** (`prof_cinderlune`) | Professor Cinderlune | Professeure de Pyromancie | bosquet | TRAINER | Pyromancie | 40 | school_trial_pyromancy | school_trial_pyromancy | 1 |
| **Professeur Orageval** (`prof_stormvale`) | Professor Stormvale | Professeur de Tempete | bosquet | TRAINER | Tempete | 40 | school_trial_tempest | school_trial_tempest | 1 |
| **Professeure Givrefleur** (`prof_frostbloom`) | Professor Frostbloom | Professeure de Givre | bosquet | TRAINER | Givre | 40 | school_trial_frost | school_trial_frost | 1 |
| **Professeur Cyren** (`prof_cyren`) | Professor Cyren | Professeur de Runomancie | bosquet | TRAINER | Runomancie | 40 | school_trial_runomancy | school_trial_runomancy | 1 |
| **Professeure Verdeterre** (`prof_greenearth`) | Professor Greenearth | Professeure de Sylve | bosquet | TRAINER | Sylve | 40 | school_trial_sylvan | school_trial_sylvan | 1 |
| **Professeur Osseaume** (`prof_ossuary`) | Professor Ossuary | Professeur de Necromancie | bosquet | TRAINER | Necromancie | 40 | school_trial_necromancy | school_trial_necromancy | 1 |
| **Professeur Plumesage** (`prof_whitequill`) | Professor Whitequill | Professeur d'Equilibre | bosquet | TRAINER | Equilibre | 40 | school_trial_equilibrium | school_trial_equilibrium | 1 |
| **Lea Chantenuit** (`lea_nightchant`) | Lea Nightchant | Gardienne du Sentier des Lucioles | sentier_lucioles | QUEST_GIVER | - | 12 | first_steps, unwelcome_spirits, bones_and_badges, report_to_hanna, pixie_problem, sprite_trouble, scarabs_in_the_grass, ticklin_da_bones, nightmare_time | first_steps, unwelcome_spirits, bones_and_badges, pixie_problem, sprite_trouble, scarabs_in_the_grass, ticklin_da_bones, nightmare_time | 3 |
| **Melisande Corneblanche** (`melisande_whitehorn`) | Melisande Whitehorn | Cueilleuse d'herbes | sentier_lucioles | VILLAGER | - | 8 | - | - | 2 |
| **Hanna Brassefonte** (`hanna_emberwright`) | Hanna Emberwright | Garde la maison de la professeure de feu | allee_braises | QUEST_GIVER | - | 14 | she_ran_crying_wildfire, investigate_alley, point_taken, the_cure, administer_cure, oil_rush | report_to_hanna, she_ran_crying_wildfire, investigate_alley, point_taken, the_cure, administer_cure, oil_rush | 2 |
| **Soldat Quinn** (`private_quinn`) | Private Quinn | Garde de la cite, poste de l'Allee des Braises | allee_braises | GUARD | - | 16 | danger_danger, dire_news, cold_hard_steel, magma_mania, gotta_see_gretta | danger_danger, dire_news, cold_hard_steel, magma_mania, gotta_see_gretta | 2 |
| **Esprit du Volcan** (`volcano_spirit`) | Volcano Spirit | Vieux feu, vieille colere | allee_braises | VILLAGER | - | 20 | which_craft, which_hunt, not_that_witch, the_embercat, swiftarrow_ambush | which_craft, which_hunt, not_that_witch, the_embercat, swiftarrow_ambush | 2 |
| **Capitaine Moelle** (`captain_marrow`) | Captain Marrow | Garde du port | avenue_marees | QUEST_GIVER | - | 18 | tide_trouble, screaming_shores, drowning_fears, the_eel_deal, kraken_wakes, the_harvest_lord_quest | tide_trouble, screaming_shores, drowning_fears, the_eel_deal, kraken_wakes, the_harvest_lord_quest | 3 |
| **Pecheur Pike** (`fisherman_pike`) | Fisherman Pike | Peche toujours | avenue_marees | VILLAGER | - | 10 | pikes_net | pikes_net | 2 |
| **Gardienne Cendrevent** (`warden_ashwind`) | Warden Ashwind | Gardienne du Chemin des Colosses | chemin_colosses | QUEST_GIVER | - | 25 | road_of_colossi, cold_shoulders, sentinels_stone, shades_of_night, ashes_of_the_road, frost_knight_fight | road_of_colossi, cold_shoulders, sentinels_stone, shades_of_night, ashes_of_the_road, frost_knight_fight | 3 |
| **Apprenti Perdu** (`lost_apprentice`) | Lost Apprentice | Prisonnier du creux | grotte_hantee | QUEST_GIVER | - | 16 | hollow_beckons, lost_apprentice_quest, the_hollow_lord, end_of_the_hollow | hollow_beckons, lost_apprentice_quest, the_hollow_lord, end_of_the_hollow | 3 |

## Quetes

### Chaine principale

| Quete | Nom (EN) | Niv. | Donneur | Objectifs | Remettant | Recompenses | Suite |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **Bienvenue dans la Cite** (`welcome_to_the_city`) | Welcome to the City | 1 | headmaster_alderwise | Se rendre au Sentier des Lucioles / Parler a Lea Chantenuit | headmaster_alderwise | 20 XP, 5 or | first_steps (nv 1) |
| **Premiers Pas** (`first_steps`) | First Steps | 1 | lea_nightchant | Vaincre 2 Ames Perdues | lea_nightchant | 50 XP, 6 or, Chapeau de Novice | unwelcome_spirits (nv 1) |
| **Esprits Indesirables** (`unwelcome_spirits`) | Unwelcome Spirits | 1 | lea_nightchant | Vaincre 3 Ames Perdues / Vaincre 1 Serviteur Hante | lea_nightchant | 60 XP, 8 or, Anneau de Novice | bones_and_badges (nv 3) |
| **Os et Insignes** (`bones_and_badges`) | Bones and Badges | 3 | lea_nightchant | Vaincre 2 Guerriers Squelettiques / Recuperer 3 os | lea_nightchant | 90 XP, 12 or, Robe de Novice | report_to_hanna (nv 5) |
| **Rapport a Hanna** (`report_to_hanna`) | Report to Hanna | 5 | lea_nightchant | Se rendre a l'Allee des Braises / Parler a Hanna Brassefonte | hanna_emberwright | 80 XP, 10 or | she_ran_crying_wildfire (nv 8) |
| **Elle Fuyait en Feu** (`she_ran_crying_wildfire`) | She Ran Crying Wildfire | 8 | hanna_emberwright | Vaincre 3 Chasseurs Elfes de Braise / Recuperer 2 poudres de blaze | hanna_emberwright | 150 XP, 18 or, Baguette du Bosquet | investigate_alley (nv 8) |
| **Enquete dans la Ruelle** (`investigate_alley`) | Investigate Alley | 8 | hanna_emberwright | Se rendre a l'Allee des Braises / Vaincre 3 Eclaireurs Elfes de Braise | hanna_emberwright | 170 XP, 20 or, Coiffe du Bosquet | point_taken (nv 9) |
| **La Pointe du Clocher** (`point_taken`) | Point Taken | 9 | hanna_emberwright | Vaincre 2 Hommes de Magma / Recuperer 1 eclat d'amethyste | hanna_emberwright | 200 XP, 22 or, Robe du Bosquet | danger_danger (nv 9) |
| **Danger, Danger !** (`danger_danger`) | Danger, Danger! | 9 | private_quinn | Vaincre 1 Gretta Chaudron-Noir | private_quinn | 230 XP, 25 or, Anneau du Bosquet | dire_news (nv 10) |
| **Sombre Nouvelle** (`dire_news`) | Dire News | 10 | private_quinn | Vaincre 3 Hommes de Magma | private_quinn | 250 XP, 28 or, Bottes du Bosquet | the_cure (nv 10) |
| **Le Remede** (`the_cure`) | The Cure | 10 | hanna_emberwright | Recuperer 3 poudres de blaze / Vaincre 2 Serviteurs Hantes | hanna_emberwright | 270 XP, 30 or, Marque du Bucher | administer_cure (nv 11) |
| **Administrer le Remede** (`administer_cure`) | Administer Cure | 11 | hanna_emberwright | Vaincre 3 Serviteurs Hantes / Parler a Soldat Quinn | hanna_emberwright | 280 XP, 32 or, Coiffe de Braise | which_craft (nv 11) |
| **Quel Art ?** (`which_craft`) | Which Craft? | 11 | volcano_spirit | Recuperer 1 barre de blaze | volcano_spirit | 120 XP, 15 or, Robe de Braise | which_hunt (nv 11) |
| **Quelle Chasse ?** (`which_hunt`) | Which Hunt? | 11 | volcano_spirit | Vaincre 1 Sepulcra Ombrevent | volcano_spirit | 320 XP, 40 or, Coiffe de Braise | not_that_witch (nv 12) |
| **Pas Cette Sorciere** (`not_that_witch`) | Not That Witch | 12 | volcano_spirit | Vaincre 1 Melweena Chatiment | volcano_spirit | 340 XP, 44 or, Anneau de Braise | the_embercat (nv 12) |
| **L'Embraiseur dans le Sac** (`the_embercat`) | The Embercat in the Bag | 12 | volcano_spirit | Vaincre 1 Vermigut / Recuperer 1 lingot d'or | volcano_spirit | 360 XP, 46 or, Athame de Braise | swiftarrow_ambush (nv 12) |
| **L'Embuscade d'Alcane** (`swiftarrow_ambush`) | Swiftarrow's Ambush | 12 | volcano_spirit | Vaincre 1 Alcane Vive-Fleche | volcano_spirit | 400 XP, 50 or, Baguette de Braise | tide_trouble (nv 12) |
| **Problemes de Maree** (`tide_trouble`) | Tide Trouble | 12 | captain_marrow | Vaincre 3 Chairs Pourrissantes | captain_marrow | 180 XP, 22 or, Bottes du Bosquet | screaming_shores (nv 12) |
| **Les Rives Hurlantes** (`screaming_shores`) | Screaming Shores | 12 | captain_marrow | Vaincre 4 Hurleurs Ecarlates / Vaincre 2 Hurleurs Griffe-Sort | captain_marrow | 260 XP, 30 or, Amulette du Bosquet | drowning_fears (nv 13) |
| **Peur de Se Noyer** (`drowning_fears`) | Drowning Fears | 13 | captain_marrow | Vaincre 3 Peurs de Se Noyer / Vaincre 2 Marcheurs des Cryptes | captain_marrow | 280 XP, 32 or, Deck du Bosquet | the_eel_deal (nv 13) |
| **Le Marche des Anguilles** (`the_eel_deal`) | The Eel Deal | 13 | captain_marrow | Vaincre 3 Anguilles Electriques / Recuperer 3 eclats de prismarine | captain_marrow | 300 XP, 35 or, Couronne des Marees | kraken_wakes (nv 14) |
| **Le Kraken s'Eveille** (`kraken_wakes`) | The Kraken Wakes | 14 | captain_marrow | Vaincre 1 Kraken des Marees | captain_marrow | 450 XP, 60 or, Robe des Marees | the_harvest_lord_quest (nv 15) |
| **Le Seigneur des Moissons** (`the_harvest_lord_quest`) | The Harvest Lord | 15 | captain_marrow | Vaincre 1 Le Seigneur des Moissons | captain_marrow | 550 XP, 70 or, Baguette des Marees | road_of_colossi (nv 15) |
| **Le Chemin des Colosses** (`road_of_colossi`) | The Colossus Road | 15 | warden_ashwind | Parler a Gardienne Cendrevent | warden_ashwind | 100 XP, 12 or | cold_shoulders (nv 15) |
| **Epaules Froides** (`cold_shoulders`) | Cold Shoulders | 15 | warden_ashwind | Vaincre 4 Trolls des Glaces | warden_ashwind | 320 XP, 38 or, Bottes des Marees | sentinels_stone (nv 16) |
| **La Pierre des Sentinelles** (`sentinels_stone`) | Sentinels of Stone | 16 | warden_ashwind | Vaincre 3 Sentinelles Colossales / Recuperer 4 glaces bleues | warden_ashwind | 380 XP, 45 or, Amulette des Marees | shades_of_night (nv 16) |
| **Ombres Nocturnes** (`shades_of_night`) | Shades of Night | 16 | warden_ashwind | Vaincre 3 Ombres Nocturnes / Vaincre 2 Maraudeurs Runiques | warden_ashwind | 420 XP, 50 or, Deck des Marees | ashes_of_the_road (nv 17) |
| **Cendres sur la Route** (`ashes_of_the_road`) | Ashes on the Road | 17 | warden_ashwind | Vaincre 4 Diablotins Maraudeur | warden_ashwind | 360 XP, 42 or, Athame des Marees | hollow_beckons (nv 17) |
| **Le Creux Appelle** (`hollow_beckons`) | The Hollow Beckons | 17 | lost_apprentice | Se rendre a la Grotte Hantee / Parler a Apprenti Perdu | lost_apprentice | 150 XP, 20 or | lost_apprentice_quest (nv 17) |
| **L'Apprenti Perdu** (`lost_apprentice_quest`) | The Lost Apprentice | 17 | lost_apprentice | Vaincre 3 Legions d'Os / Vaincre 1 Os de Corsaire | lost_apprentice | 400 XP, 45 or, Couronne du Creux | the_hollow_lord (nv 18) |
| **Le Seigneur du Creux** (`the_hollow_lord`) | The Hollow Lord | 18 | lost_apprentice | Vaincre 1 Le Seigneur du Creux | lost_apprentice | 800 XP, 90 or, Robe du Creux | end_of_the_hollow (nv 19) |
| **La Fin du Creux** (`end_of_the_hollow`) | The End of the Hollow | 19 | lost_apprentice | Vaincre 1 Le Grand Necromancien | lost_apprentice | 1200 XP, 150 or, Baguette du Creux, Athame du Creux, Amulette du Creux | - |

### Quetes annexes

| Quete | Nom (EN) | Niv. | Prealable | Donneur | Objectifs | Recompenses |
| --- | --- | --- | --- | --- | --- | --- |
| **Probleme de Lutins** (`pixie_problem`) | Pixie Problem | 1 | - | lea_nightchant | Vaincre 4 Lutins Ennuyeux | 35 XP, 5 or, Bottes de Novice |
| **Ronces Vivantes** (`sprite_trouble`) | Living Thorns | 2 | - | lea_nightchant | Vaincre 3 Lutins des Ronces | 40 XP, 6 or, Amulette de Novice |
| **Scarabees dans l'Herbe** (`scarabs_in_the_grass`) | Scarabs in the Grass | 2 | - | lea_nightchant | Vaincre 3 Essaims de Scarabees / Recuperer 1 eclat d'amethyste | 45 XP, 7 or, Deck de Novice |
| **Acier Froid** (`cold_hard_steel`) | Cold Hard Steel | 9 | she_ran_crying_wildfire | private_quinn | Vaincre 3 Hommes de Magma / Recuperer 2 pepites d'or | 140 XP, 18 or, Athame du Bosquet |
| **Folie de Magma** (`magma_mania`) | Magma Mania | 10 | - | private_quinn | Vaincre 4 Hommes de Magma | 180 XP, 22 or, Amulette du Bosquet |
| **Ruee vers l'Huile** (`oil_rush`) | Oil Rush | 10 | - | hanna_emberwright | Recuperer 2 poudres de blaze / Vaincre 2 Hommes de Magma | 160 XP, 20 or, Deck du Bosquet |
| **Chatouiller les Os** (`ticklin_da_bones`) | Ticklin' da Bones | 4 | - | lea_nightchant | Vaincre 4 Guerriers Squelettiques | 110 XP, 14 or, Athame de Novice |
| **L'Heure du Cauchemar** (`nightmare_time`) | Nightmare Time | 6 | - | lea_nightchant | Vaincre 5 Serviteurs Hantes | 150 XP, 18 or, Anneau du Bosquet |
| **Voir Gretta** (`gotta_see_gretta`) | Gotta See Gretta | 9 | - | private_quinn | Vaincre 1 Gretta Chaudron-Noir / Recuperer 3 pepites d'or | 170 XP, 24 or, Baguette du Bosquet |
| **Combat du Chevalier de Givre** (`frost_knight_fight`) | Frost Knight Fight | 15 | - | warden_ashwind | Vaincre 2 Sentinelles Colossales | 300 XP, 38 or, Amulette du Bosquet |
| **Les Filets de Pike** (`pikes_net`) | Pike's Net | 13 | the_eel_deal | fisherman_pike | Recuperer 4 fils | 120 XP, 15 or, Anneau des Marees |
| **Epreuve de Pyromancie** (`school_trial_pyromancy`) | Pyromancy Trial | 10 | - | prof_cinderlune | Vaincre 3 Chasseurs Elfes de Braise / Recuperer 1 poudre de blaze | 200 XP, 20 or, Faucon Solaire, Athame Pyromantique |
| **Epreuve de Tempete** (`school_trial_tempest`) | Tempest Trial | 10 | - | prof_stormvale | Vaincre 3 Hurleurs Ecarlates / Recuperer 1 eclat de prismarine | 200 XP, 20 or, Requin de Ressac, Athame de Tempete |
| **Epreuve de Givre** (`school_trial_frost`) | Frost Trial | 10 | - | prof_frostbloom | Vaincre 2 Sentinelles Colossales / Recuperer 2 glaces bleues | 200 XP, 20 or, Egide Glaciaire, Athame de Givre |
| **Epreuve de Runomancie** (`school_trial_runomancy`) | Runomancy Trial | 10 | - | prof_cyren | Vaincre 3 Trolls des Glaces / Recuperer 2 eclats d'amethyste | 200 XP, 20 or, Cyclope Runique, Athame Runique |
| **Epreuve de Sylve** (`school_trial_sylvan`) | Sylvan Trial | 10 | - | prof_greenearth | Vaincre 3 Chairs Pourrissantes / Recuperer 3 bles | 200 XP, 20 or, Benediction de la Licorne, Athame Sylvestre |
| **Epreuve de Necromancie** (`school_trial_necromancy`) | Necromancy Trial | 10 | - | prof_ossuary | Vaincre 2 Os de Corsaire / Recuperer 6 os | 200 XP, 20 or, Spectre, Athame Necromantique |
| **Epreuve de Equilibre** (`school_trial_equilibrium`) | Equilibrium Trial | 10 | - | prof_whitequill | Vaincre 2 Ombres Nocturnes / Recuperer 3 pepites d'or | 200 XP, 20 or, Jugement, Athame d'Equilibre |

## Equipement

### Palier 0 : Chapeau

| Piece | Nom (EN) | Emplacement | Niv. | Ecole | Statistiques |
| --- | --- | --- | --- | --- | --- |
| **Chapeau de Novice** (`novice_hat`) | Novice Hat | Chapeau | 1 | - | +20 PV +2 % pip de puissance +2 % precision |
| **Robe de Novice** (`novice_robe`) | Novice Robe | Robe | 1 | - | +40 PV +1 % pip de puissance +1 % resistance |
| **Bottes de Novice** (`novice_boots`) | Novice Shoes | Bottes | 1 | - | +15 PV +1 blocage +3 % precision |
| **Baguette de Novice** (`novice_wand`) | Novice Wand | Baguette | 1 | - | +10 PV +1 % pip de puissance +2 critique +3 % degats |
| **Athame de Novice** (`novice_athame`) | Novice Athame | Athame | 1 | - | +1 % pip de puissance +2 % precision +2 % degats |
| **Anneau de Novice** (`novice_ring`) | Novice Ring | Anneau | 1 | - | +10 PV +2 % pip de puissance +1 critique |
| **Amulette de Novice** (`novice_amulet`) | Novice Amulet | Amulette | 1 | - | +25 PV +2 critique +2 blocage |
| **Deck de Novice** (`novice_deck`) | Novice Deck | Deck | 1 | - | +10 PV +1 % pip de puissance +2 % precision |

### Palier 1 : Coiffe

| Piece | Nom (EN) | Emplacement | Niv. | Ecole | Statistiques |
| --- | --- | --- | --- | --- | --- |
| **Coiffe du Bosquet** (`grove_hood`) | Grove Hood | Chapeau | 5 | - | +32 PV +3 % pip de puissance +3 % precision |
| **Robe du Bosquet** (`grove_robe`) | Grove Robe | Robe | 5 | - | +60 PV +2 % pip de puissance +2 % resistance |
| **Bottes du Bosquet** (`grove_boots`) | Grove Boots | Bottes | 5 | - | +25 PV +2 blocage +4 % precision +1 % degats |
| **Baguette du Bosquet** (`grove_wand`) | Grove Wand | Baguette | 5 | - | +15 PV +2 % pip de puissance +4 critique +1 % percee +5 % degats |
| **Athame du Bosquet** (`grove_athame`) | Grove Athame | Athame | 5 | - | +2 % pip de puissance +3 % precision +4 % degats |
| **Anneau du Bosquet** (`grove_ring`) | Grove Ring | Anneau | 5 | - | +18 PV +3 % pip de puissance +2 critique +2 blocage |
| **Amulette du Bosquet** (`grove_amulet`) | Grove Amulet | Amulette | 5 | - | +40 PV +4 critique +4 blocage +2 % precision |
| **Deck du Bosquet** (`grove_deck`) | Grove Deck | Deck | 5 | - | +15 PV +2 % pip de puissance +2 critique +4 % precision |

### Palier 2 : Coiffe

| Piece | Nom (EN) | Emplacement | Niv. | Ecole | Statistiques |
| --- | --- | --- | --- | --- | --- |
| **Coiffe de Braise** (`ember_cowl`) | Ember Cowl | Chapeau | 10 | - | +45 PV +4 % pip de puissance +2 critique +4 % precision |
| **Robe de Braise** (`ember_robe`) | Ember Robe | Robe | 10 | - | +85 PV +3 % pip de puissance +3 % resistance |
| **Bottes de Braise** (`ember_boots`) | Ember Boots | Bottes | 10 | - | +36 PV +3 blocage +5 % precision +2 % degats |
| **Baguette de Braise** (`ember_wand`) | Ember Wand | Baguette | 10 | - | +20 PV +3 % pip de puissance +6 critique +2 % percee +7 % degats |
| **Athame de Braise** (`ember_athame`) | Ember Athame | Athame | 10 | - | +3 % pip de puissance +4 % precision +1 % percee +5 % degats |
| **Anneau de Braise** (`ember_ring`) | Ember Ring | Anneau | 10 | - | +26 PV +4 % pip de puissance +3 critique +3 blocage |
| **Amulette de Braise** (`ember_amulet`) | Ember Amulet | Amulette | 10 | - | +60 PV +6 critique +6 blocage +3 % precision |
| **Deck de Braise** (`ember_deck`) | Ember Deck | Deck | 10 | - | +20 PV +3 % pip de puissance +3 critique +6 % precision |
| **Athame Pyromantique** (`pyromancy_athame`) | Pyromancy Athame | Athame | 10 | Pyromancie | +2 % pip de puissance +2 critique +3 % precision +9 % degats |
| **Athame de Tempete** (`tempest_athame`) | Tempest Athame | Athame | 10 | Tempete | +2 % pip de puissance +2 critique +3 % precision +9 % degats |
| **Athame de Givre** (`frost_athame`) | Frost Athame | Athame | 10 | Givre | +2 % pip de puissance +2 critique +3 % precision +9 % degats |
| **Athame Runique** (`runomancy_athame`) | Runomancy Athame | Athame | 10 | Runomancie | +2 % pip de puissance +2 critique +3 % precision +9 % degats |
| **Athame Sylvestre** (`sylvan_athame`) | Sylvan Athame | Athame | 10 | Sylve | +2 % pip de puissance +2 critique +3 % precision +9 % degats |
| **Athame Necromantique** (`necromancy_athame`) | Necromancy Athame | Athame | 10 | Necromancie | +2 % pip de puissance +2 critique +3 % precision +9 % degats |
| **Athame d'Equilibre** (`equilibrium_athame`) | Equilibrium Athame | Athame | 10 | Equilibre | +2 % pip de puissance +2 critique +3 % precision +9 % degats |

### Palier 3 : Couronne

| Piece | Nom (EN) | Emplacement | Niv. | Ecole | Statistiques |
| --- | --- | --- | --- | --- | --- |
| **Couronne des Marees** (`tide_crown`) | Tide Crown | Chapeau | 15 | - | +60 PV +5 % pip de puissance +3 critique +5 % precision |
| **Robe des Marees** (`tide_robe`) | Tide Robe | Robe | 15 | - | +115 PV +4 % pip de puissance +4 % resistance |
| **Bottes des Marees** (`tide_boots`) | Tide Boots | Bottes | 15 | - | +48 PV +4 blocage +6 % precision +3 % degats |
| **Baguette des Marees** (`tide_wand`) | Tide Wand | Baguette | 15 | - | +25 PV +4 % pip de puissance +8 critique +3 % percee +9 % degats |
| **Athame des Marees** (`tide_athame`) | Tide Athame | Athame | 15 | - | +4 % pip de puissance +5 % precision +2 % percee +7 % degats |
| **Anneau des Marees** (`tide_ring`) | Tide Ring | Anneau | 15 | - | +34 PV +5 % pip de puissance +4 critique +4 blocage |
| **Amulette des Marees** (`tide_amulet`) | Tide Amulet | Amulette | 15 | - | +80 PV +8 critique +8 blocage +4 % precision |
| **Deck des Marees** (`tide_deck`) | Tide Deck | Deck | 15 | - | +25 PV +4 % pip de puissance +4 critique +8 % precision |

### Palier 4 : Couronne

| Piece | Nom (EN) | Emplacement | Niv. | Ecole | Statistiques |
| --- | --- | --- | --- | --- | --- |
| **Couronne du Creux** (`hollow_crown`) | Hollow Crown | Chapeau | 20 | - | +78 PV +6 % pip de puissance +4 critique +6 % precision |
| **Robe du Creux** (`hollow_robe`) | Hollow Robe | Robe | 20 | - | +150 PV +5 % pip de puissance +5 % resistance |
| **Bottes du Creux** (`hollow_boots`) | Hollow Boots | Bottes | 20 | - | +62 PV +5 blocage +7 % precision +4 % degats |
| **Baguette du Creux** (`hollow_wand`) | Hollow Wand | Baguette | 20 | - | +30 PV +5 % pip de puissance +10 critique +4 % percee +12 % degats |
| **Athame du Creux** (`hollow_athame`) | Hollow Athame | Athame | 20 | - | +5 % pip de puissance +6 % precision +3 % percee +9 % degats |
| **Anneau du Creux** (`hollow_ring`) | Hollow Ring | Anneau | 20 | - | +44 PV +6 % pip de puissance +5 critique +5 blocage |
| **Amulette du Creux** (`hollow_amulet`) | Hollow Amulet | Amulette | 20 | - | +105 PV +10 critique +10 blocage +5 % precision |
| **Deck du Creux** (`hollow_deck`) | Hollow Deck | Deck | 20 | - | +32 PV +5 % pip de puissance +5 critique +10 % precision |

## Tables de butin

| Table | Or | Butin |
| --- | --- | --- |
| `rank1` | 1-3 | novice_hat 2% ×1-1 / novice_boots 2% ×1-1 / novice_ring 2% ×1-1 / minecraft:bone 12% ×1-2 / minecraft:rotten_flesh 10% ×1-2 |
| `rank2` | 2-5 | novice_robe 3% ×1-1 / novice_wand 3% ×1-1 / novice_athame 3% ×1-1 / grove_hood 2% ×1-1 / grove_boots 2% ×1-1 / minecraft:bone 15% ×1-3 / minecraft:amethyst_shard 5% ×1-1 |
| `rank3` | 4-9 | grove_robe 3% ×1-1 / grove_wand 3% ×1-1 / grove_ring 3% ×1-1 / ember_cowl 2% ×1-1 / ember_boots 2% ×1-1 / minecraft:bone 18% ×2-3 / minecraft:blaze_powder 10% ×1-2 / minecraft:amethyst_shard 8% ×1-1 |
| `rank4` | 6-12 | ember_robe 3% ×1-1 / ember_wand 3% ×1-1 / ember_athame 3% ×1-1 / tide_crown 2% ×1-1 / tide_boots 2% ×1-1 / minecraft:blaze_powder 12% ×1-3 / minecraft:prismarine_shard 12% ×1-3 / minecraft:amethyst_shard 10% ×1-2 |
| `rank5` | 9-16 | tide_robe 3% ×1-1 / tide_wand 3% ×1-1 / tide_ring 3% ×1-1 / hollow_crown 2% ×1-1 / minecraft:bone 20% ×2-4 / minecraft:blue_ice 12% ×1-2 / minecraft:diamond 4% ×1-1 |
| `rank6` | 14-24 | hollow_robe 3% ×1-1 / hollow_wand 3% ×1-1 / hollow_athame 3% ×1-1 / minecraft:diamond 8% ×1-1 / minecraft:netherite_scrap 3% ×1-1 |
| `boss_alley` | 25-45 | ember_robe 25% ×1-1 / ember_wand 25% ×1-1 / ember_amulet 20% ×1-1 / ember_athame 20% ×1-1 / pyromancy_athame 12% ×1-1 / necromancy_athame 12% ×1-1 / minecraft:blaze_rod 40% ×1-2 / minecraft:amethyst_shard 30% ×1-3 |
| `boss_avenue` | 30-55 | tide_robe 25% ×1-1 / tide_wand 25% ×1-1 / tide_amulet 20% ×1-1 / tide_deck 20% ×1-1 / tempest_athame 12% ×1-1 / runomancy_athame 12% ×1-1 / minecraft:prismarine_shard 40% ×2-4 / minecraft:heart_of_the_sea 15% ×1-1 |
| `boss_road` | 40-70 | hollow_robe 25% ×1-1 / hollow_wand 25% ×1-1 / hollow_amulet 20% ×1-1 / hollow_ring 20% ×1-1 / frost_athame 12% ×1-1 / sylvan_athame 12% ×1-1 / minecraft:blue_ice 45% ×2-4 / minecraft:diamond 25% ×1-3 |
| `boss_hollow` | 60-110 | hollow_crown 30% ×1-1 / hollow_robe 30% ×1-1 / hollow_wand 30% ×1-1 / hollow_athame 25% ×1-1 / hollow_amulet 25% ×1-1 / hollow_deck 25% ×1-1 / necromancy_athame 20% ×1-1 / equilibrium_athame 20% ×1-1 / minecraft:netherite_scrap 30% ×1-2 / minecraft:diamond 40% ×2-4 / minecraft:echo_shard 20% ×1-2 |
