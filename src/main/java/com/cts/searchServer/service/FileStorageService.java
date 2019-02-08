package com.cts.searchServer.service;

import com.cts.searchServer.exception.FileStorageException;
import com.cts.searchServer.exception.MyFileNotFoundException;
import com.cts.searchServer.property.FileStorageProperties;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Properties;

import javax.inject.Inject;

@Service
public class FileStorageService {

    
    private final Path fileStorageLocation;
    
    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public StringBuffer storeFile(MultipartFile file) throws FileNotFoundException {
    	 	StringBuffer searchResults = new StringBuffer();
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }else if(!fileName.contains(".xlsx")){
            	throw new FileStorageException("Sorry! Filetype should be .xlsx format...");
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                   
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            System.out.println("File path-->"+filePath.toString());
            
            FileInputStream inputStream;
    		
    			String cellValue = null;
    			inputStream = new FileInputStream(new File(filePath.toString()));

    			Workbook workbook = new XSSFWorkbook(inputStream);
    			Sheet firstSheet = workbook.getSheetAt(0);
    			Iterator<Row> iterator = firstSheet.iterator();

    			while (iterator.hasNext()) {
    				Row nextRow = iterator.next();
    				Iterator<Cell> cellIterator = nextRow.cellIterator();

    				while (cellIterator.hasNext()) {
    					Cell cell = cellIterator.next();

    					switch (cell.getCellType()) {
    					case Cell.CELL_TYPE_STRING:
    						//System.out.print(cell.getStringCellValue().trim());
    						cellValue = cell.getStringCellValue().trim().toLowerCase();
    						break;
    					
    					}

    				}
    				String searchResult =	serverFromPropertyFile(cellValue);
    				if(searchResult!=null)
    				searchResults.append(searchResult+"<br>");
    			}

    			workbook.close();
    			inputStream.close();
            return searchResults;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    
    private static String serverFromPropertyFile(String serverName) {
    	
    	String searchResult = null;
    	
		/////////////////////////// Reading properties////////////////////////////////////////////////////
		try
						
			//(InputStream in = new FileInputStream((File) resourceLoader.getResource("classpath:BHPServers.properties")))  
			(InputStream in = new ClassPathResource("/BHPServers.properties").getInputStream()) { 
			String propertyValue = null;
			Properties prop = new Properties();
			prop.load(in);
			propertyValue = prop.getProperty(serverName);
			if(propertyValue!=null){
			// System.out.println(serverName+"="+propertyValue);
			  searchResult = serverName+"="+propertyValue;
			 
			
			 
			}
			// for (String property : prop.stringPropertyNames()) {
			// String value = prop.getProperty(property);
			//// System.out.println(property + "=" + value);
			// System.out.println(value);
			// }

//			Set<Object> keySet = prop.keySet();
//			TreeSet<Object> sortedSet = new TreeSet<Object>(keySet);
//			Iterator i = sortedSet.iterator();
//			while (i.hasNext()) {
//				String key = (String) i.next();
//				System.out.println(prop.getProperty(key).trim());
//
//			}
			
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 //System.out.println("searchResult in service layer-->"+searchResult);
		return searchResult;
		//////////////////////////////////////////////////////////////////////////////////////////////////
	}

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
