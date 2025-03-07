import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.PrintWriter;
public class JackCompiler {

    /**
     * Return all the .jack files in a directory
     * @param dir
     * @return
     */
    public static ArrayList<File> getJackFiles(File dir){

        File[] files = dir.listFiles();

        ArrayList<File> result = new ArrayList<File>();

        if (files == null) return result;

        for (File f:files){

            if (f.getName().endsWith(".jack")){

                result.add(f);

            }

        }

        return result;

    }

    public static void main(String[] args) {

        if (args.length != 1){

            System.out.println("Usage:java JackCompiler [filename|directory]");

        }else {

            String fileInName = args[0];
            File fileIn = new File(fileInName);

            String fileOutPath = "";

            File fileOut;

            ArrayList<File> jackFiles = new ArrayList<File>();

            if (fileIn.isFile()) {

                //if it is a single file, see whether it is a vm file
                String path = fileIn.getAbsolutePath();

                if (!path.endsWith(".jack")) {

                    throw new IllegalArgumentException(".jack file is required!");

                }

                jackFiles.add(fileIn);

            } else if (fileIn.isDirectory()) {

                //if it is a directory get all jack files under this directory
                jackFiles = getJackFiles(fileIn);

                //if no vn file in this directory
                if (jackFiles.size() == 0) {

                    throw new IllegalArgumentException("No jack file in this directory");

                }

            }

            for (File f: jackFiles) {

                fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".vm";
                fileOut = new File(fileOutPath);

                CompilationEngine compilationEngine = new CompilationEngine(f,fileOut);
                compilationEngine.compileClass();

                System.out.println("File created : " + fileOutPath);
            }

        }

    }

}
class JackTokenizer {

    //constant for type
    public static enum TYPE {KEYWORD,
                            SYMBOL,
                            IDENTIFIER,
                            INT_CONST,
                            STRING_CONST,
                            NONE};
    //constant for keyword
    public static enum KEYWORD {CLASS,
                                METHOD,FUNCTION,CONSTRUCTOR,
                                INT,BOOLEAN,CHAR,VOID,
                                VAR,STATIC,FIELD,
                                LET,DO,IF,ELSE,WHILE,
                                RETURN,
                                TRUE,FALSE,NULL,
                                THIS};

    private String currentToken;
    private TYPE currentTokenType;
    private int pointer;
    private ArrayList<String> tokens;

    private static Pattern tokenPatterns;
    private static String keyWordReg;
    private static String symbolReg;
    private static String intReg;
    private static String strReg;
    private static String idReg;

    private static HashMap<String,KEYWORD> keyWordMap = new HashMap<String, KEYWORD>();
    private static HashSet<Character> opSet = new HashSet<Character>();

    static {

        keyWordMap.put("class",KEYWORD.CLASS);keyWordMap.put("constructor",KEYWORD.CONSTRUCTOR);keyWordMap.put("function",KEYWORD.FUNCTION);
        keyWordMap.put("method",KEYWORD.METHOD);keyWordMap.put("field",KEYWORD.FIELD);keyWordMap.put("static",KEYWORD.STATIC);
        keyWordMap.put("var",KEYWORD.VAR);keyWordMap.put("int",KEYWORD.INT);keyWordMap.put("char",KEYWORD.CHAR);
        keyWordMap.put("boolean",KEYWORD.BOOLEAN);keyWordMap.put("void",KEYWORD.VOID);keyWordMap.put("true",KEYWORD.TRUE);
        keyWordMap.put("false",KEYWORD.FALSE);keyWordMap.put("null",KEYWORD.NULL);keyWordMap.put("this",KEYWORD.THIS);
        keyWordMap.put("let",KEYWORD.LET);keyWordMap.put("do",KEYWORD.DO);keyWordMap.put("if",KEYWORD.IF);
        keyWordMap.put("else",KEYWORD.ELSE);keyWordMap.put("while",KEYWORD.WHILE);keyWordMap.put("return",KEYWORD.RETURN);

        opSet.add('+');opSet.add('-');opSet.add('*');opSet.add('/');opSet.add('&');opSet.add('|');
        opSet.add('<');opSet.add('>');opSet.add('=');
    }



