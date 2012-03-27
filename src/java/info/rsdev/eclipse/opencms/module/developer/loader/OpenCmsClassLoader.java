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
package info.rsdev.eclipse.opencms.module.developer.loader;

import info.rsdev.eclipse.opencms.module.developer.loader.util.ClassMetaInformation;
import info.rsdev.eclipse.opencms.module.developer.loader.util.ManifestInformation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * This classloader adds a layer on top of the normal classloader in that it can
 * load OpenCms class files without OpenCms being added to the classpath at startup 
 * time. The location of the OpenCms libraries are set by the use in the preferences
 * and are   
 * 
 * @author Dave Schoorl
 *
 */
public class OpenCmsClassLoader extends ClassLoader
{
	static Boolean createNewClassLoaderInstance = Boolean.TRUE;
	
	private static OpenCmsClassLoader openCmsClassLoader = null;
	
	private OpenCmsClasspathManager openCmsClasspathManager = OpenCmsClasspathManager.getInstance();
	
	private OpenCmsClassLoader() {
		super(OpenCmsClassLoader.class.getClassLoader());
	}
	
	protected Class<?> findClass(String className) throws ClassNotFoundException {
		Class<?> target = null;
		
		byte[] classBytes = null;
		if ("org.opencms.main.Communicator".equals(className)) {
			try {
				String searchClassName = className.replaceAll("\\.", "/");
				searchClassName += ".class";
				InputStream stream = getClass().getClassLoader().getResourceAsStream(searchClassName);
				if (stream != null) {
					classBytes = openCmsClasspathManager.readByteArray(stream);
				}
			} catch (IOException e) {
				ClassNotFoundException cnfe = new ClassNotFoundException(e.getMessage());
				cnfe.setStackTrace(e.getStackTrace());
				throw cnfe;
			}
		}
		
		if (classBytes == null) {
			ClassMetaInformation classMetaInformation = openCmsClasspathManager.getClassMetaInformation(className);
			if (classMetaInformation != null) {
				definePackage(classMetaInformation);
				classBytes = classMetaInformation.getClassBytes();
			}
		}

		if (classBytes != null) {
			target = defineClass(className, classBytes, 0, classBytes.length);
			//System.out.println("OpenCmsClassLoader just loaded: "+className);
		} else {
			//System.out.println("OpenCmsClassLoader could not load: "+className);
		}
		
		return target;
	}
	
	protected URL findResource(String resourceName) {
		return openCmsClasspathManager.getResourceURL(resourceName);
	}
	
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		
		// First, check if this classloader can resolve  class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			if ("org.opencms.main.Communicator".equals(name)) {
				c = findClass(name);
			}
		}
			
		if (c == null) {
			//System.out.println("OpenCmsClassLoader did not previously load this class: '"+name+"'. Delegating loading.");
			return super.loadClass(name, resolve);
		}
		
		if (resolve) {
		    resolveClass(c);
		}
		
		return c;
	}
	
	protected void definePackage(ClassMetaInformation classMetaInformation) {
	    // Extract the package name from the class name
		String className = classMetaInformation.getClassName();
	    String packageName = className;
	    int index = className.lastIndexOf('.');
	    if (index >= 0) 
	    {
	      packageName =  className.substring(0, index);
	    }
	    
	    //Pre-conditions: jar from which the class is loaded must have a manifest and 
	    //the package is not previously defined
	    if ((classMetaInformation.getManifestInformation().hasManifest() == false) || 
	    		(getPackage(packageName) != null)) 
	    {
	      return;
	    }
	    
	    ManifestInformation manifest = classMetaInformation.getManifestInformation();
	    String specTitle = manifest.getSpecTitle();
	    String specVersion = manifest.getSpecVersion();
	    String specVendor = manifest.getSpecVendor();
	    String implTitle = manifest.getImplTitle();
	    String implVersion = manifest.getImplVersion();
	    String implVendor = manifest.getImplVendor();
	    definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, null);
	}
	
	public static OpenCmsClassLoader getInstance() {
		synchronized (createNewClassLoaderInstance) {
			if (createNewClassLoaderInstance.equals(Boolean.TRUE)) {
				openCmsClassLoader = new OpenCmsClassLoader();	//Will the old one be GC'ed?
				createNewClassLoaderInstance = Boolean.FALSE;
			}
		}
		
		return openCmsClassLoader;
	}
	
	public static void markInvalid() {
		synchronized (createNewClassLoaderInstance) {
			createNewClassLoaderInstance = Boolean.TRUE;
		}
	}
}
