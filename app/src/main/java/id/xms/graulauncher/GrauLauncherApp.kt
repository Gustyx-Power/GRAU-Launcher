package id.xms.graulauncher

import android.app.Application
import id.xms.graulauncher.data.repository.InstalledAppsRepository


class GrauLauncherApp : Application() {
    val installedAppsRepository: InstalledAppsRepository by lazy {
        InstalledAppsRepository(this)
    }
}
