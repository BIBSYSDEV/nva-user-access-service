package no.unit.nva.database.intefaces;

import java.util.Map;
import java.util.Optional;
import nva.commons.utils.Environment;

public interface WithEnvironment {

    String SOME_ENV_VALUE = "*";

    /**
     * mock environment. Returns "*" for
     *
     * @return an Environment that returns "*" for all env variables.
     */
    default Environment mockEnvironment() {
        return mockEnvironment(SOME_ENV_VALUE);
    }

    /**
     * Mock environment.
     *
     * @param returnValueForAllEnvVariables the value to be returned for every env variable.
     * @return return the parameter for every env variable.
     */
    default Environment mockEnvironment(String returnValueForAllEnvVariables) {
        return new Environment() {
            @Override
            public String readEnv(String variableName) {
                return returnValueForAllEnvVariables;
            }

            @Override
            public Optional<String> readEnvOpt(String variableName) {
                return Optional.of(returnValueForAllEnvVariables);
            }
        };
    }

    default Environment mockEnvironment(Map<String, String> envVariables, String defaultValue) {
        return new Environment() {
            @Override
            public String readEnv(String variableName) {
                if (envVariables.containsKey(variableName)) {
                    return envVariables.get(variableName);
                } else {
                    return defaultValue;
                }
            }

            @Override
            public Optional<String> readEnvOpt(String variableName) {
                return Optional.ofNullable(readEnv(variableName));
            }
        };
    }
}
