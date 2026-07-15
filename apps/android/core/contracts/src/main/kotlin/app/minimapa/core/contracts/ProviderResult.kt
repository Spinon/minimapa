package app.minimapa.core.contracts

sealed interface ProviderResult<out T> {
    data class Success<T>(val value: T) : ProviderResult<T>

    data class RetryableFailure(
        val code: String,
        val message: String,
    ) : ProviderResult<Nothing>

    data class PermanentFailure(
        val code: String,
        val message: String,
    ) : ProviderResult<Nothing>
}
