import java.io.File;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;



public class VMT {
public static class Lex {
    private Scanner cmds;
    private String currentCmd;
    public static final int ARITHMETIC = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    public static final int LABEL = 3;
    public static final int GOTO = 4;
    public static final int IF = 5;
    public static final int FUNCTION = 6;
    public static final int RETURN = 7;
    public static final int CALL = 8;
    public static final ArrayList<String> arithmeticCmds = new ArrayList<String>();
    private int argType;
    private String argument1;
    private int argument2;

    static {
        arithmeticCmds.add("add");
        arithmeticCmds.add("sub");
        arithmeticCmds.add("neg");
        arithmeticCmds.add("eq");
        arithmeticCmds.add("gt");
        arithmeticCmds.add("lt");
        arithmeticCmds.add("and");
        arithmeticCmds.add("or");
        arithmeticCmds.add("not");
    }

    /**
     * Opens the input file and gets ready to parse it.
     * @param fileIn The input file to be parsed.
     */
    public Lex(File fileIn) {
        argType = -1;
        argument1 = "";
        argument2 = -1;

        try {
            cmds = new Scanner(fileIn);
            String preprocessed = "";
            String line = "";

            while (cmds.hasNext()) {
                line = noComments(cmds.nextLine()).trim();
                if (line.length() > 0) {
                    preprocessed += line + "\n";
                }
            }

            cmds = new Scanner(preprocessed.trim());
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
    }

    /**
     * Checks if there are more commands to read.
     * @return True if there are more commands, false otherwise.
     */
    public boolean hasMoreCommands() {
        return cmds.hasNextLine();
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * Should only be called when hasMoreCommands() returns true.
     */
    public void advance() {
        currentCmd = cmds.nextLine();
        argument1 = ""; // Initialize arg1
        argument2 = -1; // Initialize arg2

        String[] segs = currentCmd.split(" ");

        if (segs.length > 3) {
            throw new IllegalArgumentException("Too many arguments!");
        }

        if (arithmeticCmds.contains(segs[0])) {
            argType = ARITHMETIC;
            argument1 = segs[0];
        } else if (segs[0].equals("return")) {
            argType = RETURN;
            argument1 = segs[0];
        } else {
            argument1 = segs[1];

            switch (segs[0]) {
                case "push":
                    argType = PUSH;
                    break;
                case "pop":
                    argType = POP;
                    break;
                case "label":
                    argType = LABEL;
                    break;
                case "if":
                    argType = IF;
                    break;
                case "goto":
                    argType = GOTO;
                    break;
                case "function":
                    argType = FUNCTION;
                    break;
                case "call":
                    argType = CALL;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Command Type!");
            }

            if (argType == PUSH || argType == POP || argType == FUNCTION || argType == CALL) {
                try {
                    argument2 = Integer.parseInt(segs[2]);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Argument2 is not an integer!");
                }
            }
        }
    }

    /**
     * Returns the type of the current command.
     * ARITHMETIC is returned for all ARITHMETIC type commands.
     * @return The type of the current command.
     */
    public int commandType() {
        if (argType != -1) {
            return argType;
        } else {
            throw new IllegalStateException("No command!");
        }
    }

    /**
     * Returns the first argument of the current command.
     * When the command is ARITHMETIC, return the command itself.
     * Should not be called for RETURN type commands.
     * @return The first argument of the current command.
     */
    public String arg1() {
        if (commandType() != RETURN) {
            return argument1;
        } else {
            throw new IllegalStateException("Cannot get arg1 from a RETURN type command!");
        }
    }

    /**
     * Returns the second argument of the current command.
     * Should be called only for PUSH, POP, FUNCTION, or CALL commands.
     * @return The second argument of the current command.
     */
    public int arg2() {
        if (commandType() == PUSH || commandType() == POP || commandType() == FUNCTION || commandType() == CALL) {
            return argument2;
        } else {
            throw new IllegalStateException("Cannot get arg2!");
        }
    }

    /**
     * Removes comments (text after "//") from a string.
     * @param strIn The input string.
     * @return The string without comments.
     */
    public static String noComments(String strIn) {
        int position = strIn.indexOf("//");
        if (position != -1) {
            strIn = strIn.substring(0, position);
        }
        return strIn;
    }

    /**
     * Removes spaces from a string.
     * @param strIn The input string.
     * @return The string without spaces.
     */
    public static String noSpaces(String strIn) {
        String result = "";
        if (strIn.length() != 0) {
            String[] segs = strIn.split(" ");
            for (String s : segs) {
                result += s;
            }
        }
        return result;
    }

    /**
     * Gets the extension from a filename.
     * @param fileName The filename.
     * @return The file extension.
     */
    public static String getExt(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(index);
        } else {
            return "";
        }
    }
}
public static class Writer {

