package test.actions.parameters;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;
import proto.action.annotation.ActionParameter;

@Component
public class ParameterActionHandlers {
    public static final UUID _UUID = UUID.randomUUID();

    @ActionMethod("int-parameters")
    public void intParameter(Action action, @ActionParameter("param-42") int param) {
        Assert.notNull(action, "action");
        Assert.isTrue(param == 42, "param-42");
    }

    @ActionMethod("string-parameters")
    public void stringParameter(Action action, @ActionParameter("param-42") String param) {
        Assert.notNull(action, "action");
        Assert.isTrue("42".equals(param), "param-42");
    }

    @ActionMethod("uuid-parameters")
    public void uuidParameter(Action action, @ActionParameter("param_uuid") UUID param) {
        Assert.notNull(action, "action");
        Assert.isTrue(_UUID.equals(param), "param_uuid");
    }

    @ActionMethod("optional-parameters")
    public void optionalParameter(Action action, @ActionParameter(value = "param-optional", required = false) String param) {
        Assert.notNull(action, "action");
        Assert.isNull(param, "param-optional");
    }
}
