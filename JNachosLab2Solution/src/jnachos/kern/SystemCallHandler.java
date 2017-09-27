/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.*;

/** The class handles System calls made from user programs. */
public class SystemCallHandler  {
	/** The System call index for halting. */
	public static final int SC_Halt = 0;

	/** The System call index for exiting a program. */
	public static final int SC_Exit = 1;

	/** The System call index for executing program. */
	public static final int SC_Exec = 2;

	/** The System call index for joining with a process. */
	public static final int SC_Join = 3;

	/** The System call index for creating a file. */
	public static final int SC_Create = 4;

	/** The System call index for opening a file. */
	public static final int SC_Open = 5;

	/** The System call index for reading a file. */
	public static final int SC_Read = 6;

	/** The System call index for writing a file. */
	public static final int SC_Write = 7;

	/** The System call index for closing a file. */
	public static final int SC_Close = 8;

	/** The System call index for forking a forking a new process. */
	public static final int SC_Fork = 9;

	/** The System call index for yielding a program. */
	public static final int SC_Yield = 10;
	

	/**
	 * Entry point into the Nachos kernel. Called when a user program is
	 * executing, and either does a syscall, or generates an addressing or
	 * arithmetic exception.
	 * 
	 * For system calls, the following is the calling convention:
	 * 
	 * system call code -- r2 arg1 -- r4 arg2 -- r5 arg3 -- r6 arg4 -- r7
	 * 
	 * The result of the system call, if any, must be put back into r2.
	 * 
	 * And don't forget to increment the pc before returning. (Or else you'll
	 * loop making the same system call forever!
	 * 
	 * @pWhich is the kind of exception. The list of possible exceptions are in
	 *         Machine.java
	 **/
	public static void handleSystemCall(int pWhichSysCall) {

		System.out.println("SysCall:" + pWhichSysCall);
		
		Machine.writeRegister(Machine.PrevPCReg, Machine.mRegisters[Machine.PCReg]);
		Machine.writeRegister(Machine.PCReg, Machine.mRegisters[Machine.NextPCReg]);
		Machine.writeRegister(Machine.NextPCReg, Machine.mRegisters[Machine.NextPCReg] + 4);
		
		switch (pWhichSysCall) {
		case SC_Fork:
					
			System.out.println("---------------------------FORK---------------------------");
			
			System.out.println("Process id-->"+JNachos.getCurrentProcess().getPid());
			boolean level = Interrupt.setLevel(false);
		
			Machine.writeRegister(2, 0);
			
			System.out.println("PID of current process "+JNachos.getCurrentProcess().getPid());
			
			//Creating a new child process for forking 
			//Generating a space for the child 
			//Calling fork on child process 
			
			NachosProcess childProcess = new NachosProcess(JNachos.getCurrentProcess().getName());
			int childPid = childProcess.getPid();
					
			childProcess.setSpace(new AddrSpace(JNachos.getCurrentProcess().getSpace()));
			
			childProcess.saveUserState();			
			
			Machine.writeRegister(2, childPid);
			
			Interrupt.setLevel(level);
			childProcess.fork(new CreatorClass(), childProcess.getName());		
			break;
			
		case SC_Join:
					
			System.out.println("---------------------------JOIN---------------------------");
						
			System.out.println("Process id--> "+JNachos.getCurrentProcess().getPid()+
					" Trying to join process with process id--> "+Machine.readRegister(4));
								
			boolean var = Interrupt.setLevel(false);			
			
			// Checking whether process is trying to join itself or the pid passed is invalid
			if(Machine.readRegister(4)==0 || Machine.readRegister(4)==JNachos.getCurrentProcess().getPid() || NachosProcess.allProcesses.containsKey(Machine.readRegister(4)))		
			{
				break;
			}			
			else
			{
				//If pId is valid then put current process to sleep 
				//and put the process with the specified pId in waiting processes list
				JNachos.waitingprocesstable.put(Machine.readRegister(4),JNachos.getCurrentProcess());		
				System.out.println("Current process pid "+JNachos.getCurrentProcess().getPid());
				JNachos.getCurrentProcess().sleep();					
			}
				
			Interrupt.setLevel(var);
			Machine.run();
			break;
			
		case SC_Exec:
			
			System.out.println("---------------------------EXEC---------------------------");
			
			System.out.println("Process id-->"+JNachos.getCurrentProcess().getPid());
			
			boolean savedLevel = Interrupt.setLevel(false);
			int fileAddress = Machine.readRegister(4);
			int checkVal = 1;
			String fileVal = new String();
			
			while((char)checkVal!='\0')
			{
				checkVal = Machine.readMem(fileAddress, 1);
				if((char)checkVal!='\0')
				{
					fileVal+= (char) checkVal;}
				fileAddress++;
				
			}
			
			System.out.println(fileVal);
			OpenFile executableFile = JNachos.mFileSystem.open(fileVal);
		
			if(executableFile==null)
			{
				System.out.println("No content in file");
			}
			
			JNachos.getCurrentProcess().setSpace(new AddrSpace(executableFile));
			JNachos.getCurrentProcess().getSpace().initRegisters();
			JNachos.getCurrentProcess().getSpace().restoreState();
			
			Interrupt.setLevel(savedLevel);
			Machine.run();		
			
			break;
			
		case SC_Halt:
			Debug.print('a', "Shutdown, initiated by user program.");
			Interrupt.halt();
			break;

		case SC_Exit:
			
			System.out.println("---------------------------EXIT---------------------------");
			
			System.out.println("Process id-->"+JNachos.getCurrentProcess().getPid());
									
			int arg = Machine.readRegister(4);
			
			System.out
			.println("Current Process " + JNachos.getCurrentProcess().getName() + " exiting with code " + arg);	
			
			
			//While exiting any process check whether any other process is waiting for it to finish 
			if(JNachos.waitingprocesstable.contains(JNachos.getCurrentProcess()))
			{
				NachosProcess tempNachosProcess = JNachos.waitingprocesstable.remove(JNachos.getCurrentProcess().getPid());
				NachosProcess.allProcesses.remove(JNachos.getCurrentProcess().getPid());
				JNachos.waitingprocesstable.get(JNachos.getCurrentProcess().getPid()).savingRegisters(2, arg);
				Scheduler.readyToRun(tempNachosProcess);
			}
			
			JNachos.getCurrentProcess().finish();		
			
						
			break;

		default:
			Interrupt.halt();
			break;
		}
	}
}
