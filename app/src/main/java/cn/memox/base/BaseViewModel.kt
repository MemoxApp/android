package cn.memox.base

import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.memox.App
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

abstract class BaseViewModel<S, A>(s: S) : ViewModel() {
    var state: S by Keep(s, ::onChange)
    private var baseState: BaseState by Keep(BaseState()) { old, new ->
        if (new.toast.isNotBlank() && new.toast != old.toast) {
            Toast.makeText(App.CONTEXT, new.toast, Toast.LENGTH_SHORT).show()
            viewModelScope.launch {
                delay(2000) // 可过滤掉当前 toast 显示期间相同内容的新 toast
                baseState = baseState.copy(toast = "")
            }
        }
    }

    fun A.send() {
        state = reduce(this)
    }

    infix fun act(action: () -> A) {
        state = reduce(action())
    }

    infix fun act(action: A) {
        state = reduce(action)
    }

    /**
     * 立即更新值,并在之后执行 action
     */
    inline fun S.then(action: () -> Unit): S {
        state = this
        action()
        return state
    }

    /**
     * 立即更新值,并在之后执行 action
     */
    fun S.update() {
        state = this
    }

    fun toast(msg: String) {
        baseState = baseState.copy(toast = msg)
    }

    protected abstract fun reduce(action: A): S

    protected open fun onChange(old: S, new: S) {
        // 做一个差值计算,存储
//        val diff = new - old
    }
}

class Keep<T>(v: T, private val onChange: (T, T) -> Unit = { _, _ -> }) {
    private val v: MutableState<T> = mutableStateOf(v)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return v.value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        // 可以尝试做 Time Travel
        println("Ref:$thisRef, Property:${property}, Value:$value")
//        Log.i("State", value.toString())
        if (v.value == value) return
        onChange(v.value, value)
        v.value = value
    }
}

/*
[{"nickname":"NewValue","password":"NewPwd"}]
 */