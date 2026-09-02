# Publish wiki/ directory to GitHub Wiki
$repoUrl = "https://github.com/mitrafun-creator/enchant-by-doing.wiki.git"
$tempDir = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), "ebd_wiki_publish")

if (Test-Path $tempDir) {
    Remove-Item -Path $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

Copy-Item "wiki\*" -Destination $tempDir -Recurse -Force

Push-Location $tempDir
try {
    git init -b master
    git config user.name "MitraFun"
    git config user.email "mitrafun-creator@users.noreply.github.com"
    git add .
    git commit -m "Update Wiki documentation"
    git remote add origin $repoUrl
    git push origin master --force
    Write-Host "`n[SUCCESS] Wiki successfully published to $repoUrl!" -ForegroundColor Green
} catch {
    Write-Host "`n[ERROR] Failed to push wiki: $_" -ForegroundColor Red
} finally {
    Pop-Location
}