"""
Assembler Class

This module defines an Assembler class that processes Hack assembly language files (.asm) and converts them 
into Hack machine code (.hack). The Assembler follows a two-pass approach to generate the corresponding binary 
machine code, which is compatible with the Hack CPU emulator.

The class supports parsing assembly instructions, translating them into machine code, handling symbol addresses 
(e.g., variables and labels), and writing the resulting machine code to an output file.

Usage:

1. Initialize the Assembler:
    - Create an instance of the Assembler class using `asm = Assembler()`.
    - The assembler will initialize with an empty symbol table and the starting address for variables set to 16.

2. First Pass (Pass 0): 
    - The `pass0(file)` method is used to scan the input assembly file and record the locations of labels 
      (e.g., `(LOOP)`) in the symbol table. This phase determines where the labels are located in memory.

3. Second Pass (Pass 1): 
    - The `pass1(infile, outfile)` method processes the file to translate assembly commands into binary machine code.
      It generates A-type and C-type instructions based on the symbol addresses and writes the output to a `.hack` file.
      
4. Address Lookup:
    - The `_get_address(symbol)` method looks up the address of a given symbol, either from the symbol table 
      or by assigning a new address if the symbol is encountered for the first time.

5. Assembling the File:
    - The `assemble(file)` method runs both passes in sequence, converting an input `.asm` file into a `.hack` file.
    - The method automatically creates a new `.hack` file in the same directory as the input `.asm` file, 
      and it checks for existing files to avoid overwriting.

6. Output File Generation:
    - The `_outfile(infile)` method generates the name of the output file by replacing the `.asm` extension with `.hack`.
    - It ensures the output file is created in the same directory as the input file and avoids overwriting existing files.

Tests:
- The script can be run from the command line, where the user provides the input `.asm` file.
- The assembler processes the file and outputs a corresponding `.hack` file that is ready to be loaded into the Hack CPU emulator.
"""


import os
import Parser, Coding, SymbolTable, sys

class Assembler():
    def __init__(self):
        # Initialize the symbol table and set the starting memory address for variables
        self.symbols = SymbolTable.SymbolTable()
        self.symbol_addr = 16  # The next available address for variables, starting from 16
    
    def pass0(self, file):
        """
        First pass of the assembler: 
        - This pass processes the input file to determine the memory locations of labels.
        - Label locations are stored in the symbol table.
        """
        parser = Parser.Parser(file)  # Initialize the parser for reading the input file
        cur_address = 0  # Start with the first memory address for program code

        # Iterate through all commands in the file
        while parser.has_more_commands():
            parser.advance()  # Move to the next command
            cmd = parser.command_type()  # Get the type of the current command
            
            # Increment address for A and C commands, since they correspond to instructions
            if cmd == parser.A_COMMAND or cmd == parser.C_COMMAND:
                cur_address += 1  
            
            # For label definitions (L_COMMAND), add them to the symbol table
            elif cmd == parser.L_COMMAND:
                self.symbols.add_symbol(parser.get_symbol(), cur_address)  # Store label and its address
    
    def pass1(self, infile, outfile):
        """
        Second pass of the assembler:
        - This pass generates the machine code for each command in the input file.
        - The result is written to an output file.
        """
        parser = Parser.Parser(infile)  # Initialize parser for input file
        outf = open(outfile, 'w')  # Open the output file for writing the machine code
        code = Coding.Coding() # Code generator to produce binary representations of commands

        # Iterate through all commands in the input file
        while parser.has_more_commands():
            parser.advance()  # Move to the next command
            cmd = parser.command_type()  # Get the type of the current command
            
            if cmd == parser.A_COMMAND:
                # For A commands, generate binary for the address and write it to the file
                outf.write(code.generateA(self._get_address(parser.get_symbol())) + '\n')
            
            elif cmd == parser.C_COMMAND:
                # For C commands, generate binary for the computation and jump logic
                outf.write(code.generateC(parser.get_dest(), parser.get_comp(), parser.get_jmp()) + '\n')
            
            # Labels (L_COMMAND) don't produce machine code, so we skip them
            elif cmd == parser.L_COMMAND:
                pass
        
        outf.close()  # Close the output file after writing all commands
    
    def _get_address(self, symbol):
        """
        Lookup an address:
        - If the symbol is already a numeric address, return it.
        - Otherwise, if it's a variable or label, ensure it's in the symbol table and assign it an address.
        """
        if symbol.isdigit():
            return symbol  # Return the address if it's already a numeric string
        else:
            # If symbol is not in the symbol table, add it with a new address
            if not self.symbols.contain_symbol(symbol):
                self.symbols.add_symbol(symbol, self.symbol_addr)
                self.symbol_addr += 1  # Increment for the next potential variable/label
            return self.symbols.get_symbol(symbol)  # Return the symbol's address from the table
    
    def assemble(self, file):
        """
        Main assembly function:
        - First runs the label collection (pass0), then generates the machine code (pass1).
        """
        self.pass0(file)  # First pass: Collect labels and their addresses
        self.pass1(file, self._outfile(file))  # Second pass: Generate machine code and write to output
    

    def _outfile(self, infile):
        """
    Generate the output file name by replacing the .asm extension with .hack.
    - The output file will be saved in the same directory as the input file.
    - If the output file already exists, it will be overwritten.
    - If the output file doesn't exist, it will be created.
    """
        # Get the directory path of the input file
        directory = os.path.dirname(os.path.abspath(infile))  # Ensure absolute path
    
        # Generate the output file path by changing the extension to .hack
        output_file = os.path.join(directory, infile.replace('.asm', '.hack'))

        # Check if the output file already exists
        if os.path.exists(output_file):
            print(f"Warning: '{output_file}' already exists. It will be overwritten.")
        else:
            print(f"Creating new file: {output_file}")

        return output_file
def main():
    """
    Main function:
    - Checks the command-line arguments.
    - Initializes the Assembler and runs the assembly process on the provided file.
    """
    if len(sys.argv) != 2:
        print("Usage: Assembler file.asm")  # Error message if incorrect arguments are passed
    else:
        infile = sys.argv[1]  # Get the input file name from the command-line arguments
        asm = Assembler()  # Create an instance of the Assembler class
        asm.assemble(infile)  # Run the assembly process on the input file

if __name__ == "__main__":
    main()  # Call the main function to start the assembly process