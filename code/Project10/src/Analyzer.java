 
import java.io.File;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.PrintWriter;
public class Analyzer {

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

            System.out.println("Usage:java JackAnalyzer [filename|directory]");

        }else {

            String fileInName = args[0];

            File fileIn = new File(fileInName);

            String fileOutPath = "", tokenFileOutPath = "";

            File fileOut,tokenFileOut;

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

                fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".xml";
                tokenFileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + "T.xml";
                fileOut = new File(fileOutPath);
                tokenFileOut = new File(tokenFileOutPath);

                CompilationEngine compilationEngine = new CompilationEngine(f,fileOut,tokenFileOut);
                compilationEngine.compileClass();

                System.out.println("File created : " + fileOutPath);
                System.out.println("File created : " + tokenFileOutPath);
            }

        }

    }
}
class Tokenizer {

    //constant for type
    public final static int KEYWORD = 1;
    public final static int SYMBOL = 2;
    public final static int IDENTIFIER = 3;
    public final static int INT_CONST = 4;
    public final static int STRING_CONST = 5;

    //constant for keyword
    public final static int CLASS = 10;
    public final static int METHOD = 11;
    public final static int FUNCTION = 12;
    public final static int CONSTRUCTOR = 13;
    public final static int INT = 14;
    public final static int BOOLEAN = 15;
    public final static int CHAR = 16;
    public final static int VOID = 17;
    public final static int VAR = 18;
    public final static int STATIC = 19;
    public final static int FIELD = 20;
    public final static int LET = 21;
    public final static int DO = 22;
    public final static int IF = 23;
    public final static int ELSE = 24;
    public final static int WHILE = 25;
    public final static int RETURN = 26;
    public final static int TRUE = 27;
    public final static int FALSE = 28;
    public final static int NULL = 29;
    public final static int THIS = 30;

    private Scanner scanner;
    private String currentToken;
    private int currentTokenType;
    private int pointer;
    private ArrayList<String> tokens;


    private static Pattern tokenPatterns;
    private static String keyWordReg;
    private static String symbolReg;
    private static String intReg;
    private static String strReg;
    private static String idReg;

    private static HashMap<String,Integer> keyWordMap = new HashMap<String, Integer>();
    private static HashSet<Character> opSet = new HashSet<Character>();

    static {

        keyWordMap.put("class",CLASS);keyWordMap.put("constructor",CONSTRUCTOR);keyWordMap.put("function",FUNCTION);
        keyWordMap.put("method",METHOD);keyWordMap.put("field",FIELD);keyWordMap.put("static",STATIC);
        keyWordMap.put("var",VAR);keyWordMap.put("int",INT);keyWordMap.put("char",CHAR);
        keyWordMap.put("boolean",BOOLEAN);keyWordMap.put("void",VOID);keyWordMap.put("true",TRUE);
        keyWordMap.put("false",FALSE);keyWordMap.put("null",NULL);keyWordMap.put("this",THIS);
        keyWordMap.put("let",LET);keyWordMap.put("do",DO);keyWordMap.put("if",IF);
        keyWordMap.put("else",ELSE);keyWordMap.put("while",WHILE);keyWordMap.put("return",RETURN);

        opSet.add('+');opSet.add('-');opSet.add('*');opSet.add('/');opSet.add('&');opSet.add('|');
        opSet.add('<');opSet.add('>');opSet.add('=');
    }



