package pg.gipter.toolkit.configuration;

import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import pg.gipter.toolkit.ws.GetList;
import pg.gipter.toolkit.ws.GetListResponse;
import pg.gipter.toolkit.ws.ObjectFactory;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
public class SharePointClient {

    private static final String WS_URL = "https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx";
    private static final SoapActionCallback GET_LIST_SOAP_ACTION = new SoapActionCallback("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists");

    private WebServiceTemplate webServiceTemplate;
    private final ObjectFactory objectFactory;

    public SharePointClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
        objectFactory = new ObjectFactory();
    }

    public void getListAndView() {
        GetList request = buildGetListRequest();
        GetListResponse response = (GetListResponse) webServiceTemplate.marshalSendAndReceive(WS_URL, request, GET_LIST_SOAP_ACTION);

        /*for (Object content : response.getGetListResult().getContent()) {
            System.out.println(content);
        }*/
    }

    private GetList buildGetListRequest() {
        GetList getListRequest = objectFactory.createGetList();
        getListRequest.setListName("WorkItems");
        return getListRequest;
    }

}
