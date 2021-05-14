package net.teamtruta.tiaires.extensions

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<T>(private val success: Boolean,
                    private val message: String = "",
                    private val content: T? = null) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): Triple<Boolean, String, T?>?{
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            Triple(success, message, content)
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): Triple<Boolean, String, T?> = Triple(success, message, content)

}
