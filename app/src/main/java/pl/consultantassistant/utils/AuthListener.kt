package pl.consultantassistant.utils

interface AuthListener {

    fun onStarted()
    fun onFailure(message: String)
    fun onIncorrectEmail(errorCode: Int)
    fun onIncorrectPassword(errorCode: Int)
    fun onIncorrect2ndPassword(errorCode: Int)
    fun onSuccess()
    fun onNoConnectionAvailable()
}