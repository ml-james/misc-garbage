package miscellany.concXecution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkerTest
{
    private static final long DEFAULT_TIMEOUT = Duration.ofSeconds(10).toMillis();

    private TaskFactory factory;
    private Worker worker;

    @BeforeEach
    public void setUp()
    {
        factory = new TaskFactory();
        worker = new Worker();
    }

    @Test
    public void testSuccessful() throws InterruptedException
    {
        List<Runnable> tasks = new ArrayList<>();
        List<Runnable> expectedSuccess = new ArrayList<>();

        expectedSuccess.add(factory.createSuccessfulTask(50));
        expectedSuccess.add(factory.createSuccessfulTask(100));

        tasks.add(expectedSuccess.get(1));
        tasks.add(expectedSuccess.get(0));

        Worker.ExecutedTasks result = worker.execute(tasks, DEFAULT_TIMEOUT);

        assertEquals(expectedSuccess, result.successful);
        assertTrue(result.failed.isEmpty());
        assertTrue(result.timedOut.isEmpty());
    }

    @Test
    public void testFailed() throws InterruptedException
    {
        List<Runnable> tasks = new ArrayList<>();
        Set<Runnable> expectedFailed = new HashSet<>();

        expectedFailed.add(factory.createFailingTask(500));
        expectedFailed.add(factory.createFailingTask(200));

        tasks.addAll(expectedFailed);

        Worker.ExecutedTasks result = worker.execute(tasks, DEFAULT_TIMEOUT);

        assertTrue(result.successful.isEmpty());
        assertEquals(expectedFailed, result.failed);
        assertTrue(result.timedOut.isEmpty());
    }

    @Test
    public void testTimedOut() throws InterruptedException
    {
        List<Runnable> tasks = new ArrayList<>();
        Set<Runnable> expectedTimedOut = new HashSet<>();

        expectedTimedOut.add(factory.createTimeoutTask());
        expectedTimedOut.add(factory.createTimeoutTask());

        tasks.addAll(expectedTimedOut);

        Worker.ExecutedTasks result = worker.execute(tasks, DEFAULT_TIMEOUT);

        assertTrue(result.successful.isEmpty());
        assertTrue(result.failed.isEmpty());
        assertEquals(expectedTimedOut, result.timedOut);
    }

    @Test
    public void testBasic() throws InterruptedException
    {
        List<Runnable> tasks = new ArrayList<>();

        List<Runnable> expectedSuccess = new ArrayList<>();
        expectedSuccess.add(factory.createSuccessfulTask(50));
        expectedSuccess.add(factory.createSuccessfulTask(100));

        tasks.add(expectedSuccess.get(1));
        tasks.add(expectedSuccess.get(0));

        Set<Runnable> expectedFailed = new HashSet<>();
        expectedFailed.add(factory.createFailingTask(200));
        expectedFailed.add(factory.createFailingTask(100));
        tasks.addAll(expectedFailed);

        Set<Runnable> expectedTimedOut = new HashSet<>();
        expectedTimedOut.add(factory.createTimeoutTask());
        expectedTimedOut.add(factory.createTimeoutTask());
        tasks.addAll(expectedTimedOut);

        Worker.ExecutedTasks result = worker.execute(tasks, DEFAULT_TIMEOUT);

        assertEquals(expectedSuccess, result.successful);
        assertEquals(expectedFailed, result.failed);
        assertEquals(expectedTimedOut, result.timedOut);
    }

    @Test
    public void testNoExcessiveWait() throws InterruptedException
    {
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(factory.createSuccessfulTask(50));

        int timeLimit = 700;
        long startTime = System.currentTimeMillis();
        Worker.ExecutedTasks result = worker.execute(tasks, DEFAULT_TIMEOUT);
        long elapsed = System.currentTimeMillis() - startTime;

        assertEquals(tasks, result.successful);
        assertTrue(result.failed.isEmpty());
        assertTrue(result.timedOut.isEmpty());

        assertTrue(elapsed <= timeLimit);
    }
}