    private int arthJumpFlag;
    private PrintWriter outPrinter;

    /**
     * Open an output file and be ready to write content.
     * @param fileOut can be a directory!
     */
    public Writer(File fileOut) {
        try {
            outPrinter = new PrintWriter(fileOut);
            arthJumpFlag = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inform the CodeWriter that the translation of a new VM file is started.
     * @param fileOut the file to be processed
     */
    public void setFileName(File fileOut) {
        // Implementation can be added if needed
    }

    /**
     * Write the assembly code that is the translation of the given arithmetic command.
     * @param command the arithmetic command to translate
     */
    public void writeArithmetic(String command) {
        switch (command) {
            case "add":
                outPrinter.print(arithmeticTemplate1() + "M=D+M\n");
                break;
            case "sub":
                outPrinter.print(arithmeticTemplate1() + "M=M-D\n");
                break;
            case "and":
                outPrinter.print(arithmeticTemplate1() + "M=D&M\n");
                break;
            case "or":
                outPrinter.print(arithmeticTemplate1() + "M=D|M\n");
                break;
            case "gt":
                outPrinter.print(arithmeticTemplate2("JLE")); // not <=
                arthJumpFlag++;
                break;
            case "lt":
                outPrinter.print(arithmeticTemplate2("JGE")); // not >=
                arthJumpFlag++;
                break;
            case "eq":
                outPrinter.print(arithmeticTemplate2("JNE")); // not <>
                arthJumpFlag++;
                break;
            case "not":
                outPrinter.print("@SP\nA=M-1\nM=!M\n");
                break;
            case "neg":
                outPrinter.print("D=0\n@SP\nA=M-1\nM=D-M\n");
                break;
            default:
                throw new IllegalArgumentException("Call writeArithmetic() for a non-arithmetic command");
        }
    }

    /**
     * Write the assembly code that is the translation of the given command,
     * where the command is either PUSH or POP.
     * @param command PUSH or POP
     * @param segment the memory segment
     * @param index the index in the segment
     */
    public void writePushPop(int command, String segment, int index) {
        if (command == Lex.PUSH) {
            switch (segment) {
                case "constant":
                    outPrinter.print("@" + index + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
                    break;
                case "local":
                    outPrinter.print(pushTemplate1("LCL", index, false));
                    break;
                case "argument":
                    outPrinter.print(pushTemplate1("ARG", index, false));
                    break;
                case "this":
                    outPrinter.print(pushTemplate1("THIS", index, false));
                    break;
                case "that":
                    outPrinter.print(pushTemplate1("THAT", index, false));
                    break;
                case "temp":
                    outPrinter.print(pushTemplate1("R5", index + 5, false));
                    break;
                case "pointer":
                    if (index == 0) {
                        outPrinter.print(pushTemplate1("THIS", index, true));
                    } else if (index == 1) {
                        outPrinter.print(pushTemplate1("THAT", index, true));
                    }
                    break;
                case "static":
                    outPrinter.print(pushTemplate1(String.valueOf(16 + index), index, true));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid segment for PUSH command");
            }
        } else if (command == Lex.POP) {
            switch (segment) {
                case "local":
                    outPrinter.print(popTemplate1("LCL", index, false));
                    break;
                case "argument":
                    outPrinter.print(popTemplate1("ARG", index, false));
                    break;
                case "this":
                    outPrinter.print(popTemplate1("THIS", index, false));
                    break;
                case "that":
                    outPrinter.print(popTemplate1("THAT", index, false));
                    break;
                case "temp":
                    outPrinter.print(popTemplate1("R5", index + 5, false));
                    break;
                case "pointer":
                    if (index == 0) {
                        outPrinter.print(popTemplate1("THIS", index, true));
                    } else if (index == 1) {
                        outPrinter.print(popTemplate1("THAT", index, true));
                    }
                    break;
                case "static":
                    outPrinter.print(popTemplate1(String.valueOf(16 + index), index, true));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid segment for POP command");
            }
        } else {
            throw new IllegalArgumentException("Call writePushPop() for a non-pushpop command");
        }
    }

    /**
     * Close the output file.
     */
    public void close() {
        outPrinter.close();
    }

    /**
     * Template for add, sub, and, or.
     * @return the assembly template
     */
    private String arithmeticTemplate1() {
        return "@SP\n" +
               "AM=M-1\n" +
               "D=M\n" +
               "A=A-1\n";
    }

    /**
     * Template for gt, lt, eq.
     * @param type JLE, JGT, JEQ
     * @return the assembly template
     */
    private String arithmeticTemplate2(String type) {
        return "@SP\n" +
               "AM=M-1\n" +
               "D=M\n" +
               "A=A-1\n" +
               "D=M-D\n" +
               "@FALSE" + arthJumpFlag + "\n" +
               "D;" + type + "\n" +
               "@SP\n" +
               "A=M-1\n" +
               "M=-1\n" +
               "@CONTINUE" + arthJumpFlag + "\n" +
               "0;JMP\n" +
               "(FALSE" + arthJumpFlag + ")\n" +
               "@SP\n" +
               "A=M-1\n" +
               "M=0\n" +
               "(CONTINUE" + arthJumpFlag + ")\n";
    }

    /**
     * Template for push local, this, that, argument, temp, pointer, static.
     * @param segment the memory segment
     * @param index the index in the segment
     * @param isDirect whether this command is direct addressing
     * @return the assembly template
     */
    private String pushTemplate1(String segment, int index, boolean isDirect) {
        // When it is a pointer, just read the data stored in THIS or THAT
        // When it is static, just read the data stored in that address
        String noPointerCode = (isDirect) ? "" : "@" + index + "\nA=D+A\nD=M\n";

        return "@" + segment + "\n" +
               "D=M\n" +
               noPointerCode +
               "@SP\n" +
               "A=M\n" +
               "M=D\n" +
               "@SP\n" +
               "M=M+1\n";
    }

    /**
     * Template for pop local, this, that, argument, temp, pointer, static.
     * @param segment the memory segment
     * @param index the index in the segment
     * @param isDirect whether this command is direct addressing
     * @return the assembly template
     */
    private String popTemplate1(String segment, int index, boolean isDirect) {
        // When it is a pointer, R13 will store the address of THIS or THAT
        // When it is static, R13 will store the index address
        String noPointerCode = (isDirect) ? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";

        return "@" + segment + "\n" +
               noPointerCode +
               "@R13\n" +
               "M=D\n" +
               "@SP\n" +
               "AM=M-1\n" +
               "D=M\n" +
               "@R13\n" +
               "A=M\n" +
               "M=D\n";
    }
}
    /**
     * Returns all the .vm files in a directory.
     * 
     * @param dir The directory to scan for .vm files.
     * @return A list of .vm files found in the directory.
     */
    public static ArrayList<File> getVMFiles(File dir) {
        File[] files = dir.listFiles();
        ArrayList<File> result = new ArrayList<>();

        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".vm")) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java VMtranslator [filename|directory]");
            return;
        }

