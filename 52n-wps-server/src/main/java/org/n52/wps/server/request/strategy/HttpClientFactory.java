package org.n52.wps.server.request.strategy;

import java.util.concurrent.TimeUnit;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class HttpClientFactory {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientFactory.class);
    
    // Connection pool setup
    private final static int CONNECTION_TTL = 15 * 60 * 1000;       // 15 minutes, default is infinte
    private final static int CONNECTIONS_MAX_TOTAL = 256;
    private final static int CONNECTIONS_MAX_ROUTE = 32;
    
    // Connection timeouts
    private final static int CLIENT_SOCKET_TIMEOUT = 5 * 60 * 1000; // 5 minutes, default is infinite
    private final static int CLIENT_CONNECTION_TIMEOUT = 15 * 1000; // 15 seconds, default is infinte
    
    // Cache setup
    private final static boolean CACHING_ENABLED = true;
    private final static int CACHING_MAX_ENTRIES = 128;
    private final static int CACHING_MAX_RESPONSE_SIZE = 32767;
    private final static boolean CACHING_HEURISTIC_ENABLED = true; // behaves per RFC 2616
    private final static long CACHIN_HEURITIC_DEFAULT_LIFETIME_SECONDS = 300;  // 5 minutes

    
    private static ClientConnectionManager createConnectionManager() {
        PoolingClientConnectionManager clientConnectionManager = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), CONNECTION_TTL, TimeUnit.MILLISECONDS);
        clientConnectionManager.setMaxTotal(CONNECTIONS_MAX_TOTAL);
        clientConnectionManager.setDefaultMaxPerRoute(CONNECTIONS_MAX_ROUTE);
        LOGGER.info("Created HTTP client connection manager: maximum connections total = {}, maximum connections per route = {}",
            clientConnectionManager.getMaxTotal(),
            clientConnectionManager.getDefaultMaxPerRoute());
        return clientConnectionManager;
    }
    
    private static HttpClientFactory instance;
    public synchronized static HttpClientFactory getDefault() {
        if (instance == null) {
            instance = new HttpClientFactory(createConnectionManager());
        }
        return instance;
    }
    
    public static HttpClient getDefaultClient() {
        return getDefault().getClient();
    }
    
    public static ClientConnectionManager getDefaultClientConnectionManager() {
        return getDefault().getClientConnectionManager();
    }
    
    
    private ClientConnectionManager clientConnectionManager;
    private HttpCacheStorage cacheStorage;
    private CacheConfig cacheConfig;
    private HttpClient client;
    
    private HttpClientFactory(ClientConnectionManager connectionManager) {
        
        // If enabled setup a memory/heap cache for server responses.  If we
        // want we could setup a layered L1/L2/... cache scheme by wrapping
        // CachingHttpClient instances with appropriate storage (i.e. small cache
        // in heap storage, larger with file storage, etc..) 
        if (CACHING_ENABLED) {
            cacheConfig = new CacheConfig();  
            cacheConfig.setMaxCacheEntries(CACHING_MAX_ENTRIES);
            cacheConfig.setMaxObjectSize(CACHING_MAX_RESPONSE_SIZE);
            cacheConfig.setHeuristicCachingEnabled(CACHING_HEURISTIC_ENABLED);
            cacheConfig.setHeuristicDefaultLifetime(CACHIN_HEURITIC_DEFAULT_LIFETIME_SECONDS);
            cacheConfig.setSharedCache(true);  // won't cache authorized responses
            cacheStorage = new BasicHttpCacheStorage(cacheConfig);
            LOGGER.info("HTTP Response caching enabled: maximum cache entries = {}, maximum response size = {} bytes, heuristic caching enabled = {}, heuristic default lifetime = {} s",
                    new Object[] {
                        cacheConfig.getMaxCacheEntries(),
                        cacheConfig.getMaxObjectSize(),
                        cacheConfig.isHeuristicCachingEnabled(),
                        cacheConfig.getHeuristicDefaultLifetime(),
                    });
        } else {
            LOGGER.info("HTTP Response caching disabled");
        }
        
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(httpParams, CLIENT_SOCKET_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(httpParams, CLIENT_CONNECTION_TIMEOUT);
        DefaultHttpClient defaultClient = new DefaultHttpClient(clientConnectionManager, httpParams);
        this.client = CACHING_ENABLED ? new CachingHttpClient(defaultClient, cacheStorage, cacheConfig) : defaultClient;
    }
    
    public HttpClient getClient() {
        return client;
    }
    
    public ClientConnectionManager getClientConnectionManager() {
        return clientConnectionManager;
    }
}
