# Histoires et estimation

### Histoire 1 : Constituer une équipe

Le joueur peut constituer une équipe de 1 à 6 Bugémons via une interface affichant au minimum le nom et l’image de chaque Bugémon. Il doit pouvoir ajouter et retirer des Bugémons facilement. Un même Bugémon ne peut pas être sélectionné plusieurs fois et cela doit être signalé visuellement.

- **Priorité client** : 1
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 1
- **Statut** : Finalisée
- **Points** : 0

### Histoire 2 : Sauvegarder et charger une équipe

Le joueur doit pouvoir sauvegarder une équipe déjà constituée en lui donnant un nom. Depuis le menu de constitution d’équipe, il doit aussi pouvoir recharger une équipe sauvegardée précédemment.

- **Priorité client** : 2
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 1
- **Statut** : Finalisée
- **Points** : 0

### Histoire 3 : Modifier équipes sauvegardées

Le joueur doit pouvoir modifier, supprimer et renommer les équipes sauvegardées depuis un menu dédié. Depuis ce menu, il peut aussi lancer une partie avec une équipe existante ou créer une nouvelle équipe à partir de zéro.

- **Priorité client** : 2
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 1
- **Statut** : Finalisée
- **Points** : 0

### Histoire 4 : Combat automatique

Au lancement d’une nouvelle partie, un combat est automatiquement lancé entre l’équipe du joueur et une équipe adverse générée aléatoirement avec le même nombre de Bugémons. Le combat se déroule de manière semi-automatique jusqu’à la victoire ou la défaite.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 1
- **Statut** : Finalisée
- **Points** : 0

### Histoire 5 : Contrôle des actions en combat

Le joueur peut contrôler les actions de ses Bugémons pendant le combat. À chaque tour, il peut attaquer, changer de Bugémon, utiliser un objet si disponible ou abandonner. Lorsqu’un Bugémon du joueur est K.O., le remplacement doit être choisi par le joueur.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 1
- **Statut** : Finalisée
- **Points** : 0

### Histoire 6 : Calcul de dégâts avec statistiques

Le calcul des dégâts utilise la formule complète du cahier des charges, en tenant compte de l’attaque, de la défense, de l’efficacité de type, des coups critiques et de l’initiative. L’interface doit aussi afficher les informations utiles sur les types et l’efficacité des attaques.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 2
- **Statut** : Finalisée
- **Points** : 0

### Histoire 7 : Expérience et montée de niveau

Les Bugémons gagnent de l’expérience après chaque combat remporté. Lorsqu’un seuil est atteint, ils montent de niveau, récupèrent tous leurs PV et bénéficient d’un bonus de statistiques choisi par le joueur parmi plusieurs options.

- **Priorité client** : 1
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 2
- **Statut** : Finalisée
- **Points** : 0

### Histoire 8 : Statuts

Le jeu doit gérer les effets de statut liés aux attaques, notamment les soins et les réductions de statistiques décrits dans les fichiers de données et le cahier des charges.

- **Priorité client** : 1
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 2
- **Statut** : Finalisée
- **Points** : 0

### Histoire 9 : Structure de la Tour NO

La Tour NO est composée d’étages suivant une structure fixe alternant combats, récompenses et boss d’étage. Les équipes adverses sont générées aléatoirement avec autant de Bugémons que l’équipe du joueur.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 2
- **Statut** : Finalisée
- **Points** : 0

### Histoire 10 : Utiliser un objet

Un dresseur doit pouvoir utiliser un objet en combat. Le joueur commence chaque nouvelle partie avec un inventaire de base et l’utilisation d’un objet consomme le tour du dresseur.

- **Priorité client** : 1
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 2
- **Statut** : Finalisée
- **Points** : 0

### Histoire 11 : Récompenses d'étage

Après certains combats, le joueur obtient une récompense parmi trois choix possibles : objet de combat, nouvelle attaque pour un Bugémon ou bonus permanent de statistiques pour un Bugémon de l’équipe.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 3
- **Statut** : Finalisée
- **Points** : 0

### Histoire 12 : Bugédex

Au départ, seuls les trois starters sont disponibles. Lorsqu’un Bugémon est vaincu, il est ajouté au Bugédex et devient sélectionnable pour les parties suivantes, avec déblocage progressif de ses attaques.

- **Priorité client** : 2
- **Risque développeurs** : 3
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 20

### Histoire 13 : Ajouter des Bugémons

Le joueur peut ajouter de nouveaux Bugémons au jeu via une interface dédiée, en fournissant leurs caractéristiques, les attaques qu’ils peuvent apprendre et les sprites nécessaires à leur affichage.

