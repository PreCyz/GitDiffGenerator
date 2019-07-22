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

    public void setName(String name) {
        this.name.set(name);
    }

    public void setCvsType(String cvsType) {
        this.cvsType.set(cvsType);
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public String getName() {
        return name.get();
    }

    public String getCvsType() {
        return cvsType.get();
    }

    public String getPath() {
        return path.get();
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
