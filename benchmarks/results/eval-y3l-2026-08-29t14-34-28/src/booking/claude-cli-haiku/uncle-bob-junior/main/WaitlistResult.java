import java.time.*;
import java.util.*;

class WaitlistResult {
    private final boolean success;
    private final String entryId;
    private final int position;

    private WaitlistResult(boolean success, String entryId, int position) {
        this.success = success;
        this.entryId = entryId;
        this.position = position;
    }

    boolean isSuccess() {
        return success;
    }

    String entryId() {
        return entryId;
    }

    int position() {
        return position;
    }

    static WaitlistResult queued(String entryId, int position) {
        return new WaitlistResult(true, entryId, position);
    }
}
