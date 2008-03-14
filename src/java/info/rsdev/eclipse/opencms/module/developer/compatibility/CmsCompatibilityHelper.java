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
import org.opencms.main.CmsSystemInfo;

/**
 * @author Dave Schoorl
 *
 */
public class CmsCompatibilityHelper {
	
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
	
	/**
	 * As of OpenCms 7.0.4, the signature of the CmsSystemInfo initializer has changed: a
	 * new parameter, called servletContainerName is added, breaking backwards compatibility.
	 * 
	 * This method will use reflection to check whether 4 or 5 parameters are needed by the
	 * init-method. When we are running on OpenCms 7.0.4 or newer, we will use a null value for 
	 * the servletContainerName, since we are running OpenCms outside of the ServletContainer.
	 * 
	 * @param systemInfo
	 */
	public static void initCmsSystemInfo(CmsSystemInfo systemInfo, String webinfLocation, String servletMapping, String webappName) {
		Method initMethod = findUniqueMethod("init", systemInfo.getClass().getMethods());
		if (initMethod != null) {
			Class[] parameterTypes = initMethod.getParameterTypes();
		}
	}
	
	/**
	 * From an array of methods, return the first one that matches the targetted method.
	 * Only use this method when you know there can be only one. Only public Methods are
	 * searched.
	 * 
	 * @param targetName The name of the method to find
	 * @param methods An array of Methods the find the Method in
	 * @return the first public Method encountered in the array of methods which name matches the targetted name
	 */
	protected static Method findUniqueMethod(String targetName, Method[] methods) {
		Method targetMethod = null;
		if (methods != null) {
			for (int i=0; ((targetMethod == null) && (i<methods.length)); i++) {
				if (methods[i].getName().equals(targetName)) {
					targetMethod = methods[i];
				}
			}
		}
		return targetMethod;
	}

}
