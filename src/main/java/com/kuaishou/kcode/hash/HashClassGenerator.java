package com.kuaishou.kcode.hash;

import com.kuaishou.kcode.ByteString;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl;
import com.kuaishou.kcode.compiler.CompilerTest;
import com.kuaishou.kcode.compiler.User;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

public class HashClassGenerator {
    static long[] powArray = new long[256];
    static int[] intpowArray = new int[256];

    static {
        powArray[0] = 1;
        intpowArray[0] = 1;
        for (int a = 1; a < 256; ++a) {
            powArray[a] = powArray[a - 1] * 31;
            intpowArray[a] = intpowArray[a - 1] * 31;
        }

    }

    public static Class<?> generateHashCoder(int[] args, HashMap<KcodeAlertAnalysisImpl.HashString, Collection<String>[]> fastHashMap) {

        HashAnalyzer.fourArray farray=null;

        StringBuilder b = new StringBuilder();
        b.append("   public int fromString(String s1, String s2) {\n");
//        b.append("this.s1 = s1;\n" +
//                "        this.s2 = s2;\n");
        int allIndex = 0;

        if (args != null) {
            farray=HashAnalyzer.tryDeletePoint(args,fastHashMap);
            for (int i = 0; i < 4; ++i) {
                allIndex += args[i];
            }
            allIndex-=args[4];
            allIndex-=args[5];
            allIndex=farray.frontA.size()+farray.frontB.size()+farray.backA.size()+farray.backB.size();
        }
        if (args == null || allIndex == 0) {
            b.append(
//                    " middle = s1.length();\n" +
//                    "            s2length = s2.length();\n" +
//                    "            length = middle + s2length;\n" +
                    "          int  hashint = s1.hashCode() * 31 + s2.hashCode();\n" +
                    "            ");
        } else {

            b.append(" char[] c1 = (char[]) THE_UNSAFE.getObject(s1, 12);\n" +
                    "            char[] c2 = (char[]) THE_UNSAFE.getObject(s2, 12);" +
                    "\nlong hashcodelong=0;" +
                    "\n");

            b.append(" int  middle = c1.length;");
            b.append("int s2length = c2.length;");
            b.append(" hashcodelong =");
            if(farray.frontA.size()>0&&farray.backA.size()>0){
                b.append("(");
            }
            char c;
            if (farray.frontA.size() > 0) {
                int lastIndex = farray.frontA.size()+farray.backA.size();
                if(farray.backA.size()==0){
                    b.append("(\n");
                }
                for(int a:farray.frontA){
                    --allIndex;
                    int mul = intpowArray[--lastIndex];
                    if (mul != 1) {
                        b.append("c1[").append(a).append("]").append("*(").append(mul).append(")+\n");
                    } else {
                        b.append("c1[").append(a).append("]").append("+\n");
                    }
                }
//                for (int a = args[4]; a < args[0]; ++a) {
//                    --allIndex;
//                    int mul = intpowArray[--lastIndex];
//                    if (mul != 1) {
//                        b.append("c1[").append(a).append("]").append("*(").append(mul).append(")+\n");
//                    } else {
//                        b.append("c1[").append(a).append("]").append("+\n");
//                    }
//                }
                b.setLength(b.length()-2);
                if(farray.backA.size()==0){
                    b.append(")*(").append(powArray[allIndex]).append("L)+");
                }else{
                    b.append("+");
                }
            }
            c=b.charAt(b.length()-1);
            while(c=='\n'||c=='+'){
                b.setLength(b.length()-1);
                c=b.charAt(b.length()-1);
            }
            b.append("+\n");
            if (farray.backA.size() > 0) {
                int lastIndex =farray.backA.size();
                for(int a:farray.backA){
                    if(a==0)continue;
                    --allIndex;
                    int mul = intpowArray[--lastIndex];
                    if (mul != 1) {
                        b.append("c1[").append("middle-").append(a).append("]").append("*(").append(mul).append(")+\n");
                    } else {
                        b.append("c1[").append("middle-").append(a).append("]").append("+\n");
                    }
                }

                for (int a = args[2]; a > 0; --a) {

                }
                b.setLength(b.length()-2);
            }
            if(farray.frontA.size()>0&&farray.backA.size()>0){
                b.append(")*(").append(powArray[allIndex]).append("L)+");
            }
            c=b.charAt(b.length()-1);
            while(c=='\n'||c=='+'){
                b.setLength(b.length()-1);
                c=b.charAt(b.length()-1);
            }
            b.append("+");
            b.append("\n");
            if (farray.frontB.size()> 0) {
                int lastIndex = farray.frontB.size()+farray.backB.size();
                for(int a:farray.frontB){
                    --allIndex;
                    int mul = intpowArray[--lastIndex];
                    if (mul != 1) {
                        b.append("c2[").append(a).append("]").append("*(").append(mul).append(")+\n");
                    } else {
                        b.append("c2[").append(a).append("]").append("+");
                    }
                }
                for (int a = args[5]; a < args[1]; ++a) {

                }
            }
            c=b.charAt(b.length()-1);
            while(c=='\n'||c=='+'){
                b.setLength(b.length()-1);
                c=b.charAt(b.length()-1);
            }
            b.append("+\n");
            if (farray.backB.size() > 0) {
                int lastIndex = farray.backB.size();
                for(int a:farray.backB){
                    if(a==0)continue;
                    --allIndex;
                    int mul = intpowArray[--lastIndex];
                    if (mul != 1) {
                        b.append("c2[").append("s2length-").append(a).append("]").append("*(").append(mul).append(")+\n");
                    } else {
                        b.append("c2[").append("s2length-").append(a).append("]").append("+\n");
                    }
                }
                for (int a = args[3]; a > 0; --a) {

                }
            }
            c=b.charAt(b.length()-1);
            while(c=='\n'||c=='+'){
                b.setLength(b.length()-1);
                c=b.charAt(b.length()-1);
            }

            b.append("\n;");
            b.append(" int  hashint=(int)hashcodelong;");
//            b.append(
//                    " int length = middle + s2length;\n" +
//                    "    int        hashint = (int) (hashcodelong % 1000000007);");
//            b.append("int        hashint = (int) (hashcodelong % 1000000007);");
        }
        b.append("return hashint;");
        b.append(" \n }\n");
        String strHashCore = b.toString();
        String allStr = sFront + strHashCore + sBottom;
        clazz = generate(allStr, "HardHashImpl2.java", "com.kuaishou.kcode.hash.HardHashImpl2");
        return clazz;
    }

