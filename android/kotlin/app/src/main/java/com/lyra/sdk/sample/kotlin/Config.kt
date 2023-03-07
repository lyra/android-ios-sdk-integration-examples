package com.lyra.sdk.sample.kotlin

object Config {

    // FIXME: change by the right merchant payment server url
    const val SERVER_URL = "<REPLACE_ME>" // without / at the end, example https://myserverurl.com

    // FIXME: Change by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
    const val API_SERVER_NAME = "<REPLACE_ME>" // without / at the end, example https://myapiservername.com

    // FIXME: change by your public key
    const val PUBLIC_KEY = "<REPLACE_ME>"

    // Environment TEST or PRODUCTION, refer to documentation for more information
    // FIXME: change by your targeted environment
    const val PAYMENT_MODE = "TEST"


    // TRUE to display a "register the card" switch in the payment form
    const val ASK_REGISTER_PAY = false

    // Payment parameters
    // FIXME: change currency for your targeted environment
    const val CURRENCY = "EUR"
    const val AMOUNT = "100"
    const val ORDER_ID = ""

    //Customer informations
    const val CUSTOMER_EMAIL = "customeremail@domain.com"
    const val CUSTOMER_REFERENCE = "customerReference"

    //Basic auth
    // FIXME: set your basic auth credentials
    const val SERVER_AUTH_USER = "<REPLACE_ME>"
    const val SERVER_AUTH_TOKEN = "<REPLACE_ME>"
    const val CREDENTIALS: String = "$SERVER_AUTH_USER:$SERVER_AUTH_TOKEN"
}