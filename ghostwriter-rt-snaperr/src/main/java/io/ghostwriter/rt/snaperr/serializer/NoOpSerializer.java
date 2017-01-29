package io.ghostwriter.rt.snaperr.serializer;

import io.ghostwriter.rt.snaperr.api.ErrorTrigger;
import io.ghostwriter.rt.snaperr.api.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.api.TriggerSerializer;

public class NoOpSerializer implements TriggerSerializer<String> {


    @Override
    public String serializeTrigger(ErrorTrigger errorTrigger) {

        return "noop";
    }

    @Override
    public String serializeTrigger(TimeoutTrigger timeoutTrigger) {
	return "noop";
    }

}
