"""
Parser Class

This module defines a Parser class that processes assembly language commands after they have been tokenized 
by the Lexer. It interprets the tokens as different types of commands (A-command, C-command, L-command) and 
extracts the relevant components (symbol, destination, computation, and jump) for each command type. 

The Parser is designed to handle the parsing of assembly instructions typically used in the Hack assembly language.
It classifies commands into three types:
1. A-command: A simple command with a symbol or number (e.g., '@100').
2. C-command: A computation command that can include a destination, computation, and optional jump (e.g., 'D=A;JGT').
3. L-command: A label command that contains a label in parentheses (e.g., '(LOOP)').

Usage:

1. Initialize the Parser:
    - Create an instance of the Parser class using `parser = Parser("file.asm")`.
    - The parser will initialize a Lexer instance to read and tokenize the assembly file.

2. Process Commands:
    - Use `parser.advance()` to process the next command in the assembly file.
    - This will update internal state variables such as `cmd_type`, `symbol`, `dest`, `comp`, and `jmp`.

3. Accessing Command Information:
    - Use methods such as `parser.command_type()` to get the type of the current command (A, C, or L).
    - Use `parser.get_symbol()`, `parser.get_dest()`, `parser.get_comp()`, and `parser.get_jmp()` to retrieve the relevant parts of the current command.

4. Parsing Details:
    - A-commands are parsed by extracting a symbol or number.
    - C-commands are parsed into three parts: `dest`, `comp`, and `jmp`.
    - L-commands are parsed by extracting the label.

Tests:
- The script includes test cases to validate the parser's ability to correctly parse A, C, and L commands from an example assembly file.
"""

import Lexer

class Parser:
    A_COMMAND = 0
    C_COMMAND = 1
    L_COMMAND = 2
    
    def __init__(self, file):
        self.lexer = Lexer.Lexer(file)
        self.init_cmd_info()
    
    def init_cmd_info(self):
        self.cmd_type = -1
        self.symbol = ''
        self.dest = ''
        self.comp = ''
        self.jmp = ''
    
    def has_more_commands(self):
        return self.lexer.has_more_commands()
    
    def advance(self):
        """ Process the next command """
        self.init_cmd_info()
        self.lexer.next_command()

        if not self.lexer.has_more_tokens():
            print("Lexer returned an empty command!")
            return

        token, value = self.lexer.next_token()

        if token == Lexer.OP and value == '@':
            self.a_command()
        elif token == Lexer.OP and value == '(':
            self.l_command()
        else:
            self.c_command(token, value)
    
    def command_type(self):
        return self.cmd_type 
        
    def get_symbol(self):
        return self.symbol
    
    def get_dest(self):
        return self.dest
    
    def get_comp(self):
        return self.comp
        
    def get_jmp(self):
        return self.jmp
        
    def a_command(self):
        """ Parse A-command: @symbol or @number """
        self.cmd_type = Parser.A_COMMAND
        token, self.symbol = self.lexer.next_token() 
    
    def l_command(self):
        """ Parse L-command: (symbol) """
        self.cmd_type = Parser.L_COMMAND
        _, self.symbol = self.lexer.next_token()

    def c_command(self, token, value):
        """ Parse C-command: dest=comp;jump """
        self.cmd_type = Parser.C_COMMAND
        comp_token, comp_value = self.parse_dest(token, value)
        self.parse_comp(comp_token, comp_value)
        self.parse_jump()

    def parse_dest(self, token, value):
        """ Extract 'dest' part if present, return first token of 'comp' part """
        next_token, next_value = self.lexer.peek_token()
        if next_token == Lexer.OP and next_value == '=':
            self.lexer.next_token()
            self.dest = value
            return self.lexer.next_token()
        return token, value
    
    def parse_comp(self, token, value):
        """ Extract 'comp' part, must be present """
        if token == Lexer.OP and value in ('-', '!'):
            _, next_value = self.lexer.next_token()
            self.comp = value + next_value
        else:
            self.comp = value
            next_token, next_value = self.lexer.peek_token()
            if next_token == Lexer.OP and next_value != ';':
                self.lexer.next_token()
                _, third_value = self.lexer.next_token()
                self.comp += next_value + third_value
        
    def parse_jump(self):
        """ Extract 'jump' part if present """
        token, value = self.lexer.next_token()
        if token == Lexer.OP and value == ';':
            _, self.jmp = self.lexer.next_token()


if __name__ == "__main__":
    test_code = """
    @100
    D=A
    D;JGT
    (LOOP)
    M=D+1
    """
    with open('test.asm', 'w') as file:
        file.write(test_code)

    parser = Parser('test.asm')

    print("Test 1: A-command Parsing")
    parser.advance()
    assert parser.command_type() == Parser.A_COMMAND
    assert parser.get_symbol() == "100"
    print("✅ A-command Test Passed")

    print("Test 2: C-command Parsing (dest=comp)")
    parser.advance()
    assert parser.command_type() == Parser.C_COMMAND
    assert parser.get_dest() == "D"
    assert parser.get_comp() == "A"
    print("✅ C-command Test Passed (dest=comp)")

    print("Test 3: C-command Parsing (comp;jump)")
    parser.advance()
    assert parser.command_type() == Parser.C_COMMAND
    assert parser.get_comp() == "D"
    assert parser.get_jmp() == "JGT"
    print("✅ C-command Test Passed (comp;jump)")

    print("Test 4: L-command Parsing")
    parser.advance()
    assert parser.command_type() == Parser.L_COMMAND
    assert parser.get_symbol() == "LOOP"
    print("✅ L-command Test Passed")

    print("Test 5: C-command Parsing (M=D+1)")
    parser.advance()
    assert parser.command_type() == Parser.C_COMMAND
    assert parser.get_dest() == "M"
    assert parser.get_comp() == "D+1"
    print("✅ C-command Test Passed (M=D+1)")

    import os
    os.remove('test.asm')

    print("\nAll tests passed successfully!")