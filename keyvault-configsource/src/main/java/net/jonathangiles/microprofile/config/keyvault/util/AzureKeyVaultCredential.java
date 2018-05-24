package net.jonathangiles.microprofile.config.keyvault.util;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// taken from https://github.com/Microsoft/azure-spring-boot/blob/master/azure-spring-boot/src/main/java/com/microsoft/azure/keyvault/spring/AzureKeyVaultCredential.java
public class AzureKeyVaultCredential extends KeyVaultCredentials {
    private static final long DEFAULT_TOKEN_ACQUIRE_TIMEOUT_IN_SECONDS = 60L;
    private String clientId;
    private String clientKey;
    private long timeoutInSeconds;

    public AzureKeyVaultCredential(String clientId, String clientKey, long timeoutInSeconds) {
        this.clientId = clientId;
        this.clientKey = clientKey;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public AzureKeyVaultCredential(String clientId, String clientKey) {
        this(clientId, clientKey, DEFAULT_TOKEN_ACQUIRE_TIMEOUT_IN_SECONDS);
    }

    @Override
    public String doAuthenticate(String authorization, String resource, String scope) {
        AuthenticationContext context;
        AuthenticationResult result;

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            context = new AuthenticationContext(authorization, false, executorService);
            final ClientCredential credential = new ClientCredential(this.clientId, this.clientKey);

            final Future<AuthenticationResult> future = context.acquireToken(resource, credential, null);
            result = future.get(timeoutInSeconds, TimeUnit.SECONDS);
            return result.getAccessToken();
        } catch (MalformedURLException | TimeoutException | InterruptedException | ExecutionException ex) {
            throw new IllegalStateException("Failed to do authentication.", ex);
        } finally {
            executorService.shutdown();
        }
    }
}