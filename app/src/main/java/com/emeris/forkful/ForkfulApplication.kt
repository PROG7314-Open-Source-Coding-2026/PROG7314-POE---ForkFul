package com.emeris.forkful

import android.app.Application
import android.content.Context
import android.util.Log
import com.emeris.forkful.core.network.NetworkModule
import com.emeris.forkful.core.session.SessionManager
import com.emeris.forkful.data.repository.AuthRepositoryImpl
import com.emeris.forkful.data.repository.GroceryRepositoryImpl
import com.emeris.forkful.data.repository.PantryRepositoryImpl
import com.emeris.forkful.data.repository.PreferencesRepositoryImpl
import com.emeris.forkful.data.repository.RecipeRepositoryImpl

/**
 * Manual dependency container (constructor injection without a DI framework,
 * keeping the Part 2 prototype simple and fully transparent).
 */
class AppContainer(context: Context) {

    val sessionManager: SessionManager = SessionManager(context)

    val api = NetworkModule.createApi(sessionManager)

    val authRepository = AuthRepositoryImpl(api, sessionManager)
    val recipeRepository = RecipeRepositoryImpl(api)
    val pantryRepository = PantryRepositoryImpl(api)
    val groceryRepository = GroceryRepositoryImpl(api)
    val preferencesRepository = PreferencesRepositoryImpl(api, sessionManager)
}

/**
 * Application entry point: registers the global activity lifecycle logger
 * required by the POE ("functional logging demonstrating understanding of
 * the application lifecycle and state transitions").
 */
class ForkfulApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        registerActivityLifecycleCallbacks(LifecycleLogger())
        Log.i(TAG, "ForkfulApplication created - DI container ready")
    }

    private class LifecycleLogger : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onCreate")
        }

        override fun onActivityStarted(activity: android.app.Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onStart")
        }

        override fun onActivityResumed(activity: android.app.Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onResume")
        }

        override fun onActivityPaused(activity: android.app.Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onPause")
        }

        override fun onActivityStopped(activity: android.app.Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onStop")
        }

        override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onSaveInstanceState")
        }

        override fun onActivityDestroyed(activity: android.app.Activity) {
            Log.i(TAG, "[LIFECYCLE] ${activity.javaClass.simpleName} -> onDestroy")
        }
    }

    companion object {
        private const val TAG = "ForkfulApp"
    }
}
