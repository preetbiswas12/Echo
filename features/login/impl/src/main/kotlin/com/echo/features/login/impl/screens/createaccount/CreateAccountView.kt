/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package com.echo.features.login.impl.screens.createaccount

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.echo.compound.theme.ElementTheme
import com.echo.compound.tokens.generated.CompoundIcons
import com.echo.features.login.impl.R
import com.echo.features.login.impl.error.registrationError
import com.echo.libraries.architecture.AsyncData
import com.echo.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import com.echo.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import com.echo.libraries.designsystem.components.BigIcon
import com.echo.libraries.designsystem.components.button.BackButton
import com.echo.libraries.designsystem.components.dialogs.ErrorDialog
import com.echo.libraries.designsystem.components.form.textFieldState
import com.echo.libraries.designsystem.modifiers.bringIntoViewOnImeVisible
import com.echo.libraries.designsystem.modifiers.onTabOrEnterKeyFocusNext
import com.echo.libraries.designsystem.preview.ElementPreview
import com.echo.libraries.designsystem.preview.PreviewsDayNight
import com.echo.libraries.designsystem.theme.components.Button
import com.echo.libraries.designsystem.theme.components.Icon
import com.echo.libraries.designsystem.theme.components.PasswordVisibilityToggle
import com.echo.libraries.designsystem.theme.components.Scaffold
import com.echo.libraries.designsystem.theme.components.TextField
import com.echo.libraries.designsystem.theme.components.TopAppBar
import com.echo.libraries.ui.strings.CommonStrings

@Composable
fun CreateAccountView(
    state: CreateAccountState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val autofillManager = LocalAutofillManager.current

    BackHandler {
        autofillManager?.cancel()
        onBackClick()
    }

    val isLoading by remember(state.registerAction) {
        derivedStateOf {
            state.registerAction is AsyncData.Loading
        }
    }
    val focusManager = LocalFocusManager.current

    fun submit() {
        // Clear focus to prevent keyboard issues with textfields
        focusManager.clearFocus(force = true)

        autofillManager?.commit()

        state.eventSink(CreateAccountEvent.Submit)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(onClick = {
                        autofillManager?.cancel()
                        onBackClick()
                    })
                },
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(state = scrollState)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
        ) {
            // Title
            IconTitleSubtitleMolecule(
                modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp),
                iconStyle = BigIcon.Style.Default(CompoundIcons.UserProfileSolid()),
                title = stringResource(
                    id = R.string.screen_create_account_title_with_homeserver,
                    state.accountProvider.friendlyServerName()
                ),
                subTitle = stringResource(id = R.string.screen_create_account_subtitle)
            )
            Spacer(Modifier.height(40.dp))
            CreateAccountForm(
                state = state,
                isLoading = isLoading,
                onSubmit = ::submit
            )
            // Min spacing
            Spacer(Modifier.height(24.dp))
            // Flexible spacing to keep the submit button at the bottom
            Spacer(modifier = Modifier.weight(1f))
            // Submit
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                ButtonColumnMolecule {
                    Button(
                        text = stringResource(CommonStrings.action_continue),
                        showProgress = isLoading,
                        onClick = ::submit,
                        enabled = state.submitEnabled || isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            if (state.registerAction is AsyncData.Failure) {
                CreateAccountErrorDialog(error = state.registerAction.error, onDismiss = {
                    state.eventSink(CreateAccountEvent.ClearError)
                })
            }
        }
    }
}

