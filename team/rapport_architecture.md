# ⚙️ Rapport d'architecture

## 1. Objet du document

Ce rapport décrit l'architecture du projet **Bugémon** telle qu'elle est implémentée.

Il présente :
- le démarrage réel de l'application JavaFX ;
- l'organisation MVC du code ;
- les responsabilités des contrôleurs, services, modèles, vues et DTO ;
- les mécanismes de persistance ;
- les principaux flux de jeu : équipe, run, exploration, combat, récompenses, progression ;
- la couverture des histoires déjà prises en charge par l'architecture actuelle.

---

## Table des matières

1. [Objet du document](#1-objet-du-document)
2. [Vue d'ensemble](#2-vue-densemble)
3. [Démarrage de l'application](#3-démarrage-de-lapplication)
4. [Architecture MVC](#4-architecture-mvc)
5. [Données, persistance et ressources](#5-données-persistance-et-ressources)
6. [Flux fonctionnels principaux](#6-flux-fonctionnels-principaux)
7. [Couverture fonctionnelle](#7-couverture-fonctionnelle)
8. [Points importants de l'implémentation](#8-points-importants-de-limplémentation)

---

## 2. Vue d'ensemble

Le projet est une application JavaFX structurée autour du package racine `ulb`.

### 2.1. Structure générale

- `ulb.Main` : point d'entrée JavaFX.
- `ulb.controller` : orchestration applicative, navigation, cycle de vie des runs et flux de combat.
- `ulb.controller.service` : services extraits des contrôleurs pour les DTO, récompenses, slots, état de jeu, création de Bugémon, arbre de compétences.
- `ulb.view` : interface JavaFX.
- `ulb.models.data` : définitions métier statiques chargées depuis les JSON.
- `ulb.models.game` : état mutable d'une équipe, d'une run, de la tour et de la progression.
- `ulb.models.battle` : moteur de combat.
- `ulb.models.skilltree` : arbre de compétences permanent et calcul des bonus.
- `ulb.parsing` : chargement des fichiers JSON.
- `ulb.dto` : snapshots immuables transmis aux vues.
- `ulb.audio` : lecture des musiques et sons.
- `src/main/resources` : données, images, audio, polices, CSS.
- `src/test/java` : tests unitaires, tests de services et tests GUI.

### 2.2. Dépendances et build

Le projet est construit avec Maven (`pom.xml`) et utilise :
- Java 18 ;
- JavaFX 23.0.2 (`controls`, `fxml`, `graphics`, `media`) ;
- Jackson pour le chargement des JSON métier ;
- Gson pour la persistance locale et l'écriture de certains fichiers JSON utilisateur ;
- JUnit 4, JUnit 5, TestFX et AssertJ pour les tests.

L'application se lance via `mvn exec:java`, dont le `mainClass` est `ulb.Main`.

---

## 3. Démarrage de l'application

### 3.1. Point d'entrée

Le point d'entrée est `src/main/java/ulb/Main.java`.

`Main` hérite de `javafx.application.Application`. Sa méthode `start(Stage primaryStage)` :
1. instancie un `MainController` avec le `Stage` principal ;
2. définit le titre de la fenêtre ;
3. appelle `controller.showMainMenu()`.

La méthode `main(String[] args)` appelle simplement `launch(args)`.

### 3.2. Orchestration initiale

Le démarrage applicatif est réparti entre trois composants :

- `MainController` : crée les contrôleurs et services principaux, charge les polices, conserve l'état courant de la tour, de la difficulté et des bonus actifs de run.
- `SceneManager` : possède le `Stage`, crée ou réutilise la `Scene`, applique le CSS global et verrouille le ratio de fenêtre.
- `MainWindowView` : vue JavaFX du menu principal, sous forme de `StackPane`.

`MainWindowView` n'est plus une classe `Application`. Elle construit le menu principal :
- `Continuer`
- `Nouvelle Partie`
- `Charger une partie`
- `Gérer équipe`
- `Arbre de competences`
- `Quitter`

Les actions des boutons sont déléguées au `MainController`. La vue affiche aussi la vidéo d'arrière-plan du menu, avec un fallback statique si la ressource média n'est pas disponible.

---

## 4. Architecture MVC

Le projet suit une architecture MVC pragmatique :

- les **modèles** contiennent les règles métier et l'état ;
- les **vues** affichent les données et déclenchent des actions utilisateur ;
- les **contrôleurs** coordonnent les vues, les modèles et les services ;
- les **DTO** isolent les vues des objets métier mutables.

### 4.1. Modèle

#### Modèle statique

Les classes de `ulb.models.data` décrivent le catalogue du jeu :
- `BugemonDefinition`
- `Attack`
- `ItemDefinition`
- `Stats`
- `Type`
- `Difficulty`
- `GameMode`
- `Effect` et ses implémentations : `HealingEffect`, `StatModifierEffect`, `ResetMalusEffect`, `UnknownEffect`
- `EffectVisitor`

Ces données sont principalement chargées depuis :
- `src/main/resources/data/bugemons.json`
- `src/main/resources/data/attaques.json`
- `src/main/resources/data/objets.json`
- `src/main/resources/data/skill_tree.json`

#### Modèle dynamique

Les classes de `ulb.models.game` représentent l'état concret du joueur et de la partie :
- `PlayerProfile`
- `GameSlotData`
- `TowerNOState`
- `TowerNO`
- `FloorInstance`
- `RoomCell`
- `RoomType`
- `Team`
- `BugemonInstance`
- `Inventory`
- `Reward`
- `StatBonus`
- `LevelUpBonus`
- `PendingLevelUpChoice`

Il n'existe plus de classe `RunState`. L'état d'une run est réparti entre :
- `PlayerProfile`, qui contient le slot actif, les équipes sauvegardées, l'équipe active, l'inventaire et l'arbre de compétences ;
- `GameSlotData`, qui contient les métadonnées et la progression d'un emplacement de sauvegarde ;
- `TowerNO` / `TowerNOState`, qui représentent l'état courant ou sérialisé de la tour.

#### Arbre de compétences

Le package `ulb.models.skilltree` gère la progression passive entre les runs :
- `SkillTreeNode`
- `SkillTreeProgress`
- `SkillTreeBonuses`
- `SkillEffect`

`SkillTreeProgress` conserve les points disponibles et les niveaux alloués. `computeBonuses()` produit un `SkillTreeBonuses` appliqué au lancement ou au chargement d'une run.

Les bonus pris en compte incluent notamment :
- statistiques globales d'équipe ;
- multiplicateur d'XP ;
- régénération post-combat ;
- objets bonus au démarrage ;
- bonus de critique ;
- multiplicateurs de dégâts par type ;
- nombre de choix de montée de niveau.

#### Moteur de combat

Le package `ulb.models.battle` contient le moteur de combat :
- `Battle`
- `BattleService`
- `BattleAction`
- `AttackAction`
- `SwitchAction`
- `UseItemAction`
- `SurrenderAction`
- `EnemyAttackAction`
- `DamageCalculator`
- `BattleAttackResolver`
- `BattleEffectService`
- `BattleItemService`
- `BattleKOHandler`
- `BattleSelectionService`
- `EnemyTeamFactory`
- `ExperienceService`
- `ExperienceGain`
- `ExperienceResolution`

`Battle` travaille sur une copie profonde des équipes afin d'isoler le combat de l'état persistant. Après résolution, `TeamManagerController.syncBattleProgress(...)` recopie les PV, XP et niveaux vers l'équipe active du profil.

### 4.2. Vue

La couche `ulb.view` contient les écrans et composants JavaFX.

Vues de navigation :
- `MainWindowView`
- `NewGameView`
- `LoadGameView`
- `RunSlotSelectionView`

Vues d'équipe et de progression :
- `TeamManagerView`
- `BugemonCreatorView`
- `SkillTreeView`

Vues de combat :
- `BattleView`
- `BugemonZoneBattleView`
- `ActionSelectBattleView`
- `AttackSelectionView`
- `ItemSelectionView`
- `SwitchSelectionView`
- `AbstractSelectionBattleView`
- `HealthBarStackPane`

Vues d'exploration et de récompense :
- `FloorMapView`
- `RoomView`
- `RewardView`
- `LevelUpSelectionView`

Vues de résultat :
- `VictoryView`
- `GameOverView`
- `FloorBossVictoryView`

Autres composants :
- `GameNotificationPopup`
- `ViewConfig`
- `RewardActionHandler`

La vue ne modifie pas directement le modèle métier. Elle passe par `MainController`, `TeamManagerController` ou `BattleController`, puis reçoit des DTO prêts à afficher.

### 4.3. Contrôleurs

#### `MainController`

`MainController` est l'orchestrateur applicatif. Il :
- instancie les services principaux ;
- délègue la navigation à `NavigationController` ;
- délègue les runs à `RunLifecycleController` ;
- délègue les combats à `BattleFlowController` ;
- expose les méthodes utilisées par les vues ;
- maintient la `TowerNO`, la difficulté courante et les bonus actifs de run ;
- applique les récompenses via `RewardApplicationService` ;
- persiste l'état de run via `PlayerProfile`.

#### `SceneManager`

`SceneManager` centralise la manipulation du `Stage`.

Il :
- crée la `Scene` si nécessaire ;
- remplace la racine affichée lors des changements d'écran ;
- applique la feuille de style globale ;
- impose les dimensions minimales et un ratio 4:3.

#### `NavigationController`

`NavigationController` construit les vues de navigation et choisit la scène à afficher :
- menu principal ;
- nouvelle partie ;
- sélection de slot ;
- chargement ;
- création de Bugémon ;
- gestion d'équipe ;
- arbre de compétences ;
- carte d'étage.

Il déclenche aussi les musiques adaptées aux menus et à la carte.

#### `RunLifecycleController`

`RunLifecycleController` gère le cycle de vie des runs :
- création d'une run dans un slot ;
- reprise d'un slot ;
- continuation automatique de la dernière sauvegarde chargeable ;
- initialisation de la difficulté ;
- réinitialisation de la tour et de l'inventaire ;
- application des bonus d'arbre au lancement ou au chargement.

#### `BattleFlowController`

`BattleFlowController` gère le déroulement applicatif d'un combat :
- création du `BattleController` ;
- affichage de `BattleView` ;
- transformation d'une action utilisateur en `BattleAction` ;
- exécution de la séquence de tour via `BattleOrchestrationService` ;
- résolution post-combat ;
- application des bonus de régénération ;
- traitement des montées de niveau ;
- finalisation de la progression via `GameStateService` ;
- affichage des écrans de victoire, boss, défaite ou carte.

#### `TeamManagerController`

`TeamManagerController` gère :
- les Bugémons disponibles ;
- l'équipe en cours de composition ;
- les équipes sauvegardées ;
- la conversion entre `BugemonDefinition` et `BugemonInstance` ;
- les DTO d'équipe et de catalogue ;
- l'accès au `PlayerProfile` ;
- l'inventaire de la run active ;
- la progression de l'arbre de compétences ;
- la synchronisation des résultats de combat vers l'équipe persistée.

#### `BattleController`

`BattleController` est une façade entre la vue de combat et le moteur :
- charge les attaques et objets ;
- génère l'équipe ennemie via `EnemyTeamFactory` ;
- crée `Battle` et `BattleService` ;
- expose les actions de combat ;
- construit les DTO de combat via `BattleStateMapper` et `BattleUIDataProvider` ;
- calcule et distribue l'XP ;
- prépare les choix de montée de niveau ;
- applique les bonus de run sur l'XP, les critiques et les dégâts par type.

### 4.4. Services

Les services de `ulb.controller.service` réduisent la taille des contrôleurs :

- `BattleStateMapper` : transforme l'état de combat en DTO.
- `BattleUIDataProvider` : prépare les données utiles à la vue de combat.
- `BattleOrchestrationService` : détermine et exécute l'ordre des actions d'un tour.
- `LevelUpChoiceHandler` : file des choix de montée de niveau.
- `RewardProvider` : fournit les récompenses de la salle courante.
- `RewardApplicationService` : applique les récompenses choisies.
- `AttackProvider` : fournit les attaques disponibles par type.
- `BugemonCreationService` : crée un Bugémon personnalisé, copie son sprite et met à jour `custom.json`.
- `SkillBonusApplicationService` : applique les bonus de l'arbre à l'équipe et à l'inventaire.
- `RunSlotService` : convertit les slots de sauvegarde en `RunSlotDTO`.
- `FloorNamingService` : fournit les noms lisibles des étages.
- `GameStateService` : finalise une victoire une seule fois et suit les victoires de boss à afficher.
- `UINotificationService` : affiche des notifications et erreurs non bloquantes.

### 4.5. DTO

Le package `ulb.dto` contient des records immuables transmis aux vues.

DTO de combat :

| DTO | Rôle |
|---|---|
| `BattleStateDTO` | Snapshot des deux Bugémons actifs |
| `BugemonActiveStateDTO` | Nom, type, PV, niveau, sprite, attaques, dégâts reçus |
| `AttackSummaryDTO` | Identifiant, nom et type d'une attaque |
| `BugemonDisplayDTO` | Affichage simplifié d'un membre d'équipe |

DTO d'équipe :

| DTO | Rôle |
|---|---|
| `BugemonSummaryDTO` | Entrée de catalogue affichable |
| `BugemonStatsDTO` | Stats de base et attaques accessibles |
| `TeamStateDTO` | État de l'équipe en cours d'édition |

DTO de run, récompenses et progression :

| DTO | Rôle |
|---|---|
| `RunSlotDTO` | État affichable d'un slot de sauvegarde |
| `RewardChoiceDTO` | Choix de récompense ou cible sélectionnable |
| `TowerProgressDTO` | Progression lisible dans la tour |

DTO d'exploration :

| DTO | Rôle |
|---|---|
| `FloorMapDTO` | Carte complète de l'étage |
| `FloorRoomDTO` | Salle individuelle |
| `FloorConnectionDTO` | Connexion entre deux salles |

DTO d'arbre de compétences :

| DTO | Rôle |
|---|---|
| `SkillTreeStateDTO` | Points disponibles et liste des nœuds |
| `SkillTreeNodeDTO` | Identité, position, niveau, coût, état actif/disponible et prérequis |

---

## 5. Données, persistance et ressources

### 5.1. Chargement JSON

`JsonDataLoader<T>` est le chargeur générique principal. Il :
- ouvre une ressource du classpath ou un fichier local ;
- lit le JSON avec Jackson ;
- extrait une clé racine ;
- convertit les entrées vers le type cible ;
- indexe les objets par ID ;
- refuse les IDs dupliqués.

Les classes de parsing utilisent ce chargeur :
- `BugemonData` charge `data/bugemons.json`, puis fusionne `custom.json` si présent ;
- `AttackData` charge `data/attaques.json` ;
- `ItemData` charge `data/objets.json` et l'inventaire de départ ;
- `SkillTreeData` charge `data/skill_tree.json`.

### 5.2. Persistance locale

La persistance principale se fait dans `teams_save.json` via `PlayerProfile`.

Ce fichier contient :
- les équipes sauvegardées ;
- le nom de l'équipe courante ;
- les cinq `GameSlotData` ;
- le slot actif ;
- les points disponibles de l'arbre de compétences ;
- les niveaux alloués dans l'arbre.

`PlayerProfile` garde aussi une compatibilité avec les anciens formats de sauvegarde : les anciens champs `towerLevel`, `towerFloor` et `inventory` sont migrés vers le premier slot si nécessaire.

Les Bugémons créés par le joueur sont écrits dans `custom.json` par `BugemonCreationService`. Le sprite importé est copié dans le dossier `images/`, puis référencé dans le JSON personnalisé.

### 5.3. Ressources visuelles et audio

Les ressources se trouvent dans `src/main/resources` :
- sprites de Bugémons ;
- images de fond des étages ;
- icônes de combat et de carte ;
- CSS ;
- polices ;
- fichiers audio.

`ViewConfig` centralise les chemins utilisés par les vues.

`AudioConfig` centralise les chemins audio :
- menu ;
- exploration ;
- combat ;
- boss ;
- victoire ;
- défaite ;
- fin.

`AudioManager` lit les ressources avec `MediaPlayer`, maintient une boucle musicale et gère les sons ponctuels.

---

## 6. Flux fonctionnels principaux

### 6.1. Constituer et sauvegarder une équipe

```text
MainWindowView
  -> MainController.showTeamManagement()
      -> NavigationController.showTeamManagement()
          -> TeamManagerView
              -> TeamManagerController.getAllBugemonsSummaries()
              -> TeamManagerController.addBugemonOnTeamById(id)
              -> TeamManagerController.getCurrentTeamStateDTO()
```

La sauvegarde suit ensuite :

```text
TeamManagerView
  -> MainController.handleSaveTeam(name)
      -> TeamManagerController.saveCurrentTeam(name)
          -> new Team(name)
          -> new BugemonInstance(definition)
          -> PlayerProfile.saveTeam(name)
          -> teams_save.json
```

### 6.2. Créer une run

```text
NewGameView
  -> sélection d'une équipe + difficulté
  -> MainController.showRunSlotSelection(teamName, difficulty)
      -> NavigationController.showRunSlotSelection(...)
          -> RunSlotSelectionView
              -> MainController.startNewRunInSlot(slot, runName, difficulty)
                  -> PlayerProfile.prepareNewRunAtSlot(...)
                  -> RunLifecycleController.startNewRun(...)
                      -> resetActiveRunToFreshTower()
                      -> resetInventoryForNewRun()
                      -> SkillTreeProgress.computeBonuses()
                      -> SkillBonusApplicationService.applyRunStartSkillBonuses(...)
                      -> MainController.persistRunState()
```

La difficulté choisie est conservée dans le slot et utilisée par `EnemyTeamFactory` lors de la génération des adversaires.

### 6.3. Charger ou continuer une run

```text
MainWindowView / LoadGameView
  -> MainController.continueLastRun()
     ou MainController.loadGameFromSlot(index)
      -> RunLifecycleController
          -> PlayerProfile.getRunSlot(index)
          -> TeamManagerController.loadTeamFromProfile(teamName)
          -> TowerNOState.toTower()
          -> SkillBonusApplicationService.applyLoadedRunSkillBonuses(...)
          -> MainController.showFloorMap()
```

`RunSlotService` fournit les `RunSlotDTO` utilisés par `LoadGameView` et `RunSlotSelectionView`.

### 6.4. Explorer un étage

```text
FloorMapView
  -> MainController.getCurrentFloorMap()
      -> TowerNO.getFloorMap()
          -> FloorMapDTO / FloorRoomDTO / FloorConnectionDTO
```

Quand le joueur clique sur une salle accessible :

```text
RoomView
  -> MainController.enterRoom(roomId)
      -> TowerNO.enterRoom(roomId)
          -> FloorInstance.moveTo(roomId)
  -> MainController.handleMapRoom(roomType)
      -> BattleFlowController.handleMapRoom(roomType)
```

Selon le `RoomType`, l'application lance un combat, affiche une récompense ou revient à la carte.

### 6.5. Lancer et résoudre un combat

```text
BattleFlowController.startNewBattle(difficulty)
  -> new BattleController(team, inventory, difficulty, skillBonuses)
      -> EnemyTeamFactory.buildEnemyTeam(...)
      -> new Battle(...)
      -> new BattleService(...)
      -> BattleStateMapper / BattleUIDataProvider
```

Action d'attaque :

```text
BattleView
  -> MainController.executeAction(ATTACK, attackId)
      -> BattleFlowController.executeAction(...)
          -> new AttackAction(...)
          -> BattleOrchestrationService.determineTurnSequence(...)
          -> BattleController.executePlayerAction(action)
              -> BattleService.playerAttack(...)
                  -> DamageCalculator.calculateDamage(...)
                  -> BattleEffectService
```

Les autres actions (`UseItemAction`, `SwitchAction`, `SurrenderAction`, `EnemyAttackAction`) passent par le même mécanisme `BattleAction`.

### 6.6. Fin de combat et progression

En cas de victoire :

```text
BattleFlowController.processBattleEnd()
  -> SkillBonusApplicationService.applyPostCombatRegeneration(...)
  -> BattleController.resolveVictoryRewards(floor)
      -> ExperienceService.calculateVictoryXp(...)
      -> ExperienceService.distributeVictoryXp(...)
      -> LevelUpChoiceHandler.queueLevelUpChoices(...)
  -> TeamManagerController.syncBattleProgress(...)
```

Une fois les choix de montée de niveau traités :

```text
BattleFlowController.finalizeVictoriousBattleOutcome()
  -> GameStateService.finalizeVictoriousBattleOutcome()
      -> TowerNO.processBattleResult(true)
  -> PlayerProfile.grantSkillPoint() si boss vaincu
  -> MainController.persistRunState()
  -> BattleFlowController.navigateAfterVictory()
```

Si un boss d'étage est vaincu, `FloorBossVictoryView` est affichée avant le retour à la carte du nouvel étage. Si le boss final est vaincu, `VictoryView` est affichée.

En cas de défaite :

```text
BattleFlowController.resetGameAndReturnToMenu()
  -> TeamManagerController.resetCurrentTeamProgressToLevelOne()
  -> PlayerProfile.clearActiveRunSlot()
  -> new TowerNO()
  -> GameOverView
```

### 6.7. Récompenses

Les récompenses sont liées aux salles `REWARD`.

```text
RewardView
  -> MainController.getCurrentRewardOptions()
      -> RewardProvider.getCurrentRewardOptions()
          -> FloorInstance.generateRewardOptions(...)
```

Les récompenses possibles sont :
- `Reward.Item` : ajoute un objet à l'inventaire ;
- `Reward.Stats` : applique un bonus permanent à un Bugémon ;
- `Reward.AttackReward` : remplace une attaque existante par une nouvelle attaque compatible.

L'application se fait via `RewardApplicationService`, puis l'état de run est persisté.

### 6.8. Arbre de compétences

```text
SkillTreeView
  -> MainController.getSkillTreeState()
      -> TeamManagerController.getSkillTreeStateDTO()
          -> SkillTreeProgress
```

Allocation ou retrait :

```text
SkillTreeView
  -> MainController.allocateSkillPoint(nodeId)
     ou MainController.removeSkillPoint(nodeId)
      -> TeamManagerController
          -> SkillTreeProgress
          -> PlayerProfile.saveToDisk()
```

Les points sont gagnés après une victoire contre un boss d'étage.

---

## 7. Couverture fonctionnelle

### Histoires 1 à 3 — Équipes

Ce qui existe :
- sélection d'une équipe de Bugémons via `TeamManagerView` ;
- affichage des sprites et statistiques ;
- ajout, retrait, limite de taille et refus des doublons ;
- sauvegarde nommée ;
- chargement d'une équipe sauvegardée ;
- suppression d'une équipe ;
- persistance dans `teams_save.json`.

Limite actuelle :
- il n'existe pas de renommage explicite d'une équipe sauvegardée.

### Histoires 4 et 5 — Combat

Ce qui existe :
- génération automatique d'une équipe adverse de même taille ;
- niveau ennemi ajusté selon la difficulté ;
- IA adverse jouée automatiquement via `EnemyAttackAction` ;
- choix d'action joueur : attaque, objet, changement, abandon ;
- menus de combat séparés ;
- ordre de tour déterminé par l'initiative.

Le flux principal actuel est un combat contrôlé par le joueur. Le combat entièrement automatique n'est plus le mode d'interaction principal.

### Histoire 6 — Dégâts et statistiques

Ce qui existe :
- calcul centralisé dans `DamageCalculator` ;
- prise en compte de l'attaque, de la défense, du type, des critiques et des dégâts minimums ;
- messages d'efficacité ;
- aperçu des types dans la vue de combat.

### Histoire 7 — Expérience et montée de niveau

Ce qui existe :
- calcul d'XP selon l'étage, le type de combat et le nombre d'adversaires ;
- distribution aux participants ;
- file de choix de montée de niveau ;
- bonus permanents appliqués après sélection du joueur ;
- intégration des bonus d'arbre sur l'XP et le nombre de choix.

### Histoire 8 — Statuts

Ce qui existe :
- effets de soin ;
- modificateurs de statistiques ;
- durée permanente ou limitée au tour ;
- reset des malus ;
- support des cibles lanceur, adversaire et équipe ;
- remise à zéro des bonus temporaires en fin de combat.

### Histoire 9 — Tour NO

Ce qui existe :
- `TowerNO` pour la progression macro ;
- `FloorInstance` pour la carte d'étage ;
- carte fixe en croix ;
- salles `START`, `COMBAT`, `REWARD`, `BOSS`, `END` ;
- passage à l'étage suivant après boss ;
- fin de tour après boss final.

### Histoire 10 — Objets

Ce qui existe :
- inventaire de départ chargé depuis `objets.json` ;
- inventaire conservé dans le slot de run ;
- menu d'objets en combat ;
- consommation d'objet ;
- application d'effet via `BattleItemService`.

### Histoire 11 — Récompenses d'étage

Ce qui existe :
- salles de récompense affichées par `RewardView` ;
- trois types de récompenses : objet, stats, attaque ;
- sélection du Bugémon cible pour les récompenses de stats ou d'attaque ;
- remplacement d'une attaque existante ;
- persistance après application.

### Histoire 13 — Ajouter des Bugémons

Ce qui existe :
- formulaire `BugemonCreatorView` ;
- validation du nom, du sprite et des attaques ;
- copie du sprite dans `images/` ;
- écriture dans `custom.json` ;
- rechargement du catalogue via `BugemonData.loadCustomBugemons()`.

### Histoire 14 — Exploration visuelle

Ce qui existe :
- `FloorMapView` affiche la carte d'étage ;
- `RoomView` affiche les salles avec icônes ;
- clic sur les salles adjacentes accessibles ;
- mise en évidence de la salle courante et des salles accessibles ;
- assombrissement des salles non accessibles ou déjà visitées ;
- branchement vers combat, récompense ou boss selon le type de salle.

### Histoire 17 — Arbre de compétences

Ce qui existe :
- `SkillTreeView` permet d'afficher l'arbre et d'allouer/retraiter des points ;
- `SkillTreeProgress` conserve les points disponibles et les niveaux alloués et est persisté dans `PlayerProfile` ;
- `SkillTreeBonuses` et `SkillEffect` calculent et appliquent les bonus globaux (statistiques d'équipe, multiplicateurs d'XP, régénération post-combat, objets de départ, modificateurs de critique et de dégâts par type, nombre de choix de montée de niveau) ;
- `SkillBonusApplicationService` applique ces bonus au démarrage ou au chargement d'une run ;
- les points sont gagnés après la victoire d'un boss et les allocations sont sauvées via `PlayerProfile.saveToDisk()`.

### Histoire 20 — Sauvegarder la progression de la tour NO

Ce qui existe :
- état de la tour sérialisé via `TowerNOState` et ré-hydraté en `TowerNO` (`TowerNOState.toTower()`) ;
- la progression de la tour (salles visitées, étage courant, boss vaincus) est conservée dans le `PlayerProfile` et persistée dans `teams_save.json` ;
- `PlayerProfile` gère la migration des anciens champs (ex. `towerLevel`, `towerFloor`) si nécessaire.

### Histoire 21 — Plusieurs difficultés

Ce qui existe :
- type `Difficulty` (ex. `EASY`, `NORMAL`, `HARD`) exposé à la création de run ;
- la difficulté choisie est stockée dans le `GameSlotData` et persistée dans le profil ;
- `EnemyTeamFactory` et la génération d'ennemis adaptent le niveau/forces des adversaires en fonction de la difficulté ;
- l'interface de sélection de difficulté est disponible lors de la création d'une nouvelle run.

### Fonctionnalités transversales ajoutées

Le dépôt actuel contient aussi plusieurs fonctionnalités au-delà du premier périmètre documenté :

- arbre de compétences permanent (`SkillTreeView`, `SkillTreeProgress`, `SkillTreeBonuses`) ;
- points de compétence gagnés après victoire contre un boss ;
- cinq emplacements de sauvegarde de run (`GameSlotData`, `RunSlotDTO`, `RunSlotService`) ;
- bouton `Continuer` et écran `Charger une partie` ;
- choix de difficulté (`EASY`, `NORMAL`, `HARD`) ;
- musique de menu, exploration, combat, victoire, boss, défaite et fin via `AudioManager`.

---

## 8. Points importants de l'implémentation

### 8.1. Séparation statique / dynamique

`BugemonDefinition` décrit une espèce de Bugémon. `BugemonInstance` décrit un individu dans une équipe ou un combat.

Cette séparation évite de modifier le catalogue global lorsqu'une run fait progresser un Bugémon.

### 8.2. Combat sur copie

`Battle` crée une copie profonde des équipes. Le combat modifie donc son propre état. La synchronisation vers l'équipe active se fait explicitement après victoire avec `TeamManagerController.syncBattleProgress(...)`.

### 8.3. Services plutôt que contrôleurs monolithiques

La logique a été extraite progressivement :
- navigation dans `NavigationController` ;
- cycle de vie de run dans `RunLifecycleController` ;
- combat applicatif dans `BattleFlowController` ;
- récompenses dans `RewardProvider` et `RewardApplicationService` ;
- progression de combat dans `GameStateService` ;
- bonus d'arbre dans `SkillBonusApplicationService`.

Cela garde `MainController` comme façade d'orchestration plutôt que comme classe contenant toute la logique.

### 8.4. DTO entre modèle et vue

Les vues utilisent des snapshots (`record`) au lieu de manipuler directement les objets métier.

Cela réduit le couplage, surtout pour :
- l'équipe ;
- le combat ;
- la carte d'étage ;
- les slots de sauvegarde ;
- l'arbre de compétences ;
- les récompenses.

### 8.5. Persistance unique du profil

`PlayerProfile` centralise la persistance locale dans `teams_save.json`.

Le fichier contient à la fois :
- les équipes ;
- les runs ;
- les inventaires ;
- la tour ;
- la difficulté ;
- l'arbre de compétences.

### 8.6. Données pilotées par JSON

Les Bugémons, attaques, objets et compétences sont data-driven. Ajouter ou modifier une grande partie du contenu passe par les JSON plutôt que par du code Java.

Les Bugémons personnalisés suivent le même principe via `custom.json`.
