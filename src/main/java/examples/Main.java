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
     * For example:<br/>
     * <code>java -jar commons-net-examples-m.n.jar FTPClientExample -l host user password</code>
     *
     * @param args the first argument is used to name the class; remaining arguments
     * are passed to the target class.
     * @throws Exception
     * @throws Exception
     */
    public static void main(String[] args) throws Exception  {
        if (args.length==0) {
            System.out.println("Usage: java -jar examples.jar <exampleClass> <exampleClass parameters>");
        }
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        Map<String, String> map = new HashMap<String, String>();
        if ( codeSource != null) {
            final String sourceFile = codeSource.getLocation().getFile();
            if (sourceFile.endsWith(".jar")) {
                if (args.length==0) {
                    System.out.println("\nClasses found in the jar:");
                }
                JarFile jf = new JarFile(sourceFile);
                Enumeration<JarEntry> e = jf.entries();
                while (e.hasMoreElements()) {
                  JarEntry je = e.nextElement();
                  String name = je.getName();
                  if (!name.endsWith(".class")
                          || name.contains("$") // subclasses
                          || name.equals("examples/nntp/NNTPUtils.class") // no main class
                          || name.equals("examples/util/IOUtil.class") // no main class
                          || name.equals("examples/Main.class")) {
                      continue;
                  }
                  name = name.replace(".class", "");
                  int lastSep = name.lastIndexOf('/');
                  String alias = name.substring(lastSep+1);
                  if (args.length==0) {
                      System.out.printf("%-25s %s%n",alias,name);
                  }
                  map.put(alias, name);
                }
                jf.close();
            }
        }

        if (args.length==0) {
            return;
        }

        String shortName = args[0];
        String fullName = map.get(shortName);
        if (fullName == null) {
            fullName = shortName;
        }
        fullName = fullName.replace('/', '.');
        Class<?> clazz = Class.forName(fullName);
        Method m = clazz.getDeclaredMethod("main", new Class[]{args.getClass()});
        String[] args2 = new String[args.length-1];
        System.arraycopy(args, 1, args2, 0, args2.length);
        m.invoke(null, (Object)args2);
    }
}
