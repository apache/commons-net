/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package examples;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Helper application for example classes.
 */
public class Main {

    /**
     * Helper application for example classes.
     * Lists available classes, and provides shorthand invocation.
     * For example:<br>
     * <code>java -jar commons-net-examples-m.n.jar FTPClientExample -l host user password</code>
     *
     * @param args the first argument is used to name the class; remaining arguments
     * are passed to the target class.
     * @throws Throwable if an error occurs
     */
    public static void main(String[] args) throws Throwable  {
        final Properties fp = new Properties();
        final InputStream ras = Main.class.getResourceAsStream("examples.properties");
        if (ras != null) {
            fp.load(ras);
        } else {
            System.err.println("[Cannot find examples.properties file, so aliases cannot be used]");
        }
        if (args.length == 0) {
            if (Thread.currentThread().getStackTrace().length > 2) { // called by Maven
                System.out.println("Usage: mvn -q exec:java  -Dexec.arguments=<alias or" +
                                    " exampleClass>,<exampleClass parameters> (comma-separated, no spaces)");
                System.out.println("Or   : mvn -q exec:java  -Dexec.args=\"<alias" +
                                    " or exampleClass> <exampleClass parameters>\" (space separated)");
            } else {
                if (fromJar()) {
                    System.out.println(
                        "Usage: java -jar commons-net-examples-m.n.jar <alias or exampleClass> <exampleClass parameters>");
                } else {
                    System.out.println(
                        "Usage: java -cp target/classes examples/Main <alias or exampleClass> <exampleClass parameters>");
                }
            }
            @SuppressWarnings("unchecked") // property names are Strings
            List<String> l = (List<String>) Collections.list(fp.propertyNames());
            if (l.isEmpty()) {
                return;
            }
            Collections.sort(l);
            System.out.println("\nAliases and their classes:");
            for(String s : l) {
                System.out.printf("%-25s %s%n",s,fp.getProperty(s));
            }
            return;
        }

        String shortName = args[0];
        String fullName = fp.getProperty(shortName);
        if (fullName == null) {
            fullName = shortName;
        }
        fullName = fullName.replace('/', '.');
        try {
            Class<?> clazz = Class.forName(fullName);
            Method m = clazz.getDeclaredMethod("main", new Class[]{args.getClass()});
            String[] args2 = new String[args.length-1];
            System.arraycopy(args, 1, args2, 0, args2.length);
            try {
                m.invoke(null, (Object)args2);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause != null) {
                    throw cause;
                } else {
                    throw ite;
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    private static boolean fromJar() {
        final CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        if ( codeSource != null) {
            return codeSource.getLocation().getFile().endsWith(".jar");
        }
        return false; // No idea if this can happen
    }
}
