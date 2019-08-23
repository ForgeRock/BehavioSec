<!--
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2019 ForgeRock AS.
-->
# BehavioSec - Continues Authentication

The [BehavioSec platform][behaviosec_platform] provides a continuous and transparent sensory capability that helps verify that people online are who they say they are. It analyzes users’ keystrokes, cursor movements, screen pressure, and device handling to continuously authenticate users based on their innate behaviors in real time.
The system is invisible to end users. End users interact with the app in their normal ways, and their normal usage patterns are gathered.

In the background on the client app, the [BehavioSec SDK][behaviosec_platform]  collects data on how a user interacts with the app. A proxy mechanism on the backend passes the data to the BehavioSense Server, a machine learning framework, that then creates a user-specific Profile. Once a Profile is established, BehavioSense compares real-time data with previously saved data in the Profile. It analyzes the data to get an assessment of the similarity between the old and the new behavior. In this way BehavioSec can identify whether the user is the expected user. This analysis is reflected in the Behavioral Score, along with other results.

The platform can also identify behavioral patterns that may indicate a bot or RAT or other possibly malicious activity. In addition, it can capture non-behavioral descriptive data, such as IP addresses and mobile platform details, for additional types of analysis.

You can use the BehavioSec platform with any type of app where an end user inputs text, interacts with touch surfaces, or uses a mouse. An SDK captures the end user’s keystroke or pressure, swipes, etc. The system provides continuous authentication when multiple input fields and buttons throughout an app are instrumented.

## Using BehavioSense for Continuous Authentication

Conventional approaches to security bring a need to choose between robust protections and streamlined user experience, and now there is a shift from these legacy systems to layered, adaptive approaches. BehavioSense is a layer in that security process that can authenticate users based on their own behaviors, using sophisticated data collection, //without interrupting the user experience//. BehavioSense provides passive verification and makes it more difficult for bad actors to mimic or compromise the security of the interaction.

For example, when an app's user types in various fields or clicks on various buttons, BehavioSense creates a Behavioral Score that indicates how close the user's behavioral patterns are to previously stored patterns. If BehavioSense indicates a low similarity score, an integrated application or third party security system can respond to that potential risk. If the low score is triggered on a login, for example, the system can respond with a step-up in authentication. The step-up can be a one-time password, security question, static biometric authenticator, or other threat-appropriate security measure defined by the customer's security infrastructure. Even after login, BehavioSense can analyze activity in other fields and buttons during a user session. Continuous authentication provides additional security when there is a risk of a device being compromised mid-session.

## Using BehavioSense for Fraud Detection and Analysis

BehavioSense is part of a multi-layered fraud analysis system. It adds dynamic behavioral biometrics to provide continuous user authentication. BehavioSense gathers and analyzes behavioral data and provides a Behavioral Score, among other metrics. The Behavioral Score indicates whether the current user is the same as the expected user. For more information about the metrics, see [[scoring_and_metrics|Understanding Behavioral Scores and Metrics]].

In a conventional enterprise security system, the volume of alerts makes it difficult to distinguish false alarms from real risks. BehavioSense helps security teams to prioritize threats and improve efficiency in identifying breaches such as account takeovers, bots, or troll and spam accounts.

In the case of a fraudulent session, the system analyzes and identifies the characteristics of the fraud or breach type. A security team can see both an audit trail and fraud breakdown in the BehavioSense Dashboard Risk Flags, and/or use the BehavioSense REST API to pull the data into another security system component. 

Flags can include:
  * A change in IP address
  * A change in device during a session
  * A Remote Access Trojan (RAT) in the browser
  * Bot activity
  * Replay attacks
  * Etc. (See **[[risk_flags|Risk Flags]]** for complete listing.)

### More information
Please familiarize yourself with [BehavioSec SDK][behaviosec_platform] and ask customer Support for deep dive into the flag and configuration options.

# Installation

Please contact sales representative sales-xxx@forgerock.com
## Market space


## Binary
Download latest release from xxxxx and copy file to the ../web-container/webapps/openam/WEB-INF/lib directory where AM is deployed.  Restart the web container to pick up the new node.  The node will then appear in the authentication trees components palette.

