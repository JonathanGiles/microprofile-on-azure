package net.jonathangiles.microprofile.config.keyvault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import net.jonathangiles.microprofile.config.keyvault.util.AzureKeyVaultCredential;
import net.jonathangiles.microprofile.config.keyvault.util.AzureKeyVaultOperation;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.*;

public class AzureKeyVaultConfigSource implements ConfigSource {

    private AzureKeyVaultOperation keyVaultOperation;

    public AzureKeyVaultConfigSource() {
        // no-op
    }

    private void init() {
        if (keyVaultOperation != null) return;

        // read in keyvault config settings from external config source (normally the environment or a microprofile-config.properties file)
        Config config = ConfigProvider.getConfig();
        String keyvaultClientID = config.getValue("azure.keyvault.client.id", String.class);
        String keyvaultClientKey = config.getValue("azure.keyvault.client.key", String.class);
        String keyvaultURL = config.getValue("azure.keyvault.url", String.class);

        // create the keyvault client
        KeyVaultCredentials credentials = new AzureKeyVaultCredential(keyvaultClientID, keyvaultClientKey);
        KeyVaultClient keyVaultClient = new KeyVaultClient(credentials);
        keyVaultOperation = new AzureKeyVaultOperation(keyVaultClient, keyvaultURL);
    }

    @Override
    public Map<String, String> getProperties() {
        init();
        return keyVaultOperation.getProperties();
    }

    @Override
    public String getValue(String key) {
        init();
        return keyVaultOperation.getValue(key);
    }

    @Override
    public String getName() {
        return "AzureKeyVaultConfigSource";
    }

    @Override
    public Set<String> getPropertyNames() {
        init();
        return keyVaultOperation.getKeys();
    }

    @Override
    public int getOrdinal() {
        return 90;
    }
}