        File fileIn = new File(args[0]);
        String fileOutPath = "";
        File fileOut;
        Writer writer;
        ArrayList<File> vmFiles = new ArrayList<>();

        if (fileIn.isFile()) {
            // If it's a single file, check whether it's a .vm file
            String path = fileIn.getAbsolutePath();
            if (!Lex.getExt(path).equals(".vm")) {
                throw new IllegalArgumentException(".vm file is required!");
            }
            vmFiles.add(fileIn);
            fileOutPath = path.substring(0, path.lastIndexOf(".")) + ".asm";
        } else if (fileIn.isDirectory()) {
            // If it's a directory, get all .vm files within
            vmFiles = getVMFiles(fileIn);
            if (vmFiles.isEmpty()) {
                throw new IllegalArgumentException("No .vm file in this directory");
            }
            fileOutPath = fileIn.getAbsolutePath() + "/" + fileIn.getName() + ".asm";
        }

        fileOut = new File(fileOutPath);
        writer = new Writer(fileOut);

        for (File f : vmFiles) {
            Lex parser = new Lex(f);

            // Start parsing
            while (parser.hasMoreCommands()) {
                parser.advance();
                int type = parser.commandType();

                if (type == Lex.ARITHMETIC) {
                    writer.writeArithmetic(parser.arg1());
                } else if (type == Lex.POP || type == Lex.PUSH) {
                    writer.writePushPop(type, parser.arg1(), parser.arg2());
                }
            }
        }

        // Save file
        writer.close();
        System.out.println("File created: " + fileOutPath);
    }
}