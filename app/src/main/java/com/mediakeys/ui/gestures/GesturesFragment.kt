package com.mediakeys.ui.gestures

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mediakeys.R
import com.mediakeys.data.PreferencesManager
import com.mediakeys.databinding.FragmentGesturesBinding
import com.mediakeys.databinding.ItemGestureBinding

/**
 * Gestures screen — lets the user view and reassign gesture → action mappings.
 * Uses simple inline dialogs; no external adapter or RecyclerView needed
 * because the gesture count is fixed (4 standard + 1 experimental toggle).
 */
class GesturesFragment : Fragment() {

    private var _binding: FragmentGesturesBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager

    private val actionLabels by lazy {
        mapOf(
            PreferencesManager.ACTION_NEXT to getString(R.string.action_next),
            PreferencesManager.ACTION_PREV to getString(R.string.action_prev),
            PreferencesManager.ACTION_PLAY_PAUSE to getString(R.string.action_play_pause),
            PreferencesManager.ACTION_LAUNCH to getString(R.string.action_launch),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGesturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())
        bindGestureRows()
        bindExperimental()
    }

    override fun onResume() {
        super.onResume()
        bindGestureRows() // refresh if prefs changed elsewhere
    }

    // ── Binding ───────────────────────────────────────────────────────────────

    private fun bindGestureRows() {
        bindRow(
            item = binding.itemVolUpDouble,
            gestureName = getString(R.string.gesture_vol_up_double),
            currentAction = prefs.actionVolUpDouble,
            onPick = { prefs.actionVolUpDouble = it }
        )
        bindRow(
            item = binding.itemVolDownDouble,
            gestureName = getString(R.string.gesture_vol_down_double),
            currentAction = prefs.actionVolDownDouble,
            onPick = { prefs.actionVolDownDouble = it }
        )
        bindRow(
            item = binding.itemVolUpDown,
            gestureName = getString(R.string.gesture_vol_up_down),
            currentAction = prefs.actionVolUpThenDown,
            onPick = { prefs.actionVolUpThenDown = it }
        )
        bindRow(
            item = binding.itemVolDownUp,
            gestureName = getString(R.string.gesture_vol_down_up),
            currentAction = prefs.actionVolDownThenUp,
            onPick = { prefs.actionVolDownThenUp = it }
        )
    }

    private fun bindRow(
        item: ItemGestureBinding,
        gestureName: String,
        currentAction: String,
        onPick: (String) -> Unit
    ) {
        item.tvGestureName.text = gestureName
        item.tvGestureAction.text = actionLabels[currentAction] ?: currentAction

        item.btnEditGesture.setOnClickListener {
            showActionPicker(currentAction) { chosen ->
                onPick(chosen)
                bindGestureRows() // refresh all rows
            }
        }
    }

    private fun bindExperimental() {
        binding.switchExperimental.isChecked = prefs.experimentalGesturesEnabled
        binding.switchExperimental.setOnCheckedChangeListener { _, checked ->
            prefs.experimentalGesturesEnabled = checked
        }
    }

    // ── Picker dialog ─────────────────────────────────────────────────────────

    private fun showActionPicker(current: String, onChoose: (String) -> Unit) {
        val actions = actionLabels.keys.toTypedArray()
        val labels = actionLabels.values.toTypedArray()
        val currentIndex = actions.indexOf(current).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Assign action")
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                onChoose(actions[which])
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
