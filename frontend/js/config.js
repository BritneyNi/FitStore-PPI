// ============================================================
//  FitStore — config.js
//  Configuracion del frontend. La URL base de la API se lee de
//  <meta name="api-base-url"> (ver index.html) para no tenerla
//  escrita en el codigo. Valor por defecto: desarrollo local.
// ============================================================
(function () {
  "use strict";

  var meta = document.querySelector('meta[name="api-base-url"]');
  var API_BASE_URL =
    (meta && meta.getAttribute("content")) || "http://localhost:8080/api";

  window.FitStore = window.FitStore || {};
  window.FitStore.config = { API_BASE_URL: API_BASE_URL };
})();
