<#
.SYNOPSIS
    Recon + captura ao vivo das propriedades de rádio (sys.radio.*) da central Haval/GWM.
    Apoia o projeto "FM-Radio" (app standalone — Opção B). Read-only: NÃO altera nada no carro.

.DESCRIPTION
    Captura por DOIS canais independentes e mostra qual funciona na sua central:

      1) GETPROP  — lê `getprop` e filtra chaves de rádio. Funciona SE as sys.radio.*
                    forem Android system properties (a confirmar na sua central).

      2) BROADCAST — captura os broadcasts que o app Impulse dispara a cada mudança:
                       android.intent.haval.<key>           (valor no extra)
                       android.intent.haval.<key>_<valor>   (valor embutido na action)
                     Só funciona se as chaves sys.radio.* estiverem em CAR_MONITOR_PROPERTIES
                     (campo "propriedades extras a monitorar" na UI do Impulse). O script avisa
                     se elas NÃO estiverem sendo monitoradas.

    Também faz um recon que alimenta a decisão da Opção B:
      - identifica o pacote do app de rádio stock
      - mostra o dono do foco de áudio (dumpsys audio) — responde "o rádio toca sem o app stock?"
      - lista serviços/atividades relacionados a rádio

.PARAMETER CarIp
    IP da central (ex: 172.20.10.2). Se informado, o script faz `adb connect <ip>:5555` antes.
    O Impulse habilita ADB TCP na 5555 no boot.

.PARAMETER Device
    Serial/host:port específico do adb (ex: 172.20.10.2:5555). Use se houver vários devices.

.PARAMETER IntervalMs
    Intervalo de polling no modo watch (default 800ms).

.PARAMETER DurationSec
    Duração do watch em segundos. 0 = roda até Ctrl+C (default).

.PARAMETER ProbeOnly
    Só faz o recon (probe) e sai, sem entrar no loop de captura.

.PARAMETER OutDir
    Pasta de saída (default: .\captures).

.EXAMPLE
    .\Watch-HavalRadio.ps1 -CarIp 172.20.10.2 -ProbeOnly
    .\Watch-HavalRadio.ps1 -CarIp 172.20.10.2          # recon + captura ao vivo (opere o rádio enquanto roda)
#>

[CmdletBinding()]
param(
    [string]$CarIp = "",
    [string]$Device = "",
    [int]$IntervalMs = 800,
    [int]$DurationSec = 0,
    [switch]$ProbeOnly,
    [string]$OutDir = "$PSScriptRoot\captures"
)

$ErrorActionPreference = "Stop"
$AppPkg = "br.com.redesurftank.havalshisuku"

# ------------------------------------------------------------------ chaves sys.radio.* (de CarConstants.java)
$RadioKeys = @(
    "sys.radio.cur_channel_info",
    "sys.radio.rds_cur_channel_info",
    "sys.radio.play_state",
    "sys.radio.play_control_action",
    "sys.radio.favorite_cur_station_action",
    "sys.radio.rds_favorite_cur_station_action",
    "sys.radio.search_state",
    "sys.radio.search_progress",
    "sys.radio.fm_valid_station_list",
    "sys.radio.fm_favorites_station_list",
    "sys.radio.am_valid_station_list",
    "sys.radio.am_favorites_station_list",
    "sys.radio.drm_valid_station_list",
    "sys.radio.drm_favorites_station_list",
    "sys.radio.rds_fm_valid_station_list",
    "sys.radio.rds_fm_favorite_station_list",
    "sys.radio.rds_am_valid_station_list",
    "sys.radio.rds_am_favorite_station_list",
    "sys.radio.rds_dab_valid_station_list",
    "sys.radio.rds_dab_favorite_station_list",
    "sys.radio.rds_dab_alternative_frequency_state",
    "sys.radio.rds_dab_reset_all_state",
    "sys.radio.rds_fm_alternative_frequency_state",
    "sys.radio.rds_regional_info",
    "sys.radio.rds_traffic_announcement_state",
    "sys.radio.rds_traffic_announcement_active_state",
    "sys.radio.rds_traffic_program_state"
)
# ordena por tamanho desc para casar o prefixo mais longo ao extrair valor da action
$RadioKeysByLen = $RadioKeys | Sort-Object { $_.Length } -Descending

