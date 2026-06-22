import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:oxidized/oxidized.dart';

import 'zencillo_nexgo_platform_interface.dart';

class MethodChannelZencilloNexgo extends ZencilloNexgoPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('zencillo_nexgo');

  @override
  Future<Result<Unit, String>> print({
    required String text,
    String code = '',
    bool isQr = false,
  }) async {
    try {
      await methodChannel.invokeMethod<String>('print', {
        'text': text,
        'code': code,
        'isQr': isQr,
      });
      return const Result.ok(unit);
    } on PlatformException catch (e) {
      return Result.err(e.message!);
    } catch (e, stacktrace) {
      log('nexgo FAILED ===> $e');
      log('nexgo FAILED ===> $stacktrace');
      return const Result.err('Algo falló!');
    }
  }

  @override
  Future<Result<String, String>> scan() async {
    try {
      final response = await methodChannel.invokeMethod<String>('scan');
      return Result.ok(response ?? '');
    } on PlatformException catch (e) {
      return Result.err(e.message!);
    } catch (e, stacktrace) {
      log('scan FAILED ===> $e');
      log('scan FAILED ===> $stacktrace');
      return const Result.err('Algo falló!');
    }
  }

  @override
  Future<Result<String, String>> nfc() async {
    try {
      final response = await methodChannel.invokeMethod<String>('nfc');
      return Result.ok(response ?? '');
    } on PlatformException catch (e) {
      return Result.err(e.message!);
    } catch (e, stacktrace) {
      log('nfc FAILED ===> $e');
      log('nfc FAILED ===> $stacktrace');
      return const Result.err('Algo falló!');
    }
  }
}
