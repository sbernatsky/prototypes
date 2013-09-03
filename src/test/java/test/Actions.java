package test;

import java.util.HashMap;
import java.util.Map;

import test.actions.parameters.ParameterActionHandlers;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import proto.action.Action;
import proto.action.ActionHandler;
import proto.action.ActionHandlerRegistry;
import proto.action.ActionResult;

public class Actions {

    public static void main(String[] args) throws Exception {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("test-actions.xml");
        Action action = new Action() {
            private final Map<String, String> params = new HashMap<String, String>();

            {
                params.put("param-42", "42");
                params.put("param_uuid", ParameterActionHandlers._UUID.toString());
            }

            @Override public String getName() { return null; }

            @Override
            public String getParameterValue(String name) {
                return params.get(name);
            }
        };

        try {
            ActionHandlerRegistry registry = context.getBean(ActionHandlerRegistry.class);
            for (String name : registry.getRegisteredNames()) {
                ActionHandler handler = registry.lookup(name);
                ActionResult result = handler.handle(action);
                System.out.println(name + ": " + result);
            }
        } finally {
            context.close();
        }
    }

}
