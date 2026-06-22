import 'package:oxidized/oxidized.dart';

import 'zencillo_nexgo_platform_interface.dart';

class ZencilloNexgo {
  static Future<Result<Unit, String>> print({
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

  static Future<Result<String, String>> scan() {
    return ZencilloNexgoPlatform.instance.scan();
  }

  static Future<Result<String, String>> nfc() {
    return ZencilloNexgoPlatform.instance.nfc();
  }
}