    /**
     * Opens the input file/stream and gets ready to tokenize it
     * @param inFile
     */
    public JackTokenizer(File inFile) {

        try {

            Scanner scanner = new Scanner(inFile);
            String preprocessed = "";
            String line = "";

            while(scanner.hasNext()){

                line = noComments(scanner.nextLine()).trim();

                if (line.length() > 0) {
                    preprocessed += line + "\n";
                }
            }

            preprocessed = noBlockComments(preprocessed).trim();

            //init all regex
            initRegs();

            Matcher m = tokenPatterns.matcher(preprocessed);
            tokens = new ArrayList<String>();
            pointer = 0;

            while (m.find()){

                tokens.add(m.group());

            }

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        }

        currentToken = "";
        currentTokenType = TYPE.NONE;

    }

    /**
     * inti regex we need in tokenizer
     */
    private void initRegs(){

        keyWordReg = "";

        for (String seg: keyWordMap.keySet()){

            keyWordReg += seg + "|";

        }

        symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
        intReg = "[0-9]+";
        strReg = "\"[^\"\n]*\"";
        idReg = "[a-zA-Z_]\\w*";

        tokenPatterns = Pattern.compile(idReg + "|" + keyWordReg + symbolReg + "|" + intReg + "|" + strReg);
    }


    /**
     * Do we have more tokens in the input?
     * @return
     */
    public boolean hasMoreTokens() {
        return pointer < tokens.size();
    }

    /**
     * Gets the next token from the input and makes it the current token
     * This method should only be called if hasMoreTokens() is true
     * Initially there is no current token
     */
    public void advance(){

        if (hasMoreTokens()) {
            currentToken = tokens.get(pointer);
            pointer++;
        }else {
            throw new IllegalStateException("No more tokens");
        }

        //System.out.println(currentToken);

        if (currentToken.matches(keyWordReg)){
            currentTokenType = TYPE.KEYWORD;
        }else if (currentToken.matches(symbolReg)){
            currentTokenType = TYPE.SYMBOL;
        }else if (currentToken.matches(intReg)){
            currentTokenType = TYPE.INT_CONST;
        }else if (currentToken.matches(strReg)){
            currentTokenType = TYPE.STRING_CONST;
        }else if (currentToken.matches(idReg)){
            currentTokenType = TYPE.IDENTIFIER;
        }else {

            throw new IllegalArgumentException("Unknown token:" + currentToken);
        }

    }

    public String getCurrentToken() {
        return currentToken;
    }

    /**
     * Returns the type of the current token
     * @return
     */
    public TYPE tokenType(){

        return currentTokenType;
    }

    /**
     * Returns the keyword which is the current token
     * Should be called only when tokeyType() is KEYWORD
     * @return
     */
    public KEYWORD keyWord(){

        if (currentTokenType == TYPE.KEYWORD){

            return keyWordMap.get(currentToken);

        }else {

            throw new IllegalStateException("Current token is not a keyword!");
        }
    }

    /**
     * Returns the character which is the current token
     * should be called only when tokenType() is SYMBOL
     * @return if current token is not a symbol return \0
     */
    public char symbol(){

        if (currentTokenType == TYPE.SYMBOL){

            return currentToken.charAt(0);

        }else{
            throw new IllegalStateException("Current token is not a symbol!");
        }
    }

    /**
     * Return the identifier which is the current token
     * should be called only when tokenType() is IDENTIFIER
     * @return
     */
    public String identifier(){

        if (currentTokenType == TYPE.IDENTIFIER){

            return currentToken;

        }else {
            throw new IllegalStateException("Current token is not an identifier! current type:" + currentTokenType);
        }
    }

    /**
     * Returns the integer value of the current token
     * should be called only when tokenType() is INT_CONST
     * @return
     */
    public int intVal(){

        if(currentTokenType == TYPE.INT_CONST){

            return Integer.parseInt(currentToken);
        }else {
            throw new IllegalStateException("Current token is not an integer constant!");
        }
    }

    /**
     * Returns the string value of the current token
     * without the double quotes
     * should be called only when tokenType() is STRING_CONST
     * @return
     */
    public String stringVal(){

        if (currentTokenType == TYPE.STRING_CONST){

            return currentToken.substring(1, currentToken.length() - 1);

        }else {
            throw new IllegalStateException("Current token is not a string constant!");
        }
    }

    /**
     * move pointer back
     */
    public void pointerBack(){

        if (pointer > 0) {
            pointer--;
            currentToken = tokens.get(pointer);
        }

    }

    /**
     * return if current symbol is a op
     * @return
     */
    public boolean isOp(){
        return opSet.contains(symbol());
    }

    /**
     * Delete comments(String after "//") from a String
     * @param strIn
     * @return
     */
    public static String noComments(String strIn){

        int position = strIn.indexOf("//");

        if (position != -1){

            strIn = strIn.substring(0, position);

        }

        return strIn;
    }

