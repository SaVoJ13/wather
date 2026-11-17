@rem Gradle runner for Weather project
@echo off
set "GRADLE_OPTS=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8"
set "GRADLE_BAT=C:\Users\cocro\.gradle\wrapper\dists\gradle-9.6.0-bin\42k10rwplmzkhuboz9kdazi7s\gradle-9.6.0\bin\gradle.bat"
if exist "%GRADLE_BAT%" (
    call "%GRADLE_BAT%" -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m" %*
) else (
    gradle %*
)
