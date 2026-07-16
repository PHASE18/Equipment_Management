# Statistics Module API E2E (ASCII-only messages)
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

Write-Host "=== Statistics Module API E2E ($BaseUrl) ===" -ForegroundColor Cyan

$login = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/login" -Body @{ username = 'admin'; password = '123456' }
Assert-True (($login.code -eq 200) -and [bool]$login.data.token) 'login get token'
$token = [string]$login.data.token
$auth = @{ Authorization = "Bearer $token" }

$noAuth = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/dashboard"
Assert-True ($noAuth.code -eq 401) 'anonymous dashboard returns 401'

$dashboard = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/dashboard" -Headers $auth
Assert-True ($dashboard.code -eq 200) 'dashboard success'
Assert-True ($null -ne $dashboard.data.summary) 'dashboard has summary'
Assert-True ($null -ne $dashboard.data.summary.deviceTotal) 'summary.deviceTotal exists'
Assert-True ($null -ne $dashboard.data.statusChart) 'dashboard has statusChart'
Assert-True ($null -ne $dashboard.data.brandChart) 'dashboard has brandChart'
Assert-True ($null -ne $dashboard.data.typeChart) 'dashboard has typeChart'
Assert-True ($null -ne $dashboard.data.departmentChart) 'dashboard has departmentChart'
Assert-True ($null -ne $dashboard.data.projectChart) 'dashboard has projectChart'
Assert-True ($null -ne $dashboard.data.faultChart) 'dashboard has faultChart'
Assert-True ($null -ne $dashboard.data.maintenanceTrendChart) 'dashboard has maintenanceTrendChart'
Assert-True ($null -ne $dashboard.data.maintenanceCostChart) 'dashboard has maintenanceCostChart'
Assert-True ($null -ne $dashboard.data.warrantyChart) 'dashboard has warrantyChart'
Assert-True ($null -ne $dashboard.data.supplierChart) 'dashboard has supplierChart'
Assert-True ($null -ne $dashboard.data.maintenanceCompanyChart) 'dashboard has maintenanceCompanyChart'
Assert-True ($null -ne $dashboard.data.modelChart) 'dashboard has modelChart'
Assert-True ($null -ne $dashboard.data.scrapChart) 'dashboard has scrapChart'

$homeStats = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/home" -Headers $auth
Assert-True (($homeStats.code -eq 200) -and ($null -ne $homeStats.data.deviceTotal)) 'home statistics success'

$endpoints = @(
  'brand', 'type', 'status', 'department', 'project', 'fault', 'faultRank',
  'maintenance-trend', 'cost', 'warranty', 'supplier', 'maintenance-company', 'model', 'scrap'
)
foreach ($ep in $endpoints) {
  $resp = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/$ep" -Headers $auth
  Assert-True ($resp.code -eq 200) "GET /statistics/$ep success"
}

# filter by brand/type should still return 200
$filtered = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/dashboard?brandCode=HUAWEI&deviceTypeCode=SERVER" -Headers $auth
Assert-True ($filtered.code -eq 200) 'dashboard with filters success'

$dateFiltered = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/brand?startDate=2020-01-01&endDate=2099-12-31" -Headers $auth
Assert-True ($dateFiltered.code -eq 200) 'brand with date range success'

Write-Host ''
Write-Host "=== Result: passed=$passed failed=$failed ===" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Red' })
if ($failed -gt 0) { exit 1 }
