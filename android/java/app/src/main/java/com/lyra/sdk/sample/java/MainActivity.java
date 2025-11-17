package com.lyra.sdk.sample.java;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.wallet.button.ButtonConstants;
import com.google.android.gms.wallet.button.ButtonOptions;
import com.google.android.gms.wallet.button.PayButton;
import com.lyra.sdk.Lyra;
import com.lyra.sdk.callback.LyraHandler;
import com.lyra.sdk.callback.LyraResponse;
import com.lyra.sdk.exception.LyraException;
import com.lyra.sdk.exception.LyraMobException;
import com.lyra.sdk.model.enums.LyraPaymentMethods;
import com.lyra.sdk.sample.java.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Main activity
 * <p>
 * This  activity allows to user to perform a payment using the Lyra Mobile SDK
 * <p>
 * In order to perform a quick test payment:
 * <li>You should deploy your merchant server in order to create a payment session</li>.
 * <li>Set the merchant server endpoint in the SERVER_URL constant</li>
 * <li>Build and launch the application</li>
 * <li>Click in Pay button and complete the payment process</li>
 * <p></p>
 * Please note that, for readability purposes in this example, we do not use logs
 *
 * @author Lyra Network
 */
public class MainActivity extends AppCompatActivity {


    //Instance of Lyra Mobile SDK
    private Lyra SDK = Lyra.INSTANCE;

    //RequestQueue instance used call server
    private RequestQueue requestQueue;

    private HashMap<String, Object> getOptions() {
        HashMap options = new HashMap();
        options.put(Lyra.OPTION_API_SERVER_NAME, Config.API_SERVER_NAME);
        options.put(Lyra.OPTION_NFC_ENABLED, false);
        options.put(Lyra.OPTION_CARD_SCANNING_ENABLED, false);
        return options;
    }