    /**
     * Delete spaces from a String
     * @param strIn
     * @return
     */
    public static String noSpaces(String strIn){
        String result = "";

        if (strIn.length() != 0){

            String[] segs = strIn.split(" ");

            for (String s: segs){
                result += s;
            }
        }

        return result;
    }

    /**
     * delete block comment
     * @param strIn
     * @return
     */
    public static String noBlockComments(String strIn){

        int startIndex = strIn.indexOf("/*");

        if (startIndex == -1) return strIn;

        String result = strIn;

        int endIndex = strIn.indexOf("*/");

        while(startIndex != -1){

            if (endIndex == -1){

                return strIn.substring(0,startIndex - 1);

            }
            result = result.substring(0,startIndex) + result.substring(endIndex + 2);

            startIndex = result.indexOf("/*");
            endIndex = result.indexOf("*/");
        }

        return result;
    }
}
class CompilationEngine {

    private VMWriter vmWriter;
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private String currentClass;
    private String currentSubroutine;

    private int labelIndex;
    /**
     * Creates a new compilation engine with the given input and output.
     * The next routine called must be compileClass()
     * @param inFile
     * @param outFile
     */
    public CompilationEngine(File inFile, File outFile) {

        tokenizer = new JackTokenizer(inFile);
        vmWriter = new VMWriter(outFile);
        symbolTable = new SymbolTable();

        labelIndex = 0;

    }

    /**
     * return current function name, className.subroutineName
     * @return
     */
    private String currentFunction(){

        if (currentClass.length() != 0 && currentSubroutine.length() !=0){

            return currentClass + "." + currentSubroutine;

        }

        return "";
    }

    /**
     * Compiles a type
     * @return type
     */
    private String compileType(){

        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && (tokenizer.keyWord() == JackTokenizer.KEYWORD.INT || tokenizer.keyWord() == JackTokenizer.KEYWORD.CHAR || tokenizer.keyWord() == JackTokenizer.KEYWORD.BOOLEAN)){
            return tokenizer.getCurrentToken();
        }

        if (tokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER){
            return tokenizer.identifier();
        }

        error("in|char|boolean|className");

