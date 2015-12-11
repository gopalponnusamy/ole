package org.kuali.ole.dsng.indexer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.kuali.ole.DocumentUniqueIDPrefix;
import org.kuali.ole.docstore.common.exception.DocstoreIndexException;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.*;
import org.kuali.ole.docstore.model.enums.DocCategory;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by SheikS on 11/30/2015.
 */
public class ItemIndexer extends OleDsNgIndexer  {
    
    private static final Logger LOG = LoggerFactory.getLogger(ItemIndexer.class);
    
    @Override
    public void indexDocument(Object object) {
        ItemRecord itemRecord = (ItemRecord) object;
        Map<String, SolrInputDocument> inputDocumentForItem = getInputDocumentForItem(itemRecord, null);

        List<SolrInputDocument> solrInputDocuments = (List<SolrInputDocument>) inputDocumentForItem;
        commitDocumentToSolr(solrInputDocuments);
    }

    @Override
    public void updateDocument(Object object) {

        ItemRecord itemRecord = (ItemRecord) object;
        Map<String,SolrInputDocument> parameterMap = new HashedMap();

        Map<String, SolrInputDocument> inputDocumentForItem = getInputDocumentForItem(itemRecord, parameterMap);

        List<SolrInputDocument> solrInputDocuments = getSolrInputDocumentListFromMap(inputDocumentForItem);
        commitDocumentToSolr(solrInputDocuments);

    }

    public Map<String,SolrInputDocument> getInputDocumentForItem(ItemRecord itemRecord,Map parameterMap) {
        SolrInputDocument itemSolrInputDocument = buildSolrInputDocument(itemRecord, parameterMap);

        //***********************
        String holdingsId = itemRecord.getHoldingsId();
        String holdingsIdentifierWithPrefix = DocumentUniqueIDPrefix.getPrefixedId(
                DocumentUniqueIDPrefix.PREFIX_WORK_HOLDINGS_OLEML, String.valueOf(holdingsId));
        if (holdingsId != null) {
            itemSolrInputDocument.addField(HOLDINGS_IDENTIFIER, holdingsIdentifierWithPrefix);
        }

        SolrInputDocument holdingSolrInputDocuemnt = getSolrInputDocumentFromMap(parameterMap, holdingsIdentifierWithPrefix);
        if(null == holdingSolrInputDocuemnt) {
            // Todo : Need to Build for Holdings
        }

        if(null != holdingSolrInputDocuemnt) {
            Object bibs = holdingSolrInputDocuemnt.get(BIB_IDENTIFIER);
            addItemDetailsToHoldings(itemSolrInputDocument, holdingSolrInputDocuemnt);


            addBibInfoForHoldingsOrItems(itemSolrInputDocument, holdingSolrInputDocuemnt);

            itemSolrInputDocument.addField("bibIdentifier", bibs);  // Todo Need to verify


            // Todo : Need to populate the holdings record
            // Todo : Need to do auto fetch (Lazy Loading)
            Map<String, String> map = new HashMap<String,String>();
            map.put("holdingsId", holdingsId);
            HoldingsRecord holdingsRecord = KRADServiceLocator.getBusinessObjectService().findByPrimaryKey(HoldingsRecord.class, map);
            String bibId = "";
            if(null != holdingsRecord) {
                bibId = holdingsRecord.getBibId();
            }
            String bibIdentifierWithPrefix = DocumentUniqueIDPrefix.getPrefixedId(
                    DocumentUniqueIDPrefix.PREFIX_WORK_BIB_MARC, String.valueOf(bibId));
            SolrInputDocument bibSolrInputDocuemnt = getSolrInputDocumentFromMap(parameterMap, bibIdentifierWithPrefix);
            if(null == bibSolrInputDocuemnt) {
                // Todo : Need to Build for bib
            }
            addSolrInputDocumentToMap(parameterMap,itemSolrInputDocument);
            addSolrInputDocumentToMap(parameterMap,holdingSolrInputDocuemnt);
            bibSolrInputDocuemnt = addItemDetailsToBib(itemSolrInputDocument, bibSolrInputDocuemnt);
            if(null != bibSolrInputDocuemnt) {
                addSolrInputDocumentToMap(parameterMap, bibSolrInputDocuemnt);
            }
        }

        //***********************

        return parameterMap;
    }

