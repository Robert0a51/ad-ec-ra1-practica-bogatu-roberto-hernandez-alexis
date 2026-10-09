package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {

    /**
     * LLamamos al dao
     * @param fileXml la ruta del fichero xml
     * @return los calculos hechos
     * @throws JAXBException la excepcion por so ocurre un error
     */
    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        ProductoDAO productoDAO = new ProductoDAOImpl();
        List<ProductoEntity> listaFinal = new ArrayList<>();

        try {
            Productos productosXML = productoDAO.getProductos();
            BigDecimal cien = new BigDecimal("100");

            for (Producto p : productosXML.getProducto()) {
                ProductoEntity entidad = new ProductoEntity();
                entidad.setProducto(p);

                // hacemos los calculos
                BigDecimal descuentoCalculado = p.getPrecio().multiply(p.getDescuento()).divide(cien, 2, RoundingMode.HALF_UP);
                BigDecimal precioFinal = p.getPrecio().subtract(descuentoCalculado);
                entidad.setPrecioFinal(precioFinal);

                BigDecimal coste = p.getCostes().getCostesEnvio().add(p.getCostes().getCostesAlmacenaje());
                entidad.setCost(coste);

                BigDecimal beneficio = precioFinal.subtract(coste);
                entidad.setProfit(beneficio);

                listaFinal.add(entidad);
            }
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return listaFinal;
    }


    /**
     * genera el txt con beneficios del inventario
     * @param path Carpeta destino
     * @param fileXml
     * @throws JAXBException por si falla la lectura
     * @throws IOException por si falla al escribir el txt
     */
    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        List<ProductoEntity> listaProductos = readFile(fileXml);

        java.math.BigDecimal beneficioTotal = new java.math.BigDecimal(0);
        for (ProductoEntity p : listaProductos) {
            beneficioTotal = beneficioTotal.add(p.getProfit());
        }

        java.io.File archivoXml = new java.io.File(fileXml);
        String nombreFicheroXML = archivoXml.getName();
        String fecha = nombreFicheroXML.replace("inventario_","").replace(".xml", "");
        String nombreSinExtension = nombreFicheroXML.replace(".xml","");

        SummaryEntity resumen = new SummaryEntity();
        resumen.setName(fecha);
        resumen.setNumberOfProducts(listaProductos.size());
        resumen.setTotalProfit(beneficioTotal);
        resumen.setFileAbsolutePath(archivoXml.getAbsolutePath());
        resumen.setFileName(nombreSinExtension);
        resumen.setFileSize(archivoXml.length());
    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
