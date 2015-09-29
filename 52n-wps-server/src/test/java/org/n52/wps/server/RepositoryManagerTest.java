package org.n52.wps.server;

import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;
import org.mockito.runners.MockitoJUnitRunner;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class RepositoryManagerTest extends AbstractITClass {

    private RepositoryManager repositoryManager;

    @Mock
    private ITransactionalAlgorithmRepository tRepo;

    @Mock
    private IAlgorithmRepository repo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repositoryManager = new RepositoryManagerSeam();
        repositoryManager.setApplicationContext(wac);
        repositoryManager.init();
    }

    @Test
    public void shouldInvokeTransactionalRepository() {
        Object item = new Object();
        repositoryManager.addAlgorithm(item);
        verify(tRepo).addAlgorithm(item);
    }

    private class RepositoryManagerSeam extends RepositoryManager {

        @Override
        protected void loadAllRepositories() {
            addRepository("transactional", tRepo);
            addRepository("non-transactional", repo);
        }

    }

}
