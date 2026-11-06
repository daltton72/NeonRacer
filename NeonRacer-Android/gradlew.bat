@echo off
setlocal
set APP_HOME=%~dp0
if not exist "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$b=[Convert]::FromBase64String((Get-Content -Raw '%APP_HOME%gradle\wrapper\gradle-wrapper.jar.b64')); [IO.File]::WriteAllBytes('%APP_HOME%gradle\wrapper\gradle-wrapper.jar',$b)"
  if errorlevel 1 exit /b 1
)
if defined JAVA_HOME (set JAVA_EXE=%JAVA_HOME%\bin\java.exe) else (set JAVA_EXE=java.exe)
"%JAVA_EXE%" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
endlocal
