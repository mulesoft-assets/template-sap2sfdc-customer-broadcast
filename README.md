
# Anypoint Template: SAP to Salesforce Customer Broadcast

Broadcast changes or creating of customers in SAP as accounts to Salesforce in real time. The detection criteria, and fields to move are configurable. Additional systems can be added to be notified of the changes. 

Real time synchronization is achieved via rapid polling of SAP. This template uses Mule batching and watermarking capabilities to capture only recent changes, and to efficiently process large amounts of records.

![07e01cbf-2396-41ce-b066-58d35bc95d4f-image.png](https://exchange2-file-upload-service-kprod.s3.us-east-1.amazonaws.com:443/07e01cbf-2396-41ce-b066-58d35bc95d4f-image.png)

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
This template should serve as a foundation for setting an online sync of customers from SAP to Salesforce.
Every time there is a new Customer (SFDC Account) or a change in an already existing one, SAP sends the IDoc with it to the running template which updates or creates an account in the Salesforce target instance.

Requirements have been set not only to be used as examples, but also to establish a starting point to adapt your integration to your requirements.

As implemented, this template leverages the Batch Module. The batch job is divided in *Process* and *On Complete* stages.

The integration is triggered by an SAP Endpoint that receives a SAP Customer as IDoc XML. This XML is passed to the batch process.
In the *Process* stage the SAP Customer is transformed to a Salesforce Account and then upserted to Salesforce in the Batch Step using a Batch Aggregator.
Finally during the *On Complete* stage the template logs output statistics data into the console.

# Considerations

To make this template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source (SAP) and destination (SFDC) systems, that must be made for the template to run smoothly. Failing to do so can lead to unexpected behavior of the template.

Before you continue with the use of this template, you may want to check out this [Documentation Page](https://docs.mulesoft.com/connectors/sap/sap-connector), that teaches you how to work with SAP and Anypoint Studio.

## Disclaimer

This template uses a few private Maven dependencies from Mulesoft to work. If you intend to run this template with Maven support, you need to add extra dependencies for SAP to the pom.xml file.


## SAP Considerations

Here's what you need to know to get this template to work with SAP.

### As a Data Source

The SAP backend system is used as a source of data. The SAP connector is used to send and receive the data from the SAP backend. 
The connector can either use RFC calls of BAPI functions and/or IDoc messages for data exchange, and needs to be properly customized per the "Properties to Configure" section.

The Partner profile needs to have a customized type of logical system set as partner type. An outbound parameter of message type DEBMAS should be defined in the partner profile. A RFC destination created earlier should be defined as Receiver Port. IDOC Type base type should be set as DEBMAS01.

## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>


### As a Data Destination

This template makes use of the `External ID` field offered by Salesforce. Here is a short description on how SFDC define external ID's 

+ [What is an external ID?](http://help.salesforce.com/apex/HTViewHelpDoc?id=faq_import_general_what_is_an_external.htm)

The templates uses the External ID in order to do xRef between the entities in both systems. The idea is, once an entity is created in Salesforce it's decorated with an ID from the source system which is used afterwards for the template to reference it.

Create a new custom field in your **Account** entity in Salesforce with the following name: 

+ `sap_external_id`

For instructions on how to create a custom field in Salesforce please check this link:

+ [Create Custom Fields](https://help.salesforce.com/HTViewHelpDoc?id=adding_fields.htm)

# Run it!
Simple steps to get SAP to Salesforce Customer Broadcast running.


## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`.
+ Click `Mule Application (configure)`.
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
+ Click `Run`.
To make this template run on Studio there are a few extra steps that needs to be made.
Check this Documentation Page: [Enabling Your Studio Project for SAP](https://docs.mulesoft.com/connectors/sap-connector#configuring-the-connector-in-studio-7).

### Running on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 


## Running on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.


### Deploying your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.

### Application Configuration
**Batch Aggregator Configuration**
+ page.size `100`
		
**SAP Connector Configuration**

+ sap.jco.ashost `your.sap.address.com`
+ sap.jco.user `SAP_USER`
+ sap.jco.passwd `SAP_PASS`
+ sap.jco.sysnr `14`
+ sap.jco.client `800`
+ sap.jco.lang `EN`

**SAP Endpoint Configuration**
+ sap.jco.operationTimeout `0`
+ sap.jco.connectioncount `2`
+ sap.jco.gwhost `your.sap.addres.com`
+ sap.jco.gwservice `sapgw14`
+ sap.jco.idoc.programid `PROGRAM_ID`

**SalesForce Connector Configuration**

+ sfdc.username `bob.dylan@sfdc`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`

# API Calls
Salesforce imposes limits on the number of API calls that can be made. Therefore calculating this amount may be an important factor to consider. Customer Broadcast Template calls to the API can be calculated using the formula:

**X / ${page.size}**

X is the number of customers to be synchronized on each run.

Divide by ${page.size} because by default, customers are gathered in groups of ${page.size} for each Upsert API Call in the aggregation step. Also consider that this calls are executed repeatedly every polling cycle.

For instance if 10 records are fetched from origin instance, then 1 API call to Salesforce is made (1).

# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml


## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.

## businessLogic.xml
Functional aspect of the template is implemented on this XML, directed by a batch job that's responsible for creations or updates. The several message processors constitute three high level actions that fully implement the logic of this template:
1. The integration is triggered by a Document Source that receives a SAP Customer as IDoc XML. This XML is passed to the batch process.
2. In the *Process* stage the SAP Customer is transformed to a Salesforce Account and then upserted to Salesforce in the Batch Step using a Batch Aggregator.
3. Finally during the *On Complete* stage the template logs output statistics data into the console.

## endpoints.xml
This file is formed by a flow containing the endpoints for triggering the template and for retrieving the objects that meet the defined criteria in the query. You can then execute the batch job process with the query results.

## errorHandling.xml
This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.
