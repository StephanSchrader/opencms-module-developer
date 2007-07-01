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
package info.rsdev.eclipse.opencms.module.developer.loader.util;

import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

/**
 * @author Dave Schoorl
 *
 */
public class ManifestInformation {
	
	private Manifest manifest;
	
    public ManifestInformation(Manifest manifest) {
    	this.manifest = manifest;
    }

	public Manifest getManifest() {
		return manifest;
	}
	
	public boolean hasManifest() {
		return manifest != null;
	}

	public String getImplTitle() {
		return getAttributeValue(Name.IMPLEMENTATION_TITLE);
	}

	public String getImplVendor() {
		return getAttributeValue(Name.IMPLEMENTATION_VENDOR);
	}

	public String getImplVersion() {
		return getAttributeValue(Name.IMPLEMENTATION_VERSION);
	}

	public String getSpecTitle() {
		return getAttributeValue(Name.SPECIFICATION_TITLE);
	}

	public String getSpecVendor() {
		return getAttributeValue(Name.SPECIFICATION_VENDOR);
	}

	public String getSpecVersion() {
		return getAttributeValue(Name.SPECIFICATION_VERSION);
	}
	
	private String getAttributeValue(Name attributeName) {
		String attributeValue = null;
		if (manifest != null) {
			Attributes attributes = manifest.getMainAttributes();
			if (attributes != null) {
				attributeValue = attributes.getValue(attributeName);
			}
		}
		return attributeValue;
	}
	
}
