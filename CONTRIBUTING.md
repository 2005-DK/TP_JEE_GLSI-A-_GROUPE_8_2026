# CONTRIBUTING

Ce dépôt est destiné aux travaux pratiques Java EE (GLSI A & B) 2025-2026.

Conventions de nommage du compte GitHub (obligatoire pour la soumission):
- `TP_JEE_GLSI<A|B>_GROUPE_<NumGroupe>_2026`
  - Exemple: `TP_JEE_GLSI_A_GROUPE_05_2026` ou `TP_JEE_GLSI_B_GROUPE_12_2026`

Règles de contribution:
- Chaque étudiant doit committer sous son propre compte GitHub.
- Utiliser des messages de commit clairs: `Feat: add account creation`, `Fix: handle validation`.
- Faire des commits atomiques et fréquents pour permettre la traçabilité.

Branches:
- `main` contient la version finale à soumettre.
- Travailler sur des branches de fonctionnalités: `feature/<nom>`.
- Ouvrir une Pull Request quand la fonctionnalité est prête.

Vérification des contributions:
- L'évaluation portera sur l'historique des commits (auteur, email, message).
- Assurez-vous que chaque membre du groupe a réalisé au moins un commit significatif.

Tests & CI:
- Les tests s'exécutent avec Maven: `./mvnw test` (Windows: `./mvnw.cmd test`).
- Le repository inclut un workflow GitHub Actions pour exécuter les tests et la collection Postman via Newman.

Soumission:
- Voir `SUBMISSION.md` pour les étapes de publication du dépôt.
