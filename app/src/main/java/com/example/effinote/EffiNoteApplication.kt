package com.example.effinote

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.effinote.data.repository.DefaultTaskRepository
import com.example.effinote.debug.DebugInitDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EffiNoteApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        DefaultTaskRepository.init(this)
        DebugInitDataLoader.loadIfNeeded(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                appScope.launch(Dispatchers.IO) {
                    DefaultTaskRepository.instance.checkAndRollPeriod()
                }
            }
        })
    }
}
