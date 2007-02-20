package org.codehaus.groovy.testng;


import groovy.lang.GroovyClassLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.TestNGCommandLineArgs;


/**
 * The Test'N'Groove command line support class.
 * 
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class GroovyTestNG {
    private GroovyClassLoader gcl;
    private List<String> inputOptions;
    private List<File> files = new ArrayList<File>();
    private List<String> outputOptions = new ArrayList<String>();
    
    public GroovyTestNG(String[] args) {
        inputOptions = expand(args);
        gcl = new GroovyNGClassLoader();
    }
    
    public int run() {
        initializeOptions();
        if(files.size() == 0) return TestNG.HAS_NO_TEST;
        Class[] testclasses = loadTests(files);
//        dump(testclasses);
        TestNG testng = new TestNG();
        testng.configure(TestNGCommandLineArgs.parseCommandLine(
                this.outputOptions.toArray(new String[this.outputOptions.size()])));
        testng.setTestClasses(testclasses);
        setSuiteAndTestNames(testng, files);
        testng.run();
        return testng.getStatus();
    }

    private void setSuiteAndTestNames(TestNG testng, List<File> files) {
        String testname = null;
        StringBuffer suitename = new StringBuffer();
        
        if(files.size() == 1) {
            File f = files.get(0);
            testname= f.getName();
            suitename.append("Single script suite (")
                    .append(f.getName())
                    .append(")");
        }
        else {
            testname= "Multi-script test";
            suitename.append("Suite (");
            for(int i = 0; i < 2; i++) {
                suitename.append(files.get(i).getName())
                        .append(",");
            }
            suitename.append("...)");
        }
        
        testng.setDefaultTestName(testname);
        testng.setDefaultSuiteName(suitename.toString());
    }
    
    private void initializeOptions() {
        int optiondsIdx = -1;
        for(int i = this.inputOptions.size() - 1; i >= 0; i--) {
            File f = getScript(this.inputOptions.get(i));
            if(f != null) {
                files.add(f);
            }
            else {
                optiondsIdx = i;
                break;
            }
        }
        
        this.outputOptions = this.inputOptions.subList(0, optiondsIdx + 1);
    }
    
    private List<String> expand(String[] args) {
        List<String> result= new ArrayList<String>();
        String argsFile= null;
        for(String opt: args) {
            if(opt.startsWith("@")) {
                argsFile= opt.substring(1);
            }
            else {
                result.add(opt);
            }
        }
        
        if(null != argsFile) {
            result.addAll(readFile(argsFile));
        }
        return result;
    }
    
    private List<String> readFile(String filePath) {
        List<String> lines = new ArrayList<String>();

        try {
          BufferedReader bufRead = new BufferedReader(new FileReader(filePath));

          String line;
          while ((line = bufRead.readLine()) != null) {
            lines.add(line);
          }

          bufRead.close();
        }
        catch (IOException ioex) {
            throw new RuntimeException("Cannot read argument file:" + filePath, ioex);
        }

        return lines;
    }
    
    private Class[] loadTests(List<File> files) {
        for(Iterator<File> it = files.iterator(); it.hasNext(); ) {
            File f = it.next();
            try {
                gcl.parseClass(f);
            }
            catch(IOException ioex) {
                System.err.print("Cannot load script " + f.getAbsolutePath() + " Cause:" + ioex.getMessage());
            }
        }
        
        Class[] loadedClasses = gcl.getLoadedClasses();
        List<Class> classes = new ArrayList<Class>();
        for(Class cls: loadedClasses) {
            if(accept(cls.getName())) {
                classes.add(cls);
            }
        }
        
        return classes.toArray(new Class[classes.size()]);
    }
    
    private boolean accept(String className) {
        // no need to pass Closure classes
        if(className.indexOf("$_closure_closure") != -1) return false;
        // no need to pass GString classes
        int len = className.length() - 1;
        boolean isGString = false;
        for(int i = len; i >= 0; i--) {
            if(className.charAt(i) < '0' || className.charAt(i) > '9') {
                if(className.charAt(i) == '$') {
                    isGString = true;
                }
                else {
                    break;
                }
            }
        }
        
        return !isGString;
    }
    
    private File getScript(String string) {
        if (!string.endsWith(".groovy")) return null;
        File f = new File(string);
        return (f.exists() && f.isFile() ? f : null);
    }
    
    private void dump(Class[] testclasses) {
        int i= 0;
        for(String s: this.outputOptions) {
            System.out.println("Opt" + (i++) + " :" + s);
        }
        for(Iterator it = this.files.iterator(); it.hasNext(); ) {
            System.out.println("Script :" + it.next());
        }
        for(Class clazz: testclasses) {
            System.out.println("Class  :" + clazz.getName());
        }
    }
    
    public static void main(String[] args) {
        int exitCode = new GroovyTestNG(args).run();
        System.exit(exitCode);
    }
    
    /**
     * A simple GCL extension that pre-imports TestNG annotations.
     */
    private static class GroovyNGClassLoader extends GroovyClassLoader {
        protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource source) {
            CompilationUnit cu = super.createCompilationUnit(config, source);
            cu.addPhaseOperation(new CompilationUnit.SourceUnitOperation() {
                public void call(SourceUnit source) throws CompilationFailedException {
                    ModuleNode module = source.getAST();
                    module.addImportPackage("org.testng.annotations.");
                    module.addImport("Assert", new ClassNode(Assert.class));
                }
            }, Phases.CONVERSION);
            
            return cu;
        }
    }
}
