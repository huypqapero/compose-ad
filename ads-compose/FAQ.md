# Q: Logcat shows `AperoAdmob` `onShowSplash: fail on background` when using `AppOpenAdEffect`

- A: find `<provider android:name="androidx.startup.InitializationProvider" />` and make sure remove
  `tools:node="remove"` element or replace to `"tools:node="merge"`

```xml

<provider android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup" android:exported="false"
    tools:node="merge" />
```

# Q: I use `BannerAdView` or `NativeAdView` and have this crash, how do i get over with?

```
java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/databinding/DataBinderMapperImpl;
```

- A: enable `dataBinding` in your app's `build.gradle` file
