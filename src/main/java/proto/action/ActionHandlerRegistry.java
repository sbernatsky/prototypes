package proto.action;

public interface ActionHandlerRegistry {
    ActionHandler lookup(String name);
}
