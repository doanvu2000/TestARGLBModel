package com.hit.testarloadglbmodel

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class BaseActivity : AppCompatActivity() {

    //region logging
    fun logInfo(msg: String) = Log.i("AppDebug", "Log[${this.javaClass.simpleName}] " + msg)
    fun logWarn(msg: String) = Log.w("AppDebug", "Log[${this.javaClass.simpleName}] " + msg)
    fun logDebug(msg: String) = Log.d("AppDebug", "Log[${this.javaClass.simpleName}] " + msg)
    fun logError(msg: String) = Log.e("AppDebug", "Log[${this.javaClass.simpleName}] " + msg)
    //endregion

    //region coroutine handling
    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    open fun handleError(throwable: Throwable) {
        val errorMessage = throwable.message ?: ""
        logError(errorMessage)
        throwable.printStackTrace()
    }

    fun launchCoroutine(
        dispatcher: CoroutineContext, blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            if (isFinishing) {
                return
            }
            lifecycleScope.launch(dispatcher + coroutineExceptionHandler) {
                blockCoroutine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchCoroutineMain(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.Main) {
            blockCoroutine()
        }
    }

    fun mainScope(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutineMain(blockCoroutine)
    }

    fun launchCoroutineIO(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.IO) {
            blockCoroutine()
        }
    }

    fun ioScope(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutineIO(blockCoroutine)
    }

    fun delayToAction(delayTime: Long = 200L, action: () -> Unit) {
        launchCoroutineIO {
            delay(delayTime)
            launchCoroutineMain {
                action()
            }
        }
    }
    //endregion
}