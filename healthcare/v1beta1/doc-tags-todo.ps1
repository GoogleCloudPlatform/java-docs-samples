$srcs = gci -Recurse *.java

$start_tags = @{}
$end_tags = @{}

foreach ($src in $srcs) {
    $line_number = 1    
    foreach ($line in (Get-Content $src)) {
        if ($line -match "\[\s?START\s+([A-Za-z0-9_-]+)\s?\]") {
            $start_tags.Add($matches[1], "${src}:$line_number") | Out-Null
        }
        if ($line -match "\[\s?END\s+([A-Za-z0-9_-]+)\s?\]") {
            $end_tags.Add($matches[1], "${src}:$line_number") | Out-Null
        }
        $line_number += 1
    }
}

foreach ($key in $start_tags.Keys) {
    if (!$end_tags.ContainsKey($key)) {
        $where = $start_tags[$key]
        "START tag at $where missing END tag." | Write-Warning
    }
}

foreach ($line in (Get-Content "doc-tags-all.txt")) {
    if ($start_tags.Contains($line.Trim())) {
        # done.
    } else {
        $line
    }
}


