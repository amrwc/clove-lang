for %%f in (*.dumb) do (
    echo %%f
    java -classpath ../bin Dumb < "%%f"
)
