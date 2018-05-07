/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.library;

import com.qwazr.library.annotations.Library;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CustomTest {

    @Library("custom")
    private CustomLibrary custom;

    @Library("customAbstract")
    private CustomAbstractLibrary customAbstract;

    @Library("customPassword")
    private CustomPasswordLibrary customPassword;

    private static LibraryManager libraryManager;

    @BeforeClass
    public static void beforeClass() throws IOException {
        final Path dataDirectory = Files.createTempDirectory("library-test");
        libraryManager =
                new LibraryManager(dataDirectory, Arrays.asList(Paths.get("src/test/resources/etc/library.json")));
    }

    @Before
    public void before() {
        libraryManager.getService().inject(this);
    }

    @Test
    public void checkCustom() {
        Assert.assertNotNull(custom);
        Assert.assertTrue(custom.isLoaded());
        Assert.assertEquals(Integer.valueOf(12), custom.myParam);
    }

    @Test
    public void checkCustomAbstract() {
        Assert.assertNotNull(customAbstract);
        Assert.assertEquals(libraryManager, customAbstract.libraryManager);
        Assert.assertEquals(libraryManager.getDataDirectory(), customAbstract.getDataDirectory());
    }

    @Test
    public void checkCustomPassword() {
        Assert.assertNotNull(customPassword);
        Assert.assertEquals(libraryManager, customPassword.getLibraryManager());
        Assert.assertEquals("myPass", customPassword.password);
    }
}
