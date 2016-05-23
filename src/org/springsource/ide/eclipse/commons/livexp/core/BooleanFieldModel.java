/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;


/**
 * @author Kris De Volder
 */
public class BooleanFieldModel extends FieldModel<Boolean> {


	public BooleanFieldModel(String name, boolean defaultValue) {
		super(Boolean.class, name, defaultValue);
	}

}
