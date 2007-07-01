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
package org.opencms.main;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dave Schoorl
 *
 */
public class OpenCmsConstants {

	public static final int OPENCMS_TYPE_FOLDER = 0;
	public static final int OPENCMS_TYPE_JSP = 4;
	public static final int OPENCMS_TYPE_XMLPAGE = 6;
	public static final int OPENCMS_TYPE_XMLCONTENT = 7;
	public static final int OPENCMS_TYPE_BINARY = 2;
	public static final int OPENCMS_TYPE_IMAGE = 3;
	
	public static final Map OPENCMS_TYPE_MAPPING = new HashMap();
	static {
		OPENCMS_TYPE_MAPPING.put("jsp", new Integer(OPENCMS_TYPE_JSP));
		OPENCMS_TYPE_MAPPING.put("xml", new Integer(OPENCMS_TYPE_XMLCONTENT));
		OPENCMS_TYPE_MAPPING.put("pdf", new Integer(OPENCMS_TYPE_BINARY));
		OPENCMS_TYPE_MAPPING.put("zip", new Integer(OPENCMS_TYPE_BINARY));
		OPENCMS_TYPE_MAPPING.put("class", new Integer(OPENCMS_TYPE_BINARY));
		OPENCMS_TYPE_MAPPING.put("ppt", new Integer(OPENCMS_TYPE_BINARY));
		OPENCMS_TYPE_MAPPING.put("doc", new Integer(OPENCMS_TYPE_BINARY));
		OPENCMS_TYPE_MAPPING.put("xls", new Integer(OPENCMS_TYPE_BINARY));
		OPENCMS_TYPE_MAPPING.put("jpeg", new Integer(OPENCMS_TYPE_IMAGE));
		OPENCMS_TYPE_MAPPING.put("jpg", new Integer(OPENCMS_TYPE_IMAGE));
		OPENCMS_TYPE_MAPPING.put("gif", new Integer(OPENCMS_TYPE_IMAGE));
		OPENCMS_TYPE_MAPPING.put("png", new Integer(OPENCMS_TYPE_IMAGE));
		OPENCMS_TYPE_MAPPING.put("tif", new Integer(OPENCMS_TYPE_IMAGE));
		OPENCMS_TYPE_MAPPING.put("tiff", new Integer(OPENCMS_TYPE_IMAGE));
	}
	
	public static final Map OPENCMS_EXTENSION_MAPPING = new HashMap();
	static {
		OPENCMS_EXTENSION_MAPPING.put(new Integer(OpenCmsConstants.OPENCMS_TYPE_JSP), "jsp");
		OPENCMS_EXTENSION_MAPPING.put(new Integer(OpenCmsConstants.OPENCMS_TYPE_XMLPAGE), "xml");
		OPENCMS_EXTENSION_MAPPING.put(new Integer(OpenCmsConstants.OPENCMS_TYPE_XMLCONTENT), "xml");
	}
	

	
}
