package me.zain.idea.plugins.i18n;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class TranslationReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        //Mustache support.
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.dmarcotte.handlebars"));
        ClassLoader classLoader = plugin != null ? plugin.getPluginClassLoader() : getClass().getClassLoader();
        try {
            Class<PsiElement> clazz = (Class<PsiElement>) Class.forName("com.dmarcotte.handlebars.psi.HbBlockWrapper", true, classLoader);
            registrar.registerReferenceProvider(StandardPatterns.instanceOf(clazz), new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    return new PsiReference[]{new MustacheTranslationReferenceLocator(element)};
                }
            });
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // JS Support
        registerForPlugin(registrar, "JavaScript", "com.intellij.lang.javascript.psi.JSLiteralExpression");

        // PHP Support
        registerForPlugin(registrar, "com.jetbrains.php", "com.jetbrains.php.lang.psi.elements.StringLiteralExpression");
    }


    private void registerForPlugin(PsiReferenceRegistrar registrar, String pluginId, String className) {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId(pluginId));
        ClassLoader classLoader = plugin != null ? plugin.getPluginClassLoader() : getClass().getClassLoader();
        register(registrar, className, classLoader);
    }

    @SuppressWarnings("unchecked")
    private void register(PsiReferenceRegistrar registrar, String className, ClassLoader classLoader) {
        try {
            Class<PsiElement> clazz = (Class<PsiElement>) Class.forName(className, true, classLoader);
            registrar.registerReferenceProvider(StandardPatterns.instanceOf(clazz), new PsiReferenceProvider() {
                @NotNull
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    return new PsiReference[]{new TranslationReferenceLocator(element)};
                }
            });

        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
