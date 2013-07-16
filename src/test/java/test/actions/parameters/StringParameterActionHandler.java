package test.actions.parameters;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;
import proto.action.annotation.ActionParameter;

@Component
public class StringParameterActionHandler {

    @ActionMethod("string-parameters")
    public void method(Action action, @ActionParameter("param-42") String param) {
        Assert.notNull(action, "action");
        Assert.isTrue("42".equals(param), "param-42");
    }
}
