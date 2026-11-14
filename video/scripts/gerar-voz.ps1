# Gera a narração com a voz do Windows (Microsoft Maria, pt-BR): um WAV por cena em public/voz e a duração
# de cada fala em src/narracao.json, que o vídeo usa para sincronizar cena e legenda.
# Uso (PowerShell, na pasta video): .\scripts\gerar-voz.ps1
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Speech
$raiz = Split-Path -Parent $PSScriptRoot
$falas = Get-Content -Raw -Encoding UTF8 (Join-Path $raiz 'narracao.json') | ConvertFrom-Json
$voz = New-Object System.Speech.Synthesis.SpeechSynthesizer
$voz.SelectVoice('Microsoft Maria Desktop')
$voz.Rate = 1
$formato = New-Object System.Speech.AudioFormat.SpeechAudioFormatInfo(22050, [System.Speech.AudioFormat.AudioBitsPerSample]::Sixteen, [System.Speech.AudioFormat.AudioChannel]::Mono)
$saida = @()
foreach ($fala in $falas) {
  $arquivo = Join-Path $raiz "public\voz\$($fala.cena).wav"
  $voz.SetOutputToWaveFile($arquivo, $formato)
  $voz.Speak($fala.texto)
  $voz.SetOutputToNull()
  # 22050 amostras por segundo, 2 bytes cada, cabeçalho WAV de 44 bytes.
  $segundos = [math]::Round(((Get-Item $arquivo).Length - 44) / (22050 * 2), 2)
  $saida += [pscustomobject]@{ cena = $fala.cena; texto = $fala.texto; segundos = $segundos }
}
$voz.Dispose()
$json = $saida | ConvertTo-Json
[System.IO.File]::WriteAllText((Join-Path $raiz 'src\narracao.json'), $json, (New-Object System.Text.UTF8Encoding($false)))
$saida | Format-Table -AutoSize
