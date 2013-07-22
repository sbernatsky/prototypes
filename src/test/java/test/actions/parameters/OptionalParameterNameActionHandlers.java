package test.actions.parameters;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.annotation.ActionMethod;
import proto.action.annotation.ActionParameter;

@Component
public class OptionalParameterNameActionHandlers {

    @ActionMethod("optional-uuid-parameters")
    public void uuidParameter(Action action, @ActionParameter UUID param_uuid) {
        Assert.notNull(action, "action");
        Assert.isTrue(ParameterActionHandlers._UUID.equals(param_uuid), "param_uuid");
    }

}
