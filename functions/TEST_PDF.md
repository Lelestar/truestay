# Test de génération PDF en local

Ce guide explique comment tester la génération PDF localement sans redéployer sur Firebase.

## Prérequis

- Node.js installé
- Firebase CLI installé et configuré (`firebase login`)
- Credentials Firebase configurés (voir ci-dessous)
- Un inventoryId valide dans votre base de données Firestore

## Configuration des credentials (première fois seulement)

### Option 1 : Utiliser les credentials de l'application par défaut (recommandé)

```bash
# Connectez-vous avec Firebase CLI (si pas déjà fait)
firebase login

# Configurez les credentials par défaut
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\Users\<votre-username>\.config\gcloud\application_default_credentials.json"

# OU utilisez gcloud (si installé)
gcloud auth application-default login
```

### Option 2 : Utiliser un service account key

1. Allez dans la [Console Firebase](https://console.firebase.google.com/)
2. Sélectionnez votre projet
3. Allez dans **Project Settings** (⚙️) → **Service Accounts**
4. Cliquez sur **Generate new private key**
5. Téléchargez le fichier JSON
6. Placez-le dans `functions/service-account.json`
7. Configurez la variable d'environnement :

```bash
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\Users\leona\AndroidStudioProjects\TrueStay\functions\service-account.json"
```

**⚠️ Important** : Ajoutez `service-account.json` dans `.gitignore` pour ne pas le commiter !

## Utilisation

### 1. Trouver un inventoryId

Dans la console Firebase Firestore, allez dans la collection `inventories` et copiez l'ID d'un inventaire qui a :
- Les deux signatures (landlord et tenant)
- Le statut `signed` ou `completed`

### 2. Générer le PDF localement

```bash
cd functions
npm run test:pdf -- <inventoryId>
```

Exemple :
```bash
npm run test:pdf -- Vxwhs2UFwdFW3YFH44Qr
```

### 3. Voir le résultat

Le PDF sera généré dans le dossier `functions/` avec le nom `test-output-<inventoryId>.pdf`

Vous pouvez l'ouvrir directement pour vérifier :
- Le type d'inventaire (Entrée/Sortie)
- Les images des éléments
- Les signatures
- La mise en page générale

## Workflow de développement recommandé

1. **Modifier** le code de génération PDF dans `src/inventory.ts`
2. **Tester** avec `npm run test:pdf -- <inventoryId>`
3. **Vérifier** le PDF généré
4. **Itérer** jusqu'à satisfaction
5. **Déployer** avec `npm run deploy` seulement quand c'est parfait

## Dépannage

### Erreur "Inventory not found"
- Vérifiez que l'inventoryId existe dans Firestore
- Vérifiez que vous êtes connecté au bon projet Firebase

### Erreur "Both signatures must be present"
- L'inventaire doit avoir les deux signatures
- Vérifiez les champs `landlordSignature` et `tenantSignature` dans Firestore

### PDF vide ou incomplet
- Vérifiez que toutes les données liées existent (rental, property, users)
- Regardez les logs dans la console pour voir les erreurs éventuelles

## Notes

- Le script télécharge les vraies images depuis Firebase Storage
- Il utilise les vraies données de votre base Firestore
- Le PDF généré est identique à celui qui sera généré en production