        return "";
    }

    /**
     * Complies a complete class
     * class: 'class' className '{' classVarDec* subroutineDec* '}'
     */
    public void compileClass(){

        //'class'
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || tokenizer.keyWord() != JackTokenizer.KEYWORD.CLASS){
            System.out.println(tokenizer.getCurrentToken());
            error("class");
        }

        //className
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("className");
        }

        //classname does not need to be put in symbol table
        currentClass = tokenizer.identifier();

        //'{'
        requireSymbol('{');

        //classVarDec* subroutineDec*
        compileClassVarDec();
        compileSubroutine();

        //'}'
        requireSymbol('}');

        if (tokenizer.hasMoreTokens()){
            throw new IllegalStateException("Unexpected tokens");
        }

        //save file
        vmWriter.close();

    }

    /**
     * Compiles a static declaration or a field declaration
     * classVarDec ('static'|'field') type varName (','varNAme)* ';'
     */
    private void compileClassVarDec(){

        //first determine whether there is a classVarDec, nextToken is } or start subroutineDec
        tokenizer.advance();

        //next is a '}'
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //next is start subroutineDec or classVarDec, both start with keyword
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD){
            error("Keywords");
        }

        //next is subroutineDec
        if (tokenizer.keyWord() == JackTokenizer.KEYWORD.CONSTRUCTOR || tokenizer.keyWord() == JackTokenizer.KEYWORD.FUNCTION || tokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD){
            tokenizer.pointerBack();
            return;
        }

        //classVarDec exists
        if (tokenizer.keyWord() != JackTokenizer.KEYWORD.STATIC && tokenizer.keyWord() != JackTokenizer.KEYWORD.FIELD){
            error("static or field");
        }

        Symbol.KIND kind = null;
        String type = "";
        String name = "";

        switch (tokenizer.keyWord()){
            case STATIC:kind = Symbol.KIND.STATIC;break;
            case FIELD:kind = Symbol.KIND.FIELD;break;
        }

        //type
        type = compileType();

        //at least one varName
        boolean varNamesDone = false;

        do {

            //varName
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            name = tokenizer.identifier();

            symbolTable.define(name,type,kind);

            //',' or ';'
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ';'){
                break;
            }


        }while(true);

        compileClassVarDec();
    }

    /**
     * Compiles a complete method function or constructor
     */
    private void compileSubroutine(){

        //determine whether there is a subroutine, next can be a '}'
        tokenizer.advance();

        //next is a '}'
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //start of a subroutine
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || (tokenizer.keyWord() != JackTokenizer.KEYWORD.CONSTRUCTOR && tokenizer.keyWord() != JackTokenizer.KEYWORD.FUNCTION && tokenizer.keyWord() != JackTokenizer.KEYWORD.METHOD)){
            error("constructor|function|method");
        }

        JackTokenizer.KEYWORD keyword = tokenizer.keyWord();

        symbolTable.startSubroutine();

        //for method this is the first argument
        if (tokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD){
            symbolTable.define("this",currentClass, Symbol.KIND.ARG);
        }

        String type = "";

        //'void' or type
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.VOID){
            type = "void";
        }else {
            tokenizer.pointerBack();
            type = compileType();
        }

        //subroutineName which is a identifier
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("subroutineName");
        }

        currentSubroutine = tokenizer.identifier();

        //'('
        requireSymbol('(');

        //parameterList
        compileParameterList();

        //')'
        requireSymbol(')');

        //subroutineBody
        compileSubroutineBody(keyword);

        compileSubroutine();

    }

    /**
     * Compiles the body of a subroutine
     * '{'  varDec* statements '}'
     */
    private void compileSubroutineBody(JackTokenizer.KEYWORD keyword){
        //'{'
        requireSymbol('{');
        //varDec*
        compileVarDec();
        //write VM function declaration
        wrtieFunctionDec(keyword);
        //statements
        compileStatement();
        //'}'
        requireSymbol('}');
    }

    /**
     * write function declaration, load pointer when keyword is METHOD or CONSTRUCTOR
     */
    private void wrtieFunctionDec(JackTokenizer.KEYWORD keyword){

        vmWriter.writeFunction(currentFunction(),symbolTable.varCount(Symbol.KIND.VAR));

        //METHOD and CONSTRUCTOR need to load this pointer
        if (keyword == JackTokenizer.KEYWORD.METHOD){
            //A Jack method with k arguments is compiled into a VM function that operates on k + 1 arguments.
            // The first argument (argument number 0) always refers to the this object.
            vmWriter.writePush(VMWriter.SEGMENT.ARG, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);

        }else if (keyword == JackTokenizer.KEYWORD.CONSTRUCTOR){
            //A Jack function or constructor with k arguments is compiled into a VM function that operates on k arguments.
            vmWriter.writePush(VMWriter.SEGMENT.CONST,symbolTable.varCount(Symbol.KIND.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,0);
        }
    }

    /**
     * Compiles a single statement
     */
    private void compileStatement(){

        //determine whether there is a statement next can be a '}'
        tokenizer.advance();

        //next is a '}'
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //next is 'let'|'if'|'while'|'do'|'return'
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD){
            error("keyword");
        }else {
            switch (tokenizer.keyWord()){
                case LET:compileLet();break;
                case IF:compileIf();break;
                case WHILE:compilesWhile();break;
                case DO:compileDo();break;
                case RETURN:compileReturn();break;
                default:error("'let'|'if'|'while'|'do'|'return'");
            }
        }

        compileStatement();
    }

    /**
     * Compiles a (possibly empty) parameter list
     * not including the enclosing "()"
     * ((type varName)(',' type varName)*)?
     */
    private void compileParameterList(){

        //check if there is parameterList, if next token is ')' than go back
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
            return;
        }

        String type = "";

        //there is parameter, at least one varName
        tokenizer.pointerBack();
        do {
            //type
            type = compileType();

            //varName
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            symbolTable.define(tokenizer.identifier(),type, Symbol.KIND.ARG);

            //',' or ')'
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')){
                error("',' or ')'");
            }

            if (tokenizer.symbol() == ')'){
                tokenizer.pointerBack();
                break;
            }

        }while(true);

    }

    /**
     * Compiles a var declaration
     * 'var' type varName (',' varName)*;
     */
    private void compileVarDec(){

        //determine if there is a varDec

        tokenizer.advance();
        //no 'var' go back
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || tokenizer.keyWord() != JackTokenizer.KEYWORD.VAR){
            tokenizer.pointerBack();
            return;
        }

        //type
        String type = compileType();

        //at least one varName
        boolean varNamesDone = false;

        do {

            //varName
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            symbolTable.define(tokenizer.identifier(),type, Symbol.KIND.VAR);

            //',' or ';'
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ';'){
                break;
            }


        }while(true);

        compileVarDec();

    }

    /**
     * Compiles a do statement
     * 'do' subroutineCall ';'
     */
    private void compileDo(){

        //subroutineCall
        compileSubroutineCall();
        //';'
        requireSymbol(';');
        //pop return value
        vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
    }

    /**
     * Compiles a let statement
     * 'let' varName ('[' ']')? '=' expression ';'
     */
    private void compileLet(){

        //varName
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("varName");
        }

        String varName = tokenizer.identifier();

        //'[' or '='
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')){
            error("'['|'='");
        }

        boolean expExist = false;

        //'[' expression ']' ,need to deal with array [base+offset]
        if (tokenizer.symbol() == '['){

            expExist = true;

            //push array variable,base address into stack
            vmWriter.writePush(getSeg(symbolTable.kindOf(varName)),symbolTable.indexOf(varName));

            //calc offset
            compileExpression();

            //']'
            requireSymbol(']');

            //base+offset
            vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
        }

        if (expExist) tokenizer.advance();

        //expression
        compileExpression();

        //';'
        requireSymbol(';');

        if (expExist){
            //*(base+offset) = expression
            //pop expression value to temp
            vmWriter.writePop(VMWriter.SEGMENT.TEMP,0);
            //pop base+index into 'that'
            vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
            //pop expression value into *(base+index)
            vmWriter.writePush(VMWriter.SEGMENT.TEMP,0);
            vmWriter.writePop(VMWriter.SEGMENT.THAT,0);
        }else {
            //pop expression value directly
            vmWriter.writePop(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));

        }
    }

    /**
     * return corresponding seg for input kind
     * @param kind
     * @return
     */
    private VMWriter.SEGMENT getSeg(Symbol.KIND kind){

        switch (kind){
            case FIELD:return VMWriter.SEGMENT.THIS;
            case STATIC:return VMWriter.SEGMENT.STATIC;
            case VAR:return VMWriter.SEGMENT.LOCAL;
            case ARG:return VMWriter.SEGMENT.ARG;
            default:return VMWriter.SEGMENT.NONE;
        }

    }

    /**
     * Compiles a while statement
     * 'while' '(' expression ')' '{' statements '}'
     */
    private void compilesWhile(){

        String continueLabel = newLabel();
        String topLabel = newLabel();

        //top label for while loop
        vmWriter.writeLabel(topLabel);

        //'('
        requireSymbol('(');
        //expression while condition: true or false
        compileExpression();
        //')'
        requireSymbol(')');
        //if ~(condition) go to continue label
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(continueLabel);
        //'{'
        requireSymbol('{');
        //statements
        compileStatement();
        //'}'
        requireSymbol('}');
        //if (condition) go to top label
        vmWriter.writeGoto(topLabel);
        //or continue
        vmWriter.writeLabel(continueLabel);
    }

    private String newLabel(){
        return "LABEL_" + (labelIndex++);
    }

    /**
     * Compiles a return statement
     * ‘return’ expression? ';'
     */
    private void compileReturn(){

        //check if there is any expression
        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ';'){
            //no expression push 0 to stack
            vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
        }else {
            //expression exist
            tokenizer.pointerBack();
            //expression
            compileExpression();
            //';'
            requireSymbol(';');
        }

        vmWriter.writeReturn();

    }

    /**
     * Compiles an if statement
     * possibly with a trailing else clause
     * 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
     */
    private void compileIf(){

        String elseLabel = newLabel();
        String endLabel = newLabel();

        //'('
        requireSymbol('(');
        //expression
        compileExpression();
        //')'
        requireSymbol(')');
        //if ~(condition) go to else label
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(elseLabel);
        //'{'
        requireSymbol('{');
        //statements
        compileStatement();
        //'}'
        requireSymbol('}');
        //if condition after statement finishing, go to end label
        vmWriter.writeGoto(endLabel);

        vmWriter.writeLabel(elseLabel);
        //check if there is 'else'
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.ELSE){
            //'{'
            requireSymbol('{');
            //statements
            compileStatement();
            //'}'
            requireSymbol('}');
        }else {
            tokenizer.pointerBack();
        }

        vmWriter.writeLabel(endLabel);

    }

    /**
     * Compiles a term.
     * This routine is faced with a slight difficulty when trying to decide between some of the alternative parsing rules.
     * Specifically, if the current token is an identifier
     *      the routine must distinguish between a variable, an array entry and a subroutine call
     * A single look-ahead token, which may be one of "[" "(" "." suffices to distinguish between the three possibilities
     * Any other token is not part of this term and should not be advanced over
     *
     * integerConstant|stringConstant|keywordConstant|varName|varName '[' expression ']'|subroutineCall|
     * '(' expression ')'|unaryOp term
     */
    private void compileTerm(){

        tokenizer.advance();
        //check if it is an identifier
        if (tokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER){
            //varName|varName '[' expression ']'|subroutineCall
            String tempId = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '['){
                //this is an array entry

                //push array variable,base address into stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(tempId)),symbolTable.indexOf(tempId));

                //expression
                compileExpression();
                //']'
                requireSymbol(']');

                //base+offset
                vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);

                //pop into 'that' pointer
                vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
                //push *(base+index) onto stack
                vmWriter.writePush(VMWriter.SEGMENT.THAT,0);

            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')){
                //this is a subroutineCall
                tokenizer.pointerBack();tokenizer.pointerBack();
                compileSubroutineCall();
            }else {
                //this is varName
                tokenizer.pointerBack();
                //push variable directly onto stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(tempId)), symbolTable.indexOf(tempId));
            }

        }else{
            //integerConstant|stringConstant|keywordConstant|'(' expression ')'|unaryOp term
            if (tokenizer.tokenType() == JackTokenizer.TYPE.INT_CONST){
                //integerConstant just push its value onto stack
                vmWriter.writePush(VMWriter.SEGMENT.CONST,tokenizer.intVal());
            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.STRING_CONST){
                //stringConstant new a string and append every char to the new stack
                String str = tokenizer.stringVal();

                vmWriter.writePush(VMWriter.SEGMENT.CONST,str.length());
                vmWriter.writeCall("String.new",1);

                for (int i = 0; i < str.length(); i++){
                    vmWriter.writePush(VMWriter.SEGMENT.CONST,(int)str.charAt(i));
                    vmWriter.writeCall("String.appendChar",2);
                }

            }else if(tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.TRUE){
                //~0 is true
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
                vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);

            }else if(tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.THIS){
                //push this pointer onto stack
                vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);

            }else if(tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && (tokenizer.keyWord() == JackTokenizer.KEYWORD.FALSE || tokenizer.keyWord() == JackTokenizer.KEYWORD.NULL)){
                //0 for false and null
                vmWriter.writePush(VMWriter.SEGMENT.CONST,0);
            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '('){
                //expression
                compileExpression();
                //')'
                requireSymbol(')');
            }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')){

                char s = tokenizer.symbol();

                //term
                compileTerm();

                if (s == '-'){
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
                }else {
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
                }

            }else {
                error("integerConstant|stringConstant|keywordConstant|'(' expression ')'|unaryOp term");
            }
        }

    }

    /**
     * Compiles a subroutine call
     * subroutineName '(' expressionList ')' | (className|varName) '.' subroutineName '(' expressionList ')'
     */
    private void compileSubroutineCall(){

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("identifier");
        }

        String name = tokenizer.identifier();
        int nArgs = 0;

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '('){
            //push this pointer
            vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);
            //'(' expressionList ')'
            //expressionList
            nArgs = compileExpressionList() + 1;
            //')'
            requireSymbol(')');
            //call subroutine
            vmWriter.writeCall(currentClass + '.' + name, nArgs);

        }else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '.'){
            //(className|varName) '.' subroutineName '(' expressionList ')'

            String objName = name;
            //subroutineName
            tokenizer.advance();
           // System.out.println("subroutineName:" + tokenizer.identifier());
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier");
            }

            name = tokenizer.identifier();

            //check for if it is built-in type
            String type = symbolTable.typeOf(objName);

            if (type.equals("int")||type.equals("boolean")||type.equals("char")||type.equals("void")){
                error("no built-in type");
            }else if (type.equals("")){
                name = objName + "." + name;
            }else {
                nArgs = 1;
                //push variable directly onto stack
                vmWriter.writePush(getSeg(symbolTable.kindOf(objName)), symbolTable.indexOf(objName));
                name = symbolTable.typeOf(objName) + "." + name;
            }

            //'('
            requireSymbol('(');
            //expressionList
            nArgs += compileExpressionList();
            //')'
            requireSymbol(')');
            //call subroutine
            vmWriter.writeCall(name,nArgs);
        }else {
            error("'('|'.'");
        }

    }

    /**
     * Compiles an expression
     * term (op term)*
     */
    private void compileExpression(){
        //term
        compileTerm();
        //(op term)*
        do {
            tokenizer.advance();
            //op
            if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.isOp()){

                String opCmd = "";

                switch (tokenizer.symbol()){
                    case '+':opCmd = "add";break;
                    case '-':opCmd = "sub";break;
                    case '*':opCmd = "call Math.multiply 2";break;
                    case '/':opCmd = "call Math.divide 2";break;
                    case '<':opCmd = "lt";break;
                    case '>':opCmd = "gt";break;
                    case '=':opCmd = "eq";break;
                    case '&':opCmd = "and";break;
                    case '|':opCmd = "or";break;
                    default:error("Unknown op!");
                }

                //term
                compileTerm();

                vmWriter.writeCommand(opCmd,"","");

            }else {
                tokenizer.pointerBack();
                break;
            }

        }while (true);

    }

    /**
     * Compiles a (possibly empty) comma-separated list of expressions
     * (expression(','expression)*)?
     * @return nArgs
     */
    private int compileExpressionList(){
        int nArgs = 0;

        tokenizer.advance();
        //determine if there is any expression, if next is ')' then no
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
        }else {
            nArgs = 1;
            tokenizer.pointerBack();
            //expression
            compileExpression();
            //(','expression)*
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ','){
                    //expression
                    compileExpression();
                    nArgs++;
                }else {
                    tokenizer.pointerBack();
                    break;
                }

            }while (true);
        }

        return nArgs;
    }

    /**
     * throw an exception to report errors
     * @param val
     */
    private void error(String val){
        throw new IllegalStateException("Expected token missing : " + val + " Current token:" + tokenizer.getCurrentToken());
    }

    /**
     * require symbol when we know there must be such symbol
     * @param symbol
     */
    private void requireSymbol(char symbol){
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || tokenizer.symbol() != symbol){
            error("'" + symbol + "'");
        }
    }
}
class Symbol {

