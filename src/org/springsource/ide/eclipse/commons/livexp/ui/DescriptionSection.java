/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.UIConstants;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * Displays a short textual desciption.
 */
public class DescriptionSection extends WizardPageSection {

	private final LiveExpression<String> model;

	private String label;
	private boolean isReadOnly = true;

	public DescriptionSection(IPageWithSections owner, LiveExpression<String> description) {
		super(owner);
		this.model = description;
	}

	/**
	 * Set readOnly option. The default value for this option is true.
	 */
	public DescriptionSection readOnly(boolean isReadOnly) {
		Assert.isTrue(!isReadOnly || model instanceof LiveVariable<?>, "The model does not support editing");
		this.isReadOnly = isReadOnly;
		return this;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}

	@Override
	public void createContents(Composite page) {
		Composite composite;
		if (label!=null) {
			composite = new Composite(page, SWT.NONE);
			GridLayout layout = GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).create();
			composite.setLayout(layout);
			Label fieldNameLabel = new Label(composite, SWT.NONE);
			fieldNameLabel.setText(label);
			GridDataFactory.fillDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING)
				.hint(UIConstants.fieldLabelWidthHint(fieldNameLabel), SWT.DEFAULT)
				.applyTo(fieldNameLabel);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		} else {
			composite = page;
		}

		int swtStyle = SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER;
		if (isReadOnly) {
			swtStyle |= SWT.READ_ONLY;
		}
		final Text text = new Text(composite, swtStyle);
		configureTextWidget(text);
		if (!isReadOnly) {
			text.addModifyListener(new ModifyListener() {
				//@Override
				public void modifyText(ModifyEvent e) {
					//Cast should succeed because readOnly option can only be disabled
					//if model is a variable.
					((LiveVariable<String>)model).setValue(text.getText());
				}
			});
		}

		//Determine vertical space so there's enough room for about X lines of text
		GC gc = new GC(text);
		FontMetrics fm = gc.getFontMetrics();
		int preferredHeight = fm.getHeight()*preferredNumberOfLines();

//		GridDataFactory grab = GridDataFactory
//				.fillDefaults().align(SWT.FILL, SWT.FILL) //without this SWT.WRAP doesn't work?
//				.grab(true, false)
//				.minSize(SWT.DEFAULT, preferredHeight)
//				.hint(SWT.DEFAULT, preferredHeight);
//		grab.applyTo(field);
//		grab.applyTo(text);
		GridData data = new GridData(GridData.FILL_HORIZONTAL); //Without this, SWT.WRAP doesn't work!
		  //See: http://vzurczak.wordpress.com/2012/08/28/force-a-swt-text-to-wrap/
		data.heightHint = preferredHeight;
		data.widthHint = UIConstants.FIELD_TEXT_AREA_WIDTH;
		text.setLayoutData(data);

//		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(fieldNameLabel);

		this.model.addListener(new ValueListener<String>() {
			public void gotValue(LiveExpression<String> exp, String value) {
				String oldText = text.getText();
				if (!oldText.equals(value)) {
					text.setText(value==null?"":value);
				}
			}
		});
	}

	protected void configureTextWidget(Text text) {
	}

	protected int preferredNumberOfLines() {
		return 5;
	}

	public DescriptionSection label(String label) {
		this.label = label;
		return this;
	}

}
