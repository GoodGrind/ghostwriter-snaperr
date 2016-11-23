package io.ghostwriter.rt.snaperr.serializer;


import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;
import io.ghostwriter.rt.snaperr.trigger.TimeoutTrigger;

public interface TriggerSerializer<T> {

    /***
     * Convert the {@link ErrorTrigger} instance to a given format
     * @param errorTrigger Instance to be converted
     * @return Serialized format of the  {@link ErrorTrigger} instance
     */
    T serializeTrigger(ErrorTrigger errorTrigger);

    /***
     * Convert the {@link io.ghostwriter.rt.snaperr.trigger.TimeoutTrigger} instance to a given format
     * @param timeoutTrigger Instance to be converted
     * @return Serialized format of the  {@link ErrorTrigger} instance
     */
    T serializeTrigger(TimeoutTrigger timeoutTrigger);

}
