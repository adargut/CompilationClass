import ast.*;
import codegen.vtable.VTables;
import semanticanalysis.SemanticException;
import symboltable.SymbolTable;
import visitor.*;

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
                Parser p = new Parser(new Lexer(new FileReader(filename)));
                prog = (Program)(p.parse().value);

            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            // Create the full directory tree to outfilename
            File file = new File(outfilename);
            file.getParentFile().mkdirs();

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
                    try {
                        BuildClassHierarchyVisitor buildClassHierarchyVisitor = new BuildClassHierarchyVisitor();
                        buildClassHierarchyVisitor.visit(prog);
                        SymbolTable symbolTable = buildClassHierarchyVisitor.getSymbolTable();

                        ValidateTypeVisitor validateTypeVisitor = new ValidateTypeVisitor(symbolTable);
                        validateTypeVisitor.visit(prog);

                        ValidateInitVisitor validateInitVisitor = new ValidateInitVisitor(symbolTable);
                        validateInitVisitor.visit(prog);

                        outFile.write("OK\n");
                    }

                    catch (SemanticException e){
                        System.out.println(String.format("ERROR: %s, Message: %s",
                                e.getErrorCode().name(),
                                e.getMessage()));
                        outFile.write("ERROR\n");
                    }

                } else if (action.equals("compile")) {
                    BuildClassHierarchyVisitor buildClassHierarchyVisitor = new BuildClassHierarchyVisitor();
                    buildClassHierarchyVisitor.visit(prog);
                    SymbolTable symbolTable = buildClassHierarchyVisitor.getSymbolTable();

                    VTables vTables = VTables.createVTables(symbolTable);
                    LLVMGeneratorVisitor llvmGeneratorVisitor = new LLVMGeneratorVisitor(vTables, symbolTable);
                    llvmGeneratorVisitor.visit(prog);
                    outFile.write(llvmGeneratorVisitor.getString());

                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
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
