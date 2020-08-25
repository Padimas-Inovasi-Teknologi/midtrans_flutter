import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:midtransflutter/midtrans_flutter.dart';

void main() => runApp(MidFlutterDemo());

class MidFlutterDemo extends StatefulWidget {
  @override
  _MidFlutterDemoState createState() => _MidFlutterDemoState();
}

class _MidFlutterDemoState extends State<MidFlutterDemo> {
  TextEditingController _creditCardCtrl = TextEditingController();
  TextEditingController _expiryMonthCtrl = TextEditingController();
  TextEditingController _expiryYearCtrl = TextEditingController();
  TextEditingController _cvvCtrl = TextEditingController();
  TextEditingController _amountCtrl = TextEditingController();
  String _error = "";
  String _result = "";

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: SingleChildScrollView(child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: _creditCardCtrl,
                decoration: InputDecoration(hintText: "16 digits only", labelText: "Credit Card"),
                keyboardType: TextInputType.number,
                maxLength: 16,
              ),
              Row(children: [
                Expanded(
                  child: TextField(
                    controller: _expiryMonthCtrl,
                    decoration:
                    InputDecoration(hintText: "2 digits only", labelText: "Expiry Month"),
                    keyboardType: TextInputType.number,
                    maxLength: 2,
                  ),
                ),
                Container(width: 16.0),
                Expanded(
                  child: TextField(
                    controller: _expiryYearCtrl,
                    decoration:
                    InputDecoration(hintText: "4 digits only", labelText: "Expiry Year"),
                    keyboardType: TextInputType.number,
                    maxLength: 4,
                  ),
                )
              ]),
              TextField(
                controller: _cvvCtrl,
                decoration: InputDecoration(hintText: "3 digits only", labelText: "CVV"),
                keyboardType: TextInputType.number,
                maxLength: 3,
              ),
              TextField(
                controller: _amountCtrl,
                decoration: InputDecoration(hintText: "Amount of charge", labelText: "Amount"),
                keyboardType: TextInputType.number,
              ),
              Padding(
                padding: EdgeInsets.symmetric(vertical: 16.0),
                child: _error != ""
                    ? Text(
                  _error,
                  style: TextStyle(color: Colors.red),
                )
                    : Text(_result),
              ),
              FlatButton(
                child: Text("Submit"),
                onPressed: () async {
                  _error = "";
                  _result = "";

                  if ((_creditCardCtrl.text ?? "").isEmpty) {
                    _error = "Credit Card must be filled!";
                  } else if ((_expiryMonthCtrl.text ?? "").isEmpty) {
                    _error = "Expiry Month must be filled!";
                  } else if ((_expiryYearCtrl.text ?? "").isEmpty) {
                    _error = "Expiry Year must be filled!";
                  } else if ((_cvvCtrl.text ?? "").isEmpty) {
                    _error = "CVV must be filled!";
                  } else if ((_amountCtrl.text ?? "").isEmpty) {
                    _error = "Amount must be filled!";
                  }

                  if (_error == "") {
                    bool configured = await MidtransFlutter.configure(clientKey: "SB-Mid-client-StNU2Niogny-bdPT");

                    if (configured) {
                      try {
                        _result = await MidtransFlutter.generateCreditCardToken(
                            creditCardNumber: _creditCardCtrl.text,
                            expiryMonth: int.tryParse(_expiryMonthCtrl.text),
                            expiryYear: int.tryParse(_expiryYearCtrl.text),
                            cvv: int.tryParse(_cvvCtrl.text),
                            amount: double.tryParse(_amountCtrl.text));
                      } on Exception catch (e) {
                        _error = "$e";
                      }
                    }
                  }
                  print("_result => $_result");
                  setState(() {});
                },
                color: Theme.of(context).primaryColor,
                textColor: Colors.white,
              )
            ],
          ),
        ),),
      ),
    );
  }
}

