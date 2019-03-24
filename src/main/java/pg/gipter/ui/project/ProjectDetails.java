package pg.gipter.ui.project;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import pg.gipter.settings.ArgName;

import java.util.Objects;

/** Pawel Gawedzki 2019-03-24 */
public class ProjectDetails {
    private StringProperty name;
    private StringProperty cvsType;
    private StringProperty path;

    static ProjectDetails DEFAULT = new ProjectDetails(
            ArgName.projectPath.defaultValue(), "N/A", ArgName.projectPath.defaultValue()
    );

    public ProjectDetails(String name, String cvsType, String path) {
        this.name = new SimpleStringProperty(name);
        this.cvsType = new SimpleStringProperty(cvsType);
        this.path = new SimpleStringProperty(path);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getCvsType() {
        return cvsType.get();
    }

    public StringProperty cvsTypeProperty() {
        return cvsType;
    }

    public String getPath() {
        return path.get();
    }

    public StringProperty pathProperty() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectDetails that = (ProjectDetails) o;
        return getName().equals(that.getName()) &&
                getCvsType().equals(that.getCvsType()) &&
                getPath().equals(that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCvsType(), getPath());
    }

    @Override
    public String toString() {
        return "ProjectDetails{" +
                "name=" + name +
                ", cvsType=" + cvsType +
                ", path=" + path +
                '}';
    }
}
