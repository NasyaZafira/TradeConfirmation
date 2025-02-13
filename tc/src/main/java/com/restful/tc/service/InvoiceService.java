package com.restful.tc.service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.restful.tc.model.Executed;
import com.restful.tc.model.Invoice;
import com.restful.tc.model.Subacc;
import com.restful.tc.repository.ExecutedRepository;
import com.restful.tc.repository.InvRepository;
import com.restful.tc.repository.SubaccRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    @Autowired
    private InvRepository invoiceRepository;
    @Autowired
    private ExecutedRepository executedRepository;
    @Autowired
    private SubaccRepository subaccRepository;

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


    String createPdf(Invoice invoice, Subacc subacc, List<Executed> executed, String tempDir) throws IOException {
        // Ensure the variables are initialized
        if (invoice == null || subacc == null || tempDir == null) {
            throw new IllegalArgumentException("Invoice, Subacc, and tempDir must not be null.");
        }
        System.out.println("ceknasya getNoCust: " + invoice.getNoCust());
        String fileName = tempDir + File.separator + "TC_" + invoice.getNoCust() + ".pdf";
        String imgFile = "classpath:templates/logo_bcas.png";

        try {
            int count = 1;
            com.itextpdf.text.Rectangle pageSize = PageSize.A4;
            com.itextpdf.text.Document doc = new Document(pageSize);
            doc.setMargins(36, 36, 80, 70);
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fileName));

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font normalBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font nineFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Attach the header and footer event, passing the required variables
            writer.setPageEvent(new HeaderFooterEvent(invoice, subacc, imgFile));

            doc.open();

            // Reserve space for the header
            com.itextpdf.text.Paragraph spacer = new com.itextpdf.text.Paragraph(" ");
            spacer.setSpacingBefore(100f); // Adjust this value to match the height of your header
            doc.add(spacer);

            // TEXT ==================================================================================================================================
            PdfPTable table = new PdfPTable(1); // Single-column table
            table.setWidthPercentage(100);
            table.setSpacingBefore(150f); // Add spacing before the table

            PdfPCell cell = new PdfPCell(new Phrase("This is to confirm that we have BOUGHT and SOLD for your account :", normalBoldFont));
            cell.setBorder(PdfPCell.NO_BORDER);
//            cell.setPadding(10f);
            table.addCell(cell);

            doc.add(table);

            // TABLE 2 ==================================================================================================================================
            PdfPTable table2 = new PdfPTable(8); // 7 columns
            table2.setWidthPercentage(100); // Full width
            table2.setSpacingBefore(5f);


            float[] columnWidths2 = {1.5f, 1.5f, 6f, 2f, 2f, 3f, 4f, 3f};
            table2.setWidths(columnWidths2);

            // string[] nanti diganti aja pake get data dari DB
            String[] tab2row1 = {"REF#", "board", "Securities", "Lots", "Shares", "Price", "Amount Buy", "Amount Sell"};

            String str = "";
            BigDecimal grossAmountBuy = BigDecimal.ZERO;
            BigDecimal grossAmountSell = BigDecimal.ZERO;
            BigDecimal totalBrokFeeBuy = BigDecimal.ZERO;
            BigDecimal totalBrokFeeSell = BigDecimal.ZERO;

            for (int i = 0; i < tab2row1.length; i++) {
                cell = new PdfPCell(new Phrase(tab2row1[i], normalFont));
                cell.setBorder(PdfPCell.NO_BORDER);
                cell.setPaddingBottom(5);
                cell.setBorderWidthBottom(1f);

                // Atur alignment teks pada setiap kolom
                if (i == 0 || i == 1 || i == 2) {
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT); // Rata kiri untuk "REF# board" dan "Securities"
                } else {
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT); // Rata kanan untuk kolom lainnya
                }

                table2.addCell(cell);
            }

            // Map to store summed data by ref#
            Map<String, Executed> groupedData = new HashMap<>();

