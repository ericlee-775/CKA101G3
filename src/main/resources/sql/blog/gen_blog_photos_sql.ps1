# ============================================================
#  一鍵：貼 GCP 圖片連結 → 自動下載 → 產生「內嵌 BLOB」的 SQL
#  (純 SQL 無法從網址下載，所以由這支腳本下載後寫進 SQL)
#
#  用法：
#   1. 把下面 $map 裡每個 blog_id 後面，貼上你的 GCP 圖片網址
#   2. 在這個檔上右鍵 → 用 PowerShell 執行
#      (或終端機：powershell -File gen_blog_photos_sql.ps1)
#   3. 它會下載圖片並產生 blog_photos_data.sql
#      → 用 MySQL Workbench/DBeaver 執行那個檔，圖片就進資料庫
#   4. 回瀏覽器重新整理 blogs 頁，圖片出現(前端 <img> 已加好)
# ============================================================

# ↓↓↓ 你只要改這裡：blog_id = '你的 GCP 圖片網址'（沒有的留空字串 '' 會自動跳過）
$map = [ordered]@{
  9006 = 'https://storage.googleapis.com/cka101-15/Blog/9006%E9%A3%9F%E8%AD%9C.png'   # 香蕉燕麥鬆餅
  9005 = 'https://storage.googleapis.com/cka101-15/Blog/9005%E6%B5%B7%E9%A2%A8.png'   # 海風農場體驗日
  9004 = 'https://storage.googleapis.com/cka101-15/Blog/9004%E5%B1%B1%E9%96%93%E8%BE%B2%E5%A0%B4.png'   # 山間農場參訪記
  9003 = 'https://storage.googleapis.com/cka101-15/Blog/9003%E7%95%B6%E5%AD%A3.png'   # 當季蔬果怎麼挑
  9002 = 'https://storage.googleapis.com/cka101-15/Blog/9002%E6%9C%89%E6%A9%9F%E8%BE%B2%E5%A0%B4.png'   # 有機農場的一天
  9001 = 'https://storage.googleapis.com/cka101-15/Blog/9001%E4%B8%80%E6%A0%B9%E9%A6%99%E8%95%89.png'   # 一根香蕉的產地旅程
}
# ↑↑↑ 例如：9006 = 'https://storage.googleapis.com/你的桶/banana.jpg'

$ErrorActionPreference = 'Stop'
$here   = Split-Path -Parent $MyInvocation.MyCommand.Path
$outSql = Join-Path $here 'blog_photos_data.sql'

# ---- 資料庫連線（如果帳密不同就改這裡）----
$dbHost = '127.0.0.1'; $dbPort = '3306'; $dbName = 'farmily'
$dbUser = 'root';      $dbPass = '123456'

$lines  = @('-- 自動產生：GCP 圖片已下載並內嵌成 BLOB，可直接執行')

foreach ($entry in $map.GetEnumerator()) {
  $blogId = $entry.Key
  $url    = $entry.Value
  if ([string]::IsNullOrWhiteSpace($url)) { continue }
  $tmp = [System.IO.Path]::GetTempFileName()
  try {
    # 用 OutFile 下載成檔案再讀 bytes，避免 PowerShell 5.1 處理二進位的問題
    Invoke-WebRequest -Uri $url -OutFile $tmp -UseBasicParsing
    $bytes = [System.IO.File]::ReadAllBytes($tmp)
    $hex   = ([System.BitConverter]::ToString($bytes)) -replace '-', ''
    $lines += "UPDATE blog SET blog_img = 0x$hex WHERE blog_id = $blogId;"
    Write-Host ("blog_id={0} 下載成功 ({1:N0} bytes)" -f $blogId, $bytes.Length) -ForegroundColor Green
  } catch {
    Write-Host ("blog_id={0} 下載失敗：{1}" -f $blogId, $_.Exception.Message) -ForegroundColor Red
  } finally {
    if (Test-Path $tmp) { Remove-Item $tmp -Force }
  }
}

$lines += 'SELECT blog_id, blog_title, LENGTH(blog_img) AS img_bytes FROM blog WHERE blog_img IS NOT NULL;'
[System.IO.File]::WriteAllText($outSql, ($lines -join "`r`n"), [System.Text.Encoding]::UTF8)

Write-Host ""
Write-Host "完成！已產生：$outSql" -ForegroundColor Green

# ---- 嘗試自動寫進資料庫（找得到 mysql.exe 就直接匯入，不用再開 Workbench）----
$mysqlPath = $null
$found = Get-ChildItem 'C:\Program Files\MySQL\MySQL Server*\bin\mysql.exe' -ErrorAction SilentlyContinue | Select-Object -First 1
if ($found) { $mysqlPath = $found.FullName }
if (-not $mysqlPath) { $c = Get-Command mysql.exe -ErrorAction SilentlyContinue; if ($c) { $mysqlPath = $c.Source } }

if ($mysqlPath) {
  Write-Host "找到 mysql，正在自動匯入資料庫..." -ForegroundColor Cyan
  $myArgs = @("-h$dbHost", "-P$dbPort", "-u$dbUser", "-p$dbPass", $dbName)
  Get-Content $outSql -Raw | & $mysqlPath @myArgs
  if ($LASTEXITCODE -eq 0) {
    Write-Host "成功！圖片已匯入資料庫，回網頁重新整理就看得到圖。" -ForegroundColor Green
  } else {
    Write-Host "自動匯入失敗，請改用 MySQL Workbench 開 blog_photos_data.sql 手動執行。" -ForegroundColor Yellow
  }
} else {
  Write-Host "找不到 mysql.exe，請用 MySQL Workbench 開 blog_photos_data.sql 手動執行。" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "（按任意鍵關閉視窗）"
