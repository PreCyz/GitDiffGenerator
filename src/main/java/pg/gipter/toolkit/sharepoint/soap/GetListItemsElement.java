package pg.gipter.toolkit.sharepoint.soap;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.util.Set;

/**Created by Pawel Gawedzki on 17-Oct-2018.*/
class GetListItemsElement {

    private GetListItemsElement() {}

    static Element titleQuery(String title) {
        try {
            Document document = initNewDocument();
            Element query = document.createElement("Query");

            Element where = document.createElement("Where");
            Element eq = document.createElement("Eq");
            Element fieldRef = document.createElement("FieldRef");
            fieldRef.setAttribute("Name", "Title");
            Element value = document.createElement("Value");
            value.setAttribute("Type", "Text");
            Text textNode = document.createTextNode(title);

            value.appendChild(textNode);
            fieldRef.appendChild(value);
            eq.appendChild(fieldRef);
            where.appendChild(eq);
            query.appendChild(where);

            return query;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document initNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    static Element queryOptions() {
        try {
            Document document = initNewDocument();
            Element queryOptions = document.createElement("QueryOptions");
            Text textNode = document.createTextNode("");
            queryOptions.appendChild(textNode);

            return queryOptions;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    static Element viewFields(Set<String> fieldRefs) {
        try {
            Document document = initNewDocument();
            Element viewFields = document.createElement("ViewFields");

            /*<Field ColName="nvarchar1" Description="" DisplayName="Work Item Name" FromBaseType="TRUE" ID="{fa564e0f-0c70-4ab9-b863-0177e6ddd247}"
            Name="Title" Required="TRUE" Sealed="TRUE" SourceID="http://schemas.microsoft.com/sharepoint/v3" StaticName="Title" Type="Text"/>*/

            for (String fieldRefName : fieldRefs) {
                Element fieldRef = document.createElement("FieldRef");
                fieldRef.setAttribute("Name", fieldRefName);
                viewFields.appendChild(fieldRef);
            }

            return viewFields;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
