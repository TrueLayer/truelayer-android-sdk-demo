package com.truelayer.demo.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.truelayer.demo.payments.api.PaymentStatus
import com.truelayer.payments.core.domain.utils.Fail
import com.truelayer.payments.core.domain.utils.Ok
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for fetching the Payment's status
 */
class PaymentStatusViewModel(
    private val paymentId: String?,
    private val retryInterval: Long = 2_000,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val paymentContextProvider = PaymentContextProvider()

    private val _status = MutableStateFlow(PaymentStatus.Status.AUTHORIZING)
    val status: StateFlow<PaymentStatus.Status> = _status

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    // Polls the payment status API to receive updates to the payment
    fun pollPaymentStatus() {
        _status.tryEmit(PaymentStatus.Status.AUTHORIZING)
        viewModelScope.launch(dispatcher) {
            var retryCount = 5
            var canRetry = true
            while (canRetry) {
                retryCount -= 1
                canRetry = retryCount > 0
                when (val paymentStatus = paymentContextProvider.getPaymentStatus(paymentId!!)) {
                    is Ok -> {
                        if (paymentStatus.value.status != PaymentStatus.Status.AUTHORIZED && canRetry) {
                            delay(retryInterval)
                            continue
                        } else {
                            _status.emit(paymentStatus.value.status)
                            break
                        }
                    }
                    is Fail -> {
                        _error.emit(paymentStatus.error.localizedMessage ?: "Unknown Error")
                        break
                    }
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun paymentStatusViewModel(paymentId: String?) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PaymentStatusViewModel(paymentId) as T
}
