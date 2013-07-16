package proto.action;

import java.util.Set;

public interface ActionHandlerRegistry {
    ActionHandler lookup(String name);
    Set<String> getRegisteredNames();
}
