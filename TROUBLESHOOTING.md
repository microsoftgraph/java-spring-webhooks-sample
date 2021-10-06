# Troubleshooting

This document covers some of the common issues you may encounter when running this sample.

## You get a 403 Forbidden response when you attempt to create a subscription

Make sure that your app registration includes the required permission for Microsoft Graph (as described in the [Register the app](README.md#register-the-app) section). This permission must be set before you try to create a subscription. Otherwise you'll get an error. Then, make sure a tenant administrator has granted consent to the application.

## You do not receive notifications

If you're using ngrok, you can use the web interface [http://127.0.0.1:4040](http://127.0.0.1:4040) to see whether the notification is being received. If you're not using ngrok, monitor the network traffic using the tools your hosting service provides, or try using ngrok.

If Microsoft Graph is not sending notifications, please open a [Microsoft Q&A](https://docs.microsoft.com/answers/products/graph) issue tagged `MicrosoftGraph`. Include the subscription ID and the time it was created.

## You get a "Subscription validation request timed out" response

This indicates that Microsoft Graph did not receive a validation response within the expected time frame (about 10 seconds).

- Make sure that you are not paused in the debugger when the validation request is received.
- If you're using ngrok, make sure that you used your project's HTTP port for the tunnel (not HTTPS).
