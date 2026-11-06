plugins {
    id("com.echo-library")
}

android {
    namespace = "com.echo.features.${MODULE_NAME}.api"
}

dependencies {
    implementation(projects.libraries.architecture)
}

