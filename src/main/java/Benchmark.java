import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;


/**
 * EL benchmark
 *
 * @author boyan
 * @Date 2011-7-13
 */
public class Benchmark {
    static int preheatCount = 1000000;
    static int runCount = 9000000;


    public static void main(String args[]) throws Exception {
        // AviatorEvaluator.setOptimize(AviatorEvaluator.COMPILE);
        // AviatorEvaluator.setTrace(true);
        // AviatorEvaluator.setTraceOutputStream(new FileOutputStream(new
        // File("aviator.log")));
        testCompile();
        testLiteral();
        testVariableExpression();
        testFunction();
        senciTestFunction();
    }


    public static void testLiteral() throws Exception {
        System.out.println("test literal arith expression ...");
        benchmark("1000+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71");
        benchmark("6.7-100>39.6 ? 5==5? 4+5:6-1 : !(100%3-39.0<27) ? 8*2-199: 100%3");
        System.out.println();
    }


    public static void testVariableExpression() throws Exception {
        System.out.println("test including variable expression ...");
        Map<String, Object> env = new HashMap<String, Object>();
        int i = 100;
        float pi = 3.14f;
        double d = -3.9;
        byte b = (byte) 4;
        boolean bool = false;
        env.put("i", i);
        env.put("pi", pi);
        env.put("d", d);
        env.put("b", b);
        env.put("bool", bool);
        benchmark("i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99 ==i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99", env);
        benchmark("pi*d+b-(1000-d*b/pi)/(pi+99-i*d)-i*pi*d/b", env);
        System.out.println();
    }


    public static void testCompile() throws Exception {
        System.out.println("test compile....");
        String exp = "i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99 ==i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99";
        testCompileAviator(exp);
        testCompileGroovy(exp);
        testCompileJEXL(exp);
        System.out.println("test compile end...");
        System.out.println();
    }

