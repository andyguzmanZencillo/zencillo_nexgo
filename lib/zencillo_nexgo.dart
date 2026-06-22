import 'package:failures/failures.dart';
import 'package:oxidized/oxidized.dart';

import 'zencillo_nexgo_platform_interface.dart';

class ZencilloNexgo {
  static Future<Result<Unit, Failure>> print({
    required String text,
    String code = '',
    bool isQr = false,
  }) {
    return ZencilloNexgoPlatform.instance.print(
      text: text,
      code: code,
      isQr: isQr,
    );
  }

  static Future<Result<String, Failure>> scan() {
    return ZencilloNexgoPlatform.instance.scan();
  }

  static Future<Result<String, Failure>> nfc() {
    return ZencilloNexgoPlatform.instance.nfc();
  }
}
