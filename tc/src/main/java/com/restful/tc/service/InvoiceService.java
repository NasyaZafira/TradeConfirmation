package com.restful.tc.service;


import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.restful.tc.model.Invoice;
import com.restful.tc.repository.InvRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    @Autowired
    private InvRepository invoiceRepository;
//    @Autowired
//    private InvoiceHtmlGenerator invoiceHtmlGenerator;


    private final List<String> pdfFileNames = new CopyOnWriteArrayList<>();

    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), // core pool size
            Runtime.getRuntime().availableProcessors() * 2, // maximum pool size
            60L, // keep-alive time for excess threads
            TimeUnit.SECONDS, // time unit for keep-alive time
            new LinkedBlockingQueue<>() // queue for holding tasks before they are executed
    );

//    private String createPdf(String tempDir) throws IOException {
//        String fileName = tempDir + File.separator + "simple_invoice.pdf";
//        PdfWriter writer = new PdfWriter(fileName);
//        PdfDocument pdfDoc = new PdfDocument(writer);
//        Document doc = new Document(pdfDoc);
//        doc.add(new Paragraph("Hello World"));
//        doc.close();
//        return fileName;
//    }

    private String createPdf(Invoice invoice, String tempDir) throws IOException {
        String fileName = tempDir + File.separator + "TC_" + invoice.getNoCust() + ".pdf";

        File imgFile = ResourceUtils.getFile("classpath:templates/template_header_footer.jpg");
        String imgPath = imgFile.getAbsolutePath();

        int count = 1;
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdfDoc = new PdfDocument(writer);
        PageSize pageSize = PageSize.A4;

        Document doc = new Document(pdfDoc, pageSize);

        doc.setMargins(80, 35, 50, 35);
        PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());
        ImageData imageData = ImageDataFactory.create(imgPath);
        canvas.addImageFittedIntoRectangle(imageData, pageSize, false);

        Table table1 = new Table(new float[]{15, 20, 20});
        table1.setWidth(UnitValue.createPercentValue(100));

        Cell cell = new Cell(1, 1).add(new Paragraph("TRADE CONFIRMATION"));
        cell.setHorizontalAlignment(HorizontalAlignment.CENTER);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setBorder(Border.NO_BORDER);
        table1.addCell(cell);


        float columnWidth[] = {80, 300, 100, 80};
        Table table2 = new Table(2);
        Cell cell2 = new Cell();
        cell2.add(new Paragraph("To"));
        cell2.setBorder(Border.NO_BORDER);
        table2.addCell(cell2);


        doc.add(table1);
        doc.add(table2);
        doc.close();
        System.out.println("PDF created: " + fileName); // Log the creation of the PDF


        // Convert HTML to PDF
//        String htmlContent = invoiceHtmlGenerator.generateHtml(invoice);
//        System.out.println("HTML Content: " + htmlContent); // Debug HTML content
//        HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(fileName));
//        System.out.println("PDF created: " + fileName); // Log the creation of the PDF
//
        return fileName;
    }

    private void createZip(String tempDir) {
        String downloadsDir = System.getProperty("user.home") + File.separator + "Downloads";
        String zipFileName = downloadsDir + File.separator + "invoices.zip";

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            for (String pdfFileName : pdfFileNames) {
                File pdfFile = new File(pdfFileName);
                if (pdfFile.exists()) { // Periksa apakah file PDF ada
                    try (FileInputStream fis = new FileInputStream(pdfFile)) {
                        ZipEntry zipEntry = new ZipEntry(pdfFile.getName());
                        zipOut.putNextEntry(zipEntry);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zipOut.write(buffer, 0, length);
                        }
                        zipOut.closeEntry();
                    }
                    // Hapus file PDF setelah di-zip
                    pdfFile.delete();
                } else {
                    System.err.println("PDF berhasil dihapus dari Local temp: " + pdfFileName);
                }
            }
            System.out.println("ZIP file created: " + zipFileName);
        } catch (IOException e) {
            System.err.println("Error creating ZIP file: " + e.getMessage());
        }
    }

    public void generateInvoicesPdf() {
        //List<Invoice> invoices = invoiceRepository.findDistinctNoCustByToday(); // kalau sudah live prod pakai ini
        List<Invoice> invoices = invoiceRepository.finddistinct10Nocustorderbydtinvdesc();


        for (Invoice invoice : invoices) {
            log.info("Cust No: {}", invoice.getNoCust());
        }
        // System.exit(0);

        String tempDir = System.getProperty("java.io.tmpdir"); // Direktori sementara untuk menyimpan file PDF
//        for (Invoice invoice : invoices) {
//            try {
//                String fileName = createPdf(invoice, tempDir);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }

        CountDownLatch latch = new CountDownLatch(invoices.size());

//        int count = 1;
        for (Invoice invoice : invoices) {
//            int finalCount = count;
            executorService.submit(() -> {
                System.out.println("ON WORKING Thread Name: " + Thread.currentThread().getName());
                try {
                    String fileName = createPdf(invoice, tempDir);
                    pdfFileNames.add(fileName);

                } catch (Exception e) {
                    System.err.println("Error creating PDF for invoice " + invoice.getNoInvoice() + ": " + e.getMessage());
                } finally {
                    latch.countDown(); // Decrement the latch count
                }
            });
        }
        try {
            latch.await();
            System.out.println("All threads completed. Proceeding to create ZIP.");// Wait for all threads to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while waiting for PDF generation to complete.");
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        createZip(tempDir);


    }


}