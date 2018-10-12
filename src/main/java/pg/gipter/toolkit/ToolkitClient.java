package pg.gipter.toolkit;

import pg.gipter.toolkit.ws.GetListAndView;
import pg.gipter.toolkit.ws.GetListAndViewResponse;
import pg.gipter.toolkit.ws.ObjectFactory;

class ToolkitClient {

    private final ObjectFactory objectFactory;

    ToolkitClient() {
        this.objectFactory = new ObjectFactory();
    }

    void getListAndView() {
        GetListAndView getListAndView = objectFactory.createGetListAndView();
        getListAndView.setListName("WorkItems");
        getListAndView.setViewName("");

        GetListAndViewResponse getListAndViewResponse = objectFactory.createGetListAndViewResponse();
        GetListAndViewResponse.GetListAndViewResult listAndViewResult = getListAndViewResponse.getGetListAndViewResult();
        for (Object result : listAndViewResult.getContent()) {
            System.out.println(result);
        }
    }
}
