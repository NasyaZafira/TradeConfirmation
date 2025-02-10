package com.restful.tc.service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.restful.tc.model.Invoice;
import com.restful.tc.repository.InvRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
//
//        // Convert HTML to PDF
//
//    String htmlContent = invoiceHtmlGenerator.generateHtml(invoice);
//    System.out.println("HTML Content: " + htmlContent); // Debug HTML content
//    HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(fileName));
//    System.out.println("PDF created: " + fileName); // Log the creation of the PDF
//
//        return fileName;
//    }


    String createPdf(Invoice invoice, String tempDir) throws IOException {
        System.out.println("ceknasya getNoCust: " + invoice.getNoCust());
        String fileName = tempDir + File.separator + "TC_" + invoice.getNoCust() + ".pdf";
        String imgFile = "classpath:templates/logo_bcas.png";


        try {
            int count = 1;
            com.itextpdf.text.Rectangle pageSize = PageSize.A4;
            com.itextpdf.text.Document doc = new Document(pageSize);
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font normalBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font nineFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            doc.open();

            //TABLE CORPORATE
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100); // Full width
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            float[] columnWidth = {6f, 4f};
            table.setWidths(columnWidth);

            Paragraph headreParagraph = new Paragraph();
            headreParagraph.setFont(nineFont);
            headreParagraph.add("PT. BCA Sekuritas\n");
            headreParagraph.add("Menara BCA - Grand Indonesia 41st Floor Suite 4101\n");
            headreParagraph.add("Jl. M.H. Thamrin No 1 Jakarta 10310\n");
            headreParagraph.add("Phone: 2358 7222 (Hunting). 2358 7277 (Sales) ; Fax 2358 7250\n");
            headreParagraph.add("NPWP : 01.357.392.8-054.000\n");

            // column 1
            PdfPCell cell01 = new PdfPCell(headreParagraph);
            cell01.setBorder(PdfPCell.NO_BORDER);
            cell01.setBorderWidthBottom(1f);
            cell01.setPaddingBottom(10f);
            table.addCell(cell01);

            //column2
            Image image = Image.getInstance(imgFile);
            image.scaleToFit(150, 150); // Mengubah ukuran gambar jika perlu
            PdfPCell cell2 = new PdfPCell(image);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER); // Mengatur posisi gambar di tengah
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE); // Mengatur posisi gambar di tengah vertikal
            cell2.setBorder(PdfPCell.NO_BORDER);
            cell2.setBorderWidthBottom(1f);
            cell2.setPaddingBottom(10f);
            table.addCell(cell2);

            doc.add(table);

            // header ===============================================================================
            com.itextpdf.text.Paragraph header = new com.itextpdf.text.Paragraph("TRADE CONFIRMATION", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            doc.add(header);

            // TABLE 1 ===============================================================================
            PdfPTable table1 = new PdfPTable(4); // 5 columns
            table1.setWidthPercentage(100); // Full width
            table1.setSpacingBefore(10f);
            table1.setSpacingAfter(10f);
            float[] columnWidths = {3f, 7f, 4f, 6f};
            table1.setWidths(columnWidths);

            // String[] nnti bisa diganti pakai hasil get dari databasenya aja
            String[] tab1col1 = {"To", "Address", "", "", "Phone/Fax", "Email", "Bank"};
            String[] tab1col2 = {"jefri", "jl siantan", "rt rw", "jakbar", "0/", "jeff@gmail", "BCA 12345"};
            String[] tab1col3 = {"Document No", "Transaction Date", "Settlement Date", "Currency", "Office", "Sales Person", "SID", "CBest Account", "Commission"};
            String[] tab1col4 = {"25012298BO", "Wednesday, 22-Jan-2025", "Friday, 24-Jan-2025", "IDR", "04", "online", "IDD1508ZD554162", "SQ00198BO00146", "0.1815 %"};

            for (int i = 0; i <= tab1col4.length; i++) {
                PdfPCell cell = new PdfPCell();

                // column 1
                if (i < tab1col1.length) {
                    cell = new PdfPCell(new Phrase(tab1col1[i], normalFont));
                } else {
                    cell = new PdfPCell(new Phrase("", normalFont));
                }
                cell.setBorder(PdfPCell.NO_BORDER);
                table1.addCell(cell);

                // column 2
                if (i < tab1col1.length) {
                    cell = new PdfPCell(new Phrase(": " + tab1col2[i], normalFont));
                } else {
                    cell = new PdfPCell(new Phrase("", normalFont));
                }
                cell.setBorder(PdfPCell.NO_BORDER);
                table1.addCell(cell);

                //  column 3
                if (i < tab1col3.length) {
                    cell = new PdfPCell(new Phrase(tab1col3[i], normalFont));
                } else {
                    cell = new PdfPCell(new Phrase("", normalFont));
                }
                cell.setBorder(PdfPCell.NO_BORDER);
                table1.addCell(cell);

                // column 4
                if (i < tab1col4.length) {
                    cell = new PdfPCell(new Phrase(": " + tab1col4[i], normalFont));
                } else {
                    cell = new PdfPCell(new Phrase("", normalFont));
                }
                cell.setBorder(PdfPCell.NO_BORDER);
                table1.addCell(cell);
            }

            doc.add(table1);

            // TEXT ==================================================================================================================================
            com.itextpdf.text.Paragraph paragraph = new Paragraph("This is to confirm that we have BOUGHT and SOLD for your account :", normalBoldFont);
            doc.add(paragraph);

            // TABLE 2 ==================================================================================================================================
            PdfPTable table2 = new PdfPTable(7); // 7 columns
            table2.setWidthPercentage(100); // Full width
            table2.setSpacingBefore(10f);

            float[] columnWidths2 = {3f, 5f, 1f, 2f, 3f, 4f, 4f};
            table2.setWidths(columnWidths2);

            // string[] nanti diganti aja pake get data dari DB
            String[] tab2row1 = {"REF# board", "Securities", "Lots", "Shares", "Price", "Amount Buy", "Amount Sell"};

            String[] refBoard = {"403211 RG", "403211 RG", "403212 RG", "406840 RG", ""};
            String[] securities = {"BRPT-Barito Pacific Tbk.", "BRPT-Barito Pacific Tbk.", "SGER-Sumber Global Energy T", "", ""};
            Integer[] lots = {70, 15, 29, 380, 99};
            Integer[] price = {920, 925, 354, 124, 358};
            String[] isBuy = {"Y", "Y", "Y", "N", "N"};
            Integer[] amountBuy = {0, 0, 0, 0, 0};
            Integer[] amountSell = {0, 0, 0, 0, 0};

            Integer grossAmountBuy = 0;
            Integer grossAmountSell = 0;

            PdfPCell cell = new PdfPCell();
            String str = "";

            for (int i = 0; i < tab2row1.length; i++) {
                cell = new PdfPCell(new Phrase(tab2row1[i], normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                cell.setPaddingBottom(5);
                cell.setBorderWidthBottom(1f);
                table2.addCell(cell);
            }

            Integer n = refBoard.length; //banyak data

            for (int i = 0; i < n; i++) {
                // REF#board
                if (refBoard[i] != null && refBoard[i] != "") {
                    str = refBoard[i];
                } else {
                    str = "";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);

                // securities
                if (securities[i] != null && securities[i] != "") {
                    str = securities[i];
                } else {
                    str = "";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);

                // lots
                if (lots[i] != null) {
                    str = lots[i].toString();
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);

                // shares
                if (lots[i] != null) {
                    str = adjustNumberFormat(lots[i] * 100, 0);
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);

                // price
                if (price[i] != null) {
                    str = adjustNumberFormat(price[i], 4);
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);

                // amount buy
                if (lots[i] != null && price[i] != null && isBuy[i] == "Y") {
                    Integer amount = lots[i] * 100 * price[i];
                    str = adjustNumberFormat(amount, 0);
                    amountBuy[i] = amount;
                    grossAmountBuy += amount;
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);

                // amount sell
                if (lots[i] != null && price[i] != null && isBuy[i] == "N") {
                    Integer amount = lots[i] * 100 * price[i];
                    str = adjustNumberFormat(amount, 0);
                    amountSell[i] = amount;
                    grossAmountSell += amount;
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == n - 1) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                }
                table2.addCell(cell);
            }

            doc.add(table2);

            //TABLE 3 ==================================================================================================================================
            PdfPTable table3 = new PdfPTable(4); // 4 columns
            table3.setWidthPercentage(100); // Full width
            table3.setSpacingAfter(10f);

            float[] columnWidths3 = {7f, 7f, 3f, 3f};
            table3.setWidths(columnWidths3);

            Integer brokFeeBuy = 11024;
            Integer brokFeeSell = -10279;
            Integer pajak = 11 / 100;
            Integer idxLevyBuy = 2656; //(grossAmountBuy * 0,03) / 100
            Integer idxLevySell = -2477; //(grossAmountSell * 0,03) / 100
            Integer KPEIBuy = 885 ; //grossAmountBuy * 0,01 / 100
            Integer KPEISell = -826; //grossAmountSell * 0,01 / 100

            Integer totalChargeBuy = brokFeeBuy + brokFeeBuy * pajak + idxLevyBuy + idxLevyBuy * pajak + KPEIBuy;
            Integer totalChargeSell = brokFeeSell + brokFeeSell * pajak + idxLevySell + idxLevySell * pajak + KPEISell;
            Integer salesTaxBuy = 0; // 0,1% * grossAmountBuy
            Integer salesTaxSell = 0; // 0,1% * grossAmountSell
            Integer stampDutyBuy = -8256;
            Integer stampDutySell = -10000;

            Integer totalAmountBuy = grossAmountBuy + totalChargeBuy;
            Integer totalAmountSell = grossAmountSell + totalChargeSell;
            Integer paymentDueBuy = totalAmountBuy - totalAmountSell;
            Integer paymentDueSell = totalAmountSell - totalAmountBuy;

            String[] tab3col2 = {
                    "Gross Amount", "Brokerage Fee", "V.A.T Brokerage Fee", "IDX Levy", "VAT IDX Levy", "KPEI",
                    "Total Charges", "Sales Tax", "Stamp Duty",
                    "Total Amount", "Payment due to us (IDR)"};
            Integer[] tab3col3 = {
                    grossAmountBuy, brokFeeBuy, brokFeeBuy * pajak, idxLevyBuy, idxLevyBuy * pajak, KPEIBuy,
                    totalChargeBuy, salesTaxBuy, stampDutyBuy,
                    totalAmountBuy, paymentDueBuy};
            Integer[] tab3col4 = {
                    grossAmountSell, brokFeeSell, brokFeeSell * pajak, idxLevySell, idxLevySell * pajak, KPEISell,
                    totalChargeSell, salesTaxSell, stampDutySell,
                    totalAmountSell, paymentDueSell};

            for (int i = 0; i < tab3col2.length; i++) {
                // column 1 (empty)
                cell = new PdfPCell(new Phrase("", normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                table3.addCell(cell);

                // column 2
                if (tab3col2[i] != null && tab3col2[i] != "") {
                    str = tab3col2[i];
                } else {
                    str = "";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == 5) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                } else if (i == 9) {
                    cell.setPaddingBottom(5);
                } else if (i == 10) {
                    cell.setPaddingTop(10);
                    cell.setPaddingBottom(10);
                    cell.setBorderWidthTop(2f);
                    cell.setBorderWidthBottom(2f);
                }
                table3.addCell(cell);

                // column 3 (buy)
                if (tab3col3[i] != null) {
                    str = adjustNumberFormat(tab3col3[i], 0);
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == 5) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                } else if (i == 9) {
                    cell.setPaddingBottom(5);
                } else if (i == 10) {
                    cell.setPaddingTop(10);
                    cell.setPaddingBottom(10);
                    cell.setBorderWidthTop(2f);
                    cell.setBorderWidthBottom(2f);
                }
                table3.addCell(cell);

                // column 4 (sell)
                if (tab3col4[i] != null) {
                    str = adjustNumberFormat(tab3col4[i], 0);
                } else {
                    str = "0";
                }
                cell = new PdfPCell(new Phrase(str, normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                if (i == 5) {
                    cell.setPaddingBottom(5);
                    cell.setBorderWidthBottom(1f);
                } else if (i == 9) {
                    cell.setPaddingBottom(5);
                } else if (i == 10) {
                    cell.setPaddingTop(10);
                    cell.setPaddingBottom(10);
                    cell.setBorderWidthTop(2f);
                    cell.setBorderWidthBottom(2f);
                }
                table3.addCell(cell);
            }

            doc.add(table3);

            // GENERATE PDF ==================================================================================================================================
            doc.close();

            System.out.println("PDF created: " + fileName); // Log the creation of the PDF

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    public String adjustNumberFormat(Integer number, Integer decimalPlaces) {
        String retval = "";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(','); // Set comma as the thousands separator
        symbols.setDecimalSeparator('.'); // Set dot as the decimal separator

        StringBuilder pattern = new StringBuilder("#,###");
        if (decimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append("0");
            }
        }

        DecimalFormat formatter = new DecimalFormat(pattern.toString(), symbols);
        retval = formatter.format(number);
        return retval;
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