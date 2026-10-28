# Ev2-DAM · Punto Verde (MVP académico)

App Android (Kotlin + Jetpack Compose + Material 3) para que estudiantes en formación
guarden y administren sus documentos de estudio **sin conexión**. Todos los datos son ficticios.

## Funciones de esta etapa
1. **Bienvenida** con perfiles de prueba (Estudiante Demo A, B y C).
2. **Listado de documentos** del usuario, con datos de prueba precargados.
3. **Cargar documento** desde el selector de archivos del sistema (se copia al almacenamiento privado de la app).
4. **Eliminar documento** (con confirmación; borra el registro y el archivo).
5. **Aislamiento por usuario**: cada perfil ve y puede borrar únicamente sus propios documentos.

## Arquitectura (MVVM)
```
ui/screens      -> BienvenidaScreen, DocumentosScreen   (solo dibujan y avisan eventos)
ui/navigation   -> AppNavegacion                         (bienvenida -> documentos/{usuarioId})
viewmodel       -> BienvenidaViewModel, DocumentosViewModel (estado de pantalla + lógica de UI)
data            -> DocumentoRepository, Room (Usuario, Documento, DAOs, AppDatabase, DatosDePrueba)
util            -> Formato (tamaño y fecha legibles)
```

## Perfiles de prueba
| Perfil | Código | Documentos de ejemplo |
|---|---|---|
| Estudiante Demo A | DEMO-001 | 3 |
| Estudiante Demo B | DEMO-002 | 2 |
| Estudiante Demo C | DEMO-003 | 0 (prueba de lista vacía) |

> Los datos de prueba se insertan una sola vez, al crear la base de datos. Para volver a
> generarlos: desinstalar la app o "Borrar datos" desde los ajustes del teléfono.

## Pruebas
- Unitarias (JVM): `./gradlew testDebugUnitTest`
- Instrumentadas (emulador/teléfono): `./gradlew connectedDebugAndroidTest`
  (`AislamientoDocumentosTest` verifica que un usuario no ve ni borra documentos ajenos).
