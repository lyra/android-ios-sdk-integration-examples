package com.lyra.sdk.sample.java;

public class Config {
  // Merchant server url
  // FIXME: change by the right payment server
  public static final String SERVER_URL = "<REPLACE_ME>"; // without / at the end, example https://myserverurl.com

  // Public key
  // FIXME: change by your public key
  public static final String PUBLIC_KEY = "<REPLACE_ME>";

  // FIXME: Change by the right REST API Server Name (available in merchant BO: Settings->Shop->REST API Keys)
  public static final String API_SERVER_NAME = "<REPLACE_ME>"; // without / at the end, example https://myapiservername.com

  // Environment TEST or PRODUCTION, refer to documentation for more information
  // FIXME: change by your targeted environment
  public static final String PAYMENT_MODE = "TEST";

  // FIXME: activate if we want to ask to register the card
  public static final boolean ASK_REGISTER_PAY = false;

  //Basic auth
  // FIXME: set your basic auth credentials
  public static final String SERVER_AUTH_USER = "<REPLACE_ME>";
  public static final String SERVER_AUTH_TOKEN = "<REPLACE_ME>";
  public static final String CREDENTIALS = SERVER_AUTH_USER + ":" + SERVER_AUTH_TOKEN;

  // Payment parameters
  // Change by the desired parameters if necessary
  public static final String AMOUNT = "100";
  public static final String CURRENCY = "EUR";
  public static final String ORDER_ID = "";

  // Customer information parameters
  // Change by the desired parameters if necessary
  public static final String CUSTOMER_EMAIL = "";
  public static final String CUSTOMER_REFERENCE = "";
}
