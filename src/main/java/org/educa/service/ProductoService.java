package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;
import org.xml.sax.SAXException;

import java.io.*;
import java.lang.reflect.Field;
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

        // miramos si la carptea existe y sino la crea

        File carpetaDestino = new File(path);
            if (!carpetaDestino.exists()) {
                carpetaDestino.mkdir();
            }

            // escribimos

            File ficheroTxt = new File(path + "resultado" + fecha + ".txt");
            FileWriter fw = new FileWriter(ficheroTxt);
            BufferedWriter bw = new BufferedWriter(fw);

            // el elemento toPrint no da el formato que quermeos
            bw.write(resumen.toPrint());

            bw.close();
            fw.close();
    }

    /**
     * Exporta los datoss a un excel usando Apache POI
     *
     * @param path carpeta destino
     * @param fileXml archivo de origen
     * @throws JAXBException
     * @throws IOException
     * @throws ParseException
     */
    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        List<ProductoEntity> listaProductos = readFile(fileXml);
        java.io.File archivoXml = new java.io.File(fileXml);
        String fecha = archivoXml.getName().replace("inventario_", "").replace(".xml", "");

        org.apache.poi.ss.usermodel.Workbook libro = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet hoja = libro.createSheet("Inventario");

        //estilo cabecera en negrita
        org.apache.poi.ss.usermodel.CellStyle estiloCabecera = libro.createCellStyle();
        org.apache.poi.ss.usermodel.Font fuenteNegrita = libro.createFont();
        fuenteNegrita.setBold(true);
        estiloCabecera.setFont(fuenteNegrita);
        estiloCabecera.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

        //estilos para los colores alternos
        org.apache.poi.ss.usermodel.CellStyle estiloFilaPar = libro.createCellStyle();
        estiloFilaPar.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        estiloFilaPar.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        org.apache.poi.ss.usermodel.CellStyle estiloFilaImpar = libro.createCellStyle();

        org.apache.poi.ss.usermodel.Row filaCabecera = hoja.createRow(0);
        String[] cabeceras = {"Codigo", "Numero de Serie", "Precio", "Descuento", "Precio Final", "Costes Envio", "Costes Almacenaje", "Beneficio"};
        for (int i = 0; i < cabeceras.length; i++) {
            org.apache.poi.ss.usermodel.Cell celda = filaCabecera.createCell(i);
            celda.setCellValue(cabeceras[i]);
            celda.setCellStyle(estiloCabecera);
        }

        int numFila = 1;
        for (ProductoEntity p : listaProductos) {
            Row fila = hoja.createRow(numFila);
            fila.createCell(0).setCellValue(p.getProducto().getCodigo());
            fila.createCell(1).setCellValue(p.getProducto().getNumeroSerie());
            fila.createCell(2).setCellValue(p.getProducto().getPrecio().doubleValue());
            fila.createCell(3).setCellValue(p.getProducto().getDescuento().doubleValue() / 100);
            fila.createCell(4).setCellValue(p.getPrecioFinal().doubleValue());
            fila.createCell(5).setCellValue(p.getProducto().getCostes().getCostesEnvio().doubleValue());
            fila.createCell(6).setCellValue(p.getProducto().getCostes().getCostesAlmacenaje().doubleValue());
            fila.createCell(7).setCellValue(p.getProfit().doubleValue());

            //aplicamos los colores distintos por fila
            CellStyle estiloActual = (numFila % 2 == 0) ? estiloFilaPar : estiloFilaImpar;
            for (int i = 0; i < cabeceras.length; i++) {
                fila.getCell(i).setCellStyle(estiloActual);
            }
            numFila++;
        }

        // el tamaño de las columnas
        for (int i = 0; i < cabeceras.length; i++) {
            hoja.autoSizeColumn(i);
        }

        File carpetaDestino = new File(path);
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdir();
        }

        FileOutputStream archivoSalida = new FileOutputStream(path + "exportado_" + fecha + ".xlsx");
        libro.write(archivoSalida);

        archivoSalida.close();
        libro.close();
    }
}
