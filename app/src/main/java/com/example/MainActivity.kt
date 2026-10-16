package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CallLogItem
import com.example.data.model.ContactItem
import com.example.data.repository.TelecomRepository
import com.example.incall.InCallActivity
import com.example.telecom.CallManager
import com.example.telecom.CallUiStatus
import com.example.ui.components.DefaultDialerBanner
import com.example.ui.components.IosBottomBar
import com.example.ui.components.MainTab
import com.example.ui.screens.contacts.ContactsScreen
import com.example.ui.screens.contacts.ContactsViewModel
import com.example.ui.screens.details.CallDetailsScreen
import com.example.ui.screens.details.ContactDetailsScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.favorites.FavoritesViewModel
import com.example.ui.screens.keypad.KeypadScreen
import com.example.ui.screens.keypad.KeypadViewModel
import com.example.ui.screens.recents.RecentsScreen
import com.example.ui.screens.recents.RecentsViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.settings.ThemeMode
import com.example.ui.screens.voicemail.VoicemailScreen
import com.example.ui.theme.LocalIosColors
import com.example.ui.theme.MyApplicationTheme

sealed class Screen {
    data class Tabs(val tab: MainTab = MainTab.KEYPAD) : Screen()
    data class ContactDetails(val contact: ContactItem) : Screen()
    data class CallDetails(val callItem: CallLogItem) : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    private var pendingNumberFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        CallManager.init(applicationContext)

        handleDialIntent(intent)

