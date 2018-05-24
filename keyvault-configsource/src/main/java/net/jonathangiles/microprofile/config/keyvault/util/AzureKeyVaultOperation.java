package net.jonathangiles.microprofile.config.keyvault.util;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretItem;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AzureKeyVaultOperation {
    private static final long CACHE_REFRESH_INTERVAL_IN_MS = 1800000L; // 30 minutes

    private final KeyVaultClient keyVaultClient;
    private final String vaultUri;

    private final Set<String> knownSecretKeys;
    private final Map<String, String> propertiesMap;

    private final AtomicLong lastUpdateTime = new AtomicLong();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public AzureKeyVaultOperation(KeyVaultClient keyVaultClient, String vaultUri) {
        this.keyVaultClient = keyVaultClient;
        this.propertiesMap = new ConcurrentHashMap<>();
        this.knownSecretKeys = new TreeSet<>();

        vaultUri = vaultUri.trim();
        if (vaultUri.endsWith("/")) {
            vaultUri = vaultUri.substring(0, vaultUri.length() - 1);
        }
        this.vaultUri = vaultUri;

        createOrUpdateHashMap();
    }

    public Set<String> getKeys() {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return propertiesMap.keySet();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Map<String, String> getProperties() {
        checkRefreshTimeOut();

        try {
            rwLock.readLock().lock();
            return Collections.unmodifiableMap(propertiesMap);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public String getValue(String secretName) {
        checkRefreshTimeOut();

//        // NOTE: azure keyvault secret name convention: ^[0-9a-zA-Z-]+$ "." is not allowed
//        final String localSecretName = secretName.replace(".", "-");

        if (knownSecretKeys.contains(secretName)) {
            return propertiesMap.computeIfAbsent(secretName, key -> keyVaultClient.getSecret(vaultUri, key).value());
        }

        return null;
    }

    private void checkRefreshTimeOut() {
        // refresh periodically
        if (System.currentTimeMillis() - lastUpdateTime.get() > CACHE_REFRESH_INTERVAL_IN_MS) {
            lastUpdateTime.set(System.currentTimeMillis());
            createOrUpdateHashMap();
        }
    }

    private void createOrUpdateHashMap() {
        try {
            rwLock.writeLock().lock();
            propertiesMap.clear();
            knownSecretKeys.clear();

            PagedList<SecretItem> knownSecrets = keyVaultClient.listSecrets(vaultUri);
            knownSecrets.loadAll();
//            for (final SecretItem secret : secrets) {
//                propertiesMap.putIfAbsent(secret.id().replaceFirst(vaultUri + "/secrets/", "")
//                        .replaceAll("-", "."), secret.id());
//                propertiesMap.putIfAbsent(secret.id().replaceFirst(vaultUri + "/secrets/", ""), secret.id());
//            }
            knownSecrets.stream()
                    .map(SecretItem::id)
                    .map(s -> s.replaceFirst("(?i)" + vaultUri + "/secrets/", ""))
                    .forEach(knownSecretKeys::add);

            lastUpdateTime.set(System.currentTimeMillis());
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}