    /**
     * onCreate method
     * Activity creation and SDK initialization
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add Google Pay Button
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

        //Initialize the SDK
        try {
            SDK.initialize(getApplicationContext(), Config.PUBLIC_KEY, getOptions());
            binding.sdkVersion.setText(SDK.getSDKVersion());
        } catch (LyraMobException ex) {
            // handle possible exceptions when initializing SDK (Ex: invalid public key format)
            Toast.makeText(this, "Cant initialize SDK. Please set REPLACE_ME values on Config.kt file.", Toast.LENGTH_LONG).show();
        }

        // Add Google Pay Button
        PayButton googlePayButton = binding.googlePayButton;
        googlePayButton.initialize(
                ButtonOptions.newBuilder()
                        .setButtonType(ButtonConstants.ButtonType.PLAIN)
                        .setCornerRadius(10)
                        .setAllowedPaymentMethods(SDK.getAllowedPaymentMethodsMock())
                        .build()
        );
        googlePayButton.setOnClickListener(this::onGooglePayClick);


        requestQueue = Volley.newRequestQueue(getApplicationContext());
        setContentView(binding.getRoot());
    }

    /**
     * onPayClick method
     * Invokes the payment
     *
     * @param view View of the Pay button
     */
    public void onPayClick(View view) {
        displayLoadingPanel();
        try {
            getPaymentContext(getProcessOptions());
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Unexpected Error", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * onPayClick method
     * Invokes the payment
     *
     * @param view View of the Pay button
     */
    public void onGooglePayClick(View view) {
        displayLoadingPanel();
        try {
            getPaymentContext(getProcessOptionsDirectGooglePay());
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Unexpected Error", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Performs the create operation, calling the merchant server.
     * This call creates the session on server and retrieves the payment context that is necessary to continue the process
     */
    private void getPaymentContext(HashMap<String, Object> options) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.SERVER_URL + "/createPayment", getPaymentParams(), new Response.Listener<JSONObject>() {
            //Process merchant server response
            @Override
            public void onResponse(JSONObject response) {
                //In this sample, we extract the formToken from the serverResponse, call processServerResponse() which execute the process method of the SDK
                processFormToken(extractFormToken(response.toString()), options);
            }
        }, new Response.ErrorListener() {
            //Error when calling merchant server
            @Override
            public void onErrorResponse(VolleyError error) {
                //Please manage your error behaviour here
                hideLoadingPanel();
                Toast.makeText(getApplicationContext(), "Error Creating Payment" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return constructBasicAuthHeaders();
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Return a formToken if extraction is done correctly
     * Return an empty formToken if an error occur -> SDK will return an INVALID_FORMTOKEN exception
     */
    private String extractFormToken(String serverResponse) {
        try {
            JSONObject answer = new JSONObject(serverResponse).getJSONObject("answer");
            String formToken = answer.optString("formToken");
            if (formToken.equals("")) {
                // TODO Please manage your error behaviour here
                // in this case, an error is present in the serverResponse, check the returned errorCode errorMessage
                Toast.makeText(getApplicationContext(), "extractFormToken() -> formToken is empty" + "\n" +
                        "errorCode = " + answer.getString("errorCode") + "\n" +
                        "errorMessage = " + answer.optString("errorMessage") + "\n" +
                        "detailedErrorCode = " + answer.optString("detailedErrorCode") + "\n" +
                        "detailedErrorMessage = " + answer.optString("detailedErrorMessage"), Toast.LENGTH_LONG).show();
            }
            return formToken;
        } catch (Throwable throwable) {
            // TODO Please manage your error behaviour here
            // in this case, the serverResponse isn't as expected, please check the input serverResponse param
            Toast.makeText(getApplicationContext(), "Cannot extract formToken from serverResponse", Toast.LENGTH_LONG).show();
            return "";
        }
    }

    /**
     * Calls the Lyra Mobile SDK in order to handle the payment operation
     *
     * @param formToken the formToken extracted from the information of the payment session
     */
    private void processFormToken(String formToken, HashMap<String, Object> options) {
        hideLoadingPanel();
        //Call Lyra Mobile SDK
        SDK.process(getSupportFragmentManager(), formToken, new LyraHandler() {
            @Override
            public void onSuccess(LyraResponse lyraResponse) {
                verifyPayment(lyraResponse);
            }

            @Override
            public void onError(LyraException e, LyraResponse lyraResponse) {
                Toast.makeText(getApplicationContext(), "Payment fail: " + e.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        }, options);
    }

    private HashMap<String, Object> getProcessOptionsDirectGooglePay() {
        HashMap<String, Object> options = getProcessOptions();

        options.put(Lyra.PAYMENT_METHOD_TYPE, LyraPaymentMethods.GOOGLE_PAY);

        return options;
    }

    private HashMap<String, Object> getProcessOptions() {
        HashMap<String, Object> options = new HashMap<String, Object>();
        // options[Lyra.CUSTOM_PAY_BUTTON_LABEL] = "Hello World"
        return options;
    }

    /**
     * Call the server to verify that the response received from payment platform through the SDK is valid and has not been modified.
     *
     * @param response information about the result of the operation
     */
    private void verifyPayment(JSONObject response) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.SERVER_URL + "/verifyResult", response, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Check the response integrity by verifying the hash on your server
                Toast.makeText(getApplicationContext(), "Payment Success", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            //Error when verifying payment
            @Override
            public void onErrorResponse(VolleyError error) {
                //Manage error here, please refer to the documentation for more information
                Toast.makeText(getApplicationContext(), "Payment verification fail", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return constructBasicAuthHeaders();
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    private Map<String, String> constructBasicAuthHeaders() {
        HashMap headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Authorization", "Basic " + Base64.encodeToString(Config.CREDENTIALS.getBytes(), Base64.NO_WRAP));
        return headers;
    }

    /*
     * Create a JSONObject with all the payment params.
     *
     * Check API REST integration documentation
     */
    private JSONObject getPaymentParams() {
        JSONObject paymentParams = new JSONObject();

        try {
            paymentParams.put("amount", Config.AMOUNT);
            paymentParams.put("currency", Config.CURRENCY);
            paymentParams.put("orderId", Config.ORDER_ID);

            paymentParams.put("customer",
                    new JSONObject(String.format("{\"email\":\"%s\", \"reference\":\"%s\"}", Config.CUSTOMER_EMAIL, Config.CUSTOMER_REFERENCE)));

            if (Config.ASK_REGISTER_PAY) {
                paymentParams.put("formAction", "ASK_REGISTER_PAY");
            }

            // FIXME: add all your payment params here. Check integration documentation for further information

            paymentParams.put("formTokenVersion", SDK.getFormTokenVersion());
            paymentParams.put("mode", Config.PAYMENT_MODE);
        } catch (JSONException ex) {
            // FIXME: handle possible exceptions when constructing JSON
        }

        return paymentParams;
    }
    /**
     * Display the loading panel
     */
    private void displayLoadingPanel() {
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    private void hideLoadingPanel() {
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }
}
