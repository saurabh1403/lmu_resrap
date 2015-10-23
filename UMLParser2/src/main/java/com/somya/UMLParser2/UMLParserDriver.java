//TODO: A class can only extend one class, so remove the implements list
//TODO: Add support for LIST, SET, UNORDERED SET, MAP and QUEUE
//TODO: Check on lollipop interfaces from Plant UML

package com.somya.UMLParser2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ParseException;

public class UMLParserDriver {

	public static void main(String [] args) throws ParseException, IOException {
//		if (args.length == 0) {
//            System.out.println("no arguments were given.");
//            return;
//        }

//		String classPath = args[0];
//		String umlImagePath = args[1];	

//		String classPath = "/Users/saurabg/saurabg/Personal/sjsu/sem1_202/UMLParser2/lmu_resrap/UMLParser2/src/main/java/com/somya/UMLParser2";
//		String cn = "UMLParserDriver";
//		String fn = "main";

		String classPath = "/Users/saurabg/saurabg/Personal/sjsu/sem1_202/midterm1-junit-problem/junit-problem";
		String cn = "MTest";
		String fn = "test4";

		String umlImagePath = "/Users/saurabg/saurabg/Personal/sjsu/sem1_202/TestInput/T2/output.png";

		//obtain all the java files path in a list
		JavaFilesIterator iter = new JavaFilesIterator(classPath);
		List<String> javaFilePaths = new ArrayList<String>();
		javaFilePaths = iter.iterateClassFolder();
		
		ClassInfoData cinfo = new ClassInfoData();
		Map<String, ClassInfoData> classNameToObjMap = new HashMap <String, ClassInfoData>();
		
		for (int i = 0; i < javaFilePaths.size(); i++) {
			String filePath = javaFilePaths.get(i);			
			JavaFileParser jp_obj = new JavaFileParser(filePath);
			ClassInfoData c_obj = new ClassInfoData();
			
			c_obj = jp_obj.populateClassInfo();
			
			//Fill the map for className to class Info
			classNameToObjMap.put(c_obj.className, c_obj);
		}
		
		JavaFileParser.pruneLibFunctions(classNameToObjMap);
		
		for (ClassInfoData c: classNameToObjMap.values()) {
			printClassInfo(c);
		}

		UMLDataGenerator uml = new UMLDataGenerator(cn, fn, classNameToObjMap);
		String umlFormat = uml.populateUMLFormat();
		System.out.println(umlFormat);
//		String umlstr = "@startuml\n  "
//				+ "actor Alice\n"
//				+ "participant Bob  \n "
//				+ "		Alice -> Bob: Authentication Request(string a, int b)\n"
//				+ "		Bob --> Alice: Authentication Response\n"
//				+ "		Alice -> Bob: Another authentication Request\n"
//				+ "		Alice <-- Bob: another authentication Response\n"
//				+ "		@enduml";
		uml.generateClassDiag(umlFormat, umlImagePath);
	}

	static public void printClassInfo(ClassInfoData c_obj){
		System.out.println("\nClassName:"+c_obj.className);
		for(Map.Entry<String, MethodInfo> entry: c_obj.funcList.entrySet()) {
			System.out.println("  > " + entry.getValue().signature);
			for (functionCallInfo fn: entry.getValue().mCalls) {
				System.out.println("       >> " + fn.className + "." + fn.functionName);
			}
		}
	}
}