@Composable
private fun CreateAccountForm(
    state: CreateAccountState,
    isLoading: Boolean,
    onSubmit: () -> Unit,
) {
    var usernameFieldState by textFieldState(stateValue = state.formState.username)
    var passwordFieldState by textFieldState(stateValue = state.formState.password)
    var confirmedPasswordFieldState by textFieldState(stateValue = state.formState.confirmedPassword)
    var registrationCodeFieldState by textFieldState(stateValue = state.formState.registrationCode)

    val focusManager = LocalFocusManager.current
    val eventSink = state.eventSink

    Column {
        TextField(
            label = stringResource(R.string.screen_create_account_username_header),
            value = usernameFieldState,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnImeVisible()
                .onTabOrEnterKeyFocusNext(focusManager)
                .semantics {
                    contentType = ContentType.Username
                },
            placeholder = stringResource(CommonStrings.common_username),
            supportingText = if (usernameFieldState.isNotEmpty() && !state.isUsernameValid) {
                stringResource(R.string.screen_create_account_error_invalid_username)
            } else {
                null
            },
            onValueChange = {
                // Usernames only contain lowercase characters, enforced on the fly so that the field
                // never displays a value the homeserver would reject.
                val sanitized = it.sanitize().lowercase()
                usernameFieldState = sanitized
                eventSink(CreateAccountEvent.SetUsername(sanitized))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            singleLine = true,
            trailingIcon = if (usernameFieldState.isNotEmpty()) {
                {
                    Box(
                        Modifier.clickable(
                            onClickLabel = stringResource(CommonStrings.action_clear),
                            role = Role.Button,
                        ) {
                            usernameFieldState = ""
                            eventSink(CreateAccountEvent.SetUsername(""))
                        }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.Close(),
                            contentDescription = stringResource(CommonStrings.action_clear),
                            tint = ElementTheme.colors.iconSecondary
                        )
                    }
                }
            } else {
                null
            },
        )
        Spacer(Modifier.height(20.dp))
        CreatePasswordFields(
            state = state,
            isLoading = isLoading,
            eventSink = eventSink,
            focusManager = focusManager,
            onSubmit = onSubmit,
        )
        Spacer(Modifier.height(20.dp))
        TextField(
            label = stringResource(R.string.screen_create_account_registration_code),
            value = registrationCodeFieldState,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnImeVisible()
                .onTabOrEnterKeyFocusNext(focusManager),
            placeholder = stringResource(R.string.screen_create_account_registration_code),
            supportingText = stringResource(R.string.screen_create_account_registration_code_supporting_text),
            onValueChange = {
                val sanitized = it.sanitize()
                registrationCodeFieldState = sanitized
                eventSink(CreateAccountEvent.SetRegistrationCode(sanitized))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            singleLine = true,
        )
    }
}

@Composable
private fun CreatePasswordFields(
    state: CreateAccountState,
    isLoading: Boolean,
    eventSink: (CreateAccountEvent) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onSubmit: () -> Unit,
) {
    var passwordFieldState by textFieldState(stateValue = state.formState.password)
    var confirmedPasswordFieldState by textFieldState(stateValue = state.formState.confirmedPassword)

    Column {
        var passwordVisible by remember { mutableStateOf(false) }
        if (state.registerAction is AsyncData.Loading) {
            // Ensure password is hidden when the form is submitted
            passwordVisible = false
        }
        TextField(
            value = passwordFieldState,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnImeVisible()
                .onTabOrEnterKeyFocusNext(focusManager)
                .semantics {
                    contentType = ContentType.Password
                },
            onValueChange = {
                val sanitized = it.sanitize()
                passwordFieldState = sanitized
                eventSink(CreateAccountEvent.SetPassword(sanitized))
            },
            placeholder = stringResource(CommonStrings.common_password),
            supportingText = if (state.formState.password.isNotEmpty() && !state.isPasswordValid) {
                stringResource(R.string.screen_create_account_password_supporting_text)
            } else {
                null
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                PasswordVisibilityToggle(
                    visible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible },
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
        )
        Spacer(Modifier.height(20.dp))
        TextField(
            value = confirmedPasswordFieldState,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnImeVisible()
                .onTabOrEnterKeyFocusNext(focusManager)
                .semantics {
                    contentType = ContentType.Password
                },
            onValueChange = {
                val sanitized = it.sanitize()
                confirmedPasswordFieldState = sanitized
                eventSink(CreateAccountEvent.SetConfirmedPassword(sanitized))
            },
            placeholder = stringResource(CommonStrings.action_confirm_password),
            supportingText = if (state.formState.confirmedPassword.isNotEmpty() && !state.isPasswordConfirmed) {
                stringResource(R.string.screen_create_account_error_password_mismatch)
            } else {
                null
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                PasswordVisibilityToggle(
                    visible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible },
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
        )
    }
}

/**
 * Ensure that the string does not contain any line separator, which can happen when pasting values.
 */
private fun String.sanitize(): String {
    return filterNot { it == '\n' || it == '\r' }
}

@Composable
private fun CreateAccountErrorDialog(error: Throwable, onDismiss: () -> Unit) {
    ErrorDialog(
        title = stringResource(id = CommonStrings.dialog_title_error),
        content = stringResource(registrationError(error)),
        onSubmit = onDismiss
    )
}

@PreviewsDayNight
@Composable
internal fun CreateAccountViewPreview(@PreviewParameter(CreateAccountStatePreviewParam::class) state: CreateAccountState) = ElementPreview {
    CreateAccountView(
        state = state,
        onBackClick = {},
    )
}