// Loop through the list of executed objects to sum data with the same ref#
            for (Executed exec : executed) {
                if (groupedData.containsKey(exec.getPrcDone().toString())) { // Check if the ref# already exists in the map
                    Executed existing = groupedData.get(exec.getPrcDone().toString());

                    // Update the existing object's volDone and prcDone fields
                    existing.setVolDone(existing.getVolDone().add(exec.getVolDone())); // Sum volDone
//                    existing.setPrcDone(existing.getPrcDone().add(exec.getPrcDone())); // Sum prcDone
                } else {
                    // Add a new entry to the map
                    groupedData.put(
                            exec.getPrcDone().toString(),
                            new Executed(
                                    exec.getShare(),
                                    exec.getBors(),
                                    exec.getNoInv(),
                                    exec.getPrcDone(),
                                    exec.getVolDone(),
                                    exec.getExecutionDate(),
                                    exec.getNoCust(),
                                    exec.getBoard(),
                                    exec.getNoDone()
                            )
                    );
                }
            }

// Convert the grouped data to a list or use it directly
            List<Executed> groupedList = new ArrayList<>(groupedData.values());

            for (Executed exec : groupedList) {
                // REF#
                cell = new PdfPCell(new Phrase(exec.getNoInv(), normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT); // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Board
                cell = new PdfPCell(new Phrase(exec.getBoard(), normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT); // Rata kanan untuk kolom lainnya
                // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Securities
                String securities = exec.getShare().getNoShare() + " - " + exec.getShare().getDescr();
                cell = new PdfPCell(new Phrase(securities, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT); // Rata kanan untuk kolom lainnya
                // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Lots
                cell = new PdfPCell(new Phrase(exec.getVolDone().toString(), normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT); // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Shares
                cell = new PdfPCell(new Phrase(exec.getVolDone().toString(), normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT); // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Price
                cell = new PdfPCell(new Phrase(exec.getPrcDone().toString(), normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT); // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Amount Buy
                BigDecimal amountBuy = exec.getBors().equalsIgnoreCase("B") ? exec.getVolDone().multiply(exec.getPrcDone()) : BigDecimal.ZERO;
                grossAmountBuy = grossAmountBuy.add(amountBuy);
                String amountBuyResult = adjustNumberFormat(amountBuy.intValue(), 0);
                cell = new PdfPCell(new Phrase(amountBuyResult, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT); // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);

                // Amount Sell
                BigDecimal amountSell = exec.getBors().equalsIgnoreCase("S") ? exec.getVolDone().multiply(exec.getPrcDone()) : BigDecimal.ZERO;
                grossAmountSell = grossAmountSell.add(amountSell);
                String amountSellResult = adjustNumberFormat(amountSell.intValue(), 0);
                cell = new PdfPCell(new Phrase(amountSellResult, normalFont));
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT); // Rata kanan untuk kolom lainnya
                cell.setBorder(PdfPCell.NO_BORDER);
                table2.addCell(cell);
            }

            PdfPCell emptyCell = new PdfPCell(new Phrase(" ")); // Baris kosong
            emptyCell.setColspan(8); // Sesuaikan dengan jumlah kolom tabel
            emptyCell.setBorder(PdfPCell.NO_BORDER);
            emptyCell.setFixedHeight(5f);
            emptyCell.setBorderWidthBottom(1f); // Tambahkan garis bawah
            table2.addCell(emptyCell);


            doc.add(table2);

            //TABLE 3 ==================================================================================================================================
            PdfPTable table3 = new PdfPTable(4); // 4 columns
            table3.setWidthPercentage(100); // Full width
            table3.setSpacingAfter(10f);

            float[] columnWidths3 = {7f, 7f, 4f, 3f};
            table3.setWidths(columnWidths3);

            List<Invoice> invoices = invoiceRepository.finddistinct10Nocustorderbydtinvdesc();
            for (Invoice invoiceBors : invoices) {
                if ("S".equals(invoiceBors.getBors())) {
                    totalBrokFeeSell = totalBrokFeeSell.add(invoiceBors.getComm());
                    System.out.println("Adding to Sell: " + invoiceBors.getComm());
                } else if ("B".equals(invoiceBors.getBors())) {
                    totalBrokFeeBuy = totalBrokFeeBuy.add(invoiceBors.getComm());
                    System.out.println("Adding to Buy: " + invoiceBors.getComm());
                }
            }

            System.out.println("Total Comm for BORS 'S': " + totalBrokFeeSell);
            System.out.println("Total Comm for BORS 'B': " + totalBrokFeeBuy);

            System.out.println( "BUY KELUAR " +totalBrokFeeBuy);
            System.out.println( "SELL KELUAR " +totalBrokFeeSell);
            System.out.println("BORS VALUE: " + invoice.getBors());
            System.out.println("COMM VALUE: " + invoice.getComm());

            Integer pajak = 11 / 100;
            Integer idxLevyBuy = grossAmountBuy
                    .multiply(new BigDecimal("0.03"))
                    .divide(new BigDecimal("100"), RoundingMode.HALF_UP)
                    .intValue(); //(grossAmountBuy * 0,03) / 100
            Integer idxLevySell = grossAmountSell
                    .multiply(new BigDecimal("0.03"))
                    .divide(new BigDecimal("100"), RoundingMode.HALF_UP)
                    .negate()
                    .intValue(); //(grossAmountSell * 0,03) / 100
            Integer KPEIBuy = grossAmountBuy
                    .multiply(new BigDecimal("0.01"))
                    .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                    .intValue();//grossAmountBuy * 0,01 / 100
            Integer KPEISell = grossAmountSell
                    .multiply(new BigDecimal("0.01"))
                    .divide(new BigDecimal("100"), RoundingMode.HALF_UP)
                    .negate()
                    .intValue(); //grossAmountSell * 0,01 / 100
            BigDecimal salesTaxBuyInt = new BigDecimal(grossAmountBuy.intValue())
                    .multiply(new BigDecimal("0.1"))
                    .divide(new BigDecimal("100"), RoundingMode.HALF_UP);
            BigDecimal salesTaxSellInt = new BigDecimal(grossAmountSell.intValue())
                    .multiply(new BigDecimal("0.1"))
                    .divide(new BigDecimal("100"), RoundingMode.HALF_UP);

            //BUY
            BigDecimal vatlevy = BigDecimal.valueOf(idxLevyBuy).multiply(BigDecimal.valueOf(pajak));
            BigDecimal vatbfee = totalBrokFeeBuy.multiply(BigDecimal.valueOf(pajak));

            //SELL
            BigDecimal vatsfee = totalBrokFeeSell.multiply(BigDecimal.valueOf(pajak));
            BigDecimal vatselllevy = BigDecimal.valueOf(idxLevySell).multiply(BigDecimal.valueOf(pajak));

            BigDecimal idxLevyBuyBD = new BigDecimal(idxLevyBuy);
            BigDecimal KPEIBuyBD = new BigDecimal(KPEIBuy);

            BigDecimal idxLevySellD = new BigDecimal(idxLevySell);
            BigDecimal KPEISellD = new BigDecimal(KPEISell);

            // Penjumlahan menggunakan add()
            BigDecimal totalChargeBuy = totalBrokFeeBuy
                    .add(vatbfee)
                    .add(vatlevy)
                    .add(idxLevyBuyBD)
                    .add(KPEIBuyBD);

            BigDecimal totalChargeSell = totalBrokFeeSell
                    .add(vatsfee)
                    .add(vatselllevy)
                    .add(idxLevySellD)
                    .add(KPEISellD);
            Integer salesTaxBuy = salesTaxBuyInt.intValue();// 0,1% * grossAmountBuy //kata ini bermasalah (coba cek lagi kenapa pakai Integer) rossAmountBuy is a BigDecimal, and calling intValue() will convert it to an int. However, this conversion truncates any fractional part of the BigDecimal, which may lead to a loss of precision.
            Integer salesTaxSell = salesTaxSellInt.intValue(); // 0,1% * grossAmountSell
            Integer stampDutyBuy = -10000;
            Integer stampDutySell = -10000;

            BigDecimal totalAmountBuy = grossAmountBuy.add(BigDecimal.valueOf(totalChargeBuy.intValue()));
            BigDecimal totalAmountSell = grossAmountSell.add(BigDecimal.valueOf(totalChargeSell.intValue()));
            BigDecimal paymentDueBuy = totalAmountBuy.subtract(totalAmountSell);
            BigDecimal paymentDueSell = totalAmountSell.subtract(totalAmountBuy);

            Integer paymentDue1 = 0;
            Integer paymentDue2 = 0;

            if (totalAmountBuy.compareTo(totalAmountSell) > 0) {
                paymentDue1 = paymentDueBuy.subtract(paymentDueSell).intValue();
            } else {
                paymentDue2 = paymentDueSell.subtract(paymentDueBuy).intValue();
            }

            String[] tab3col2 = {
                    "Gross Amount", "Brokerage Fee", "V.A.T Brokerage Fee", "IDX Levy", "VAT IDX Levy", "KPEI",
                    "Total Charges", "Sales Tax", "Stamp Duty",
                    "Total Amount", "Payment due to us (IDR)"};
            Integer[] tab3col3 = {
                    grossAmountBuy.intValue(), //gross amount
                    totalBrokFeeBuy.intValue(), //brokerage fee
                    vatbfee.intValue(), //v.a.t brokerage fee
                    idxLevyBuy, //idx levy
                    vatlevy.intValue(),//vat idx levy
                    KPEIBuy, //kpei
                    totalChargeBuy.intValue(), //total changes
                    salesTaxBuy, //sales tax
                    stampDutyBuy, //stamp duty
                    totalAmountBuy.intValue(), // total amount
                    paymentDue1 // payment final due
            };
            Integer[] tab3col4 = {
                    grossAmountSell.intValue(),
                    totalBrokFeeSell.intValue(),
                    vatsfee.intValue(),
                    idxLevySell,
                    vatselllevy.intValue(),
                    KPEISell,
                    totalChargeSell.intValue(),
                    salesTaxSell,
                    stampDutySell,
                    totalAmountSell.intValue(),
                    paymentDue2
            };

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
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
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
                cell.setVerticalAlignment(Element.ALIGN_RIGHT);
                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
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

            //TABLE 4 ==============================================================================================================================
            PdfPTable tremit = new PdfPTable(1);
            tremit.setWidthPercentage(100); // Full width
            float[] columninWidth = {10f};
            tremit.setWidths(columninWidth);

            Paragraph remitParagraph = new Paragraph();
            remitParagraph.setFont(nineFont);
            remitParagraph.add("Please remit the due amount to effectively on (in good fund) from \n"); //tc individu
            remitParagraph.add("23 January 2025\n"); //hari transaksi + 1

            PdfPCell remit = new PdfPCell(remitParagraph);
            remit.setBorder(PdfPCell.NO_BORDER);
            tremit.addCell(remit);

            doc.add(tremit);

            //TABLE 5 ====================================================================================================================================
            PdfPTable bankAccount = new PdfPTable(2);
            bankAccount.setWidthPercentage(100); // Full width
            float[] columnbankWidth = {4f, 18f};
            bankAccount.setWidths(columnbankWidth);

            //column1
            Paragraph bankParagraph = new Paragraph();
            bankParagraph.setFont(nineFont);
            bankParagraph.add("Bank\n");
            bankParagraph.add("Account Number\n");
            bankParagraph.add("Name\n");

            PdfPCell bankAcc = new PdfPCell(bankParagraph);
            bankAcc.setBorder(PdfPCell.NO_BORDER);
            bankAcc.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            bankAccount.addCell(bankAcc);

            //column2
            Paragraph custParagraph = new Paragraph();
            custParagraph.setFont(nineFont);
            custParagraph.setSpacingBefore(5f);
            custParagraph.add(subacc.getBank() + "\n");
            custParagraph.add(subacc.getAccount() + "\n");
            custParagraph.add(subacc.getNameBank() + "\n");

            PdfPCell custData = new PdfPCell(custParagraph);
            custData.setBorder(PdfPCell.NO_BORDER);
            custData.setPaddingBottom(5f);
            custData.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            bankAccount.addCell(custData);

            doc.add(bankAccount);

            //TABLE 6 =============================================================================================
            PdfPTable announce = new PdfPTable(1);
            announce.setWidthPercentage(100); // Full width
            float[] columnanWidth = {10f};
            announce.setWidths(columnanWidth);

            Paragraph announParagraph = new Paragraph();
            announParagraph.setFont(nineFont);
            announParagraph.add("This transaction is taken as confirmed, if no discrepancies are reported within 24 hours\n");
            announParagraph.add("Should there be any discrepancies, kindly contact our customer sevices or your sales officer prior\n");
            announParagraph.add("to market opens on the next business day\n");

            PdfPCell announCell = new PdfPCell(announParagraph);
            announCell.setBorder(PdfPCell.NO_BORDER);
            announCell.setPaddingBottom(10f);
            announCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            announce.addCell(announCell);

            doc.add(announce);

            //TABLE 7 ==================================================================================================
            PdfPTable pengTable1 = new PdfPTable(1);
            pengTable1.setWidthPercentage(100);// Full width
            float[] columnanWidth1 = {10f};
            pengTable1.setWidths(columnanWidth1);

            Paragraph pengParagraph1 = new Paragraph();
            pengParagraph1.setFont(nineFont);
            pengParagraph1.setLeading(20f);
            pengParagraph1.add("Trade Confirmation ini berlaku sebagai Dokumen tertentu yang kedudukannya dipersamakan dengan Faktur Pajak sesuai dengan Peraturan Direktur Jendral Pajak Nomor PER-27/PJ/2011 jo, PER-67/PJ/2010 jo, PER-10/PJ/2010\n");
//            pengParagraph1.add("Peraturan Direktur Jendral Pajak Nomor PER-27/PJ/2011 jo, PER-67/PJ/2010 jo, PER-10/PJ/2010\n");

            PdfPCell pengCell1 = new PdfPCell(pengParagraph1);
            pengCell1.setPaddingBottom(10f);
            pengCell1.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            pengCell1.setPadding(3f);
            pengTable1.addCell(pengCell1);

            doc.add(pengTable1);

            //TABLE 8 ===========================================================================================================================================
            PdfPTable pengTable2 = new PdfPTable(1);
            pengTable2.setWidthPercentage(100);// Full width
            float[] columnanWidth2 = {10f};
            pengTable2.setWidths(columnanWidth2);

            Paragraph pengParagraph2 = new Paragraph();
            pengParagraph2.setFont(nineFont);
            pengParagraph2.setLeading(20f);
            pengParagraph2.add("Nasabah dihimbau untuk memeriksa catatan saldo dan mutasi Efek dan/atau dana pada Sub Rekening Efek dan dana pada RDN melalui Akses KSEI (https://akses.ksei.co.id)\n");
//
            PdfPCell pengCell2 = new PdfPCell(pengParagraph2);
            pengCell2.setPaddingBottom(10f);
            pengCell2.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            pengCell2.setPadding(3f);
            pengTable2.addCell(pengCell2);

            doc.add(pengTable2);

            //TABLE 9 ======================================================================================================================================================
            PdfPTable noteTable = new PdfPTable(1);
            noteTable.setWidthPercentage(100);// Full width
            float[] columnoteWidth = {10f};
            noteTable.setWidths(columnoteWidth);

            Paragraph noteParagraph = new Paragraph();
            noteParagraph.setFont(nineFont);
            noteParagraph.add("NOTE : Board NG, RG, TN (Bursa Efek); Board UH (Di luar Bursa Efek); V= Vs Payment; F = Free of Payment\n");
//
            PdfPCell noteCell = new PdfPCell(noteParagraph);
            noteCell.setPaddingBottom(1f);
            noteCell.setBorder(PdfPCell.NO_BORDER);
            noteCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            noteCell.setPadding(3f);
            noteTable.addCell(noteCell);

            doc.add(noteTable);
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

    // Custom HeaderFooterEvent class
    static class HeaderFooterEvent extends PdfPageEventHelper {
        private final Invoice invoice;
        private final Subacc subacc;
        private final String imgFile;
        private final Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        private final Font normalBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        private final Font nineFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        private final Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 15);

        public HeaderFooterEvent(Invoice invoice, Subacc subacc, String imgFile) {
            this.invoice = invoice;
            this.subacc = subacc;
            this.imgFile = imgFile;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Add header
            try {
                PdfPTable headerTable = createHeaderTable();
                headerTable.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
                headerTable.writeSelectedRows(0, -1, document.leftMargin(), document.top() + 50, cb);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Add footer
            cb.setLineWidth(1f); // Set line thickness
            cb.moveTo(document.leftMargin(), document.bottom() - 10); // Start point of the line
            cb.lineTo(document.getPageSize().getWidth() - document.rightMargin(), document.bottom() - 10); // End point of the line
            cb.stroke(); // Draw the line
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), new Font(Font.FontFamily.HELVETICA, 6)),
                    (document.getPageSize().getWidth()) / 2, document.bottomMargin() - 20, 0);
        }

        private PdfPTable createFooterTable() throws DocumentException {
            PdfPTable footerTable = new PdfPTable(1);
            PdfPCell disclaimerCell = new PdfPCell(new Phrase("This document is confidential and intended for the recipient only.", normalFont));
            disclaimerCell.setBorder(PdfPCell.NO_BORDER);
            disclaimerCell.setColspan(3); // Span across all 3 columns
            disclaimerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(disclaimerCell);
            return footerTable;
        }

        private PdfPTable createHeaderTable() throws DocumentException, IOException {
            //TABLE CORPORATE
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100); // Full width
            table.setSpacingBefore(5f);
            table.setSpacingAfter(5f);
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
            table.addCell(cell2);

            // header ===============================================================================


            PdfPCell headerCell = new PdfPCell(new Phrase("TRADE CONFIRMATION", headerFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setColspan(2); // Span across both columns
            headerCell.setBorder(PdfPCell.NO_BORDER);
            headerCell.setPaddingBottom(10f);
            table.addCell(headerCell);

            // TABLE 1 ===============================================================================
            PdfPTable table1 = new PdfPTable(4); // 5 columns
            table1.setWidthPercentage(100); // Full width
            table1.setSpacingBefore(5f);
            table1.setSpacingAfter(5f);
            float[] columnWidths = {2f, 8f, 4f, 6f};
            table1.setWidths(columnWidths);

            //PARSING DATA
            String docNo = invoice.getDate() + invoice.getNoCust();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yyyy");
            String data = "(" + invoice.getNoCust() + ") " + subacc.getName();

            // String[] nnti bisa diganti pakai hasil get dari databasenya aja
            String[] tab1col1 = {"To", "Address", "", "", "", "Phone/Fax", "Email", "Bank"};
            String[] tab1col2 = {
                    data.replaceAll("\\(\\s*(.*?)\\s*\\)", "($1)"),
                    subacc.getAdd1(),
                    subacc.getAdd2(),
                    subacc.getCityJoin().getDesc(),
                    subacc.getZip() + " " + subacc.getCountryJoin().getDesc(),
                    subacc.getPhone1() + " / " + subacc.getFax(),
                    subacc.getEmail(),
                    subacc.getBank() + " " + subacc.getAccount()};
            String[] tab1col3 = {"Document No", "Transaction Date", "Settlement Date", "Currency", "Office", "Sales Person", "SID", "CBest Account", "Commission"};
            String[] tab1col4 = {
                    docNo.replace("-", ""),
                    invoice.getDate().format(formatter),
                    invoice.getDt_due().format(formatter),
                    "IDR Minimum Fee : 0",
                    subacc.getNo_sub(),
                    subacc.getStaff().getNameStaff(),
                    subacc.getInvestor_no(),
                    "SQ001" + subacc.getNo_ksei().substring(0, 4) + "001" + subacc.getNo_ksei().substring(4),
                    subacc.getCommission() + "%  (Excluding Sales Tax)"};

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
                    if (i == 0) {
                        cell = new PdfPCell(new Phrase(": " + tab1col2[i], normalBoldFont));
                    } else {
                        cell = new PdfPCell(new Phrase(": " + tab1col2[i], normalFont));
                    }
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

            PdfPCell table1Cell = new PdfPCell(table1);
            table1Cell.setColspan(2);
            table1Cell.setBorder(PdfPCell.NO_BORDER);
            table.addCell(table1Cell);

            return table;
        }
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

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateNow = currentDate.format(formatter);

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
            //get invoice detail
            String custNo = invoice.getNoCust();
            // List<Invoice> detailInvoices = invoiceRepository.findInvoiceDetail(custNo, dateNow); // prod pakai ini
            List<Invoice> detailInvoices = invoiceRepository.findInvoiceDetail(custNo, "2025-01-31"); // testing

            log.info("=================");
            log.info("Customer: {}", custNo);
            log.info("==================");

            for (Invoice detail : detailInvoices) {
                log.info("Invoice No: {}", detail.getNoInvoice());
            }

            executorService.submit(() -> {
                System.out.println("ON WORKING Thread Name: " + Thread.currentThread().getName());
                try {
                    //ambil data dari subacc
                    Subacc subacc = subaccRepository.findByNoCust(invoice.getNoCust());
                    if (subacc == null) {
                        throw new RuntimeException("Subacc not found for noCust: " + invoice.getNoCust());
                    }
                    // Ambil data Executed berdasarkan noCust (atau logika lain yang sesuai)
//                    List<Executed> executed = executedRepository.findByNoCust(invoice.getNoCust());
                    LocalDate date = LocalDate.of(2025, 1, 22);
                    List<Executed> executed = executedRepository.findByNoCustAndExecutionDate(invoice.getNoCust(), date);
                    ;
                    if (executed == null) {
                        throw new RuntimeException("Executed not found for noCust: " + invoice.getNoCust());
                    }

                    String fileName = createPdf(invoice, subacc, executed, tempDir);
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