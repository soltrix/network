import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class NetworkService {
  static const _channel = "NETWORK";
  static const _platform = MethodChannel(_channel);

  Future<String> getNetworkInfo() async {
    try {
      return await _platform.invokeMethod('getNetworkInfo');
    } on PlatformException catch (e) {
      return "Network Info: '${e.message}'.";
    }
  }
}