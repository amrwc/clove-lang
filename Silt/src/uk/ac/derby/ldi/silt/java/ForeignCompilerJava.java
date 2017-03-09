package uk.ac.derby.ldi.silt.java;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.eclipse.jdt.core.compiler.CompilationProgress;

import uk.ac.derby.ldi.sili.exceptions.ExceptionFatal;
import uk.ac.derby.ldi.sili.exceptions.ExceptionSemantic;

/**
 * @author Dave
 *
 */
public class ForeignCompilerJava {
	
	public final static String dataDir = "data";
	
	private boolean verbose;
	
	public ForeignCompilerJava(boolean verbose) {
		this.verbose = verbose;
	}
    
    /** Compile foreign code using Eclipse JDT compiler. */
    public void compileForeignCode(PrintStream stream, String className, String src) {
    	ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
    	ByteArrayOutputStream warningStream = new ByteArrayOutputStream();
    	String warningSetting = new String("allDeprecation,"
    			+ "assertIdentifier," + "charConcat,"
    			+ "conditionAssign," + "constructorName," + "deprecation,"
    			+ "emptyBlock," + "fieldHiding," + "finalBound,"
    			+ "finally," + "indirectStatic," + "intfNonInherited,"
    		 	+ "localHiding," + "maskedCatchBlocks,"
    			+ "noEffectAssign," + "pkgDefaultMethod," + "serial,"
    			+ "semicolon," + "specialParamHiding," + "staticReceiver,"
    			+ "syntheticAccess," + "unqualifiedField,"
    			+ "unnecessaryElse," + "uselessTypeCheck," + "unsafe,"
				+ "unusedImport," + "unusedLocal,"
    			+ "unusedPrivate," + "unusedThrown");

        // If resource directory doesn't exist, create it.
        File resourceDir = new File(dataDir); 
        if (!(resourceDir.exists()))
            resourceDir.mkdirs();
    	File sourcef;
    	try {
    		// Write source to a Java source file
    		sourcef = new File(resourceDir + java.io.File.separator + getStrippedClassname(className) + ".java");
    		PrintStream sourcePS = new PrintStream(new FileOutputStream(sourcef));
    		sourcePS.print(src);
    		sourcePS.close();
    	} catch (IOException ioe) {
    		throw new ExceptionFatal("Unable to save Java source: " + ioe.toString());
    	}

    	String classpath = 
    			cleanClassPath(System.getProperty("java.class.path")) + 
    			java.io.File.pathSeparatorChar + 
    			resourceDir.getAbsolutePath();
    	
   		notify("ForeignCompilerJava: classpath = " + classpath);
    	
    	// Start compilation using JDT
   		String commandLine = "-1.8 -source 1.8 -warn:" + 
    			warningSetting + " " + 
    			"-cp " + classpath + " \"" + sourcef + "\"";
    	boolean compiled = org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
    			commandLine,
    			new PrintWriter(messageStream), new PrintWriter(warningStream), 
    			new CompilationProgress() {
					@Override
					public void begin(int arg0) {
					}
					@Override
					public void done() {
					}
					@Override
					public boolean isCanceled() {
						return false;
					}
					@Override
					public void setTaskName(String arg0) {
						ForeignCompilerJava.this.notify(arg0);
					}
					@Override
					public void worked(int arg0, int arg1) {
					}
    			}
    	);

    	String compilerMessages = "";
    	// Parse the messages and the warnings.
    	BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(messageStream.toByteArray())));
    	while (true) {
    		String str = null;
    		try {
    			str = br.readLine();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		if (str == null) {
    			break;
    		}
    		compilerMessages += str + '\n';
    	}
    	BufferedReader brWarnings = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(warningStream.toByteArray())));
    	while (true) {
    		String str = null;
    		try {
    			str = brWarnings.readLine();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		if (str == null) {
    			break;
    		}
    		compilerMessages += str + '\n';
    	}

    	if (!compiled)
        	throw new ExceptionSemantic("Compilation failed due to errors: \n" + compilerMessages + "\n");    		    
    }
    
    /** Return a classpath cleaned of non-existent files.  
     * Classpath elements with spaces are converted to quote-delimited strings. */
    private final static String cleanClassPath(String s) {
    	if (java.io.File.separatorChar == '/')
    		s = s.replace('\\', '/');
    	else
    		s = s.replace('/', '\\');
        String outstr = "";
        java.util.StringTokenizer st = new java.util.StringTokenizer(s, java.io.File.pathSeparator);
        while (st.hasMoreElements()) {
            String element = (String)st.nextElement();
            java.io.File f = new java.io.File(element);
            if (f.exists()) {
            	String fname = f.toString();
            	if (fname.indexOf(' ') >= 0)
            		fname = '"' + fname + '"';
                outstr += ((outstr.length()>0) ? java.io.File.pathSeparator : "") + fname;
            }
        }
        return outstr;
    }

    /** Get a stripped name.  Only return text after the final '.' */
    private static String getStrippedName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0)
            return name.substring(lastDot + 1);
        else
            return name;
    }
    
    /** Get stripped Java Class name. */
    private static String getStrippedClassname(String name) {
    	return getStrippedName(name);
    }
    
    private void notify(String s) {
    	if (verbose)
    		System.out.println(s);
    }
    
}
