/**
 * Copyright (C) 2007 Red Star Development
 * All rights reserved. This source code and accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Star Development - initial API and implementation
 */
package info.rsdev.eclipse.opencms.wizards;

/**
 * Define the methods for a Wizard page to exchange data between it's widgets and
 * the underlying data object. Can be replaced by a databinding mechanism in future
 * releases.
 *  
 * @author Dave Schoorl
 */
public interface IExchanger {
	
	public void getWidgetValues();
	
	public void setWidgetValues();
}
