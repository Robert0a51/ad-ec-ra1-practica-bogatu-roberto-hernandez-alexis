package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * Clase que implementa la interfaz ProductoDAO para leer el XML
 */
public class ProductoDAOImpl implements ProductoDAO {

    private static final String PATH = "src/main/resources/xml/inventario_junio2026.xml";
    private static final String PATH_XSD = "src/main/resources/xsd/inventario_junio2026.xsd";

    /**
     * Obtiene los productos cargando el fichero XML y validandolo con el XSD
     * @return
     * @throws JAXBException
     * @throws SAXException
     */
    @Override
    public Productos getProductos() throws JAXBException, SAXException {
        File f = new File(PATH);

        JAXBContext contexto = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = contexto.createUnmarshaller();

        unmarshaller.setSchema(
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new File(PATH_XSD))
        );

        return (Productos) unmarshaller.unmarshal(f);
    }
}