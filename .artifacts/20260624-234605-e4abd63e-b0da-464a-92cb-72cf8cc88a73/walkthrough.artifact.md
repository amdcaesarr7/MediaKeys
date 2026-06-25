# MediaKeys: Gesture Polish & App Selector

I have refined the gesture logic to eliminate confusion and added a custom app selector so you can choose exactly which music app the fallback should launch.

## 🛠️ Gesture Refinement (The "Chord Priority" Fix)
Previously, the app would sometimes process a chord (like Vol Up + Down) and then immediately process a sequence (like a double-tap) because it didn't clear the buffer properly.
- **Fixed Confusion**: I've implemented a "Chord Lock." If the app detects a simultaneous press, it immediately kills all pending sequences. It won't let another gesture trigger until you release all buttons.
- **Removed Camera Conflict**: I've **removed the Power Double-Tap to Home** gesture. This was fighting with the Android system setting that opens the camera.
- **Navigation Update**: Use **Power + Vol Down** to go Home reliably.

## 📱 Custom Fallback App Selector
You can now choose your preferred music app in the **Settings** tab:
1.  Go to the **Settings** screen.
2.  Under **Preferred Media App**, choose between **YT Music**, **ReVanced**, or **Spotify**.
3.  When a "Launch" gesture is triggered or music is inactive, the app will explicitly target your choice.

---

## 📋 Updated Gesture Reference

| Sequence / Chord | Action | UI Feedback |
| :--- | :--- | :--- |
| **Vol Up + Vol Down** | **Play / Pause** | `Chord Triggered: play_pause` |
| **UP, UP** | **Next Track** | `Executing Action: next` |
| **DOWN, DOWN** | **Prev Track** | `Executing Action: prev` |
| **Power + Vol Down** | **Go Home** | `Chord Triggered: home` |
| **Power + Vol Up** | **Launch Preferred** | `Chord Triggered: launch` |

---

## 🛠️ Mandatory Setup for Tab A7
Ensure these commands are run via ADB to keep the services active:

```bash
# Grant Accessibility (Home + Power logic)
adb shell settings put secure enabled_accessibility_services com.mediakeys/.service.MediaKeyAccessibilityService

# Grant Notification Access (Media eyes)
adb shell cmd notification allow_listener com.mediakeys/com.mediakeys.service.MediaNotificationListenerService

# Disable Battery Optimization (Critical for background)
adb shell am set-standby-bucket com.mediakeys active
```
