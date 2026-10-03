// ============================================================
//  Hoja de vida web – interactividad
//  Expone window.cvApp para que el contenedor Flutter lo controle.
// ============================================================
(function () {
  "use strict";

  const root = document.documentElement;
  const THEME_KEY = "cv-theme";

  // ---------- Tema claro / oscuro ----------
  function setTheme(mode) {
    if (mode !== "light" && mode !== "dark") return;
    root.setAttribute("data-theme", mode);
    try { localStorage.setItem(THEME_KEY, mode); } catch (e) { /* sin almacenamiento */ }
  }

  function currentTheme() {
    const attr = root.getAttribute("data-theme");
    if (attr) return attr;
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  }

  // Tema inicial: el guardado o, si no hay, el del sistema (siempre explícito en <html>)
  let saved = null;
  try { saved = localStorage.getItem(THEME_KEY); } catch (e) { /* sin almacenamiento */ }
  root.setAttribute("data-theme", saved || currentTheme());

  document.getElementById("themeToggle").addEventListener("click", function () {
    setTheme(currentTheme() === "dark" ? "light" : "dark");
  });

  // ---------- Edad y año ----------
  const edadEl = document.getElementById("edad");
  const nac = new Date(edadEl.dataset.nacimiento + "T00:00:00");
  const hoy = new Date();
  let edad = hoy.getFullYear() - nac.getFullYear();
  const m = hoy.getMonth() - nac.getMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) edad--;
  edadEl.textContent = edad;
  document.getElementById("year").textContent = hoy.getFullYear();

  // ---------- Desplegables ----------
  document.querySelectorAll(".accordion__head").forEach(function (btn) {
    btn.addEventListener("click", function () {
      const open = btn.getAttribute("aria-expanded") === "true";
      btn.setAttribute("aria-expanded", String(!open));
    });
  });

  // ---------- Filtrado de habilidades ----------
  const skills = Array.from(document.querySelectorAll(".skill"));
  const search = document.getElementById("skillSearch");
  const empty = document.getElementById("skillsEmpty");
  let filtro = "todas";

  function normalizar(txt) {
    return txt.toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "");
  }

  function aplicarFiltro() {
    const q = normalizar(search.value.trim());
    let visibles = 0;
    skills.forEach(function (li) {
      const okTipo = filtro === "todas" || li.dataset.tipo === filtro;
      const okTexto = !q || normalizar(li.textContent).includes(q);
      const mostrar = okTipo && okTexto;
      li.classList.toggle("is-hidden", !mostrar);
      if (mostrar) visibles++;
    });
    empty.hidden = visibles > 0;
  }

  document.querySelectorAll(".chip-filter").forEach(function (chip) {
    chip.addEventListener("click", function () {
      document.querySelectorAll(".chip-filter").forEach(function (c) { c.classList.remove("is-active"); });
      chip.classList.add("is-active");
      filtro = chip.dataset.filter;
      aplicarFiltro();
    });
  });
  search.addEventListener("input", aplicarFiltro);

  // ---------- Validación del formulario de contacto ----------
  const form = document.getElementById("contactForm");
  const ok = document.getElementById("formOk");
  const reglas = {
    nombre: function (v) { return v.length >= 3 ? "" : "Ingresa tu nombre (mínimo 3 caracteres)."; },
    correo: function (v) { return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v) ? "" : "Ingresa un correo válido."; },
    mensaje: function (v) { return v.length >= 10 ? "" : "El mensaje debe tener al menos 10 caracteres."; },
  };

  function validarCampo(input) {
    const error = reglas[input.name](input.value.trim());
    const field = input.closest(".field");
    field.classList.toggle("has-error", !!error);
    field.querySelector(".field__error").textContent = error;
    return !error;
  }

  Object.keys(reglas).forEach(function (name) {
    form.elements[name].addEventListener("blur", function (e) { validarCampo(e.target); });
  });

  form.addEventListener("submit", function (e) {
    e.preventDefault();
    const campos = Object.keys(reglas).map(function (n) { return form.elements[n]; });
    const valido = campos.map(validarCampo).every(Boolean);
    if (!valido) {
      ok.hidden = true;
      campos.find(function (c) { return c.closest(".field").classList.contains("has-error"); }).focus();
      return;
    }
    const asunto = encodeURIComponent("Contacto desde hoja de vida – " + form.elements.nombre.value.trim());
    const cuerpo = encodeURIComponent(form.elements.mensaje.value.trim() + "\n\n" +
      form.elements.nombre.value.trim() + " <" + form.elements.correo.value.trim() + ">");
    ok.textContent = "¡Gracias! Se abrirá tu aplicación de correo para enviar el mensaje.";
    ok.hidden = false;
    window.location.href = "mailto:rogerlasxvilla@gmail.com?subject=" + asunto + "&body=" + cuerpo;
    form.reset();
  });

  // ---------- Animación al hacer scroll ----------
  const secciones = document.querySelectorAll(".reveal");
  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) { en.target.classList.add("is-visible"); io.unobserve(en.target); }
      });
    }, { threshold: 0.12 });
    secciones.forEach(function (s) { io.observe(s); });
  } else {
    secciones.forEach(function (s) { s.classList.add("is-visible"); });
  }

  // ---------- API para el contenedor Flutter ----------
  window.cvApp = {
    setTheme: setTheme,
    getTheme: currentTheme,
    setEmbedded: function (on) { root.classList.toggle("embedded", !!on); },
    scrollTo: function (id) {
      const el = document.getElementById(id);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    },
  };
})();
