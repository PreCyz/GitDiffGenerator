package pg.gipter.ui;

/**Created by Gawa 2019-03-02*/
class MainWindow extends AbstractWindow {

    MainWindow(AbstractController controller) {
        super(controller);
    }

    @Override
    protected String fxmlFileName() {
        return "main.fxml";
    }

    @Override
    protected String windowImgFileName() {
        return "chicken-face.jpg";
    }

    @Override
    protected String cssFileName() {
        return "main.css";
    }

    @Override
    public String windowTitleBundle() {
        return "main.title";
    }
}