    static String sFront = "package com.kuaishou.kcode.hash;\n" +
            "\n" +
            "import com.kuaishou.kcode.ByteString;\n" +
            "import com.kuaishou.kcode.KcodeAlertAnalysisImpl;\n" +
            "import sun.misc.Unsafe;\n" +
            "\n" +
            "import java.lang.reflect.Field;\n" +
            "import java.security.AccessController;\n" +
            "import java.security.PrivilegedExceptionAction;\n" +
            "\n" +
            "public final class HardHashImpl2  implements HardHashInterface{\n" +
//            "    public int length = 0;\n" +
//            "    public int middle = 0, s2length = 0;\n" +
//            "    public String s1 = null, s2 = null;\n" +
            "    public long hashcodelong = 0;\n" +
            "    public int hashint = 0;\n" +
            "    private static final Unsafe THE_UNSAFE;\n" +
            "\n" +
            "    static {\n" +
            "        try {\n" +
            "            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {\n" +
            "                public Unsafe run() throws Exception {\n" +
            "                    Field theUnsafe = Unsafe.class.getDeclaredField(\"theUnsafe\");\n" +
            "                    theUnsafe.setAccessible(true);\n" +
            "                    return (Unsafe) theUnsafe.get(null);\n" +
            "                }\n" +
            "            };\n" +
            "            THE_UNSAFE = AccessController.doPrivileged(action);\n" +
            "        } catch (Exception e) {\n" +
            "            throw new RuntimeException(\"Unable to load unsafe\", e);\n" +
            "        }\n" +
            "    }\n" +
            "    public HardHashImpl2() {\n" +
            "    }\n";
    static String sBottom = "    public int hashCode() {\n" +
            "        return hashint;\n" +
            "    }\n" +
            "\n" +
            "    public boolean equals(Object obj) {\n" +
            "        return this.hashcodelong == ((HardHashImpl2) obj).hashcodelong;\n" +
            "//            return this.hashcodelong == ((HashString) obj).hashcodelong && this.middle == ((HashString) obj).middle;\n" +
            "//        return this.length==fs.length && this.middle == fs.middle &&fs.s1.equals(s1) &&fs.s2.equals(s2);\n" +
            "    }\n" +
            "}";

