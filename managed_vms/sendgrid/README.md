# Java SendGrid email sample for Google Managed VMs
This sample demonstrates how to use [SendGrid](https://www.sendgrid.com) on Google Managed VMs
For more information about SendGrid, see their [documentation](https://sendgrid.com/docs/User_Guide/index.html).
## Setup
Before you can run or deploy the sample, you will need to do the following:
1. [Create a SendGrid Account](http://sendgrid.com/partner/google). As of September 2015, Google users start with 25,000 free emails per month.
1. Configure your SendGrid settings in the environment variables section in ``src/main/appengine/app.yaml``.
## Running locally
You can run the application locally and send emails from your local machine. You
will need to set environment variables before starting your application:
    $ export SENDGRID_API_KEY=[your-sendgrid-api-key]
    $ export SENDGRID_SENDER=[your-sendgrid-sender-email-address]
    $ mvn clean jetty:run
