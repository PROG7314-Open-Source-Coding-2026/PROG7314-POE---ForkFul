package com.emeris.forkful.core.i18n

/**
 * Forkful - Localisation & Multilingual String Provider
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Provides dynamic runtime string translation dictionaries across English, Afrikaans, and isiZulu.
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

data class ForkfulStrings(
    // Bottom Bar / Nav
    val navExplore: String,
    val navPantry: String,
    val navMatch: String,
    val navBasket: String,
    val navSettings: String,
    val navRecipeBox: String,

    // Common Actions
    val save: String,
    val cancel: String,
    val delete: String,
    val back: String,
    val done: String,
    val retry: String,
    val search: String,
    val loading: String,

    // Explore Screen
    val exploreTitle: String,
    val exploreSubtitle: String,
    val exploreSearchPlaceholder: String,
    val moodItalian: String,
    val moodComfort: String,
    val moodHealthy: String,
    val moodPantry: String,
    val moodSweets: String,

    // Pantry Screen
    val pantryTitle: String,
    val pantryAddButton: String,
    val pantryOfflineBanner: String,
    val pantrySyncedAt: String,
    val pantryExpiringSoon: String,
    val pantryEmptyTitle: String,
    val pantryEmptySubtitle: String,
    val pantryItemAdded: String,
    val pantryItemUpdated: String,

    // Recipe & Cooking
    val startCooking: String,
    val markAsCooked: String,
    val ingredients: String,
    val instructions: String,
    val prepTime: String,
    val cookTime: String,
    val calories: String,
    val protein: String,
    val difficulty: String,
    val step: String,
    val nextStep: String,
    val previousStep: String,
    val finishCooking: String,

    // Grocery Basket
    val basketTitle: String,
    val basketShareWhatsApp: String,
    val basketClearAll: String,
    val basketEmpty: String,

    // Settings Screen
    val settingsTitle: String,
    val settingsPreferences: String,
    val settingsLanguage: String,
    val settingsDarkMode: String,
    val settingsNotifications: String,
    val settingsBiometricLock: String,
    val settingsLogOut: String,
    val settingsResetTaste: String,

    // Biometric & Lock
    val lockTitle: String,
    val lockSubtitle: String,
    val lockUnlockButton: String,
    val lockLogoutButton: String
)

val EnglishStrings = ForkfulStrings(
    navExplore = "Explore",
    navPantry = "Pantry",
    navMatch = "Match",
    navBasket = "Basket",
    navSettings = "Settings",
    navRecipeBox = "Recipe Box",

    save = "Save",
    cancel = "Cancel",
    delete = "Delete",
    back = "Back",
    done = "Done",
    retry = "Retry",
    search = "Search",
    loading = "Loading...",

    exploreTitle = "What are you cooking?",
    exploreSubtitle = "Discover delicious recipes tailored to your taste",
    exploreSearchPlaceholder = "Search recipes, ingredients...",
    moodItalian = "Italian night",
    moodComfort = "Comfort food",
    moodHealthy = "Something healthy",
    moodPantry = "Use up your pantry",
    moodSweets = "Sweet tooth",

    pantryTitle = "Pantry Inventory",
    pantryAddButton = "Add Item",
    pantryOfflineBanner = "Offline - viewing cached pantry",
    pantrySyncedAt = "Synced at",
    pantryExpiringSoon = "Expiring Soon",
    pantryEmptyTitle = "Your pantry is empty",
    pantryEmptySubtitle = "Add ingredients to discover matching recipes",
    pantryItemAdded = "added to your pantry",
    pantryItemUpdated = "updated",

    startCooking = "Start Cooking",
    markAsCooked = "Mark as Cooked",
    ingredients = "Ingredients",
    instructions = "Instructions",
    prepTime = "Prep",
    cookTime = "Cook",
    calories = "Calories",
    protein = "Protein",
    difficulty = "Difficulty",
    step = "Step",
    nextStep = "Next Step",
    previousStep = "Previous Step",
    finishCooking = "Finish Cooking",

    basketTitle = "Grocery Basket",
    basketShareWhatsApp = "Share list via WhatsApp",
    basketClearAll = "Clear Checked",
    basketEmpty = "Your basket is empty",

    settingsTitle = "Settings",
    settingsPreferences = "Preferences",
    settingsLanguage = "App language",
    settingsDarkMode = "Dark mode",
    settingsNotifications = "Notifications",
    settingsBiometricLock = "Biometric lock (Final POE)",
    settingsLogOut = "Log Out",
    settingsResetTaste = "Reset Taste Profiles",

    lockTitle = "Forkful is Locked",
    lockSubtitle = "Biometric authentication is required to access your kitchen inventory, pantry, and saved recipes.",
    lockUnlockButton = "Unlock with Biometrics",
    lockLogoutButton = "Log Out of Account"
)

// Official South African Language 1: isiZulu
val ZuluStrings = ForkfulStrings(
    navExplore = "Hlola",
    navPantry = "Indlu Yokudla",
    navMatch = "Fanisa",
    navBasket = "Ubhasikidi",
    navSettings = "Izilungiselelo",
    navRecipeBox = "Ibhokisi Leresiphi",

    save = "Londoloza",
    cancel = "Khansela",
    delete = "Sula",
    back = "Emuva",
    done = "Kwenziwe",
    retry = "Zama futhi",
    search = "Sesha",
    loading = "Iyalayisha...",

    exploreTitle = "Uphekani namhlanje?",
    exploreSubtitle = "Thola amaresiphi amnandi ahambisana nokuthandayo",
    exploreSearchPlaceholder = "Sesha amaresiphi, izithako...",
    moodItalian = "Ubusuku base-Italy",
    moodComfort = "Ukudla okumnandi",
    moodHealthy = "Okunempilo",
    moodPantry = "Sebenzisa okusepantry",
    moodSweets = "Okumnandi",

    pantryTitle = "Impahla Yasepantry",
    pantryAddButton = "Faka Isithako",
    pantryOfflineBanner = "Akukho nethiwekhi - ubuka ipantry egciniwe",
    pantrySyncedAt = "Kuvumelaniswe ngo",
    pantryExpiringSoon = "Kuphelelwa Yisikhathi Maduze",
    pantryEmptyTitle = "Indlu yakho yokudla ayinalutho",
    pantryEmptySubtitle = "Faka izithako ukuze uthole amaresiphi afanayo",
    pantryItemAdded = "kufakwe epantry yakho",
    pantryItemUpdated = "kubuyekeziwe",

    startCooking = "Qala Ukupheka",
    markAsCooked = "Maka njengokuphekiwe",
    ingredients = "Izithako",
    instructions = "Imiyalelo",
    prepTime = "Ukulungisa",
    cookTime = "Ukupheka",
    calories = "Amakhalori",
    protein = "Iprotheni",
    difficulty = "Ubulukhuni",
    step = "Isinyathelo",
    nextStep = "Isinyathelo Esilandelayo",
    previousStep = "Isinyathelo Esedlule",
    finishCooking = "Qeda Ukupheka",

    basketTitle = "Ubhasikidi Wokuthenga",
    basketShareWhatsApp = "Yabelana nge-WhatsApp",
    basketClearAll = "Sula Okukhethiwe",
    basketEmpty = "Ubhasikidi wakho awunalutho",

    settingsTitle = "Izilungiselelo",
    settingsPreferences = "Okukhethwayo",
    settingsLanguage = "Ulimi lohlelo lokusebenza",
    settingsDarkMode = "Imodi emnyama",
    settingsNotifications = "Izaziso",
    settingsBiometricLock = "Ukhiye wesigxivizo somunwe (POE)",
    settingsLogOut = "Phuma",
    settingsResetTaste = "Setha Kabusha Iphrofayela Yokunambitha",

    lockTitle = "I-Forkful Ikhiyiwe",
    lockSubtitle = "Kudingeka ukufakazela ubuwena ukuze ungene ekhishini lakho nasemaresiphini.",
    lockUnlockButton = "Vula Ngesigxivizo",
    lockLogoutButton = "Phuma ku-akhawunti"
)

// Official South African Language 2: Afrikaans
val AfrikaansStrings = ForkfulStrings(
    navExplore = "Verken",
    navPantry = "Spens",
    navMatch = "Pasmaak",
    navBasket = "Mandjie",
    navSettings = "Instellings",
    navRecipeBox = "Resepteboks",

    save = "Stoor",
    cancel = "Kanselleer",
    delete = "Verwyder",
    back = "Terug",
    done = "Klaar",
    retry = "Probeer weer",
    search = "Soek",
    loading = "Laai tans...",

    exploreTitle = "Wat kook jy vandag?",
    exploreSubtitle = "Ontdek heerlike resepte volgens jou smaak",
    exploreSearchPlaceholder = "Soek resepte, bestanddele...",
    moodItalian = "Italiaanse aand",
    moodComfort = "Trooskos",
    moodHealthy = "Iets gesonds",
    moodPantry = "Gebruik jou spens",
    moodSweets = "Soettand",

    pantryTitle = "Spensvoorraad",
    pantryAddButton = "Voeg Item By",
    pantryOfflineBanner = "Vanlyn - bekyk gestoorde spens",
    pantrySyncedAt = "Gesinchroniseer om",
    pantryExpiringSoon = "Verval Binnekort",
    pantryEmptyTitle = "Jou spens is leeg",
    pantryEmptySubtitle = "Voeg bestanddele by om bypassende resepte te vind",
    pantryItemAdded = "by jou spens gevoeg",
    pantryItemUpdated = "opgedateer",

    startCooking = "Begin Kook",
    markAsCooked = "Merk as Gekook",
    ingredients = "Bestanddele",
    instructions = "Instruksies",
    prepTime = "Voorbereiding",
    cookTime = "Kooktyd",
    calories = "Kalorieë",
    protein = "Proteïen",
    difficulty = "Moeilikheidsgraad",
    step = "Stap",
    nextStep = "Volgende Stap",
    previousStep = "Vorige Stap",
    finishCooking = "Klaar Gekook",

    basketTitle = "Kruideniersmandjie",
    basketShareWhatsApp = "Deel lys via WhatsApp",
    basketClearAll = "Verwyder Gemerkte",
    basketEmpty = "Jou mandjie is leeg",

    settingsTitle = "Instellings",
    settingsPreferences = "Voorkeure",
    settingsLanguage = "Toepassingstaal",
    settingsDarkMode = "Donkermodus",
    settingsNotifications = "Kennisgewings",
    settingsBiometricLock = "Biometriese slot (Finale POE)",
    settingsLogOut = "Teken Uit",
    settingsResetTaste = "Herstel Smaakprofiele",

    lockTitle = "Forkful is Gesluit",
    lockSubtitle = "Biometriese verifikasie word vereis om toegang tot jou spens en resepte te kry.",
    lockUnlockButton = "Ontsluit met Biometrie",
    lockLogoutButton = "Teken Uit van Rekening"
)

fun getForkfulStrings(languageKey: String): ForkfulStrings = when (languageKey) {
    "zu" -> ZuluStrings
    "af" -> AfrikaansStrings
    else -> EnglishStrings
}

val LocalForkfulStrings = compositionLocalOf { EnglishStrings }

@Composable
fun currentStrings(): ForkfulStrings = LocalForkfulStrings.current

//------------------------------End-Of-File------------------------------//
