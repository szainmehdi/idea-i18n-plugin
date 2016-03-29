package me.zain.idea.plugins.i18n;

import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TranslationReferenceLocator<T extends PsiElement> extends PsiPolyVariantReferenceBase<T> {

    public TranslationReferenceLocator(T psiElement) {
        super(psiElement, true);
    }

    public TranslationReferenceLocator(T psiElement, TextRange textRange) {
        super(psiElement, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        // Just the default return value, if nothing was found.
        final ResolveResult[] empty = new ResolveResult[0];

        // Get the string from the cursor
        String tlKey = computeStringValue();
        if (!isKeyLikeString(tlKey)) {
            return empty;
        }

        // Get the translation file name from the key
        String filename = getTranslationFilePath(tlKey);

        // Strip off the first part of the key, which is the filename.
        tlKey = tlKey.substring(tlKey.indexOf('.') + 1);

        // Find the translation file.
        Project project = getElement().getProject();
        VirtualFile file = project.getBaseDir().findFileByRelativePath(filename);
        if (file == null) {
            return empty;
        }

        // Find the PSI File instance.
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            return empty;
        }

        // Split the key into parts
        String[] keyParts = tlKey.split("\\.");

        // If we don't find the exact element in the file, we'll return the closest we get.
        PsiElement element = psiFile;

        // Loop through the parts of the key to semi-recursively find the exact element in the file.
        for (String k : keyParts) {
            PsiElement next = findElement(element, k);
            if (next == null) {
                break;
            }
            // We found the next element. We must go deeper.
            element = next;
        }

        // Wrap up the result and return!
        ResolveResult[] results = new ResolveResult[1];
        results[0] = new PsiElementResolveResult(element);
        return results;
    }

    @NotNull
    protected String computeStringValue() {
        String text = getElement().getText();
        String call = getCallingElementText();

        if (!isTlFunctionCall(call)) {
            return "";
        }

        return stripQuotes(text);
    }

    private boolean isKeyLikeString(String tlKey) {
        return tlKey.length() != 0 && tlKey.indexOf('.') != -1;
    }

    @NotNull
    private String getTranslationFilePath(String tlKey) {
        return "resources/translations/en/" + tlKey.substring(0, tlKey.indexOf('.')) + ".json";
    }

    @Nullable
    private PsiElement findElement(PsiElement element, String key) {
        PsiElement child = PsiTreeUtil.findChildOfType(element, JsonProperty.class);

        for (PsiElement e = child; e != null; e = e.getNextSibling()) {
            PsiElement c = e.getFirstChild();
            if (c == null) continue;

            String prop = stripQuotes(c.getText());
            if (prop.equals(key)) {
                return e;
            }
        }

        return null;
    }

    private String getCallingElementText() {
        PsiElement parent = getElement().getParent();

        if (parent == null) {
            return "";
        }

        parent = parent.getParent();

        if (parent == null) {
            return "";
        }

        return parent.getText();
    }

    private boolean isTlFunctionCall(String call) {
        if (call.startsWith("tl(")) {
            return true;
        }

        if (call.startsWith("_W.utl") || call.startsWith("_W.stl")) {
            return true;
        }

        if (call.startsWith("Weebly.utl") || call.startsWith("Weebly.stl")) {
            return true;
        }

        return false;
    }

    @NotNull
    protected String stripQuotes(String text) {
        if (text.length() >= 2 && (this.surroundedWith(text, "\"") || this.surroundedWith(text, "'"))) {
            return text.substring(1, text.length() - 1);
        }

        // No quotes, return.
        return text;
    }

    protected boolean surroundedWith(String target, String search) {
        return target.startsWith(search) && target.endsWith(search);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return false;
    }
}
