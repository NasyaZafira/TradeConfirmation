package com.restful.tc.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.restful.tc.model.Invoice;
import com.restful.tc.repository.InvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class InvoiceService {

    @Autowired
    private InvRepository invoiceRepository;

    private final List<String> pdfFileNames = new ArrayList<>();

    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), // core pool size
            Runtime.getRuntime().availableProcessors() * 2, // maximum pool size
            60L, // keep-alive time for excess threads
            TimeUnit.SECONDS, // time unit for keep-alive time
            new LinkedBlockingQueue<>() // queue for holding tasks before they are executed
    );

    private String createPdf(Invoice invoice, String tempDir) throws IOException, DocumentException {
        Document document = new Document();
        String fileName = tempDir + File.separator + "invoice_" + invoice.getNoInvoice() + ".pdf";

        int count = 1;
        File file = new File(fileName);
        while (file.exists()) {
            fileName = tempDir + File.separator + "invoice_" + invoice.getNoInvoice() + "_" + count + ".pdf";
            file = new File(fileName);
            count++;
        }

        PdfWriter.getInstance(document, new FileOutputStream(fileName));

        document.open();
        document.add(new Paragraph("Invoice ID: " + invoice.getNoInvoice()));
        document.add(new Paragraph("Client Name: " + invoice.getNoCust()));
        document.add(new Paragraph("Invoice Date: " + invoice.getDate()));
        document.close();

        System.out.println("PDF created: " + fileName); // Log the creation of the PDF
        return fileName;
    }

    private void createZip(String tempDir) {
        String zipFileName = tempDir + File.separator + "invoices.zip";
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            for (String pdfFileName : pdfFileNames) {
                File pdfFile = new File(pdfFileName);
                if (pdfFile.exists()) { // Check if the file exists
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
                    System.err.println("File not found: " + pdfFileName);
                }
            }
            System.out.println("ZIP file created: " + zipFileName);
        } catch (IOException e) {
            System.err.println("Error creating ZIP file: " + e.getMessage());
        }
    }

    public void generateInvoicesPdf() {
//        List<Invoice> invoices = invoiceRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findFirst10ByOrderByNoInvAsc();
        CountDownLatch latch = new CountDownLatch(invoices.size());
        String tempDir = System.getProperty("java.io.tmpdir"); // Direktori sementara untuk menyimpan file PDF

        for (Invoice invoice : invoices) {
            executorService.submit(() -> {
                System.out.println("ON WORKING Thread Name: " + Thread.currentThread().getName());
                try {
                    String fileName = createPdf(invoice, tempDir);
                    synchronized (pdfFileNames) { // Sinkronisasi untuk menghindari masalah thread
                        pdfFileNames.add(fileName);
                    }
                } catch (IOException | DocumentException e) {
                    System.err.println("Error creating PDF for invoice " + invoice.getNoInvoice() + ": " + e.getMessage());
                } finally {
                    latch.countDown(); // Decrement the latch count
                }
            });
        }
        try {
            latch.await(); // Wait for all threads to finish
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