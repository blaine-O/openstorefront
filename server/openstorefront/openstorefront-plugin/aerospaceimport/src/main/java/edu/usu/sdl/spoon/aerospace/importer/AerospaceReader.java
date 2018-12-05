/*
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.spoon.aerospace.importer;


import edu.usu.sdl.openstorefront.core.model.FileHistoryAll;
import edu.usu.sdl.openstorefront.core.spi.parser.reader.GenericReader;
import edu.usu.sdl.spoon.aerospace.importer.model.Product;
import edu.usu.sdl.spoon.aerospace.importer.model.Services;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author bmichaelis
 */
public class AerospaceReader 
        extends GenericReader<Product>
{
    public static final String XMLFILE = "recordsList.xml";
    private ZipFile zipFile;
    private List<Product> productList = new ArrayList<>();
    private Iterator<Product> productIterator;

    public AerospaceReader(InputStream in, FileHistoryAll fileHistoryAll) throws IOException {
        super(in);
        
        zipFile = new ZipFile(fileHistoryAll.getFileHistory().pathToFileName().toFile());       
        
    }
    
   public void preProcess() {
        try {
            AerospaceXMLParser aeroXMLParser = new AerospaceXMLParser();
            ZipEntry zipEntry = zipFile.getEntry(XMLFILE);
            Services services = aeroXMLParser.parseXML(zipFile.getInputStream(zipEntry));
            productList.addAll(services.getProducts());
            productIterator = productList.iterator();
        } catch (IOException ex) {
            Logger.getLogger(AerospaceReader.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
       
   }

    @Override
    public Product nextRecord() {
        return productIterator.next();
    }
    
    
    @Override
    public void close() throws Exception
    {
        super.close();
        zipFile.close();
    }
   
    
}