echo %1 %0
ant24 -Dbasedir=%CD%   -f %~dp0/../../pceant/pce.xml %1 %2 %3
