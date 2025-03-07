[toc]

==Thank you for your time==

==Any Problem Appears I beg you to email==

==·`eaminc0328@gmail.com`==

==First==

## How to compile the code



### 0.confirm the file is now

```bash
tree
```

```
(base) eamin@EamindeMacBook-Pro YimingChengProject11 % tree
.
├── README.md
├── Test
│   ├── Average
│   │   └── Main.jack
│   ├── ComplexArrays
│   │   └── Main.jack
│   ├── ConvertToBin
│   │   └── Main.jack
│   ├── Pong
│   │   ├── Ball.jack
│   │   ├── Ball.jack.bak
│   │   ├── Bat.jack
│   │   ├── Bat.jack.bak
│   │   ├── Main.jack
│   │   ├── Main.jack.bak
│   │   ├── PongGame.jack
│   │   └── PongGame.jack.bak
│   ├── Seven
│   │   └── Main.jack
│   └── Square
│       ├── Main.jack
│       ├── Square.jack
│       └── SquareGame.jack
└── src
    ├── JackCompiler.java
    └── test_in_one_file.sh

9 directories, 18 files
```



### 1.Go to folder `src/`

```bash
cd  path to src
```

 Make sure you can see

```bash
ls
```

```
(base) eamin@EamindeMacBook-Pro src % ls
JackCompiler.java       test_in_one_file.sh
```



### 2.Run command

```bash
javac JackCompiler.java  
```

Make sure you can see

```
(base) eamin@EamindeMacBook-Pro src % javac JackCompiler.java 
(base) eamin@EamindeMacBook-Pro src % ls
CompilationEngine$1.class       JackCompiler.java               JackTokenizer.class             SymbolTable.class               VMWriter.class
CompilationEngine.class         JackTokenizer$KEYWORD.class     Symbol$KIND.class               VMWriter$COMMAND.class          test_in_one_file.sh
JackCompiler.class              JackTokenizer$TYPE.class        Symbol.class                    VMWriter$SEGMENT.class
```



### 3.After compiling, there will be lots of classes file shown above in current path





## How to run the code

1.Command format is

a:

```
  java JackCompiler XXX.jack
```
Analyzer will generate

 `XXX.vm`:result of orginal jack file



in the same directory of `XXX.jack`.



b:

```
java Analyzer directory
```

Analyzer will generate 

several `.vm` files according to the number of .jack files named by filename in the same directory.

## **[Saving Your Time]:Test Yourself**

### Generate vm files one by one:

==make sure you are at `src`==

Compile

```
cd src
```

```
javac JackCompiler.java     
```

Processing directory `Average`
```
java JackCompiler ../Test/Average
```


Processing directory `ComplexArrays`
```
java JackCompiler ../Test/ComplexArrays
```


Processing directory `ConvertToBin`
```
java JackCompiler ../Test/ConvertToBin
```
Processing directory `Pong`
```
java JackCompiler ../Test/Pong
```


Processing directory `Seven`
```
java JackCompiler ../Test/Seven
```


Processing directory `Square`
```
java JackCompiler ../Test/Square
```



### Generate vm files with one shell:

==make sure you are at `src`==

```
sudo ./test_in_one_file.sh
```

All vm are generated at once like follow：

![image-20250306215934645](/Users/eamin/Library/Application Support/typora-user-images/image-20250306215934645.png)





Now you can see vm files：

```
(base) eamin@EamindeMacBook-Pro YimingChengProject11 % tree ../
.
├── README.md
├── Test
│   ├── Average
│   │   ├── Main.jack
│   │   └── Main.vm
│   ├── ComplexArrays
│   │   ├── Main.jack
│   │   └── Main.vm
│   ├── ConvertToBin
│   │   ├── Main.jack
│   │   └── Main.vm
│   ├── Pong
│   │   ├── Ball.jack
│   │   ├── Ball.jack.bak
│   │   ├── Ball.vm
│   │   ├── Bat.jack
│   │   ├── Bat.jack.bak
│   │   ├── Bat.vm
│   │   ├── Main.jack
│   │   ├── Main.jack.bak
│   │   ├── Main.vm
│   │   ├── PongGame.jack
│   │   ├── PongGame.jack.bak
│   │   └── PongGame.vm
│   ├── Seven
│   │   ├── Main.jack
│   │   └── Main.vm
│   └── Square
│       ├── Main.jack
│       ├── Main.vm
│       ├── Square.jack
│       ├── Square.vm
│       ├── SquareGame.jack
│       └── SquareGame.vm
└── src
    ├── CompilationEngine$1.class
    ├── CompilationEngine.class
    ├── JackCompiler.class
    ├── JackCompiler.java
    ├── JackTokenizer$KEYWORD.class
    ├── JackTokenizer$TYPE.class
    ├── JackTokenizer.class
    ├── Symbol$KIND.class
    ├── Symbol.class
    ├── SymbolTable.class
    ├── VMWriter$COMMAND.class
    ├── VMWriter$SEGMENT.class
    ├── VMWriter.class
    └── test_in_one_file.sh

9 directories, 41 files
```

## **[Saving Your Time] :Test Result**

### Seven:☑️

(7 is shown in the screen)

![image-20250306220524623](/Users/eamin/Library/Application Support/typora-user-images/image-20250306220524623.png)

### ConvertToBin☑️

$23456 _{10}= 0101 1011 0100 0000_{2} $

SO theortically

If Ram[8000]=23456

The result should be

==Ram[8001] is LSB means the number is Writing FromRam[8016] to Ram[8001]==  

<img src="/Users/eamin/Library/Application Support/typora-user-images/image-20250306222150665.png" alt="image-20250306222150665" style="zoom:33%;" />

Output:☑️

![image-20250306221859908](/Users/eamin/Library/Application Support/typora-user-images/image-20250306221859908.png)

### Square Dance☑️

see detail in the `video_of_some_result/Square.mp4`

![image-20250306222618492](/Users/eamin/Library/Application Support/typora-user-images/image-20250306222618492.png)

### Average☑️

![image-20250306222950260](/Users/eamin/Library/Application Support/typora-user-images/image-20250306222950260.png)

### Pong☑️

the program is too slow in `video_of_some_result/Pong.mp4`

![image-20250306223637769](/Users/eamin/Library/Application Support/typora-user-images/image-20250306223637769.png)

### ComplexArrays☑️

![image-20250306223902394](/Users/eamin/Library/Application Support/typora-user-images/image-20250306223902394.png)
