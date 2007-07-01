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

import info.rsdev.eclipse.opencms.module.developer.OpenCmsModuleDeveloperPlugin;
import info.rsdev.eclipse.opencms.module.developer.loader.util.ClassMetaInformation;
import info.rsdev.eclipse.opencms.module.developer.preferences.OpenCmsModuleDeveloperPreferencePage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.Preferences;

/**
 * This class is responsible for managing the OpenCms classpath. It is a helper class 
 * for the OpenCmsClassLoader and it loads classes and resources from the jarfiles that 
 * make up the OpenCms codebase. It provides both the class bytes as well as package 
 * information from the manifest for versioning by the classloader.
 * 
 * When the classpath of OpenCms is changed by the user via the user preferences, this
 * class is responsible for invalidating the portions of the cache that are no longer
 * valid.
 * 
 * @author Dave Schoorl
 */
public class OpenCmsClasspathManager implements OpenCmsClasspathChangeListener
{
	private static final OpenCmsClasspathManager instance = new OpenCmsClasspathManager();
	
	private Map availableClasspathFiles = null;
	
	/* The jar file that most recently provided a classfile. Assume that this jar
	 * is the most likely candidate to provide the next requested jar.
	 */
	private File previousFileUsed = null;
	
	private OpenCmsClasspathManager() {
		Preferences preferences = OpenCmsModuleDeveloperPlugin.getDefault().getPluginPreferences();
		
		//Make all jar and zipfiles from the configured OpenCms classpath available to the OpenCmsClasspathManager
		String openCmsWebInfLocation = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_WEBINF_DIR);
		this.changeWebInfLocation(null, openCmsWebInfLocation);
		
		//Make all additionally configured Jar-entries from the preferences available to the OpenCmsClasspathManager
		List classpathEntries = new ArrayList();
		String additionalEntries = preferences.getString(OpenCmsModuleDeveloperPreferencePage.OPENCMS_ADDITIONAL_JARS);
		if ((additionalEntries != null) && (additionalEntries.length() > 0)) {
			String[] entries = additionalEntries.split("\\?");
			File[] fileEntries = new File[entries.length];
			if ((entries != null) && (entries.length > 0)) {
				for (int i = 0; i < entries.length; i++) {
					String entry = entries[i];
					File jarEntry = new File(entry);
					if (jarEntry.exists() && jarEntry.isFile() && (classpathEntries.contains(jarEntry) == false)) {
						classpathEntries.add(jarEntry);
					}
				}
			}
		}
		
