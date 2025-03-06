package com.itsmcodez.assetstudio.util;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DomXmlModifier {

    public static String modifyXml(
            String xmlString,
            String tagName,
            String newValue,
            String attributeName,
            String newAttributeValue)
            throws ParserConfigurationException, IOException, SAXException,
                    TransformerConfigurationException, TransformerException {
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));

        NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i); // Cast to Element

            // Modify text content
            if(newValue != null) {
                element.setTextContent(newValue);
            }

            // Modify or add attribute
            if (attributeName != null && newAttributeValue != null) {
                element.setAttribute(attributeName, newAttributeValue);
            }
        }

        return serializeXml(doc); // Helper method to serialize (see below)
    }

    // Helper method to serialize the DOM to XML string
    private static String serializeXml(Document doc) throws TransformerConfigurationException, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
    
}
