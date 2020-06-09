package no.unit.nva.database.intefaces;

import java.util.Optional;
import nva.commons.utils.Environment;

public interface WithEnvironment {

    String SOME_ENV_VALUE = "*";

    /**
     * mock environment.
     *
     * @return an Environment
     */
    default Environment mockEnvironment() {
        return new Environment() {
            @Override
            public String readEnv(String variableName) {
                return SOME_ENV_VALUE;
            }

            @Override
            public Optional<String> readEnvOpt(String variableName) {
                return Optional.of(SOME_ENV_VALUE);
            }
        };
    }
}
