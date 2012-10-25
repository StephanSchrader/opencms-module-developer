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
package info.rsdev.eclipse.opencms.loader;

import java.util.List;

/**
 * @author Dave Schoorl
 *
 */
public interface OpenCmsClasspathChangeListener {

	public void changeWebInfLocation(String oldDir, String newDir);
	
	public void changeAdditionalJars(List<String> oldJars, List<String> newJars);
}
