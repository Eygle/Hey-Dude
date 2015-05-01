Hey Dude !

Johan Cavalec
Thomas Crouzet

> Import Android application in Android Studio

	* Open Android studio
	* File -> Open
	* Browse until you find the android folder and select it
	* Android studio will build it, it could take a few time

To launch the application just plug your android device or launch a Virtual Android Device and press Run (green arrow)
Select your device and click 'OK'


> Install and configure server
	* copy / paste all files from the folder php/srcs inside the root of your apache server folder (www)
	* Replace the ids inside the file db/dbconfig.php to match your MySQL ids and database name
	* Create database 'heydude' and import the SQL file heydude.sql inside that database from phpmyadmin
	* Do not forget to change the 'host' value in the HeyDudeApplication.java class (android\app\src\main\java\com\crouzet\cavalec\heydude)


> Understand the application
	* Home activity is the first activity to be launch. It inherits from GooglePlusSigninActivity in order to implement Google+ Sigin-in button
	* Chat activity is the activity which alllow a user to call and communicate with a friend
	* The database contains the messages in plaintext from former conversations (stored only on the smartphone)
	* gcm contains the class that allow the user to communicate through Google Cloud Messaging application (push)
	* http folder contains classes that allows to communicate with your server where the API is running
	* utils contains generic methods included Crypto methods to be encrypt and decrypt messages and keys

> Understand the communication protocol: (Example Bob call Alice)
	* When Bob call Alice, Alice receive an alert that says Bob is calling her
	* If Alice refuse the call the alert is dismiss and Bob see a notification that warn him of Alice refusal
	* If Alice accept the call the server will send alice RSA public key to Bob
	* Bob will encrypt the AES 256 bits key with Alice and send her the key
	* When alice receives the AES key she will be able to decrypt and encrypt any messages from and to Bob
	* AES key is used to any message encryption and decryption
	* RSA public keys are used to any AES key encrypton
	* RSA private keys are used to any AES key decryption
	* The goal is for Alice and Bob to share the same key (AES) securly.
	* The public RSA keys are send to the the server and store whenever a user login.

> Understand the server API
	* Login: store the user in online_users table and update user token and publicKey. Also create user in users table if he does not exist.
	* Logout: remove user from online_users table.
	* Delete account: remove user from users table
	* Call: add user in calls table until the receiver accept, refuse or timeout the call
	* Hangout: delete user from calls table
	* Answer call: delete user from calls table
	* Any of those request send push notification to the receiver or, in the case of login/logout, to every online users.