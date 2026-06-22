import 'package:failures/failures.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:oxidized/oxidized.dart';

import 'zencillo_nexgo_platform_interface.dart';

class MethodChannelZencilloNexgo extends ZencilloNexgoPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('zencillo_nexgo');

  @override
  Future<Result<Unit, Failure>> print({
    required String text,
    String code = '',
    bool isQr = false,
  }) async {
    return handleExceptions(() async {
      await methodChannel.invokeMethod<String>('print', {
        'text': text,
        'code': code,
        'isQr': isQr,
      });
      return unit;
    });
  }

  @override
  Future<Result<String, Failure>> scan() async {
    return handleExceptions(() async {
      final response = await methodChannel.invokeMethod<String>('scan');
      return response ?? '';
    });
  }

  @override
  Future<Result<String, Failure>> nfc() async {
    return handleExceptions(() async {
      final response = await methodChannel.invokeMethod<String>('nfc');
      return response ?? '';
    });
  }
}
