package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

/**
 * Interfaz para seperar el acceso a datos del resto del programa
 * @return
 * @throws JAXBException
 * @throws SAXException
 */
public interface ProductoDAO {
    Productos getProductos() throws JAXBException, SAXException;
}