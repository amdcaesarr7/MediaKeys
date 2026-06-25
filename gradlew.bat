@rem Gradle wrapper script for Windows
@if "%DEBUG%"=="" @echo off
@rem Set local scope for the variables
setlocal

set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve JAVA_HOME
if defined JAVA_HOME goto execute
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail

:execute
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
"%JAVA_HOME%\bin\java" ^
  -classpath "%CLASSPATH%" ^
  org.gradle.wrapper.GradleWrapperMain %*

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd_ return code!
if not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1
