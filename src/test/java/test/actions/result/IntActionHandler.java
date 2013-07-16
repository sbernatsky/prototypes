package test.actions.result;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;

@Component
public class IntActionHandler {

    @ActionMethod("int-result")
    public int method(Action action) {
        Assert.notNull(action, "action");
        return 42;
    }
}
