# Changelog

## 0.4.0

- Updated the native iOS and Android Estimote Proximity SDKs to 1.0.0.

- Updated the plugin to compile against Android 26 SDK, and target the Android API level 26. This is to [match the latest React Native version, 0.56][0.4.0-1].

  - The bundled "example" app was also updated to React Native 0.56.

  - Since the Android plugin now targets API level 26, the background-scanning notification now requires to be assigned to a notification channel. You specify the channel ID and name in the `config` object, next to the notification's title, text, and icon, for example:

    ```
    const config = {
      notification: {
        title: "Exploration mode is on",
        text: "We'll notify you when you're next to something interesting.",
        icon: 'ic_exploration_mode',

        channel: {
          id: "exploration-mode",
          name: "Exploration Mode"
        }
      }
    };
    RNEP.proximityObserver.initialize(credentials, config);
    ```

    You can learn more about notifications channels in [Notifications Overview: Notification channels][0.4.0-2].

[0.4.0-1]: https://github.com/react-native-community/react-native-releases/blob/master/CHANGELOG.md#android-projects-are-now-compiled-using-the-android-26-sdk
[0.4.0-2]: https://developer.android.com/guide/topics/ui/notifiers/notifications#ManageChannels
