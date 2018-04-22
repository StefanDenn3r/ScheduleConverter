import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Event {
    private String title;
    private String location;
    private Date start;
    private Date end;

    public Event() {
    }

}
