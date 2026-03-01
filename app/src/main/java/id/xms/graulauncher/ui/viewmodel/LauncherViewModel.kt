package id.xms.graulauncher.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.xms.graulauncher.data.model.AppInfo
import id.xms.graulauncher.data.repository.InstalledAppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class LauncherViewModel(
    private val repository: InstalledAppsRepository,
    private val appContext: Context
) : ViewModel() {


    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())


    /** The current search query string. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** True while the initial app list is being loaded. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** The full, unfiltered list of installed apps (sorted alphabetically). */
    val allApps: StateFlow<List<AppInfo>> = _allApps.asStateFlow()

   
    val filteredApps: StateFlow<List<AppInfo>> = combine(
        _allApps,
        _searchQuery
    ) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.label.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        observeApps()
    }

    // ----- Public actions (called by the UI) -----

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onClearSearch() {
        _searchQuery.value = ""
    }

    fun launchApp(packageName: String) {
        val launchIntent = appContext.packageManager
            .getLaunchIntentForPackage(packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        launchIntent?.let { appContext.startActivity(it) }
    }

    // ----- Private helpers -----
    private fun observeApps() {
        viewModelScope.launch {
            repository.observeInstalledApps().collect { apps ->
                _allApps.value = apps
                _isLoading.value = false
            }
        }
    }

    // ----- ViewModelProvider.Factory for manual DI -----
    class Factory(
        private val repository: InstalledAppsRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
                return LauncherViewModel(repository, appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
