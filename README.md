# MicroProfile on Azure
A repo containing samples demonstrating MicroProfile samples that can be easily deployed onto Azure.

Currently there are the following samples:

1. [docker-helloworld](https://github.com/JonathanGiles/microprofile-on-azure/tree/master/docker-helloworld) This is perhaps the simplest of all samples that will ever find its way into this repo - it simply demonstrates the ease in which a microprofile-based microservice can be written, containerised, and deployed into Microsoft Azure. This sample makes use of [Payara Micro](https://www.payara.fish/payara_micro) and [Microprofile](https://microprofile.io/) to create a tiny Java war file (5,637 bytes on my machine), and then packages it up into a Docker image (which is 174 megabytes on my machine). This image contains everything necessary for a fully-containerised deployment of this webapp.

1. [keyvault-configsource](https://github.com/JonathanGiles/microprofile-on-azure/tree/master/keyvault-configsource) This project demonstrates how to configure a MicroProfile application to retrieve secrets from Azure Key Vault, using the MicroProfile Config APIs, to create a client-side data connection (that is, the MicroProfile application will directly connect to Azure Key Vault, without any config microservice inbetween).
