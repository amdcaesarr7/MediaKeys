package com.mediakeys.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mediakeys.R
import com.mediakeys.data.PreferencesManager
import com.mediakeys.databinding.FragmentSettingsBinding

/**
 * Settings screen — allows the user to customize gesture timing,
 * toggle boot startup, and select the preferred media app fallback.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupTimingSlider()
        setupBootToggle()
        setupMediaAppSelector()
        setupAccessibilityButton()
        setupBatteryButton()
    }

    private fun setupTimingSlider() {
        val current = prefs.doubleTapWindowMs
        binding.sliderTiming.value = current.toFloat()
        updateTimingLabel(current)

        binding.sliderTiming.addOnChangeListener { _, value, _ ->
            val ms = value.toInt()
            prefs.doubleTapWindowMs = ms
            prefs.sequenceWindowMs = ms // Keep them in sync for simplicity
            updateTimingLabel(ms)
        }
    }

    private fun updateTimingLabel(ms: Int) {
        binding.tvTimingValue.text = getString(R.string.timing_window_value_format, ms, "ms")
    }

    private fun setupBootToggle() {
        binding.switchBoot.isChecked = prefs.startOnBoot
        binding.switchBoot.setOnCheckedChangeListener { _, isChecked ->
            prefs.startOnBoot = isChecked
        }
    }

    private fun setupMediaAppSelector() {
        // Map UI buttons to package names
        val appMap = mapOf(
            R.id.btn_rvx_music to PreferencesManager.APP_RVX_MUSIC,
            R.id.btn_youtube_music to PreferencesManager.APP_YOUTUBE_MUSIC,
            R.id.btn_revanced_music to PreferencesManager.APP_REVANCED_MUSIC,
            R.id.btn_spotify to PreferencesManager.APP_SPOTIFY
        )

        // Set initial selection
        val currentApp = prefs.preferredMediaApp
        val initialButtonId = appMap.entries.find { it.value == currentApp }?.key ?: R.id.btn_youtube_music
        binding.toggleMediaApp.check(initialButtonId)

        binding.toggleMediaApp.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                appMap[checkedId]?.let { pkg ->
                    prefs.preferredMediaApp = pkg
                }
            }
        }
    }

    private fun setupAccessibilityButton() {
        binding.btnOpenAccessibilitySettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun setupBatteryButton() {
        binding.btnBatterySettings.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general battery settings if direct request fails
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
