/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.examples;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.Test;

public class MainTest {

    private static boolean hasMainMethod(String name) {
        name = name.replace(".class", "");
        try {
            final Class<?> clazz = Class.forName(name, false, MainTest.class.getClassLoader());
            clazz.getMethod("main", String[].class);
            return true;
        } catch (final ClassNotFoundException e) {
            System.out.println("Cannot find " + name);
            return false;
        } catch (final NoSuchMethodException e) {
            return false;
        } catch (final SecurityException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void processFileName(String name, final Properties p) {
        name = name.replace(File.separatorChar, '.');
        if (!name.endsWith(".class") || name.contains("$") // subclasses
                || name.endsWith("examples.Main.class") // the initial class, don't want to add that
                || !hasMainMethod(name)) {
            return;
        }
        name = name.replace(".class", "");
        final int lastSep = name.lastIndexOf('.');
        final String alias = name.substring(lastSep + 1);
        if (p.containsKey(alias)) {
            System.out.printf("Duplicate alias: %-25s %s %s %n", alias, name, p.getProperty(alias));
        } else {
            p.setProperty(alias, name);
        }
    }

    private static void scanForClasses(final int rootLength, final File current, final Properties p) {
        final File[] files = current.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    scanForClasses(rootLength, file, p);
                } else {
                    processFileName(file.getPath().substring(rootLength), p);
                }
            }
        }
    }

    @Test
    public void checkExamplesPropertiesIsComplete() throws Exception {
        final Properties cp = scanClasses();
        final Properties fp = new Properties();
        try (final InputStream inputStream = this.getClass().getResourceAsStream("examples.properties")) {
            fp.load(inputStream);
        }
        @SuppressWarnings("unchecked") // OK
        final Enumeration<String> propertyNames = (Enumeration<String>) cp.propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String c = propertyNames.nextElement();
            final String fv = fp.getProperty(c);
            final String cv = cp.getProperty(c);
            if (fv == null) {
                System.out.printf("%-25s %s - missing from examples.properties%n", c, cv);
            } else if (!fv.equals(cv)) {
                System.out.printf("%-25s %s - expected value %s %n", c, fv, cv);
            }
        }
    }

    private Properties scanClasses() throws IOException {
        final CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        // ensure special characters are decoded OK by uing the charset
        // Use canonical path to ensure consistency with Windows
        final String sourceFile = new File(URLDecoder.decode(codeSource.getLocation().getFile(), "UTF-8")).getCanonicalPath();
        final Properties p = new Properties();
        if (sourceFile.endsWith(".jar")) {
            try (final JarFile jf = new JarFile(sourceFile)) {
                final Enumeration<JarEntry> e = jf.entries();
                while (e.hasMoreElements()) {
                    final JarEntry je = e.nextElement();
                    final String name = je.getName();
                    processFileName(name, p);
                }
            }
        } else {
            final File examples = new File(sourceFile, "org/apache/commons/net/examples"); // must match top level examples package name
            if (examples.exists()) {
                // need to add 1 to allow for path separator between root and file
                scanForClasses(sourceFile.length() + 1, examples, p);
            } else {
                fail("Could not find examples classes: " + examples.getCanonicalPath());
            }
        }
        return p;
    }
}
