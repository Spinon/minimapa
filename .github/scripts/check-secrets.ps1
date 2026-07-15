$ErrorActionPreference = 'Stop'

$patterns = '(sk_live_[A-Za-z0-9]+|rk_live_[A-Za-z0-9]+|ghp_[A-Za-z0-9]{20,}|AKIA[A-Z0-9]{16}|-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----)'
$arguments = @(
    'grep', '-nEI', $patterns, '--', '.',
    ':(exclude).github/scripts/check-secrets.ps1',
    ':(exclude)*.example',
    ':(exclude)*.md'
)

$matches = & git @arguments 2>$null
if ($LASTEXITCODE -gt 1) {
    throw "git grep failed with exit code $LASTEXITCODE"
}

if ($matches) {
    Write-Error "Possible production secret detected:`n$($matches -join "`n")"
}

Write-Output 'No known production-secret pattern found.'
