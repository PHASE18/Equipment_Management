# Project Module API E2E (ASCII-only messages for Windows PowerShell 5 encoding safety)
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

Write-Host "=== Project Module API E2E ($BaseUrl) ===" -ForegroundColor Cyan

$login = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/login" -Body @{ username = 'admin'; password = '123456' }
Assert-True (($login.code -eq 200) -and [bool]$login.data.token) 'login get token'
$token = [string]$login.data.token
$auth = @{ Authorization = "Bearer $token" }

$noAuth = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=1"
Assert-True ($noAuth.code -eq 401) 'anonymous list returns 401'

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$code = "API-PRJ-$ts"

$create = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/project" -Headers $auth -Body @{
  projectName  = "API_PROJECT_$ts"
  projectCode  = $code
  departmentId = 1
  remark       = 'api-e2e'
}
Assert-True ($create.code -eq 200) 'create project'

$list = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=20&keyword=$code" -Headers $auth
Assert-True (($list.code -eq 200) -and ($list.data.total -ge 1)) 'list project by keyword'
$projectId = [long]$list.data.records[0].id

$detail = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/$projectId" -Headers $auth
Assert-True (($detail.code -eq 200) -and ($detail.data.projectCode -eq $code)) 'get project detail'

$dup = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/project" -Headers $auth -Body @{
  projectName = 'DUP'
  projectCode = $code
}
Assert-True ($dup.code -eq 409) 'duplicate project code returns 409'

$update = Invoke-ApiJson -Method Put -Url "$BaseUrl/api/project" -Headers $auth -Body @{
  id           = $projectId
  projectName  = "API_PROJECT_UPD_$ts"
  projectCode  = $code
  departmentId = 1
  remark       = 'updated'
}
Assert-True ($update.code -eq 200) 'update project'

$deviceList = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/list?pageNum=1&pageSize=1" -Headers $auth
$deviceId = $null
if ($deviceList.data.total -gt 0) {
  $deviceId = [long]$deviceList.data.records[0].id
} else {
  $devCode = "API-DEV-$ts"
  $null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/device" -Headers $auth -Body @{
    deviceNo       = $devCode
    deviceName     = "API_DEVICE_$ts"
    sn             = "SN-$ts"
    brandCode      = 'HUAWEI'
    deviceTypeCode = 'SERVER'
    departmentId   = 1
    statusCode     = 'IN_USE'
    managerUserId  = 1
  }
  $deviceList = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/list?pageNum=1&pageSize=5&keyword=$devCode" -Headers $auth
  $deviceId = [long]$deviceList.data.records[0].id
}
Assert-True ($null -ne $deviceId) "prepare device id=$deviceId"

$bind = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/project/bindDevice" -Headers $auth -Body @{
  projectId = $projectId
  deviceId  = $deviceId
}
Assert-True ($bind.code -eq 200) 'bind device to project'

$pageRel = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device-project/page?pageNum=1&pageSize=10&deviceId=$deviceId&projectId=$projectId" -Headers $auth
Assert-True (($pageRel.code -eq 200) -and ($pageRel.data.total -ge 1)) 'page device-project relations'

$byDevice = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device-project/by-device/$deviceId" -Headers $auth
$ids = @()
if ($byDevice.data) { $ids = @($byDevice.data | ForEach-Object { [long]$_ }) }
Assert-True (($byDevice.code -eq 200) -and ($ids -contains $projectId)) 'list project ids by device'

$sync = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/device-project/sync" -Headers $auth -Body @{
  deviceId   = $deviceId
  projectIds = @($projectId)
}
Assert-True ($sync.code -eq 200) 'sync device projects'

$unbind = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/project/unbind" -Headers $auth -Body @{
  projectId = $projectId
  deviceId  = $deviceId
}
Assert-True ($unbind.code -eq 200) 'unbind device'

$afterUnbind = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device-project/by-device/$deviceId" -Headers $auth
$ids2 = @()
if ($afterUnbind.data) { $ids2 = @($afterUnbind.data | ForEach-Object { [long]$_ }) }
Assert-True (-not ($ids2 -contains $projectId)) 'relation cleared after unbind'

$delete = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/project/$projectId" -Headers $auth
Assert-True ($delete.code -eq 200) 'delete project'

$afterDelete = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/$projectId" -Headers $auth
Assert-True ($afterDelete.code -eq 404) 'get deleted project returns 404'

Write-Host ''
Write-Host "=== Result: passed=$passed failed=$failed ===" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Red' })
if ($failed -gt 0) { exit 1 }
