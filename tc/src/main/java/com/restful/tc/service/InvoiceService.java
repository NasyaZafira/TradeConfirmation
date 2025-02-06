package com.restful.tc.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.restful.tc.html.InvoiceHtmlGenerator;
import com.restful.tc.model.Invoice;
import com.restful.tc.repository.InvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private InvRepository invoiceRepository;
    @Autowired
    private InvoiceHtmlGenerator invoiceHtmlGenerator;


    private final List<String> pdfFileNames = new CopyOnWriteArrayList<>();

    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), // core pool size
            Runtime.getRuntime().availableProcessors() * 2, // maximum pool size
            60L, // keep-alive time for excess threads
            TimeUnit.SECONDS, // time unit for keep-alive time
            new LinkedBlockingQueue<>() // queue for holding tasks before they are executed
    );

    private String createPdf(Invoice invoice, String tempDir) throws IOException, DocumentException {
        String fileName = tempDir + File.separator + "invoice_" + invoice.getNoInvoice() + ".pdf";
        int count = 1;
        File file = new File(fileName);
        while (file.exists()) {
            fileName = tempDir + File.separator + "invoice_" + invoice.getNoInvoice() + "_" + count + ".pdf";
            file = new File(fileName);
            count++;
        }

        // Convert HTML to PDF
        String htmlContent = invoiceHtmlGenerator.generateHtml(invoice);
        System.out.println("HTML Content: " + htmlContent); // Debug HTML content
        HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(fileName));
        System.out.println("PDF created: " + fileName); // Log the creation of the PDF

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
//        List<Invoice> invoices = invoiceRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findFirst5ByOrderByNoInvAsc();
        String tempDir = System.getProperty("java.io.tmpdir"); // Direktori sementara untuk menyimpan file PDF
        CountDownLatch latch = new CountDownLatch(invoices.size());

        for (Invoice invoice : invoices) {
            executorService.submit(() -> {
                System.out.println("ON WORKING Thread Name: " + Thread.currentThread().getName());
                try {
                    String fileName = createPdf(invoice, tempDir);
                    pdfFileNames.add(fileName);

                } catch (IOException | DocumentException e) {
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