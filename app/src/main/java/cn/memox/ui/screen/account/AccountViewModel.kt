package cn.memox.ui.screen.account

import ForgetMutation
import LoginMutation
import RegisterMutation
import SendEmailCodeMutation
import androidx.lifecycle.viewModelScope
import cn.memox.R
import cn.memox.base.BaseViewModel
import cn.memox.utils.apollo
import cn.memox.utils.defaultErrorHandler
import cn.memox.utils.ifError
import cn.memox.utils.kv
import cn.memox.utils.onSuccess
import cn.memox.utils.string
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import type.ForgetInput
import type.LoginInput
import type.RegisterInput
import type.SendEmailCodeInput

class AccountViewModel : BaseViewModel<AccountState, AccountAction>(AccountState()) {

    override fun reduce(action: AccountAction): AccountState {
        return when (action) {
            is AccountAction.SendVerifyCode -> {
                if (state.mailSendCoolDown > 0) {
                    return state
                }
                state::checkMail.ifError {
                    toast(it.message ?: string(R.string.unknown_error))
                    return state
                }
                state.copy(showLoadingMsg = string(R.string.requesting)).then {
                    viewModelScope.launch {
                        apollo().mutation(
                            SendEmailCodeMutation(SendEmailCodeInput(state.email, state.isRegister))
                        )
                            .toFlow()
                            .onSuccess {
                                startCoolDown()
                                toast(string(R.string.verify_code_sent))
                            }
                            .onCompletion { // Flow Complete
                                state = state.copy(showLoadingMsg = "")
                            }
                            .defaultErrorHandler(::toast)
                            .launchIn(viewModelScope)
                            .start()
                    }
                }
            }

            is AccountAction.Register -> {
                state::checkValid.ifError {
                    toast(it.message ?: "未知错误")
                    return state
                }
                state.copy(showLoadingMsg = string(R.string.registering))
                    .then {
                        viewModelScope.launch {
                            apollo().mutation(
                                RegisterMutation(
                                    RegisterInput(
                                        state.email,
                                        state.verifyCode,
                                        state.password,
                                        state.nickname
                                    )
                                )
                            )
                                .toFlow()
                                .onSuccess {
                                    toast(string(R.string.register_success))
                                }
                                .onCompletion { // Flow Complete
                                    state = state.copy(showLoadingMsg = "")
                                }
                                .defaultErrorHandler(::toast)
                                .launchIn(viewModelScope)
                                .start()
                        }
                    }
            }

            is AccountAction.Reset -> {
                state::checkValid.ifError {
                    toast(it.message ?: "未知错误")
                    return state
                }
                state.copy(showLoadingMsg = string(R.string.requesting))
                    .then {
                        viewModelScope.launch {
                            apollo().mutation(
                                ForgetMutation(
                                    ForgetInput(
                                        state.email,
                                        state.verifyCode,
                                        state.password
                                    )
                                )
                            )
                                .toFlow()
                                .onSuccess {
                                    toast(string(R.string.reset_success))
                                }
                                .onCompletion { // Flow Complete
                                    state = state.copy(showLoadingMsg = "")
                                }
                                .defaultErrorHandler(::toast)
                                .launchIn(viewModelScope)
                                .start()
                        }
                    }
            }

            is AccountAction.Login -> {
                state::checkValid.ifError {
                    toast(it.message ?: string(R.string.unknown_error))
                    return state
                }
                state.copy(showLoadingMsg = string(R.string.logging_in))
                    .also { stats ->
                        viewModelScope.launch {
                            apollo().mutation(
                                LoginMutation(
                                    LoginInput(
                                        stats.email,
                                        stats.password
                                    )
                                )
                            )
                                .toFlow()
                                .onSuccess { data ->
                                    kv.token = data.login.token
                                    toast(string(R.string.login_success))
                                    action.onNav()
                                }
                                .onCompletion { // Flow Complete
                                    state = stats.copy(showLoadingMsg = "")
                                }
                                .defaultErrorHandler(::toast)
                                .launchIn(viewModelScope)
                                .start()
                        }
                    }
            }

            is AccountAction.TogglePasswordVisibility -> state.copy(showPassword = !state.showPassword)
            is AccountAction.ToggleState -> state.copy(state = action.v)
            is AccountAction.UpdateNickname -> state.copy(nickname = action.v)
            is AccountAction.UpdatePassword -> state.copy(password = action.v)
            is AccountAction.UpdateEmail -> state.copy(email = action.v)
            is AccountAction.UpdateVerifyCode -> state.copy(verifyCode = action.v)
            is AccountAction.ShowLoading -> state.copy(showLoadingMsg = action.msg)
            is AccountAction.ToggleSetting -> state.copy(showSetting = !state.showSetting)
        }
    }

    private fun startCoolDown() {
        viewModelScope.launch {
            if (state.mailSendCoolDown <= 0) {
                state = state.updateCoolDown(60)
                while (state.mailSendCoolDown > 0) {
                    delay(1000)
                    state = state.updateCoolDown(state.mailSendCoolDown - 1)
                }
            }
        }
    }
}