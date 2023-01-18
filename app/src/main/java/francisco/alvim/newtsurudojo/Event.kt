package francisco.alvim.newtsurudojo

open class Event<out T>(private val content: T) {

    /**
     * On first read returns true, on sequent reads returns false
     */
    var isFirstRun = true
        get() = if (!field) {
            false
        } else {
            isFirstRun = false
            true
        }
        private set // Allow external read but not write

    fun onFirstRun(function: () -> Unit) {
        if(isFirstRun)  function()
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun getContent(): T = content
}