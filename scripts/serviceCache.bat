@rem ***************************************************************************
@rem Copyright (c) 2016 eBay Software Foundation.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem  http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem ***************************************************************************
@echo off
setlocal EnableDelayedExpansion

title Service COLD Cache Util

call setenv.bat

SET "CLASS_LAUNCHER=org.ebayopensource.io.netty.http.snoop.HttpSnoopClient"

if not ""%1""=="""" goto validateCMD

:validateCMD
if ""%1""==""enableCache"" goto setCMD
if ""%1""==""disableCache"" goto setCMD
if ""%1""==""cleanupCache"" goto setCMD
echo "Usage: %~n0 <action>(enableCache/disableCache/cleanupCache)"
goto end

:setCMD
set "EXEC_CMD=%1"

:setArgs
set CMD_LINE_ARGS=%EXEC_CMD%
if ""%2""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %2
shift
goto setArgs
:doneSetArgs

echo EXEC_CMD is %EXEC_CMD%

if not "%SCC_HOME%" == "" goto gotHome
set "SCC_HOME=%cd%"
if exist "%SCC_HOME%\%SCC_LIB_NAME%" goto okHome
:gotHome
if exist "%SCC_HOME%\%SCC_LIB_NAME%" goto okHome
echo ERROR: the library %SCC_LIB_NAME% is not found.
goto end
:okHome


SET "SCC_LIB_PATH=%SCC_HOME%\%SCC_LIB_NAME%"

echo Using JAVA_HOME:       "%JAVA_HOME%"
echo Using SCC_HOME:   "%SCC_HOME%"
echo Using SCC_LIB_PATH:   "%SCC_LIB_PATH%"

"%JAVA_HOME%\bin\java" -cp "%SCC_LIB_PATH%" %CLASS_LAUNCHER% %CMD_LINE_ARGS%


::curl http://127.0.0.1:55321/cmd/disableCache?arg1=value1

:end
