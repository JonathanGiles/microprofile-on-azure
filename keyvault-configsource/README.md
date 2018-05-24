# keyvault-configsource
This project demonstrates how to configure a MicroProfile application to retrieve secrets from Azure Key Vault, using 
the MicroProfile Config APIs, to create a client-side data connection (that is, the MicroProfile application will 
directly connect to Azure Key Vault, without any config microservice inbetween).

This sample makes use of [Payara Micro](https://www.payara.fish/payara_micro) and [MicroProfile](https://microprofile.io/) 
to create a tiny Java war file that can be run locally on your machine. It does not 
demonstrate how to dockerise or push the code to Azure, but you may refer to the [docker-helloworld](../docker-helloworld)
project for details on how to do this.

Here's the steps required to run this code on your local machine, starting with creating an Azure Key Vault resource.

### Creating an Azure Key Vault resource
We will use the Azure CLI to create the Key Vault resource and populate it with one secret.

1. Firstly, lets create an Azure service principal. This will provide us with the client ID and key we need to access
Key Vault:

```text
az login
az account set --subscription <subscription_id>

az ad sp create-for-rbac --name <name_to_use_for_service_principal>
```

If I use `microprofile-keyvault-service-principal` for the service principal name, the response from this will be in the 
following, slightly censored, form:

```json
{
  "appId": "5292398e-XXXX-40ce-XXXX-d49fXXXX9e79",
  "displayName": "microprofile-keyvault-service-principal",
  "name": "http://microprofile-keyvault-service-principal",
  "password": "9b217777-XXXX-4954-XXXX-deafXXXX790a",
  "tenant": "72f988bf-XXXX-41af-XXXX-2d7cd011db47"
}
```

Of particular note here is the appID and password - these are what we will use later on as client ID and key, 
respectively.

Now that we have created a service principal, we can create a resource group:

```text
# Create a new resource group (this is optional if you already have one you want to use)
# To get a full list of Azure locations, you can run `az account list-locations`, and select a `name` from that list.
# I personally chose `westus`, and used `jg-test` for the resource group name.
az group create -l <resource_group_location> -n <resource_group_name>
```

We now create a Key Vault resource. Note that the key vault name is what you will use to reference the key vault later,
so choose something memorable.

```text
az keyvault create --name <your_keyvault_name>            \
                   --resource-group <your_resource_group> \
                   --location <location>                  \
                   --enabled-for-deployment true          \
                   --enabled-for-disk-encryption true     \
                   --enabled-for-template-deployment true \
                   --sku standard
```

We also need to grant the appropriate permissions to the service principal we created earlier, so that it may access
the Key Vault secrets. Note that the appID value is the `appId` value from above where we created the service principal
(that is, `5292398e-XXXX-40ce-XXXX-d49fXXXX9e79` - but use the value from your terminal output). 

```text
az keyvault set-policy --name <your_keyvault_name>   \
                       --secret-permission get list  \
                       --spn <your_sp_appId_created_in_step1>
```

We are now at the point where we can push a secret into Key Vault. For the sake of this code base, I (in my infinite ego)
used the key name 'jogiles-key', so that is what I will demonstrate below:

```text
az keyvault secret set --name jogiles-key      \
                       --value jogiles-value   \
                       --vault-name <your_keyvault_name>  
```

That's it! We now have Key Vault running in Azure with a single secret. We can now clone this repo and configure it to
use this resource in our app.

### Getting up and running locally
1. `git clone https://github.com/JonathanGiles/microprofile-on-azure.git`
1. `cd keyvault-configsource`
1. Edit the `pom.xml` file, changing the `payaraMicroAbsolutePath` property to suit your system (I'm
sure there is a better way to handle this, but it isn't clear to me right now).
1. Navigate to `src/main/resources/META-INF/microprofile-config.properties` and change the properties in 
microprofile-config.properties file with details from above.
1. Try running the server using `mvn clean package payara-micro:start`
1. Try accessing [http://localhost:8080/keyvault-configsource/api/config](http://localhost:8080/keyvault-configsource/api/config)
 in your web browser - you should see a simple response that demonstrates values being read from Azure Key Vault.


