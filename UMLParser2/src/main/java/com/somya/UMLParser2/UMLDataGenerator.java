package com.somya.UMLParser2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.plantuml.SourceStringReader;

public class UMLDataGenerator {
	private Map<String, ClassInfoData> classNameToObjMap = new HashMap<String, ClassInfoData>();
	String cn_;
	String fn_;
	
	List<String> classFnSeq = new ArrayList<String>();		// class to another function direction
	Set<String> classSet = new HashSet<String>();			// set of participants
	List<String> classSeq = new ArrayList<String>();		// participants should be in some order so that seq diagram looks beautiful

	public UMLDataGenerator(String cName, String fName, Map<String, ClassInfoData> classNameToObjMap) {
		this.classNameToObjMap = classNameToObjMap;
		this.cn_ = cName;
		this.fn_ = fName;
	}


	public String populateUMLFormat() {
		String umlBody;
		umlBody = insertStartTag();
		umlBody += "\n";

		this.generateFnCalls(cn_, fn_);

		// generating participants
		for (String c: classSeq) {
			umlBody += "participant " + c + " \n";
		}

		for (String cf : classFnSeq) {
			umlBody += cf + " \n";
		}

		umlBody += inserEndTag();
		return umlBody;
	}
	
	private String getFuncSignature(String cn, String fn) {
		ClassInfoData cc = classNameToObjMap.get(cn);
		if (cc != null) {
			MethodInfo mm = cc.funcList.get(fn);
			if (mm != null) {
				return mm.signature;
			}
		}
		return fn;
	}
	
	private void generateFnCalls(String cn, String fn) {
		ClassInfoData c = classNameToObjMap.get(cn);
		if (c != null) {
			MethodInfo m = c.funcList.get(fn);
			if (m != null) {
				if (!classSet.contains(cn)) {
					classSeq.add(cn);
					classSet.add(cn);
				}
				for (functionCallInfo fc: m.mCalls) {
					if (!fc.functionName.equals(fn)) {
						classFnSeq.add(cn + " -> " + fc.className + ": " + getFuncSignature(fc.className, fc.functionName));
						this.generateFnCalls(fc.className, fc.functionName);
	//					classFnSeq.add(fc.className +  " -> " + cn);
					}
				}
			}
		}
	}

	public void generateClassDiag(String umlFormat, String umlImagePath) throws FileNotFoundException {
		OutputStream png = new FileOutputStream(umlImagePath);
		SourceStringReader reader = null;
		reader = new SourceStringReader(umlFormat);

		try {
			String desc = reader.generateImage(png);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private String insertStartTag() {
		String src = "@startuml\n";
		src += "skinparam nodesep 500\n";
		src += "skinparam ranksep 500\n";
		return src;
	}

	private String inserEndTag() {
		return "\n@enduml";
	}

}
