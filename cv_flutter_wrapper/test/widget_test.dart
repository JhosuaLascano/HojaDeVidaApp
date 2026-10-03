import 'package:cv_flutter_wrapper/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('los enlaces de contacto se abren fuera del WebView', () {
    expect(esEnlaceExterno('mailto:rogerlasxvilla@gmail.com'), isTrue);
    expect(esEnlaceExterno('tel:+593960260813'), isTrue);
    expect(esEnlaceExterno('https://wa.me/593960260813'), isTrue);
  });

  test('la web local se queda dentro del WebView', () {
    expect(esEnlaceExterno('file:///android_asset/flutter_assets/assets/web/index.html#contacto'), isFalse);
    expect(esEnlaceExterno('about:blank'), isFalse);
  });
}
