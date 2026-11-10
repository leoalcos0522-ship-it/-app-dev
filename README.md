# Vape POS (MonitoringVape)

Simpleng POS + inventory monitoring para sa vape shop. Android (Kotlin + Jetpack Compose).
Offline, walang extra dependencies — naka-save ang data sa phone (SharedPreferences).

## Features
- **Dashboard (awtomatiko):** kita (profit), benta, ilang piraso ang nabawas, natitirang stock, at bawat produkto (nabenta / kita / natitira). Pwedeng Ngayon, 7 araw, 30 araw o Lahat. Nag-a-update agad kapag may bagong benta.
- **Benta:** isang tap lang sa **Benta ×1** (o **Ibang dami**) — awtomatikong nababawasan ang stock at pumapasok ang kita. May **UNDO** kapag nagkamali.
- **Inventory:** dagdag/edit/burahin ang produkto (puhunan, presyo, stock), `+Stock`, low-stock alert
- **Kasaysayan:** lahat ng naitalang benta, may Void (ibabalik ang stock)

Kita = (presyo − puhunan) × dami.

## Paano ilagay sa Android Studio project mo
1. Buksan ang `MonitoringVape` project.
2. Kopyahin ang lahat ng `.kt` file sa `app/src/main/java/com/example/monitoringvape/`
   papunta sa parehong folder sa project mo. **Palitan** ang existing na `MainActivity.kt`.
3. Hayaan ang existing na `ui/theme` folder (`MonitoringVapeTheme`) — ginagamit ito ng `MainActivity`.
4. *Sync Project with Gradle Files*, tapos pindutin ang ▶ Run.

Walang kailangang baguhin sa `build.gradle` o `AndroidManifest.xml`.
