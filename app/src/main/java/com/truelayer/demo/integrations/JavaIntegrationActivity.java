package com.truelayer.demo.integrations;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import com.truelayer.demo.R;
import com.truelayer.demo.databinding.ActivityIntegrationBinding;
import com.truelayer.demo.payments.PaymentType;
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

import java.util.Map;

import kotlinx.serialization.json.Json;

/**
 * Example integration of the SDK with Java and the AndroidX Activity
 */
public class JavaIntegrationActivity extends AppCompatActivity {

    private static final String TAG = "JavaActivity";

    private Consumer<Intent> newIntentConsumer = null;

    @Nullable
    private ProcessorContext currentProcessorContext = null;

    ActivityResultLauncher<ProcessorContext> flow = null;

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
                tryHandleIntentWithRedirectFromBankData(intent, flow);
                setIntent(intent);
            }
        };
        addOnNewIntentListener(newIntentConsumer);

        flow = registerFlow();

        tryHandleIntentWithRedirectFromBankData(getIntent(), flow);

        binding.launchButton.setOnClickListener(v ->
            startNewPayment(flow)
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ProcessorContext ctx = currentProcessorContext;
        if (ctx != null) {
            try {
                String ctxString = Json.Default.encodeToString(ProcessorContext.Companion.serializer(), ctx);
                outState.putString("processorContext", ctxString);
            } catch (Throwable e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String ctxString = savedInstanceState.getString("processorContext");
        if (ctxString != null) {
            try {
                currentProcessorContext = Json.Default.decodeFromString(ProcessorContext.Companion.serializer(), ctxString);
            } catch (Throwable e) {
                // failed to decode
                Log.e(TAG, e.toString());
            }
        }
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
                (ActivityResultCallback< ProcessorResult>) result -> {
                    if (result instanceof ProcessorResult.Failure res) {
                        if (res.getReason() == ProcessorResult.FailureReason.Unknown && res.getResultShown() == ProcessorResult.ResultShown.None) {
                            // in this case the Processor was terminated without setting a result
                            // this is a common case when a redirect from bank is coming
                            // and the same activity that is holding the SDK is not brought forward
                            // but a new one is created. In such case it is safe to ignore it.
                            return;
                        }
                    }
                    Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();
                }
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
            launchFlow(storedProcessorContext);
        } else {
            ProcessorContext ctx = currentProcessorContext;
            if (ctx != null) {
                launchFlow(ctx);
            }
        }
    }

    private void launchFlow(ProcessorContext ctx) {
        currentProcessorContext = ctx;
        flow.launch(ctx);
    }

    private void startNewPayment(ActivityResultLauncher<ProcessorContext> flow) {
        PaymentType paymentType = PrefUtils.getPaymentType(this);
        ProcessorContextProvider processorContextProvider = new ProcessorContextProvider(PrefUtils.getQuickstartUrl(this));
        // Create a payment context
        processorContextProvider.getProcessorContext(paymentType, this, outcome -> {
            if(outcome instanceof Ok) {
                // Start the payment flow
                launchFlow(((Ok<ProcessorContext>) outcome).getValue());
            } else if(outcome instanceof Fail) {
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
