Examples for 6CC509 Language Design and Implementation 

To load these as an Eclipse project, select "File | Import... | Git | Projects from Git" and pick "Clone URI". The URI is https://github.com/DaveVoorhis/LDI.git

Install the JavaCC Eclipse plugin via "Help | Install New Software". The update site (i.e., the URL to put in "Work with", then press "Add") is http://eclipse-javacc.sourceforge.net/

If a project doesn't compile, right click the .jjt or .jjt file -- usually found in the \*.parser package -- and select "Compile with javacc | jjtree | jtb". If it's greyed-out, double click on the file to load it into the editor, then right click on it again. 
