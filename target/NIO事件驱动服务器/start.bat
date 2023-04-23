@ECHO OFF
title NIO-EVENT-SERVER[PORT: 8921]
cls
@ECHO rutime environment
java -version
:RESTART
@echo. 
@echo. ---------------------------------------------------------
@echo. Please wait while the server is starting
@echo. use command: "stop",to shutdown the server
@echo. ---------------------------------------------------------
@echo.
java -jar BaseEventLoopServer.jar
@echo. 
@echo. ---------------------------------------------------------
@echo. Server is closed ,please press any to restart
@echo. ---------------------------------------------------------
@echo. 
pause
goto restart