package com.mediakeys.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mediakeys.R
import com.mediakeys.data.PreferencesManager
import com.mediakeys.databinding.FragmentHomeBinding
import com.mediakeys.util.Logger

/**
 * Modern Dark Home — featuring real-time Button Stream and Action Status.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupToggle()
        setupButtons()
        setupDebugConsole()
    }

    override fun onResume() {
        super.onResume()
        syncUi()
        Logger.onLogUpdate = {
            activity?.runOnUiThread {
                updateLogs()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Logger.onLogUpdate = null
    }

    private fun setupToggle() {
        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isAccessibilityGranted()) {
                binding.switchEnable.isChecked = false
                showSetupBanner(true)
                return@setOnCheckedChangeListener
            }
            prefs.serviceEnabled = isChecked
            showSetupBanner(!isAccessibilityGranted())
            syncStatus()
        }
    }

    private fun setupButtons() {
        binding.btnOpenAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        
        binding.btnCopyLogs.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("MediaKeys Logs", Logger.getLogs())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Logs copied", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearLogs.setOnClickListener {
            Logger.clear()
            binding.tvLastAction.text = "Waiting for gesture..."
        }
    }

    private fun setupDebugConsole() {
        binding.tvLogs.movementMethod = ScrollingMovementMethod()
        updateLogs()
    }

    private fun syncUi() {
        val granted = isAccessibilityGranted()
        binding.switchEnable.isChecked = prefs.serviceEnabled && granted
        showSetupBanner(!granted)
        syncStatus()
    }

    private fun syncStatus() {
        val running = prefs.serviceEnabled && isAccessibilityGranted()
        binding.tvStatus.text = if (running) "Active" else "Stopped"
        val dotColor = requireContext().getColor(if (running) R.color.neon_accent else R.color.status_inactive)
        binding.statusDot.background.setTint(dotColor)

        // Show currently active media source if any
        val mediaHelper = com.mediakeys.media.MediaSessionHelper(requireContext(), prefs)
        val activePkg = mediaHelper.getActiveMediaApp()
        if (activePkg != null) {
            val appName = when (activePkg) {
                PreferencesManager.APP_RVX_MUSIC -> "YT Music (RVX)"
                PreferencesManager.APP_YOUTUBE_MUSIC -> "YouTube Music"
                PreferencesManager.APP_REVANCED_MUSIC -> "YT Music (ReVanced)"
                PreferencesManager.APP_SPOTIFY -> "Spotify"
                else -> activePkg.substringAfterLast('.')
            }
            Logger.d("Active Media Source: $appName")
        }
    }

    private fun updateLogs() {
        val logs = Logger.getLogs()
        binding.tvLogs.text = logs
        
        // Extract last action for the status card
        val lastLine = logs.split("\n").lastOrNull { it.contains("Executing Action:") || it.contains("Chord Triggered:") }
        if (lastLine != null) {
            binding.tvLastAction.text = lastLine.substringAfter("Action: ").substringAfter("Triggered: ")
        }

        // Auto-scroll
        val scrollAmount = binding.tvLogs.layout?.let { 
            it.lineCount * binding.tvLogs.lineHeight - binding.tvLogs.height 
        } ?: 0
        if (scrollAmount > 0) binding.tvLogs.scrollTo(0, scrollAmount)
    }

    private fun showSetupBanner(show: Boolean) {
        binding.cardSetup.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun isAccessibilityGranted(): Boolean {
        val enabledServices = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(requireContext().packageName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
