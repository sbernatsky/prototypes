package proto.action;

public class ActionResult {
    public static final ActionResult SUCCESS = new ActionResult(0);
    private final int value;

    public ActionResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
