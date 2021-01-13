/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/
import java_cup.runtime.*;



/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/

/*****************************************************/
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column
%{
int comment_start_line;
%}

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
%state SINGLE_COMMENT
%state MULTI_COMMENT

/*****************************************************************************/
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */
/*****************************************************************************/
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getCommentBeginLine() { return comment_start_line + 1; }
	public int getLine()    { return yyline + 1; }
	public int getCharPos() { return yycolumn;   }
	public void yyerror()   { throw new java.lang.Error(); }
%}

/***********************/
/* MACRO DECALARATIONS */
DIGIT           = [0-9]
NONZERO_DIGITS  = [1-9]
LETTER          = [a-zA-Z]
SINGLE_COMMENT  = "//"
MULTI_COMMENT_L = "/*"
MULTI_COMMENT_R = "*/"
UNDERSCORE      = "_"
IDENTIFIER      = {LETTER}({LETTER}|{DIGIT}|{UNDERSCORE})*
INTEGER         = {DIGIT}|{NONZERO_DIGITS}({DIGIT})*
WHITESPACE      = \s
NL              = \r | \n | \n\r

/***********************/

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {
"public"            { return symbol(sym.PUBLIC); }
"static"            { return symbol(sym.STATIC); }
"void"              { return symbol(sym.VOID); }
"main"              { return symbol(sym.MAIN); }
"String"            { return symbol(sym.STRING); }
"System.out.println" { return symbol(sym.PRINT); }
"return"            { return symbol(sym.RETURN); }
"true"              { return symbol(sym.TRUE); }
"false"             { return symbol(sym.FALSE); }
"this"              { return symbol(sym.THIS); }
"new"               { return symbol(sym.NEW); }
"length"            { return symbol(sym.LENGTH); }
"."                 { return symbol(sym.DOT); }
","                 { return symbol(sym.COMMA); }
";"                 { return symbol(sym.SEMICOLON); }
"+"                 { return symbol(sym.PLUS); }
"-"                 { return symbol(sym.MINUS); }
"*"                 { return symbol(sym.MULT); }
"<"                 { return symbol(sym.LT); }
"&&"                { return symbol(sym.AND); }
"!"                 { return symbol(sym.NOT); }
"["                 { return symbol(sym.LBRACKET); }
"]"                 { return symbol(sym.RBRACKET); }
"("                 { return symbol(sym.LPAREN); }
")"                 { return symbol(sym.RPAREN); }
"{"                 { return symbol(sym.LCURL); }
"}"                 { return symbol(sym.RCURL); }
"="                 { return symbol(sym.ASSIGN); }
"boolean"           { return symbol(sym.BOOLEAN_TYPE); }
"int"               { return symbol(sym.INT_TYPE); }
"null"              { return symbol(sym.NULL); }
"class"             { return symbol(sym.CLASS); }
"extends"           { return symbol(sym.EXTENDS); }
"if"                { return symbol(sym.IF); }
"while"             { return symbol(sym.WHILE); }
"else"              { return symbol(sym.ELSE); }
{IDENTIFIER}        { return symbol(sym.IDENTIFIER, yytext()); }
{INTEGER}           { return symbol(sym.INTEGER, new Integer(yytext())); }
{WHITESPACE}+       { /* Ignore whitespace */}
{SINGLE_COMMENT}    { yybegin(SINGLE_COMMENT); }
{MULTI_COMMENT_L}   { comment_start_line = yyline; yybegin(MULTI_COMMENT); }
<<EOF>>				{ return symbol(sym.EOF); }
.                   { yyerror(); /* If we reached here, the token does not match any known pattern */ }
}

<SINGLE_COMMENT> {
[^\n]          { /* do nothing */ }
{NL}           { yybegin(YYINITIAL); }
}

<MULTI_COMMENT> {
<<EOF>>				 { yyline = comment_start_line; yyerror(); /* Multi-line comment was not closed */ }
{MULTI_COMMENT_R}    { yybegin(YYINITIAL); }
.                    { /* do nothing */ }
}