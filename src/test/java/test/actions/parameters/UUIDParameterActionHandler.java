package test.actions.parameters;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;
import proto.action.annotation.ActionParameter;

@Component
public class UUIDParameterActionHandler {
    public static final UUID _UUID = UUID.randomUUID();

    @ActionMethod("uuid-parameters")
    public void method(Action action, @ActionParameter("param-uuid") UUID param) {
        Assert.notNull(action, "action");
        Assert.isTrue(_UUID.equals(param), "param-uuid");
    }
}
