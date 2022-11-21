package com.truelayer.demo.integrations;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

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
import com.truelayer.payments.ui.TrueLayerUI;
import com.truelayer.payments.ui.screens.processor.ProcessorActivityContract;
import com.truelayer.payments.ui.screens.processor.ProcessorContext;
import com.truelayer.payments.ui.screens.processor.ProcessorResult;

/**
 * Example integration of the SDK with Java and the AndroidX Activity
 */
public class JavaIntegrationActivity extends AppCompatActivity {

    private ProcessorContextProvider processorContextProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integration);

        processorContextProvider = new ProcessorContextProvider(PrefUtils.getQuickstartUrl(this));

        ActivityIntegrationBinding binding = ActivityIntegrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialise the payments configuration
        TrueLayerUI.Builder builder = new TrueLayerUI.Builder()
                .environment(PrefUtils.getEnvironment(this))
                .httpConnection(new HttpConnectionConfiguration(
                        45000,
                        HttpLoggingLevel.None
                ));

        TrueLayerUI.init(getApplicationContext(), builder);

        // Create a contract to receive the results
        ProcessorActivityContract contract = new ProcessorActivityContract();
        // Handle the result when returned at the end of the payment flow
        ActivityResultLauncher<ProcessorContext> flow = registerForActivityResult(contract,
                (ActivityResultCallback< ProcessorResult>) result ->
                        Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
        );

        binding.launchButton.setOnClickListener(v -> launchFlow(flow));
    }

    private void launchFlow(ActivityResultLauncher<ProcessorContext> flow) {
        PaymentType paymentType = PrefUtils.getPaymentType(this);
        // Create a payment context
        processorContextProvider.getProcessorContext(paymentType, contextOutcome -> {
            if(contextOutcome instanceof Ok) {
                // Start the payment flow
                flow.launch(((Ok<ProcessorContext>) contextOutcome).getValue());
            }
            else if(contextOutcome instanceof Fail) {
                // Display error if payment context creation failed
                Toast.makeText(
                        this,
                "Unable to get processor context: " + ((Fail<?>) contextOutcome).getError(),
                        Toast.LENGTH_LONG
                    ).show();
            }
            return null;
        });
    }
}
