package pg.gipter.testfx;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import org.testfx.api.FxRobot;

import java.util.*;

import static java.util.stream.Collectors.toCollection;

public abstract class AbstractWindowObject {

    protected final FxRobot robot;

    protected AbstractWindowObject(FxRobot robot) {
        this.robot = robot;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T> ComboBox<T> getComboBox(String id) {
        final Set<ComboBox> comboBoxes = robot.lookup(".combo-box").queryAllAs(ComboBox.class);
        return (ComboBox<T>) getNode(comboBoxes, id);
    }

    protected Hyperlink getHyperLink(String id) {
        final Set<Hyperlink> hyperlinks = robot.lookup(".hyperlink").queryAllAs(Hyperlink.class);
        return getNode(hyperlinks, id);
    }

    private <T extends Node> T getNode(Set<T> nodes, String id) {
        final LinkedList<T> result = nodes.stream()
                .filter(cb -> id.equals(cb.getId()))
                .collect(toCollection(LinkedList::new));
        return Optional.of(result)
                .map(LinkedList::getFirst)
                .orElseThrow(NoSuchElementException::new);
    }

}
