package pg.gipter.services.dto;

public class CreationTime {
    public final long baseTime;
    public final long subtime;

    public CreationTime(long baseTime, long subtime) {
        this.baseTime = baseTime;
        this.subtime = subtime;
    }
}