    public static void senciTestFunction() throws Exception {
        Map<String, Object> env = new HashMap<String, Object>();
        Map<String, Object> env1 = new HashMap<String, Object>();
        Map<String, Object> env2 = new HashMap<String, Object>();
        env.put("a", 1);
        env.put("b", env1);
        env.put("s", "hello world");
        env1.put("c", env2);
        env1.put("d", 5);
        env2.put("e", 4);
        System.out.println("env == " + env);
        System.out.println("字符串函数嵌套函数 02");
        System.out.println("string.contains(s,\"ld\") && string.contains(s,\"or\") && string.contains(s,\"he\") && string.length(s) >0 && string.length(s) < 1000");

        testAviator("string.contains(s,\"ld\") && string.contains(s,\"or\") && string.contains(s,\"he\") && string.length(s) >0 && string.length(s) < 1000", env);
        testGroovy("s.contains(\"ld\")  && s.contains(\"or\") && s.contains(\"he\") && s.length() >0 && s.length() < 1000", env);
        System.out.println();

        System.out.println("字符串正则匹配测试");
        System.out.println("email=killme2008@gmail.com, email=~/([\\w0-8]+)@\\w+[\\.\\w+]+/ ? $1 : 'unknow' ");
        String email = "killme2008@gmail.com";
        env.put("email", email);
        testAviator("email=~/([\\w0-8]+)@\\w+[\\.\\w+]+/ ? $1 : 'unknow' ", env);
        testGroovy("email=~/([\\w0-8]+)@\\w+[\\.\\w+]+/ ? $1 : 'unknow' ", env);
        System.out.println();

        System.out.println("数学函数测试");
        System.out.println("math.sqrt(math.pow(math.abs(a1),4)) == 4");
        env.put("a1", -2);
        testAviator("math.sqrt(math.pow(math.abs(a1),4)) == 4", env);
        testGroovy("Math.sqrt(Math.pow(Math.abs(a1),4)) == 4", env);
        System.out.println();

        System.out.println("Sequence 函数（集合处理）测试");
        testAviator("originalList = seq.list(9,1,-1,2,2,2,0,111111,3); " +
                "is_empty(originalList) && include(originalList,0) && seq.get(sort(originalList),0) == -1 ", env);
        testGroovy("originalList =[9,1,-1,2,2,2,0,111111,3]; " +
                "originalList.isEmpty() && originalList.contains(0) && originalList.get(originalList.sort(),0) == -1", env);
        System.out.println();

        System.out.println("嵌套 list 对 map 结构测试");
        testAviator("mmp = seq.map(\"a\", seq.list(seq.map(\"aa1\", 6661)),4,seq.list(seq.map(\"aa2\", 6662)),7,seq.map(\"aa3\", 6663)); " +
                "seq.add(mmp, 666, 666); for x in mmp { assert(seq.contains_key(mmp, 666)); assert(666 == seq.get(mmp, 666)); } " +
                " tmp = seq.get(mmp, 4); return tmp; ", env);

        testGroovy("mmp = [\"a\":[[\"aa1\":6661]],4:[[\"aa2\":6662]],7:[\"aa3\":6663]];" +
                "mmp.put(666,666); for(x in mmp){ assert mmp.containsKey(666); assert 666 == mmp.get(666); } " +
                " tmp = mmp.get(4); return tmp; ", env);
        System.out.println();

        System.out.println("嵌套 map 对 list 结构测试");
        testAviator("vaList = seq.list(\"a\", seq.map(\"aa1\",seq.map(\"aaa1\", 6661)),4,seq.map(\"aa2\",seq.map(\"aaa2\", 6662)),7,seq.list(\"aa3\", 6663)); " +
                "seq.add(vaList, 666); assert(include(vaList, 666)); assert(666 == seq.get(vaList, 6));  " +
                " tmp = seq.get(vaList,1); return tmp; ", env);

        testGroovy("vaList = [\"a\",[\"aa1\":[\"aaa1\":6661]],4,[\"aa2\":[\"aaa2\":6662]],7,[\"aa3\",6663]];" +
                "vaList.add(666); assert vaList.contains(666); assert 666 == vaList.get(6); " +
                "tmp = vaList.get(1); return tmp;", env);
        System.out.println();

//        System.out.println("强制类型转换测试");
//        env.put("intString", "666");
//        env.put("doubleString", "5.8");
//        final String exp5 = "long(intString) > 50 && double(intString) > 50 && boolean(doubleString) && str(doubleString) == \"5.8\" && bigint(intString) > 50 ";
//        testAviator(exp5, env);
//        testGroovy("new Long(intString) > 50 && new Double(intString) > 50 && new Boolean(doubleString) && new String(doubleString) == \"5.8\" && new BigInteger(intString) > 50", env);
//        System.out.println();
    }

    public static void testFunction() throws Exception {
        Map<String, Object> env = new HashMap<String, Object>();
        Map<String, Object> env1 = new HashMap<String, Object>();
        Map<String, Object> env2 = new HashMap<String, Object>();
        env.put("a", 1);
        env.put("b", env1);
        env.put("s", "hello world");
        env1.put("c", env2);
        env1.put("d", 5);
        env2.put("e", 4);
        System.out.println("env == " + env);
        //日期函数
        final String exp2 = " new java.util.Date()";
        System.out.println("expression:" + exp2);
        testAviator("sysdate()", env);
        testGroovy(exp2, env);
        testJEXL("new(\"java.util.Date\")", env);
        System.out.println();

        //字符串函数
        final String exp1 = "s.substring(b.d)";
        System.out.println("expression:" + exp1);
        testAviator("string.substring(s,b.d)", env);
        testGroovy(exp1, env);
        testJEXL(exp1, env);
        System.out.println();

        //字符串函数嵌套函数01
        final String exp3 = "s.substring(b.d).substring(a,b.c.e)";
        System.out.println("expression:" + exp3);
        testAviator("string.substring(string.substring(s,b.d),a,b.c.e)", env);
        testGroovy(exp3, env);
        testJEXL(exp3, env);
        System.out.println();
    }


    private static void benchmark(String exp) throws Exception {
        System.out.println("expression:" + exp);
        testAviator(exp);
        testGroovy(exp);
        testJEXL(exp);
        System.out.println();
    }


    private static void benchmark(String exp, Map<String, Object> env) throws Exception {
        System.out.println("expression:" + exp);
        testAviator(exp, env);
        testGroovy(exp, env);
        testJEXL(exp, env);
        System.out.println();
    }


