package io.ghostwriter.rt.snaperr.api;

public interface TriggerSerializer<T> {

    /***
     * Convert the {@link ErrorTrigger} instance to a given format
     * @param errorTrigger Instance to be converted
     * @return Serialized format of the  {@link ErrorTrigger} instance
     */
    T serializeTrigger(ErrorTrigger errorTrigger);

    /***
     * Convert the {@link io.ghostwriter.rt.snaperr.api.TimeoutTrigger} instance to a given format
     * @param timeoutTrigger Instance to be converted
     * @return Serialized format of the  {@link ErrorTrigger} instance
     */
    T serializeTrigger(TimeoutTrigger timeoutTrigger);

}
