package com.lyra.sdk.sample.kotlin

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.wallet.button.ButtonConstants
import com.google.android.gms.wallet.button.ButtonOptions
import com.lyra.sdk.Lyra
import com.lyra.sdk.callback.LyraHandler
import com.lyra.sdk.callback.LyraResponse
import com.lyra.sdk.exception.LyraException
import com.lyra.sdk.model.enums.LyraPaymentMethods
import com.lyra.sdk.sample.kotlin.databinding.ActivityMainBinding
import org.json.JSONObject

/**
 * Main activity
 * <p>
 * This activity allows to user to perform a payment using the Lyra Mobile SDK
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
class MainActivity : AppCompatActivity() {

    // Initialize a new RequestQueue instance
    private lateinit var requestQueue: RequestQueue
    private lateinit var binding: ActivityMainBinding

    private fun getOptions(): HashMap<String, Any?> {
        val options = HashMap<String, Any?>()

        options[Lyra.OPTION_API_SERVER_NAME] = Config.API_SERVER_NAME

        // android.permission.NFC must be added on AndroidManifest file
        // options[Lyra.OPTION_NFC_ENABLED] = true

        // cards-camera-recognizer dependency must be added on gradle file
        // options[Lyra.OPTION_CARD_SCANNING_ENABLED] = true

        return options
    }

    /**
     * onCreate method
     * Activity creation and SDK initialization
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        try {
            // FIXME: Change PUBLIC_KEY by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
            Lyra.initialize(applicationContext, Config.PUBLIC_KEY, getOptions())
            binding.sdkVersion.text = Lyra.getSDKVersion()
        } catch (exception: Exception) {
            // handle possible exceptions when initializing SDK (Ex: invalid public key format)
            Toast.makeText(this, "Cant initialize SDK. Please set REPLACE_ME values on Config.kt file.", Toast.LENGTH_LONG).show()
        }
        requestQueue = Volley.newRequestQueue(applicationContext)


        // Add Google Pay Button
        val googlePayButton = binding.googlePayButton

        googlePayButton.initialize(
            ButtonOptions
                .newBuilder()
                .setButtonType(ButtonConstants.ButtonType.PLAIN)
                .setCornerRadius(10)
                .setAllowedPaymentMethods(Lyra.getAllowedPaymentMethodsMock())
                .build()
        )

        googlePayButton.setOnClickListener {
            displayLoadingPanel()
            getPaymentContext(getPaymentParams(), getProcessOptionsDirectGooglePay())
        }

        val view = binding.root
        setContentView(view)
    }

    private fun getProcessOptionsDirectGooglePay(): HashMap<String, Any?> {
        val options = getProcessOptions()
        options[Lyra.PAYMENT_METHOD_TYPE] = LyraPaymentMethods.GOOGLE_PAY
        return options
    }

    private fun getProcessOptions(): HashMap<String, Any?> {
        val options = HashMap<String, Any?>()
        // options[Lyra.CUSTOM_PAY_BUTTON_LABEL] = "Hello World"
        return options
    }


    /**
     * onPayClick method
     * Invokes the payment
     *
     * @param view View of the Pay button
     */
    fun onPayClick(view: View) {
        displayLoadingPanel()
        getPaymentContext(getPaymentParams(), getProcessOptions())
    }

    /**
     * Create a JSONObject with all the payment params.
     *
     * Check API REST integration documentation
     */
    private fun getPaymentParams(): JSONObject {
        val json = JSONObject()
            .put("currency", Config.CURRENCY)
            .put("amount", Config.AMOUNT)
            .put("orderId", Config.ORDER_ID)
            .put(
                "customer",
                JSONObject("{\"email\":${Config.CUSTOMER_EMAIL}, \"reference\":${Config.CUSTOMER_REFERENCE}}")
            )
            .put("formTokenVersion", Lyra.getFormTokenVersion())
            .put("mode", Config.PAYMENT_MODE)
        if (Config.ASK_REGISTER_PAY) {
            json.put("formAction", "ASK_REGISTER_PAY")
        }
        return json
    }

    /**
     * Performs the create operation, calling the merchant server.
     * This call creates the session on server and retrieves the payment information necessary to continue the process
     *
     * @param paymentParams the operation parameters
     */
    private fun getPaymentContext(paymentParams: JSONObject, processOptions: HashMap<String, Any?>) {
        val jsonObjectRequest: JsonObjectRequest =
                object : JsonObjectRequest(
                        Method.POST, "${Config.SERVER_URL}/createPayment",
                        paymentParams,
                        Response.Listener { response ->
                            //In this sample, we extract the formToken from the serverResponse, call processServerResponse() which execute the process method of the SDK
                            processFormToken(extractFormToken(response.toString()), processOptions)
                        },
                        Response.ErrorListener { error ->
                            hideLoadingPanel()
                            //Please manage your error behaviour here
                            Toast.makeText(
                                applicationContext,
                                "Error Creating Payment: $error",
                                Toast.LENGTH_LONG
                            ).show()
                        }

            ) {
                /**
                 * Passing some request headers
                 */
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    return constructBasicAuthHeaders()
                }
            }

        requestQueue.add(jsonObjectRequest)
    }

    /**
     * Return a formToken if extraction is done correctly
     * Return an empty formToken if an error occur -> SDK will return an INVALID_FORMTOKEN exception
     */
    private fun extractFormToken(serverResponse: String): String {
        try {
            val answer = JSONObject(serverResponse).getJSONObject("answer")
            val formToken = answer.optString("formToken")
            if (formToken.isBlank()) {
                // TODO Please manage your error behaviour here
                // in this case, an error is present in the serverResponse, check the returned errorCode errorMessage
                Toast.makeText(applicationContext, "extractFormToken() -> formToken is empty" + "\n" +
                        "errorCode = " + answer.getString("errorCode") + "\n" +
                        "errorMessage = " + answer.optString("errorMessage") + "\n" +
                        "detailedErrorCode = " + answer.optString("detailedErrorCode") + "\n" +
                        "detailedErrorMessage = " + answer.optString("detailedErrorMessage"), Toast.LENGTH_LONG).show()
            }
            return formToken
        } catch (throwable: Throwable) {
            // TODO Please manage your error behaviour here
            // in this case, the serverResponse isn't as expected, please check the input serverResponse param
            Toast.makeText(applicationContext, "Cannot extract formToken from serverResponse", Toast.LENGTH_LONG).show()
            return ""
        }
    }

    /**
     * Calls the Lyra Mobile SDK in order to handle the payment operation
     *
     * @param formToken the formToken extracted from the information of the payment session
     */
    private fun processFormToken(formToken: String, processOptions: HashMap<String, Any?>) {
        hideLoadingPanel()
        try {// Open the payment form
            Lyra.process(supportFragmentManager, formToken, object : LyraHandler {
                override fun onSuccess(lyraResponse: LyraResponse) {
                    verifyPayment(lyraResponse)
                }

                override fun onError(lyraException: LyraException, lyraResponse: LyraResponse?) {
                    Toast.makeText(
                        applicationContext,
                        "Payment fail: ${lyraException.errorMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }, processOptions)
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Call the server to verify that the response received from payment platform through the SDK is valid and has not been modified.
     *
     * @param payload information about the result of the operation
     */
    fun verifyPayment(payload: LyraResponse) {
        val jsonObjectRequest: JsonObjectRequest =
            object : JsonObjectRequest(
                Method.POST, "${Config.SERVER_URL}/verifyResult",
                payload,
                Response.Listener { response ->
                    //Check the response integrity by verifying the hash on your server
                    Toast.makeText(
                        applicationContext,
                        "Payment success",
                        Toast.LENGTH_LONG
                    ).show()
                },
                Response.ErrorListener { error ->
                    //Manage error here, please refer to the documentation for more information
                    Toast.makeText(
                        applicationContext,
                        "Payment verification fail",
                        Toast.LENGTH_LONG
                    ).show()
                }
            ) {
                /**
                 * Passing some request headers
                 */
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    return constructBasicAuthHeaders()
                }
            }
        requestQueue.add(jsonObjectRequest)
    }

    private fun constructBasicAuthHeaders(): HashMap<String, String> {
        val headers =
            HashMap<String, String>()
        headers["Content-Type"] = "application/json; charset=utf-8"
        headers["Authorization"] =
            "Basic " + Base64.encodeToString(Config.CREDENTIALS.toByteArray(), Base64.NO_WRAP)
        return headers
    }


    /**
     * Hide the loading panel
     */
    fun hideLoadingPanel() {
        findViewById<View>(R.id.loadingPanel).visibility = View.GONE
    }

    /**
     * Display the loading panel
     */
    fun displayLoadingPanel() {
        findViewById<View>(R.id.loadingPanel).visibility = View.VISIBLE
    }
}