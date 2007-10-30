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
package info.rsdev.eclipse.opencms.module.developer.compatibility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.opencms.file.CmsResource;

/**
 * @author Dave Schoorl
 *
 */
public class CmsResourceCompatibility {
	
	/* (non-Javadoc)
	 * @see info.rsdev.eclipse.opencms.module.developer.compatibility.ICmsResourceCompatibility#isChanged(org.opencms.file.CmsResource)
	 */
	public static boolean isChanged(CmsResource cmsResource) {
		try {
			Method getStateMethod = cmsResource.getClass().getMethod("getState", new Class[] {});
			Object returnValue = getStateMethod.invoke(cmsResource, new Object[] {});
			Field changedState = cmsResource.getClass().getDeclaredField("STATE_UNCHANGED");
			return !returnValue.equals(changedState.get(cmsResource));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see info.rsdev.eclipse.opencms.module.developer.compatibility.ICmsResourceCompatibility#isDeleted(org.opencms.file.CmsResource)
	 */
	public static boolean isDeleted(CmsResource cmsResource) {
		try {
			Method getStateMethod = cmsResource.getClass().getMethod("getState", new Class[] {});
			Object returnValue = getStateMethod.invoke(cmsResource, new Object[] {});
			Field changedState = cmsResource.getClass().getDeclaredField("STATE_DELETED");
			return returnValue.equals(changedState.get(cmsResource));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
