# docker-helloworld
This is perhaps the simplest of all samples that will ever find its way into this repo - it simply demonstrates the ease
in which a microprofile-based microservice can be written, containerised, and deployed into Microsoft Azure. 

This sample makes use of [Payara Micro](https://www.payara.fish/payara_micro) and [Microprofile](https://microprofile.io/) 
to create a tiny Java war file (5,637 bytes on my machine), and then packages it up into a Docker image (which is 174 
megabytes on my machine). This image contains everything necessary for a fully-containerised deployment of this webapp.

Here's the steps required to run this code, firstly on your local machine, and secondly as a deployed web app on Azure.

### Getting up and running locally
1. `git clone https://github.com/JonathanGiles/microprofile-on-azure.git`
1. `cd docker-helloworld`
1. Edit the `pom.xml` file, changing the `docker.registry` and `docker.name` properties to suit your needs. These will
eventually need to be changed to match the details of your container registry, but they can be anything whilst testing
locally.
1. `mvn clean package`
1. `docker run -it --rm -p 8080:8080 <docker.registry>/<docker.name>:latest`, for example, 
`docker run -it --rm -p 8080:8080 jogilescr.azurecr.io/samples/docker-helloworld:latest`, if your `docker.registry` is
`jogilescr.azurecr.io` and `docker.name` is `samples/docker-helloworld`.
1. Try accessing [http://localhost:8080/microprofile/api/helloworld](http://localhost:8080/microprofile/api/helloworld) 
and [http://localhost:8080/health](http://localhost:8080/health) in your web browser.

### Creating an Azure Container Registry
Personally, I'm a fan of using the [Azure Portal](http://portal.azure.com) for doing the management tasks, so that is 
what I will cover here. For fans of the Azure CLI, I welcome a pull request to add these instructions here!

1. Log in to the [Azure Portal](http://portal.azure.com) and create a new Azure Container Registry resource. Provide
a registry name (note that this is the name that should be set as the `docker.registry` property in `pom.xml`). Change
the defaults as you wish, and then click 'create'.
1. Once the container registry is live (which is about 30 seconds after clicking 'create'), click on the container 
registry, and click on the 'Access keys' link in the left-menu area. In here, you need to enable the 'admin user'
setting, so that this container registry can be accessed from our machines (to push docker containers into), and also
to enable access from the Azure Web Apps for Containers instance we will setup soon.
1. Whilst you are in the 'Access keys' area, note the `username` and `password` values. We will copy / paste these into
our global Maven `settings.xml` file  (for more information on Maven settings, refer to the 
[Apache Maven Project](https://maven.apache.org/settings.html) website). For reference, here is an obfuscated version of
the `${user.home}/.m2/settings.xml` file on my system:

    ```xml
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">
        <servers>
          <server>
            <id>jogilescr.azurecr.io</id>
            <username>jogilescr</username>
            <password>ojoirshois.this-isn't-real.hrihslirhlishrglih</password>
          </server>
        </servers>
    </settings>
    ```

1. Run `mvn clean package` to clean, compile, and create a local docker image.
1. Run `mvn dockerfile:push` to push to the Azure Container Repository you configured previously.

At this stage you now have your docker container image uploaded to the Azure Container Registry, but it is not yet
running as we now have to deploy it into an Azure Web App for Containers instance.

### Creating an Azure Web App for Containers instance

1. Return to the [Azure Portal](http://portal.azure.com) and create a new Web App for Containers instance (located under
the 'Web + Mobile' heading in the menu). 
   1. The name you specify here will be the public URL of the web app (although a custom domain can be added later if 
   desired), so it is a good idea to pick a name that you can easily remember.
   1. When you get to the 'Configure container' section, you can select 'Azure Container Registry' for the 'Image 
   source', and then select the correct image from the drop-down lists. 
   1. You do not need to specify any value in the 'Startup File' field.
1. Once the instance is created (again, it is very quick), click on it and then click on the 'Application Settings' menu
item. In here you need to add a new application setting, where the key is `WEBSITES_PORT` and the value is `8080`. This
tells Azure which port you want to expose in the container, and it will be mapped to port 80 externally.
1. Optionally, click on the 'Docker Container' link, and enable 'Continuous Deployment', so that whenever you update the
Azure Container Registry image it is automatically updated in the Azure Web App for Containers instance.
1. You should be able to access the Azure-hosted instances at 
`http://<appname>.azurewebsites.net/microprofile/api/helloworld` and `http://<appname>.azurewebsites.net/health`.