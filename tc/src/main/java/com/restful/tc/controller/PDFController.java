package com.restful.tc.controller;

import com.restful.tc.PDFGenerator;
import com.restful.tc.model.Invoice;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PDFController {
    private final PDFGenerator pdfGenerator;

    public PDFController(PDFGenerator pdfGenerator) {
        this.pdfGenerator = pdfGenerator;
    }

    @PostMapping("/generate-massive-pdfs")
    public ResponseEntity<byte[]> generateMultiplePDFs(@RequestBody List<Invoice> invoices) {
        try {
            byte[] zipContent = pdfGenerator.generatePDFs(invoices);
            return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=invoices.zip")
                    .body(zipContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}