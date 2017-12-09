package ru.innopolis.university.innometrics.jb_plugin.handlers;

import com.intellij.openapi.editor.Editor;
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
        String line = String.valueOf(e.getNewPosition().line);

        Editor editorInterface = e.getEditor();
        EditorImpl editor;
        if (editorInterface instanceof EditorImpl) {
            editor = (EditorImpl) editorInterface;
        } else {
            return;
        }

        if (editor.getVirtualFile() == null) {
            return;
        }

        String lastCaretPosLine = editor.getUserData(CARET_POSITION_KEY);

        // collect only if line has changed
        if (lastCaretPosLine == null || !lastCaretPosLine.equals(line)) {
            // save last line in the editor
            editor.putUserData(CARET_POSITION_KEY, line);

            String filePath = editor.getVirtualFile().getPath();

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
                    codePath.addElement(CodeLocation.CodeElementLabel.NS, directoryPackage.getQualifiedName());

                    // java specific class (CLASS) and method (FUNC) elements
                    targetElements.put(PsiClass.class, CodeLocation.CodeElementLabel.CLASS);
                    targetElements.put(PsiMethod.class, CodeLocation.CodeElementLabel.FUNC);
                    // TODO lambda function PsiLambdaExpression

                } else if (langFileType.getLanguage().getID().equals(PYTHON)) {

                    // TODO module path for python as NS element
                    /*PsiDirectory directory = PsiManager.getInstance(editor.getProject()).findDirectory(editor.getVirtualFile().getParent());
                    PyConvertModuleToPackageAction*/
//                    codePath.addElement(CodeLocation.CodeElementLabel.NS, modulePath);

                    // python specific class (CLASS) and function (FUNC) elements
                    targetElements.put(PyClass.class, CodeLocation.CodeElementLabel.CLASS);
                    targetElements.put(PyFunction.class, CodeLocation.CodeElementLabel.FUNC);

                } else {
                    // language is not supported
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(editor.getProject()).findFile(editor.getVirtualFile());
                PsiElement el = psiFile.findElementAt(e.getCaret().getOffset());

                List<Pair<CodeLocation.CodeElementLabel, PsiNamedElement>> pathElements = new ArrayList<>();
                // find required code path elements
                while (!(el == null || el instanceof PsiFile)) {
                    PsiElement finalEl = el;
                    targetElements.entrySet().stream()
                            .filter(entry -> entry.getKey().isInstance(finalEl))
                            .findFirst()
                            .ifPresent(entry -> pathElements.add(new Pair<>(entry.getValue(), (PsiNamedElement) finalEl)));
                    el = el.getParent();
                }
                // TODO try to replace with PsiElementVisitor

                Collections.reverse(pathElements);
                for (Pair<CodeLocation.CodeElementLabel, PsiNamedElement> pathElement : pathElements) {
                    codePath.addElement(pathElement.first, pathElement.second.getName());
                }

            } else {
                // not a programming language file
                return;
            }

            long millis = System.currentTimeMillis();
            codePath.addElement(CodeLocation.CodeElementLabel.LINE, line);
            String path = codePath.buildPath();

            innometricsComponent.editorLogActivity(path, filePath, millis);
        }/* else {
            System.out.println("same");
        }*/
//        DateFormatUtil.formatDuration(duration)
    }
}
