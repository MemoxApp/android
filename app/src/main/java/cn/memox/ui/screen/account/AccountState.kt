package cn.memox.ui.screen.account

import cn.memox.R
import cn.memox.utils.ifElse
import cn.memox.utils.string

data class AccountState(
    val nickname: String = "",
    val password: String = "",
    val email: String = "",
    val verifyCode: String = "",
    val mailSendCoolDown: Int = 0,
    val showPassword: Boolean = false,
    val showLoadingMsg: String = "",
    val showSetting: Boolean = false,
    val state: State = State.Idle,
) {
    fun sendMailButtonText() =
        (mailSendCoolDown == 0).ifElse(
            string(R.string.send_verify_code),
            string(R.string.secs, mailSendCoolDown)
        )

    val isRegister get() = state == State.Register
    val isReset get() = state == State.Reset

    fun updateCoolDown(v: Int) = copy(mailSendCoolDown = v)

    fun checkMail() {
        if (email.isEmpty()) throw Exception(string(R.string.mail_empty))
        if (!email.matches("""^[a-zA-Z0-9.!#${'$'}%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*${'$'}""".toRegex()))
            throw Exception(string(R.string.mail_pattern_incorrect))
    }

    fun checkValid() {
        checkMail()

        if (password.isEmpty()) throw Exception(string(R.string.pwd_empty))
        if (password.length !in 6..32) throw Exception(string(R.string.pwd_lengh_incorrect))

        if (isRegister || isReset) {
            if (verifyCode.isEmpty()) throw Exception(string(R.string.verify_code_empty))
            if (!verifyCode.matches("""[0-9]{4,}""".toRegex())) throw Exception(string(R.string.verify_code_incorrect))
        }

        if (isRegister) {
            if (nickname.isEmpty()) throw Exception(string(R.string.nickname_empty))
            if (nickname.length !in 2..16) throw Exception(string(R.string.nickname_length_incorrect))
        }
    }

    enum class State {
        Idle, Login, Reset, Register,
    }
}

sealed class AccountAction {
    object SendVerifyCode : AccountAction()
    object Register : AccountAction()
    data class Login(val onNav: () -> Unit) : AccountAction()
    object Reset : AccountAction()
    object TogglePasswordVisibility : AccountAction()
    object ToggleSetting : AccountAction()
    data class ShowLoading(val msg: String) : AccountAction()
    data class ToggleState(val v: AccountState.State) : AccountAction()
    data class UpdateNickname(val v: String) : AccountAction()
    data class UpdatePassword(val v: String) : AccountAction()
    data class UpdateEmail(val v: String) : AccountAction()
    data class UpdateVerifyCode(val v: String) : AccountAction()
}