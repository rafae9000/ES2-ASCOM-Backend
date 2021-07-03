package com.ES2.ASCOM.helpers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;

public class ConfigValues {

	private String clippingFolder;
	private String contatoFolder;
	
	public ConfigValues(){
		String applicationPath = this.getApplicationPath(); 
		this.clippingFolder = applicationPath + File.separator + "clipping-files" + File.separator;
		this.contatoFolder = applicationPath + File.separator + "contato-files" + File.separator;
		this.createFolder(this.clippingFolder);
		this.createFolder(this.contatoFolder);
	}
	
	/***
	 * Retorna o diretório dos arquivos do Clipping. 
	 * @return diretório dos arquivos do clipping.
	 */
	public String getClippingFolder() {
		return this.clippingFolder;
	}
	
	/**
	 * Retorna o diretório dos arquivos do contato.
	 * @return diretório dos arquivos do contato.
	 */
	public String getContatoFolder() {
		return this.contatoFolder;
	}
	
	/**
	 * 
	 */
	
	public void createFolder(String folder) {
		File dir = new File(folder);
		if(!dir.exists()) {
			dir.mkdir();
		}
	}
	
	public boolean folderExist(String folder) {
		File dir = new File(folder);
		if(!dir.exists()) {
			return false;
		}
		return true;
	}
	
	private String getApplicationPath() {
	        
	        Class<? extends ConfigValues> currentClass = this.getClass();
	        String nameClass = currentClass.getSimpleName() + ".class";
	        URL urlClass = currentClass.getResource(nameClass);
	        String classCurrentPath = urlClass.getPath(); 
	        
	        File currentClassFile = new File(classCurrentPath);
	        File dirCurrentClassFile = currentClassFile.getParentFile();
	        
	        String path = "";
	        	        
	        path = findJarParentPath(dirCurrentClassFile, "ASCOM-0.0.1-SNAPSHOT.jar");  
	        
	        try {  
	            return URLDecoder.decode(path, "UTF-8");  
	        }  
	        catch (UnsupportedEncodingException e) {                  
	            return path.replace("%20", " ");  
	        }  
  }

	 private String findJarParentPath(File jarFile, String projectJar) {    
	        String [] paths = null; 
	        String files = "";
		 	do {
	        	paths = jarFile.getParentFile().list();
	        	files = Arrays.toString(paths);
	            jarFile = jarFile.getParentFile();   
	        }while(jarFile != null && !files.contains(projectJar));
	        
	        return jarFile.getPath();  
	 }   
}
