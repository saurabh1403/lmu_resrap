package com.somya.UMLParser2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class JavaFileParser extends VoidVisitorAdapter<ClassInfoData> {
	private CompilationUnit cu;
	
	String currMethodName = "";
	Map<String, String> currMethodLocalObjects = new HashMap<String, String>();
	Map<String, String> memberObjects = new HashMap<String, String>();

	public JavaFileParser(String filePath) throws ParseException, IOException {
		FileInputStream in = new FileInputStream(filePath);

		// parse the file
		cu = JavaParser.parse(in);
		in.close();
	}

	public ClassInfoData populateClassInfo() {
		ClassInfoData cdata = new ClassInfoData();
		visit(cu, cdata);
		return cdata;
	}

    @Override
    public void visit(final MethodCallExpr n, final ClassInfoData arg){

    	String mn = n.getName();     	// get the fn name which is called

    	if (n.getScope() != null) {
	    	String on = n.getScope().toString();    	// get the obj name on which it is called
	    	
	    	functionCallInfo fn = null;
	    	if (this.currMethodLocalObjects.containsKey(on)) {
	    		fn = new functionCallInfo(this.currMethodLocalObjects.get(on), mn);
	    	} else if (this.memberObjects.containsKey(on)) {
	    		fn = new functionCallInfo(this.memberObjects.get(on), mn);
	    	} else {
	    		// should never happen on this planet
	    		System.out.println("no obj for: " + on + " for fn: " + mn);
	    	}
	    	
	    	if (fn != null && arg.funcList.containsKey(this.currMethodName)) {
	    		arg.funcList.get(this.currMethodName).mCalls.add(fn);
	    	}
    	}
        super.visit(n, arg);
    }

	@Override
	public void visit(VariableDeclarationExpr n, ClassInfoData arg) {
		// here you can access the attributes of the method.
		// this method will be called for all methods in this 
		// CompilationUnit, including inner class methods
		for (VariableDeclarator id: n.getVars()) {
			this.currMethodLocalObjects.put(id.getId().getName(), n.getType().toString());
		}
		arg.test();
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, ClassInfoData cid) {
		// prints current class Name
		cid.className = n.getName();
		cid.test();
		super.visit(n, cid);
	}


	@Override
	public void visit(MethodDeclaration n, ClassInfoData cid) {
		MethodInfo m = new MethodInfo();

		// get method name
		m.name = n.getName();
		// get return type
		m.returnType = n.getType().toString();
		String sig = n.getDeclarationAsString(false, false, false);
		m.signature = sig.substring(sig.indexOf(" "));

		this.currMethodName = n.getName();
		currMethodLocalObjects.clear();
		
		// get the parameters of the function in form of a list
		java.util.List<Parameter> ret = n.getParameters();
		for (Parameter elem : ret) {
			this.currMethodLocalObjects.put(elem.getId().toString(), elem.getType().toString());
		}

		cid.test();
		cid.funcList.put(n.getName(),  m);
		super.visit(n, cid);
	}

	@Override
	public void visit(FieldDeclaration n, ClassInfoData cid) {
		for (VariableDeclarator elem : n.getVariables()) {
			this.memberObjects.put(elem.getId().getName(), n.getType().toString());
		}
		cid.test();
	}

	private static void pruneLibfunctionsFromMethod(MethodInfo m, Map<String, ClassInfoData> classes) {
		Iterator<functionCallInfo> itr = m.mCalls.iterator();
		while(itr.hasNext()) {
			functionCallInfo fn = itr.next();
			if (!classes.containsKey(fn.className)) {
				itr.remove();
			}
		}
	}
	
	public static void pruneLibFunctions(Map<String, ClassInfoData> classes) {
		for (Map.Entry<String, ClassInfoData> entry: classes.entrySet()) {
			for (MethodInfo m: entry.getValue().funcList.values()) {
				pruneLibfunctionsFromMethod(m, classes);
			}
		}
		
	}
}
