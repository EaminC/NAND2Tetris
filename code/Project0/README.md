# Blank Comments Cleaner

This is a project from the **UChicago MPCS 52011: Introduction to Computer Systems** course.  
It processes text files by removing unnecessary content, including:

- Blank lines
- Leading and trailing whitespace
- Inline comments (`//`)
- Block comments (`/* ... */`)

---

## File Structure

```plaintext
.
├── README.txt
├── src
│   └── program0.py
└── test_files
    ├── Project0example.in
    └── Project0example.out
```

---

## Author

- **Yiming Cheng**  
  [eaminchan@uchicago.edu](mailto:eaminchan@uchicago.edu)

---

## Functionality

This program processes text files to clean unnecessary content. It performs the following actions:

### What Works

1. **Remove Blank Lines**:

   - Deletes all blank lines from the input file.

2. **Remove Leading Whitespace**:

   - Removes spaces and tabs before the first non-space character on each line.

3. **Remove Comments**:
   - **Inline Comments**: Lines with comments starting with `//` will have the comment part removed.
   - **Block Comments**: Comments enclosed by `/* ... */` are removed, even if they span multiple lines.

### What Doesn't Work

- **Nested Block Comments**: The program does not support nested block comments, such as `/* /* nested */ */`.
- **Non-UTF-8 Encoding**: Input files must be in UTF-8 encoding. Files with other encodings may cause errors.

---

## Portability

This program has been designed to work across different operating systems without reliance on platform-specific features. It uses only the Python standard library and avoids dependencies on:

- Native compilers
- OS-specific libraries
- System hooks unique to certain environments

### Tested Environments

The program has been tested and verified to function correctly on the following systems:

- macOS Ventura (Python 3.9.4)
- Ubuntu 20.04 (Python 3.8.10)
- Windows 10 (Python 3.9.0)

---

## Compilation Instructions

This program is written in Python and does not require compilation. (Only uses built-in sys and os)
Ensure you have Python 3.9.4 or a compatible version installed on your system.

---

## Run Instructions

Navigate to the `src` directory and run the program using the following command:

```bash
python3 program0.py path/to/<input_file>.in
```

### Input

- A text file with a `.in` extension.
- The file should contain plain text, with or without comments.

### Output

- A cleaned version of the file is saved as `<input_file>.out` in the same directory as the input file.

---

## Example

### Input File (`Project0example.in`)

```plaintext
/* Draws a rectangle at the top-left corner of the screen.
The rectangle is 16 pixels wide and R0 pixels high. */

(KBDLOOP)
    @KBD    // loop until key pressed
    D=M
    @KBDLOOP
    D;JEQ

    @50        // setup: rect will be 50 high
    D=A
    @R0
```

### Output File (`Project0example.out`)

```plaintext
(KBDLOOP)
@KBD
D=M
@KBDLOOP
D;JEQ
@50
D=A
@R0
```

### Running on Different Operating Systems

1. **macOS/Linux**:

   - Open a terminal and navigate to the `src` directory.
   - Run the program using the following command:
     ```bash
     python3 program0.py path/to/<input_file>.in
     ```
   - Example:
     ```bash
     python3 program0.py ../test_files/Project0example.in
     ```

2. **Windows**:

   - Open Command Prompt or PowerShell.
   - Navigate to the `src` directory (e.g., `cd path\to\src`).
   - Run the program with:
     ```cmd
     python program0.py path\to\<input_file>.in
     ```
   - Example:
     ```cmd
     python program0.py ..\test_files\Project0example.in
     ```

3. **Notes**:
   - Replace `<input_file>.in` with the name of your input file.
   - The output file will be saved in the same directory as the input file with a `.out` extension.

---

## Notes

- Ensure the input file uses UTF-8 encoding.
- If no output is produced, check the input file for unsupported comment structures.

---

## License

This project is licensed under the **WTFPL**.  
For more details, see [http://www.wtfpl.net/about/](http://www.wtfpl.net/about/).

---

## Disclaimer

This program is for educational purposes only. The author is not responsible for any misuse or damage caused by this program.

```

```
