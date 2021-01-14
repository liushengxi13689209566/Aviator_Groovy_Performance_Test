import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;


public class Benchmark {
    static int count = 10000000;


    public static void main(String args[]) throws Exception {
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
        final String exp =
                "i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99 ==i * pi + (d * b - 199) / (1 - d * pi) - (2 + 100 - i / pi) % 99";
        // testGroovy(exp, env);
        // testAviator(exp, env);
        // // testIKExpression(exp, env);
        // testLiteral();
        // testCompileIKExpression(exp);
        /*
         * testCompileAviator(exp); testCompileGroovy(exp);
         */
        // testAviator("sysdate()");
        // testGroovy("new java.util.Date()");
        // testIKExpression("$SYSDATE()");
        testLiteral();

    }


    public static void testLiteral() throws Exception {

        System.out.println("test literal arith expression ...");

        benchmark("1000+100.0*99-(600-3*15)/(((68-9)-3)*2-100)+10000%7*71");

        System.out.println("test literal logic expression ...");
        benchmark("6.7-100>39.6 ? 5==5? 4+5:6-1 : !(100%3-39.0<27) ? 8*2-199: 100%3");
    }


    private static void benchmark(String exp) throws Exception {
        testAviator(exp);
        testGroovy(exp);
    }


    private static void testAviator(String exp, Map<String, Object> env) throws Exception {
        Expression expression = AviatorEvaluator.compile(exp);
        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = expression.execute(env);
        }
        System.out.println(result + ",aviator duration:" + (System.currentTimeMillis() - start));
    }


    private static void testCompileAviator(String exp) throws Exception {
        long start = System.currentTimeMillis();
        Expression expression = null;
        for (int i = 0; i < count; i++) {
            expression = AviatorEvaluator.compile(exp);
        }
        System.out.println(expression + " compile aviator duration:" + (System.currentTimeMillis() - start));
    }


    private static void testAviator(String exp) throws Exception {
        Expression expression = AviatorEvaluator.compile(exp);
        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = expression.execute();
        }
        System.out.println(result + ",aviator duration:" + (System.currentTimeMillis() - start));
    }


    public static void testGroovy(String exp) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(exp);
        GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            result = groovyObject.invokeMethod("run", null);
        }

        System.out.println(result + ",groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testCompileGroovy(String exp) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

        GroovyObject groovyObject = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            Class clazz = loader.parseClass(exp);
            groovyObject = (GroovyObject) clazz.newInstance();
        }

        System.out.println(groovyObject + " compile groovy duration:" + (System.currentTimeMillis() - start));
    }


    public static void testGroovy(String exp, Map<String, Object> env) throws Exception {
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(exp);
        GroovyObject groovyObject = (GroovyObject) clazz.newInstance();

        Object result = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            for (Map.Entry<String, Object> entry : env.entrySet()) {
                groovyObject.setProperty(entry.getKey(), entry.getValue());
            }
            result = groovyObject.invokeMethod("run", null);
        }

        System.out.println(result + ",groovy duration:" + (System.currentTimeMillis() - start));
    }

}
