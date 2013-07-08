package proto.action.spring.converters;

import org.springframework.core.convert.converter.Converter;
import proto.action.ActionResult;

public class VoidToActionResultConverter implements Converter<Void, ActionResult> {

    @Override
    public ActionResult convert(Void s) {
        return ActionResult.SUCCESS;
    }

}
