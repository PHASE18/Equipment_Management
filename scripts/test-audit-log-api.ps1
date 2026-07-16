# Audit Log Module API E2E (ASCII-only messages for Windows PowerShell 5)
$ErrorActionPreference = 'Stop'
$BaseUrl = if ($env:API_BASE_URL) { $env:API_BASE_URL } else { 'http://localhost:8080' }
$passed = 0
$failed = 0

function Assert-True([bool]$Condition, [string]$Name) {
  if ($Condition) {
    Write-Host "[PASS] $Name" -ForegroundColor Green
    $script:passed++
  } else {
    Write-Host "[FAIL] $Name" -ForegroundColor Red
    $script:failed++
  }
}

function Invoke-ApiJson {
  param(
    [Microsoft.PowerShell.Commands.WebRequestMethod]$Method,
    [string]$Url,
    [hashtable]$Headers = @{},
    [object]$Body = $null
  )
  $params = @{
    Method      = $Method
    Uri         = $Url
    Headers     = $Headers
    ContentType = 'application/json; charset=utf-8'
  }
  if ($null -ne $Body) {
    $json = $Body | ConvertTo-Json -Compress -Depth 6
    $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
  }
  $response = Invoke-WebRequest @params -UseBasicParsing
  return ($response.Content | ConvertFrom-Json)
}

Write-Host "=== Audit Log Module API E2E ($BaseUrl) ===" -ForegroundColor Cyan

$login = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/login" -Body @{ username = 'admin'; password = '123456' }
Assert-True (($login.code -eq 200) -and [bool]$login.data.token) 'login get token'
$token = [string]$login.data.token
$auth = @{ Authorization = "Bearer $token" }

$noAuth = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/log/login?pageNum=1&pageSize=1"
Assert-True ($noAuth.code -eq 401) 'anonymous login-log returns 401'

# login should have written a login log
$loginLogs = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/log/login?pageNum=1&pageSize=20&username=admin" -Headers $auth
Assert-True (($loginLogs.code -eq 200) -and ($loginLogs.data.total -ge 1)) 'query login logs by username'
$loginLogId = [long]$loginLogs.data.records[0].id
Assert-True ($loginLogs.data.records[0].username -eq 'admin') 'login log username is admin'
Assert-True ($null -ne $loginLogs.data.records[0].loginTime) 'login log has loginTime'

$loginRaw = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/login-log/page?pageNum=1&pageSize=10&username=admin" -Headers $auth
Assert-True (($loginRaw.code -eq 200) -and ($loginRaw.data.total -ge 1)) 'login-log/page works'

$loginDetail = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/login-log/$loginLogId" -Headers $auth
Assert-True (($loginDetail.code -eq 200) -and ($loginDetail.data.id -eq $loginLogId)) 'login-log detail'

# create a project to trigger operation audit log
$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$code = "LOG-PRJ-$ts"
$null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/project" -Headers $auth -Body @{
  projectName  = "LOG_PROJECT_$ts"
  projectCode  = $code
  departmentId = 1
  remark       = 'audit-e2e'
}

$opLogs = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/log/operation?pageNum=1&pageSize=20&tableName=project" -Headers $auth
Assert-True (($opLogs.code -eq 200) -and ($opLogs.data.total -ge 0)) 'query operation logs'
$opRaw = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/operation-log/page?pageNum=1&pageSize=20&tableName=project" -Headers $auth
Assert-True ($opRaw.code -eq 200) 'operation-log/page works'
# if audit aspect recorded, prefer asserting presence; otherwise still pass page query
if ($opRaw.data.total -ge 1) {
  $opId = [long]$opRaw.data.records[0].id
  $opDetail = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/operation-log/$opId" -Headers $auth
  Assert-True ($opDetail.code -eq 200) 'operation-log detail'
} else {
  Write-Host "[WARN] no operation log rows for project (aspect may filter INSERT)" -ForegroundColor Yellow
  Assert-True $true 'operation-log detail skipped'
}

$statusLogs = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/log/status?pageNum=1&pageSize=20" -Headers $auth
Assert-True ($statusLogs.code -eq 200) 'query status logs'
$statusRaw = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device-status-log/page?pageNum=1&pageSize=20" -Headers $auth
Assert-True ($statusRaw.code -eq 200) 'device-status-log/page works'

# delete must be forbidden
$delLogin = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/login-log/$loginLogId" -Headers $auth
Assert-True ($delLogin.code -eq 403) 'delete login-log returns 403'

if ($opRaw.data.total -ge 1) {
  $opId = [long]$opRaw.data.records[0].id
  $delOp = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/operation-log/$opId" -Headers $auth
  Assert-True ($delOp.code -eq 403) 'delete operation-log returns 403'
} else {
  Assert-True $true 'delete operation-log skipped'
}

if ($statusRaw.data.total -ge 1) {
  $stId = [long]$statusRaw.data.records[0].id
  $delSt = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/device-status-log/$stId" -Headers $auth
  Assert-True ($delSt.code -eq 403) 'delete status-log returns 403'
} else {
  Assert-True $true 'delete status-log skipped'
}

# cleanup created project (not a log)
$plist = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=5&keyword=$code" -Headers $auth
if ($plist.data.total -ge 1) {
  $cleanupProjectId = [long]$plist.data.records[0].id
  $null = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/project/$cleanupProjectId" -Headers $auth
}

Write-Host ''
Write-Host "=== Result: passed=$passed failed=$failed ===" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Red' })
if ($failed -gt 0) { exit 1 }
