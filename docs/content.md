# Écrire vous-même les dialogues, les noms de PNJ et les noms de sorts

Tout le texte du mod est **traduisible** : le code ne contient plus de noms
« en dur » que vous seriez obligé de modifier. Vous pouvez donc choisir les
dialogues, renommer les PNJ et les sorts — **sans recompiler le mod**.

## Les trois façons de faire

| Méthode | Effort | Quand l'utiliser |
| --- | --- | --- |
| **Pack de ressources** (recommandé) | aucune compilation | écrire les textes, recharger, jouer |
| Fichiers de langue du mod | recompiler (`./gradlew build`) | préparer les textes de base du mod |
| Code source du moteur | recompiler + tests | changer la *structure* (nouveaux PNJ, nouvelles quêtes) |

### 1. Pack de ressources (aucune compilation)

```bash
python3 tools/make-content-pack.py /chemin/vers/jdk17
```

Le script écrit `content/arcanum-realms-textes/` : c'est un pack de ressources
prêt à l'emploi, avec les deux fichiers de langue et un `README.md` détaillé.
Copiez ce dossier dans le dossier `resourcepacks` de votre instance, activez-le
dans **Options > Ressources**, puis rechargez avec **F3 + T** après chaque
modification.

Les fichiers à éditer sont :

```text
content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json
content/arcanum-realms-textes/assets/arcanum_realms/lang/en_us.json
```

Vous n'êtes pas obligé de garder toutes les clés : **seules celles que vous
écrivez** remplacent le texte d'origine. Pour ne changer que les dialogues de
Léa Chantenuit, il suffit de garder ses clés :

```json
{
  "dialogue.arcanum_realms.lea_nightchant.greeting.0": "Tiens, un nouveau visage.",
  "dialogue.arcanum_realms.lea_nightchant.greeting.1": "Les âmes perdues rodent encore sur le sentier.",
  "dialogue.arcanum_realms.lea_nightchant.farewell.0": "Revenez vite, on a besoin de vous.",
  "npc.arcanum_realms.lea_nightchant": "Léa Chantenuit"
}
```

### 2. Fichiers de langue du mod

Les mêmes clés existent dans `src/main/resources/assets/arcanum_realms/lang/`.
Ils sont **générés** par `tools/generate-lang.py` :

```bash
python3 tools/generate-lang.py /chemin/vers/jdk17
```

Si vous les modifiez, notez que la prochaine exécution du générateur écrasera vos
textes : préférez le pack de ressources, ou modifiez le code du moteur (méthode 3).

### 3. Code source du moteur

Les textes par défaut vivent dans `src/main/java/com/arcanumrealms/core/world/` :

| Fichier | Contenu |
| --- | --- |
| `Npcs.java` | noms, titres, descriptions et répliques des PNJ |
| `Quests.java` | noms, résumés, objectifs et récompenses des quêtes |
| `Bestiary.java` | noms et fiches des créatures |
| `Zones.java` | noms et descriptions des zones |
| `Gears.java` | noms de l'équipement |
| `core/spell/SpellBook.java` | noms et valeurs des sorts |

Les dialogues s'écrivent avec `Dialogue.of(fr, en)` : la première chaîne est la
version française, la seconde l'anglaise. Plusieurs lignes se suivent dans
l'ordre où elles seront affichées :

```java
.greeting(Dialogue.of(
        "Tiens, un nouveau visage.",
        "A new face, is it?"))
.inProgress(Dialogue.of(
        "Les âmes perdues rodent encore sur le sentier.",
        "The lost souls still prowl the path."))
.completion(Dialogue.of(
        "Vous les avez repoussés. Bien joué.",
        "You pushed them back. Well fought."))
```

## Comment ça marche

Chaque texte voyage **avec sa clé** (`ContentLine`) entre le serveur et le
client : le serveur envoie la clé *et* son texte par défaut, le client affiche la
traduction du joueur si elle existe. C'est ce qui permet à un pack de ressources
de tout réécrire, et à deux joueurs de langues différentes de voir chacun la leur.

Pour les textes composés par le serveur (journal de combat, noms de créatures
dans les messages), le client remplace les noms connus par les vôtres : renommer
un sort se voit donc **partout**, y compris dans le compte rendu des rounds.

## Conventions des clés

```text
npc.arcanum_realms.<pnj>                       nom du PNJ
npc.arcanum_realms.<pnj>.title                 titre ("Directrice de l'Allée")
npc.arcanum_realms.<pnj>.description           description de la fiche
dialogue.arcanum_realms.<pnj>.<moment>.<n>     réplique n du moment donné
spell.arcanum_realms.<sort>.name               nom du sort
spell.arcanum_realms.<sort>.desc               description du sort
creature.arcanum_realms.<creature>             nom de la créature
quest.arcanum_realms.<quete>                   nom de la quête
quest.arcanum_realms.<quete>.summary           résumé de la quête
quest.arcanum_realms.<quete>.goal.<n>          objectif n
quest.arcanum_realms.<quete>.rewards           ligne des récompenses
zone.arcanum_realms.<zone>                     nom de la zone
gear.arcanum_realms.<piece>                    nom de la pièce d'équipement
```

Les quatre *moments* d'un dialogue sont `greeting`, `inProgress`, `completion`
et `farewell` (voir `ContentKeys.SLOTS`). Les numéros commencent à 0.

## Vérifier vos textes

```bash
# cohérence : clés inconnues, valeurs vides, ce qui est personnalisé
python3 tools/make-content-pack.py /chemin/vers/jdk17 --check content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json

# rendu en jeu, sans lancer Minecraft
python3 tools/make-content-pack.py /chemin/vers/jdk17 --simulate content/arcanum-realms-textes/assets/arcanum_realms/lang/fr_fr.json
```

Le vérificateur signale les clés inconnues (faute de frappe), les valeurs vides
et compte ce que vous avez personnalisé : dialogues, noms de sorts, noms de PNJ.

La **simulation** reproduit ce que fait le client et affiche le résultat : vos
dialogues tels qu'ils apparaîtront, le nom du sort dans le grimoire, et le journal
de combat où les noms écrits par le moteur sont remplacés par les vôtres :

```text
  Journal de combat (noms de sorts du moteur, reecrits par le pack) :
      Ember Kitten casts Cinder Wisp (2 pips)
      -> Chaton de Braise casts Feu Follet de Braise (2 pips)
```

Les tests du moteur vérifient de leur côté que chaque texte du contenu possède
bien une clé unique et non vide (`./tools/run-core-tests.sh`) : impossible
d'ajouter un PNJ ou un sort sans clé.
