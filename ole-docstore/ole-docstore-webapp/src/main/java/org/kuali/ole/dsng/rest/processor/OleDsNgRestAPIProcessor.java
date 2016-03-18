package org.kuali.ole.dsng.rest.processor;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kuali.ole.DocumentUniqueIDPrefix;
import org.kuali.ole.Exchange;
import org.kuali.ole.constants.OleNGConstants;
import org.kuali.ole.docstore.common.document.EHoldings;
import org.kuali.ole.docstore.common.document.Holdings;
import org.kuali.ole.docstore.common.exception.DocstoreException;
import org.kuali.ole.docstore.common.exception.DocstoreResources;
import org.kuali.ole.docstore.common.exception.DocstoreValidationException;
import org.kuali.ole.docstore.engine.service.storage.rdbms.RdbmsHoldingsDocumentManager;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.BibRecord;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.HoldingsRecord;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.ItemRecord;
import org.kuali.ole.dsng.indexer.BibIndexer;
import org.kuali.ole.dsng.indexer.HoldingIndexer;
import org.kuali.ole.dsng.indexer.ItemIndexer;
import org.kuali.ole.dsng.indexer.OleDsNgIndexer;
import org.kuali.ole.dsng.rest.handler.eholdings.CreateEHoldingsHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by SheikS on 11/25/2015.
 */
@Service("oleDsNgRestAPIProcessor")
public class OleDsNgRestAPIProcessor extends OleDsNgOverlayProcessor {

    public String createBib(String jsonBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        BibRecord bibRecord = objectMapper.readValue(jsonBody, BibRecord.class);
        bibDAO.save(bibRecord);
        OleDsNgIndexer bibIndexer = new BibIndexer();
        bibIndexer.indexDocument(bibRecord);
        return objectMapper.writeValueAsString(bibRecord);
    }


    public String updateBib(String jsonBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        BibRecord bibRecord = objectMapper.readValue(jsonBody, BibRecord.class);

        if(StringUtils.isNotBlank(bibRecord.getBibId())){
            bibDAO.save(bibRecord);
            BibIndexer bibIndexer = new BibIndexer();
            bibIndexer.updateDocument(bibRecord);
            return objectMapper.writeValueAsString(bibRecord);
        } else {
            DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.BIB_ID_NOT_FOUND, DocstoreResources.BIB_ID_NOT_FOUND);
            docstoreException.addErrorParams("bibId", bibRecord.getBibId());
            throw docstoreException;
        }

    }

    public String createHolding(String jsonBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        HoldingsRecord holdingsRecord = objectMapper.readValue(jsonBody, HoldingsRecord.class);
        holdingDAO.save(holdingsRecord);
        OleDsNgIndexer bibIndexer = new HoldingIndexer();
        bibIndexer.indexDocument(holdingsRecord);
        return objectMapper.writeValueAsString(holdingsRecord);
    }


    public String updateHolding(String jsonBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        HoldingsRecord holdingsRecord = objectMapper.readValue(jsonBody, HoldingsRecord.class);

        if(StringUtils.isNotBlank(holdingsRecord.getHoldingsId())){
            holdingDAO.save(holdingsRecord);
            OleDsNgIndexer holdingIndexer = new HoldingIndexer();
            holdingIndexer.updateDocument(holdingsRecord);
            return objectMapper.writeValueAsString(holdingsRecord);
        } else {
            DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.HOLDING_ID_NOT_FOUND, DocstoreResources.HOLDING_ID_NOT_FOUND);
            docstoreException.addErrorParams("holdingId", holdingsRecord.getHoldingsId());
            throw docstoreException;
        }

    }

    public String createItem(String jsonBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemRecord itemRecord = objectMapper.readValue(jsonBody, ItemRecord.class);
        itemDAO.save(itemRecord);
        OleDsNgIndexer itemIndexer = new ItemIndexer();
        itemIndexer.indexDocument(itemRecord);
        return objectMapper.writeValueAsString(itemRecord);
    }


    public String updateItem(String jsonBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemRecord itemRecord = objectMapper.readValue(jsonBody, ItemRecord.class);

        if(StringUtils.isNotBlank(itemRecord.getItemId())){
            itemDAO.save(itemRecord);
            OleDsNgIndexer itemIndexer = new ItemIndexer();
            itemIndexer.updateDocument(itemRecord);
            return objectMapper.writeValueAsString(itemRecord);
        } else {
            DocstoreException docstoreException = new DocstoreValidationException(DocstoreResources.ITEM_ID_NOT_FOUND, DocstoreResources.ITEM_ID_NOT_FOUND);
            docstoreException.addErrorParams("itemId", itemRecord.getItemId());
            throw docstoreException;
        }

    }

    public String createDummyHoldings(String body) {
        String serializedContent = null;
        JSONObject responseObject = new JSONObject();
        try {
            JSONObject requestJson = new JSONObject(body);
            String holdingsType = getStringValueFromJsonObject(requestJson, OleNGConstants.HOLDINGS_TYPE);
            JSONObject dataMappings = getJSONObjectFromJSONObject(requestJson, OleNGConstants.DATAMAPPING);
            if(StringUtils.isNotBlank(holdingsType)) {
                if(holdingsType.equalsIgnoreCase(EHoldings.ELECTRONIC)) {
                    serializedContent = createEHoldings(dataMappings);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            responseObject.put(OleNGConstants.CONTENT, serializedContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseObject.toString();
    }

    private String createEHoldings(JSONObject dataMappings) {
        HoldingsRecord holdingsRecord = new HoldingsRecord();
        holdingsRecord.setHoldingsType(EHoldings.ELECTRONIC);
        CreateEHoldingsHandler createEHoldingsHandler = new CreateEHoldingsHandler();
        holdingsRecord.setUniqueIdPrefix(DocumentUniqueIDPrefix.PREFIX_WORK_HOLDINGS_OLEML);

        Exchange exchange = new Exchange();
        exchange.add(OleNGConstants.HOLDINGS_RECORD, holdingsRecord);
        exchange.add(OleNGConstants.DATAMAPPING, dataMappings);
        try {
            createEHoldingsHandler.processDataMappings(dataMappings, exchange);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holdingsRecord = (HoldingsRecord) exchange.get(OleNGConstants.HOLDINGS_RECORD);
        RdbmsHoldingsDocumentManager rdbmsHoldingsDocumentManager = new RdbmsHoldingsDocumentManager();
        Holdings holdings = rdbmsHoldingsDocumentManager.buildHoldingsFromHoldingsRecord(holdingsRecord);
        holdings.serializeContent();
        String serializedContent = new EHoldings().serialize(holdings);
        return serializedContent;
    }
}

