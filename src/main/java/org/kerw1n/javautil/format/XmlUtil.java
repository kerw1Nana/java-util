package org.kerw1n.javautil.format;

import org.kerw1n.javautil.file.IoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.kerw1n.javautil.constant.BaseConst.CHARSET_UTF8_VALUE;

/**
 * xml 解析工具类
 *
 * @author Guan Yonchao
 */
public class XmlUtil {

    /**
     * map 转 xml
     *
     * @param data 数据
     * @return xml 字符串
     * @throws Exception
     */
    public static String mapToXml(Map<String, String> data) throws Exception {
        Document document = newDocument();
        Element root = document.createElement("xml");
        document.appendChild(root);

        for (String key : data.keySet()) {
            String value = data.get(key) == null ? "" : data.get(key).trim();

            Element filed = document.createElement(key);
            filed.appendChild(document.createTextNode(value));
            root.appendChild(filed);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, CHARSET_UTF8_VALUE);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.getBuffer().toString();
        } finally {
            IoUtil.close(writer);
        }
    }

    /**
     * xml 转 map
     *
     * @param xmlStr xml 字符串
     * @return map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String xmlStr) throws Exception {
        InputStream stream = null;
        try {
            Map<String, String> data = new HashMap<>();
            DocumentBuilder documentBuilder = newDocumentBuilder();
            stream = new ByteArrayInputStream(xmlStr.getBytes(CHARSET_UTF8_VALUE));
            Document doc = documentBuilder.parse(stream);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                Node node = nodeList.item(idx);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    data.put(element.getNodeName(), element.getTextContent());
                }
            }
            return data;
        } finally {
            IoUtil.close(stream);
        }
    }

    /**
     * 创建 Dom 解析器.
     *
     * @return 解析器对象
     * @throws ParserConfigurationException
     */
    private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);

        return documentBuilderFactory.newDocumentBuilder();
    }

    private static Document newDocument() throws ParserConfigurationException {
        return newDocumentBuilder().newDocument();
    }
}
