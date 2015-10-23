package com.somya.UMLParser2;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
	String name;
	String signature;
	String scope;
	String returnType;
	
	List<functionCallInfo> mCalls = new ArrayList<functionCallInfo>();
}

// info about the class and the function which is called. 
class functionCallInfo {
	String className;
	String functionName;
	
	public functionCallInfo(String cName, String fName) {
		this.className = cName;
		this.functionName = fName;
	}
}