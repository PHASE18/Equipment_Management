# RBAC + data-scope E2E (ASCII-only messages for Windows PowerShell 5)
# Accounts (password 123456): admin / dept_admin / user01
$ErrorActionPreference = 'Stop'
$BaseUrl = if ($env:API_BASE_URL) { $env:API_BASE_URL } else { 'http://localhost:8080' }
$passed = 0
$failed = 0
$tempDir = Join-Path $env:TEMP 'eq-rbac-e2e'
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

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
    $json = $Body | ConvertTo-Json -Compress -Depth 8
    $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
  }
  $response = Invoke-WebRequest @params -UseBasicParsing
  return ($response.Content | ConvertFrom-Json)
}

function Invoke-MultipartUpload {
  param(
    [string]$Url,
    [hashtable]$Headers,
    [string]$FilePath,
    [hashtable]$Fields
  )
  $boundary = [guid]::NewGuid().ToString('N')
  $fileBytes = [System.IO.File]::ReadAllBytes($FilePath)
  $fileName = [System.IO.Path]::GetFileName($FilePath)
  $encoding = [System.Text.Encoding]::UTF8
  $bodyStream = New-Object System.IO.MemoryStream

  foreach ($key in $Fields.Keys) {
    $part = "--$boundary`r`nContent-Disposition: form-data; name=`"$key`"`r`n`r`n$($Fields[$key])`r`n"
    $bytes = $encoding.GetBytes($part)
    $bodyStream.Write($bytes, 0, $bytes.Length)
  }

  $header = "--$boundary`r`nContent-Disposition: form-data; name=`"file`"; filename=`"$fileName`"`r`nContent-Type: application/octet-stream`r`n`r`n"
  $headerBytes = $encoding.GetBytes($header)
  $bodyStream.Write($headerBytes, 0, $headerBytes.Length)
  $bodyStream.Write($fileBytes, 0, $fileBytes.Length)
  $footer = $encoding.GetBytes("`r`n--$boundary--`r`n")
  $bodyStream.Write($footer, 0, $footer.Length)

  $reqHeaders = @{}
  foreach ($k in $Headers.Keys) { $reqHeaders[$k] = $Headers[$k] }
  $response = Invoke-WebRequest -Method Post -Uri $Url -Headers $reqHeaders `
    -ContentType "multipart/form-data; boundary=$boundary" -Body $bodyStream.ToArray() -UseBasicParsing
  return ($response.Content | ConvertFrom-Json)
}

function Login-User([string]$Username) {
  $login = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/login" -Body @{
    username = $Username
    password = '123456'
  }
  if ($login.code -ne 200 -or -not $login.data.token) {
    throw "Login failed for $Username : code=$($login.code) msg=$($login.message)"
  }
  return @{
    Username = $Username
    Token    = [string]$login.data.token
    Auth     = @{ Authorization = "Bearer $([string]$login.data.token)" }
    User     = $login.data.user
  }
}

function Get-DeviceIdsByKeyword([hashtable]$Auth, [string]$Keyword) {
  $list = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/list?pageNum=1&pageSize=50&keyword=$Keyword" -Headers $Auth
  $ids = @()
  if ($list.data -and $list.data.records) {
    $ids = @($list.data.records | ForEach-Object { [long]$_.id })
  }
  return @{ Response = $list; Ids = $ids; Total = [long]$list.data.total }
}

function Test-ContainsId([object[]]$Ids, [long]$Id) {
  return ($Ids -contains $Id)
}

Write-Host "=== RBAC Data-Scope API E2E ($BaseUrl) ===" -ForegroundColor Cyan

# ---------- Login all roles ----------
$admin = Login-User 'admin'
$dept  = Login-User 'dept_admin'
$user  = Login-User 'user01'

Assert-True ($admin.User.dataScope -eq 'ALL') 'admin dataScope=ALL'
Assert-True ($dept.User.dataScope -eq 'DEPARTMENT') 'dept_admin dataScope=DEPARTMENT'
Assert-True ($user.User.dataScope -eq 'SELF') 'user01 dataScope=SELF'
Assert-True ([long]$dept.User.departmentId -eq 2) 'dept_admin departmentId=2'
Assert-True ([long]$user.User.departmentId -eq 2) 'user01 departmentId=2'
Assert-True ([long]$user.User.id -eq 3) 'user01 id=3'

$adminMenus = @()
if ($admin.User.menus) { $adminMenus = @($admin.User.menus | ForEach-Object { [string]$_.permissionCode }) }
Assert-True ($adminMenus -contains 'dashboard:view') 'admin has dashboard menu'
Assert-True (-not ($adminMenus -contains 'statistics:view')) 'statistics menu removed'
$deptMenus = @()
if ($dept.User.menus) { $deptMenus = @($dept.User.menus | ForEach-Object { [string]$_.permissionCode }) }
Assert-True (-not ($deptMenus -contains 'system:manage')) 'dept_admin has no system:manage menu'

# ---------- Seed cross-department fixtures (as admin) ----------
$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$noA  = "RBAC-A-$ts"
$noB  = "RBAC-B-$ts"
$noB2 = "RBAC-B2-$ts"
$codeA = "RBAC-PRJ-A-$ts"
$codeB = "RBAC-PRJ-B-$ts"

Write-Host "--- Seed fixtures ts=$ts ---" -ForegroundColor Yellow

$null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/device" -Headers $admin.Auth -Body @{
  deviceNo       = $noA
  deviceName     = "RBAC_DEPT1_$ts"
  sn             = "SN-A-$ts"
  brandCode      = 'HUAWEI'
  deviceTypeCode = 'SERVER'
  departmentId   = 1
  statusCode     = 'IN_USE'
  managerUserId  = 1
  remark         = 'rbac-seed-dept1'
}
$null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/device" -Headers $admin.Auth -Body @{
  deviceNo       = $noB
  deviceName     = "RBAC_OWNER_USER01_$ts"
  sn             = "SN-B-$ts"
  brandCode      = 'HUAWEI'
  deviceTypeCode = 'SERVER'
  departmentId   = 2
  statusCode     = 'IN_USE'
  managerUserId  = 3
  remark         = 'rbac-seed-dept2-user01'
}
$null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/device" -Headers $admin.Auth -Body @{
  deviceNo       = $noB2
  deviceName     = "RBAC_OWNER_DEPTADMIN_$ts"
  sn             = "SN-B2-$ts"
  brandCode      = 'HUAWEI'
  deviceTypeCode = 'SERVER'
  departmentId   = 2
  statusCode     = 'IN_USE'
  managerUserId  = 2
  remark         = 'rbac-seed-dept2-deptadmin'
}

$devA  = (Get-DeviceIdsByKeyword $admin.Auth $noA).Response.data.records[0]
$devB  = (Get-DeviceIdsByKeyword $admin.Auth $noB).Response.data.records[0]
$devB2 = (Get-DeviceIdsByKeyword $admin.Auth $noB2).Response.data.records[0]
$idA  = [long]$devA.id
$idB  = [long]$devB.id
$idB2 = [long]$devB2.id
Assert-True (($idA -gt 0) -and ($idB -gt 0) -and ($idB2 -gt 0)) "seed devices idA=$idA idB=$idB idB2=$idB2"

$null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/project" -Headers $admin.Auth -Body @{
  projectName  = "RBAC_PRJ_DEPT1_$ts"
  projectCode  = $codeA
  departmentId = 1
  remark       = 'rbac-seed'
}
$null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/project" -Headers $admin.Auth -Body @{
  projectName  = "RBAC_PRJ_DEPT2_$ts"
  projectCode  = $codeB
  departmentId = 2
  remark       = 'rbac-seed'
}

$prjA = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=5&keyword=$codeA" -Headers $admin.Auth
$prjB = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=5&keyword=$codeB" -Headers $admin.Auth
$prjIdA = [long]$prjA.data.records[0].id
$prjIdB = [long]$prjB.data.records[0].id
Assert-True (($prjIdA -gt 0) -and ($prjIdB -gt 0)) "seed projects prjA=$prjIdA prjB=$prjIdB"

$today = (Get-Date).ToString('yyyy-MM-dd')
foreach ($pair in @(
  @{ DeviceId = $idA;  Tag = 'A' },
  @{ DeviceId = $idB;  Tag = 'B' },
  @{ DeviceId = $idB2; Tag = 'B2' }
)) {
  $null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/maintenance" -Headers $admin.Auth -Body @{
    deviceId           = $pair.DeviceId
    maintenanceDate    = $today
    maintenancePerson  = "rbac-$($pair.Tag)"
    faultTypeCode      = 'HARDWARE'
    faultDescription   = "rbac-maint-$($pair.Tag)-$ts"
    isResolved         = 1
    maintenanceCost    = 100
  }
}

$tmpFile = Join-Path $tempDir "rbac-$ts.txt"
Set-Content -Path $tmpFile -Value "rbac-attachment-$ts" -Encoding UTF8
foreach ($did in @($idA, $idB, $idB2)) {
  $up = Invoke-MultipartUpload -Url "$BaseUrl/api/file/upload" -Headers $admin.Auth -FilePath $tmpFile -Fields @{
    deviceId     = "$did"
    category     = 'document'
    fileTypeCode = 'OTHER'
  }
  Assert-True ($up.code -eq 200) "upload attachment for deviceId=$did"
}

# ---------- Functional permission ----------
Write-Host "--- Functional permissions ---" -ForegroundColor Yellow

$userListDept = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/user/list?pageNum=1&pageSize=1" -Headers $dept.Auth
Assert-True ($userListDept.code -eq 1003) 'dept_admin denied system user list (1003)'

$userListUser = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/user/list?pageNum=1&pageSize=1" -Headers $user.Auth
Assert-True ($userListUser.code -eq 1003) 'user01 denied system user list (1003)'

$userListAdmin = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/user/list?pageNum=1&pageSize=5" -Headers $admin.Auth
Assert-True ($userListAdmin.code -eq 200) 'admin can list users'

$prjUser = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=1" -Headers $user.Auth
Assert-True ($prjUser.code -eq 1003) 'user01 denied project list (no project:list)'

# ---------- Device data scope ----------
Write-Host "--- Device data scope ---" -ForegroundColor Yellow

$adminDev = Get-DeviceIdsByKeyword $admin.Auth "RBAC-"
Assert-True (Test-ContainsId $adminDev.Ids $idA)  'admin sees device A (dept1)'
Assert-True (Test-ContainsId $adminDev.Ids $idB)  'admin sees device B (dept2/user01)'
Assert-True (Test-ContainsId $adminDev.Ids $idB2) 'admin sees device B2 (dept2/dept_admin)'

$deptDev = Get-DeviceIdsByKeyword $dept.Auth "RBAC-"
Assert-True (-not (Test-ContainsId $deptDev.Ids $idA)) 'dept_admin cannot see device A (other dept)'
Assert-True (Test-ContainsId $deptDev.Ids $idB)        'dept_admin sees device B (same dept)'
Assert-True (Test-ContainsId $deptDev.Ids $idB2)       'dept_admin sees device B2 (same dept)'

$userDev = Get-DeviceIdsByKeyword $user.Auth "RBAC-"
Assert-True (-not (Test-ContainsId $userDev.Ids $idA))  'user01 cannot see device A'
Assert-True (Test-ContainsId $userDev.Ids $idB)         'user01 sees own device B'
Assert-True (-not (Test-ContainsId $userDev.Ids $idB2)) 'user01 cannot see B2 (other owner)'

$crossDept = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/$idA" -Headers $dept.Auth
Assert-True ($crossDept.code -eq 404) 'dept_admin get other-dept device returns 404'

$crossSelf = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/$idB2" -Headers $user.Auth
Assert-True ($crossSelf.code -eq 404) 'user01 get non-owned device returns 404'

$okSelf = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/$idB" -Headers $user.Auth
Assert-True (($okSelf.code -eq 200) -and ([long]$okSelf.data.id -eq $idB)) 'user01 get own device ok'

# ---------- Project data scope ----------
Write-Host "--- Project data scope ---" -ForegroundColor Yellow

$deptPrj = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=50&keyword=RBAC-PRJ-" -Headers $dept.Auth
$deptPrjIds = @()
if ($deptPrj.data.records) { $deptPrjIds = @($deptPrj.data.records | ForEach-Object { [long]$_.id }) }
Assert-True ($deptPrj.code -eq 200) 'dept_admin can list projects'
Assert-True (-not (Test-ContainsId $deptPrjIds $prjIdA)) 'dept_admin cannot see dept1 project'
Assert-True (Test-ContainsId $deptPrjIds $prjIdB)        'dept_admin sees dept2 project'

$adminPrj = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/project/list?pageNum=1&pageSize=50&keyword=RBAC-PRJ-" -Headers $admin.Auth
$adminPrjIds = @()
if ($adminPrj.data.records) { $adminPrjIds = @($adminPrj.data.records | ForEach-Object { [long]$_.id }) }
Assert-True ((Test-ContainsId $adminPrjIds $prjIdA) -and (Test-ContainsId $adminPrjIds $prjIdB)) 'admin sees both projects'

# ---------- Maintenance data scope ----------
Write-Host "--- Maintenance data scope ---" -ForegroundColor Yellow

function Get-MaintenanceDeviceIds([hashtable]$Auth) {
  $resp = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/maintenance/list?pageNum=1&pageSize=200" -Headers $Auth
  $ids = @()
  if ($resp.data -and $resp.data.records) {
    $ids = @($resp.data.records | ForEach-Object { [long]$_.deviceId })
  }
  return @{ Response = $resp; DeviceIds = $ids }
}

$adminMaint = Get-MaintenanceDeviceIds $admin.Auth
Assert-True (Test-ContainsId $adminMaint.DeviceIds $idA)  'admin maintenance includes device A'
Assert-True (Test-ContainsId $adminMaint.DeviceIds $idB)  'admin maintenance includes device B'
Assert-True (Test-ContainsId $adminMaint.DeviceIds $idB2) 'admin maintenance includes device B2'

$deptMaint = Get-MaintenanceDeviceIds $dept.Auth
Assert-True (-not (Test-ContainsId $deptMaint.DeviceIds $idA)) 'dept_admin maintenance excludes device A'
Assert-True (Test-ContainsId $deptMaint.DeviceIds $idB)        'dept_admin maintenance includes device B'
Assert-True (Test-ContainsId $deptMaint.DeviceIds $idB2)       'dept_admin maintenance includes device B2'

$userMaint = Get-MaintenanceDeviceIds $user.Auth
Assert-True (-not (Test-ContainsId $userMaint.DeviceIds $idA))  'user01 maintenance excludes device A'
Assert-True (Test-ContainsId $userMaint.DeviceIds $idB)         'user01 maintenance includes own device B'
Assert-True (-not (Test-ContainsId $userMaint.DeviceIds $idB2)) 'user01 maintenance excludes device B2'

# ---------- Attachment data scope ----------
Write-Host "--- Attachment data scope ---" -ForegroundColor Yellow

$attAdminA = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$idA" -Headers $admin.Auth
Assert-True (($attAdminA.code -eq 200) -and (@($attAdminA.data).Count -ge 1)) 'admin lists attachments of A'

$attDeptA = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$idA" -Headers $dept.Auth
$attDeptACount = 0
if ($attDeptA.data) { $attDeptACount = @($attDeptA.data).Count }
Assert-True (($attDeptA.code -eq 200) -and ($attDeptACount -eq 0)) 'dept_admin cannot see attachments of other-dept device A'

$attDeptB = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$idB" -Headers $dept.Auth
Assert-True (($attDeptB.code -eq 200) -and (@($attDeptB.data).Count -ge 1)) 'dept_admin sees attachments of dept device B'

$attUserB2 = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$idB2" -Headers $user.Auth
$attUserB2Count = 0
if ($attUserB2.data) { $attUserB2Count = @($attUserB2.data).Count }
Assert-True (($attUserB2.code -eq 200) -and ($attUserB2Count -eq 0)) 'user01 cannot see attachments of non-owned B2'

$attUserB = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$idB" -Headers $user.Auth
Assert-True (($attUserB.code -eq 200) -and (@($attUserB.data).Count -ge 1)) 'user01 sees attachments of own device B'

# ---------- Statistics data scope ----------
Write-Host "--- Statistics data scope ---" -ForegroundColor Yellow

$dashAdmin = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/dashboard" -Headers $admin.Auth
$dashDept  = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/dashboard" -Headers $dept.Auth
$dashUser  = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/statistics/dashboard" -Headers $user.Auth
Assert-True ($dashAdmin.code -eq 200) 'admin dashboard ok'
Assert-True ($dashDept.code -eq 200)  'dept_admin dashboard ok'
Assert-True ($dashUser.code -eq 200)  'user01 dashboard ok'

$totalAdmin = [long]$dashAdmin.data.summary.deviceTotal
$totalDept  = [long]$dashDept.data.summary.deviceTotal
$totalUser  = [long]$dashUser.data.summary.deviceTotal
Assert-True ($totalAdmin -ge $totalDept) 'admin deviceTotal >= dept_admin'
Assert-True ($totalDept  -ge $totalUser) 'dept_admin deviceTotal >= user01'
Assert-True ($totalUser -ge 1) 'user01 deviceTotal >= 1 (owns at least B)'

Write-Host ''
Write-Host "=== Result: passed=$passed failed=$failed ===" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Red' })
Write-Host "Seeded devices: $noA / $noB / $noB2 ; projects: $codeA / $codeB"
if ($failed -gt 0) { exit 1 }