		addToClasspath(classpathEntries);
	}
	
	public static OpenCmsClasspathManager getInstance() {
		return instance;
	}
	
	public void addToClasspath(List files) {
		if (files == null) { return; }
		if (availableClasspathFiles == null) {
			availableClasspathFiles = new HashMap(files.size());
		}
		
		for (int i=0;i<files.size(); i++) {
			File file = (File)files.get(i);
			if (file.exists()) {
				/* the value in the HashMap will be the jar files' Manifest, but this is 
				 * only set when the first class from that jarfile is loaded.
				 * The Manifest is cached in this Map for performance reasons, and to
				 * be able to determine if changes to the jarFiles are allowed (it is allowed
				 * for all jarFiles that did not provide any classFiles yet.
				 */				
				availableClasspathFiles.put(file, null);
			}
		}
	}
	
	public void removeFromClasspath(List files) {
		if ((availableClasspathFiles == null) || (files == null)) {
			return;
		}
		
		boolean invalidateClassLoader = false;
		for (int i=0;i<files.size(); i++) {
			File file = (File)files.get(i);
			if (availableClasspathFiles.containsKey(file)) {
				
				//Are classes loaded from this jarfile?
				if (availableClasspathFiles.get(file) != null) {
					invalidateClassLoader = true;
				}
				
				availableClasspathFiles.remove(file);
				
				if ((previousFileUsed != null) && (previousFileUsed.equals(file))) {
					previousFileUsed = null;
				}
			}
		}
		
		if (invalidateClassLoader) {
			OpenCmsClassLoader.markInvalid();
		}
	}

	
	public URL getResourceURL(String resourceName) {
		URL resourceURL = null;
		
		if (availableClasspathFiles == null) {
			return null;
		}
		
		//First try to locate the class file in the previously succesfully used jar file
		if (previousFileUsed != null) {
			try {
				resourceURL = getResourceURL(resourceName, previousFileUsed);
			} catch (IOException e) {
				//problems reading jar file: remove jar file from list of available jars
				List removeList = new ArrayList();
				removeList.add(previousFileUsed);
				removeFromClasspath(removeList);
				previousFileUsed = null;
			}
		}
		
		//If not found in the previously successfully used jar file, then search the other jar files 
		if (resourceURL == null) {
			File file = null;
			Iterator fileIterator = availableClasspathFiles.keySet().iterator();
			try {
				while ((fileIterator.hasNext()) && (resourceURL == null)) {
					file = (File)fileIterator.next();
					if (file.equals(previousFileUsed) == false) {
						resourceURL = getResourceURL(resourceName, file);
					}
				}
			} catch (IOException e) {
				//problems reading jar file: remove jar file from list of available jars
				if (file != null) {
					if (file.equals(previousFileUsed)) {
						previousFileUsed = null;
					}
				}
			}
		}
		
		return resourceURL;
	}
	
	private URL getResourceURL(String resourceName, File file) throws IOException {
		
		URL resourceURL = null;
		
		InputStream entryStream = null;
		if (isJarFile(file)) {
			JarFile jarFile = new JarFile(file);
			JarEntry jarEntry = jarFile.getJarEntry(resourceName);
			if (jarEntry != null) {
				//create an URL to the resource
				try {
					String fullFileName = file.getAbsolutePath();
					fullFileName.replaceAll("\\\\", "/");
					resourceURL = new URL("jar:file:/"+fullFileName+"!/"+resourceName);
				} catch (MalformedURLException e) {
					e.printStackTrace(System.out);
				}
				
			}
		} else {
			if (file.isDirectory()) {
				File classFile = new File(file, resourceName);
				if (classFile.exists()) {
					resourceURL = new URL("file:" + classFile.getAbsolutePath());
				}
			} else {
				//return null to indicate resource is not found in this classpath element
				//throw new IOException(Messages.exception_unsupported_file_type);
			}
		}
		return resourceURL;
	}
	
	private boolean isJarFile(File file) {
		boolean isJarFile = file.isFile();
		String fileName = file.getName().toLowerCase(); 
		isJarFile &= fileName.endsWith(".jar") || fileName.endsWith(".zip"); 
		return isJarFile; 
	}
	
	
	public ClassMetaInformation getClassMetaInformation(String className) {
		
		if (availableClasspathFiles == null) {
			return null;
		}
		
		ClassMetaInformation result = null;
		
		//First try to locate the class file in the previously succesfully used jar file
		if (previousFileUsed != null) {
			try {
				result = getClassMetaInformation(className, previousFileUsed);
			} catch (IOException e) {
//				problems reading jar file: remove jar file from list of available jars
				availableClasspathFiles.remove(previousFileUsed);
				previousFileUsed = null;
			}
		}
		
		//If not found in the previously successfully used jar file, then search the other jar files 
		if (result == null) {
			File jarFile = null;
			Iterator jarIterator = availableClasspathFiles.keySet().iterator();
			try {
				while ((jarIterator.hasNext()) && (result == null)) {
					jarFile = (File)jarIterator.next();
					if (jarFile.equals(previousFileUsed) == false) {
						result = getClassMetaInformation(className, jarFile);
					}
				}
			} catch (IOException e) {
				//problems reading jar file: remove jar file from list of available jars
				if (jarFile != null) {
					availableClasspathFiles.remove(jarFile);
					if (jarFile.equals(previousFileUsed)) {
						previousFileUsed = null;
					}
				}
			}
		}
		
		return result;
	}
	
	private ClassMetaInformation getClassMetaInformation(String className, File file) throws IOException {
		ClassMetaInformation result = null;
		Manifest manifest = null;
		byte[] classBytes = null;
		
		if (isJarFile(file)) {
			
			String searchClassName = className.replaceAll("\\.", "/"); 
			searchClassName += ".class";
			
			InputStream entryStream = null;
			try {
				JarFile jarFile = new JarFile(file);
				JarEntry jarEntry = jarFile.getJarEntry(searchClassName);
				if (jarEntry != null) {
					//read class bytes from jar file
					entryStream = jarFile.getInputStream(jarEntry);
					classBytes = readByteArray(entryStream);
					
					//read manifest and cache in list of available jar files for perfomance reasons 
					manifest = (Manifest)availableClasspathFiles.get(file);
					if (manifest == null) {
						manifest = jarFile.getManifest();
						if (manifest == null) {
							manifest = new Manifest();
						}
						//record the manifest for performance reasons
						availableClasspathFiles.put(file, manifest);
					}
					
					previousFileUsed = file;
					result = new ClassMetaInformation(className, manifest, classBytes);
				}
			} finally {
				if (entryStream != null) {
					entryStream.close();
				}
			}
		} else {
			if (file.isDirectory()) {
				String dummyManifest = (String)availableClasspathFiles.get(file);
				if (dummyManifest == null) {
					dummyManifest = "dummy-manifest";
				}
				//record the manifest for performance reasons
				availableClasspathFiles.put(file, dummyManifest);
			}
		}

		return result;
	}
	
	public byte[] readByteArray(InputStream stream) throws IOException {
		byte[] classBytes = null;
		ByteArrayOutputStream classReader = null;
		try {
			int bufferSize = 1000 * 41;
			byte[] buffer = new byte[bufferSize];
			classReader = new ByteArrayOutputStream(bufferSize);
			int totalBytesRead = 0;
			int bufferRead = 0;
			while ((bufferRead = stream.read(buffer,0,bufferSize)) >= 0) {
				totalBytesRead += bufferRead;
				classReader.write(buffer, 0, bufferRead);
			}
			classReader.flush();
			classBytes = classReader.toByteArray();
		} finally {
			if (classReader != null) {
				try {
					classReader.close();
				} catch (IOException e) {}	//swallow this exception
			}
		}
		
		return classBytes;
	}
	
	public void changeWebInfLocation(String oldDir, String newDir) {
		//first remove the entries contributed from the old WEB-INF dir
		if ((oldDir != null) && (oldDir.length() > 0) && (availableClasspathFiles != null)) {
			List removeClasspathEntries = new ArrayList();
			Iterator fileIterator = availableClasspathFiles.keySet().iterator();
			while (fileIterator.hasNext()) {
				File file = (File)fileIterator.next();
				String fileName = file.getPath();
				if (fileName.startsWith(oldDir)) {
					removeClasspathEntries.add(file);
				}
 			}
			removeFromClasspath(removeClasspathEntries);
		}
		
		//Then construct a list of jar files present in the new WEB-INF location
		if ((newDir != null) && (newDir.length() > 0)) {
			try {
				File openCmsWebInfDir = new File(newDir);
				if (openCmsWebInfDir.exists() && openCmsWebInfDir.isDirectory()) {
					File openCmsLibDir = new File(openCmsWebInfDir, "lib");
					List fileList = new ArrayList();
					if (openCmsLibDir.exists() && openCmsLibDir.isDirectory()) {
						File[] files = openCmsLibDir.listFiles(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								if ((name.endsWith(".jar")) || (name.endsWith(".zip"))) {
									File test = new File(dir, name);
									if (test.isFile()) {
										return true;
									}
								}
								
								return false;
							}
						});
						fileList.addAll(Arrays.asList(files));
					}
					
					File openCmsClassesDir = new File(openCmsWebInfDir, "classes");
					if (openCmsClassesDir.exists() && openCmsClassesDir.isDirectory()) {
						fileList.add(openCmsClassesDir);
					}
					addToClasspath(fileList);
				}
			} catch (Throwable t) {
				t.printStackTrace(System.out);
			}
		}
	}
	
	public void changeAdditionalJars(List oldJars, List newJars) {
		
		//determine which jars need to be deleted -- those that are present in oldJars, but not in newJars
		List removeJars = new ArrayList();	//list of File-objects that need to be removed from the OpenCmsClasspath
		if (oldJars != null) {
			Iterator oldJarIterator = oldJars.iterator();
			while (oldJarIterator.hasNext()) {
				String fileName = (String)oldJarIterator.next();
				if (newJars.contains(fileName) == false) {
					File file = new File(fileName);
					removeJars.add(file);
				} else {
					/* this else branche is just to increase preformance a little bit:
					 * unchanged entries will not get reevaluated when determining the
					 * list of jars to be added to the classpath
					 */
					newJars.remove(fileName);
				}
			}
		}
		
		//determine which jars need to be added -- those that are present in newJars, but not in oldJars
		List addJars = new ArrayList();	//list of File-objects that need to be added to the OpenCmsClasspath
		if (newJars != null) {
			Iterator newJarIterator = newJars.iterator();
			while (newJarIterator.hasNext()) {
				String fileName = (String)newJarIterator.next();
				if (oldJars.contains(fileName) == false) {
					File file = new File(fileName);
					addJars.add(file);
				}
			}
		}
		
		//process changes
		addToClasspath(addJars);
		removeFromClasspath(removeJars);
	}

}
