package ru.innopolis.university.innometrics.jb_plugin.handlers;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyLambdaExpression;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;
import ru.innopolis.university.innometrics.jb_plugin.models.CodeLocation;

import java.util.*;

public class CodePathCaretListener implements CaretListener {
    private static final Key<String> CARET_POSITION_KEY = new Key<>("LAST_CARET_POSITION_LINE");

    private static final String JAVA = "JAVA";
    private static final String PYTHON = "Python";

    private InnometricsComponent innometricsComponent;

    public CodePathCaretListener(InnometricsComponent innometricsComponent) {
        this.innometricsComponent = innometricsComponent;
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {
        if (!(e.getEditor() instanceof EditorImpl)) {
            return;
        }

        EditorImpl editor = (EditorImpl) e.getEditor();

        if (editor.getVirtualFile() == null) {
            return;
        }

        // count lines from 1 instead of 0
        String line = String.valueOf(e.getNewPosition().line + 1);
        String lastCaretPosLine = editor.getUserData(CARET_POSITION_KEY);

        // collect only if line has changed
        if (lastCaretPosLine == null || !lastCaretPosLine.equals(line)) {
            // save last line in the editor
            editor.putUserData(CARET_POSITION_KEY, line);

            String filePath = editor.getVirtualFile().getPath();
            PsiFile psiFile = PsiManager.getInstance(editor.getProject()).findFile(editor.getVirtualFile());

            CodeLocation codePath = new CodeLocation();
            codePath.addElement(CodeLocation.CodeElementLabel.PROJ, editor.getProject().getName());

            FileType fileType = editor.getVirtualFile().getFileType();
            if (fileType instanceof LanguageFileType) {
                LanguageFileType langFileType = (LanguageFileType) fileType;
                codePath.addElement(CodeLocation.CodeElementLabel.LANG, langFileType.getLanguage().getDisplayName());

                // define language-dependent code elements
                Map<Class, CodeLocation.CodeElementLabel> targetElements = new HashMap<>();
                if (langFileType.getLanguage().getID().equals(JAVA)) {

                    PsiDirectory directory = PsiManager.getInstance(editor.getProject()).findDirectory(editor.getVirtualFile().getParent());
                    PsiPackage directoryPackage = JavaDirectoryService.getInstance().getPackage(directory);

                    // java package as NS element
                    String packageName = directoryPackage.getQualifiedName();
                    if (!packageName.isEmpty()) {
                        codePath.addElement(CodeLocation.CodeElementLabel.NS, packageName);
                    }

                    // java specific class (CLASS) and method (FUNC) elements
                    targetElements.put(PsiClass.class, CodeLocation.CodeElementLabel.CLASS);
                    targetElements.put(PsiMethod.class, CodeLocation.CodeElementLabel.FUNC);
                    targetElements.put(PsiLambdaExpression.class, CodeLocation.CodeElementLabel.FUNC);

                } else if (langFileType.getLanguage().getID().equals(PYTHON)) {

                    // python module path as NS element
                    String module = QualifiedNameFinder.findShortestImportableName(psiFile, editor.getVirtualFile());
                    if (module != null) {
                        codePath.addElement(CodeLocation.CodeElementLabel.NS, module);
                    }

                    // python specific class (CLASS) and function (FUNC) elements
                    targetElements.put(PyClass.class, CodeLocation.CodeElementLabel.CLASS);
                    targetElements.put(PyFunction.class, CodeLocation.CodeElementLabel.FUNC);
                    targetElements.put(PyLambdaExpression.class, CodeLocation.CodeElementLabel.FUNC);

                } else {
                    // language is not supported by this plugin
                    return;
                }

                PsiElement el = psiFile.findElementAt(e.getCaret().getOffset());

                List<Pair<CodeLocation.CodeElementLabel, NavigationItem>> pathElements = new ArrayList<>();
                // find required code path elements
                while (!(el == null || el instanceof PsiFile)) {
                    PsiElement finalEl = el;
                    targetElements.entrySet().stream()
                            .filter(entry -> entry.getKey().isInstance(finalEl))
                            .findFirst()
                            .ifPresent(entry -> pathElements.add(new Pair<>(entry.getValue(), (NavigationItem) finalEl)));
                    el = el.getParent();
                }
                // TODO try to replace with PsiElementVisitor

                Collections.reverse(pathElements);
                for (Pair<CodeLocation.CodeElementLabel, NavigationItem> pathElement : pathElements) {
                    String name = pathElement.second.getName();

                    // naming anonymous classes and lambda expressions
                    if (name == null) {
                        switch (pathElement.first) {
                            case CLASS:
                                name = "[ANONYMOUS]";
                                break;
                            case FUNC:
                                name = "[LAMBDA]";
                                break;
                            default:
                                name = "";
                        }
                    }
                    codePath.addElement(pathElement.first, name);
                }

            } else {
                // not a programming language file
                // or language is not supported by this app
                return;
            }

            long millis = System.currentTimeMillis();
            codePath.addElement(CodeLocation.CodeElementLabel.LINE, line);
            String path = codePath.buildPath();

            innometricsComponent.switchActivity(path, filePath, millis);
        }
    }
}
