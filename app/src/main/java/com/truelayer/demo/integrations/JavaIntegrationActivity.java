package com.truelayer.demo.integrations;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.truelayer.demo.Configuration;
import com.truelayer.demo.R;
import com.truelayer.demo.databinding.ActivityIntegrationBinding;
import com.truelayer.demo.payments.PaymentContextProvider;
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

    private final PaymentContextProvider paymentContextProvider = new PaymentContextProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integration);

        ActivityIntegrationBinding binding = ActivityIntegrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialise the payments configuration
        TrueLayerUI.Builder builder = new TrueLayerUI.Builder()
                .environment(Configuration.getEnvironment())
                .httpConnection(Configuration.getHttpConfig());

        TrueLayerUI.init(getApplicationContext(), builder);

        // Create a contract to receive the results
        ProcessorActivityContract contract = new ProcessorActivityContract();
        // Handle the result when returned at the end of the payment flow
        ActivityResultLauncher<ProcessorContext> flow = registerForActivityResult(contract,
                (ActivityResultCallback< ProcessorResult>) result ->
                        Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show()
        );

        binding.titleTextView.setText(R.string.integration_java);
        binding.launchPaymentButton.setOnClickListener(v -> {
            launchPaymentFlow(flow);
        });
    }

    private void launchPaymentFlow(ActivityResultLauncher<ProcessorContext> flow) {
        // Create a payment context
       paymentContextProvider.getPaymentContext(paymentContextOutcome -> {
            if(paymentContextOutcome instanceof Ok) {
                // Start the payment flow
                flow.launch(((Ok<ProcessorContext>) paymentContextOutcome).getValue());
            }
            else if(paymentContextOutcome instanceof Fail) {
                // Display error if payment context creation failed
                Toast.makeText(
                        this,
                "Unable to get payment context: " + ((Fail<?>) paymentContextOutcome).getError(),
                        Toast.LENGTH_LONG
                    ).show();
            }
            return null;
       });
    }
}
