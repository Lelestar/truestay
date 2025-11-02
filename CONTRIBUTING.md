# Guide de contribution

## 🚀 Première fois

### 1. Cloner le projet
```bash
git clone https://github.com/Lelestar/truestay.git
cd truestay
```

### 2. Configurer Firebase

1. Recupérer le fichier `google-services.json` sur Clickup (Ressources)
2. Le placer dans `app/google-services.json`
3. **NE JAMAIS** commit ce fichier

### 3. Installer les dépendances
```bash
npm install
```

### 4. Ouvrir dans Android Studio

File → Open → Sélectionner le dossier

## 💻 Workflow quotidien

### Démarrer une nouvelle feature
```bash
# 1. Mettre à jour main
git checkout main
git pull origin main

# 2. Créer une branche
git checkout -b feature/ma-feature
```

### Développer
```bash
# 1. Lancer les émulateurs Firebase (dans un terminal séparé)
npm run emulators

# 2. Lancer l'app Android (dans Android Studio)
Run → Run 'app'

# 3. Développer votre feature

# 4. Commit régulièrement
git add .
git commit -m "feat: add button to login screen"
```

### Soumettre pour review
```bash
# 1. Pousser votre branche
git push origin feature/ma-feature

# 2. Sur GitHub :
#    - Aller dans "Pull Requests"
#    - Cliquer "New Pull Request"
#    - Sélectionner votre branche
#    - Remplir le template
#    - Assigner Léonard comme reviewer
#    - Cliquer "Create Pull Request"

# 3. Attendre la review et les commentaires

# 4. Si des changements sont demandés :
#    - Faire les changements
#    - Commit
#    - Push (la PR se met à jour automatiquement)
```

## Conventions de Commits

TrueStay utilise les **Conventional Commits** :

```bash
feat: ajouter la recherche par ville
fix: corriger le crash au login
docs: mettre à jour le README
style: formater le code
refactor: extraire la logique de validation
test: ajouter des tests pour AuthRepository
```

## ❌ À ne JAMAIS faire

- ❌ Push directement sur `main`
- ❌ Commit `google-services.json`
- ❌ Commit des fichiers générés (`build/`, `.idea/`, etc.)
- ❌ Merge votre propre PR sans review

## ✅ Bonnes pratiques

- ✅ Faire des commits petits et fréquents
- ✅ Écrire des messages de commit clairs
- ✅ Tester avant de push
- ✅ Répondre aux commentaires de review
- ✅ Demander de l'aide si bloqué

## 🆘 Problèmes courants

### "Failed to push"
```bash
# Mettre à jour votre branche
git pull origin feature/ma-feature --rebase
git push origin feature/ma-feature
```

### "Merge conflict"
```bash
# Mettre à jour main
git checkout main
git pull origin main

# Retourner sur votre branche et rebase
git checkout feature/ma-feature
git rebase main

# Résoudre les conflits dans Android Studio
# Puis :
git add .
git rebase --continue
git push origin feature/ma-feature --force
```

### "Build failed"
```bash
# Clean et rebuild
./gradlew clean
./gradlew assembleDebug
```

## 📞 Besoin d'aide ?

- Clickup : Poster un message dans le chat du projet (# Discussion)
- Issues GitHub avec le label `question`