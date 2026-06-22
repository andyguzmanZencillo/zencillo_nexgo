import 'package:failures/failures.dart';
import 'package:oxidized/oxidized.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'zencillo_nexgo_method_channel.dart';

abstract class ZencilloNexgoPlatform extends PlatformInterface {
  ZencilloNexgoPlatform() : super(token: _token);

  static final Object _token = Object();

  static ZencilloNexgoPlatform _instance = MethodChannelZencilloNexgo();

  static ZencilloNexgoPlatform get instance => _instance;

  static set instance(ZencilloNexgoPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<Result<Unit, Failure>> print({
    required String text,
    String code = '',
    bool isQr = false,
  }) {
    throw UnimplementedError('print() has not been implemented.');
  }

  Future<Result<String, Failure>> scan() {
    throw UnimplementedError('scan() has not been implemented.');
  }

  Future<Result<String, Failure>> nfc() {
    throw UnimplementedError('nfc() has not been implemented.');
  }
}
