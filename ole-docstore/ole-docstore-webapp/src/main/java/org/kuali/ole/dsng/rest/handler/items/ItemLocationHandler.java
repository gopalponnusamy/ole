package org.kuali.ole.dsng.rest.handler.items;

import org.codehaus.jettison.json.JSONObject;
import org.kuali.ole.describe.bo.OleLocation;
import org.kuali.ole.describe.bo.OleLocationLevel;
import org.kuali.ole.docstore.engine.service.storage.rdbms.pojo.ItemRecord;
import org.kuali.ole.dsng.rest.Exchange;
import org.kuali.ole.dsng.util.LocationUtil;

import java.util.*;

/**
 * Created by SheikS on 12/20/2015.
 */
public class ItemLocationHandler extends ItemHandler {

    private LocationUtil locationUtil;

    @Override
    public Boolean isInterested(String operation) {
        return (operation.equals(LOCATION_LEVEL_1) || operation.equals(LOCATION_LEVEL_2) || operation.equals(LOCATION_LEVEL_3) ||
                operation.equals(LOCATION_LEVEL_4) || operation.equals(LOCATION_LEVEL_5));
    }

    @Override
    public void process(JSONObject requestJsonObject, Exchange exchange) {

        ItemRecord itemRecord = (ItemRecord) exchange.get("itemRecord");

        for (Iterator iterator = requestJsonObject.keys(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            String value = getStringValueFromJsonObject(requestJsonObject, key);


            OleLocation locationBasedOnCode = locationUtil.getLocationByCode(value);

            OleLocationLevel oleLocationLevel = locationBasedOnCode.getOleLocationLevel();
            String matchPointLevelId = oleLocationLevel.getLevelId();

            Map map = new TreeMap();
            String location = itemRecord.getLocation();
            StringTokenizer stringTokenizer = new StringTokenizer(location, "/");


            while (stringTokenizer.hasMoreTokens()) {
                String token = stringTokenizer.nextToken();
                map.put(getLocationUtil().getLevelIdByLocationCode(token), token);
            }
            if (map.get(matchPointLevelId).equals(value)) {
                exchange.add("matchedItem", itemRecord);
                break;
            }

        }

    }

    public LocationUtil getLocationUtil() {
        if(null == locationUtil){
            locationUtil = new LocationUtil();
        }
        return locationUtil;
    }

    @Override
    public void processDataMappings(JSONObject requestJsonObject, Exchange exchange) {
        ItemRecord itemRecord = (ItemRecord) exchange.get("itemRecord");
        // Todo : Set Location.
        exchange.add("itemRecord", itemRecord);
    }
}
