package test.actions.legacy;

import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.ActionHandler;
import proto.action.ActionResult;

public class LegacyActionHandler implements ActionHandler {

    @Override
    public ActionResult handle(Action action) {
        Assert.notNull(action, "action");
        return new ActionResult(42);
    }

}
