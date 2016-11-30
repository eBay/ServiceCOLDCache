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

call setenv.bat

if not "%SCC_HOME%" == "" goto gotHome
set "SCC_HOME=%cd%"
if exist "%SCC_HOME%\%SCC_LIB_NAME%" goto okHome
:gotHome
if exist "%SCC_HOME%\%SCC_LIB_NAME%" goto okHome
echo ERROR: the library %SCC_LIB_NAME% is not found.
goto end
:okHome


SET "SCC_LIB_PATH=%SCC_HOME%\%SCC_LIB_NAME%"
echo Stopping Service COLD Cache...!
echo Using SCC_HOME:   "%SCC_HOME%"
echo Using SCC_LIB_PATH:   "%SCC_LIB_PATH%"

TASKKILL /f /FI "Windowtitle eq %WINDOWS_TITLE%" 

:end