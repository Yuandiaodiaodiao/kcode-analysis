package com.kuaishou.kcode.hash;

import com.kuaishou.kcode.ByteString;
import com.kuaishou.kcode.KcodeAlertAnalysisImpl;
import com.kuaishou.kcode.compiler.CompilerTest;
import com.kuaishou.kcode.compiler.User;

import java.lang.reflect.Method;

public class HashClassGenerator {
    public static Class<?> test(){
        String s="package com.kuaishou.kcode.hash;\n" +
                "\n" +
                "import com.kuaishou.kcode.hash.FashHashStringInterface;\n"+
                "import com.kuaishou.kcode.KcodeAlertAnalysisImpl;\n" +
                "import com.kuaishou.kcode.ByteString;\n"+
                "\n" +
                "public class HardCodeHash extends FashHashStringInterface {\n" +
                "\n" +
                "        static {\n" +
                        "powArray = new long[256];\n"+
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
                "        }"+
                "}\n";
          clazz=generate(s);
          return clazz;
    }
    public static FashHashStringInterface getInstance(){
        try {
            FashHashStringInterface f=(FashHashStringInterface) clazz.newInstance();
            return f;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static FashHashStringInterface getInstance(ByteString bs){
        try {
            FashHashStringInterface f=(FashHashStringInterface) clazz.newInstance();
            f.fromByteString(bs);
            return f;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    static  Class<?> clazz;
    public static Class<?> generate(String s){
        return  CompilerTest.output(s);
    }
}
