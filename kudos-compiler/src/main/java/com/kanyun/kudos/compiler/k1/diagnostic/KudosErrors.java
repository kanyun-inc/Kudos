package com.kanyun.kudos.compiler.k1.diagnostic;

import com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1;
import org.jetbrains.kotlin.diagnostics.Errors;

import static org.jetbrains.kotlin.diagnostics.Severity.ERROR;

/**
 * Created by Benny Huo on 2023/8/21
 */
public interface KudosErrors {

    DiagnosticFactory1<PsiElement, String> CONFLICTS_WITH_NOARG = DiagnosticFactory1.create(ERROR);
    DiagnosticFactory0<PsiElement> CONFLICTS_WITH_JSON_ADAPTER = DiagnosticFactory0.create(ERROR);
    DiagnosticFactory0<PsiElement> NOT_PROPERTY_PARAMETER = DiagnosticFactory0.create(ERROR);
    DiagnosticFactory0<PsiElement> VARARG_PARAMETER = DiagnosticFactory0.create(ERROR);
    DiagnosticFactory0<PsiElement> INIT_BLOCK = DiagnosticFactory0.create(ERROR);
    DiagnosticFactory1<PsiElement, String> PROPERTY_INITIALIZER = DiagnosticFactory1.create(ERROR);
    DiagnosticFactory1<PsiElement, String> PROPERTY_DELEGATE = DiagnosticFactory1.create(ERROR);
    DiagnosticFactory1<PsiElement, String> NON_KUDOS_PROPERTY_TYPE = DiagnosticFactory1.create(ERROR);
    DiagnosticFactory0<PsiElement> GENERIC_TYPE = DiagnosticFactory0.create(ERROR);

    Object _initializer = new Object() {
        {
            Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(KudosErrors.class, KudosDefaultErrorMessages.INSTANCE);
        }
    };
}
