package com.truelayer.demo.integrations;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.truelayer.demo.payments.PaymentType;
import com.truelayer.demo.R;
import com.truelayer.demo.databinding.ActivityIntegrationBinding;
import com.truelayer.demo.payments.ProcessorContextProvider;
import com.truelayer.demo.utils.PrefUtils;
import com.truelayer.payments.core.domain.configuration.HttpConnectionConfiguration;
import com.truelayer.payments.core.domain.configuration.HttpLoggingLevel;
import com.truelayer.payments.core.domain.utils.Fail;
import com.truelayer.payments.core.domain.utils.Ok;
import com.truelayer.payments.core.utils.ExtensionsKt;
import com.truelayer.payments.ui.TrueLayerUI;
import com.truelayer.payments.ui.screens.processor.ProcessorActivityContract;
import com.truelayer.payments.ui.screens.processor.ProcessorContext;
import com.truelayer.payments.ui.screens.processor.ProcessorResult;

import java.util.Dictionary;
import java.util.Map;

/**
 * Example integration of the SDK with Java and the AndroidX Activity
 */
public class JavaIntegrationActivity extends AppCompatActivity {

    private Consumer<Intent> newIntentConsumer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integration);

        ActivityIntegrationBinding binding = ActivityIntegrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialise the payments configuration
        initPaymentsSdk();

        newIntentConsumer = new Consumer<Intent>() {
            @Override
            public void accept(Intent intent) {
                ActivityResultLauncher<ProcessorContext> flow = registerFlow();
                tryHandleIntentWithRedirectFromBankData(intent, flow);
            }
        };
        addOnNewIntentListener(newIntentConsumer);

        ActivityResultLauncher<ProcessorContext> flow = registerFlow();

        tryHandleIntentWithRedirectFromBankData(getIntent(), flow);

        binding.launchButton.setOnClickListener(v ->
                launchFlow(flow)
        );
    }

    private void initPaymentsSdk() {
        // Initialise the payments configuration
        TrueLayerUI.Builder builder = new TrueLayerUI.Builder()
                .environment(PrefUtils.getEnvironment(this))
                .httpConnection(new HttpConnectionConfiguration(
                        45000,
                        HttpLoggingLevel.None
                ));

        TrueLayerUI.init(getApplicationContext(), builder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (newIntentConsumer != null) {
            removeOnNewIntentListener(newIntentConsumer);
        }
    }

    private ActivityResultLauncher<ProcessorContext> registerFlow() {
        // Create a contract to receive the results
        ProcessorActivityContract contract = new ProcessorActivityContract();

        // Handle the result returned from the SDK at the end of the payment flow
        return registerForActivityResult(contract,
                (ActivityResultCallback< ProcessorResult>) result ->
                        Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
        );
    }

    private void tryHandleIntentWithRedirectFromBankData(Intent intent, ActivityResultLauncher<ProcessorContext> flow) {
        Uri data = intent.getData();
        Map<String, String> params = ExtensionsKt.extractTrueLayerRedirectParams(data);
        ProcessorContext storedProcessorContext = PrefUtils.getProcessorContext(this);

        if (!params.isEmpty() && storedProcessorContext != null &&
                (storedProcessorContext.getId().equals(params.get("payment_id")) || storedProcessorContext.getId().equals(params.get("mandate_id")))) {
            // The user is returning from the provider app
            // and the payment/mandate ID matches the one we have stored
            // so we can fetch the payment status
            flow.launch(storedProcessorContext);
        }
    }

    private void startPaymentProcessor(ActivityResultLauncher<ProcessorContext> flow, ProcessorContext.PaymentContext paymentContext) {
        // Start the payment processor

    }

    private void startNewPayment(ActivityResultLauncher<ProcessorContext> flow) {
        PaymentType paymentType = PrefUtils.getPaymentType(this);
        ProcessorContextProvider processorContextProvider = new ProcessorContextProvider(PrefUtils.getQuickstartUrl(this));
        // Create a payment context
        processorContextProvider.getProcessorContext(paymentType, this, outcome -> {
            if(outcome instanceof Ok) {
                // Start the payment flow
                flow.launch(((Ok<ProcessorContext>) outcome).getValue());
            }
            else if(outcome instanceof Fail) {
                // Display error if payment context creation failed
                Toast.makeText(
                        this,
                        getString(R.string.processor_context_error, ((Fail<?>) outcome).getError()),
                        Toast.LENGTH_LONG
                ).show();
            }
            return null;
        });
    }

    private void launchFlow(ActivityResultLauncher<ProcessorContext> flow) {
        PaymentType paymentType = PrefUtils.getPaymentType(this);
        ProcessorContextProvider processorContextProvider = new ProcessorContextProvider(PrefUtils.getQuickstartUrl(this));
        // Create a payment context
        processorContextProvider.getProcessorContext(paymentType, this, outcome -> {
            if(outcome instanceof Ok) {
                // Start the payment flow
                flow.launch(((Ok<ProcessorContext>) outcome).getValue());
            }
            else if(outcome instanceof Fail) {
                // Display error if payment context creation failed
                Toast.makeText(
                        this,
                        getString(R.string.processor_context_error, ((Fail<?>) outcome).getError()),
                        Toast.LENGTH_LONG
                    ).show();
            }
            return null;
        });
    }
}
