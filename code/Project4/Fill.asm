// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File: projects/04/Fill.asm

// The program toggles the screen state based on keyboard input.
// When a key is pressed, it fills the screen with black pixels.
// When no key is pressed, it clears the screen to white.

// Initialize variables
@SCREEN
D=A
@CURSOR        // Current pixel position
M=D-1

// Main loop
(LOOP)
@KBD
D=M            // Read keyboard state
@DRAW_BLACK    // Jump to blacken if a key is pressed
D;JGT
@CLEAR_WHITE   // Otherwise, clear to white
0;JMP




// Blacken the screen
(DRAW_BLACK)

//Dectect if arrives the end
@24576         // End of screen memory
D=A
@CURSOR
D=D-M          // Compare current position with end
@LOOP          // If at end, return to loop
D;JEQ

//Draw
@CURSOR
A=M            // Access current position
M=-1           // Set pixel to black
@CURSOR
D=M+1          // Increment position
M=D
@LOOP          // Return to main loop
0;JMP




// Clear the screen
(CLEAR_WHITE)

//Dectect if arrives the start
@SCREEN        // Start of screen memory
D=A-1          // Offset to avoid clearing top-left artifact
@CURSOR
D=D-M          // Compare current position with start
@LOOP          // If at start, return to loop
D;JEQ

//clear
@CURSOR
A=M            // Access current position
M=0            // Set pixel to white
@CURSOR
D=M-1          // Decrement position
M=D
@LOOP          // Return to main loop
0;JMP