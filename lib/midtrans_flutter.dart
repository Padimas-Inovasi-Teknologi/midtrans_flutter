import 'dart:async';

import 'package:flutter/services.dart';

class MidtransFlutter {
  static const MethodChannel _channel =
      const MethodChannel('midtransflutter');

  static Future<bool> configure({
    String clientKey,
    bool isProduction = false
  }) async {
    final String result = await _channel.invokeMethod('configure', {
      "clientKey": clientKey,
      "isProduction": isProduction
    });

    if (result != "") {
      throw Exception(result);
    }

    return true;
  }
  static Future<String> generateCreditCardToken({
    String creditCardNumber,
    int expiryMonth,
    int expiryYear,
    int cvv,
    double amount,
  }) async {
    assert(creditCardNumber != null && creditCardNumber.length == 16);
    assert(expiryMonth != null && expiryMonth >= 1 && expiryMonth <= 12);
    assert(expiryYear != null);
    assert(cvv != null);
    assert(amount != null && amount > 0.0 && amount <= 999999999);

    String result;

    try {
      result = await _channel.invokeMethod('generateCreditCardToken', {
        "creditCardNumber": creditCardNumber,
        "expiryMonth": expiryMonth,
        "expiryYear": expiryYear,
        "cvv": cvv,
        "amount": amount
      });
    } catch (e) {
      throw Exception(e.message); //handle android
    }

    if (result.indexOf("Token: ") == -1) {
      throw Exception(result); // handle ios
    }

    return result;
  }
}
