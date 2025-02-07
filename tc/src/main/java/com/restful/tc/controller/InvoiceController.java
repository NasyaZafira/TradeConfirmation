package com.restful.tc.controller;

import com.restful.tc.model.Invoice;
import com.restful.tc.service.InvoiceService;
import com.restful.tc.service.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices") // Menambahkan base path untuk controller
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PDFGenerator pdfGenerator;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, PDFGenerator pdfGenerator) {
        this.invoiceService = invoiceService;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping("/generate-and-download")
    public String generateAndDownloadInvoices() throws IOException {

        // Panggil service untuk menghasilkan PDF dan ZIP
        invoiceService.generateInvoicesPdf();

        return "OK";
        /*

        // Path ke file ZIP yang dihasilkan
        String zipFilePath = System.getProperty("java.io.tmpdir") + File.separator + "invoices.zip";
        File zipFile = new File(zipFilePath);

        // Jika file ZIP berhasil dibuat, kirim sebagai respons
        if (zipFile.exists()) {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoices.zip"); // Nama file yang akan diunduh
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE); // Tipe konten untuk file biner

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(zipFile.length())
                    .body(resource);
        } else {
            // Jika file ZIP tidak ditemukan, kembalikan respons error
            return ResponseEntity.status(500)
                    .body(null);
        }

         */
    }

    @PostMapping("/generate-massive-pdfs")
    public ResponseEntity<byte[]> generateMultiplePDFs(@RequestBody List<Invoice> invoices) {
        try {
            byte[] zipContent = pdfGenerator.generatePDFs(invoices);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=invoices.zip")
                    .body(zipContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World";
    }
}
