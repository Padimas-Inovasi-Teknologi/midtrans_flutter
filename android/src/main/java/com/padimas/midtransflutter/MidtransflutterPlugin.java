package com.padimas.midtransflutter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.midtrans.sdk.corekit.BuildConfig;
import com.midtrans.sdk.corekit.callback.CardTokenCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.SdkCoreFlowBuilder;
import com.midtrans.sdk.corekit.models.CardTokenRequest;
import com.midtrans.sdk.corekit.models.TokenDetailsResponse;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_OK;

/** MidtransflutterPlugin */
public class MidtransflutterPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.ActivityResultListener {

  private Context context;
  private Activity activity;
  public static final int REQUEST_RENT_FEE = 4569;
  private static String token;
  private static Result result;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "midtransflutter");
    channel.setMethodCallHandler(new MidtransflutterPlugin());
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "midtransflutter");
    channel.setMethodCallHandler(new MidtransflutterPlugin());
    MidtransflutterPlugin plugin = new MidtransflutterPlugin();
    channel.setMethodCallHandler(plugin);

    plugin.context = registrar.context();
    plugin.activity = registrar.activity();

    registrar.addActivityResultListener(plugin);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("configure")) {
      String clientKey = call.argument("clientKey");
      Object isProductionRaw = call.argument("isProduction");
      if (isProductionRaw == null) isProductionRaw = "false";
      boolean isProduction = Boolean.valueOf(isProductionRaw.toString());
      this.result = result;

      configure(clientKey, isProduction);
    } else if (call.method.equals("generateCreditCardToken")) {
      String creditCardNumber = call.argument("creditCardNumber");
      String expiryMonth = call.argument("expiryMonth").toString();
      String expiryYear = call.argument("expiryYear").toString();
      String cvv = call.argument("cvv").toString();
      Object amountRaw = call.argument("amount");
      if (amountRaw == null) amountRaw = "0.0";
      double amount = Double.valueOf(amountRaw.toString());
      this.result = result;

      submitCreditCard(creditCardNumber, cvv, expiryMonth, expiryYear, amount);
    } else {
      result.notImplemented();
    }
  }

  private void configure(String clientKey, boolean isProduction) {
    String merchantBaseUrl;
    if (!isProduction) {
      merchantBaseUrl = "https://api.sandbox.veritrans.co.id/v2/transactions";
    } else {
      merchantBaseUrl = "https://api.veritrans.co.id/v2/transactions";
    }

    SdkCoreFlowBuilder.init()
            .setContext(this.context)
            .setClientKey(clientKey)
            .setMerchantBaseUrl(merchantBaseUrl)
            .enableLog(BuildConfig.DEBUG)
            .buildSDK();

    this.result.success("");
  }

  private void submitCreditCard(String cardNumber, String cvv, String expireMonth,
                                String expireYear, double totalPrice) {
    CardTokenRequest cardTokenRequest = new CardTokenRequest(
            // Card number
            cardNumber,
            cvv,
            expireMonth,
            expireYear,
            MidtransSDK.getInstance().getClientKey());

    cardTokenRequest.setGrossAmount(totalPrice);

    cardTokenRequest.setSecure(true);

    MidtransSDK.getInstance().getCardToken(cardTokenRequest, new CardTokenCallback() {
      @Override
      public void onSuccess(TokenDetailsResponse tokenDetailsResponse) {
        String token = tokenDetailsResponse.getTokenId();
        String url = tokenDetailsResponse.getRedirectUrl();

        MidtransflutterPlugin.this.token = token;
        Intent intent = new Intent(activity, WebviewVerifyActivity.class);
        intent.putExtra(WebviewVerifyActivity.EXTRA_URL, url);
        activity.startActivityForResult(intent, REQUEST_RENT_FEE);
      }

      @Override
      public void onFailure(TokenDetailsResponse tokenDetailsResponse, String errorMessage) {
        result.error("Error: " + errorMessage, "", "");
      }

      @Override
      public void onError(Throwable throwable) {
        result.error("Error: " + throwable.getMessage(), "", "");
      }
    });
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    return triggerResult(requestCode, resultCode, data);
  }

  public static boolean triggerResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == REQUEST_RENT_FEE) {
      if (resultCode == RESULT_OK) {
        if (result != null) result.success("Token: " + (token == null ? "" : token));
      } else {
        String cancelMessage = "3D Secure transaction canceled by user";
        String message = intent == null ? cancelMessage : intent.getStringExtra("message");
        result.error("Error", message, "");
      }
    } else {
      result.success("Failed!");
    }
    return false;
  }
}
