$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$tmpdir = Join-Path $env:TEMP "booking-api-test"
New-Item -ItemType Directory -Force -Path $tmpdir | Out-Null

$passed = 0
$failed = 0
$results = @()
$teacherToken = $null
$parentToken = $null
$courseId = $null
$offeringId = $null
$offering2Id = $null

function Write-JsonFile {
    param([string]$Path, [object]$Object)
    $json = $Object | ConvertTo-Json -Depth 10 -Compress
    [System.IO.File]::WriteAllText($Path, $json, [System.Text.UTF8Encoding]::new($false))
}

function Invoke-Api {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [int[]]$ExpectedStatus
    )

    $bodyFile = $null
    $curlArgs = @("-s", "-w", "`n__HTTP__%{http_code}", "-X", $Method, $Url)

    foreach ($key in $Headers.Keys) {
        $curlArgs += @("-H", "$key`: $($Headers[$key])")
    }

    if ($null -ne $Body) {
        $bodyFile = Join-Path $tmpdir "$([guid]::NewGuid()).json"
        Write-JsonFile -Path $bodyFile -Object $Body
        $curlArgs += @("-H", "Content-Type: application/json", "--data-binary", "@$bodyFile")
    }

    $raw = (& curl.exe @curlArgs) -join "`n"
    if ($bodyFile) { Remove-Item $bodyFile -Force -ErrorAction SilentlyContinue }

    if ($raw -match '__HTTP__(\d+)$') {
        $status = [int]$Matches[1]
        $content = $raw -replace '__HTTP__\d+$', ''
    } else {
        $status = 0
        $content = $raw
    }
    $ok = $ExpectedStatus -contains $status

    if ($ok) { $script:passed++ } else { $script:failed++ }

    $preview = if ($content.Length -gt 180) { $content.Substring(0, 180) + "..." } else { $content }
    $script:results += [pscustomobject]@{
        Name   = $Name
        Status = $status
        Pass   = $ok
        Body   = $preview
    }

    return [pscustomobject]@{
        Status  = $status
        Content = $content
        Json    = if ($content) { try { $content | ConvertFrom-Json } catch { $null } } else { $null }
    }
}

Write-Host "=== API Endpoint Tests (curl) ===" -ForegroundColor Cyan

Invoke-Api "Health" GET "$base/actuator/health" @{} $null @(200) | Out-Null

$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$teacherEmail = "teacher-$ts@test.com"
$parentEmail = "parent-$ts@test.com"
$password = "password123"

$regTeacher = Invoke-Api "Register Teacher" POST "$base/api/v1/auth/register" @{} @{
    email       = $teacherEmail
    password    = $password
    role        = "TEACHER"
    displayName = "Test Teacher"
    timezone    = "America/New_York"
} @(201)

$regParent = Invoke-Api "Register Parent" POST "$base/api/v1/auth/register" @{} @{
    email       = $parentEmail
    password    = $password
    role        = "PARENT"
    displayName = "Test Parent"
    timezone    = "Europe/London"
} @(201)

$teacherToken = $regTeacher.Json.accessToken
$parentToken = $regParent.Json.accessToken

Invoke-Api "Login Teacher" POST "$base/api/v1/auth/login" @{} @{
    email    = $teacherEmail
    password = $password
} @(200) | Out-Null

Invoke-Api "Login Parent" POST "$base/api/v1/auth/login" @{} @{
    email    = $parentEmail
    password = $password
} @(200) | Out-Null

Invoke-Api "Duplicate Register" POST "$base/api/v1/auth/register" @{} @{
    email       = $teacherEmail
    password    = $password
    role        = "TEACHER"
    displayName = "Dup"
    timezone    = "UTC"
} @(400) | Out-Null

Invoke-Api "Profile Teacher" GET "$base/api/v1/users/me/profile" @{ Authorization = "Bearer $teacherToken" } $null @(200) | Out-Null
Invoke-Api "Profile Parent" GET "$base/api/v1/users/me/profile" @{ Authorization = "Bearer $parentToken" } $null @(200) | Out-Null
Invoke-Api "Profile No Auth" GET "$base/api/v1/users/me/profile" @{} $null @(401, 403) | Out-Null
Invoke-Api "Parent Teacher API" GET "$base/api/v1/teachers/offerings" @{ Authorization = "Bearer $parentToken" } $null @(403) | Out-Null

$courseResp = Invoke-Api "Create Course" POST "$base/api/v1/teachers/courses" @{
    Authorization = "Bearer $teacherToken"
} @{
    title       = "Math 101"
    description = "Intro math"
} @(201)
$courseId = $courseResp.Json.id

$offeringResp = Invoke-Api "Create Offering" POST "$base/api/v1/teachers/offerings" @{
    Authorization = "Bearer $teacherToken"
} @{
    courseId        = $courseId
    name            = "Spring Math"
    teacherTimezone = "America/New_York"
    status          = "PUBLISHED"
} @(201)
$offeringId = $offeringResp.Json.id

Invoke-Api "Add Sessions" POST "$base/api/v1/teachers/offerings/$offeringId/sessions" @{
    Authorization = "Bearer $teacherToken"
} @{
    sessions = @(
        @{ localStart = "2026-06-15T10:00:00"; localEnd = "2026-06-15T11:00:00" }
        @{ localStart = "2026-06-22T10:00:00"; localEnd = "2026-06-22T11:00:00" }
    )
} @(200) | Out-Null

Invoke-Api "Teacher List Offerings" GET "$base/api/v1/teachers/offerings" @{
    Authorization = "Bearer $teacherToken"
} $null @(200) | Out-Null

Invoke-Api "Parent List Offerings" GET "$base/api/v1/offerings" @{
    Authorization = "Bearer $parentToken"
    "X-Timezone"  = "Europe/London"
} $null @(200) | Out-Null

Invoke-Api "Book Offering" POST "$base/api/v1/offerings/$offeringId/bookings" @{
    Authorization = "Bearer $parentToken"
} @{} @(201) | Out-Null

Invoke-Api "Duplicate Booking" POST "$base/api/v1/offerings/$offeringId/bookings" @{
    Authorization = "Bearer $parentToken"
} @{} @(409) | Out-Null

Invoke-Api "Parent List Bookings" GET "$base/api/v1/parents/me/bookings" @{
    Authorization = "Bearer $parentToken"
    "X-Timezone"  = "Europe/London"
} $null @(200) | Out-Null

$offering2Resp = Invoke-Api "Create Overlap Offering" POST "$base/api/v1/teachers/offerings" @{
    Authorization = "Bearer $teacherToken"
} @{
    courseId        = $courseId
    name            = "Overlap Math"
    teacherTimezone = "America/New_York"
    status          = "PUBLISHED"
} @(201)
$offering2Id = $offering2Resp.Json.id

Invoke-Api "Add Overlap Sessions" POST "$base/api/v1/teachers/offerings/$offering2Id/sessions" @{
    Authorization = "Bearer $teacherToken"
} @{
    sessions = @(
        @{ localStart = "2026-06-15T10:30:00"; localEnd = "2026-06-15T11:30:00" }
    )
} @(200) | Out-Null

Invoke-Api "Overlap Booking Conflict" POST "$base/api/v1/offerings/$offering2Id/bookings" @{
    Authorization = "Bearer $parentToken"
} @{} @(409) | Out-Null

Write-Host ""
$results | Format-Table -AutoSize
Write-Host "PASSED: $passed / $($passed + $failed)" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
if ($failed -gt 0) {
    Write-Host "FAILED: $failed" -ForegroundColor Red
    exit 1
}
