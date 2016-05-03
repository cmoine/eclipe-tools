package org.eclipse.etools.jpa;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

public class FieldItem {

	final String typeSignature;
	final IType type;
	final IField field;
	EEnum e;

	public FieldItem(IField field) throws JavaModelException {
		this.field = field;
		String signatureSimpleName = Signature.getSignatureSimpleName(field.getTypeSignature());
		String[][] resolveType = field.getDeclaringType().resolveType(signatureSimpleName);
		IType type=resolveType==null ? null : field.getCompilationUnit().getJavaProject().findType(resolveType[0][0] + '.' + resolveType[0][1]);
//		if(signatureSimpleName.startsWith("Set") || signatureSimpleName.startsWith("List")) {
//			signatureSimpleName=StringUtils.substringBeforeLast(StringUtils.substringAfter(signatureSimpleName, "<"), ">");
//			System.out.println("FieldItem.FieldItem()");
		String typeSignature= type==null ? signatureSimpleName : type.getFullyQualifiedName();
		if(type!=null && ArrayUtils.contains(type.getSuperInterfaceNames(), "java.util.Collection")) {
			signatureSimpleName=StringUtils.substringBeforeLast(StringUtils.substringAfter(signatureSimpleName, "<"), ">");
			resolveType = field.getDeclaringType().resolveType(signatureSimpleName);
			type=resolveType==null ? null : field.getCompilationUnit().getJavaProject().findType(resolveType[0][0] + '.' + resolveType[0][1]);
			typeSignature= type==null ? signatureSimpleName : type.getFullyQualifiedName();
		}
		this.type=type;
		this.typeSignature=typeSignature;
		
//		}
//		if("java.util.Set".equals(type.getFullyQualifiedName())) {
//			
//		} else {
//		typeSignature = type==null ? signatureSimpleName : type.getFullyQualifiedName();//Signature.getSignatureSimpleName(field.getTypeSignature());
//		}
	}
}
