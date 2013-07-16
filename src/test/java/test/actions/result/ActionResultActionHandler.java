package test.actions.result;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionMethod;

@Component
public class ActionResultActionHandler {

    @ActionMethod("actionResult-result")
    public ActionResult method(Action action) {
        Assert.notNull(action, "action");
        return ActionResult.SUCCESS;
    }
}