    @Override
    public SolrInputDocument buildSolrInputDocument(Object object,Map<String, SolrInputDocument> parameterMap) {
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        try {
            ItemRecord itemRecord = (ItemRecord) object;


            String itemIdentifierWithPrefix = DocumentUniqueIDPrefix.getPrefixedId(DocumentUniqueIDPrefix.PREFIX_WORK_ITEM_OLEML, String.valueOf(itemRecord.getItemId()));

            solrInputDocument.addField(DOC_CATEGORY, DocCategory.WORK.getCode());
            solrInputDocument.addField(DOC_TYPE, DOC_TYPE_ITEM_VALUE);
            solrInputDocument.addField(DOC_FORMAT, DOC_FORMAT_INSTANCE_VALUE);
            solrInputDocument.addField(ID, itemIdentifierWithPrefix);
            solrInputDocument.addField(ITEM_IDENTIFIER, itemIdentifierWithPrefix);
            solrInputDocument.addField(LOCALID_DISPLAY, itemRecord.getItemId());
            solrInputDocument.addField(LOCALID_SEARCH, itemRecord.getItemId());


            solrInputDocument.addField(CLMS_RET_FLAG, itemRecord.getClaimsReturnedFlag());
            Date claimsReturnedDateCreated = itemRecord.getClaimsReturnedFlagCreateDate();
            solrInputDocument.addField(CLMS_RET_FLAG_CRE_DATE, (claimsReturnedDateCreated != null ? DOCSTORE_DATE_FORMAT.format(claimsReturnedDateCreated) : null));
            solrInputDocument.addField(CLMS_RET_NOTE, itemRecord.getClaimsReturnedNote());
            solrInputDocument.addField(CURRENT_BORROWER, itemRecord.getCurrentBorrower());
            solrInputDocument.addField(PROXY_BORROWER, itemRecord.getProxyBorrower());
            solrInputDocument.addField(DUE_DATE_TIME, itemRecord.getDueDateTime());
            solrInputDocument.addField(ORG_DUE_DATE_TIME, itemRecord.getOriginalDueDate());
            solrInputDocument.addField(ITEM_STATUS_EFFECTIVE_DATE, itemRecord.getEffectiveDate());
            solrInputDocument.addField(CHECK_OUT_DUE_DATE_TIME, itemRecord.getCheckOutDateTime());
            solrInputDocument.addField(STAFF_ONLY_FLAG, itemRecord.getStaffOnlyFlag());
//        solrInputDocument.addField(IS_ANALYTIC, itemRecord.isAnalytic()); // Todo : Need to verify (Ans : Need to verify with bib status) (HoldingsItemRecord - holdingsId and ItemId)
            solrInputDocument.addField(ITEM_IDENTIFIER_SEARCH, itemIdentifierWithPrefix);
            solrInputDocument.addField(BARCODE_ARSL_SEARCH, itemRecord.getBarCodeArsl());
            solrInputDocument.addField(COPY_NUMBER_SEARCH, itemRecord.getCopyNumber());
//        solrInputDocument.addField(COPY_NUMBER_LABEL_SEARCH, itemRecord.getCopyNumberLabel()); // Todo : Need to verify
            solrInputDocument.addField(PURCHASE_ORDER_LINE_ITEM_IDENTIFIER_SEARCH, itemRecord.getPurchaseOrderItemLineId());
            solrInputDocument.addField(VENDOR_LINE_ITEM_IDENTIFIER_SEARCH, itemRecord.getVendorLineItemId());
//        solrInputDocument.addField(VOLUME_NUMBER_LABEL_SEARCH, itemRecord.getVolumeNumberLabel()); // Todo :Need to verify
//        solrInputDocument.addField(VOLUME_NUMBER_SEARCH, itemRecord.getVolumeNumberLabel());// Todo :Need to verify
            solrInputDocument.addField(ENUMERATION_SEARCH, itemRecord.getEnumeration());
            solrInputDocument.addField(CHRONOLOGY_SEARCH, itemRecord.getChronology());
            solrInputDocument.addField(MISSING_PIECE_FLAG_NOTE_SEARCH, itemRecord.getMissingPieceFlagNote());
            solrInputDocument.addField(CLAIMS_RETURNED_NOTE_SEARCH, itemRecord.getClaimsReturnedNote());
            solrInputDocument.addField(DAMAGED_ITEM_NOTE_SEARCH, itemRecord.getDamagedItemNote());
            solrInputDocument.addField(MISSING_PIECE_FLAG_SEARCH, itemRecord.isMissingPieceFlag());
            solrInputDocument.addField(CLAIMS_RETURNED_FLAG_SEARCH, itemRecord.getClaimsReturnedFlag());
            solrInputDocument.addField(ITEM_DAMAGED_FLAG_SEARCH, itemRecord.isItemDamagedStatus());
            solrInputDocument.addField(MISSING_PIECE_COUNT_SEARCH,itemRecord.getMissingPiecesCount());
            solrInputDocument.addField(NUMBER_OF_PIECES_SEARCH,itemRecord.getNumberOfPieces());

            Date date = new Date();
            // Item call number should be indexed if it is available at item level or holdings level.
            String itemCallNumber = null;
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(itemRecord.getCallNumber())) {
                solrInputDocument.addField(CALL_NUMBER_SEARCH, itemRecord.getCallNumber());
                solrInputDocument.addField(CALL_NUMBER_DISPLAY, itemRecord.getCallNumber());
                solrInputDocument.setField(CALL_NUMBER_SORT, itemRecord.getCallNumber());
                solrInputDocument.addField(CALL_NUMBER_PREFIX_SEARCH, itemRecord.getCallNumberPrefix());
                solrInputDocument.addField(CALL_NUMBER_PREFIX_DISPLAY, itemRecord.getCallNumberPrefix());

                //Shelving scheme code should be indexed if it is available at holdings level
                String shelvingSchemeCode = "";
                String shelvingSchemeValue = "";

                if (null != itemRecord.getCallNumberTypeRecord()) {
                    shelvingSchemeCode = itemRecord.getCallNumberTypeRecord().getCode();
                    shelvingSchemeValue = itemRecord.getCallNumberTypeRecord().getName();
                    if (org.apache.commons.lang3.StringUtils.isNotEmpty(shelvingSchemeCode)) {
                        solrInputDocument.addField(SHELVING_SCHEME_CODE_SEARCH, shelvingSchemeCode);
                        solrInputDocument.addField(SHELVING_SCHEME_CODE_DISPLAY, shelvingSchemeCode);
                    }
                    if (org.apache.commons.lang3.StringUtils.isNotEmpty(shelvingSchemeValue)) {
                        solrInputDocument.addField(SHELVING_SCHEME_VALUE_SEARCH, shelvingSchemeValue);
                        solrInputDocument.addField(SHELVING_SCHEME_VALUE_DISPLAY, shelvingSchemeValue);
                    }
                }

                String shelvingOrder = null;
                //TODO:Shelving order not present at item level
                if (org.apache.commons.lang3.StringUtils.isNotBlank(itemRecord.getShelvingOrder())) {
                    shelvingOrder = itemRecord.getShelvingOrder();
                }else if (org.apache.commons.lang3.StringUtils.isEmpty(shelvingOrder) && itemRecord.getCallNumber() != null) {
                    try {
                        //Build sortable key for a valid call number
                        if (itemRecord.getCallNumberTypeRecord() != null) {
                            if(org.apache.commons.lang3.StringUtils.isNotEmpty(itemCallNumber) && itemCallNumber.trim().length() > 0) {
                                shelvingOrder = getOleDsHelperUtil().buildSortableCallNumber(itemCallNumber, itemRecord.getShelvingOrder());
                            }
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(shelvingOrder)) {
                    shelvingOrder = shelvingOrder.replaceAll(" ", "-");
                    solrInputDocument.addField(SHELVING_ORDER_SORT, shelvingOrder + itemIdentifierWithPrefix);
                    solrInputDocument.addField(SHELVING_ORDER_SEARCH, shelvingOrder);
                    solrInputDocument.addField(SHELVING_ORDER_DISPLAY, shelvingOrder);
                }
                if (itemRecord.getCallNumberPrefix() != null) {
                    solrInputDocument.addField(CALLNUMBER_PREFIX_SORT, itemRecord.getCallNumberPrefix());
                }
                if (itemRecord.getCallNumber() != null) {
                    solrInputDocument.setField(CALLNUMBER_SORT, itemRecord.getCallNumber());
                }
                if (itemRecord.getEnumeration() != null) {
                    String enumerationSort = getOleDsHelperUtil().getNormalizedEnumeration(itemRecord.getEnumeration());
                    solrInputDocument.addField(ENUMERATION_SORT, enumerationSort);
                }
                if (itemRecord.getChronology() != null) {
                    solrInputDocument.addField(CHRONOLOGY_SORT, itemRecord.getChronology());
                }
                if (itemRecord.getCopyNumber() != null) {
                    String copyNumberSort = getOleDsHelperUtil().getNormalizedEnumeration(itemRecord.getCopyNumber());
                    solrInputDocument.addField(COPYNUMBER_SORT, copyNumberSort);
                }
                if (null != itemRecord.getBarCode()) {
                    solrInputDocument.addField(ITEM_BARCODE_SORT, itemRecord.getBarCode());
                }
            }

            if (itemRecord.getItemStatusRecord() != null) {
                solrInputDocument.addField(ITEM_STATUS_DISPLAY, itemRecord.getItemStatusRecord().getCode());
                solrInputDocument.addField(ITEM_STATUS_SEARCH, itemRecord.getItemStatusRecord().getName());
                solrInputDocument.addField(ITEM_STATUS_SORT, itemRecord.getItemStatusRecord().getName());
            }

            StringBuffer loactionLevelStr = new StringBuffer(" ");
            if (itemRecord.getLocation() != null &&
                    itemRecord.getLocationLevel() != null) {
                solrInputDocument.addField(LOCATION_LEVEL_SEARCH, itemRecord.getLocation());
                solrInputDocument.addField(LOCATION_LEVEL_NAME_SEARCH, itemRecord.getLocationLevel());
                solrInputDocument.addField(LOCATION_LEVEL_DISPLAY, itemRecord.getLocation());
                solrInputDocument.addField(LOCATION_LEVEL_NAME_DISPLAY, itemRecord.getLocationLevel());
                solrInputDocument.addField(LOCATION_LEVEL_SORT, itemRecord.getLocation());
                StringTokenizer locationTokenizer = new StringTokenizer(itemRecord.getLocation(),"/");
                StringTokenizer locationLevelTokenizer = new StringTokenizer(itemRecord.getLocationLevel(),"/");
                while(locationLevelTokenizer.hasMoreTokens()){
                    String locationLevel = locationLevelTokenizer.nextToken();
                    String location = locationTokenizer.nextToken();
                    getOleDsHelperUtil().buildLocationLevels(location,locationLevel,solrInputDocument,loactionLevelStr);
                }
            }


            if (itemRecord.getItemTypeRecord() != null) {
                solrInputDocument.addField(ITEM_TYPE_FULL_VALUE_SEARCH, itemRecord.getItemTypeRecord().getName());
                solrInputDocument.addField(ITEM_TYPE_CODE_VALUE_SEARCH, itemRecord.getItemTypeRecord().getCode());
                solrInputDocument.addField(ITEM_TYPE_FULL_VALUE_DISPLAY, itemRecord.getItemTypeRecord().getName());
                solrInputDocument.addField(ITEM_TYPE_CODE_VALUE_DISPLAY, itemRecord.getItemTypeRecord().getCode());
            }

            if (itemRecord.getItemTempTypeRecord() != null) {
                solrInputDocument.addField(TEMPORARY_ITEM_TYPE_FULL_VALUE_SEARCH, itemRecord.getItemTempTypeRecord().getName());
                solrInputDocument.addField(TEMPORARY_ITEM_TYPE_CODE_VALUE_SEARCH, itemRecord.getItemTempTypeRecord().getCode());
                solrInputDocument.addField(TEMPORARY_ITEM_TYPE_FULL_VALUE_DISPLAY, itemRecord.getItemTempTypeRecord().getName());
                solrInputDocument.addField(TEMPORARY_ITEM_TYPE_CODE_VALUE_DISPLAY, itemRecord.getItemTempTypeRecord().getCode());
            }

            solrInputDocument.addField(ITEM_BARCODE_SEARCH, itemRecord.getBarCode());
            solrInputDocument.addField(ITEM_BARCODE_DISPLAY, itemRecord.getBarCode());
            solrInputDocument.addField(ITEM_URI_SEARCH, itemRecord.getUri());
            solrInputDocument.addField(ITEM_URI_DISPLAY, itemRecord.getUri());

            List<ItemStatisticalSearchRecord> itemStatisticalSearchRecords = itemRecord.getItemStatisticalSearchRecords();
            for (Iterator<ItemStatisticalSearchRecord> iterator = itemStatisticalSearchRecords.iterator(); iterator.hasNext(); ) {
                ItemStatisticalSearchRecord itemStatisticalSearchRecord = iterator.next();
                if (null != itemStatisticalSearchRecord && null != itemStatisticalSearchRecord.getStatisticalSearchRecord()) {
                    solrInputDocument.addField(STATISTICAL_SEARCHING_CODE_VALUE_SEARCH, itemStatisticalSearchRecord.getStatisticalSearchRecord().getCode());
                    solrInputDocument.addField(STATISTICAL_SEARCHING_CODE_VALUE_DISPLAY, itemStatisticalSearchRecord.getStatisticalSearchRecord().getCode());
                    solrInputDocument.addField(STATISTICAL_SEARCHING_FULL_VALUE_SEARCH, itemStatisticalSearchRecord.getStatisticalSearchRecord().getName());
                    solrInputDocument.addField(STATISTICAL_SEARCHING_FULL_VALUE_DISPLAY, itemStatisticalSearchRecord.getStatisticalSearchRecord().getName());
                }
            }

            solrInputDocument.addField(ITEM_IDENTIFIER_DISPLAY, itemIdentifierWithPrefix);
            solrInputDocument.addField(BARCODE_ARSL_DISPLAY, itemRecord.getBarCodeArsl());
            solrInputDocument.addField(COPY_NUMBER_DISPLAY, itemRecord.getCopyNumber());
            solrInputDocument.addField(PURCHASE_ORDER_LINE_ITEM_IDENTIFIER_DISPLAY, itemRecord.getPurchaseOrderItemLineId());
            solrInputDocument.addField(VENDOR_LINE_ITEM_IDENTIFIER_DISPLAY, itemRecord.getVendorLineItemId());
            solrInputDocument.addField(ENUMERATION_DISPLAY, itemRecord.getEnumeration());
            solrInputDocument.addField(CHRONOLOGY_DISPLAY, itemRecord.getChronology());
            solrInputDocument.addField(MISSING_PIECE_FLAG_NOTE_DISPLAY, itemRecord.getMissingPieceFlagNote());
            solrInputDocument.addField(CLAIMS_RETURNED_NOTE_DISPLAY, itemRecord.getClaimsReturnedNote());
            solrInputDocument.addField(DAMAGED_ITEM_NOTE_DISPLAY, itemRecord.getDamagedItemNote());
            solrInputDocument.addField(MISSING_PIECE_FLAG_DISPLAY, itemRecord.getMissingPieceFlagNote());
            solrInputDocument.addField(CLAIMS_RETURNED_FLAG_DISPLAY, itemRecord.getClaimsReturnedFlag());
            solrInputDocument.addField(ITEM_DAMAGED_FLAG_DISPLAY, itemRecord.isItemDamagedStatus());
            solrInputDocument.addField(MISSING_PIECE_COUNT_DISPLAY,itemRecord.getMissingPiecesCount());
            solrInputDocument.addField(NUMBER_OF_PIECES_DISPLAY,itemRecord.getNumberOfPieces());
            solrInputDocument.addField(CREATED_BY,itemRecord.getCreatedBy());
            solrInputDocument.addField(UPDATED_BY,itemRecord.getUpdatedBy());



            solrInputDocument.setField(DATE_UPDATED, itemRecord.getUpdatedDate());
            solrInputDocument.setField(DATE_ENTERED, itemRecord.getCreatedDate());

            List<OLEItemDonorRecord> itemDonorRecords = itemRecord.getDonorList();
            for (Iterator<OLEItemDonorRecord> iterator = itemDonorRecords.iterator(); iterator.hasNext(); ) {
                OLEItemDonorRecord itemDonorRecord = iterator.next();
                solrInputDocument.addField(DONOR_CODE_SEARCH, itemDonorRecord.getDonorCode());
                solrInputDocument.addField(DONOR_CODE_DISPLAY, itemDonorRecord.getDonorCode());
                solrInputDocument.addField(DONOR_PUBLIC_DISPLAY, itemDonorRecord.getDonorPublicDisplay());
                solrInputDocument.addField(DONOR_NOTE_DISPLAY, itemDonorRecord.getDonorNote());
            }
            if (itemRecord.getHighDensityStorageRecord() != null) {
                solrInputDocument.addField(HIGHDENSITYSTORAGE_ROW_DISPLAY, itemRecord.getHighDensityStorageRecord().getRow());
                solrInputDocument.addField(HIGHDENSITYSTORAGE_MODULE_DISPLAY, itemRecord.getHighDensityStorageRecord().getModule());
                solrInputDocument.addField(HIGHDENSITYSTORAGE_SHELF_DISPLAY, itemRecord.getHighDensityStorageRecord().getShelf());
                solrInputDocument.addField(HIGHDENSITYSTORAGE_TRAY_DISPLAY, itemRecord.getHighDensityStorageRecord().getTray());
            }
            List<ItemNoteRecord> itemNoteRecords = itemRecord.getItemNoteRecords();
            for (Iterator<ItemNoteRecord> iterator = itemNoteRecords.iterator(); iterator.hasNext(); ) {
                ItemNoteRecord oleDsItemNitemNoteRecordteT = iterator.next();
                solrInputDocument.addField(ITEMNOTE_VALUE_DISPLAY, oleDsItemNitemNoteRecordteT.getNote());
                solrInputDocument.addField(ITEMNOTE_TYPE_DISPLAY, oleDsItemNitemNoteRecordteT.getType());
            }
            solrInputDocument.addField(NUMBER_OF_RENEW, itemRecord.getNumberOfRenew());
            solrInputDocument.addField(UNIQUE_ID, itemIdentifierWithPrefix);

            //Todo : Need to do the all text part

            assignUUIDs(solrInputDocument);
        } catch (Exception e) {
            LOG.info("Exception :", e);
            e.printStackTrace();
            throw new DocstoreIndexException(e.getMessage());
        }

        return solrInputDocument;
    }

    private SolrInputDocument addItemDetailsToBib(SolrInputDocument solrInputDocument, SolrInputDocument destinationSolrInputDocument) {
        if(null != destinationSolrInputDocument) {
            addDetails(solrInputDocument, destinationSolrInputDocument, ITEM_BARCODE_SEARCH);
            addDetails(solrInputDocument, destinationSolrInputDocument, ITEM_IDENTIFIER);
            return destinationSolrInputDocument;
        }
        return null;
    }

    private SolrInputDocument addItemDetailsToHoldings(SolrInputDocument sourceSolrInputDocument,SolrInputDocument destinationSolrInputDocument) {
        if(null != destinationSolrInputDocument) {
            addDetails(sourceSolrInputDocument, destinationSolrInputDocument, ITEM_BARCODE_SEARCH);
            addDetails(sourceSolrInputDocument, destinationSolrInputDocument, ITEM_IDENTIFIER);
            return destinationSolrInputDocument;
        }
        return null;
    }
}
