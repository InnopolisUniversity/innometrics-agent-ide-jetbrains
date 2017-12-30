package ru.innopolis.university.innometrics.jb_plugin.models;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class CodeLocationTest extends LightCodeInsightFixtureTestCase {

    public void testBuildPath() {
        CodeLocation codeLocation = new CodeLocation();
        assertEmpty(codeLocation.buildPath());

        codeLocation.addElement(CodeLocation.CodeElementLabel.PROJ, "test_proj");
        assertEquals("PROJ:test_proj", codeLocation.buildPath());

        codeLocation.addElement(CodeLocation.CodeElementLabel.LANG, "test_lang");
        assertEquals("PROJ:test_proj|LANG:test_lang", codeLocation.buildPath());

        codeLocation.addElement(CodeLocation.CodeElementLabel.NS, "test_namespace");
        assertEquals("PROJ:test_proj|LANG:test_lang|NS:test_namespace", codeLocation.buildPath());

        codeLocation.addElement(CodeLocation.CodeElementLabel.CLASS, "test_class");
        assertEquals("PROJ:test_proj|LANG:test_lang|NS:test_namespace|CLASS:test_class", codeLocation.buildPath());

        codeLocation.addElement(CodeLocation.CodeElementLabel.FUNC, "test_method");
        assertEquals("PROJ:test_proj|LANG:test_lang|NS:test_namespace|CLASS:test_class|FUNC:test_method", codeLocation.buildPath());

        codeLocation.addElement(CodeLocation.CodeElementLabel.LINE, "11");
        assertEquals("PROJ:test_proj|LANG:test_lang|NS:test_namespace|CLASS:test_class|FUNC:test_method|LINE:11", codeLocation.buildPath());
    }

}