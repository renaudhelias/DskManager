@echo off
java -jar DskManager.jar help
java -jar DskManager.jar newDsk DOSD2 hello.dsk
java -jar DskManager.jar addFileDsk hello.dsk src\dskmanager\HELLO.BAS
java -jar DskManager.jar addFileDsk hello.dsk ETOILE.BAS
pause
exit