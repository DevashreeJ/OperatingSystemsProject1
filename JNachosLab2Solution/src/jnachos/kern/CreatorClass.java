package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Machine;

public class CreatorClass implements VoidFunctionPtr{
	
	@Override
	public void call(Object arg) {

		JNachos.getCurrentProcess().restoreUserState();
		
		JNachos.getCurrentProcess().getSpace().restoreState();
		
		Machine.run();
		
		assert(false);
	}

}