- **Priorité client** : 2
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 3
- **Statut** : Finalisée
- **Points** : 0

### Histoire 14 : Exploration visuelle des étages

La progression linéaire dans la Tour NO est remplacée par une carte visuelle où le joueur choisit son parcours entre différentes salles : combats, bonus et boss. Les salles visitées et la position actuelle doivent être visibles.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 3
- **Statut** : Finalisée
- **Points** : 0

### Histoire 15 : Animations de combat

Le jeu doit intégrer des animations simples pendant les combats : mouvement d’attaque, clignotement lors de la réception de dégâts, baisse visible de la barre de PV et disparition progressive lors d’un K.O.

- **Priorité client** : 3
- **Risque développeurs** : 1
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 40

### Histoire 16 : Animations de déplacement en mode exploration

En mode exploration, les déplacements entre salles doivent être animés de manière fluide. Les salles visitées doivent changer visuellement d’état une fois atteintes.

- **Priorité client** : 3
- **Risque développeurs** : 2
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 30

### Histoire 17 : Arbre de compétences

Le joueur dispose d’un arbre de compétences permanent accessible depuis le menu principal. Les points gagnés persistent entre les runs, peuvent être redistribués librement et appliquent leurs bonus au début de chaque nouvelle partie.

- **Priorité client** : 2
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 4
- **Statut** : Finalisée
- **Points** : 0

### Histoire 18 : Jouer à la manette

Le joueur peut choisir d’utiliser le clavier et la souris, ou bien une manette pour jouer.

- **Priorité client** : 3
- **Risque développeurs** : 3
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 15

### Histoire 19 : Génération procédurale des étages

La structure fixe des étages est remplacée par une génération procédurale différente à chaque run, sur une grille 5×5 avec un point de départ central, plusieurs branches et un boss clairement identifiable.

- **Priorité client** : 2
- **Risque développeurs** : 1
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 45

### Histoire 20 : Sauvegarder sa progression, et continuer

Le jeu sauvegarde automatiquement la progression du joueur après chaque action importante. Le bouton « Continuer » recharge la dernière sauvegarde, tandis que « Nouvelle Partie » réinitialise la progression complète.

- **Priorité client** : 2
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 4
- **Statut** : Finalisée
- **Points** : 0

### Histoire 21 : Choisir un niveau de difficulté

Lorsque le joueur lance une nouvelle partie, il peut choisir un niveau de difficulté. Plus la difficulté est élevée, plus les adversaires sont coriaces.

- **Priorité client** : 2
- **Risque développeurs** : 2
- **Introduite dans l'itération** : 4
- **Statut** : Finalisée
- **Points** : 0

### Histoire 22 : Intelligence artificielle de combat

L’adversaire contrôlé par l’ordinateur doit s’adapter au contexte du combat afin de choisir des actions pertinentes à chaque tour, y compris l’usage d’objets et le changement de Bugémon.

- **Priorité client** : 2
- **Risque développeurs** : 1
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 30

### Histoire 23 : Générer une équipe dynamiquement

Chaque étage de la tour doit être généré dynamiquement en fonction de la difficulté choisie, tout en tenant compte des types de Bugémons présents dans l’équipe du joueur.

- **Priorité client** : 2
- **Risque développeurs** : 2
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 30

### Histoire 24 : Musique d'ambiance

La musique du jeu varie selon le contexte : création d’équipe, combat ou exploration.

- **Priorité client** : 3
- **Risque développeurs** : 3
- **Introduite dans l'itération** : 4
- **Statut** : Finalisée
- **Points** : 0

### Histoire 25 : Défier un autre joueur

En mode multijoueur, un joueur peut défier un autre joueur connecté. Si le défi est accepté, le combat se lance.

- **Priorité client** : 3
- **Risque développeurs** : 2
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 80

### Histoire 26 : Chat

En mode multijoueur, un salon de discussion permet aux joueurs de communiquer entre eux.

- **Priorité client** : 1
- **Risque développeurs** : 2
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 20

### Histoire 27 : Filtrer les messages inappropriés

Dans le chat, les messages inappropriés doivent être adoucis en remplaçant les lettres des mots concernés par des astérisques, sauf la première et la dernière lettre.

- **Priorité client** : 3
- **Risque développeurs** : 3
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 5

### Histoire 28 : Leaderboard

En mode multijoueur, chaque victoire rapporte un point au joueur. Un classement permet ensuite de comparer les scores obtenus par les différents joueurs.

- **Priorité client** : 1
- **Risque développeurs** : 3
- **Introduite dans l'itération** : /
- **Statut** : À compléter
- **Points** : 10
