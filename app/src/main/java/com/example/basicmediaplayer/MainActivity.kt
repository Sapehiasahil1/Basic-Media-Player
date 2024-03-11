package com.example.basicmediaplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.basicmediaplayer.player.service.MediaAudioService
import com.example.basicmediaplayer.ui.audio.AudioViewModel
import com.example.basicmediaplayer.ui.audio.HomeScreen
import com.example.basicmediaplayer.ui.audio.UIEvents
import com.example.basicmediaplayer.ui.theme.BasicMediaPlayerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false


    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicMediaPlayerTheme {
                // A surface container using the 'background' color from the theme
                val permissionState = rememberPermissionState(
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE
                )
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if(event == Lifecycle.Event.ON_RESUME) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        progress = viewModel.progress,
                        onProgress = {viewModel.onUiEvents((UIEvents.SeekTo(it)))},
                        isAudioPlaying = viewModel.isPlaying,
                        currentPlayingAudio = viewModel.currentSelectedAudio,
                        audiList = viewModel.audioList,
                        onStart = {
                                  viewModel.onUiEvents(UIEvents.PlayPause)
                        },
                        onItemClick = {
                            viewModel.onUiEvents(UIEvents.SelectedAudioChange(it))
                            startService()
                        },
                        onNext = {
                            viewModel.onUiEvents(UIEvents.SeekToNext)
                        }
                    )
                }
            }
        }
    }

    private fun startService() {
        if(!isServiceRunning) {
            val intent = Intent(this,MediaAudioService::class.java)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            }
            isServiceRunning = true
        }
    }
}

