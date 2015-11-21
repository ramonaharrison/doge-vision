package nyc.c4q.ramonaharrison.dogevision;

/**
 * Created by Ramona Harrison
 * on 6/14/15.
 */

public class DescriptionMessage {
    private String status;
    private String name;
    private String reason;

    public DescriptionMessage(String status, String name, String reason) {
        this.status = status;
        this.name = name;
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return this.name;
    }
}