    public static enum KIND {STATIC, FIELD, ARG, VAR, NONE};

    private String type;
    private KIND kind;
    private int index;

    public Symbol(String type, KIND kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public KIND getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "type='" + type + '\'' +
                ", kind=" + kind +
                ", index=" + index +
                '}';
    }
}
class SymbolTable {

    private HashMap<String,Symbol> classSymbols;//for STATIC, FIELD
    private HashMap<String,Symbol> subroutineSymbols;//for ARG, VAR
    private HashMap<Symbol.KIND,Integer> indices;

    /**
     * creates a new empty symbol table
     * init all indices
     */
    public SymbolTable() {
        classSymbols = new HashMap<String, Symbol>();
        subroutineSymbols = new HashMap<String, Symbol>();

        indices = new HashMap<Symbol.KIND, Integer>();
        indices.put(Symbol.KIND.ARG,0);
        indices.put(Symbol.KIND.FIELD,0);
        indices.put(Symbol.KIND.STATIC,0);
        indices.put(Symbol.KIND.VAR,0);

    }

    /**
     * starts a new subroutine scope
     * resets the subroutine's symbol table
     */
    public void startSubroutine(){
        subroutineSymbols.clear();
        indices.put(Symbol.KIND.VAR,0);
        indices.put(Symbol.KIND.ARG,0);
    }

