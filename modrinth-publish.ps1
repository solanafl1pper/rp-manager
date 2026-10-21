param (
    [string]$Token = "mrp_DZxrWfGm1UuoftUbTnBXkiJbIo92wLuwl4gcg3qtm1aj1HPDxT3ojz7BUlMS",
    [string]$ProjectId = "uRCj67dz"
)

$baseDir = $PSScriptRoot
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

Write-Host "Modrinth Publisher for RP Manager v0.0.35"
Write-Host "Target Project: $ProjectId"
Write-Host "Checking Authentication..."

try {
    $userCheck = Invoke-RestMethod -Uri "https://api.modrinth.com/v2/user" -Headers @{
        "Authorization" = $Token
        "User-Agent" = "solana/rp-manager/0.0.35 (mikhailkazmerchuk@gmail.com)"
    } -Method Get
    Write-Host "Authenticated as Modrinth user: $($userCheck.username)" -ForegroundColor Green
} catch {
    Write-Host "Warning: Authentication check failed. Response: $($_.Exception.Message)" -ForegroundColor Red
}

$releases = @(
    @{
        name = "RP Manager v0.0.35 (1.21.x / 26.3)"
        version_number = "0.0.35-1.21.1"
        jar = "$baseDir\rp-manager-1.21.1\build\libs\rp-manager-fabric-1.21.1-0.0.35.jar"
        game_versions = @("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.11", "26.3")
        loaders = @("fabric", "quilt", "neoforge")
    },
    @{
        name = "RP Manager v0.0.35 (1.20.x)"
        version_number = "0.0.35-1.20.1"
        jar = "$baseDir\rp-manager-1.20.1\build\libs\rp-manager-fabric-1.20.1-0.0.35.jar"
        game_versions = @("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.6")
        loaders = @("fabric", "quilt", "neoforge", "forge")
    },
    @{
        name = "RP Manager v0.0.35 (1.19.x)"
        version_number = "0.0.35-1.19.2"
        jar = "$baseDir\rp-manager-1.19.2\build\libs\rp-manager-fabric-1.19.2-0.0.35.jar"
        game_versions = @("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4")
        loaders = @("fabric", "quilt", "forge")
    },
    @{
        name = "RP Manager v0.0.35 (1.18.x)"
        version_number = "0.0.35-1.18.2"
        jar = "$baseDir\rp-manager-1.18.2\build\libs\rp-manager-fabric-1.18.2-0.0.35.jar"
        game_versions = @("1.18", "1.18.1", "1.18.2")
        loaders = @("fabric", "quilt", "forge")
    },
    @{
        name = "RP Manager v0.0.35 (1.16.x)"
        version_number = "0.0.35-1.16.5"
        jar = "$baseDir\rp-manager-1.16.5\build\libs\rp-manager-fabric-1.16.5-0.0.35.jar"
        game_versions = @("1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5")
        loaders = @("fabric", "forge")
    }
)

foreach ($rel in $releases) {
    $jarPath = $rel.jar
    $fileName = [System.IO.Path]::GetFileName($jarPath)
    Write-Host "Uploading $fileName ($($rel.name))..."

    $metadata = @{
        name = $rel.name
        version_number = $rel.version_number
        changelog = "Resource Pack Manager v0.0.35: multi-version release supporting 26.3, 1.21.x, 1.20.x, 1.19.x, 1.18.x, 1.16.x"
        dependencies = @()
        game_versions = $rel.game_versions
        version_type = "release"
        loaders = $rel.loaders
        featured = $true
        status = "listed"
        requested_status = "listed"
        project_id = $ProjectId
        file_parts = @("file")
    } | ConvertTo-Json -Compress

    $metaFile = "$env:TEMP\mr_meta_$($rel.version_number).json"
    [System.IO.File]::WriteAllText($metaFile, $metadata, $utf8NoBom)

    $resp = & curl.exe -s -w "\nHTTP: %{http_code}\n" -X POST "https://api.modrinth.com/v2/version" `
        -H "Authorization: $Token" `
        -H "User-Agent: solana/rp-manager/0.0.35 (mikhailkazmerchuk@gmail.com)" `
        -F "data=<$metaFile;type=application/json" `
        -F "file=@$jarPath;type=application/java-archive"

    Write-Host "Response: $resp"
}
