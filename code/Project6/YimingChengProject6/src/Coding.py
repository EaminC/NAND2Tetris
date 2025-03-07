"""
coding Class

This module defines a coding class that handles the generation of binary code for assembly instructions.
The class includes methods to generate both A-instructions (for address assignments) and C-instructions 
(for computation and control operations). It supports encoding the destination (dest), computation (comp), 
and jump conditions (jump) into their corresponding binary representations.

Usage:

1. Initialize the coding class:
    - Create an instance of the coding class using `code = coding()`. 
    - The class will be initialized with predefined mappings for comp, dest, and jump codes.

2. Generating an A-instruction:
    - Use `code.generateA(address)` to generate an A-instruction.
    Example: `code.generateA(100)` returns `'0000000001100100'` (100 in 15-bit binary with leading 0).

3. Generating a C-instruction:
    - Use `code.generateC(dest, comp, jump)` to generate a C-instruction.
    Example: `code.generateC("D", "A", "JGT")` returns `'1110110000001001000000111'` representing the C-instruction.

4. Generating a Destination Code:
    - Use `code.getDest(dest)` to get the binary representation of the destination.
    Example: `code.getDest("D")` returns `'010'`.

5. Generating a Computation Code:
    - Use `code.getComp(comp)` to get the binary representation of the computation.
    Example: `code.getComp("A")` returns `'0110000'`.

6. Generating a Jump Code:
    - Use `code.getJump(jump)` to get the binary representation of the jump condition.
    Example: `code.getJump("JGT")` returns `'001'`.

Tests:
- The script includes tests to validate the behavior of generating both A- and C-instructions, checking the correctness of binary outputs for specific instructions.
"""

class Coding():
    def __init__(self):
        pass
    
    def generateA(self, addr):
        """
        Generates the A-instruction in binary format.
        The binary format starts with 0 followed by the 15-bit address.
        """
        return '0' + self.convertToBits(addr, 15)
        
    def generateC(self, dest, comp, jump):
        """
        Generates the C-instruction in binary format.
        The binary format starts with 111 followed by the binary encoding of the comp, dest, and jump fields.
        """
        comp_bin = self.getComp(comp)
        dest_bin = self.getDest(dest)
        jump_bin = self.getJump(jump)

        # Concatenate the final result
        final_result = '111' + comp_bin + dest_bin + jump_bin
        
        return final_result
    
    # Destination Codes (Fixed 'D' entry)
    destCodes = ['', 'M', 'D', 'MD', 'A', 'AM', 'AD', 'AMD']
    
    def getDest(self, d):
        """
        Generates the binary string for the destination part of the C-instruction.
        """
        return self.convertToBits(self.destCodes.index(d), 3)
    
    # Computation Codes (each key-value pair is now on a separate line)
    compCodes = {
        '0': '0101010',
        '1': '0111111',
        '-1': '0111010',
        'D': '0001100',
        'A': '0110000',
        '!D': '0001101',
        '!A': '0110001',
        '-D': '0001111',
        '-A': '0110011',
        'D+1': '0011111',
        'A+1': '0110111',
        'D-1': '0001110',
        'A-1': '0110010',
        'D+A': '0000010',
        'D-A': '0010011',
        'A-D': '0000111',
        'D&A': '0000000',
        'D|A': '0010101',
        'M': '1110000',
        '!M': '1110001',
        '-M': '1110011',
        'M+1': '1110111',
        'M-1': '1110010',
        'D+M': '1000010',
        'D-M': '1010011',
        'M-D': '1000111',
        'D&M': '1000000',
        'D|M': '1010101'
    }

    def getComp(self, c):
        """
        Generates the binary string for the computation part of the C-instruction.
        """
        return self.compCodes.get(c, 'xxxxxxx')
    
    # Jump Codes
    jumpCodes = ['', 'JGT', 'JEQ', 'JGE', 'JLT', 'JNE', 'JLE', 'JMP']
    
    def getJump(self, j):
        """
        Generates the binary string for the jump part of the C-instruction.
        """
        return self.convertToBits(self.jumpCodes.index(j), 3)
        
    def convertToBits(self, n, length=0):
        """
        Converts a number to a binary string with the specified length.
        If the length is provided, the output will be zero-padded.
        """
        return bin(int(n))[2:].zfill(length)


if __name__ == "__main__":
    # Initialize the Code class for testing
    code = Coding()

    # Test 1: A-command generation (Address: 100)
    print("Test 1: A-command Generation")
    result = code.generateA(100)
    expected = '0000000001100100'  # 100 in 15-bit binary with leading 0
    assert result == expected, f"Expected {expected}, but got {result}"
    print("✅ A-command Test Passed")

    # Test 2: C-command generation (dest=D, comp=A, jump=JGT)
    print("Test 2: C-command Generation (dest=D, comp=A, jump=JGT)")
    result = code.generateC("D", "A", "JGT")
    expected = '1110110000010001'  # Corrected expected value
    assert result == expected, f"Expected {expected}, but got {result}"
    print("✅ C-command Test Passed (dest=D, comp=A, jump=JGT)")

    # Test 3: C-command generation (dest=M, comp=D+1, jump=JMP)
    print("Test 3: C-command Generation (dest=M, comp=D+1, jump=JMP)")
    result = code.generateC("M", "D+1", "JMP")
    expected = '1110011111001111'  # Corrected expected C-instruction (16 bits)
    assert result == expected, f"Expected {expected}, but got {result}"
    print("✅ C-command Test Passed (dest=M, comp=D+1, jump=JMP)")

    # Test 4: Destination part of C-command (dest=M)
    print("Test 4: Destination (dest=M)")
    result = code.getDest("M")
    expected = '001'  # 'M' maps to '001' in binary
    assert result == expected, f"Expected {expected}, but got {result}"
    print("✅ Destination Test Passed (dest=M)")

    # Test 5: Computation part of C-command (comp=D+A)
    print("Test 5: Computation (comp=D+A)")
    result = code.getComp("D+A")
    expected = '0000010'  # 'D+A' maps to '0000010' in binary
    assert result == expected, f"Expected {expected}, but got {result}"
    print("✅ Computation Test Passed (comp=D+A)")

    # Test 6: Jump part of C-command (jump=JNE)
    print("Test 6: Jump (jump=JNE)")
    result = code.getJump("JNE")
    expected = '101'  # 'JNE' maps to '011' in binary
    assert result == expected, f"Expected {expected}, but got {result}"
    print("✅ Jump Test Passed (jump=JNE)")

    print("\nAll tests passed successfully!")