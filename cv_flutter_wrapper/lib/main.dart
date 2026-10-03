import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:webview_flutter/webview_flutter.dart';

const navy = Color(0xFF0F1C2E);
const gold = Color(0xFFC9A45C);
const goldLight = Color(0xFFE6CE96);
const paper = Color(0xFFF5F3EE);
const navyDark = Color(0xFF0B1422);

/// Ruta de la hoja de vida web empaquetada como asset (Opción A, offline).
const webAsset = 'assets/web/index.html';

/// Los enlaces que no son de la web local (mailto:, tel:, https://...) se
/// abren con la app nativa correspondiente en lugar de dentro del WebView.
bool esEnlaceExterno(String url) {
  final uri = Uri.tryParse(url);
  if (uri == null) return false;
  return uri.scheme != 'file' && uri.scheme != 'about' && uri.scheme != 'data';
}

void main() => runApp(const CvWrapperApp());

class CvWrapperApp extends StatefulWidget {
  const CvWrapperApp({super.key});

  @override
  State<CvWrapperApp> createState() => _CvWrapperAppState();
}

class _CvWrapperAppState extends State<CvWrapperApp> {
  ThemeMode _modo = ThemeMode.light;

  ThemeData _tema(Brightness brillo) => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: navy,
          primary: brillo == Brightness.light ? navy : gold,
          secondary: gold,
          brightness: brillo,
        ),
        scaffoldBackgroundColor: brillo == Brightness.light ? paper : navyDark,
        appBarTheme: const AppBarTheme(
          backgroundColor: navy,
          foregroundColor: goldLight,
        ),
      );

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Hoja de Vida',
      debugShowCheckedModeBanner: false,
      theme: _tema(Brightness.light),
      darkTheme: _tema(Brightness.dark),
      themeMode: _modo,
      home: CvHomePage(
        oscuro: _modo == ThemeMode.dark,
        onCambiarTema: () => setState(() {
          _modo = _modo == ThemeMode.dark ? ThemeMode.light : ThemeMode.dark;
        }),
      ),
    );
  }
}

class CvHomePage extends StatefulWidget {
  const CvHomePage({super.key, required this.oscuro, required this.onCambiarTema});

  final bool oscuro;
  final VoidCallback onCambiarTema;

  @override
  State<CvHomePage> createState() => _CvHomePageState();
}

class _CvHomePageState extends State<CvHomePage> {
  late final WebViewController _web;
  final _cronometro = Stopwatch();
  int _progreso = 0;
  int _seccion = 0;
  int? _msCarga;

  static const _secciones = ['inicio', 'experiencia', 'habilidades', 'contacto'];

  @override
  void initState() {
    super.initState();
    _web = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(paper)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (_) => _cronometro
            ..reset()
            ..start(),
          onProgress: (p) => setState(() => _progreso = p),
          onPageFinished: (_) {
            _cronometro.stop();
            setState(() => _msCarga = _cronometro.elapsedMilliseconds);
            _sincronizarWeb();
          },
          onNavigationRequest: (peticion) {
            if (esEnlaceExterno(peticion.url)) {
              launchUrl(Uri.parse(peticion.url), mode: LaunchMode.externalApplication);
              return NavigationDecision.prevent;
            }
            return NavigationDecision.navigate;
          },
        ),
      )
      ..loadFlutterAsset(webAsset);
  }

  @override
  void didUpdateWidget(CvHomePage anterior) {
    super.didUpdateWidget(anterior);
    if (anterior.oscuro != widget.oscuro) _sincronizarWeb();
  }

  /// Controles nativos -> web: oculta el botón de tema de la web y aplica el tema de Flutter.
  Future<void> _sincronizarWeb() async {
    final tema = widget.oscuro ? 'dark' : 'light';
    await _web.setBackgroundColor(widget.oscuro ? navyDark : paper);
    await _web.runJavaScript(
      'if (window.cvApp) { cvApp.setEmbedded(true); cvApp.setTheme("$tema"); }',
    );
  }

  void _irASeccion(int i) {
    setState(() => _seccion = i);
    _web.runJavaScript('window.cvApp && cvApp.scrollTo("${_secciones[i]}")');
  }

  void _recargar() {
    setState(() {
      _progreso = 0;
      _msCarga = null;
    });
    _web.reload();
  }

  void _compartir() {
    Share.share(
      'Hoja de vida de Jhosua Lascano Villarreal\n'
      'Recepción Hotelera · Conductor Profesional\n'
      'Correo: rogerlasxvilla@gmail.com\n'
      'Teléfono: 0960260813',
      subject: 'Hoja de vida – Jhosua Lascano Villarreal',
    );
  }

  void _mostrarRendimiento() {
    showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Rendimiento'),
        content: Text(
          _msCarga == null
              ? 'La página aún se está cargando…'
              : 'La hoja de vida web se cargó en $_msCarga ms '
                  '(desde assets locales, sin internet).',
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Cerrar')),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mi Hoja de Vida'),
        actions: [
          IconButton(
            tooltip: widget.oscuro ? 'Tema claro' : 'Tema oscuro',
            icon: Icon(widget.oscuro ? Icons.light_mode : Icons.dark_mode),
            onPressed: widget.onCambiarTema,
          ),
          IconButton(tooltip: 'Recargar', icon: const Icon(Icons.refresh), onPressed: _recargar),
          IconButton(tooltip: 'Compartir perfil', icon: const Icon(Icons.share), onPressed: _compartir),
          IconButton(tooltip: 'Rendimiento', icon: const Icon(Icons.speed), onPressed: _mostrarRendimiento),
        ],
        bottom: _progreso < 100
            ? PreferredSize(
                preferredSize: const Size.fromHeight(3),
                child: LinearProgressIndicator(value: _progreso / 100, color: gold, minHeight: 3),
              )
            : null,
      ),
      body: SafeArea(child: WebViewWidget(controller: _web)),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _seccion,
        onDestinationSelected: _irASeccion,
        destinations: const [
          NavigationDestination(icon: Icon(Icons.person_outline), selectedIcon: Icon(Icons.person), label: 'Perfil'),
          NavigationDestination(icon: Icon(Icons.work_outline), selectedIcon: Icon(Icons.work), label: 'Experiencia'),
          NavigationDestination(icon: Icon(Icons.star_outline), selectedIcon: Icon(Icons.star), label: 'Habilidades'),
          NavigationDestination(icon: Icon(Icons.mail_outline), selectedIcon: Icon(Icons.mail), label: 'Contacto'),
        ],
      ),
    );
  }
}
