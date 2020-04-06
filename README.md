AWS Lambda for pushing SES e-mails to HTTP/S API
==================================================
This AWS Lambda handles e-mails which are received by Amazon's _Simple E-Mail
Service_ (AWS SES) to any HTTP or HTTPS API.

How it works
--------------
① AWS SES receives an e-mail over SMTP.
② AWS SES stores the content of this e-mail in an AWS S3 bucket ...
③ ... and pushes a notification to AWS SNS, which contains a reference to this bucket.
④ The AWS Lambda is configured to listen on this SNS queue and starts handling this e-mail.
⑤ It therefore loads the e-mail content from the S3 bucket.
⑥ ... and pushes this e-mail message to the configured possibly external HTTP(S) (REST) API.
⑦ If successful, the e-mail is removed from AWS S3.

![Overview](/doc/overview.jpg)

Side note: One might think, you could also do this without Lambda by using SNS
only, because SNS already provides this functionality to push notifications to
a HTTP API of your choice. Well, that's right for mails with a maximum size of
150kB. Unfortunately, mails are often larger than that.

How to use
------------
Log in to the AWS console and go to Simple Email Service (SES). Configure your
domains for receiving e-mails. Be aware that not all AWS regions support inbound
e-mails. Choose one, where the _Email Receiving_ menu section is not greyed out.

Go to _Email Receiving_ —> _Rule Sets_ and create a new rule. Select a recipient
filter which can be simply your full domain if you want to match all mails of
your domain. In the next step select `S3` as action type. Let AWS create a new
S3 bucket and also create and select a new SNS topic.

Switch to AWS _Lambda_ service and create a new function. Choose _Author from
scratch_, give it a name and choose _Java 11_ as runtime. Add a trigger, choose
_SNS_ and your previously created SNS queue.

Build this Lambda function by running `mvn clean package` locally on your
machine. Upload the created `target/aws-lambda-....jar` file to your previously
created AWS lambda and reference `benhub.aws.sestohttp.LambdaRequestHandler` in
the `Handler` text input field.

Configure the environment variables (see below) and you're done. Your lambda now
pushes mails to your HTTP API.

Environment variables
-----------------------
 * `AWS_S3_REGION` ... The AWS region where your e-mails are stored.
 * `AWS_S3_ACCESS_KEY` ... The access key to access your S3 bucket.
 * `AWS_S3_SECRET_KEY` ... The secret key to access your S3 bucket.
 * `API_URL` ... The URL where the e-mails should be pushed to. Example:
     `https://foo.example.com/api/${to.localpart}?auth_key=asdf1234`
     This URL may contain the following placeholders:
      * `${to.localpart}` ... The localpart of the `To` header in the received mail.
      * `${to.domain}` ... The domain of the `To` header in the received mail.
 * `API_RESPONSE_EXPECTED` ... The expected response code which is returned by
     the API if the POST to the endpoint was successful. Example: 200
 * `API_RESPONSE_REJECTED` ... A specific response code which is returned by the
     API, in case the given message is permanently rejected. Example: 406
 * `API_CLIENT_CONNECT_TIMEOUT_MILLIS` ... The connect timeout for the HTTP
     request. Example: 2000
 * `API_CLIENT_SEND_TIMEOUT_MILLIS` ... The send timeout for the HTTP request.
     Example: 30000
 * `API_HEADER_*` ... Any configured environment variable starting with
     `API_HEADER_` will be passed with the request as HTTP header. Underscores
     will be replaced with dashes (`-`).

License
---------
Copyright 2020 Benjamin Hubert

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
