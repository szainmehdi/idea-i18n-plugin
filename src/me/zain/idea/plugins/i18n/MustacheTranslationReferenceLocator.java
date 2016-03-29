package me.zain.idea.plugins.i18n;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class MustacheTranslationReferenceLocator extends TranslationReferenceLocator {
    public MustacheTranslationReferenceLocator(PsiElement psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    protected String computeStringValue() {
        System.out.println("MustacheTranslationReferenceLocator#computeStringValue() was called!");
        return "";
    }

    @Override
    public TextRange getRangeInElement() {
        return getElement().getTextRange();
    }
}
