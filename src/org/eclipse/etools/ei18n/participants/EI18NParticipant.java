package org.eclipse.etools.ei18n.participants;

import org.eclipse.core.resources.IFile;
import org.eclipse.etools.ei18n.util.EI18NUtil;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;

final class EI18NParticipant {
    protected static final String NAME="EI18N"; //$NON-NLS-1$

    IField field;
    private final boolean initialized;

    EI18NParticipant(Object element) {
        initialized=initialize(element);
    }

    private boolean initialize(Object element) {
        if (element instanceof IField) {
            field=(IField) element;
            IFile res=EI18NUtil.getFile(field);
            for (MappingPreference mapping : MappingPreference.list(res.getProject())) {
                if (mapping.getJavaFile().equals(res))
                    return true;
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
