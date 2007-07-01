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


import java.util.jar.Manifest;

/**
 * @author Dave Schoorl
 *
 */
public class ClassMetaInformation {
	
	private ManifestInformation manifestInformation;
	
	private byte[] classBytes;
	
	private String className;
	
	public ClassMetaInformation(String className, ManifestInformation manifestInformation, byte[] classBytes) {
		this.className = className;
		this.classBytes = classBytes;
		this.manifestInformation = manifestInformation;
	}

	public ClassMetaInformation(String className, Manifest manifest, byte[] classBytes) {
		this.className = className;
		this.classBytes = classBytes;
		this.manifestInformation = new ManifestInformation(manifest);
	}

	public byte[] getClassBytes() {
		return classBytes;
	}

	public ManifestInformation getManifestInformation() {
		return manifestInformation;
	}
	
	public String getClassName() {
		return className;
	}
	
}

