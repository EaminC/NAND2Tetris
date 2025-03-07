"""
SymbolTable Class

This module defines a SymbolTable class that manages a symbol table. A symbol table is a data structure 
used in compilers and assemblers to store mappings between symbols (like variable names or memory locations)
and their corresponding memory addresses. This implementation supports adding, deleting, and retrieving 
symbols, as well as checking for symbol existence.

Usage:

1. Initialize the SymbolTable:
    - Create an instance of the SymbolTable class using `st = SymbolTable()`. 
    - The table will be initialized with a set of predefined symbols like `SP`, `LCL`, `R0`, `SCREEN`, and `KBD`.

2. Adding a Symbol:
    - Use `st.add_symbol(symbol_name, symbol_position)` to add a new symbol with a given memory position.
    Example: `st.add_symbol("TEST", 999)`

3. Deleting a Symbol:
    - Delete a symbol either by its name using `st.del_symbol(symbol_name="TEST")`, or by its memory position using `st.del_symbol(symbol_position=999)`.

4. Retrieving a Symbol:
    - Use `st.get_symbol(symbol_name)` to get the memory address of a symbol.
    Example: `st.get_symbol("R0")` returns `0`.

5. Checking Symbol Existence:
    - Use `st.contain_symbol(symbol_name)` to check if a symbol exists in the table.
    Example: `st.contain_symbol("R0")` returns `True` if `"R0"` exists.

6. Printing the Symbol Table:
    - Use `st.print_symboltable()` to print the entire symbol table.
    
Tests:
- The script includes some tests to validate the behavior of adding, deleting, and retrieving symbols.
"""




class SymbolTable():
    def __init__(self):
        """Initialize the symbol table with predefined symbols and their memory addresses."""
        self.symbol_table = \
               {
                'SP':0,
                'LCL':1, 
                'ARG':2,
                'THIS':3,
                'THAT':4,
                'R0':0,
                'R1':1, 
                'R2':2, 
                'R3':3,
                'R4':4, 
                'R5':5, 
                'R6':6, 
                'R7':7,
                'R8':8, 
                'R9':9,
                'R10':10, 
                'R11':11, 
                'R12':12,
                'R13':13, 
                'R14':14,
                'R15':15,
                'SCREEN':0x4000,
                'KBD':0x6000
                }
        

    def add_symbol(self,symbol_name,symbol_position):
        """Add a new symbol with its memory position."""
        self.symbol_table[symbol_name]=symbol_position


    def del_symbol(self,symbol_name = None,symbol_position = None):
        """
        Delete a symbol from the table.
        Can delete either by symbol name or by memory position.
        """
        if symbol_name :
            del self.symbol_table[symbol_name]
            return
        elif symbol_position :
            for name,position in list(self.symbol_table.items()) :
                if position == symbol_position :
                    del self.symbol_table[name]
            return
        
    def get_symbol(self,symbol_name):
        """Retrieve the memory address of a given symbol."""
        return self.symbol_table[symbol_name]
    
    def contain_symbol(self,symbol_name):
        """Check if a symbol exists in the table."""
        return symbol_name in self.symbol_table
    
    def print_symboltable(self):
        """Print the entire symbol table."""
        print(self.symbol_table,"\n")
        return
    





if __name__ == "__main__":

    print("Running tests for SymbolTable...\n")

    st = SymbolTable()

    # Test printing the symbol table
    st.print_symboltable()

    # Test adding a new symbol
    st.add_symbol("TEST", 999)
    assert st.get_symbol("TEST") == 999
    print("✅ Add symbol test passed.")
 
    # Test deleting a symbol by name
    st.del_symbol(symbol_name="TEST")
    assert not st.contain_symbol("TEST")
    print("✅ Delete by name test passed.")

    # Test deleting a symbol by position
    st.del_symbol(symbol_position=4)
    assert not st.contain_symbol("THAT")
    print("✅ Delete by position test passed.")
    print("✅ Contain symbol test passed.")
    print("✅ Print test passed.")

    print("\nAll tests passed!")

