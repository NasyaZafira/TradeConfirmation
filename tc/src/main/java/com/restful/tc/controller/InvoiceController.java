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

    @GetMapping("/generate-invoices")
    public ResponseEntity<String> generateInvoices() {
        try {
            invoiceService.generateInvoicesPdf();
            return ResponseEntity.ok("PDF invoices generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating PDF invoices: " + e.getMessage());
        }
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
}
