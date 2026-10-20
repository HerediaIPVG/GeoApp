# Declaración de Uso de Inteligencia Artificial

**Asignatura:** Taller de Nuevas Tecnologías  
**Proyecto:** GeoApp (Aplicación Android en Java)  
**Estudiante:** Felipe Heredia  
**Fecha:** Octubre 2026  

---

## 1. Herramienta Utilizada y Finalidad Académica

Durante el desarrollo del proyecto **GeoApp**, se utilizó el asistente de Inteligencia Artificial **Google Gemini / Antigravity** como herramienta de apoyo pedagógico, estructuración de arquitectura y consultas técnica. Su finalidad fue establecer una arquitectura basada en `Activities` tradicionales en Java, resolver dudas respecto a la integración de Google Play Services (`FusedLocationProviderClient` y `SupportMapFragment`) y robustecer los comentarios explicativos del código para la preparación de la defensa oral.

---

## 2. Prompts Principales Utilizados

1. **Estructura y configuración segura:**  
   *"Generar configuración de build.gradle y AndroidManifest.xml en Java para leer la API Key de Google Maps desde local.properties y configurar .gitignore excluyendo google-services.json y local.properties."*

2. **Formulario y validaciones (RF01 y RF02):**  
   *"Crear formulario en Android con Java usando EditText, Spinner de categorías, fecha/hora automática con SimpleDateFormat y método validarCampos() con setError."*

3. **Ubicación GPS y contingencias (RF04):**  
   *"Implementar FusedLocationProviderClient en Java solicitando permisos en tiempo de ejecución y controlando explícitamente en onRequestPermissionsResult cuando el permiso es denegado o cuando location es null."*

4. **Persistencia en Firestore y RecyclerView (RF03):**  
   *"Guardar objetos del modelo Incidencia en la colección 'incidencias' de Firebase Cloud Firestore y listarlos en un RecyclerView con un Adapter simple en Java."*

5. **Google Maps y Sensor Acelerómetro (RF05 y RF06):**  
   *"Mostrar marcadores de Firestore en un SupportMapFragment e implementar SensorEventListener para el acelerómetro detectando sacudidas y validando si el sensor es null."*

---

## 3. Trabajo de Revisión, Configuración y Pruebas Realizado por el Estudiante

El código sugerido por la herramienta fue íntegramente revisado, adaptado, integrado y testeado por mi parte en **Android Studio**, llevando a cabo personalmente las siguientes tareas:

* **Creación y vinculación de servicios en la nube:** Creación del proyecto en Firebase Console, vinculación del paquete `cl.heredia.geoapp`, descarga e integración local de `app/google-services.json`, habilitación de Cloud Firestore y publicación de las reglas de seguridad (`firestore.rules`).

* **Gestión de credenciales de Google Cloud:** Generación de la API Key de Maps SDK for Android e inyección local en `local.properties`, verificando mediante `git status` que ningún archivo sensible fuese subido al repositorio público de GitHub.

* **Pruebas funcionales y de contingencia en emulador/dispositivo físico:** Ejecución y verificación manual de los casos de prueba documentados en el `README.md` (denegación de permisos GPS, simulación de coordenadas y movimiento del acelerómetro en los ejes X, Y, Z).

* **Estudio y comprensión del código:** Análisis de cada clase, ciclo de vida (`onCreate`, `onResume`, `onPause`) para explicar y defender el funcionamiento integral del sistema en la interrogación oral.
