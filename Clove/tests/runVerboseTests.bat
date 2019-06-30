:: echo off -- prevent printing irrelevant
:: code to the command-line
:: @ -- prevent 'echo off' in the command-line
@echo off

:: For each file ending with '.clove' do
for %%f in (*.clove) do (
  echo ============ %%f ============
  java -classpath ../bin Clove < "%%f"
  :: echo. -- new line
  echo.
  echo.
)