        setContent {
            PhoneApp(pendingNumber = pendingNumberFromIntent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDialIntent(intent)
    }

    private fun handleDialIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "tel") {
            val raw = uri.schemeSpecificPart
            val sanitized = com.example.telecom.DialSanitizer.sanitizeExternalTelUri(raw)
            pendingNumberFromIntent = sanitized.cleanNumber
            if (sanitized.warningMessage != null) {
                Toast.makeText(this, sanitized.warningMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun PhoneApp(pendingNumber: String? = null) {
    val context = LocalContext.current
    val telecomRepo = remember { TelecomRepository(context) }

    var currentScreen by remember {
        mutableStateOf<Screen>(Screen.Tabs(if (pendingNumber != null) MainTab.KEYPAD else MainTab.RECENTS))
    }
    var currentTab by remember {
        mutableStateOf(if (pendingNumber != null) MainTab.KEYPAD else MainTab.RECENTS)
    }

    var isDefaultDialer by remember { mutableStateOf(telecomRepo.isDefaultDialer()) }

    // Role launcher for default dialer
    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialer = telecomRepo.isDefaultDialer()
    }

    // Permission launcher for required cellular dialer permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions updated
    }

    LaunchedEffect(Unit) {
        val requiredPerms = mutableListOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPerms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val ungranted = requiredPerms.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        }
    }

    // ViewModels
    val recentsViewModel: RecentsViewModel = viewModel(factory = RecentsViewModel.Factory(context))
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory(context))
    val contactsViewModel: ContactsViewModel = viewModel(factory = ContactsViewModel.Factory(context))
    val keypadViewModel: KeypadViewModel = viewModel(factory = KeypadViewModel.Factory(context))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(context))

    val settingsState by settingsViewModel.uiState.collectAsState()

    // Pass pending dialed number to keypad
    LaunchedEffect(pendingNumber) {
        if (!pendingNumber.isNullOrBlank()) {
            keypadViewModel.setNumber(pendingNumber)
            currentTab = MainTab.KEYPAD
            currentScreen = Screen.Tabs(MainTab.KEYPAD)
        }
    }

    // Listen to callState; if incoming or active, launch InCallActivity
    val callState by CallManager.callState.collectAsState()
    LaunchedEffect(callState.status) {
        if (callState.status == CallUiStatus.RINGING ||
            callState.status == CallUiStatus.ACTIVE ||
            callState.status == CallUiStatus.DIALING
        ) {
            val intent = Intent(context, InCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            context.startActivity(intent)
        }
    }

    val isDark = when (settingsState.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    fun requestDefaultDialerRole() {
        val intent = telecomRepo.createDefaultDialerIntent()
        if (intent != null) {
            roleLauncher.launch(intent)
        } else {
            Toast.makeText(context, "RoleManager not available on this OS version", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerPermissionRequest() {
        val requiredPerms = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG
        )
        permissionLauncher.launch(requiredPerms)
    }

    MyApplicationTheme(darkTheme = isDark) {
        val colors = LocalIosColors.current

        // Handle Back button for sub-screens
        BackHandler(enabled = currentScreen !is Screen.Tabs) {
            currentScreen = Screen.Tabs(currentTab)
        }

        Scaffold(
            bottomBar = {
                if (currentScreen is Screen.Tabs) {
                    IosBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            currentTab = tab
                            currentScreen = Screen.Tabs(tab)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Default dialer prompt banner on top if not yet set
                    if (currentScreen is Screen.Tabs && !isDefaultDialer) {
                        DefaultDialerBanner(
                            isDefaultDialer = isDefaultDialer,
                            onSetDefaultClicked = { requestDefaultDialerRole() }
                        )
                    }

                    // Screen Body
                    when (val screen = currentScreen) {
                        is Screen.Tabs -> {
                            when (screen.tab) {
                                MainTab.RECENTS -> {
                                    RecentsScreen(
                                        viewModel = recentsViewModel,
                                        onCallNumber = { number ->
                                            keypadViewModel.setNumber(number)
                                            keypadViewModel.onCallClicked { sims, num ->
                                                // SIM prompt handled in keypad
                                            }
                                        },
                                        onOpenCallDetails = { item ->
                                            currentScreen = Screen.CallDetails(item)
                                        },
                                        onRequestPermission = { triggerPermissionRequest() }
                                    )
                                }

                                MainTab.FAVORITES -> {
                                    FavoritesScreen(
                                        viewModel = favoritesViewModel,
                                        onCallNumber = { number ->
                                            telecomRepo.placeCall(number)
                                        },
                                        onAddFavoriteClicked = {
                                            currentTab = MainTab.CONTACTS
                                            currentScreen = Screen.Tabs(MainTab.CONTACTS)
                                        }
                                    )
                                }

                                MainTab.CONTACTS -> {
                                    ContactsScreen(
                                        viewModel = contactsViewModel,
                                        onContactClicked = { contact ->
                                            currentScreen = Screen.ContactDetails(contact)
                                        },
                                        onAddContactClicked = {
                                            val insertIntent = Intent(
                                                Intent.ACTION_INSERT,
                                                ContactsContract.Contacts.CONTENT_URI
                                            )
                                            context.startActivity(insertIntent)
                                        },
                                        onRequestPermission = { triggerPermissionRequest() }
                                    )
                                }

                                MainTab.KEYPAD -> {
                                    KeypadScreen(
                                        viewModel = keypadViewModel,
                                        onAddContactWithNumber = { number ->
                                            val insertIntent = Intent(
                                                Intent.ACTION_INSERT,
                                                ContactsContract.Contacts.CONTENT_URI
                                            ).apply {
                                                putExtra(ContactsContract.Intents.Insert.PHONE, number)
                                            }
                                            context.startActivity(insertIntent)
                                        }
                                    )
                                }

                                MainTab.VOICEMAIL -> {
                                    VoicemailScreen(
                                        onCallVoicemail = { vmNumber ->
                                            telecomRepo.placeCall(vmNumber)
                                        }
                                    )
                                }
                            }
                        }

                        is Screen.ContactDetails -> {
                            ContactDetailsScreen(
                                contact = screen.contact,
                                onBack = { currentScreen = Screen.Tabs(currentTab) },
                                onCallNumber = { number -> telecomRepo.placeCall(number) }
                            )
                        }

                        is Screen.CallDetails -> {
                            CallDetailsScreen(
                                item = screen.callItem,
                                onBack = { currentScreen = Screen.Tabs(currentTab) },
                                onCallNumber = { number -> telecomRepo.placeCall(number) },
                                onDeleteCall = { item -> recentsViewModel.deleteCallLog(item) }
                            )
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onRequestDefaultDialer = { requestDefaultDialerRole() },
                                onBack = { currentScreen = Screen.Tabs(currentTab) }
                            )
                        }
                    }
                }

                // Global Settings shortcut button floating top-right on tabs
                if (currentScreen is Screen.Tabs && currentTab == MainTab.KEYPAD) {
                    IconButton(
                        onClick = { currentScreen = Screen.Settings },
                        modifier = Modifier
                            .padding(top = 16.dp, end = 16.dp)
                            .testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