    /**
     * Opens the input file/stream and gets ready to tokenize it
     * @param inFile
     */
    public Tokenizer(File inFile) {

        try {

            scanner = new Scanner(inFile);
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
        currentTokenType = -1;

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
        idReg = "[\\w_]+";

        tokenPatterns = Pattern.compile(keyWordReg + symbolReg + "|" + intReg + "|" + strReg + "|" + idReg);
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
            currentTokenType = KEYWORD;
        }else if (currentToken.matches(symbolReg)){
            currentTokenType = SYMBOL;
        }else if (currentToken.matches(intReg)){
            currentTokenType = INT_CONST;
        }else if (currentToken.matches(strReg)){
            currentTokenType = STRING_CONST;
        }else if (currentToken.matches(idReg)){
            currentTokenType = IDENTIFIER;
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
    public int tokenType(){

        return currentTokenType;
    }

    /**
     * Returns the keyword which is the current token
     * Should be called only when tokeyType() is KEYWORD
     * @return
     */
    public int keyWord(){

        if (currentTokenType == KEYWORD){

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

        if (currentTokenType == SYMBOL){

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

        if (currentTokenType == IDENTIFIER){

            return currentToken;

        }else {
            throw new IllegalStateException("Current token is not an identifier!");
        }
    }

    /**
     * Returns the integer value of the current token
     * should be called only when tokenType() is INT_CONST
     * @return
     */
    public int intVal(){

        if(currentTokenType == INT_CONST){

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

        if (currentTokenType == STRING_CONST){

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

    private PrintWriter printWriter;
    private PrintWriter tokenPrintWriter;
    private Tokenizer tokenizer;

    /**
     * Creates a new compilation engine with the given input and output.
     * The next routine called must be compileClass()
     * @param inFile
     * @param outFile
     */
    public CompilationEngine(File inFile, File outFile, File outTokenFile) {

        try {

            tokenizer = new Tokenizer(inFile);
            printWriter = new PrintWriter(outFile);
            tokenPrintWriter = new PrintWriter(outTokenFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Compiles a type
     */
    private void compileType(){

        tokenizer.advance();

        boolean isType = false;

        if (tokenizer.tokenType() == Tokenizer.KEYWORD && (tokenizer.keyWord() == Tokenizer.INT || tokenizer.keyWord() == Tokenizer.CHAR || tokenizer.keyWord() == Tokenizer.BOOLEAN)){
            printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            isType = true;
        }

        if (tokenizer.tokenType() == Tokenizer.IDENTIFIER){
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            isType = true;
        }

        if (!isType) error("in|char|boolean|className");
    }

    /**
     * Complies a complete class
     * class: 'class' className '{' classVarDec* subroutineDec* '}'
     */
    public void compileClass(){

        //'class'
        tokenizer.advance();

        if (tokenizer.tokenType() != Tokenizer.KEYWORD || tokenizer.keyWord() != Tokenizer.CLASS){
            error("class");
        }

        printWriter.print("<class>\n");
        tokenPrintWriter.print("<tokens>\n");

        printWriter.print("<keyword>class</keyword>\n");
        tokenPrintWriter.print("<keyword>class</keyword>\n");

        //className
        tokenizer.advance();

        if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
            error("className");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

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

        tokenPrintWriter.print("</tokens>\n");
        printWriter.print("</class>\n");

        //save file
        printWriter.close();
        tokenPrintWriter.close();

    }

    /**
     * Compiles a static declaration or a field declaration
     * classVarDec ('static'|'field') type varName (','varNAme)* ';'
     */
    private void compileClassVarDec(){

        //first determine whether there is a classVarDec, nextToken is } or start subroutineDec
        tokenizer.advance();

        //next is a '}'
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //next is start subroutineDec or classVarDec, both start with keyword
        if (tokenizer.tokenType() != Tokenizer.KEYWORD){
            error("Keywords");
        }

        //next is subroutineDec
        if (tokenizer.keyWord() == Tokenizer.CONSTRUCTOR || tokenizer.keyWord() == Tokenizer.FUNCTION || tokenizer.keyWord() == Tokenizer.METHOD){
            tokenizer.pointerBack();
            return;
        }

        printWriter.print("<classVarDec>\n");

        //classVarDec exists
        if (tokenizer.keyWord() != Tokenizer.STATIC && tokenizer.keyWord() != Tokenizer.FIELD){
            error("static or field");
        }

        printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");

        //type
        compileType();

        //at least one varName
        boolean varNamesDone = false;

        do {

            //varName
            tokenizer.advance();
            if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
                error("identifier");
            }

            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

            //',' or ';'
            tokenizer.advance();

            if (tokenizer.tokenType() != Tokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ','){

                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");

            }else {

                printWriter.print("<symbol>;</symbol>\n");
                tokenPrintWriter.print("<symbol>;</symbol>\n");
                break;
            }


        }while(true);

        printWriter.print("</classVarDec>\n");

        compileClassVarDec();
    }

    /**
     * Compiles a complete method function or constructor
     */
    private void compileSubroutine(){

        //determine whether there is a subroutine, next can be a '}'
        tokenizer.advance();

        //next is a '}'
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //start of a subroutine
        if (tokenizer.tokenType() != Tokenizer.KEYWORD || (tokenizer.keyWord() != Tokenizer.CONSTRUCTOR && tokenizer.keyWord() != Tokenizer.FUNCTION && tokenizer.keyWord() != Tokenizer.METHOD)){
            error("constructor|function|method");
        }

        printWriter.print("<subroutineDec>\n");

        printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");

        //'void' or type
        tokenizer.advance();
        if (tokenizer.tokenType() == Tokenizer.KEYWORD && tokenizer.keyWord() == Tokenizer.VOID){
            printWriter.print("<keyword>void</keyword>\n");
            tokenPrintWriter.print("<keyword>void</keyword>\n");
        }else {
            tokenizer.pointerBack();
            compileType();
        }

        //subroutineName which is a identifier
        tokenizer.advance();
        if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
            error("subroutineName");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        //'('
        requireSymbol('(');

        //parameterList
        printWriter.print("<parameterList>\n");
        compileParameterList();
        printWriter.print("</parameterList>\n");

        //')'
        requireSymbol(')');

        //subroutineBody
        compileSubroutineBody();

        printWriter.print("</subroutineDec>\n");

        compileSubroutine();

    }

    /**
     * Compiles the body of a subroutine
     * '{'  varDec* statements '}'
     */
    private void compileSubroutineBody(){
        printWriter.print("<subroutineBody>\n");
        //'{'
        requireSymbol('{');
        //varDec*
        compileVarDec();
        //statements
        printWriter.print("<statements>\n");
        compileStatement();
        printWriter.print("</statements>\n");
        //'}'
        requireSymbol('}');
        printWriter.print("</subroutineBody>\n");
    }

    /**
     * Compiles a single statement
     */
    private void compileStatement(){

        //determine whether there is a statementnext can be a '}'
        tokenizer.advance();

        //next is a '}'
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        //next is 'let'|'if'|'while'|'do'|'return'
        if (tokenizer.tokenType() != Tokenizer.KEYWORD){
            error("keyword");
        }else {
            switch (tokenizer.keyWord()){
                case Tokenizer.LET:compileLet();break;
                case Tokenizer.IF:compileIf();break;
                case Tokenizer.WHILE:compilesWhile();break;
                case Tokenizer.DO:compileDo();break;
                case Tokenizer.RETURN:compileReturn();break;
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
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
            return;
        }

        //there is parameter, at least one varName
        tokenizer.pointerBack();
        do {
            //type
            compileType();

            //varName
            tokenizer.advance();
            if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
                error("identifier");
            }
             printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

            //',' or ')'
            tokenizer.advance();
            if (tokenizer.tokenType() != Tokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')){
                error("',' or ')'");
            }

            if (tokenizer.symbol() == ','){
                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");
            }else {
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
        if (tokenizer.tokenType() != Tokenizer.KEYWORD || tokenizer.keyWord() != Tokenizer.VAR){
            tokenizer.pointerBack();
            return;
        }

        printWriter.print("<varDec>\n");

        printWriter.print("<keyword>var</keyword>\n");
        tokenPrintWriter.print("<keyword>var</keyword>\n");

        //type
        compileType();

        //at least one varName
        boolean varNamesDone = false;

        do {

            //varName
            tokenizer.advance();

            if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
                error("identifier");
            }

            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

            //',' or ';'
            tokenizer.advance();

            if (tokenizer.tokenType() != Tokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ','){

                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");

            }else {

                printWriter.print("<symbol>;</symbol>\n");
                tokenPrintWriter.print("<symbol>;</symbol>\n");
                break;
            }


        }while(true);

        printWriter.print("</varDec>\n");

        compileVarDec();

    }

    /**
     * Compiles a do statement
     * 'do' subroutineCall ';'
     */
    private void compileDo(){
        printWriter.print("<doStatement>\n");

        printWriter.print("<keyword>do</keyword>\n");
        tokenPrintWriter.print("<keyword>do</keyword>\n");
        //subroutineCall
        compileSubroutineCall();
        //';'
        requireSymbol(';');

        printWriter.print("</doStatement>\n");
    }

    /**
     * Compiles a let statement
     * 'let' varName ('[' ']')? '=' expression ';'
     */
    private void compileLet(){

        printWriter.print("<letStatement>\n");

        printWriter.print("<keyword>let</keyword>\n");
        tokenPrintWriter.print("<keyword>let</keyword>\n");

        //varName
        tokenizer.advance();
        if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
            error("varName");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        //'[' or '='
        tokenizer.advance();
        if (tokenizer.tokenType() != Tokenizer.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')){
            error("'['|'='");
        }

        boolean expExist = false;

        //'[' expression ']'
        if (tokenizer.symbol() == '['){

            expExist = true;

            printWriter.print("<symbol>[</symbol>\n");
            tokenPrintWriter.print("<symbol>[</symbol>\n");

            compileExpression();

            //']'
            tokenizer.advance();
            if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == ']'){
                printWriter.print("<symbol>]</symbol>\n");
                tokenPrintWriter.print("<symbol>]</symbol>\n");
            }else {
                error("']'");
            }
        }

        if (expExist) tokenizer.advance();

        //'='
        printWriter.print("<symbol>=</symbol>\n");
        tokenPrintWriter.print("<symbol>=</symbol>\n");

        //expression
        compileExpression();

        //';'
        requireSymbol(';');

        printWriter.print("</letStatement>\n");
    }

    /**
     * Compiles a while statement
     * 'while' '(' expression ')' '{' statements '}'
     */
    private void compilesWhile(){
        printWriter.print("<whileStatement>\n");

        printWriter.print("<keyword>while</keyword>\n");
        tokenPrintWriter.print("<keyword>while</keyword>\n");
        //'('
        requireSymbol('(');
        //expression
        compileExpression();
        //')'
        requireSymbol(')');
        //'{'
        requireSymbol('{');
        //statements
        printWriter.print("<statements>\n");
        compileStatement();
        printWriter.print("</statements>\n");
        //'}'
        requireSymbol('}');

        printWriter.print("</whileStatement>\n");
    }

    /**
     * Compiles a return statement
     * ‘return’ expression? ';'
     */
    private void compileReturn(){
        printWriter.print("<returnStatement>\n");

        printWriter.print("<keyword>return</keyword>\n");
        tokenPrintWriter.print("<keyword>return</keyword>\n");

        //check if there is any expression
        tokenizer.advance();
        //no expression
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == ';'){
            printWriter.print("<symbol>;</symbol>\n");
            tokenPrintWriter.print("<symbol>;</symbol>\n");
            printWriter.print("</returnStatement>\n");
            return;
        }

        tokenizer.pointerBack();
        //expression
        compileExpression();
        //';'
        requireSymbol(';');

        printWriter.print("</returnStatement>\n");
    }

    /**
     * Compiles an if statement
     * possibly with a trailing else clause
     * 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
     */
    private void compileIf(){
        printWriter.print("<ifStatement>\n");

        printWriter.print("<keyword>if</keyword>\n");
        tokenPrintWriter.print("<keyword>if</keyword>\n");
        //'('
        requireSymbol('(');
        //expression
        compileExpression();
        //')'
        requireSymbol(')');
        //'{'
        requireSymbol('{');
        //statements
        printWriter.print("<statements>\n");
        compileStatement();
        printWriter.print("</statements>\n");
        //'}'
        requireSymbol('}');

        //check if there is 'else'
        tokenizer.advance();
        if (tokenizer.tokenType() == Tokenizer.KEYWORD && tokenizer.keyWord() == Tokenizer.ELSE){
            printWriter.print("<keyword>else</keyword>\n");
            tokenPrintWriter.print("<keyword>else</keyword>\n");
            //'{'
            requireSymbol('{');
            //statements
            printWriter.print("<statements>\n");
            compileStatement();
            printWriter.print("</statements>\n");
            //'}'
            requireSymbol('}');
        }else {
            tokenizer.pointerBack();
        }

        printWriter.print("</ifStatement>\n");

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

        printWriter.print("<term>\n");

        tokenizer.advance();
        //check if it is an identifier
        if (tokenizer.tokenType() == Tokenizer.IDENTIFIER){
            //varName|varName '[' expression ']'|subroutineCall
            String tempId = tokenizer.identifier();

            tokenizer.advance();
            if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '['){
                printWriter.print("<identifier>" + tempId + "</identifier>\n");
                tokenPrintWriter.print("<identifier>" + tempId + "</identifier>\n");
                //this is an array entry
                printWriter.print("<symbol>[</symbol>\n");
                tokenPrintWriter.print("<symbol>[</symbol>\n");
                //expression
                compileExpression();
                //']'
                requireSymbol(']');
            }else if (tokenizer.tokenType() == Tokenizer.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')){
                //this is a subroutineCall
                tokenizer.pointerBack();tokenizer.pointerBack();
                compileSubroutineCall();
            }else {
                printWriter.print("<identifier>" + tempId + "</identifier>\n");
                tokenPrintWriter.print("<identifier>" + tempId + "</identifier>\n");
                //this is varName
                tokenizer.pointerBack();
            }

        }else{
            //integerConstant|stringConstant|keywordConstant|'(' expression ')'|unaryOp term
            if (tokenizer.tokenType() == Tokenizer.INT_CONST){
                printWriter.print("<integerConstant>" + tokenizer.intVal() + "</integerConstant>\n");
                tokenPrintWriter.print("<integerConstant>" + tokenizer.intVal() + "</integerConstant>\n");
            }else if (tokenizer.tokenType() == Tokenizer.STRING_CONST){
                printWriter.print("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>\n");
                tokenPrintWriter.print("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>\n");
            }else if(tokenizer.tokenType() == Tokenizer.KEYWORD &&
                            (tokenizer.keyWord() == Tokenizer.TRUE ||
                            tokenizer.keyWord() == Tokenizer.FALSE ||
                            tokenizer.keyWord() == Tokenizer.NULL ||
                            tokenizer.keyWord() == Tokenizer.THIS)){
                    printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
                    tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            }else if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '('){
                printWriter.print("<symbol>(</symbol>\n");
                tokenPrintWriter.print("<symbol>(</symbol>\n");
                //expression
                compileExpression();
                //')'
                requireSymbol(')');
            }else if (tokenizer.tokenType() == Tokenizer.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')){
                printWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                tokenPrintWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                //term
                compileTerm();
            }else {
                error("integerConstant|stringConstant|keywordConstant|'(' expression ')'|unaryOp term");
            }
        }

        printWriter.print("</term>\n");
    }

    /**
     * Compiles a subroutine call
     * subroutineName '(' expressionList ')' | (className|varName) '.' subroutineName '(' expressionList ')'
     */
    private void compileSubroutineCall(){

        tokenizer.advance();
        if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
            error("identifier");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        tokenizer.advance();
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '('){
            //'(' expressionList ')'
            printWriter.print("<symbol>(</symbol>\n");
            tokenPrintWriter.print("<symbol>(</symbol>\n");
            //expressionList
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
            //')'
            requireSymbol(')');
        }else if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == '.'){
            //(className|varName) '.' subroutineName '(' expressionList ')'
            printWriter.print("<symbol>.</symbol>\n");
            tokenPrintWriter.print("<symbol>.</symbol>\n");
            //subroutineName
            tokenizer.advance();
            if (tokenizer.tokenType() != Tokenizer.IDENTIFIER){
                error("identifier");
            }
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            //'('
            requireSymbol('(');
            //expressionList
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
            //')'
            requireSymbol(')');
        }else {
            error("'('|'.'");
        }
    }

    /**
     * Compiles an expression
     * term (op term)*
     */
    private void compileExpression(){
        printWriter.print("<expression>\n");

        //term
        compileTerm();
        //(op term)*
        do {
            tokenizer.advance();
            //op
            if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.isOp()){
                if (tokenizer.symbol() == '>'){
                    printWriter.print("<symbol>&gt;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&gt;</symbol>\n");
                }else if (tokenizer.symbol() == '<'){
                    printWriter.print("<symbol>&lt;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&lt;</symbol>\n");
                }else if (tokenizer.symbol() == '&') {
                    printWriter.print("<symbol>&amp;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&amp;</symbol>\n");
                }else {
                    printWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                    tokenPrintWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                }
                //term
                compileTerm();
            }else {
                tokenizer.pointerBack();
                break;
            }

        }while (true);

        printWriter.print("</expression>\n");
    }

    /**
     * Compiles a (possibly empty) comma-separated list of expressions
     * (expression(','expression)*)?
     */
    private void compileExpressionList(){
        tokenizer.advance();
        //determine if there is any expression, if next is ')' then no
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == ')'){
            tokenizer.pointerBack();
        }else {

            tokenizer.pointerBack();
            //expression
            compileExpression();
            //(','expression)*
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == ','){
                    printWriter.print("<symbol>,</symbol>\n");
                    tokenPrintWriter.print("<symbol>,</symbol>\n");
                    //expression
                    compileExpression();
                }else {
                    tokenizer.pointerBack();
                    break;
                }

            }while (true);

        }
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
        if (tokenizer.tokenType() == Tokenizer.SYMBOL && tokenizer.symbol() == symbol){
            printWriter.print("<symbol>" + symbol + "</symbol>\n");
            tokenPrintWriter.print("<symbol>" + symbol + "</symbol>\n");
        }else {
            error("'" + symbol + "'");
        }
    }
}
