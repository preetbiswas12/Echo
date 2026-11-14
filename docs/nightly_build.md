# Nightly builds

<!--- TOC -->

* [Configuration](#configuration)
* [Build nightly manually](#build-nightly-manually)

<!--- END -->

## Configuration

The nightly build will contain what's on `develop`, in release mode, for the main
variant. It is signed using a dedicated signature, and has a dedicated appId
(`com.echo.x.nightly`), so it can be installed along with the production version of
Echo Android. The only other difference compared to Echo Android is a different app
name. We do not want to change the app name since it will also affect some strings in
the app, and we do not want to do that. (TODO today, the app name is changed.)

*Note:* Due to a limitation of Firebase, the nightly build is the universal build,
which means that the size of the APK is a bit bigger, but this should not have any
other side effect.

## Build nightly manually

Nightly build can be built manually from your computer. You will need to retrieve
some secrets from Passbolt and add them to your file `~/.gradle/gradle.properties`:

```
signing.element.nightly.storeFile=LOCATION_OF_THE_KEYSTORE_FILE
signing.element.nightly.storePassword=VALUE_FROM_PASSBOLT
signing.element.nightly.keyId=VALUE_FROM_PASSBOLT
signing.element.nightly.keyPassword=VALUE_FROM_PASSBOLT
```

You will also need to add the environment variable `FIREBASE_TOKEN`:

```sh
export FIREBASE_TOKEN=VALUE_FROM_PASSBOLT
```

Then you can run the following commands:

```sh
git checkout develop
./gradlew assembleGplayNightly appDistributionUploadGplayNightly
```

Then you can reset the change on the codebase.