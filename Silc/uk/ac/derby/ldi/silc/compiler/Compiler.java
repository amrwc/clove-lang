package uk.ac.derby.ldi.silc.compiler;

import uk.ac.derby.ldi.silc.parser.ast.ASTCode;
import uk.ac.derby.ldi.silc.parser.ast.Silc;
import uk.ac.derby.ldi.silc.parser.ast.SilcVisitor;
import uk.ac.derby.ldi.sili.vm.*;

import java.io.*;
import java.beans.XMLEncoder;

public class Compiler {
	
	private static void usage() {
		System.out.println("Usage: silc [-d0 | -d1] < <source>");
		System.out.println("          -d0 -- run-time debugging");
		System.out.println("          -d1 -- output AST");
	}
	
	public static void main(String args[]) {
		boolean debugOnRun = false;
		boolean debugAST = false;
		if (args.length == 1) {
			if (args[0].equals("-d0"))
				debugOnRun = true;
			else if (args[0].equals("-d1"))
				debugAST = true;
			else {
				usage();
				return;
			}
		}
		Silc language = new Silc(System.in);
		try {
			ASTCode parser = language.code();
			SilcVisitor nodeVisitor;
			if (debugAST)
				nodeVisitor = new ParserDebugger();
			else {
				if (debugOnRun)
					System.out.println("Compiling...");
				nodeVisitor = new Parser();
			}
			// Run the input stream through through parser/compiler.  Obtain an executable ValueOperator as a result.
			Operator _main = (Operator)parser.jjtAccept(nodeVisitor, null);
			// Dump if debugging
			if (debugOnRun) {
				System.out.println("Compiled:");
				(new Dumper()).dumpMachineCode(_main);
				System.out.println("Executing...");
			}
			//
			// Write a sil "executable" file
			try {
				try (FileOutputStream fstream = new FileOutputStream("out.sxe")) {
					try (XMLEncoder xmlEncoder = new XMLEncoder(fstream)) {
						xmlEncoder.writeObject(_main);
						xmlEncoder.flush();
					}
				}
			} catch (Exception e) {
				System.out.println("Generator: Error writing executable: " + e.getMessage());
			}
			//
			// Go
			(new VirtualMachine(System.out)).execute(_main);
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}
}
