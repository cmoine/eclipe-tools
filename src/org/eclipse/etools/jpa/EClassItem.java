package org.eclipse.etools.jpa;

import java.util.List;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;

import com.google.common.collect.Lists;

class EClassItem {
	final ICompilationUnit cu;
	final EClassifier clazz;
	final IType type;
	final List<FieldItem> fields=Lists.newArrayList();

	public EClassItem(ICompilationUnit cu, IType type, EClassifier clazz) {
		this.cu = cu;
		this.type = type;
		this.clazz = clazz;
	}
}
