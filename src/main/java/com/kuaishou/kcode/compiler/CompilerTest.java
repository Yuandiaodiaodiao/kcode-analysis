package com.kuaishou.kcode.compiler;



import javax.security.auth.kerberos.KerberosTicket;
import java.lang.reflect.Method;
import java.util.Map;



public class CompilerTest {

    JavaStringCompiler compiler;
    public static void main(String[] args){
        CompilerTest t=new CompilerTest();
        try {
            t.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            t.testCompileSingleClass();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  Class<?>   output(String source,String sourceName,String packageName){
        CompilerTest t=new CompilerTest();
        try {
            t.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            return t.CompileSingleClass(source,sourceName,packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void setUp() throws Exception {
        compiler = new JavaStringCompiler();

    }

    static final String SINGLE_JAVA = "/* a single java class to one file */  "
            + "package com.kuaishou.kcode.compiler;                                            "
            + "public class UserProxy extends User {     "
            + "    public void setName(String name) {                         "
            + "        System.out.println(\"ohhhhhhh\");                                     "
            + "    }                                                          "
            + "}                                                              ";
    public  Class<?>  CompileSingleClass(String sourceCode,String sourceName,String packageName) throws Exception {
        Map<String, byte[]> results = compiler.compile(sourceName, sourceCode);

        Class<?> clazz = compiler.loadClass(packageName, results);
        return clazz;
        // get method:
//        Method setName = clazz.getMethod("setName", String.class);
        // try instance:
//        Object obj = clazz.newInstance();

        // set:
//        setName.invoke(obj, "Fly");
        // get as user:
//        User user = (User) obj;
//        return obj;
    }
    public void testCompileSingleClass() throws Exception {
        Map<String, byte[]> results = compiler.compile("UserProxy.java", SINGLE_JAVA);

        Class<?> clazz = compiler.loadClass("com.kuaishou.kcode.compiler.UserProxy", results);
        // get method:
        Method setName = clazz.getMethod("setName", String.class);
        // try instance:
        Object obj = clazz.newInstance();
        // set:
        setName.invoke(obj, "Fly");
        // get as user:
        User user = (User) obj;

    }

    static final String MULTIPLE_JAVA = "/* a single class to many files */   "
            + "package com.kuaishou.kcode.compiler;                                            "
            + "import java.util.*;                                            "
            + "public class Multiple {                                        "
            + "    List<Bird> list = new ArrayList<Bird>();                   "
            + "    public void add(String name) {                             "
            + "        Bird bird = new Bird();                                "
            + "        bird.name = name;                                      "
            + "        this.list.add(bird);                                   "
            + "    }                                                          "
            + "    public Bird getFirstBird() {                               "
            + "        return this.list.get(0);                               "
            + "    }                                                          "
            + "    public static class StaticBird {                           "
            + "        public int weight = 100;                               "
            + "    }                                                          "
            + "    class NestedBird {                                         "
            + "        NestedBird() {                                         "
            + "            System.out.println(list.size() + \" birds...\");   "
            + "        }                                                      "
            + "    }                                                          "
            + "}                                                              "
            + "/* package level */                                            "
            + "class Bird {                                                   "
            + "    String name = null;                                        "
            + "}                                                              ";

    public void testCompileMultipleClasses() throws Exception {
        Map<String, byte[]> results = compiler.compile("Multiple.java", MULTIPLE_JAVA);

        Class<?> clzMul = compiler.loadClass("com.kuaishou.kcode.compiler.Multiple", results);
        // try instance:
        Object obj = clzMul.newInstance();
    }
}
