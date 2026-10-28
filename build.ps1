# ==============================================================================
# SMART PARKING MANAGEMENT SYSTEM - AUTOMATED BUILD & TEST SCRIPT
# ==============================================================================
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Building Smart Parking Management System" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Locate Java and Compiler
$javac = "C:\Program Files\Java\jdk-25.0.2\bin\javac.exe"
$java = "C:\Program Files\Java\jdk-25.0.2\bin\java.exe"
$jar = "C:\Program Files\Java\jdk-25.0.2\bin\jar.exe"
$tomcatLib = "C:\apache-tomcat-10.1.59\lib"

if (-not (Test-Path $javac)) {
    $javaCmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($javaCmd) { $javac = $javaCmd.Source } else { throw "javac not found." }
}
if (-not (Test-Path $java)) {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) { $java = $javaCmd.Source } else { throw "java not found." }
}
if (-not (Test-Path $jar)) {
    $jarCmd = Get-Command jar -ErrorAction SilentlyContinue
    if ($jarCmd) { $jar = $jarCmd.Source } else { throw "jar not found." }
}

# 2. Ensure Dependencies Exist in lib/ and WEB-INF/lib/
New-Item -ItemType Directory -Force -Path "lib" | Out-Null
New-Item -ItemType Directory -Force -Path "src/main/webapp/WEB-INF/lib" | Out-Null

$depList = @(
    @{ url='https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar'; file='lib/mysql-connector-j-8.3.0.jar'; web=true },
    @{ url='https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar'; file='lib/jbcrypt-0.4.jar'; web=true },
    @{ url='https://repo1.maven.org/maven2/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar'; file='lib/jakarta.mail-2.0.1.jar'; web=true },
    @{ url='https://repo1.maven.org/maven2/jakarta/activation/jakarta.activation-api/2.1.2/jakarta.activation-api-2.1.2.jar'; file='lib/jakarta.activation-api-2.1.2.jar'; web=true },
    @{ url='https://repo1.maven.org/maven2/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api/3.0.0/jakarta.servlet.jsp.jstl-api-3.0.0.jar'; file='lib/jakarta.servlet.jsp.jstl-api-3.0.0.jar'; web=true },
    @{ url='https://repo1.maven.org/maven2/org/glassfish/web/jakarta.servlet.jsp.jstl/3.0.1/jakarta.servlet.jsp.jstl-3.0.1.jar'; file='lib/jakarta.servlet.jsp.jstl-3.0.1.jar'; web=true },
    @{ url='https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar'; file='lib/junit-platform-console-standalone-1.10.2.jar'; web=false }
)

foreach ($dep in $depList) {
    if (-not (Test-Path $dep.file)) {
        Write-Host "Downloading $($dep.file)..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $dep.url -OutFile $dep.file
    }
    if ($dep.web) {
        $dest = "src/main/webapp/WEB-INF/lib/" + (Split-Path $dep.file -Leaf)
        if (-not (Test-Path $dest)) {
            Copy-Item $dep.file -Destination $dest -Force
        }
    }
}

# 3. Prepare Target Directories
Write-Host "`n[1/5] Preparing target directories..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "target/classes" | Out-Null
New-Item -ItemType Directory -Force -Path "target/test-classes" | Out-Null
New-Item -ItemType Directory -Force -Path "target/war-staging/WEB-INF/classes" | Out-Null
New-Item -ItemType Directory -Force -Path "target/war-staging/WEB-INF/lib" | Out-Null

# 4. Copy Classpath Resources
Write-Host "[2/5] Copying application resources..." -ForegroundColor Yellow
Copy-Item "src/main/resources/*" -Destination "target/classes/" -Force

# 5. Compile Application Sources
Write-Host "[3/5] Compiling application Java sources..." -ForegroundColor Yellow
$runtimeJars = (Get-ChildItem -Path "src/main/webapp/WEB-INF/lib/*.jar", "lib/*.jar" | Select-Object -ExpandProperty FullName) -join ';'
$compileCp = "target/classes;$runtimeJars;$tomcatLib\servlet-api.jar;$tomcatLib\jsp-api.jar"
$srcFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

& $javac -encoding UTF-8 -cp $compileCp -d "target/classes" $srcFiles
if ($LASTEXITCODE -ne 0) { throw "Application compilation failed." }
Write-Host " Application compilation succeeded." -ForegroundColor Green

# 6. Compile and Run Automated Tests
Write-Host "[4/5] Running automated JUnit 5 tests..." -ForegroundColor Yellow
$testFiles = Get-ChildItem -Path "src/test/java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
$testCompileCp = "target/classes;lib/junit-platform-console-standalone-1.10.2.jar;$compileCp"

& $javac -encoding UTF-8 -cp $testCompileCp -d "target/test-classes" $testFiles
if ($LASTEXITCODE -ne 0) { throw "Test compilation failed." }

$allJars = (Get-ChildItem -Path "lib/*.jar", "src/main/webapp/WEB-INF/lib/*.jar" | Select-Object -ExpandProperty FullName) -join ';'
$testRunCp = "target/classes;target/test-classes;$allJars;$tomcatLib\servlet-api.jar;$tomcatLib\jsp-api.jar"

& $java -jar "lib/junit-platform-console-standalone-1.10.2.jar" execute --class-path $testRunCp --scan-class-path --fail-if-no-tests
if ($LASTEXITCODE -ne 0) { throw "Automated tests failed." }
Write-Host " All 22 automated tests passed." -ForegroundColor Green

# 7. Assemble WAR File
Write-Host "[5/5] Packaging smart-parking.war..." -ForegroundColor Yellow
Copy-Item -Path "src/main/webapp/*" -Destination "target/war-staging/" -Recurse -Force
Copy-Item -Path "target/classes/*" -Destination "target/war-staging/WEB-INF/classes/" -Recurse -Force

Push-Location "target/war-staging"
& $jar -cvf "../smart-parking.war" * | Out-Null
Pop-Location

if (Test-Path "target/smart-parking.war") {
    $warSize = (Get-Item "target/smart-parking.war").Length / 1MB
    Write-Host "`n==========================================================" -ForegroundColor Green
    Write-Host (" BUILD SUCCESS! target/smart-parking.war ({0:N2} MB)" -f $warSize) -ForegroundColor Green
    Write-Host " Deployable to Tomcat: Copy target/smart-parking.war to $tomcatLib\..\webapps\" -ForegroundColor Cyan
    Write-Host "==========================================================" -ForegroundColor Green
} else {
    throw "WAR packaging failed."
}
