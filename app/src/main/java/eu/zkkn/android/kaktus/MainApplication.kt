package eu.zkkn.android.kaktus

import android.app.Application


class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Preferences.init(this)
        NotificationHelper.init(this)
    }
}
