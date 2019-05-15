for %%f in (*.clove) do (
    echo %%f
    java -classpath ../bin Clove < "%%f"
)
