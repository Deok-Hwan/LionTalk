package Application.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public  class MessageProcessing {

	public static  HashMap<String, MessageFormat> index = new HashMap<>();
	
	public synchronized static HashMap<String, MessageFormat> getIndex() {
		return index;
	}
	
}
