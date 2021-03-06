package org.kuali.ole.deliver.util.printSlip;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import org.apache.log4j.Logger;
import org.kuali.ole.deliver.bo.OleCirculationDesk;
import org.kuali.ole.deliver.bo.OleDeliverRequestBo;
import org.kuali.ole.deliver.util.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sheiksalahudeenm on 7/30/15.
 */
public class OnHoldRecieptPrintSlipUtil extends OleRecieptPrintSlipUtil {

    private static final Logger LOG = Logger.getLogger(OnHoldRecieptPrintSlipUtil.class);

    @Override
    public PdfFormatUtil getPdfFormatUtil() {
        return new ReceiptPdfFormatUtil();
    }

    @Override
    protected void populateHeader(Paragraph paraGraph) {
        paraGraph.add(new Chunk("Hold Slip", getPdfFormatUtil().getBoldFont()));
    }

    @Override
    protected void populateBody(PdfPTable pdfTable) {
        boolean missingPieceCheck = getOleItemRecordForCirc().getItemRecord().isMissingPieceFlag();
        OleCirculationDesk oleCirculationDesk = null != getOleItemRecordForCirc() ? getOleItemRecordForCirc().getCheckinLocation() : null;
        OleDeliverRequestBo oleDeliverRequestBo = getOleItemRecordForCirc().getOleDeliverRequestBo();
        String patronName = oleDeliverRequestBo != null ? oleDeliverRequestBo.getOlePatron().getPatronName() : null;
        try {
            pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(patronName));
            if (oleCirculationDesk != null) {
                int noDays = Integer.parseInt(oleCirculationDesk.getOnHoldDays());
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, noDays);
                Date date = calendar.getTime();
                if (date != null) {
                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                    String dateString = date == null ? "" : df.format(date);
                    pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(dateString));
                }
            }
            pdfTable.addCell(getPdfFormatUtil().getEmptyCell());
            if (!missingPieceCheck) {
                pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(oleCirculationDesk != null ? oleCirculationDesk.getCirculationDeskPublicName() : ""));
                pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(getOleItemRecordForCirc().getItemRecord().getBarCode()));
                pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(getOleItemSearch().getTitle()));
                pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(getOleItemSearch().getCallNumber()));
                pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(getOleItemSearch().getCopyNumber()));
                pdfTable.addCell(getPdfFormatUtil().getPdfPCellInJustified(getOleItemSearch().getVolumeNumber()));
            }
        } catch (Exception e) {
            LOG.error("Exception while creating pdf for printing slip", e);
        }
    }
}