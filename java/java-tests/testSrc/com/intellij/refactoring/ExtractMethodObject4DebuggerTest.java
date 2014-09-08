/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * User: anna
 * Date: 06-May-2008
 */
package com.intellij.refactoring;

import com.intellij.JavaTestUtil;
import com.intellij.idea.Bombed;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.*;
import com.intellij.refactoring.extractMethodObject.ExtractLightMethodObjectHandler;
import com.intellij.testFramework.IdeaTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class ExtractMethodObject4DebuggerTest extends LightRefactoringTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return JavaTestUtil.getJavaTestDataPath();
  }

  private void doTest(String evaluatedText, String expectedCallSite, String expectedClass) throws Exception {
    final String testName = getTestName(false);
    configureByFile("/refactoring/extractMethodObject4Debugger/" + testName + ".java");
    final int offset = getEditor().getCaretModel().getOffset();
    final PsiElement context = getFile().findElementAt(offset);
    final JavaCodeFragment fragment = JavaCodeFragmentFactory.getInstance(getProject()).createCodeBlockCodeFragment(evaluatedText, context, false);
    final ExtractLightMethodObjectHandler.ExtractedData extractedData =
      ExtractLightMethodObjectHandler.extractLightMethodObject(getProject(), getFile(), fragment, "test");
    assertNotNull(extractedData);
    assertEquals(expectedCallSite, extractedData.getGeneratedCallText());
    final PsiClass innerClass = extractedData.getGeneratedInnerClass();
    assertEquals(expectedClass, innerClass.getText());
  }

  public void testSimpleGeneration() throws Exception {
    doTest("int i = 0; int j = 0;", "Test test = new Test().invoke();\n" +
                                    "      int i = test.getI();\n" +
                                    "      int j = test.getJ();",

           "public class Test {\n" +
           "        private int i;\n" +
           "        private int j;\n" +
           "\n" +
           "        public int getI() {\n" +
           "            return i;\n" +
           "        }\n" +
           "\n" +
           "        public int getJ() {\n" +
           "            return j;\n" +
           "        }\n" +
           "\n" +
           "        public Test invoke() {\n" +
           "            i = 0;\n" +
           "            j = 0;\n" +
           "            return this;\n" +
           "        }\n" +
           "    }");
  }

  @Bombed(month = Calendar.SEPTEMBER, day = 20)
  public void testInvokeReturnType() throws Exception {
    doTest("x = 6; y = 6;", "Test test = new Test().invoke();\n" +
                            "      x = test.getX();\n" +
                            "      y = test.getY();",

           "public static class Test {\n" +
           "        private int x;\n" +
           "        private int y;\n" +
           "\n" +
           "        public int getX() {\n" +
           "            return x;\n" +
           "        }\n" +
           "\n" +
           "        public int getY() {\n" +
           "            return y;\n" +
           "        }\n" +
           "\n" +
           "        public Test invoke() {\n" +
           "            x = 6;\n" +
           "            y = 6;\n" +
           "            return this;\n" +
           "        }\n" +
           "    }");
  }

  @Override
  protected Sdk getProjectJDK() {
    return IdeaTestUtil.getMockJdk18();
  }
}
