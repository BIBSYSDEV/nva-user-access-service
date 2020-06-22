package features;

import no.unit.nva.database.intefaces.WithEnvironment;

public abstract class ScenarioTest implements WithEnvironment {

    protected static final int IGNORE_HEADER_ROW = 1;
    protected static final String HTTP_METHOD = "httpMethod";
}
