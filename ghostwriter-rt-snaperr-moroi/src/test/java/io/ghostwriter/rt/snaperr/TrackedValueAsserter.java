package io.ghostwriter.rt.snaperr;


import io.ghostwriter.rt.snaperr.tracker.TrackedValue;
import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;
import io.ghostwriter.rt.snaperr.trigger.TimeoutTrigger;
import io.ghostwriter.rt.snaperr.trigger.TriggerHandler;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class TrackedValueAsserter {

    public static void assertNumberOfTrackedVariables(Map<String, TrackedValue> watched, int expectedSize) {
        assertTrue("Expected scope to contain '" + expectedSize + "' watched, got:" + watched.size(), watched.size() == expectedSize);
    }

    public static <T> void assertTrackedValue(Map<String, TrackedValue> watched, String variableName, T expectedVariableValue) {
        final Class<?> expectedTypeOfVariable = expectedVariableValue.getClass();

        // Verify that the variable is present among the watched ones
        assertTrue("Variable '" + variableName + "' is not watched!", watched.containsKey(variableName));
        TrackedValue watchedVariable = watched.get(variableName);

        Object valueOfVariable = watchedVariable.getValue();
        Class<?> typeOfVariable = valueOfVariable.getClass();

        // Verify type correctness
        assertTrue("Expected type of '" + variableName + "' is " + expectedTypeOfVariable.getName() + ", got: " + typeOfVariable.getName(),
                typeOfVariable.equals(expectedTypeOfVariable));

        // Verify value correctness
        assertTrue("Expected value of '" + variableName + "' is '" + expectedVariableValue + "', got: " + valueOfVariable,
                valueOfVariable.equals(expectedVariableValue));
    }

    // used to initialize GhostWriterSnaperr with a handler that does not have runtime dependency requirements like the SLF4J handler.
    public static TriggerHandler noopTriggerHandler() {
        return new TriggerHandler() {
            @Override
            public void onError(ErrorTrigger errorTrigger) {
                return;
            }

            @Override
            public void onTimeout(TimeoutTrigger timeoutTrigger) {
                return;
            }
        };
    }

}
