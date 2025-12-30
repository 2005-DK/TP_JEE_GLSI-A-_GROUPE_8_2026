# SUBMISSION - Instructions

Étapes pour soumettre votre travail (par groupe de 3):

1. Créez le compte GitHub du groupe avec le nom suivant :
   `TP_JEE_GLSI<A|B>_GROUPE_<NumGroupe>_2026`

2. Poussez le dépôt local vers le remote du compte de groupe :

```bash
# depuis le répertoire du projet
git remote add origin https://github.com/TP_JEE_GLSI_A_GROUPE_05_2026/ega-bank-api.git
git branch -M main
git push -u origin main
```

3. Vérifiez l'historique des commits pour vous assurer que chaque membre a contribué :

```bash
git log --pretty="%h %an %ae %s" --since="2025-09-01"
```

4. Indiquez dans votre rapport (README.md) les identifiants GitHub des trois membres du groupe.

5. Nom du fichier à rendre (optionnel) : archive ZIP du projet ou lien vers le dépôt GitHub.

Remarques importantes :
- Pour que la contribution soit prise en compte, chaque étudiant doit utiliser son propre compte GitHub pour committer.
- Si vous préférez me donner les droits d'accès, ajoutez l'enseignant en tant que collaborateur.

Bon travail et bonne soumission !

---

## Checklist de soumission (recommandée)

1. Vérifier le nom du dépôt : doit respecter `TP_JEE_GLSI<A|B>_GROUPE_<NumGroupe>_2026`.

2. Vérifier les contributions des membres :

```bash
# affiche les commits depuis la date de début du semestre
git log --pretty="%h %an %ae %s" --since="2025-09-01"
```

3. Ajouter la section `Authors` dans le `README.md` avec les trois identifiants GitHub.

4. Créer un tag pour la version à rendre et pousser le tag :

```bash
git tag -a v1.0-submission -m "Submission for GLSI group <NumGroupe>"
git push origin v1.0-submission
```

5. (Optionnel) Créer une release sur GitHub et joindre une archive ZIP du projet.

6. Vérifier que `CONTRIBUTING.md` et `SUBMISSION.md` sont présents à la racine et que `README.md` contient les instructions pour lancer l'application et exécuter les tests.

7. Envoyer le lien du dépôt (et le tag) au professeur via la plateforme demandée.

## Commandes utiles CI / artefacts

- Pour produire un ZIP du projet (local) :

```bash
zip -r ega-bank-api-submission.zip . -x "target/*" ":.git/*"
```

- Pour lister les commits par auteur (vérification rapide) :

```bash
git shortlog -sne --since="2025-09-01"
```

Si vous voulez, je peux créer le tag `v1.0-submission` pour vous et pousser le tag (indiquez le numéro du groupe et le message de tag). 
