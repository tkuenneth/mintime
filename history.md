# History

### 1.7.0
- Optimizations for ChromeOS
- Added Open app button to the notifications

### 1.6.9
- Library and tools upgrades
- Added Cancel button to the notifications

### 1.6.7
- Targets Android 15
- Supports tabletop mode on foldable devices

### 1.6.4
- Targets Android 13
- General improvements regarding performance and stability

### 1.6.3
- A few descriptions added
- Libraries updated

### 1.6.2
- Tools and library upgrades
- Fixed a bug that prevented the user from opening notification settings
- Inform user to check settings if notifications are off or silent

### 1.6.1
- Added "Notification channel settings" to options menu
- Fixed a condition where the Start button could obstruct display of total time

### 1.6.0
- Android Studio project, tools, and library upgrades
- targets api level 32
- optimized for foldables and large screens

### 1.5.0
- fixed some Lint warnings
- added a *Finish* button
- *0 - 0 - 0* can no longer be saved
- switched to View binding
- switched to single activity
- added wake lock for vibrating while idle
- vibrate works again when app is in background

### 1.4.1
- `targetSdkVersion` set to 30
- `minSdkVersion` set to 23
- some library upgrades
- fixed version: `null`
- Fixed some Lint warnings

### 1.4
- German translation
- Use native SeekBar for faster time selection
- Code cleanup

### 1.3
- Code cleanup
- Updated to api level 29

### 1.2a
- *Clear* icon added

### 1.2
- taps on *+* and *-* are immediately recognized
- targets the latest api level
- improved notification
- save and delete icons added
- tap and swipe to directly set a time between 1 and 90 minutes

### 1.06
- converted to Android Studio project
- switched to Marshmallow

### 1.05
- notifications added

### 1.04
- runs on Froyo (api level 8) and later

### 1.03
- when switching from green to orange and red the device vibrates
- bugfix: total time was not updated after menu selection

### 1.02
- icons for higher resolutions had incorrect filenames
- abbreviations for minute and second conform to standards
- time distributions can be saved
- initial delay for *+* and *-* increased to 200 milliseconds: current values are no longer modified through accidental screen taps

### 1.01
- minor bugfix: current settings were not saved before the countdown was started