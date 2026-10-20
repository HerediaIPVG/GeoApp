# GeoApp - Aplicación Android de Reporte de Incidencias en Terreno

Proyecto desarrollado en **Java** para la asignatura **Taller de Nuevas Tecnologías** (Ingeniería en Informática), equivalente al **30% de la nota final** del curso. Permite registrar incidencias urbanas georreferenciadas mediante GPS, almacenarlas en **Firebase Cloud Firestore**, visualizarlas en un listado con `RecyclerView` e interactuar con ellas en **Google Maps**, además de monitorear vibraciones y movimientos bruscos en terreno mediante el **Sensor Acelerómetro**.

---

## 1. Entorno de Desarrollo y Requisitos Técnicos

* **IDE:** Android Studio Ladybug / Koala / Iguana o superior.
* **Lenguaje:** Java (JDK 11 / 17 / 21 compatible).
* **SDK Mínimo:** API 24 (Android 7.0 Nougat) / API 26.
* **SDK Objetivo / Compilación:** API 34 (Android 14) / API 35.
* **Paquete Base:** `cl.heredia.geoapp`.
* **Arquitectura:** Basada en `AppCompatActivity` tradicionales (`MainActivity`, `RegistroActivity`, `ListadoActivity`, `MapaActivity`) y modelo de datos POJO (`Incidencia.java`).

---

## 2. Configuración Segura de Credenciales (RF08)

Por razones de seguridad y buenas prácticas de desarrollo en la nube (RF08), este repositorio **excluye explícitamente** en su archivo `.gitignore` tanto las credenciales de Firebase (`app/google-services.json`) como las propiedades locales del SDK y API Key (`local.properties`).

Para compilar y ejecutar este proyecto tras clonarlo desde GitHub, sigue estos dos pasos:

### Paso A: Configurar Firebase (`google-services.json`)
1. Ingresa a [Firebase Console](https://console.firebase.google.com/) y abre tu proyecto.
2. Registra una aplicación Android con el paquete exacto: `cl.heredia.geoapp`.
3. Descarga el archivo `google-services.json` y colócalo dentro de la carpeta `app/` del proyecto (`app/google-services.json`).
4. En el menú lateral, ve a **Firestore Database -> Reglas**, copia el contenido del archivo `firestore.rules` de este repositorio y presiona **Publicar**.

### Paso B: Configurar la API Key de Google Maps (`local.properties`)
1. Abre el archivo `local.properties` ubicado en la raíz del proyecto (creado automáticamente por Android Studio con la ruta de tu SDK).
2. Añade una línea al final del archivo con tu clave de API de Google Maps:
   ```properties
   MAPS_API_KEY=AIzaSyTU_API_KEY_DE_GOOGLE_MAPS_AQUI
   ```
3. Sincroniza el proyecto con Gradle (**File -> Sync Project with Gradle Files**). El archivo `app/build.gradle.kts` inyectará de forma segura la variable `${MAPS_API_KEY}` en `AndroidManifest.xml` sin exponerla en el control de versiones.

---

## 3. Historial de Versiones y Tabla de Commits

A continuación, se detalla la evolución incremental del proyecto a través de sus principales hitos y commits en el repositorio:

| Versión | Hito / Título del Commit | Mejoras e Implementaciones Principales |
| :---: | :--- | :--- |
| **🏷️ V1.0** | **Configuración base, `.gitignore` seguro, modelo `Incidencia` y esqueleto de activities** | Establece configuración en Gradle (`compileSdk 35`, `minSdk 26`), `AndroidManifest.xml` con declaración de permisos, `.gitignore` seguro (cumpliendo **RF08**), modelo de datos `Incidencia` con constructor vacío y las 4 pantallas base (esqueleto). |
| **🏷️ V2.0** | **Interfaz Principal, Formulario de Registro y Validaciones** | Diseña el menú principal con 3 botones de navegación mediante `Intent` explícitos, formulario de captura completo con `Spinner` de categorías, asignación de fecha automática y método `validarCampos()` con retroalimentación visual vía `setError()`. |
| **🏷️ V3.0** | **Integración de GPS y Permisos en Tiempo de Ejecución** | Conecta la geolocalización real mediante *Google Play Services*, solicita permisos de ubicación en tiempo de ejecución, controla el rechazo de permisos con `AlertDialog` explicativo y gestiona la contingencia de GPS apagado. |
| **🏷️ V4.0** | **Persistencia en Firebase Cloud Firestore y Listado con `RecyclerView`** | Persiste los reportes en la colección `'incidencias'` de Firebase Cloud Firestore, implementa la visualización mediante `RecyclerView` junto a su `IncidenciaAdapter` personalizado y controla contingencias ante fallos de red. |
| **🏷️ V5.0** | **Google Maps con Marcadores y Sensor Acelerómetro** | Muestra marcadores georreferenciados en el mapa interactivo de Google Maps (`SupportMapFragment`), realiza lecturas en tiempo real de los ejes $X, Y, Z$ del acelerómetro en `MainActivity` y detecta sacudidas bruscas ($> 15\text{ m/s}^2$). |
| **🏷️ V6.0** | **Reglas de Seguridad de Firestore, Documentación y Declaración de IA** | Consolida las reglas de acceso y validación en `firestore.rules`, finaliza la documentación técnica en `README.md` e incorpora el documento de transparencia `DECLARACION_IA.md`. |