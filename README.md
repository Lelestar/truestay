# 🏠 TrueStay - Application de Gestion Locative

**TrueStay** est une application Android moderne de gestion locative qui met la **transparence** au cœur de la relation entre locataires et propriétaires.

---

## 📋 Table des Matières

1. [Vue d'ensemble](#-vue-densemble)
2. [Fonctionnalités Principales](#-fonctionnalités-principales)
3. [Architecture du Projet](#️-architecture-du-projet)
4. [Technologies Utilisées](#️-technologies-utilisées)
5. [Prérequis](#-prérequis)
6. [Installation et Configuration](#-installation-et-configuration)
7. [Structure du Projet](#-structure-du-projet)
8. [Comprendre l'Architecture MVVM + Clean Architecture](#-comprendre-larchitecture-mvvm--clean-architecture)
9. [Concepts Clés pour Débutants](#-concepts-clés-pour-débutants)
10. [Firebase et Backend](#-firebase-et-backend)
11. [Navigation et Écrans](#️-navigation-et-écrans)
12. [Composants UI Personnalisés](#-composants-ui-personnalisés)
13. [Gestion d'État](#-gestion-détat)
14. [Tests et Développement](#-tests-et-développement)
15. [Déploiement](#-déploiement)
16. [Contribution](#-contribution)
17. [Ressources Utiles](#-ressources-utiles)

---

## 🎯 Vue d'ensemble

TrueStay est conçue pour :

- **Locataires** : Rechercher des logements, gérer leurs locations actives, signer des états des lieux numériques, et laisser des avis détaillés sur les propriétés, immeubles et quartiers
- **Propriétaires** : Publier et gérer des annonces de logements, créer des locations, effectuer des états des lieux avec signatures numériques, et consulter les avis

L'application met l'accent sur la **transparence** et la **traçabilité** grâce à des fonctionnalités comme les états des lieux photographiés et signés, et un système d'avis multi-niveaux.

---

## ✨ Fonctionnalités Principales

### Pour les Locataires 🧑‍💼

- 🔍 **Recherche de logements** avec filtres avancés
- ⭐ **Système de favoris** pour sauvegarder les annonces intéressantes
- 📋 **Gestion des locations** actives et historiques
- 📋 **États des lieux d'entrée et de sortie** avec signature numérique
- 💬 **Système d'avis complet** sur 3 niveaux :
  - **Propriété** : état général, confort, conformité, rapport qualité/prix
  - **Immeuble** : entretien, sécurité, services
  - **Quartier** : transports, commodités, calme, sécurité, ambiance

### Pour les Propriétaires 🏢

- 📢 **Création et gestion d'annonces** de logements
- 🖼️ **Upload de photos** pour les annonces
- 📝 **États des lieux numériques** :
    - Inspection pièce par pièce
    - Ajout de photos et commentaires pour chaque élément
    - Signature numérique
    - Génération de PDF automatique
- 👥 **Gestion des locations** et des demandes
- 📊 **Consultation des avis** laissés par les locataires

### Fonctionnalités Communes 🔐

- 🔑 **Authentification sécurisée** avec Firebase Auth
- 👤 **Gestion de profil** utilisateur
- 🔔 **Notifications** (emails et push)
- 🌐 **Mode hors ligne** avec émulateurs Firebase pour le développement

---

## 🏗️ Architecture du Projet

TrueStay utilise une architecture moderne et recommandée pour Android : **MVVM (Model-View-ViewModel) + Clean Architecture**.

### Pourquoi cette architecture ?

Cette combinaison offre plusieurs avantages :

- ✅ **Séparation des responsabilités** : chaque partie du code a un rôle bien défini
- ✅ **Testabilité** : facile d'écrire des tests unitaires
- ✅ **Maintenabilité** : facile de modifier et faire évoluer le code
- ✅ **Scalabilité** : facile d'ajouter de nouvelles fonctionnalités
- ✅ **Réutilisabilité** : le code métier est indépendant d'Android

### Les 3 Couches Principales

```
┌─────────────────────────────────────────┐
│         PRESENTATION LAYER              │  ← UI (Compose), ViewModels, Navigation
│  (ce que l'utilisateur voit et touche) │
└─────────────────────────────────────────┘
              ↕️
┌─────────────────────────────────────────┐
│          DOMAIN LAYER                   │  ← Logique métier, Use Cases, Models
│   (les règles métier de l'application) │
└─────────────────────────────────────────┘
              ↕️
┌─────────────────────────────────────────┐
│           DATA LAYER                    │  ← Repositories, Data Sources, DTOs
│  (communication avec Firebase/API/BDD) │
└─────────────────────────────────────────┘
```

---

## 🛠️ Technologies Utilisées

### Android & Kotlin

- **Kotlin** : Langage de programmation moderne pour Android
- **Jetpack Compose** : Framework UI déclaratif, remplace l'XML classique
- **Android SDK** : Min SDK 24 (Android 7.0), Target SDK 36

### Architecture & Injection de Dépendances

- **Hilt / Dagger** : Injection de dépendances pour gérer automatiquement les instances
- **Navigation Compose** : Navigation entre les écrans
- **ViewModel & StateFlow** : Gestion d'état réactif

### Backend & Cloud

- **Firebase** :
  - **Authentication** : Gestion des utilisateurs
  - **Firestore** : Base de données NoSQL en temps réel
  - **Storage** : Stockage des images et fichiers
  - **Functions** : Fonctions serverless (TypeScript)
  - **Emulators** : Environnement de développement local

### Chargement d'Images

- **Coil** : Bibliothèque moderne pour charger et afficher les images

### Build & Dépendances

- **Gradle (Kotlin DSL)** : Système de build
- **Version Catalog** : Gestion centralisée des versions des dépendances

---

## 📦 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

### Obligatoire

1. **Android Studio** (dernière version stable)
   - [Télécharger Android Studio](https://developer.android.com/studio)

2. **JDK 11 ou supérieur**
   - Inclus avec Android Studio

3. **Node.js 22+** (pour Firebase Functions et émulateurs)
   - [Télécharger Node.js](https://nodejs.org/)

4. **Git**
   - [Télécharger Git](https://git-scm.com/)

### Optionnel

- Un appareil Android physique ou un émulateur Android configuré
- Un compte Firebase si vous voulez déployer en production

---

## 🚀 Installation et Configuration

### 1. Cloner le Projet

```bash
git clone https://github.com/Lelestar/truestay.git
cd truestay
```

### 2. Configurer Firebase

#### Option A : Utiliser les émulateurs Firebase (Recommandé pour le développement)

Les émulateurs Firebase permettent de développer localement sans compte Firebase :

```bash
# Installer les dépendances Node.js
npm install

# Télécharger le fichier `google-services.json` depuis Clickup (Ressources)
# Le placer dans `app/google-services.json`

# Lancer les émulateurs (avec les données de seed)
npm run emulators
```

Les émulateurs seront disponibles à :
- **Auth Emulator** : http://localhost:9099
- **Firestore Emulator** : http://localhost:8080
- **Storage Emulator** : http://localhost:9199
- **UI des Émulateurs** : http://localhost:4000

#### Option B : Utiliser Firebase Production

Si vous voulez utiliser Firebase en production :

1. Créer un projet Firebase sur [console.firebase.google.com](https://console.firebase.google.com)
2. Activer **Authentication** (Email/Password)
3. Activer **Firestore Database**
4. Activer **Storage**
5. Télécharger le fichier `google-services.json`
6. Le placer dans `app/google-services.json`
7. **⚠️ IMPORTANT** : Ne jamais commit ce fichier !

### 3. Ouvrir le Projet dans Android Studio

1. Ouvrir Android Studio
2. **File → Open** → Sélectionner le dossier `TrueStay`
3. Attendre la synchronisation Gradle (première fois peut prendre quelques minutes)

### 4. Configurer le Mode de Build

Dans `app/build.gradle.kts`, deux modes sont disponibles :

```kotlin
buildTypes {
    debug {
        buildConfigField("Boolean", "USE_EMULATORS", "true")  // Utilise les émulateurs
        buildConfigField("String", "EMULATOR_HOST", "\"10.0.2.2\"")  // Pour émulateur Android
    }
    release {
        buildConfigField("Boolean", "USE_EMULATORS", "false")  // Utilise Firebase Production
    }
}
```

- **`10.0.2.2`** : Pour émulateur Android (pointe vers localhost de votre machine)
- **`localhost`** : Pour appareil physique connecté à votre réseau local

### 5. Lancer l'Application

1. **Démarrer les émulateurs Firebase** (dans un terminal séparé) :
   ```bash
   npm run emulators
   ```

2. **Dans Android Studio** :
   - Sélectionner un appareil (émulateur ou physique)
   - Cliquer sur **Run** ▶️ (ou `Shift + F10`)

🎉 **L'application devrait se lancer !**

---

## 📁 Structure du Projet

Voici l'organisation du code source dans `app/src/main/java/ca/uqac/inf865/truestay/` :

```
truestay/
├── 📱 presentation/           # COUCHE PRÉSENTATION (UI & ViewModels)
│   ├── MainActivity.kt        # Point d'entrée de l'app
│   ├── theme/                 # Thème, couleurs, typographie, espacements
│   ├── navigation/            # Configuration de la navigation
│   ├── common/                # Composants UI réutilisables (boutons, cartes, etc.)
│   │   ├── components/
│   │   └── icons/
│   ├── shared/                # Écrans partagés locataires/propriétaires
│   │   ├── auth/              # Login, Register, ForgotPassword
│   │   ├── profile/           # Profil utilisateur
│   │   ├── property/          # Détails d'une propriété
│   │   ├── rental/            # Détails d'une location
│   │   └── inventory/         # États des lieux
│   ├── tenant/                # Écrans spécifiques aux locataires
│   │   ├── search/            # Recherche de logements
│   │   ├── favorites/         # Favoris
│   │   ├── rentals/           # Mes locations
│   │   └── review/            # Formulaire d'avis
│   └── landlord/              # Écrans spécifiques aux propriétaires
│       ├── property/          # Gestion des annonces
│       └── rental/            # Gestion des locations
│
├── 🎯 domain/                 # COUCHE DOMAINE (Logique métier)
│   ├── model/                 # Modèles métier (User, Property, Rental, etc.)
│   ├── repository/            # Interfaces des repositories
│   └── usecase/               # Use Cases (logique métier complexe)
│       ├── inventory/
│       └── review/
│
├── 💾 data/                   # COUCHE DATA (Accès aux données)
│   ├── model/                 # DTOs (Data Transfer Objects) pour Firebase
│   ├── repository/            # Implémentations des repositories
│   └── source/                # Sources de données (Firebase)
│       ├── AuthDataSource.kt
│       ├── FirestoreDataSource.kt
│       └── StorageDataSource.kt
│
├── 💉 di/                     # INJECTION DE DÉPENDANCES (Hilt)
│   ├── AppModule.kt
│   ├── FirebaseModule.kt      # Configuration Firebase & Émulateurs
│   └── RepositoryModule.kt
│
└── TrueStayApplication.kt     # Classe Application (point d'entrée Hilt)
```

### Autres Dossiers Importants

```
TrueStay/
├── app/src/main/res/          # Ressources Android
│   ├── drawable/              # Images et icônes vectorielles
│   ├── font/                  # Polices personnalisées
│   ├── values/
│   │   └── strings.xml        # Toutes les chaînes de caractères (FR)
│   └── xml/                   # Configuration réseau, backup, etc.
│
├── functions/                 # Firebase Cloud Functions (TypeScript)
│   └── src/
│       └── index.ts
│
├── emulator-seed-data/        # Données de test pour les émulateurs
│
├── scripts/                   # Scripts utilitaires
│
├── gradle/                    # Configuration Gradle
│   └── libs.versions.toml     # Catalogue de versions des dépendances
│
├── build.gradle.kts           # Configuration Gradle racine
├── firebase.json              # Configuration Firebase
├── firestore.rules            # Règles de sécurité Firestore
├── firestore.indexes.json     # Index Firestore
├── storage.rules              # Règles de sécurité Storage
└── CONTRIBUTING.md            # Guide de contribution (workflow Git)
```

---

## 🎓 Comprendre l'Architecture MVVM + Clean Architecture

Si vous découvrez Android ou ne connaissez pas cette architecture, voici une explication détaillée.

### Qu'est-ce que MVVM ?

**MVVM** = **Model - View - ViewModel**

```
┌──────────┐         ┌──────────────┐         ┌─────────┐
│   View   │ ←────── │  ViewModel   │ ←────── │  Model  │
│ (Compose)│  observe│ (StateFlow)  │  gets   │ (Domain)│
└──────────┘         └──────────────┘         └─────────┘
```

#### 1. **View (Vue)** - `presentation/*.Screen.kt`

C'est ce que l'utilisateur **voit et touche**. Dans TrueStay, les Vues sont écrites avec **Jetpack Compose**.

**Exemple** : `LoginScreen.kt`

```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    // La vue observe l'état du ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // La vue affiche l'état et envoie des événements au ViewModel
    Column {
        TextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) }  // ← Événement vers ViewModel
        )
        
        if (uiState.isLoading) {
            CircularProgressIndicator()  // ← Affiche l'état
        }
    }
}
```

**Responsabilités** :
- Afficher les données
- Capturer les interactions utilisateur
- **NE CONTIENT PAS** de logique métier

#### 2. **ViewModel** - `presentation/*ViewModel.kt`

C'est le **pont** entre la Vue et le Modèle. Il contient l'**état de l'UI** et gère les **événements utilisateur**.

**Exemple** : `LoginViewModel.kt`

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository  // ← Injecté par Hilt
) : ViewModel() {
    
    // État de l'UI (observable par la Vue)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // Fonction appelée par la Vue
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Appelle le Repository (Modèle)
            val result = authRepository.login(email, password)
            
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isLoading = false, isAuthenticated = true)
                } else {
                    it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                }
            }
        }
    }
}
```

**Responsabilités** :
- Gérer l'état de l'UI
- Réagir aux événements utilisateur
- Appeler les Repositories/Use Cases
- Transformer les données du domaine en données d'UI
- Survivre aux changements de configuration (rotation d'écran)

#### 3. **Model (Modèle)** - `domain/` et `data/`

Le Modèle représente les **données** et la **logique métier**.

**Exemple** : `User.kt` (modèle domaine)

```kotlin
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
)
```

### Qu'est-ce que Clean Architecture ?

**Clean Architecture** divise le code en **3 couches indépendantes** :

#### 🎨 **1. Presentation Layer** (Couche Présentation)

- **Contenu** : UI (Composables), ViewModels, Navigation
- **Dépend de** : Domain Layer
- **Rôle** : Afficher les données et capturer les interactions

**Fichiers** : Tout dans `presentation/`

#### 🎯 **2. Domain Layer** (Couche Domaine)

- **Contenu** : Modèles métier, Interfaces de Repositories, Use Cases
- **Dépend de** : Rien (totalement indépendante d'Android !)
- **Rôle** : Définir les règles métier de l'application

**Fichiers** : Tout dans `domain/`

**Exemple** : `domain/repository/AuthRepository.kt` (interface)

```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
}
```

**Exemple** : `domain/usecase/review/SubmitReviewUseCase.kt`

```kotlin
class SubmitReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val rentalRepository: RentalRepository
) {
    suspend operator fun invoke(review: Review): Result<String> {
        // Logique métier complexe :
        // 1. Vérifier que la location est terminée
        // 2. Vérifier qu'aucun avis n'existe déjà
        // 3. Sauvegarder l'avis
        // 4. Mettre à jour les ratings de la propriété
        // ...
    }
}
```

#### 💾 **3. Data Layer** (Couche Data)

- **Contenu** : Implémentations des Repositories, Data Sources, DTOs
- **Dépend de** : Domain Layer (implémente les interfaces)
- **Rôle** : Fournir les données (API, BDD, Firebase, etc.)

**Fichiers** : Tout dans `data/`

**Exemple** : `data/repository/AuthRepositoryImpl.kt`

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : AuthRepository {  // ← Implémente l'interface du Domain
    
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Appelle Firebase Auth
            val firebaseUser = authDataSource.login(email, password)
            
            // Récupère les données utilisateur depuis Firestore
            val userDto = firestoreDataSource.getUser(firebaseUser.uid)
            
            // Convertit le DTO en modèle domaine
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Flux de Données

Voici comment les données circulent dans l'application :

```
┌─────────────────────────────────────────────────────────────┐
│  User clicks "Login"                                        │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  LoginScreen (View) → viewModel.login(email, password)      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  LoginViewModel → authRepository.login()                    │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  AuthRepositoryImpl → authDataSource.login()                │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  Firebase Auth API                                          │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓ Result<User>
┌─────────────────────────────────────────────────────────────┐
│  AuthRepositoryImpl converts to Domain Model                │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  LoginViewModel updates uiState                             │
└─────────────────────────────────────────────────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────┐
│  LoginScreen recomposes with new state                      │
└─────────────────────────────────────────────────────────────┘
```

### Pourquoi DTOs et Domain Models ?

Vous remarquerez qu'il y a **deux types de modèles** :

#### **DTOs** (`data/model/*Dto.kt`)

- Représentent les données **telles qu'elles sont stockées dans Firebase**
- Ont des annotations Firebase (`@PropertyName`, etc.)
- Peuvent contenir des types Firebase (`Timestamp`, etc.)

**Exemple** : `UserDto.kt`

```kotlin
data class UserDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("first_name") val firstName: String = "",
    // ...
) {
    // Conversion vers le modèle domaine
    fun toDomain(): User = User(
        id = id,
        email = email,
        firstName = firstName,
        // ...
    )
}
```

#### **Domain Models** (`domain/model/*.kt`)

- Représentent les données **telles qu'elles sont utilisées dans l'application**
- Indépendants de Firebase ou toute autre technologie
- Peuvent avoir de la logique métier

**Exemple** : `User.kt`

```kotlin
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val role: UserRole
) {
    fun getFullName(): String = "$firstName $lastName"  // Logique métier
}
```

**Avantage** : Si vous changez de backend (Firebase → API REST), vous ne modifiez que la couche Data, pas la couche Domain ni Presentation !

---

## 🔰 Concepts Clés pour Débutants

### 1. Jetpack Compose (UI Déclarative)

Au lieu d'XML, TrueStay utilise **Jetpack Compose** pour créer l'interface.

#### Avant (XML + Code Java/Kotlin)

```xml
<!-- activity_main.xml -->
<TextView
    android:id="@+id/textView"
    android:text="Hello" />
```

```kotlin
// MainActivity.kt
val textView = findViewById<TextView>(R.id.textView)
textView.text = "Hello World"
```

#### Maintenant (Jetpack Compose)

```kotlin
@Composable
fun MyScreen() {
    Text(text = "Hello World")
}
```

**Avantages** :
- ✅ Moins de code
- ✅ Pas de `findViewById`
- ✅ UI réactive (se met à jour automatiquement quand les données changent)
- ✅ Prévisualisation en temps réel dans Android Studio

### 2. StateFlow (Gestion d'État Réactive)

`StateFlow` est un flux de données **observable**. Quand la valeur change, l'UI se met à jour automatiquement.

```kotlin
// Dans le ViewModel
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

fun updateEmail(email: String) {
    _uiState.update { it.copy(email = email) }  // ← Change la valeur
}

// Dans la View (Compose)
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()  // ← Observe
    
    Text(text = uiState.email)  // ← Se met à jour automatiquement !
}
```

### 3. Hilt (Injection de Dépendances)

**Injection de Dépendances** = Hilt crée automatiquement les instances dont vous avez besoin.

#### Sans Hilt (création manuelle)

```kotlin
class LoginViewModel {
    private val authDataSource = AuthDataSource(FirebaseAuth.getInstance())
    private val firestoreDataSource = FirestoreDataSource(FirebaseFirestore.getInstance())
    private val authRepository = AuthRepositoryImpl(authDataSource, firestoreDataSource)
}
```

#### Avec Hilt (automatique !)

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository  // ← Hilt l'injecte automatiquement !
) : ViewModel()
```

**Comment ça marche ?**

1. Vous définissez comment créer les instances dans des **Modules** (`di/`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
```

2. Hilt crée et injecte automatiquement les dépendances

**Avantages** :
- ✅ Moins de code boilerplate
- ✅ Facile de tester (on peut injecter des fakes/mocks)
- ✅ Gestion automatique du cycle de vie

### 4. Coroutines & Suspend Functions

Les **coroutines** permettent d'exécuter du code asynchrone (réseau, BDD) sans bloquer l'UI.

```kotlin
suspend fun login(email: String, password: String): Result<User> {
    // Cette fonction peut faire des appels réseau sans bloquer l'UI
    return authDataSource.login(email, password)
}

// Appel dans un ViewModel
viewModelScope.launch {  // ← Lance une coroutine
    val result = authRepository.login(email, password)  // ← Attend le résultat
}
```

**Mots-clés importants** :
- `suspend` : Fonction qui peut être suspendue (asynchrone)
- `launch` : Lance une coroutine
- `viewModelScope` : Scope lié au ViewModel (annulé quand le ViewModel est détruit)
- `await()` : Attend le résultat d'une Task Firebase

### 5. Navigation Compose

La navigation dans TrueStay est gérée par **Navigation Compose**.

**Définition des écrans** : `presentation/navigation/Screen.kt`

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Search : Screen("search")
    object PropertyDetails : Screen("property/{propertyId}") {
        fun createRoute(propertyId: String) = "property/$propertyId"
    }
}
```

**Configuration du NavHost** : `presentation/navigation/TrueStayNavigation.kt`

```kotlin
NavHost(navController = navController, startDestination = Screen.Login.route) {
    composable(Screen.Login.route) {
        LoginScreen(navController = navController)
    }
    composable(Screen.PropertyDetails.route) { backStackEntry ->
        val propertyId = backStackEntry.arguments?.getString("propertyId")
        PropertyDetailsScreen(propertyId = propertyId)
    }
}
```

**Navigation** :

```kotlin
// Naviguer vers un écran
navController.navigate(Screen.PropertyDetails.createRoute(propertyId))

// Retour arrière
navController.navigateUp()
```

---

## 🔥 Firebase et Backend

### Structure de la Base de Données (Firestore)

Firestore est une base de données **NoSQL** organisée en **collections** et **documents**.

```
Firestore
│
├── users/                          Collection
│   ├── {userId}/                   Document
│   │   ├── id: String
│   │   ├── email: String
│   │   ├── firstName: String
│   │   ├── role: String (tenant/landlord)
│   │   └── ...
│
├── properties/                     Collection
│   ├── {propertyId}/               Document
│   │   ├── id: String
│   │   ├── name: String
│   │   ├── landlordId: String
│   │   ├── address: Map
│   │   ├── rooms: Array
│   │   ├── photos: Array<String>
│   │   └── ...
│
├── rentals/                        Collection
│   ├── {rentalId}/                 Document
│   │   ├── id: String
│   │   ├── propertyId: String
│   │   ├── tenantId: String
│   │   ├── landlordId: String
│   │   ├── status: String
│   │   └── ...
│
├── inventories/                    Collection (états des lieux)
│   ├── {inventoryId}/              Document
│   │   ├── rentalId: String
│   │   ├── type: String (entry/exit)
│   │   ├── rooms: Array
│   │   ├── signatures: Map
│   │   └── ...
│
├── reviews/                        Collection
│   ├── {reviewId}/                 Document
│   │   ├── rentalId: String
│   │   ├── propertyId: String
│   │   ├── tenantId: String
│   │   ├── propertyReview: Map
│   │   ├── buildingReview: Map
│   │   ├── neighborhoodReview: Map
│   │   └── ...
│
└── favorites/                      Collection
    ├── {favoriteId}/               Document
        ├── userId: String
        ├── propertyId: String
        └── createdAt: Timestamp
```

### Règles de Sécurité Firestore

Les règles de sécurité sont définies dans `firestore.rules` :

```javascript
// Exemple : Seul le propriétaire peut modifier ses propriétés
match /properties/{propertyId} {
  allow read: if isAuthenticated();
  allow update: if resource.data.landlordId == request.auth.uid;
}
```

### Firebase Storage

Les images sont stockées dans **Firebase Storage** avec cette structure :

```
storage/
├── properties/
│   └── {propertyId}/
│       ├── photo1.jpg
│       └── photo2.jpg
├── inventories/
│   └── {inventoryId}/
│       └── {roomId}/
│           ├── element1.jpg
│           └── element2.jpg
├── signatures/
│   └── {userId}/
│       └── signature.png
└── profiles/
    └── {userId}/
        └── avatar.jpg
```

### Émulateurs Firebase

Pour le développement local, TrueStay utilise les **émulateurs Firebase** :

```bash
# Lancer les émulateurs avec les données de seed
npm run emulators

# Lancer les émulateurs sans données
npm run emulators:fresh

# Exporter les données actuelles
npm run emulators:export
```

**Configuration** : `firebase.json`

Les émulateurs permettent de :
- ✅ Développer sans compte Firebase
- ✅ Travailler hors ligne
- ✅ Tester sans coûts
- ✅ Réinitialiser facilement les données

**Accès automatique** : Le code détecte automatiquement les émulateurs en mode `debug` grâce à `BuildConfig.USE_EMULATORS` (voir `di/FirebaseModule.kt`).

---

## 🗺️ Navigation et Écrans

### Architecture de Navigation

TrueStay utilise une **navigation conditionnelle** basée sur le rôle de l'utilisateur :

```
┌─────────────────────────────────────────────────────────┐
│  Utilisateur non authentifié                            │
│  → Écrans d'authentification (Login, Register, etc.)    │
└─────────────────────────────────────────────────────────┘
                          │
                    Login réussi
                          │
                  ┌───────┴────────┐
                  │                │
        ┌─────────▼──────┐  ┌──────▼──────────┐
        │  Locataire     │  │  Propriétaire   │
        │  (TENANT)      │  │  (LANDLORD)     │
        └────────────────┘  └─────────────────┘
```

### Écrans par Rôle

#### 🔐 Écrans d'Authentification (Tous)

- `LoginScreen` : Connexion
- `RoleSelectionScreen` : Sélection du rôle
- `RegisterScreen` : Inscription (depend du rôle)
- `ForgotPasswordScreen` : Réinitialisation du mot de passe
- `ForgotPasswordEmailSentScreen` : Confirmation d'envoi d'email

#### 🧑‍💼 Écrans Locataire (TENANT)

**Navigation principale** (Bottom Bar) :
- `SearchScreen` : Recherche de logements
- `FavoritesScreen` : Favoris
- `RentalsScreen` : Mes locations

**Écrans secondaires** :
- `ReviewFormScreen` : Formulaire d'avis

#### 🏢 Écrans Propriétaire (LANDLORD)

**Navigation principale** (Bottom Bar) :
- `PropertiesScreen` : Mes annonces
- `RentalsScreen` : Mes locations

**Écrans secondaires** :
- `PropertyFormScreen` : Créer/Modifier une annonce
- `CreateRentalScreen` : Créer une location

#### 👥 Écrans Partagés (TENANT & LANDLORD)
- `ProfileScreen` : Profil utilisateur (Écran principal)
- `PropertyDetailsScreen` : Détails d'une propriété
- `RentalDetailsScreen` : Détails d'une location
- `InventoryScreen` : État des lieux
- `RoomDetailsScreen` : Inspection d'une pièce
- `SignatureScreen` : Signature de l'état des lieux

### Bottom Navigation

La barre de navigation inférieure (`TrueStayBottomBar`) change selon le rôle :

```kotlin
// presentation/navigation/BottomNavItem.kt
val tenantNavItems = listOf(
    BottomNavItem(Screen.Search.route, R.string.bottom_nav_search, TrueStayIcons.Search),
    BottomNavItem(Screen.Favorites.route, R.string.bottom_nav_favorites, TrueStayIcons.Heart),
    BottomNavItem(Screen.TenantRentals.route, R.string.bottom_nav_tenant_rentals, TrueStayIcons.Key),
    BottomNavItem(Screen.Profile.route, R.string.bottom_nav_profile, TrueStayIcons.User)
)

val landlordNavItems = listOf(
    BottomNavItem(Screen.Properties.route, R.string.bottom_nav_properties, TrueStayIcons.Home),
    BottomNavItem(Screen.LandlordRentals.route, R.string.bottom_nav_landlord_rentals, TrueStayIcons.Key),
    BottomNavItem(Screen.Profile.route, R.string.bottom_nav_profile, TrueStayIcons.User)
)
```

---

## 🎨 Composants UI Personnalisés

TrueStay utilise des composants UI réutilisables pour garantir la cohérence du design.

### Localisation : `presentation/common/components/`

### Composants Principaux

#### 1. **TrueStayButton**

Bouton personnalisé avec 3 variantes :

```kotlin
// Bouton primaire (bleu)
TrueStayButton(
    text = "Se connecter",
    onClick = { /* ... */ },
    variant = ButtonVariant.PRIMARY
)

// Bouton secondaire (blanc avec bordure)
TrueStayButton(
    text = "Annuler",
    onClick = { /* ... */ },
    variant = ButtonVariant.SECONDARY
)

// Bouton danger (rouge)
TrueStayButton(
    text = "Supprimer",
    onClick = { /* ... */ },
    variant = ButtonVariant.DANGER
)

// Avec loader
TrueStayButton(
    text = "Connexion...",
    onClick = { /* ... */ },
    isLoading = true
)
```

#### 2. **TrueStayTextField**

Champ de texte personnalisé :

```kotlin
TrueStayTextField(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    placeholder = "exemple@email.com",
    leadingIcon = TrueStayIcons.Email,
    keyboardType = KeyboardType.Email,
    error = "Email invalide"
)

// Champ de mot de passe
TrueStayTextField(
    value = password,
    onValueChange = { password = it },
    label = "Mot de passe",
    isPassword = true
)
```

#### 3. **TrueStayCard**

Carte personnalisée pour afficher du contenu :

```kotlin
TrueStayCard(
    onClick = { /* ... */ }
) {
    Column {
        Text(text = "Titre")
        Text(text = "Description")
    }
}
```

#### 4. **TrueStayIcon**

Icône personnalisée :

```kotlin
TrueStayIcon(
    iconRes = TrueStayIcons.Heart,
    contentDescriptionRes = R.string.cd_favorite,
    tint = LocalAppColors.current.primary
)
```

#### 5. **Autres Composants**

Actuellement, seulement une partie des composants necessaires ont été créés. D'autres composants réutilisables peuvent être ajoutés au fur et à mesure des besoins.

### Système de Design

#### Couleurs (`presentation/theme/Color.kt`)

TrueStay utilise un système de couleurs cohérent :

```kotlin
val colors = LocalAppColors.current

// Couleurs primaires
colors.primary           // Bleu principal
colors.primaryLight      // Bleu clair
colors.primarySurface    // Fond bleu très clair

// Couleurs de statut
colors.success           // Vert
colors.error             // Rouge
colors.warning           // Jaune
colors.info              // Bleu

// Nuances de gris
colors.grayLight
colors.grayMedium
colors.grayDark
```
Ces couleurs sont accessibles via `LocalAppColors.current` dans les Composables.

#### Espacements (`presentation/theme/Spacing.kt`)

Système d'espacement cohérent :

```kotlin
val spacing = AppSpacing

Column(
    modifier = Modifier.padding(spacing.medium),  // 16dp
    verticalArrangement = Arrangement.spacedBy(spacing.small)  // 8dp
) {
    // Contenu
}
```
Ces espacements sont accessibles via `AppSpacing` dans les Composables.

Valeurs disponibles :
- `spacing.tiny` = 4dp
- `spacing.small` = 8dp
- `spacing.medium` = 16dp
- `spacing.large` = 24dp
- `spacing.xl` = 32dp

#### Typographie (`presentation/theme/Type.kt`)

Police personnalisée : **Montserrat**

```kotlin
Text(
    text = "Titre",
    style = MaterialTheme.typography.headlineMedium
)

Text(
    text = "Corps de texte",
    style = MaterialTheme.typography.bodyMedium
)
```
Ces styles typographiques sont définis dans le thème Material, utilisables via `MaterialTheme.typography`.

---

## ⚡ Gestion d'État

### Pattern UiState

Chaque écran a un **UiState** qui représente l'état complet de l'UI :

```kotlin
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
```

### Mise à Jour de l'État

```kotlin
// Dans le ViewModel
private val _uiState = MutableStateFlow(LoginUiState())
val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

fun updateEmail(email: String) {
    _uiState.update { it.copy(email = email) }
}

fun login() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val result = authRepository.login(
            email = _uiState.value.email,
            password = _uiState.value.password
        )
        
        _uiState.update {
            if (result.isSuccess) {
                it.copy(isLoading = false, isAuthenticated = true)
            } else {
                it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
```

### Observation dans la Vue

```kotlin
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // L'UI se recompose automatiquement quand l'état change
    if (uiState.isLoading) {
        CircularProgressIndicator()
    }
    
    uiState.error?.let { error ->
        Text(text = error, color = MaterialTheme.colorScheme.error)
    }
}
```

### État Partagé (AuthViewModel)

Certains états sont partagés par toute l'application, comme l'authentification :

```kotlin
// presentation/shared/auth/AuthViewModel.kt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            _authState.value = if (result.isSuccess && result.getOrNull() != null) {
                AuthState.Authenticated(result.getOrNull()!!)
            } else {
                AuthState.Unauthenticated
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}
```

Cet état est observé dans `MainActivity` pour déterminer quelle navigation afficher.

Note: pour accéder facilement à l'utilisateur courant, une extension `rememberCurrentUser()` est définie et peut être utilisée dans les Composables.

---

## 🧪 Tests et Développement

### Lancer l'Application en Développement

1. **Démarrer les émulateurs Firebase** :
   ```bash
   npm run emulators
   ```

2. **Lancer l'app depuis Android Studio** :
   - Sélectionner un appareil/émulateur
   - Cliquer sur Run ▶️

3. **Accéder à l'UI des émulateurs** :
   - Ouvrir http://localhost:4000
   - Vous pouvez voir et modifier les données en temps réel

### Données de Test

Les émulateurs se lancent avec des données de seed (dans `emulator-seed-data/`) :

**Compte de test** :
```
Locataire :
- Email: test@example.com
- Password: 12345678
```
D'autres comptes et données pourront être ajoutés au besoin.

### Logs et Debugging

```kotlin
// Ajouter des logs
import android.util.Log

Log.d("TAG", "Message de debug")
Log.e("TAG", "Message d'erreur", exception)
```

**Voir les logs** :
- Android Studio → Logcat
- Filtrer par tag ou par package

### Modifier les Données Firebase

Pendant le développement avec les émulateurs :

1. Accéder à http://localhost:4000
2. Naviguer vers Firestore ou Auth
3. Modifier les données directement

Les données sont automatiquement exportées à l'arrêt des émulateurs (si lancés avec `npm run emulators`).

---

## 🚀 Déploiement

### Build de Production

1. **Configurer Firebase Production** :
   - Placer `google-services.json` dans `app/`
   - Dans `app/build.gradle.kts`, le build `release` utilise Firebase Production

2. **Créer un APK signé** :
   - Android Studio → Build → Generate Signed Bundle / APK
   - Sélectionner APK
   - Créer ou sélectionner une clé de signature
   - Choisir le build `release`

3. **Ou créer un APK en ligne de commande** :
   ```bash
   gradlew assembleRelease
   ```

L'APK sera dans `app/build/outputs/apk/release/`

### Déployer Firebase (Firestore Rules & Functions)

```bash
# Déployer les règles Firestore
firebase deploy --only firestore:rules

# Déployer les règles Storage
firebase deploy --only storage

# Déployer les Cloud Functions
firebase deploy --only functions

# Tout déployer
firebase deploy
```

---

## 🤝 Contribution

Pour contribuire au projet, veuillez consulter le fichier **[CONTRIBUTING.md](./CONTRIBUTING.md)** qui contient :

- Installation et config Firebase
- Workflow : branches, commits, PR
- Conventions de commits
- Erreurs à éviter
- Bonnes pratiques
- Dépannage et aide

---

## 📚 Ressources Utiles

### Documentation Officielle

- **Android** : https://developer.android.com/
- **Jetpack Compose** : https://developer.android.com/jetpack/compose
- **Kotlin** : https://kotlinlang.org/docs/home.html
- **Firebase** : https://firebase.google.com/docs
- **Hilt** : https://dagger.dev/hilt/

### Tutoriels Recommandés

#### Pour Débutants Android

1. **Android Basics with Compose** (Google) :
   - https://developer.android.com/courses/android-basics-compose/course
   - Cours officiel gratuit pour apprendre Jetpack Compose

2. **Kotlin for Beginners** :
   - https://kotlinlang.org/docs/getting-started.html
   - Apprendre les bases de Kotlin

#### Pour l'Architecture

3. **Guide to App Architecture** (Google) :
   - https://developer.android.com/topic/architecture
   - Architecture recommandée pour Android

4. **MVVM Clean Architecture in Android** :
   - https://medium.com/@anandgaur2207/mvvm-clean-architecture-in-android-be5ef3f05330
   - Comprendre MVVM avec Clean Architecture

#### Pour Firebase

5. **Firebase for Android** :
   - https://firebase.google.com/docs/android/setup
   - Intégration Firebase dans Android

### Concepts à Maîtriser

Pour bien comprendre TrueStay, familiarisez-vous avec :

1. **Kotlin** :
   - Data classes
   - Sealed classes
   - Extension functions
   - Coroutines (`suspend`, `launch`, `async`)
   - Flow & StateFlow

2. **Jetpack Compose** :
   - Composables (`@Composable`)
   - State (`remember`, `mutableStateOf`)
   - Side effects (`LaunchedEffect`, `DisposableEffect`)
   - Navigation Compose

3. **Architecture** :
   - MVVM pattern
   - Repository pattern
   - Use Cases
   - Dependency Injection (Hilt)

4. **Firebase** :
   - Authentication
   - Firestore (NoSQL)
   - Storage
   - Security Rules

### Outils Utiles

- **Android Studio** : IDE officiel
- **Logcat** : Voir les logs de l'application
- **Layout Inspector** : Inspecter la hiérarchie de l'UI
- **Firebase Emulator UI** : Gérer les données de test

---

## 🐛 Résolution des Problèmes

### L'application ne se connecte pas aux émulateurs

**Problème** : L'app utilise Firebase Production au lieu des émulateurs

**Solution** :
1. Vérifier que les émulateurs sont lancés (`npm run emulators`)
2. Vérifier que vous êtes en mode `debug` (pas `release`)
3. Vérifier `BuildConfig.USE_EMULATORS` dans le code
4. Si vous utilisez un appareil physique, changer `EMULATOR_HOST` en `"localhost"` dans `app/build.gradle.kts`

### Erreur de synchronisation Gradle

**Problème** : Gradle ne peut pas télécharger les dépendances

**Solution** :
1. Vérifier votre connexion Internet
2. Android Studio → File → Invalidate Caches → Restart
3. Supprimer `.gradle/` et relancer la synchro

### Erreur "google-services.json manquant"

**Problème** : Le fichier de configuration Firebase est absent

**Solution** :
- Télécharger le fichier depuis Clickup et le placer dans `app/`

### L'UI ne se met pas à jour

**Problème** : Changement d'état ne déclenche pas de recomposition

**Solution** :
1. Vérifier que vous utilisez `StateFlow` ou `State`
2. Vérifier que vous observez avec `collectAsStateWithLifecycle()` ou `collectAsState()`
3. Vérifier que vous mettez à jour l'état avec `.update { }` ou `.value =`

---

## 📝 License

Ce projet est un projet éducatif développé dans le cadre d'un cours universitaire.

---

## 👥 Auteurs

- **Développeurs** : Léonard Zipper, Pierre Bourgey, Samuel Midete
- **Cours** : 8INF865 - UQAC
- **Année** : 2025

---

## 📞 Contact et Support

Pour toute question :

1. Consulter ce README
2. Consulter le fichier [CONTRIBUTING.md](./CONTRIBUTING.md)
3. Regarder les issues GitHub
4. Envoyer un message sur Clickup dans # Discussion