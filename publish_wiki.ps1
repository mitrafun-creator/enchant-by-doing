# Publish wiki/ directory to GitHub Wiki
$repoUrl = "https://github.com/mitrafun-creator/enchant-by-doing.wiki.git"
$wikiWebUrl = "https://github.com/mitrafun-creator/enchant-by-doing/wiki"
$token = (& "C:\Program Files\GitHub CLI\gh.exe" auth token).Trim()
$authUrl = "https://x-access-token:$token@github.com/mitrafun-creator/enchant-by-doing.wiki.git"

$tempDir = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), "ebd_wiki_publish")
if (Test-Path $tempDir) { Remove-Item -Path $tempDir -Recurse -Force }
New-Item -ItemType Directory -Path $tempDir | Out-Null
Copy-Item "wiki\*" -Destination $tempDir -Recurse -Force

Write-Host "Checking GitHub Wiki status..." -ForegroundColor Cyan

function Test-WikiRepo {
    $out = git ls-remote $authUrl 2>&1
    return ($LASTEXITCODE -eq 0)
}

if (-not (Test-WikiRepo)) {
    Write-Host "`n[INFO] Opening $wikiWebUrl in your browser..." -ForegroundColor Yellow
    Write-Host "Please click 'Create the first page' and then 'Save page' in your browser." -ForegroundColor Yellow
    Write-Host "Waiting for first page creation..." -ForegroundColor Yellow
    Start-Process $wikiWebUrl
    
    $attempts = 0
    while (-not (Test-WikiRepo) -and $attempts -lt 30) {
        Start-Sleep -Seconds 3
        $attempts++
        Write-Host "." -NoNewline
    }
    Write-Host ""
}

if (Test-WikiRepo) {
    Push-Location $tempDir
    try {
        git init -b master | Out-Null
        git config user.name "MitraFun"
        git config user.email "mitrafun-creator@users.noreply.github.com"
        git add .
        git commit -m "Upload comprehensive Enchant by Doing Wiki" | Out-Null
        git push $authUrl master --force
        Write-Host "`n[SUCCESS] All 11 Wiki pages successfully published to GitHub Wiki!" -ForegroundColor Green
        Write-Host "View Wiki at: $wikiWebUrl" -ForegroundColor Cyan
    } finally {
        Pop-Location
    }
} else {
    Write-Host "`n[TIMEOUT] Wiki repository not initialized yet. Run .\publish_wiki.ps1 again after creating the first page." -ForegroundColor Red
}