# FM-Radio probe

Recon + captura ao vivo das propriedades `sys.radio.*` da central Haval/GWM, para o projeto
**FM-Radio** (app standalone — Opção B). Doc do projeto no Obsidian: `Projetos/Haval/FM-Radio/`.

## Uso

```powershell
# pré-requisito: adb no PATH. O Impulse habilita ADB TCP na 5555 no boot do carro.
cd C:\haval_impulse\fm-radio-probe

# só recon (pacote do rádio stock, dono do foco de áudio, canais disponíveis):
.\Watch-HavalRadio.ps1 -CarIp 172.20.10.2 -ProbeOnly

# recon + captura ao vivo — opere o rádio (trocar estação, seek, favoritar):
.\Watch-HavalRadio.ps1 -CarIp 172.20.10.2
```

Saídas em `captures/`:
- `radio-probe-*.txt` — recon (qual canal funciona, foco de áudio, app stock)
- `radio-watch-*.log` — mudanças capturadas ao vivo, com timestamp
- `radio-snapshot-*.txt` — estado final de cada chave observada

## Dois canais de captura

1. **getprop** — funciona se `sys.radio.*` forem Android system properties.
2. **broadcast** — captura `android.intent.haval.sys.radio.*` que o Impulse dispara.
   ⚠️ Só acende se as chaves estiverem em **CAR_MONITOR_PROPERTIES** (UI do Impulse →
   "propriedades extras a monitorar"). Elas **não** estão no `DEFAULT_KEYS` do app.

O script é **read-only**: não escreve nada no carro.
