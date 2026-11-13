/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "com.echo.libraries.accountselect.test"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.accountselect.api)
    implementation(projects.tests.testutils)
}
