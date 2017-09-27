# OperatingSystemsProject1
The objective of this project was to create system calls fork, join and exec on a hardware simulated 
Operating System in Java. 

The following System Calls were implemeted

1) Fork - Copy of parent process is created and equivalent mount of memory is allotted to it.
2) Join -  The pid of process to be waited for is checked for validity and current process is put to sleep until the second 
           process begins execution.
3)Exec - Opened the file at the particular address space and overwrote the contents of the process
               with the content and reset the registers to their initial default.

Classes modified : 

SystemHandler.java
NachosProcess.java
JNachos.java
CreatorClass.java
