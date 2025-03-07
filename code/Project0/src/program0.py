"""
Blank_Comments_Cleaner:
This is a project from a course project in Uchicago MPCS 52011 Introduction to Computer Systems

It removes unnecessary content from a text file, such as:
1. Blank lines
2. Leading and trailing whitespace
3. Inline comments (//)
4. Block comments (/* ... */)

Author:
    Yiming Cheng eaminchan@uchicago.edu

Compatibility:
    This program is written in Python 3.9.4 and has been tested on:
    - macOS Ventura
    - Ubuntu 20.04
    - Windows 10

Usage:
    python3 program0.py <input_file>.in

Input:
    A text file with a ".in" extension.

Output:
    A cleaned version of the file is saved as <input_file>.out.

Example:
    Input file: example.in
    -----------------------
/* Draws a rectangle at the top-left corner of the screen.
The rectangle is 16 pixels wide and R0 pixels high. */

(KBDLOOP)
	@KBD	// loop until key pressed
	D=M
	@KBDLOOP
	D;JEQ
	
	@50		// setup: rect will be 50 high
	D=A
	@R0


    Output file: example.out
    -------------------------
(KBDLOOP)
@KBD
D=M
@KBDLOOP
D;JEQ
@50
D=A
@R0


Note:
    - This program assumes the input file uses UTF-8 encoding.
    - Nested block comments (e.g., /* /* nested */ */) are not supported.

License:
    This project is licensed under the WTFPL.
    For more details, see http://www.wtfpl.net/about/.

Disclaimer:
    This program is for educational purposes only. The author is not responsible for any misuse or damage caused by this program.
"""

import sys
import os


def validate_file(file_path):
    """
    Validates the input file and generates the output file path.
    Ensures the input file exists and ends with `.in`.
    """
    if not file_path.endswith(".in"):
        print("Error: Input file must have a '.in' extension.")
        sys.exit(1)
    if not os.path.isfile(file_path):
        print(f"Error: File not found - {file_path}")
        sys.exit(1)
    return os.path.splitext(file_path)[0] + ".out"


def clean_line(line, in_comment):
    """
    Cleans a line by removing comments and unnecessary whitespace.
    Handles ongoing block comments and inline comments.
    """
    if in_comment:
        end_idx = line.find("*/")
        return (line[end_idx + 2:] if end_idx != -1 else "", end_idx == -1)

    start_idx = line.find("/*")
    if start_idx != -1:
        end_idx = line.find("*/", start_idx + 2)
        return (line[:start_idx] + line[end_idx + 2:] if end_idx != -1 else line[:start_idx], end_idx == -1)

    return line.split("//", 1)[0].strip(), False


def process_file(input_file, output_file):
    """
    Reads the input file, processes each line to clean comments and whitespace,
    and writes the cleaned content to the output file.
    """
    in_comment = False
    with open(input_file, "r") as infile, open(output_file, "w") as outfile:
        for line in infile:
            line, in_comment = clean_line(line.strip(), in_comment)
            if line:
                outfile.write(line + "\n")


def main():
    """
    Main entry point for the program. Handles input validation and file processing.
    """
    if len(sys.argv) != 2:
        print("Usage: python3 program0.py <input_file>.in")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = validate_file(input_file)
    process_file(input_file, output_file)
    print(f"File processed successfully. Output saved to: {output_file}")


if __name__ == "__main__":
    main()