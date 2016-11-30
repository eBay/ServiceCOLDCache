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

title Service Cold Cache Client

SET errorlevel=
call setenv.bat
IF errorlevel 1 (
    EXIT /b %errorlevel%
)
SET "SCC_LAUNCHER=org.ebayopensource.scc.Launcher"

if exist "%SCC_HOME%\%SCC_LIB_NAME%" goto okHome
:gotHome
if exist "%SCC_HOME%\%SCC_LIB_NAME%" goto okHome
echo ERROR: the library %SCC_LIB_NAME% is not found.
goto end
:okHome

SET "SCC_LIB_PATH=%SCC_HOME%\%SCC_LIB_NAME%"

echo Starting Service COLD Cache...
echo Using JAVA_HOME:       "%JAVA_HOME%"
echo Using SCC_HOME:   "%SCC_HOME%"
echo Using SCC_LIB_PATH:   "%SCC_LIB_PATH%"
echo Executing: %JAVA_HOME%\bin\java -cp %SCC_LIB_PATH% %SCC_LAUNCHER% %*
start /max "%WINDOWS_TITLE%" "%JAVA_HOME%\bin\java" -Xmx1536m -XX:MaxPermSize=512m -Dorg.ebayopensource.ssc.initiator=CMD -cp "%SCC_LIB_PATH%" %SCC_LAUNCHER% %*
