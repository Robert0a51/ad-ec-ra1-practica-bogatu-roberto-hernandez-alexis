package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
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

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        //TODO: Implementar

    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