    /**
     * Defines a new identifier of a given name,type and kind
     * and assigns it a running index, STATIC and FIELD identifiers
     * jave a class scope, while ARG and VAR identifiers have a subroutine scope
     * @param name
     * @param type
     * @param kind
     */
    public void define(String name, String type, Symbol.KIND kind){

        if (kind == Symbol.KIND.ARG || kind == Symbol.KIND.VAR){

            int index = indices.get(kind);
            Symbol symbol = new Symbol(type,kind,index);
            indices.put(kind,index+1);
            subroutineSymbols.put(name,symbol);

        }else if(kind == Symbol.KIND.STATIC || kind == Symbol.KIND.FIELD){

            int index = indices.get(kind);
            Symbol symbol = new Symbol(type,kind,index);
            indices.put(kind,index+1);
            classSymbols.put(name,symbol);

        }

    }

    /**
     * returns the number of variables of the given kind already defined in the current scope
     * @param kind
     * @return
     */
    public int varCount(Symbol.KIND kind){
        return indices.get(kind);
    }

    /**
     * returns the kind of the named identifier in the current scope
     * if the identifier is unknown in the current scope returns NONE
     * @param name
     * @return
     */
    public Symbol.KIND kindOf(String name){

        Symbol symbol = lookUp(name);

        if (symbol != null) return symbol.getKind();

        return Symbol.KIND.NONE;
    }

