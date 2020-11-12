import Flutter
import UIKit
import MidtransCoreKit

private class MidtransConfiguration {
    var production: Bool = false

    init(production: Bool) {
        self.production = production
    }

    var merchantServerUrl: String {
        return self.production ? "https://api.veritrans.co.id/v2/transactions" : "https://api.sandbox.veritrans.co.id/v2/transactions";
    }

    var serverEnvironment: MidtransServerEnvironment {
        return self.production ? .production : .sandbox;
    }
}


public class SwiftMidtransflutterPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "midtransflutter", binaryMessenger: registrar.messenger())
        let instance = SwiftMidtransflutterPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == "configure" {
            configure( call, result: result);
        } else if call.method == "generateCreditCardToken" {
            generateCreditCardToken( call, result: result);
        } else {
            result("Error: Method not implemented");
        }
    }
    
    public func configure(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        var clientKey:String
        var isProduction:Bool
        
        if let args = call.arguments as? Dictionary<String, Any> {
            clientKey = args["clientKey"] as? String ?? ""
            isProduction = args["isProduction"] as? Bool ?? false
            
            let midtransConfig = MidtransConfiguration(production: isProduction);
            
            MidtransConfig.shared().setClientKey(clientKey, environment: midtransConfig.serverEnvironment, merchantServerURL: midtransConfig.merchantServerUrl)
            
            result("");
        } else {
            result("Error: Bad Arguments!");
        }
    }
    
    public func generateCreditCardToken(_ call: FlutterMethodCall, result: @escaping FlutterResult){
        
        if MidtransConfig.shared()?.clientKey == nil {
            result("Error: Please call configure first!")
        } else {
            MidtransCreditCardConfig.shared().secure3DEnabled = true
            
            MidtransCreditCardConfig.shared().tokenStorageEnabled = true
            
            if let args = call.arguments as? Dictionary<String, Any> {
                var creditCard: MidtransCreditCard = MidtransCreditCard();
                
                let creditCardNumber = args["creditCardNumber"] as? String ?? ""
                let expiryMonth = args["expiryMonth"] as? Int ?? 0
                let expiryYear = args["expiryYear"] as? Int ?? 0
                let cvv = args["cvv"] as? Int ?? 0
                let amount = args["amount"] as? Double ?? 0.0
                
                creditCard = MidtransCreditCard(number: creditCardNumber, expiryMonth: String(expiryMonth), expiryYear: String(expiryYear), cvv: String(cvv))
                
                let tokenRequest = MidtransTokenizeRequest(creditCard: creditCard, grossAmount: NSNumber(integerLiteral: Int(amount)))!
                
                if creditCard.number != nil{
                    MidtransClient.shared().generateToken(tokenRequest) { token, error in
                        DispatchQueue.main.async {
                            if let error = error{
                                result("\(error.localizedDescription)");
                            }
                            result("Token: \(token ?? "")");
                        }
                    }
                }else{
                    result("Error: Invalid Credit Card Data!");
                }
            } else {
                result("Error: Bad Arguments!");
            }
        }
    }
}
