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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsSystemInfo;

/**
 * This class is used to provide a bridge between incompatible OpenCms versions
 * 
 * @author Dave Schoorl
 */
public class CmsCompatibilityHelper {
	
	/**
	 * In OpenCms 6.x, the state of a resource (new, changed, deleted etc.) is recorded through an int, in OpenCms 7,
	 * the state is replaced with a CmsResourceState-object.
	 * 
	 * @param cmsResource The resource to check whether it has changed
	 * @return false when the state of the resource is 'unchanged', true otherwise 
	 */
	public static boolean isChanged(CmsResource cmsResource) {
		try {
			Method getStateMethod = cmsResource.getClass().getMethod("getState", new Class[] {});
			Object stateObject = getStateMethod.invoke(cmsResource, new Object[] {});
			Field changedState = cmsResource.getClass().getDeclaredField("STATE_UNCHANGED");
			return !stateObject.equals(changedState.get(cmsResource));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * In OpenCms 6.x, the state of a resource (new, changed, deleted etc.) is recorded through an int, in OpenCms 7.x,
	 * the state is replaced with a CmsResourceState-object.
	 * 
	 * @param cmsResource The resource to check whether it is deleted
	 * @return true when the state of the resource is 'deleted', false otherwise 
	 */
	public static boolean isDeleted(CmsResource cmsResource) {
		try {
			Method getStateMethod = cmsResource.getClass().getMethod("getState", new Class[] {});
			Object stateObject = getStateMethod.invoke(cmsResource, new Object[] {});
			Field deletedState = cmsResource.getClass().getDeclaredField("STATE_DELETED");
			return stateObject.equals(deletedState.get(cmsResource));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Resources can have sibblings. When you delete a resource, you have to indicate in the delete method, 
	 * what to do with these sibblings: preserve or delete them. In OpenCms 6.x, this was indicated with an 
	 * int constant, in OpenCms 7, this is done with a CmsResourceDeleteMode-object.
	 * 
	 * @param cms The CmsObject to operate on
	 * @param cmsResource The resource to delete
	 */
	public static void deleteResource(CmsObject cms, CmsResource cmsResource, String resourceName) {
		try {
			Field deleteSibblingConstant = cmsResource.getClass().getField("DELETE_PRESERVE_SIBLINGS");
			Object deleteSibblingValue = deleteSibblingConstant.get(cmsResource);
			
			/* Delete method has two parameters, the name of the resource to delete and an indicator for
			 * the type of deletion (either preserve or delete sibblings). The indicator type differs,
			 * depending on which version of OpenCms is used.
			 */
			Class deleteSibblingValueType = null;
			if (deleteSibblingValue.getClass().equals(Integer.class)) {
				deleteSibblingValueType = int.class;
			} else {
				deleteSibblingValueType = deleteSibblingValue.getClass();
			}
			Method deleteMethod =  cms.getClass().getMethod("deleteResource", String.class, deleteSibblingValueType);
			//String resourceName = cmsResource.getRootPath();
			deleteMethod.invoke(cms, new Object[] {resourceName, deleteSibblingValue});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * As of OpenCms 7.0.4, the signature of the CmsSystemInfo initializer has changed: a
	 * new parameter, called servletContainerName is added, breaking backwards compatibility.
	 * 
	 * And as of OpenCms 7.0.5 there has been added another parameter to the signature of the
	 * CmsSystemInfo initializer called throwException, breaking backwards compatibility.
	 * 
	 * This method will use reflection to check whether 4, 5 or 6 parameters are needed by the
	 * init-method. When we are running on OpenCms 7.0.4 or newer, we will use a null value for 
	 * the servletContainerName, since we are running OpenCms outside of the ServletContainer.
	 * When we are running 7.0.5 or newer, we will provide the Boolean value TRUE for the 
	 * throwException parameter. This parameter is not relevant for us, since we are running
	 * outside a servlet container.
	 * 
	 * @param systemInfo
	 */
	public static void initCmsSystemInfo(CmsSystemInfo systemInfo, String webinfLocation, String servletMapping, String webappName) {
		Method initMethod = findUniqueMethod("init", systemInfo.getClass().getDeclaredMethods());
		Class[] parameterTypes = null;
		if (initMethod != null) {
			parameterTypes = initMethod.getParameterTypes();
		}
		if (parameterTypes != null) {
			Object[] parameterValues = null;
			if (parameterTypes.length == 4) {
				//We are dealing with OpenCms 7.0.3 or earlier
				parameterValues = new Object[] { webinfLocation, servletMapping, null, webappName};
			} else if (parameterTypes.length == 5) {
				//We are dealing with OpenCms 7.0.4
				String servletContainerName = null;	//not relevant, since we are running outside container
				parameterValues = new Object[] { webinfLocation, servletMapping, null, webappName, servletContainerName};
			} else if (parameterTypes.length == 6) {
			    //We are dealing with OpenCms 7.0.5 
			    String servletContainerName = null;    //not relevant, since we are running outside container
			    Boolean throwException = Boolean.TRUE;
			    parameterValues = new Object[] { webinfLocation, servletMapping, null, webappName, servletContainerName, throwException };
			} else if(parameterTypes.length == 1) {
				//We are dealing with OpenCms 7.5.1
				String defaultWebApplication = "ROOT"; 
				String servletContainerName = null;  //not relevant, since we are running outside container
				String webApplicationContext = null; //calculated from the path
				Object[] constructorParameterValues = 
					new Object[] { webinfLocation, defaultWebApplication, servletMapping, servletContainerName, webApplicationContext};
				try {
					Class[] parameterTypesForConstructor = {String.class,String.class,String.class,String.class,String.class};
					Constructor constructor = parameterTypes[0].getDeclaredConstructor(parameterTypesForConstructor);
					constructor.setAccessible(true);
					parameterValues = new Object[] {constructor.newInstance(constructorParameterValues)};
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				initMethod.setAccessible(true);	//the init-method is protected
				initMethod.invoke(systemInfo, parameterValues);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
