package id.xms.graulauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import id.xms.graulauncher.ui.minimal.MinimalLayout
import id.xms.graulauncher.ui.theme.GrauLauncherTheme
import id.xms.graulauncher.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Manual DI: get repository from the Application class
        val app = application as GrauLauncherApp
        val viewModelFactory = LauncherViewModel.Factory(
            repository = app.installedAppsRepository,
            appContext = applicationContext
        )

        setContent {
            GrauLauncherTheme {
                val launcherViewModel: LauncherViewModel = viewModel(factory = viewModelFactory)
                val filteredApps by launcherViewModel.filteredApps.collectAsState()
                val searchQuery by launcherViewModel.searchQuery.collectAsState()
                val isLoading by launcherViewModel.isLoading.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color.Black
                ) { innerPadding ->
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading apps...", color = androidx.compose.ui.graphics.Color.DarkGray)
                        }
                    } else if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No apps found", color = androidx.compose.ui.graphics.Color.DarkGray)
                        }
                    } else {
                        MinimalLayout(
                            apps = filteredApps,
                            onAppClick = launcherViewModel::launchApp,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LauncherPreview() {
    GrauLauncherTheme {
        Text("Grau Launcher")
    }
}
