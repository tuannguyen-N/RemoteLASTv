# RemoteLASTv

An Android app for discovering TVs on your local network and using your phone as a remote. RemoteLASTv currently supports remote control for **LG webOS TVs** and can discover **Android TV** devices.

## Try the app

[**Download the latest APK**](https://github.com/tuannguyen-N/RemoteLASTv/releases/latest/download/RemoteLASTv.apk) · [Browse all releases](https://github.com/tuannguyen-N/RemoteLASTv/releases)

> The download link will work after an APK named `RemoteLASTv.apk` is uploaded to a GitHub Release. Android may ask you to allow installation from your browser or file manager.

## Features

- Discover LG webOS and Android TV devices on the same Wi-Fi network.
- Use a remote interface for LG webOS, including directional navigation, volume and channel controls, number keys, power, and media buttons.
- Switch between D-pad and touchpad navigation on supported LG TVs.
- Launch supported apps installed on an LG TV, including Netflix, YouTube, Prime Video, Disney+, Spotify, and Plex.
- Save and rename TVs, and reconnect to the last connected TV automatically.
- Customize remote behavior and display preferences.

**Compatibility note:** Android TV discovery is implemented, but Android TV remote controls are not currently registered in the app. Remote control and app launching are currently implemented for LG webOS.

## Requirements

- Android 7.0 (API 24) or later.
- Phone and TV connected to the same local Wi-Fi network.
- A compatible LG webOS TV for remote-control features.

## Build from source

1. Clone this repository and open it in Android Studio.
2. Let Gradle sync and download the required dependencies.
3. Run the `app` configuration on an Android device, or build a debug APK:

```bash
./gradlew assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack

Kotlin · Jetpack Compose · MVVM · Coroutines and Flow · Hilt · Room · DataStore · Connect SDK

## License

See the repository and dependency license terms before redistributing the app or its bundled libraries.
