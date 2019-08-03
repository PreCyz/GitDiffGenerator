package pg.gipter.job.upload;

class JobPair {
    private final String name;
    private final String group;

    public JobPair(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }
}
