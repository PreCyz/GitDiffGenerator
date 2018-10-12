package pg.gipter.toolkit;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Created by Pawel Gawedzki on 12-Oct-2018.
 */
public class WSClient<R> extends WebServiceGatewaySupport {

    /*private static final String WS_OBJECTS_PACKAGE = "pg.gipter.toolkit.ws.reuse";
    private static final String HEADER_NAMESPACE_URI = "https://goto.netcompany.com/cases/GTE106/NCSCOPY";

    protected final ObjectFactory objectFactory;

    private final String userName;
    private final String password;
    private final String domain;

    protected R response;

    public WSClient(String userName, String password, String domain) {
        this.userName = userName;
        this.password = password;
        objectFactory = new ObjectFactory();
        this.domain = domain;
        setupMarshaller();
    }

    private void setupMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(WS_OBJECTS_PACKAGE);
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    protected <T> R calculateResponseAndHeader(JAXBElement<T> element) {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setPackagesToScan(WS_OBJECTS_PACKAGE);
        return getWebServiceTemplate().sendAndReceive(
                getWsAddress(),
                message -> {
                    MarshallingUtils.marshal(jaxb2Marshaller, element, message);
                    updateMessage((SaajSoapMessage) message);
                },
                message -> {
                    SoapHeader header = ((SoapMessage) message).getSoapHeader();
                    Iterator<SoapHeaderElement> it = header.examineHeaderElements(
                            new QName(HEADER_NAMESPACE_URI, "CommonIdentifierResponse")
                    );
                    //responseHeader = it.hasNext() ? (CommonIdentifierResponse) jaxb2Marshaller.unmarshal(it.next().getSource()) : null;
                    JAXBElement<R> responseElement = (JAXBElement<R>) MarshallingUtils.unmarshal(jaxb2Marshaller, message);
                    return responseElement.getValue();
                }
        );
    }

    private void updateMessage(SaajSoapMessage message) {
        try {
            SOAPMessage soapMessage = message.getSaajMessage();
            addHeader(soapMessage);
            soapMessage.saveChanges();
        } catch (SOAPException ex) {
            String errMsg = String.format("Error occurred when calling WS uri [%s].", getWsAddress());
            throw new RuntimeException(errMsg);
        }
    }

    private void addHeader(SOAPMessage soapMessage) throws SOAPException {
        SOAPHeader header = soapMessage.getSOAPHeader();
        addSecurity(header);
        addCommonIdentifier(header);
    }

    private void addSecurity(SOAPHeader header) throws SOAPException {
        SOAPHeaderElement security = header.addHeaderElement(
                new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse")
        );
        SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
        usernameToken.addAttribute(
                new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "wsu"), "UsernameToken-1"
        );
        SOAPElement username = usernameToken.addChildElement("Username", "wsse");
        SOAPElement password = usernameToken.addChildElement("Password", "wsse");
        password.addAttribute(
                new QName("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText"
        );
        username.setValue(userName);
        password.setValue(this.password);
    }*/

    /*private void addCommonIdentifier(SOAPHeader header) throws SOAPException {
        SOAPElement commonIdentifier = header.addHeaderElement(
                new QName(HEADER_NAMESPACE_URI, "CommonIdentifierRequest")
        );
        //mandatory

        SOAPElement applicationRequesterElement = commonIdentifier.addChildElement(new QName(HEADER_NAMESPACE_URI, "ApplicationRequester"));
        SOAPElement applicationID = applicationRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "ApplicationID"));
        applicationID.setValue(applicationRequester.getApplicationID());
        SOAPElement channelID = applicationRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "ChannelID"));
        channelID.setValue(applicationRequester.getChannelID());
        SOAPElement operatorID = applicationRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "OperatorID"));
        operatorID.setValue(applicationRequester.getOperatorID());
        //Optional
        if (!StringUtils.nullOrEmpty(applicationRequester.getApplicationID())) {
            SOAPElement applicationUserID = applicationRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "ApplicationUserID"));
            applicationUserID.setValue(applicationRequester.getApplicationUserID());
        }
        if (!StringUtils.nullOrEmpty(applicationRequester.getTransactionID())) {
            SOAPElement transactionID = applicationRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "TransactionID"));
            transactionID.setValue(applicationRequester.getTransactionID());
        }

        CommonIdentifierRequest.UserRequester userRequester = createUserRequester();
        SOAPElement userRequesterElement = commonIdentifier.addChildElement(new QName(HEADER_NAMESPACE_URI, "UserRequester"));
        //Optional
        if (!StringUtils.nullOrEmpty(userRequester.getRequesterID())) {
            SOAPElement requesterID = userRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "RequesterID"));
            requesterID.setValue(userRequester.getRequesterID());
        }
        if (!StringUtils.nullOrEmpty(userRequester.getCustomerCode())) {
            SOAPElement customerCode = userRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "CustomerCode"));
            customerCode.setValue(userRequester.getCustomerCode());
        }
        if (!StringUtils.nullOrEmpty(userRequester.getCustomerName())) {
            SOAPElement customerName = userRequesterElement.addChildElement(new QName(HEADER_NAMESPACE_URI, "CustomerName"));
            customerName.setValue(userRequester.getCustomerName());
        }
    }

    protected String getCarrierCode() {
        return carrierCode;
    }

    protected String getWsAddress() {
        return wsAddress;
    }

    public CommonIdentifierResponse getResponseHeader() {
        return responseHeader;
    }

    public R executeRequest() {
        return null;
    }*/
}
