package pg.gipter.ui.project;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import pg.gipter.core.ArgName;

import java.util.Objects;

/** Pawel Gawedzki 2019-03-24 */
public class ProjectDetails {
    private StringProperty name;
    private StringProperty vcsType;
    private StringProperty path;

    public static final ProjectDetails DEFAULT = new ProjectDetails(
            ArgName.projectPath.defaultValue(), "N/A", ArgName.projectPath.defaultValue()
    );

    public ProjectDetails(String name, String vcsType, String path) {
        this.name = new SimpleStringProperty(name);
        this.vcsType = new SimpleStringProperty(vcsType);
        this.path = new SimpleStringProperty(path);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setVcsType(String vcsType) {
        this.vcsType.set(vcsType);
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public String getName() {
        return name.get();
    }

    public String getVcsType() {
        return vcsType.get();
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
                getVcsType().equals(that.getVcsType()) &&
                getPath().equals(that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVcsType(), getPath());
    }

    @Override
    public String toString() {
        return "ProjectDetails{" +
                "name=" + name +
                ", cvsType=" + vcsType +
                ", path=" + path +
                '}';
    }
}
