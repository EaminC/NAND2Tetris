// Mult.asm
// Multiply the values in R0 and R1 and store the result in R2.





//initialize countdown(add R0 for R1 times,countdown is loop from R1,R1-1....0)
@R1
D=M          // D = R1 
@countdown
M=D          // countdown = R1 

// Initialize Sum(R2) to 0
@R2
M=0          // R2 = 0 



(Loop_Start)

@countdown
D=M         
@Loop_End
D;JLE         // if countdown <= 0, jump outof loop

@R0
D=M          
@R2
M=D+M        // R2 = R2 + R0

@countdown
M=M-1        // countdown--

@Loop_Start
0;JMP        //Loop


(Loop_End)
@Loop_End
0;JMP      