# ------------------------------------------------------------------ helpers
function Resolve-AdbArgs {
    if ($Device) { return @("-s", $Device) }
    return @()
}

function Invoke-AdbShell([string]$Cmd) {
    $a = Resolve-AdbArgs
    # stderr é capturado para não poluir; retorna stdout como string única
    $out = & adb @a shell $Cmd 2>$null
    if ($null -eq $out) { return "" }
    return ($out -join "`n")
}

function Test-Adb {
    try { & adb version *> $null; return $true } catch { return $false }
}

function Connect-Car {
    if (-not $CarIp) { return }
    Write-Host "==> adb connect ${CarIp}:5555" -ForegroundColor Cyan
    & adb connect "${CarIp}:5555" 2>$null | Out-Null
    if (-not $Device) { $script:Device = "${CarIp}:5555" }
}

function Parse-Getprop([string]$raw) {
    $map = @{}
    foreach ($line in ($raw -split "`n")) {
        $m = [regex]::Match($line, '^\[(?<k>[^\]]+)\]:\s*\[(?<v>.*)\]$')
        if ($m.Success) { $map[$m.Groups['k'].Value] = $m.Groups['v'].Value }
    }
    return $map
}

# extrai pares chave->valor das actions android.intent.haval.sys.radio.*_<valor> no histórico de broadcasts
function Get-RadioFromBroadcasts {
    $raw = Invoke-AdbShell "dumpsys activity broadcasts"
    $found = @{}
    $rx = [regex]'android\.intent\.haval\.(sys\.radio\.[^\s,}\]]+)'
    foreach ($mm in $rx.Matches($raw)) {
        $token = $mm.Groups[1].Value          # ex: sys.radio.play_state_1  OU  sys.radio.play_state
        $matchedKey = $null; $val = $null
        foreach ($k in $RadioKeysByLen) {
            if ($token -eq $k) { $matchedKey = $k; $val = "(extra)"; break }
            if ($token.StartsWith($k + "_")) { $matchedKey = $k; $val = $token.Substring($k.Length + 1); break }
        }
        if ($matchedKey) { $found[$matchedKey] = $val }
    }
    return $found
}

function Now { (Get-Date).ToString("yyyy-MM-dd HH:mm:ss.fff") }

# ------------------------------------------------------------------ início
if (-not (Test-Adb)) { Write-Error "adb não encontrado no PATH. Instale o platform-tools."; exit 1 }
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
Connect-Car

$devices = (& adb devices 2>$null | Select-String -Pattern "device$")
if (-not $devices) { Write-Error "Nenhum device adb conectado. Verifique o IP / a 5555 / 'adb connect'."; exit 1 }

$stamp = (Get-Date).ToString("yyyyMMdd-HHmmss")
$logFile  = Join-Path $OutDir "radio-watch-$stamp.log"
$probeFile = Join-Path $OutDir "radio-probe-$stamp.txt"

# ============================================================ PROBE / RECON
Write-Host ""
Write-Host "================= RECON =================" -ForegroundColor Yellow
$probe = New-Object System.Text.StringBuilder
function P([string]$s) { [void]$probe.AppendLine($s); Write-Host $s }

P "# Haval Radio probe — $(Now)"
P ""

# 1) app de rádio stock
P "## Pacotes com 'radio' no nome"
P (Invoke-AdbShell "pm list packages | grep -i radio")
P ""
P "## Atividades/serviços de rádio em execução"
P (Invoke-AdbShell "dumpsys activity activities | grep -iE 'radio|tuner' | head -n 20")
P (Invoke-AdbShell "dumpsys activity services  | grep -iE 'radio|tuner' | head -n 20")
P ""

# 2) sys.radio.* são system properties?
P "## getprop filtrado (radio|tuner|fm|freq)"
$gp = Invoke-AdbShell "getprop | grep -iE 'radio|tuner|\bfm\b|freq'"
P $gp
$gpMap = Parse-Getprop $gp
$radioInGetprop = @($gpMap.Keys | Where-Object { $_ -like "sys.radio.*" })
P ""
if ($radioInGetprop.Count -gt 0) {
    P ">> CANAL GETPROP: ATIVO — $($radioInGetprop.Count) chave(s) sys.radio.* visíveis em getprop."
} else {
    P ">> CANAL GETPROP: vazio — sys.radio.* provavelmente NÃO são Android system properties nesta central."
}
P ""