    /**
     * returns the type of the named identifier in the current scope
     * @param name
     * @return
     */
    public String typeOf(String name){

        Symbol symbol = lookUp(name);

        if (symbol != null) return symbol.getType();

        return "";
    }

    /**
     * returns the index assigned to the named identifier
     * @param name
     * @return
     */
    public int indexOf(String name){

        Symbol symbol = lookUp(name);

        if (symbol != null) return symbol.getIndex();

        return -1;
    }

    /**
     * check if target symbol is exist
     * @param name
     * @return
     */
    private Symbol lookUp(String name){

        if (classSymbols.get(name) != null){
            return classSymbols.get(name);
        }else if (subroutineSymbols.get(name) != null){
            return subroutineSymbols.get(name);
        }else {
            return null;
        }

    }

}
class VMWriter {

    public static enum SEGMENT {CONST,ARG,LOCAL,STATIC,THIS,THAT,POINTER,TEMP,NONE};
    public static enum COMMAND {ADD,SUB,NEG,EQ,GT,LT,AND,OR,NOT};

    private static HashMap<SEGMENT,String> segmentStringHashMap = new HashMap<SEGMENT, String>();
    private static HashMap<COMMAND,String> commandStringHashMap = new HashMap<COMMAND, String>();
    private PrintWriter printWriter;

