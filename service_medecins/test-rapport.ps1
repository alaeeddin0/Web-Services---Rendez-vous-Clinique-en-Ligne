# Execute les vrais tests JUnit et affiche le rapport Surefire en couleurs.
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$reportPath = Join-Path $PSScriptRoot 'target/surefire-reports/TEST-com.clinique.medecins.MedecinApiTests.xml'
$sourcePath = Join-Path $PSScriptRoot 'src/test/java/com/clinique/medecins/MedecinApiTests.java'
$logDirectory = Join-Path $PSScriptRoot 'target'
New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null
$logPath = Join-Path $logDirectory 'rapport-medecins-execution.log'
$wrapper = Join-Path $PSScriptRoot 'mvnw.cmd'

# Les libelles sont lus dans le code, sans dupliquer la correspondance MED 01-07.
$source = Get-Content -LiteralPath $sourcePath -Encoding UTF8 -Raw
$pattern = '(?s)@Tag\("rapport-medecins"\).*?@DisplayName\("(?<label>[^"\r\n]+)"\)\s+void\s+(?<method>\w+)'
$labels = @{}
foreach ($match in [regex]::Matches($source, $pattern)) {
    $labels[$match.Groups['method'].Value] = $match.Groups['label'].Value
}
if ($labels.Count -ne 7) {
    throw 'Le groupe rapport-medecins doit contenir exactement 7 methodes identifiees.'
}

Write-Host ''
Write-Host 'Execution des 7 tests JUnit du service medecins...' -ForegroundColor Cyan
Write-Host 'Demarrage de Spring Boot et de la base de test temporaire. Patientez.' -ForegroundColor DarkGray
$started = [DateTime]::UtcNow
# Windows PowerShell 5 transforme parfois stderr en NativeCommandError.
# Lire les deux flux du processus directement permet de conserver les
# avertissements Java sans les confondre avec un echec Maven/JUnit.
$startInfo = New-Object System.Diagnostics.ProcessStartInfo
$startInfo.FileName = $env:ComSpec
$startInfo.Arguments = '/d /s /c ""{0}" -B -ntp test "-Dgroups=rapport-medecins""' -f $wrapper
$startInfo.WorkingDirectory = $PSScriptRoot
$startInfo.UseShellExecute = $false
$startInfo.CreateNoWindow = $true
$startInfo.RedirectStandardOutput = $true
$startInfo.RedirectStandardError = $true
$process = New-Object System.Diagnostics.Process
$process.StartInfo = $startInfo
try {
    [void]$process.Start()
    # Lire les deux flux en parallele evite un blocage si un tampon se remplit.
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $process.WaitForExit()
    $mavenExit = $process.ExitCode
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    $logText = $stdout + [Environment]::NewLine + '--- Messages Java/Maven (stderr) ---' + [Environment]::NewLine + $stderr
    Set-Content -LiteralPath $logPath -Value $logText -Encoding UTF8
} finally {
    $process.Dispose()
}

# Ne jamais presenter un ancien resultat comme celui de cette execution.
if (!(Test-Path -LiteralPath $reportPath) -or (Get-Item -LiteralPath $reportPath).LastWriteTimeUtc -lt $started) {
    Write-Host 'ECHEC : aucun nouveau rapport JUnit produit.' -ForegroundColor Red
    Get-Content -LiteralPath $logPath -Tail 35
    Write-Host "Journal complet : $logPath" -ForegroundColor DarkGray
    exit 1
}

[xml]$report = Get-Content -LiteralPath $reportPath -Encoding UTF8 -Raw
$suite = $report.testsuite
$cases = @($suite.testcase)
$invariant = [System.Globalization.CultureInfo]::InvariantCulture
$passed = 0
Write-Host ''
Write-Host '  JUnit - MedecinApiTests' -ForegroundColor Cyan
Write-Host ''
foreach ($case in $cases) {
    $method = [string]$case.GetAttribute('name')
    $label = $method
    if ($labels.ContainsKey($method)) { $label = $labels[$method] }
    $seconds = [double]::Parse($case.GetAttribute('time'), $invariant)
    $duration = $seconds.ToString('0.00', $invariant)
    $failure = $case.SelectSingleNode('failure | error')
    if ($null -ne $failure) {
        Write-Host ("  [ECHEC] {0} ({1} s)" -f $label, $duration) -ForegroundColor Red
        Write-Host ("          {0}" -f $failure.GetAttribute('message')) -ForegroundColor Red
    } elseif ($null -ne $case.SelectSingleNode('skipped')) {
        Write-Host ("  [IGNORE] {0} ({1} s)" -f $label, $duration) -ForegroundColor Yellow
    } else {
        $passed++
        Write-Host ("  [OK] {0} ({1} s)" -f $label, $duration) -ForegroundColor Green
    }
}
$failures = [int]$suite.GetAttribute('failures')
$errors = [int]$suite.GetAttribute('errors')
$skipped = [int]$suite.GetAttribute('skipped')
$correctSelection = $cases.Count -eq 7 -and @($cases | Where-Object { !$labels.ContainsKey($_.GetAttribute('name')) }).Count -eq 0
$success = $mavenExit -eq 0 -and $correctSelection -and $passed -eq 7 -and $failures -eq 0 -and $errors -eq 0 -and $skipped -eq 0
$color = if ($success) { 'Green' } else { 'Red' }
Write-Host ''
Write-Host ("  Tests : {0} reussis, {1} echecs, {2} erreurs, {3} ignores" -f $passed, $failures, $errors, $skipped) -ForegroundColor $color
Write-Host ("  Duree de la suite : {0} s" -f $suite.GetAttribute('time')) -ForegroundColor $color
if (!$correctSelection) { Write-Host '  La selection ne correspond pas exactement aux 7 tests du rapport.' -ForegroundColor Red }
Write-Host "  Rapport : $reportPath" -ForegroundColor DarkGray
Write-Host "  Journal : $logPath" -ForegroundColor DarkGray
if (!$success) {
    Write-Host ''
    Get-Content -LiteralPath $logPath -Tail 35
    exit 1
}
Write-Host '  RESULTAT : 7 / 7 REUSSIS' -ForegroundColor Green
exit 0
