package ru.innopolis.university.innometrics.jb_plugin.handlers;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.Key;
import com.intellij.psi.*;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import ru.innopolis.university.innometrics.jb_plugin.components.InnometricsComponent;

public class CodePathCaretListener implements CaretListener {
    private static final String DELIMITER = "|";
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
            editor.putUserData(CARET_POSITION_KEY, line);
            String projectName = editor.getProject().getName();

            FileType fileType = editor.getVirtualFile().getFileType();
            String filePath = "";
            String languageName = "";
            String packagePath = "";
            String className = "";
            String methodName = "";

            if (fileType instanceof LanguageFileType) {
                LanguageFileType langFileType = (LanguageFileType) fileType;
                languageName = langFileType.getLanguage().getDisplayName();
                filePath = editor.getVirtualFile().getPath();

                PsiFile psiFile = PsiManager.getInstance(editor.getProject()).findFile(editor.getVirtualFile());
                PsiElement el = psiFile.findElementAt(e.getCaret().getOffset());

                if (langFileType.getLanguage().getID().equals(JAVA)) {

                    PsiDirectory directory = PsiManager.getInstance(editor.getProject()).findDirectory(editor.getVirtualFile().getParent());
                    PsiPackage directoryPackage = JavaDirectoryService.getInstance().getPackage(directory);
                    packagePath = directoryPackage.getQualifiedName();

                    while (!(el == null || el instanceof PsiMethod || el instanceof PsiClass)) {
                        el = el.getParent();
                    }
                    if (el != null && el instanceof PsiMethod) {
                        methodName = ((PsiMethod) el).getName();
                    }

                    while (!(el == null || el instanceof PsiClass)) {
                        el = el.getParent();
                    }
                    if (el != null && el instanceof PsiClass) {
                        className = ((PsiClass) el).getName();
                    }

                    // TODO try to replace with PsiElementVisitor or PsiRecursiveElementVisitor
                }

                if (langFileType.getLanguage().getID().equals(PYTHON)) {

                    /*PsiDirectory directory = PsiManager.getInstance(editor.getProject()).findDirectory(editor.getVirtualFile().getParent());
                    PyConvertModuleToPackageAction*/


                    while (!(el == null || el instanceof PyFunction || el instanceof PyClass)) {
                        el = el.getParent();
                    }
                    // "el" is always null or PyFunction or PyClass here

                    if (el != null && el instanceof PyFunction) {
                        methodName = ((PyFunction) el).getName();
                    }
                    // "el" is always null or PyFunction or PyClass here

                    while (!(el == null || el instanceof PyClass)) {
                        el = el.getParent();
                    }
                    // "el" is always null or has PyClass type here

                    if (el != null) {
                        className = ((PyClass) el).getName();
                    }
                }
            } else {
                return;
//                languageName = fileType.getName();
            }

            long millis = System.currentTimeMillis();
            String path = line + DELIMITER + projectName + DELIMITER + languageName + DELIMITER +
                    packagePath + DELIMITER + className + DELIMITER + methodName;

            innometricsComponent.editorLogActivity(path, filePath, millis);
        }/* else {
            System.out.println("same");
        }*/
//        DateFormatUtil.formatDuration(duration)
    }
}