# 3) Impulse está monitorando as chaves de rádio? (precisa de root p/ ler as prefs; best-effort)
P "## CAR_MONITOR_PROPERTIES (prefs do Impulse, best-effort via root)"
$prefs = Invoke-AdbShell "su -c 'cat /data/user_de/0/$AppPkg/shared_prefs/*.xml' 2>/dev/null | grep -iE 'CAR_MONITOR|sys.radio' | head -n 40"
if (-not $prefs) { $prefs = Invoke-AdbShell "cat /data/user_de/0/$AppPkg/shared_prefs/*.xml 2>/dev/null | grep -iE 'CAR_MONITOR|sys.radio' | head -n 40" }
if ($prefs) { P $prefs } else { P "(sem acesso de root às prefs — ignore; o teste de broadcast abaixo é mais confiável)" }
P ""

# 4) broadcasts de rádio já circulando?
P "## Broadcasts android.intent.haval.sys.radio.* no histórico"
$bc = Get-RadioFromBroadcasts
if ($bc.Count -gt 0) {
    P ">> CANAL BROADCAST: ATIVO — Impulse está transmitindo $($bc.Count) chave(s) de rádio."
    foreach ($k in ($bc.Keys | Sort-Object)) { P ("   {0} = {1}" -f $k, $bc[$k]) }
} else {
    P ">> CANAL BROADCAST: vazio."
    P "   Para ativar: no app Impulse, adicione as chaves sys.radio.* em 'propriedades extras a"
    P "   monitorar' (CAR_MONITOR_PROPERTIES) e troque de estação. Aí rode este script de novo."
}
P ""

# 5) foco de áudio — quem está tocando? (responde a pergunta-chave da Opção B)
P "## Foco de áudio atual (dumpsys audio — recorte relevante)"
P (Invoke-AdbShell "dumpsys audio | grep -iE 'focus|radio|player|piid|usage|AudioFocus' | head -n 40")
P ""

[System.IO.File]::WriteAllText($probeFile, $probe.ToString())
Write-Host "Recon salvo em: $probeFile" -ForegroundColor Green

if ($ProbeOnly) { exit 0 }

# ============================================================ WATCH (captura ao vivo)
Write-Host ""
Write-Host "================= WATCH =================" -ForegroundColor Yellow
Write-Host "Opere o rádio agora (trocar estação, seek, favoritar). Ctrl+C para parar." -ForegroundColor Cyan
Write-Host "Log: $logFile"
Write-Host ""

"# Haval Radio watch — início $(Now)" | Out-File -FilePath $logFile -Encoding utf8
$prev = @{}   # último valor visto por chave (de qualquer canal)
$tEnd = if ($DurationSec -gt 0) { (Get-Date).AddSeconds($DurationSec) } else { $null }

function Record([string]$src, [string]$key, [string]$val) {
    if ($prev.ContainsKey($key) -and $prev[$key] -eq $val) { return }
    $prev[$key] = $val
    $line = "{0}  [{1,-9}] {2} = {3}" -f (Now), $src, $key, $val
    Write-Host $line
    $line | Out-File -FilePath $logFile -Append -Encoding utf8
}

try {
    while ($true) {
        if ($tEnd -and (Get-Date) -gt $tEnd) { break }

        # canal getprop (se aplicável)
        if ($radioInGetprop.Count -gt 0) {
            $g = Parse-Getprop (Invoke-AdbShell "getprop | grep -i 'sys.radio'")
            foreach ($k in $g.Keys) { Record "getprop" $k $g[$k] }
        }

        # canal broadcast (se o Impulse estiver monitorando)
        $b = Get-RadioFromBroadcasts
        foreach ($k in $b.Keys) { Record "broadcast" $k $b[$k] }

        Start-Sleep -Milliseconds $IntervalMs
    }
}
finally {
    # snapshot final
    $snapFile = Join-Path $OutDir "radio-snapshot-$stamp.txt"
    "# Snapshot final — $(Now)" | Out-File $snapFile -Encoding utf8
    foreach ($k in ($prev.Keys | Sort-Object)) { "{0} = {1}" -f $k, $prev[$k] | Out-File $snapFile -Append -Encoding utf8 }
    Write-Host ""
    Write-Host "Snapshot final salvo em: $snapFile" -ForegroundColor Green
    Write-Host "Log completo: $logFile" -ForegroundColor Green
}
