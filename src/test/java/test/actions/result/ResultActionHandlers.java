package test.actions.result;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionMethod;

@Component
public class ResultActionHandlers {

    @ActionMethod("actionResult-result")
    public ActionResult actionResult(Action action) {
        Assert.notNull(action, "action");
        return ActionResult.SUCCESS;
    }

    @ActionMethod("int-result")
    public int intResult(Action action) {
        Assert.notNull(action, "action");
        return 42;
    }

    @ActionMethod("integer-result")
    public Integer integerResult(Action action) {
        Assert.notNull(action, "action");
        return Integer.valueOf(42);
    }

    @ActionMethod("void-result")
    public void voidResult(Action action) {
        Assert.notNull(action, "action");
    }

}
