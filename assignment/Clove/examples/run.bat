:: echo off -- prevent printing irrelevant
:: code to the command-line
:: @ -- prevent 'echo off' in the command-line
@echo off

:: %* -- inserts all command-line arguments
java -classpath ../bin Clove < %*