    private static void testAviator(String exp, Map<String, Object> env) throws Exception {
        Expression expression = AviatorEvaluator.compile(exp);
        Object result = null;
        System.out.println("预热 ing.....");
        for (int i = 0; i < preheatCount; i++) {
            result = expression.execute(env);
        }
        System.out.println("预热 end..... , 开始计算耗时");
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            result = expression.execute(env);
        }
        System.out.println(result + ",aviator duration:" + (System.currentTimeMillis() - start));
    }


    private static void testCompileAviator(String exp) throws Exception {
        long start = System.currentTimeMillis();
        Expression expression = null;
        for (int i = 0; i < runCount / 10000; i++) {
            expression = AviatorEvaluator.compile(exp);
        }
        System.out.println(expression + " compile aviator duration:" + (System.currentTimeMillis() - start));
    }


    private static void testAviator(String exp) throws Exception {
        Expression expression = AviatorEvaluator.compile(exp);
        Object result = null;
        System.out.println("预热 ing.....");
        for (int i = 0; i < preheatCount; i++) {
            result = expression.execute();
        }
        System.out.println("预热 end..... , 开始计算耗时");
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            result = expression.execute();
        }
        System.out.println(result + ",aviator duration:" + (System.currentTimeMillis() - start));
    }


    public static void testGroovy(String exp) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(exp);
        GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

        Object result = null;
        System.out.println("预热 ing.....");
        for (int i = 0; i < preheatCount; i++) {
            result = groovyObject.invokeMethod("run", null);
        }
        System.out.println("预热 end..... , 开始计算耗时");
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            result = groovyObject.invokeMethod("run", null);
        }

        System.out.println(result + ",groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testCompileGroovy(String exp) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

        GroovyObject groovyObject = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount / 10000; i++) {
            Class clazz = loader.parseClass(exp);
            groovyObject = (GroovyObject) clazz.newInstance();
        }

        System.out.println(groovyObject + " compile groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testGroovy(String exp, Map<String, Object> env) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(exp);
        GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

        System.out.println("预热 ing.....");
        Object result = null;
        for (int i = 0; i < preheatCount; i++) {
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                groovyObject.setProperty(entry.getKey(), entry.getValue());
            }
            result = groovyObject.invokeMethod("run", null);
        }
        System.out.println("预热 end..... , 开始计算耗时");
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                groovyObject.setProperty(entry.getKey(), entry.getValue());
            }
            result = groovyObject.invokeMethod("run", null);
        }

        System.out.println(result + ",groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testJEXL(String exp) throws Exception {
        JexlEngine jexl = new JexlEngine();

        Object result = null;
        final org.apache.commons.jexl2.Expression e = jexl.createExpression(exp);
        System.out.println("预热 ing.....");
        for (int i = 0; i < preheatCount; i++) {
            result = e.evaluate(null);
        }
        System.out.println("预热 end..... , 开始计算耗时");
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            result = e.evaluate(null);
        }

        System.out.println(result + ",jexl duration:" + (System.currentTimeMillis() - start));
    }


    public static void testJEXL(String exp, Map<String, Object> env) throws Exception {
        JexlEngine jexl = new JexlEngine();
        JexlContext context = new MapContext();
        for (Map.Entry<String, Object> entry : env.entrySet()) {
            context.set(entry.getKey(), entry.getValue());
        }
        Object result = null;
        final org.apache.commons.jexl2.Expression e = jexl.createExpression(exp);
        System.out.println("预热 ing.....");
        for (int i = 0; i < preheatCount; i++) {
            result = e.evaluate(context);
        }
        System.out.println("预热 end..... , 开始计算耗时");
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount; i++) {
            result = e.evaluate(context);
        }

        System.out.println(result + ",jexl duration:" + (System.currentTimeMillis() - start));
    }


    public static void testCompileJEXL(String exp) throws Exception {
        JexlEngine jexl = new JexlEngine();
        org.apache.commons.jexl2.Expression result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runCount / 10000; i++) {
            result = jexl.createExpression(exp);
        }

        System.out.println(result + " compile jexl duration:" + (System.currentTimeMillis() - start));
    }

}
