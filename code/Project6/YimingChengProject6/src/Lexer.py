"""
Lexical Analyzer (Lexer) Class

This module defines a Lex class that performs lexical analysis for assembly language programs. 
It reads an assembly file, processes each line, and tokenizes the content into meaningful 
units such as numbers, identifiers, and operators. These tokens are then used by the rest of 
the assembler or compiler for further analysis or transformation.

The Lexer handles the following tasks:
1. Reads the input file line by line.
2. Removes comments from the lines.
3. Splits the lines into tokens using regular expressions.
4. Classifies tokens into different categories: numbers, identifiers, and operators.

Usage:

1. Initialize the Lexer:
    - Create an instance of the Lex class using `lexer = Lex("file.asm")`.
    - The lexer will load and tokenize the content of the assembly file.

2. Accessing Tokens:
    - Use `lexer.next_command()` to retrieve the next command's tokens from the input file.
    - Use `lexer.next_token()` to get the next token in the current command.
    - Use `lexer.peek_token()` to preview the next token without advancing the lexer.

3. Checking for More Commands:
    - Use `lexer.has_more_commands()` to check if there are more commands to process in the input.

4. Token Classification:
    - The lexer categorizes tokens into one of the following types:
        - NUM: Numeric values (e.g., '123').
        - ID: Identifiers such as variable names or labels (e.g., 'LOOP').
        - OP: Operators like '=', '+', '-', etc.
        - ERROR: Any unrecognized token.

Tests:
- The script processes assembly language files, tokenizes each line, and ensures the correct 
  categorization of tokens. It also demonstrates how to use the methods for tokenization and 
  command parsing.
"""

import re
from collections import deque

# Token types
NUM, ID, OP, ERROR = 1, 2, 3, 4

class Lexer():            
    # Regular expressions for tokenizing
    RE_NUM  = r'\d+'  #  \d: Matches [0-9], +: One or more times
    RE_OP   = r'[=;()@+\-&|!]'  # OP is one of =;()@+\-&|!
    RE_ID   = r'[\w_.$:][\w_.$:\d]*' 
    # First character can be A-Z, a-z, _, ., $, :, but not 0-9
    # Following characters can include digits 0-9
    RE_WORD = re.compile(f'{RE_NUM}|{RE_OP}|{RE_ID}')
    RE_COMMENT = re.compile(r'//.*$')  # Matches comments starting with //

    def __init__(self, file_name):
        """Initialize the lexer by reading and tokenizing the file."""
        with open(file_name, 'r') as file:
            self.token_queue = deque(self.tokenize(file.readlines()))  
            # Stores all tokenized lines as a queue
        self.current_tokens = deque()  
        # Stores the current command (list of tokens)
        self.current_token = (ERROR, "")  
        # Stores the current token being processed

    def has_more_commands(self):
        """Check if there are more commands to process (non-empty token queue)."""
        return bool(self.token_queue)

    def next_command(self):
        """Fetch the next command (a sequence of tokens)."""
        if self.has_more_commands():
            self.current_tokens = deque(self.token_queue.popleft())  
        return self.current_tokens

    def has_more_tokens(self):
        """Check if there are more tokens in the current command."""
        return bool(self.current_tokens)

    def next_token(self):
        """Retrieve the next token from the current command."""
        self.current_token = self.current_tokens.popleft() if self.has_more_tokens() else (ERROR, "")
        return self.current_token

    def peek_token(self):
        """Look at the next token without consuming it."""
        return self.current_tokens[0] if self.has_more_tokens() else (ERROR, "")

    def tokenize(self, lines):
        """Convert program lines into a list of token sequences."""
        tokenized_lines = []
        for line in lines:
            cleaned_line = self.remove_comment(line).strip()
            if cleaned_line:  # Ignore empty lines after removing comments
                words = self.RE_WORD.findall(cleaned_line)
                tokenized_lines.append([self.classify_token(word) for word in words])
        return tokenized_lines

    def remove_comment(self, line):
        """Remove comments from a line."""
        return self.RE_COMMENT.sub('', line).strip()

    def classify_token(self, word):
        """Determine the type of a word and return it as a token tuple."""
        token_type = ERROR
        if self.is_NUM(word):
            token_type = NUM
        elif self.is_ID(word):
            token_type = ID
        elif self.is_OP(word):
            token_type = OP

        return (token_type, word)
    def is_NUM(self, word):
        """Check if the word is a valid number."""
        return re.match(Lexer.RE_NUM, word) is not None

    def is_OP(self, word):
        """Check if the word is a valid operator."""
        return re.match(Lexer.RE_OP, word) is not None

    def is_ID(self, word):
        """Check if the word is a valid identifier."""
        return re.match(Lexer.RE_ID, word) is not None
    

if __name__ == "__main__":
    # Create a temporary test file for testing
    test_code = """
    // This is a comment
    LOOP = 10;
    ADD R1, R2, R3
    """
    with open('test.asm', 'w') as file:
        file.write(test_code)

    # Test Lexer Initialization and Tokenization
    lexer = Lexer('test.asm')
    print("Test 1: Lexer Initialization")
    assert lexer.has_more_commands(), "Lexer should have commands to process."
    command = lexer.next_command()
    assert len(command) > 0, "Command should contain tokens."
    print("✅ Lexer Initialization Test Passed")

    # Test Tokenize Function
    print("Test 2: Tokenize Function")
    lexer = Lexer('test.asm')
    lexer.next_command()
    tokens = lexer.current_tokens
    assert (ID, "LOOP") in tokens, "LOOP should be classified as an ID."
    assert (OP, "=") in tokens, "Equals sign should be classified as an OP."
    assert (NUM, "10") in tokens, "10 should be classified as a NUM."
    assert (OP, ";") in tokens, "Semicolon should be classified as an OP."
    print("✅ Tokenize Function Test Passed")

    # Test Remove Comment Function
    print("Test 3: Remove Comment Function")
    lexer = Lexer('test.asm')
    line = "LOOP = 10; // This is a comment"
    cleaned_line = lexer.remove_comment(line)
    assert cleaned_line == "LOOP = 10;", "Comment removal failed."
    print("✅ Remove Comment Function Test Passed")

    # Test Classify Token Function
    print("Test 4: Classify Token Function")
    lexer = Lexer('test.asm')
    assert lexer.classify_token("LOOP") == (ID, "LOOP"), "LOOP should be classified as ID."
    assert lexer.classify_token("=") == (OP, "="), "Equals sign should be classified as OP."
    assert lexer.classify_token("10") == (NUM, "10"), "10 should be classified as NUM."
    assert lexer.classify_token(";") == (OP, ";"), "Semicolon should be classified as OP."
    print("✅ Classify Token Function Test Passed")

    # Clean up the temporary test file
    import os
    os.remove('test.asm')

    print("\nAll tests passed!")