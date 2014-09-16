/*******************************************************************************
 * Copyright (c) 2006, 2009 Eric Rizzo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eric Rizzo - initial implementation
 *******************************************************************************/
package org.eclipse.etools.ei18n.util;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A CellEditor that is a blending of DialogCellEditor and TextCellEditor. The user can either type
 * directly into the Text or use the button to open a Dialog for editing the cell's value.
 * 
 */
public abstract class TextAndDialogCellEditor extends DialogCellEditor {
	private Text textField;

	public TextAndDialogCellEditor(Composite parent) {
		super(parent);
	}


	@Override
    protected Control createContents(Composite cell) {
		textField = new Text(cell, SWT.NONE);
		textField.setFont(cell.getFont());
		textField.setBackground(cell.getBackground());
		textField.addFocusListener(new FocusAdapter() {
				@Override
                public void focusLost(FocusEvent event) {
					 setValueToModel();
				}
			});

		textField.addKeyListener(new KeyAdapter() {
				@Override
                public void keyPressed(KeyEvent event) {
					keyReleaseOccured(event);
				}
			});

		return textField;
	}

	@Override
    protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.keyCode == SWT.CR || keyEvent.keyCode == SWT.KEYPAD_CR) { // Enter key
			setValueToModel();
		}
		super.keyReleaseOccured(keyEvent);
	}

	protected void setValueToModel() {
	 	String newValue = textField.getText();
        boolean newValidState = isCorrect(newValue);
        if (newValidState) {
            markDirty();
            doSetValue(newValue);
        } else {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { newValue.toString() }));
        }
	}

	@Override
    protected void updateContents(Object value) {
        if (textField == null)
			return;

        String text=StringUtils.EMPTY;
        if (value != null) {
			text = value.toString();
		}
        textField.setText(text);
		
	}

	@Override
    protected void doSetFocus() {
		// Overridden to set focus to the Text widget instead of the Button.
		textField.setFocus();
		textField.selectAll();
	}
}
