import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'utils.dart';

class NetworkService {
  static const _channel = "NETWORK";
  static const _platform = MethodChannel(_channel);
  // 1. Объявляем EventChannel
  static const _eventChannel = EventChannel("STREAM");

  final networkStream = _eventChannel
      .receiveBroadcastStream()
      .distinct()
      .map((dynamic event) => intToConnection(event as int));

  Future<String> getNetworkInfo() async {
    try {
      return await _platform.invokeMethod('getNetworkInfo');
    } on PlatformException catch (e) {
      return "Network Info: '${e.message}'.";
    }
  }
}