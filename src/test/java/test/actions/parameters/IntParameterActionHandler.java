package test.actions.parameters;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;
import proto.action.annotation.ActionParameter;

@Component
public class IntParameterActionHandler {

    @ActionMethod("int-parameters")
    public void method(Action action, @ActionParameter("param-42") int param) {
        Assert.notNull(action, "action");
        Assert.isTrue(param == 42, "param-42");
    }
}
