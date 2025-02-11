package com.restful.tc.service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
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


    String createPdf(Invoice invoice, Subacc subacc, Executed executed, String tempDir) throws IOException {
        System.out.println("ceknasya getNoCust: " + invoice.getNoCust());
        String fileName = tempDir + File.separator + "TC_" + invoice.getNoCust() + ".pdf";
        String imgFile = "classpath:templates/logo_bcas.png";


        try {
            int count = 1;
            com.itextpdf.text.Rectangle pageSize = PageSize.A4;
            com.itextpdf.text.Document doc = new Document(pageSize);
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 15);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font normalBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font nineFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            doc.open();

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

            doc.add(table);

            // header ===============================================================================
            com.itextpdf.text.Paragraph header = new com.itextpdf.text.Paragraph("TRADE CONFIRMATION", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            doc.add(header);

            // TABLE 1 ===============================================================================
            PdfPTable table1 = new PdfPTable(4); // 5 columns
            table1.setWidthPercentage(100); // Full width
            table1.setSpacingBefore(5f);
            table1.setSpacingAfter(5f);
            float[] columnWidths = {3f, 7f, 4f, 6f};
            table1.setWidths(columnWidths);

            // String[] nnti bisa diganti pakai hasil get dari databasenya aja
            String[] tab1col1 = {"To", "Address", "", "", "Phone/Fax", "Email", "Bank"};
            String[] tab1col2 = {invoice.getNoCust(), "jl siantan", "rt rw", "jakbar", "0/", "jeff@gmail", "BCA 12345"};
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

            String[] refBoard = {executed.getNoInv() + "" + executed.getBoard(),  ""};
            String[] securities = {executed.getNoShare(), ""};
            BigDecimal[] lots = {executed.getVolDone().divide(BigDecimal.valueOf(100))};
            BigDecimal[] shares = {executed.getVolDone()};
            BigDecimal[] price = {executed.getPrcDone()};
            String[] isBuy = {executed.getBors()};
            BigDecimal[] amountBuy = {BigDecimal.ZERO};
            BigDecimal[] amountSell = {BigDecimal.ZERO};

            BigDecimal grossAmountBuy = BigDecimal.ZERO;;
            BigDecimal grossAmountSell = BigDecimal.ZERO;;

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
                    str = shares[i].toString();
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
                    str = price[i].toString();
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
                if (shares[i] != null && price[i] != null && isBuy[i].equals("B")) {
                    BigDecimal amount = shares[i].multiply(price[i]);
                    str = amount.toString();
                    amountBuy[i] = amount;
                    grossAmountBuy = grossAmountBuy.add(amount);
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
                if (shares[i] != null && price[i] != null && isBuy[i].equals("S")) {
                    BigDecimal amount = shares[i].multiply(price[i]);
                    str = amount.toString();
                    amountSell[i] = amount;
                    grossAmountSell = grossAmountSell.add(amount);
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

            BigDecimal totalAmountBuy = grossAmountBuy.add(BigDecimal.valueOf(totalChargeBuy));
            BigDecimal totalAmountSell = grossAmountSell.add(BigDecimal.valueOf(totalChargeSell));
            BigDecimal paymentDueBuy = totalAmountBuy.subtract(totalAmountSell);
            BigDecimal paymentDueSell = totalAmountSell.subtract(totalAmountBuy);

            String[] tab3col2 = {
                    "Gross Amount", "Brokerage Fee", "V.A.T Brokerage Fee", "IDX Levy", "VAT IDX Levy", "KPEI",
                    "Total Charges", "Sales Tax", "Stamp Duty",
                    "Total Amount", "Payment due to us (IDR)"};
            Integer[] tab3col3 = {
                    grossAmountBuy.intValue(),
                    brokFeeBuy,
                    BigDecimal.valueOf(brokFeeBuy).multiply(BigDecimal.valueOf(pajak)).intValue(),
                    idxLevyBuy,
                    BigDecimal.valueOf(idxLevyBuy).multiply(BigDecimal.valueOf(pajak)).intValue(),
                    KPEIBuy,
                    totalChargeBuy,
                    salesTaxBuy,
                    stampDutyBuy,
                    totalAmountBuy.intValue(),
                    paymentDueBuy.intValue()
            };
            Integer[] tab3col4 = {
                    grossAmountSell.intValue(),
                    brokFeeSell,
                    BigDecimal.valueOf(brokFeeSell).multiply(BigDecimal.valueOf(pajak)).intValue(),
                    idxLevySell,
                    BigDecimal.valueOf(idxLevySell).multiply(BigDecimal.valueOf(pajak)).intValue(),
                    KPEISell,
                    totalChargeSell,
                    salesTaxSell,
                    stampDutySell,
                    totalAmountSell.intValue(),
                    paymentDueSell.intValue()};

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
            float[] columnbankWidth = {4f,18f};
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
            custParagraph.add("BCA\n");
            custParagraph.add("4991905108\n");
            custParagraph.add("JEFRI YUNUS\n");

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
                    Executed executed = executedRepository.findByNoCust(invoice.getNoCust());
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