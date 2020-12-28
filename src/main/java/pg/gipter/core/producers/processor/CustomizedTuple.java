package pg.gipter.core.producers.processor;

import java.util.Objects;

class CustomizedTuple {
    private final String project;
    private final String listName;

    CustomizedTuple(String project, String listName) {
        this.project = project;
        this.listName = listName;
    }

    public String getProject() {
        return project;
    }

    public String getListName() {
        return listName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomizedTuple that = (CustomizedTuple) o;
        return project.equals(that.project) &&
                listName.equals(that.listName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, listName);
    }
}
