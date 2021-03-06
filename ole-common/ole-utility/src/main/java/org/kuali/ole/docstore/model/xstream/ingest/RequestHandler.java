/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.docstore.model.xstream.ingest;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.kuali.ole.docstore.model.xmlpojo.ingest.*;

import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: pvsubrah
 * Date: 9/7/11
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestHandler {
    public Request toObject(String requestXML) {
        XStream xStream = new XStream();
        xStream.registerConverter(new RequestDocumentConverter());
        xStream.alias("request", Request.class);
        xStream.alias("ingestDocument", RequestDocument.class);
        xStream.alias("linkedIngestDocument", RequestDocument.class);
        xStream.alias("content", Content.class);
        xStream.alias("additionalAttributes", AdditionalAttributes.class);
        Request request = (Request) xStream.fromXML(requestXML);
        return request;
    }

    public String toXML(Request request) {
        XStream xStream = new XStream(
                new XppDriver() {
                    public HierarchicalStreamWriter createWriter(Writer out) {
                        return new PrettyPrintWriter(out) {
                            protected void writeText(QuickWriter writer, String text) {
                                writer.write("<![CDATA[");
                                if (text == null) {
                                    text = "";
                                }
                                writer.write(text);
                                writer.write("]]>");
                            }
                        };
                    }
                }
        );
        xStream.registerConverter(new RequestDocumentConverter());
        xStream.alias("request", Request.class);
        xStream.alias("ingestDocument", RequestDocument.class);
        String xml = xStream.toXML(request);
        return xml;
    }
}
