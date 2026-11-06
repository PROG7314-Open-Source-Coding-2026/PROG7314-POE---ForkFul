package com.emeris.forkful

/**
 * Forkful - Application Entry Point & Manual Dependency Injection Container
 * Module: PROG7314 Portfolio of Evidence (POE)
 * Group Members:
 * - Jamie Abrahams (ST10434047)
 * - Isabelle Devlin (ST10445500)
 * - Justin Fussell (ST10280758)
 * - Matteo Nusca (ST10440432)
 *
 * Initialises global application state, configures logging callbacks, notification channels, and manual DI tree.
 */

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.emeris.forkful.core.network.NetworkModule
import com.emeris.forkful.core.notification.NotificationHelper
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.data.local.LocalPantryRepository
import com.emeris.forkful.data.repository.AuthRepositoryImpl
import com.emeris.forkful.data.repository.GroceryRepositoryImpl
import com.emeris.forkful.data.repository.PantryRepositoryImpl
import com.emeris.forkful.data.repository.PreferencesRepositoryImpl
import com.emeris.forkful.data.repository.RecipeRepositoryImpl

//Manual DI container
class AppContainer(context: Context) {

    val sessionManager: SessionManager = SessionManager(context)

    val api = NetworkModule.createApi(sessionManager) {
        sessionManager.expireSession()
    }

    val preferencesRepository = PreferencesRepositoryImpl(api, sessionManager)
    val authRepository = AuthRepositoryImpl(api, sessionManager, preferencesRepository)
    val recipeRepository = RecipeRepositoryImpl(api)
    val pantryRepository = PantryRepositoryImpl(api)
    val groceryRepository = GroceryRepositoryImpl(api)
    val localPantryRepository = LocalPantryRepository(context)
}

//App entry point
class ForkfulApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createNotificationChannel(this)
        registerActivityLifecycleCallbacks(LifecycleLogger())
        Log.i(TAG, "ForkfulApplication created - DI container ready")
    }

    private class LifecycleLogger : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onCreate")
        }

        override fun onActivityStarted(activity: Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onStart")
        }

        override fun onActivityResumed(activity: Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onResume")
        }

        override fun onActivityPaused(activity: Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onPause")
        }

        override fun onActivityStopped(activity: Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onStop")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onSaveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onDestroy")
        }
    }

    companion object {
        private const val TAG = "ForkfulApp"
    }
}

//------------------------------End-Of-File------------------------------//
