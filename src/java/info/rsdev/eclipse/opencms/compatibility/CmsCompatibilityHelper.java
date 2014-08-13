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
package info.rsdev.eclipse.opencms.compatibility;

import info.rsdev.eclipse.opencms.OpenCmsModuleDeveloperPlugin;
import info.rsdev.eclipse.opencms.preferences.OpenCmsModuleDeveloperPreferencePage;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jface.preference.IPreferenceStore;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCmsCore;

/**
 * This class is used to provide a bridge between incompatible OpenCms versions
 * 
 * @author Dave Schoorl
 */
public class CmsCompatibilityHelper {
    private static ClassLoader opencmsClassloader = null;
	
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
			Class<?> deleteSibblingValueType = null;
			if (deleteSibblingValue.getClass().equals(Integer.class)) {
				deleteSibblingValueType = int.class;
			} else {
				deleteSibblingValueType = deleteSibblingValue.getClass();
			}
			Method deleteMethod =  cms.getClass().getMethod("deleteResource", String.class, deleteSibblingValueType);
			//String resourceName = cmsResource.getRootPath();
			deleteMethod.invoke(cms, new Object[] {resourceName, deleteSibblingValue});
		} catch (Exception e) {
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
		Class<?>[] parameterTypes = null;
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
					Class<?>[] parameterTypesForConstructor = {String.class,String.class,String.class,String.class,String.class};
					Constructor<?> constructor = parameterTypes[0].getDeclaredConstructor(parameterTypesForConstructor);
					constructor.setAccessible(true);
					parameterValues = new Object[] {constructor.newInstance(constructorParameterValues)};
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			try {
				initMethod.setAccessible(true);	//the init-method is protected
				initMethod.invoke(systemInfo, parameterValues);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * As of OpenCms 8.0.x, upgrading the OpenCms runlevel now requires CmsParameterConfiguration. Prior versions used
	 * ExtendedProperties. We need to determine which class exists runtime and call the OpenCmsCore#upgradeRunlevel method
	 * with the correct parameter type.
	 * 
	 * @param opencms the OpenCms instance
	 * @param cmsPropertyPath the path where the properties are located
	 */
	public static void upgradeRunlevel(OpenCmsCore opencms, String cmsPropertyPath) {
		try {
			IPreferenceStore	preferenceStore		= OpenCmsModuleDeveloperPlugin.getInstance().getPreferenceStore();
			String				webinfLocation		= preferenceStore.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR);
			ClassLoader			threadClassLoader	= Thread.currentThread().getContextClassLoader();
			File				folder				= new File(webinfLocation+File.separator+"lib");
			FilenameFilter		jarFilter			= new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean accepted = false;
					
					if (name.toLowerCase().endsWith(".jar")) {
						if (name.toLowerCase().contains("lucene")) {
							accepted = true;
						}
						else if (name.toLowerCase().contains("jpa")) {
							accepted = true;
						}
					}
					
					return accepted;
				}
			};
			
			File[]	listOfJarFiles	= folder.listFiles(jarFilter);
			URL[]	jarUrls			= new URL[listOfJarFiles.length];
					
			for (int i=0; i < listOfJarFiles.length; i++) {
				jarUrls[i] = listOfJarFiles[i].toURI().toURL();
			}
			
			URLClassLoader urlClassLoader = new URLClassLoader(jarUrls, threadClassLoader);

			Thread.currentThread().setContextClassLoader(urlClassLoader);
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		
	    opencmsClassloader = Thread.currentThread().getContextClassLoader();
	    
	    Object propertyContainer = null;
	    
	    try {
    	    try {
    	        //the 8.0.2 and newer way
    	        Class<?> cmsParameterConfiguration = Class.forName("org.opencms.configuration.CmsParameterConfiguration", true, opencmsClassloader); //call constructor
    	        Constructor<?> cmsParamConfigConstructor = cmsParameterConfiguration.getDeclaredConstructor(String.class);
    	        propertyContainer = cmsParamConfigConstructor.newInstance(cmsPropertyPath);
    	    } catch (ClassNotFoundException ex) {
    	        //when class CmsParameterConfiguration is unknown, we are working with OpenCms prior to 8.0.2
    	        Class<?> cmsPropertyUtils = Class.forName("org.opencms.util.CmsPropertyUtils", true, opencmsClassloader); //call loadProperties
    	        Method loadMethod = cmsPropertyUtils.getDeclaredMethod("loadProperties", String.class);
    	        propertyContainer = loadMethod.invoke(null, cmsPropertyPath);  //call static method
    	    }
	    } catch (Exception e) {
	        throw new RuntimeException("Cannot upgrade Runlevel of OpenCms", e);
	    }
	    
	    if (propertyContainer != null) {
    	    try {
    	    	Method upgradeMethod = opencms.getClass().getDeclaredMethod("upgradeRunlevel", propertyContainer.getClass());
    	    	upgradeMethod = makeAccessible(upgradeMethod);
                upgradeMethod.invoke(opencms, propertyContainer);
            } catch (Exception e) {
                throw new RuntimeException("Cannot upgrade Runlevel of OpenCms", e);
            }
	    }
	}
	
    /**
     * <p>In OpenCms 6.x, publishing a resource is simply telling the resource to publish itself. As of OpenCms 7.x,
     * a fancy, shiny new PublishManager is needed to publish the resource. As of OpenCms 8.x, the 6.x way is removed
     * from the code.</p>
     * TODO: monitor performance, because there is a lot of reflection going on when there are a lot of resources to publish
     * @param cms the resource to publish
     * @param parentFolderName the name of the folder the resource resides
     * @throws Exception any exception that occurs is propagated
     */
    public static void publishResource(CmsObject cms, String parentFolderName) throws Exception {
        Class<?> openCmsClass = Class.forName("org.opencms.main.OpenCms", true, opencmsClassloader);
        Method getPublisherMethod = findUniqueMethod("getPublishManager", openCmsClass.getDeclaredMethods());
        if (getPublisherMethod != null) {
            Object publishManager = getPublisherMethod.invoke(null);  //call static method
            Method publishMethod = publishManager.getClass().getDeclaredMethod("publishResource", cms.getClass(), String.class);
            publishMethod.invoke(publishManager, cms, parentFolderName);
        } else {
            //Publish 6.x style
            Method publishMethod = cms.getClass().getDeclaredMethod("publishResource", String.class);
            publishMethod.invoke(cms, parentFolderName);
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
		return makeAccessible(targetMethod);
	}
	
	protected static Method makeAccessible(Method targetMethod) {
		//Make sure we have access to the method
		if (targetMethod != null) {
	        if (!Modifier.isPublic(((Member)targetMethod).getModifiers()) || !Modifier.isPublic(((Member)targetMethod).getDeclaringClass().getModifiers())) {
	            targetMethod.setAccessible(true);
	        }
		}
		return targetMethod;
	}

}
