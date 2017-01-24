package io.ghostwriter.rt.snaperr;

import io.ghostwriter.rt.snaperr.tracker.TrackedValue;

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

}
