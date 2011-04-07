package org.n52.wps.commons.context;
public class ExecutionContextFactory {
    
    private final static ThreadLocal<ExecutionContext> threadContexts =
                new ThreadLocal<ExecutionContext>();
    
    private static ExecutionContext defaultContext;
    
    public static ExecutionContext getContext() {
        return getContext(true);
    }
    
    public static ExecutionContext getContext(boolean fallBackToDefault) {
        ExecutionContext executionContext = null;
        synchronized (threadContexts) {
            executionContext = threadContexts.get();
            if (executionContext == null && fallBackToDefault) {
                executionContext = getDefault();
            }
        }
        return executionContext;
    }
    
    public synchronized static ExecutionContext getDefault() {
        if (defaultContext == null) {
            defaultContext = new ExecutionContext();
        }
        return defaultContext;
    }
    
    public static void registerContext(ExecutionContext context) {
        System.out.println("Context registered");
    	synchronized (threadContexts) {
            threadContexts.set(context);
        }
    }
    
    public static void unregisterContext() {
        synchronized (threadContexts) {
            threadContexts.remove();
        }
    }    
}


