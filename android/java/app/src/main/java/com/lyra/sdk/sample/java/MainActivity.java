package com.lyra.sdk.sample.java;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lyra.sdk.Lyra;
import com.lyra.sdk.callback.LyraHandler;
import com.lyra.sdk.callback.LyraResponse;
import com.lyra.sdk.exception.LyraException;
import com.lyra.sdk.exception.LyraMobException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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
    // Merchant server url
    // FIXME: change by the right payment server
    private static final String SERVER_URL = "<REPLACE_ME>";

    // Public key
    // FIXME: change by your public key
    private static final String PUBLIC_KEY = "<REPLACE_ME>";

    // FIXME: Change by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
    private static final String API_SERVER_NAME = "<REPLACE_ME>";

    // Environment TEST or PRODUCTION, refer to documentation for more information
    // FIXME: change by your targeted environment
    private static final String PAYMENT_MODE = "TEST";

    // FIXME: activate if we want to ask to register the card
    private static final boolean ASK_REGISTER_PAY = false;

    // Payment parameters
    // Change by the desired parameters if necessary
    private static final String AMOUNT = "100";
    private static final String CURRENCY = "EUR";
    private static final String ORDER_ID = "";

    // Customer information parameters
    // Change by the desired parameters if necessary
    private static final String CUSTOMER_EMAIL = "";
    private static final String CUSTOMER_REFERENCE = "";

    //Instance of Lyra Mobile SDK
    private Lyra SDK = Lyra.INSTANCE;

    //RequestQueue instance used call server
    private RequestQueue requestQueue;

    private HashMap<String, Object> getOptions() {
        HashMap options = new HashMap();
        options.put(Lyra.OPTION_API_SERVER_NAME, API_SERVER_NAME);
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
        setContentView(R.layout.activity_main);
        //Initialize the SDK
        try {
            SDK.initialize(getApplicationContext(), PUBLIC_KEY, getOptions());
        } catch (LyraMobException ex) {
            // handle possible exceptions when initializing SDK (Ex: invalid public key format)
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    /**
     * onPayClick method
     * Invokes the payment
     *
     * @param view View of the Pay button
     */
    public void onPayClick(View view) {
        try {
            getPaymentContext(getPaymentParams());
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Unexpected Error", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Performs the create operation, calling the merchant server.
     * This call creates the session on server and retrieves the payment context that is necessary to continue the process
     *
     * @param paymentParams the operation parameters
     */
    private void getPaymentContext(JSONObject paymentParams) {
        requestQueue.add(new JsonObjectRequest(Request.Method.POST, SERVER_URL + "/createPayment", getPaymentParams(), new Response.Listener<JSONObject>() {
            //Process merchant server response
            @Override
            public void onResponse(JSONObject response) {
                //In this sample, we extract the formToken from the serverResponse, call processServerResponse() which execute the process method of the SDK
                processServerResponse(extractFormToken(response.toString()));
            }
        }, new Response.ErrorListener() {
            //Error when calling merchant server
            @Override
            public void onErrorResponse(VolleyError error) {
                //Please manage your error behaviour here
                Toast.makeText(getApplicationContext(), "Error Creating Payment", Toast.LENGTH_LONG).show();
            }
        }));
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
    private void processServerResponse(String formToken) {
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
        });
    }

    /**
     * Call the server to verify that the response received from payment platform through the SDK is valid and has not been modified.
     *
     * @param response information about the result of the operation
     */
    private void verifyPayment(JSONObject response) {
        requestQueue.add(new JsonObjectRequest(Request.Method.POST, SERVER_URL + "/verifyResult", response, new Response.Listener<JSONObject>() {
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
        }));
    }

    /*
     * Create a JSONObject with all the payment params.
     *
     * Check API REST integration documentation
     */
    private JSONObject getPaymentParams() {
        JSONObject paymentParams = new JSONObject();

        try {
            paymentParams.put("amount", AMOUNT);
            paymentParams.put("currency", CURRENCY);
            paymentParams.put("orderId", ORDER_ID);

            paymentParams.put("customer",
                    new JSONObject(String.format("{\"email\":\"%s\", \"reference\":\"%s\"}", CUSTOMER_EMAIL, CUSTOMER_REFERENCE)));

            if (ASK_REGISTER_PAY) {
                paymentParams.put("formAction", "ASK_REGISTER_PAY");
            }

            // FIXME: add all your payment params here. Check integration documentation for further information

            paymentParams.put("formTokenVersion", SDK.getFormTokenVersion());
            paymentParams.put("mode", PAYMENT_MODE);
        } catch (JSONException ex) {
            // FIXME: handle possible exceptions when constructing JSON
        }

        return paymentParams;
    }
}
