package com.lyra.sdk.sample.kotlin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.lyra.sdk.Lyra
import com.lyra.sdk.callback.LyraHandler
import com.lyra.sdk.callback.LyraResponse
import com.lyra.sdk.exception.LyraException
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

    companion object {
        // FIXME: change by the right merchant payment server url
        private const val SERVER_URL = "<REPLACE_ME>"

        // FIXME: change by your public key
        private const val PUBLIC_KEY = "<REPLACE_ME>"

        // Environment TEST or PRODUCTION, refer to documentation for more information
        // FIXME: change by your targeted environment
        private const val PAYMENT_MODE = "TEST"

        // TRUE to display a "register the card" switch in the payment form
        private const val ASK_REGISTER_PAY = false

        // FIXME: Change by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
        private const val API_SERVER_NAME = "<REPLACE_ME>"
        
        // Payment parameters
        // FIXME: change currency for your targeted environment
        private const val CURRENCY = "EUR"
        private const val AMOUNT = "100"
        private const val ORDER_ID = ""

        //Customer informations
        private const val CUSTOMER_EMAIL = "customeremail@domain.com"
        private const val CUSTOMER_REFERENCE = "customerReference"
    }

    // Initialize a new RequestQueue instance
    private lateinit var requestQueue: RequestQueue

    private fun getOptions(): HashMap<String, Any> {
        val options = HashMap<String, Any>()
        options[Lyra.OPTION_API_SERVER_NAME] = API_SERVER_NAME
        options[Lyra.OPTION_NFC_ENABLED] = false
        options[Lyra.OPTION_CARD_SCANNING_ENABLED] = false
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
        setContentView(R.layout.activity_main)
        try {
            // FIXME: Change by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
            Lyra.initialize(applicationContext, PUBLIC_KEY, getOptions())
        } catch (exception: Exception){
            // handle possible exceptions when initializing SDK (Ex: invalid public key format)
        }
        requestQueue = Volley.newRequestQueue(applicationContext)
    }

    /**
     * onPayClick method
     * Invokes the payment
     *
     * @param view View of the Pay button
     */
    fun onPayClick(view: View){
        getPaymentContext(getPaymentParams())
    }

    /**
     * Create a JSONObject with all the payment params.
     *
     * Check API REST integration documentation
     */
    private fun getPaymentParams(): JSONObject {
        val json = JSONObject()
            .put("currency", CURRENCY)
            .put("amount", AMOUNT)
            .put("orderId", ORDER_ID)
            .put(
                "customer",
                JSONObject("{\"email\":$CUSTOMER_EMAIL, \"reference\":$CUSTOMER_REFERENCE}")
            )
            .put("formTokenVersion", Lyra.getFormTokenVersion())
            .put("mode", PAYMENT_MODE)
        if (ASK_REGISTER_PAY) {
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
    private fun getPaymentContext(paymentParams: JSONObject) {
        requestQueue.add(JsonObjectRequest(Request.Method.POST,
            "${SERVER_URL}/createPayment",
            paymentParams,
            Response.Listener { response ->
                //In this sample, the processPayment checks the response and will call the process method of the SDK if the response is good.
                processPayment(response.toString())
            },
            Response.ErrorListener { error ->
                //Please manage your error behaviour here
                Toast.makeText(
                    applicationContext,
                    "Error Creating Payment",
                    Toast.LENGTH_LONG
                ).show()
            }
        ))
    }

    /**
     * Calls the Lyra Mobile SDK in order to handle the payment operation
     *
     * @param createResponse the information of the payment session
     */
    private fun processPayment(createResponse: String?) {
        // Open the payment form
        Lyra.process(supportFragmentManager, createResponse!!, object : LyraHandler {
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
        })
    }

    /**
     * Call the server to verify that the response received from payment platform through the SDK is valid and has not been modified.
     *
     * @param payload information about the result of the operation
     */
    fun verifyPayment(payload: LyraResponse) {
        requestQueue.add(JsonObjectRequest(Request.Method.POST,
            "${SERVER_URL}/verifyResult",
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
        ))
    }
}
