package nl.geostandaarden.imx.orchestrate.souce.rest.executor;

import graphql.*;
import graphql.collect.ImmutableKit;
import graphql.execution.ExecutionId;
import graphql.execution.RawVariables;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationState;
import lombok.Getter;
import lombok.Setter;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.annotations.PublicApi;

import java.util.Locale;
import java.util.Map;

import static graphql.Assert.assertNotNull;
@PublicApi
@Getter
@Setter
public class ExecutionInputRest {
    private final String query;
    private final String operationName;
    private final Object localContext;
    private final Object root;
    private final RawVariables rawVariables;
    private final Map<String, Object> extensions;
    private final DataLoaderRegistry dataLoaderRegistry;
    private final ExecutionId executionId;
    private final Locale locale;


    @Internal
    private ExecutionInputRest(ExecutionInputRest.Builder builder) {
        this.query = assertNotNull(builder.query, () -> "query can't be null");
        this.operationName = builder.operationName;
        this.root = builder.root;
        this.rawVariables = builder.rawVariables;
        this.dataLoaderRegistry = builder.dataLoaderRegistry;
        this.executionId = builder.executionId;
        this.locale = builder.locale != null ? builder.locale : Locale.getDefault(); // always have a locale in place
        this.localContext = builder.localContext;
        this.extensions = builder.extensions;

    }

    public static ExecutionInputRest.Builder newExecutionInput() {
        return new ExecutionInputRest.Builder();
    }

    /**
     * Creates a new builder of ExecutionInput objects with the given query
     *
     * @param query the query to execute
     *
     * @return a new builder of ExecutionInput objects
     */
    public static ExecutionInput.Builder newExecutionInput(String query) {
        return new ExecutionInput.Builder().query(query);
    }

    public Map<String, Object> getVariables() {
        return this.rawVariables.toMap();
    }

    public static class Builder {

        private String query;
        private String operationName;
        private Object localContext;
        private Object root;
        private RawVariables rawVariables = RawVariables.emptyVariables();
        public Map<String, Object> extensions = ImmutableKit.emptyMap();
        //
        // this is important - it allows code to later known if we never really set a dataloader and hence it can optimize
        // dataloader field tracking away.
        //
        private DataLoaderRegistry dataLoaderRegistry = DataLoaderDispatcherInstrumentationState.EMPTY_DATALOADER_REGISTRY;
        private Locale locale = Locale.getDefault();
        private ExecutionId executionId;

        public ExecutionInputRest.Builder query(String query) {
            this.query = assertNotNull(query, () -> "query can't be null");
            return this;
        }

        public ExecutionInputRest.Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        /**
         * A default one will be assigned, but you can set your own.
         *
         * @param executionId an execution id object
         *
         * @return this builder
         */
        public ExecutionInputRest.Builder executionId(ExecutionId executionId) {
            this.executionId = executionId;
            return this;
        }

        /**
         * Sets the locale to use for this operation
         *
         * @param locale the locale to use
         *
         * @return this builder
         */
        public ExecutionInputRest.Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Sets initial localContext in root data fetchers
         *
         * @param localContext the local context to use
         *
         * @return this builder
         */
        public ExecutionInputRest.Builder localContext(Object localContext) {
            this.localContext = localContext;
            return this;
        }

        public ExecutionInputRest.Builder root(Object root) {
            this.root = root;
            return this;
        }

        /**
         * Adds raw (not coerced) variables
         *
         * @param rawVariables the map of raw variables
         *
         * @return this builder
         */
        public ExecutionInputRest.Builder variables(Map<String, Object> rawVariables) {
            assertNotNull(rawVariables, () -> "variables map can't be null");
            this.rawVariables = RawVariables.of(rawVariables);
            return this;
        }

        public ExecutionInputRest.Builder extensions(Map<String, Object> extensions) {
            this.extensions = assertNotNull(extensions, () -> "extensions map can't be null");
            return this;
        }

        /**
         * You should create new {@link org.dataloader.DataLoaderRegistry}s and new {@link org.dataloader.DataLoader}s for each execution.  Do not
         * re-use
         * instances as this will create unexpected results.
         *
         * @param dataLoaderRegistry a registry of {@link org.dataloader.DataLoader}s
         *
         * @return this builder
         */
        public ExecutionInputRest.Builder dataLoaderRegistry(DataLoaderRegistry dataLoaderRegistry) {
            this.dataLoaderRegistry = assertNotNull(dataLoaderRegistry);
            return this;
        }

        public ExecutionInputRest build() {
            return new ExecutionInputRest(this);
        }
    }
}
