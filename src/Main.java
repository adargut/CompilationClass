import ast.*;
import symboltable.SymbolTable;
import visitor.AstPrintVisitor;
import visitor.BuildClassHierarchyVisitor;
import visitor.MethodRenameVisitor;
import visitor.VariableRenameVisitor;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                throw new UnsupportedOperationException("TODO - Ex. 4");
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    throw new UnsupportedOperationException("TODO - Ex. 3");

                } else if (action.equals("compile")) {
                    throw new UnsupportedOperationException("TODO - Ex. 2");

                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
                    // TODO: Validate input
                    var originalLine = Integer.parseInt(args[4]);
                    var newName = args[5];

                    boolean isMethod;
                    if (type.equals("var")) {
                        isMethod = false;
                    } else if (type.equals("method")) {
                        isMethod = true;
                    } else {
                        throw new IllegalArgumentException("unknown rename type " + type);
                    }

                    BuildClassHierarchyVisitor buildClassHierarchyVisitor = new BuildClassHierarchyVisitor();
                    buildClassHierarchyVisitor.visit(prog);

                    SymbolTable symbolTable = buildClassHierarchyVisitor.getSymbolTable();

                    if (isMethod) {
                        MethodRenameVisitor methodRenameVisitor = new MethodRenameVisitor(newName, symbolTable, originalName, originalLine);
                        methodRenameVisitor.visit(prog);
                    }
                    else {
                        VariableRenameVisitor variableRenameVisitor = new VariableRenameVisitor(newName, symbolTable, originalName, originalLine);
                        variableRenameVisitor.visit(prog);
                    }

                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);

                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    var outFile2 = new PrintWriter(outfilename + ".java");
                    outFile2.write(astPrinter.getString());
                    outFile2.flush();
                    outFile2.close();


                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }
}
