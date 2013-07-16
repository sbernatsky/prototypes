package test.actions.result;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;

@Component
public class IntegerActionHandler {

    @ActionMethod("integer-result")
    public Integer method(Action action) {
        Assert.notNull(action, "action");
        return Integer.valueOf(42);
    }
}
