# Vape POS (MonitoringVape)

Simpleng POS + inventory monitoring para sa vape shop. Android (Kotlin + Jetpack Compose).
Offline, walang extra dependencies — naka-save ang data sa phone (SharedPreferences).

## Features
- **Benta (POS):** hanapin ang produkto, cart, Cash / GCash / Card, sukli, 18+ ID check bago mag-checkout
- **Inventory:** dagdag/edit/burahin ang produkto, puhunan at presyo, +Stock, low-stock alert
- **Kasaysayan:** lahat ng resibo, may Void (ibabalik ang stock)
- **Report:** benta, kita, top products, halaga ng stock (Ngayon / 7 araw / 30 araw / Lahat)

## Paano ilagay sa Android Studio project mo
1. Buksan ang `MonitoringVape` project.
2. Kopyahin ang lahat ng `.kt` file sa `app/src/main/java/com/example/monitoringvape/`
   papunta sa parehong folder sa project mo. **Palitan** ang existing na `MainActivity.kt`.
3. Hayaan ang existing na `ui/theme` folder (`MonitoringVapeTheme`) — ginagamit ito ng `MainActivity`.
4. *Sync Project with Gradle Files*, tapos pindutin ang ▶ Run.

Walang kailangang baguhin sa `build.gradle` o `AndroidManifest.xml`.