The code in this repository has binary dependencies that live in the ForgeRock maven repository. Maven can be configured to authenticate to this repository by following the following [ForgeRock Knowledge Base Article](https://backstage.forgerock.com/knowledge/kb/article/a74096897).


#Configuration
The following sections provides information about configuring Behaviosense tree. 

BehavioSec API backend return JSON response that has been integrated to handle outcome in ForgeRock modules. To start
 behavior metrics authentication follow the necessary steps:

* Make sure to obtain URL to the BehavioSense API endpoint, dashboard URL, and get access to developer portal
* Create a user with email address and real password (Note 1)
* Combine the components as show in Authentication Tree
* Save
* Navigate to login page with the tree URL + `1#login&service=BehavioSec`
* Login in with the user
* Verify with BehavioSense dashboard recorded session. 


Note 1: Behaviosec machine learning is developed on real scenarios, therefore it is highly recommended to use both for
 the user name and password longer than 8 characters. 

## On profile training

## Authentication Tree 
Behaviosec provides all the necessary components to use [BehavioSec platform][behaviosec_platform] platform out the box. 


A sample of the authentication tree is shown below. Details for component configuration are in the following sections. Naturally, **Failure** outcome should result in authentication step up, retry, or even account lock out.

![ScreenShot](behaviosec-authentication-tree-basic-example.png)


## BehaviosecCollector
This is a data collector node that you need to place under the page node. In the configuration you have an option to add a different collector script if needed.


![ScreenShot](behaviosec_collector_node.png)

## BehaviosecAuthNode
This node receives the collected data and communicates with the server. You have an option to fail authentication if connection to BehavioSense can not be established.
The option **Fail if no connection** allows node evaluation to true even if connection to behaviosense was not
 established.
 
![ScreenShot](behaviosec-auth-node.png)


## BehavioSecScoreEvaluator
This Score evaluation module allows you to specify BehavioSense Score, Confidence, and Risk levels. 
Anything below the specified values will fail. It also allows you to control the outcome for users whose Profiles are still in the training phase.

* **Behavioral Score** or Score is a numerical value ranging from 0 to 100, that indicates to what
 degree the timing data in the session matches the timing data in the trained profile. A high Behavioral Score means
  there is little difference between the behavior in the session and the user’s profile. Read more about scoring and
   metrics [here][https://developer.behaviosec.com/dw/scoring_and_metrics].

* **Confidence** value represents the quantity of data that we have stored in a profile and is available to check
 against the user. The higher the Confidence value is, the more data that we have to check against the behavior
  presented in a given session. Read more about the Confidence value [here][https://developer.behaviosec.com/dw/scoring_and_metrics#confidence].
  
* **Risk** value is a numerical measure of potentially fraudulent activity during the course of a session. It can be a
 number greater than or equal to zero. A Risk value in the range of 0-100 is considered minimal risk, while over 100
  is high risk and should be investigated for fraud. Read more about the Risk value [here][https://developer.behaviosec.com/dw/scoring_and_metrics].

* **Allow In Training** is indicator that the user profile is still being trained. If enabled, the score and risk
 will be ignored and the node will evaluate to true. 


![ScreenShot](behaviosec-score-evaluator.png)


## BehavioSecBooleanEvaluator
The Boolean evaluator controls the outcome for flags returned by the BehavioSense module. It will fail on any
 condition evaluating to false.

### Boolean Flag configuration
* **BOT Detection**	- Indicates that robotic behavior was detected such as a typing rhythm that is too uniform or
 jittery mouse movements. This information is received from the isBot flag in the JSON. **Allow Bot** enabled evaluates to true outcome even if bot is detected. Default is **false**.
* **Replay Attack**	Indicates that the exact same behavioral data has been received in the past. This information is
 received from the isReplay flag. **Allow Replay** enabled evaluates to true outcome even if replay is detected. Default is **false**.
* **Allow In Training**	
* **Remote Access**	Indicates that one or more remote access protocols were detected in the session. If remote access
 has been flagged, you'll be able to see a breakdown of software using the detected protocols by looking at the
  ratProtocol parameter. **Allow Remote Access** enabled evaluates to true outcome even if remote access protocol is
   detected. Default is **true**.
* **Tab Anomaly** Indicates the user displays inconsistent tabbing behavior. This information is received from the
 tabAnomaly flag in the JSON. **Allow Tab Anomaly** enabled evaluates to true outcome even if tab anomaly is detected
 . Default is **true**.
* **Numpad Anomaly** Indicates that the user displays inconsistent numeric keypad behavior. This information is
 received from the numpadAnomaly flag in the JSON.  **Allow Numpad Anomaly** enabled evaluates to true outcome even if
  numpad anomaly is detected. Default is **true**.
* **Device Changed** Indicates the device/user agent string has changed during the active session. When a new device
 type is detected (e.g., Desktop, Android, or iOS device), this flag is set to true. This information is received
  from the deviceChanged flag in the JSON.  **Allow Device Change** enabled evaluates to true outcome even if device
   change is detected. Default is **true**.


For full list of available flags please visit: https://developer.behaviosec.com/dw/risk_flags

![ScreenShot](behaviosec-boolean-evaluator.png)


# Disclaimer
        
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. BehavioSec does not warrant or guarantee the individual success developers may have in implementing the sample code on their development platforms or in production configurations.

BehavioSec does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the sample code. BehavioSec disclaims all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

BehavioSec shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the sample code.

[forgerock_platform]: https://www.forgerock.com/platform/  
[behaviosec_platform]: https://www.behaviosec.com/  
