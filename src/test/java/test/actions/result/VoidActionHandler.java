package test.actions.result;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;

@Component
public class VoidActionHandler {

    @ActionMethod("void-result")
    public void method(Action action) {
        Assert.notNull(action, "action");
    }
}
