/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.execution.resourcegroups.db;

import io.trino.plugin.resourcegroups.db.H2ResourceGroupsDao;
import io.trino.spi.QueryId;
import io.trino.testing.DistributedQueryRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static io.trino.execution.QueryRunnerUtil.createQuery;
import static io.trino.execution.QueryRunnerUtil.waitForQueryState;
import static io.trino.execution.QueryState.FAILED;
import static io.trino.execution.QueryState.RUNNING;
import static io.trino.execution.resourcegroups.db.H2TestUtil.TEST_ENVIRONMENT;
import static io.trino.execution.resourcegroups.db.H2TestUtil.TEST_ENVIRONMENT_2;
import static io.trino.execution.resourcegroups.db.H2TestUtil.adhocSession;
import static io.trino.execution.resourcegroups.db.H2TestUtil.createQueryRunner;
import static io.trino.execution.resourcegroups.db.H2TestUtil.getDao;
import static io.trino.execution.resourcegroups.db.H2TestUtil.getDbConfigUrl;

public class TestEnvironments
{
    private static final String LONG_LASTING_QUERY = "SELECT COUNT(*) FROM lineitem";

    @Test
    @Timeout(240)
    public void testEnvironment1()
            throws Exception
    {
        String dbConfigUrl = getDbConfigUrl();
        H2ResourceGroupsDao dao = getDao(dbConfigUrl);
        try (DistributedQueryRunner runner = createQueryRunner(dbConfigUrl, dao, TEST_ENVIRONMENT)) {
            QueryId firstQuery = createQuery(runner, adhocSession(), LONG_LASTING_QUERY);
            waitForQueryState(runner, firstQuery, RUNNING);
            QueryId secondQuery = createQuery(runner, adhocSession(), LONG_LASTING_QUERY);
            waitForQueryState(runner, secondQuery, RUNNING);
        }
    }

    @Test
    @Timeout(240)
    public void testEnvironment2()
            throws Exception
    {
        String dbConfigUrl = getDbConfigUrl();
        H2ResourceGroupsDao dao = getDao(dbConfigUrl);
        try (DistributedQueryRunner runner = createQueryRunner(dbConfigUrl, dao, TEST_ENVIRONMENT_2)) {
            QueryId firstQuery = createQuery(runner, adhocSession(), LONG_LASTING_QUERY);
            waitForQueryState(runner, firstQuery, RUNNING);
            QueryId secondQuery = createQuery(runner, adhocSession(), LONG_LASTING_QUERY);
            // there is no queueing in TEST_ENVIRONMENT_2, so the second query should fail right away
            waitForQueryState(runner, secondQuery, FAILED);
        }
    }
}
