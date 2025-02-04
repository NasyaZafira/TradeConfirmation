package com.restful.tc.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.restful.tc.model.Invoice;
import com.restful.tc.repository.InvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    private InvRepository invoiceRepository;

    public void generateInvoicesPdf() throws IOException, DocumentException {
        List<Invoice> invoices = invoiceRepository.findAll();

        for (Invoice invoice : invoices) {
            Document document = new Document();
            String fileName = "invoice_" + invoice.getNoInvoice() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            document.add(new Paragraph("Invoice ID: " + invoice.getNoInvoice()));
            document.add(new Paragraph("Client Name: " + invoice.getNoCust()));
            document.add(new Paragraph("Invoice Date: " + invoice.getDate()));
            document.close();
        }
    }
}
