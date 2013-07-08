package proto.action.spring;

import proto.action.Action;
import proto.action.ActionHandler;
import proto.action.ActionResult;

class BridgedActionHandler implements ActionHandler {
    private final HandlerMethod handler;

    public BridgedActionHandler(HandlerMethod handler) {
        this.handler = handler;
    }

    @Override
    public ActionResult handle(Action action) {
        return handler.invoke(action);
    }

}
