package org.eclipse.etools.ei18n.participants;

import static org.eclipse.core.runtime.IStatus.ERROR;

import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

final class EI18NParticipant {
    static final String NAME="EI18N"; //$NON-NLS-1$

    IField field;
    private final boolean initialized;

    EI18NParticipant(Object element) {
        initialized=initialize(element);
    }

    private boolean initialize(Object element) {
        if (element instanceof IField) {
            try {
                field=(IField) element;
                return EI18NConstants.NLS_CLASS_NAME.equals(getDeclaringType().getSuperclassName());
            } catch (JavaModelException e) {
                Activator.log(ERROR, "Failed to determin if the field was concerned", e); //$NON-NLS-1$
            }
        }

        return false;
    }

    IType getDeclaringType() {
        return field.getDeclaringType();
    }

    public boolean isInitialized() {
        return initialized;
    }

    IField getField() {
        return field;
    }
}