    static {

        segmentStringHashMap.put(SEGMENT.CONST,"constant");
        segmentStringHashMap.put(SEGMENT.ARG,"argument");
        segmentStringHashMap.put(SEGMENT.LOCAL,"local");
        segmentStringHashMap.put(SEGMENT.STATIC,"static");
        segmentStringHashMap.put(SEGMENT.THIS,"this");
        segmentStringHashMap.put(SEGMENT.THAT,"that");
        segmentStringHashMap.put(SEGMENT.POINTER,"pointer");
        segmentStringHashMap.put(SEGMENT.TEMP,"temp");

        commandStringHashMap.put(COMMAND.ADD,"add");
        commandStringHashMap.put(COMMAND.SUB,"sub");
        commandStringHashMap.put(COMMAND.NEG,"neg");
        commandStringHashMap.put(COMMAND.EQ,"eq");
        commandStringHashMap.put(COMMAND.GT,"gt");
        commandStringHashMap.put(COMMAND.LT,"lt");
        commandStringHashMap.put(COMMAND.AND,"and");
        commandStringHashMap.put(COMMAND.OR,"or");
        commandStringHashMap.put(COMMAND.NOT,"not");
    }

    /**
     * creates a new file and prepares it for writing
     * @param fOut
     */
    public VMWriter(File fOut) {

        try {
            printWriter = new PrintWriter(fOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * writes a VM push command
     * @param segment
     * @param index
     */
    public void writePush(SEGMENT segment, int index){
        writeCommand("push",segmentStringHashMap.get(segment),String.valueOf(index));
    }

    /**
     * writes a VM pop command
     * @param segment
     * @param index
     */
    public void writePop(SEGMENT segment, int index){
        writeCommand("pop",segmentStringHashMap.get(segment),String.valueOf(index));
    }

    /**
     * writes a VM arithmetic command
     * @param command
     */
    public void writeArithmetic(COMMAND command){
        writeCommand(commandStringHashMap.get(command),"","");
    }

    /**
     * writes a VM label command
     * @param label
     */
    public void writeLabel(String label){
        writeCommand("label",label,"");
    }

    /**
     * writes a VM goto command
     * @param label
     */
    public void writeGoto(String label){
        writeCommand("goto",label,"");
    }
    /**
     * writes a VM if-goto command
     * @param label
     */
    public void writeIf(String label){
        writeCommand("if-goto",label,"");
    }

    /**
     * writes a VM call command
     * @param name
     * @param nArgs
     */
    public void writeCall(String name, int nArgs){
        writeCommand("call",name,String.valueOf(nArgs));
    }

    /**
     * writes a VM function command
     * @param name
     * @param nLocals
     */
    public void writeFunction(String name, int nLocals){
        writeCommand("function",name,String.valueOf(nLocals));
    }

    /**
     * writes a VM return command
     */
    public void writeReturn(){
        writeCommand("return","","");
    }

    public void writeCommand(String cmd, String arg1, String arg2){

        printWriter.print(cmd + " " + arg1 + " " + arg2 + "\n");

    }

    /**
     * close the output file
     */
    public void close(){
        printWriter.close();
    }


}