    public static Class<?> test() {
        String s = "package com.kuaishou.kcode.hash;\n" +
                "\n" +
                "import com.kuaishou.kcode.hash.FashHashStringInterface;\n" +
                "import com.kuaishou.kcode.KcodeAlertAnalysisImpl;\n" +
                "import com.kuaishou.kcode.ByteString;\n" +
                "\n" +
                "public class HardCodeHash extends FashHashStringInterface {\n" +
                "\n" +
                "        static {\n" +
                "powArray = new long[256];\n" +
                "            powArray[0] = 1;\n" +
                "            for (int a = 1; a <= 200; ++a) {\n" +
                "                powArray[a] = powArray[a - 1] * 31;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        public HardCodeHash() {\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        public HardCodeHash fromByteString(ByteString bs){\n" +
                "            StringBuilder sb = new StringBuilder();\n" +
                "            for (int a = bs.offset; a < bs.middle; ++a) {\n" +
                "                sb.append((char) bs.value[a]);\n" +
                "            }\n" +
                "            s1 = sb.toString();\n" +
                "            sb.setLength(0);\n" +
                "            for (int a = bs.middle; a < bs.length; ++a) {\n" +
                "                sb.append((char) bs.value[a]);\n" +
                "            }\n" +
                "            s2 = sb.toString();\n" +
                "            fromString(s1, s2);\n" +
                "            return this;\n" +
                "        }\n" +
                "        public long hashcodelong;\n" +
                "\n" +
                "        public void fromString(String s1, String s2) {\n" +
                "            this.s1 = s1;\n" +
                "            this.s2 = s2;\n" +
                "            middle = s1.length();\n" +
                "            s2length=s2.length();\n" +
                "            length = middle + s2length;\n" +

                "            doHash();\n" +
                "        }\n" +
                "        public void doHash(){\n" +
                "            hashcodelong=s1.hashCode() * powArray[s2length] + s2.hashCode();\n" +
                "        }\n" +
                "        public int hashCode() {\n" +

                "            return (int) (hashcodelong % 1000000007);\n" +
                "        }\n" +
                "\n" +
                "        public boolean equals(Object obj) {\n" +

                "            HardCodeHash fs = (HardCodeHash) obj;\n" +
                "            return this.hashcodelong == fs.hashcodelong&& this.middle == fs.middle;\n" +
                "//        return this.length==fs.length && this.middle == fs.middle &&fs.s1.equals(s1) &&fs.s2.equals(s2);\n" +
                "        }" +
                "}\n";
        clazz = generate(s, "1", "1");
        return clazz;
    }

    public static HardHashInterface getInstance() {
        try {
            HardHashInterface f = (HardHashInterface) clazz.newInstance();
            return f;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static HardHashInterface getInstance(String s1, String s2) {
        try {
            HardHashInterface f = (HardHashInterface) clazz.newInstance();
            f.fromString(s1, s2);
            return f;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Class<?> clazz;

    public static Class<?> generate(String s, String fileName, String packageName) {
        return CompilerTest.output(s, fileName, packageName);
    }
}
