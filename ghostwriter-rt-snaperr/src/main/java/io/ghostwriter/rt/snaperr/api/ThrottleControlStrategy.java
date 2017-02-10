package io.ghostwriter.rt.snaperr.api;

/**
 * Controls whether snaperr should handle the error or timeout trigger.
 * This should be used for throttle controlling, if there are too many 
 * error happening in a short time.
 * 
 * <p>Concurrency: SnaperrTracer will synchronize access to this object
 * to make sure the throttle control strategy works on all threads
 * 
 * @author pal
 *
 */
public interface ThrottleControlStrategy {
    
    /**
     * @param timeoutTrigger
     * @return
     * <li> true - SnaperrTracer will handle the timeout
     * <li> false - SnaperrTracer will NOT handle the timeout
     */
    public boolean isHandleTimeout(TimeoutTrigger timeoutTrigger);

    /**
     * @param timeoutTrigger
     * @return
     * <li> true - SnaperrTracer will handle the error
     * <li> false - SnaperrTracer will NOT handle the error
     */
    public boolean isHandleError(ErrorTrigger errorTrigger);

}
