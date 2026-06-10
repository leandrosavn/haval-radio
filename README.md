# Haval Radio

Interface de **rádio FM customizada** para a central multimídia de veículos Haval/GWM, como app Android **standalone**.

> ⚠️ Projeto educacional/não oficial, sem vínculo com Haval/GWM. Envolve engenharia reversa para estudo; uso por sua conta e risco.

## Visão geral

O app dirige o tuner nativo do veículo pelas propriedades `sys.radio.*` expostas pelo
`IntelligentVehicleControlService` (SDK Beantechs), acessadas via **Shizuku** — o mesmo modelo de
integração do projeto Impulse (`haval-app-tool-multimidia`) e do Haval Climate Control.

Stack: **Kotlin + Jetpack Compose + Material 3 + Shizuku**. minSdk/targetSdk 28 (Android 9), compileSdk 36.

## Estado atual (v0 — bootstrap)

Esqueleto inicial. A tela atual conecta no veículo e funciona como **monitor de recon**: lê e
exibe ao vivo todas as chaves `sys.radio.*`, para mapearmos os formatos reais (estação atual,
listas de estações, estados) antes de montar a UI definitiva.

A **UI final** (favoritos editáveis, dial-régua com sintonia por arrasto, volume, modos
claro/escuro/sistema, acento parametrizável) será portada do protótipo já desenhado — ver o
projeto **FM-Radio** na base de conhecimento (Obsidian).

## Arquitetura

```
ui/RadioScreen.kt        # Compose (v0: status + estação + monitor de recon)
ui/theme/                # tema Material 3 (acento verde-água, claro/escuro)
data/RadioKeys.kt        # chaves sys.radio.*
data/VehicleClient.kt    # bind do IntelligentVehicleControlService via Shizuku + reflexão
data/RadioRepository.kt  # leitura inicial + listener + estado observável (Compose)
aidl/com/beantechs/...   # interfaces do veículo (IIntelligentVehicleControlService, IListener)
```

Integração com o veículo (igual ao Impulse):
`android.os.ServiceManager.getService("com.beantechs.intelligentvehiclecontrol")` (via reflexão +
HiddenApiBypass) → `ShizukuBinderWrapper` → `IIntelligentVehicleControlService`. Leitura com
`fetchData(key)`, escrita com `request("cmd.common.request.set", key, value)`.

## Pré-requisitos no carro

- **Shizuku** ativo e permissão concedida ao app. (O app não faz o bootstrap do Shizuku.)

## Build

```bash
./gradlew :app:assembleDebug
# saída: app/build/outputs/apk/debug/app-debug.apk
```
Ou abrir no Android Studio.

## Roadmap

- [x] Bootstrap (projeto, Shizuku, AIDL, data layer, monitor de recon)
- [ ] Recon no carro: mapear formatos de `cur_channel_info`, `*_station_list`, ações de `play_control_action`
- [ ] Confirmar foco de áudio (tocar sem o app de rádio stock em foreground)
- [ ] Portar a UI do protótipo para Compose
- [ ] Persistência de preferências, CI por tag (padrão do Haval Climate Control)

## Licença

MIT — ver [LICENSE](LICENSE).
