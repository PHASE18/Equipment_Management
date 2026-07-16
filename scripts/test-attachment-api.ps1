# Attachment Module API E2E (ASCII-only messages)
$ErrorActionPreference = 'Stop'
$BaseUrl = if ($env:API_BASE_URL) { $env:API_BASE_URL } else { 'http://localhost:8080' }
$passed = 0
$failed = 0
$tempDir = Join-Path $env:TEMP "eq-attach-e2e"
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
    [object]$Body = $null,
    [string]$ContentType = 'application/json; charset=utf-8'
  )
  $params = @{
    Method      = $Method
    Uri         = $Url
    Headers     = $Headers
    ContentType = $ContentType
  }
  if ($null -ne $Body) {
    if ($Body -is [byte[]]) {
      $params.Body = $Body
    } else {
      $json = $Body | ConvertTo-Json -Compress -Depth 6
      $params.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
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

  $all = $bodyStream.ToArray()
  $reqHeaders = @{}
  foreach ($k in $Headers.Keys) { $reqHeaders[$k] = $Headers[$k] }
  $response = Invoke-WebRequest -Method Post -Uri $Url -Headers $reqHeaders -ContentType "multipart/form-data; boundary=$boundary" -Body $all -UseBasicParsing
  return ($response.Content | ConvertFrom-Json)
}

Write-Host "=== Attachment Module API E2E ($BaseUrl) ===" -ForegroundColor Cyan

$login = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/login" -Body @{ username = 'admin'; password = '123456' }
Assert-True (($login.code -eq 200) -and [bool]$login.data.token) 'login get token'
$token = [string]$login.data.token
$auth = @{ Authorization = "Bearer $token" }

$noAuth = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/1"
Assert-True ($noAuth.code -eq 401) 'anonymous list returns 401'

# ensure device exists
$deviceList = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/device/list?pageNum=1&pageSize=1" -Headers $auth
$deviceId = $null
if ($deviceList.data.total -gt 0) {
  $deviceId = [long]$deviceList.data.records[0].id
} else {
  $ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
  $devCode = "ATT-DEV-$ts"
  $null = Invoke-ApiJson -Method Post -Url "$BaseUrl/api/device" -Headers $auth -Body @{
    deviceNo       = $devCode
    deviceName     = "ATT_DEVICE_$ts"
    sn             = "SN-ATT-$ts"
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

# validation: empty file
$emptyPath = Join-Path $tempDir 'empty.pdf'
[System.IO.File]::WriteAllBytes($emptyPath, @())
$emptyUpload = Invoke-MultipartUpload -Url "$BaseUrl/api/file/upload" -Headers $auth -FilePath $emptyPath -Fields @{
  deviceId = "$deviceId"
  category = 'document'
}
Assert-True ($emptyUpload.code -eq 400) 'empty file rejected'

# validation: exe
$exePath = Join-Path $tempDir 'bad.exe'
[System.IO.File]::WriteAllText($exePath, 'MZ')
$exeUpload = Invoke-MultipartUpload -Url "$BaseUrl/api/file/upload" -Headers $auth -FilePath $exePath -Fields @{
  deviceId = "$deviceId"
  category = 'document'
}
Assert-True ($exeUpload.code -eq 400) 'exe file rejected'

# validation: device not found
$txtPath = Join-Path $tempDir 'ok.txt'
[System.IO.File]::WriteAllText($txtPath, 'hello attachment e2e')
$missingDevice = Invoke-MultipartUpload -Url "$BaseUrl/api/file/upload" -Headers $auth -FilePath $txtPath -Fields @{
  deviceId = '999999'
  category = 'document'
}
Assert-True ($missingDevice.code -eq 1004) 'missing device returns 1004'

# upload success
$upload = Invoke-MultipartUpload -Url "$BaseUrl/api/file/upload" -Headers $auth -FilePath $txtPath -Fields @{
  deviceId     = "$deviceId"
  category     = 'document'
  fileTypeCode = 'OTHER_DOC'
}
Assert-True (($upload.code -eq 200) -and [bool]$upload.data.fileId) 'upload document success'
$fileId = [long]$upload.data.fileId
Assert-True ($upload.data.deviceId -eq $deviceId) 'upload returns deviceId'
Assert-True ($upload.data.fileName -eq 'ok.txt') 'upload returns fileName'

# list
$list = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$deviceId" -Headers $auth
Assert-True (($list.code -eq 200) -and ($list.data.Count -ge 1)) 'list device files'
$found = @($list.data | Where-Object { $_.fileId -eq $fileId }).Count -gt 0
Assert-True $found 'uploaded file appears in list'

# attachment page API
$page = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/attachment/page?pageNum=1&pageSize=20&keyword=ok.txt" -Headers $auth
Assert-True (($page.code -eq 200) -and ($page.data.total -ge 1)) 'attachment page by keyword'

# download
$download = Invoke-WebRequest -Method Get -Uri "$BaseUrl/api/file/download/$fileId" -Headers $auth -UseBasicParsing
Assert-True (($download.StatusCode -eq 200) -and ($download.Content.Length -gt 0)) 'download file success'
Assert-True ($download.Headers['Content-Disposition'] -match 'ok') 'download has content-disposition'

# batch upload (use curl for reliable multi-file multipart)
$txt2 = Join-Path $tempDir 'ok2.txt'
[System.IO.File]::WriteAllText($txt2, 'batch-2')
$batchRaw = & curl.exe -s -X POST "$BaseUrl/api/file/batch-upload" `
  -H "Authorization: Bearer $token" `
  -F "deviceId=$deviceId" `
  -F "category=document" `
  -F "files=@$txtPath;type=text/plain" `
  -F "files=@$txt2;type=text/plain"
$batchJson = $batchRaw | ConvertFrom-Json
$batchCount = @($batchJson.data).Count
Assert-True (($batchJson.code -eq 200) -and ($batchCount -ge 2)) "batch upload success count=$batchCount"

# delete
$del = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/file/$fileId" -Headers $auth
Assert-True ($del.code -eq 200) 'delete file success'
$listAfter = Invoke-ApiJson -Method Get -Url "$BaseUrl/api/file/list/$deviceId" -Headers $auth
$still = @($listAfter.data | Where-Object { $_.fileId -eq $fileId }).Count -gt 0
Assert-True (-not $still) 'deleted file removed from list'

$delMissing = Invoke-ApiJson -Method Delete -Url "$BaseUrl/api/file/999999" -Headers $auth
Assert-True ($delMissing.code -eq 1011) 'delete missing returns 1011'

Write-Host ''
Write-Host "=== Result: passed=$passed failed=$failed ===" -ForegroundColor $(if ($failed -eq 0) { 'Green' } else { 'Red' })
if ($failed -gt 0) { exit 1 }
