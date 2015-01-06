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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {

    /**
     * Helper application for example classes.
     * Lists available classes, and provides shorthand invocation.
     * For example:<br>
     * <code>java -jar commons-net-examples-m.n.jar FTPClientExample -l host user password</code>
     *
     * @param args the first argument is used to name the class; remaining arguments
     * are passed to the target class.
     * @param sourcefile 
     * @throws Exception
     * @throws Exception
     */
    public static void main(String[] args) throws Throwable  {
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        Map<String, String> map = new HashMap<String, String>();
        final boolean noArgsProvided = args.length == 0;
        if ( codeSource != null) {
            final String sourceFile = codeSource.getLocation().getFile();
            if (sourceFile.endsWith(".jar")) {
                if (noArgsProvided) {
                    System.out.println("Usage: java -jar commons-net-examples-m.n.jar <exampleClass> <exampleClass parameters>");
                    System.out.println("\nClasses found in the jar:");
                }
                JarFile jf = new JarFile(sourceFile);
                Enumeration<JarEntry> e = jf.entries();
                while (e.hasMoreElements()) {
                  JarEntry je = e.nextElement();
                  String name = je.getName();
                  processFileName(name, map, noArgsProvided);
                }
                jf.close();
            } else {
                if (noArgsProvided) {
                    System.out.println("Usage: mvn -q exec:java  -Dexec.arguments=<exampleClass>,<exampleClass parameters>");
                    System.out.println("\nClasses found in the jar:");
                }
                File examples = new File(sourceFile, "examples");
                if (examples.exists()) {
                    scanForClasses(sourceFile.length(), examples, map, noArgsProvided);
                }
            }
        } else {
            if (noArgsProvided) {
                System.out.println("Usage: java -jar commons-net-examples-m.n.jar <exampleClass> <exampleClass parameters>");
            }
        }

        if (noArgsProvided) {
            return;
        }

        String shortName = args[0];
        String fullName = map.get(shortName);
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

    private static void scanForClasses(int rootLength, File current, Map<String, String> map, boolean printAlias) {
        for(File file : current.listFiles()) {
            if (file.isDirectory()) {
                scanForClasses(rootLength, file, map, printAlias);
            } else {
                
                processFileName(file.getPath().substring(rootLength), map, printAlias);
                
            }
        }
        
    }

    private static void processFileName(String name, Map<String, String> map, boolean printAlias) {
        if (!name.endsWith(".class")
                || name.contains("$") // subclasses
                // TODO use reflection to eliminate non-main classes?
                // however that would entail loading the class.
                || name.equals("examples/nntp/NNTPUtils.class") // no main class
                || name.equals("examples/util/IOUtil.class") // no main class
                || name.equals("examples/mail/IMAPUtils.class") // no main class
                || name.equals("examples/Main.class")) { // ourself
            return;
        }
        name = name.replace(".class", "");
        final int lastSep = name.lastIndexOf('/');
        final String alias = name.substring(lastSep+1);
        if (printAlias) {
            System.out.printf("%-25s %s%n",alias,name);
        }
        map.put(alias, name);
    }
}
