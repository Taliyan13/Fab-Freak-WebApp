# Fab Freak
## IOT application
Android app using Java and Android Studio that allows users to upload photos of their clothing for inspiration and save inspirations from other users in the network.

## Installation
This project was written in JAVA and developed in Android studio, for back-end development I used Firebase databases and OOP.  
Using toast msg, animations, OOP, XML, event handling. Includes 8 activities and intents. 

To install the project, be sure you have firebase connected.

## Description of the system scenarios - actions of the system user
• Moving between different categories and accumulating ideas for the next.
• Save selected inspirations for later use.
• Upload inspiration that includes description of items and photos.

## Usage - pages:
1. LoadingActivity - The app loading screen.
<img width="219" alt="image" src="https://user-images.githubusercontent.com/87084078/216334306-dd0a7b81-3a2e-47be-ae76-6a16a9467090.png">

2. mainActivity - The main screen of the application where 4 different categories will be displayed, each of which shows a different area of clothing - work, studies, recreation, event.
  Both registered and unregistered users will be able to view this screen, but when clicking on one of the categories, a transition to the desired page will be made
But for registered users, while for those who are not registered, a toast message will pop up that in order to be able to watch the desired content, they must register for the application and they will be returned to the login page (SystemLoginActivity).
<img width="151" alt="image" src="https://user-images.githubusercontent.com/87084078/216334983-9fccf549-7368-4398-858a-cdd9282833d8.png">

3. SystemLoginActivity - On the system login page, the user must enter an email and password. When he enters the relevant details, a check will be made against the database that does exist in the specified email system linked to that password.
If it does not exist in the system, a toast message will be returned alerting you to this.
• When the user clicks on the out Log button on the toolbar, it will send back to the systemLoginActivity page.
<img width="140" alt="image" src="https://user-images.githubusercontent.com/87084078/216335310-93d4cd1d-facb-4056-aece-de30ae4d5516.png">

4. SignUpActivity - Login page to the system for the first time, the user must enter the fields - name, password, password verification, email address.
The details will be transferred to the database for checking that the email address does not exist in the system, and that the password does meet the requested conditions - a 4-character password that includes all numbers and letters and at least one special character.
<img width="144" alt="image" src="https://user-images.githubusercontent.com/87084078/216337557-8a3fe25b-393f-4298-a52a-237ff636a336.png">

5. NewLookActivity - Each user will be able to put together a look with items from his personal closet and upload
A number of images with a name description of the combinations he has chosen. On this page, the user will insert the images
desired from his gallery album, he will select an icon image that will be displayed on the selected category pages and select the desired category field in which he will enter the look.
After he fills in all the requested fields, this recommendation will be uploaded to the designated category and additional users will be exposed to it.
<img width="431" alt="image" src="https://user-images.githubusercontent.com/87084078/216337622-8bea9b75-eb3b-46d8-99d7-b7cb3c023167.png">

6. ClickOnCategoryActivity - A page that is created dynamically each time, which takes the name of the selected category and associates with it all the details that are under that category name in the database, meaning that the user will be presented with all the inspirations that users have uploaded to that category.
<img width="151" alt="image" src="https://user-images.githubusercontent.com/87084078/216338319-485fdf5f-ef0e-4da8-aaf3-723345fe7513.png">


7. MyUploadsActivity - A page that exists for every registered user, containing all the looks he has uploaded to the applicationץ
<img width="161" alt="image" src="https://user-images.githubusercontent.com/87084078/216338459-5bc4ac0b-85ca-4089-872a-b45bd28c4099.png">

8. DisplaySelectedLookActivity - We will go to this page when a user clicks on a selected look from a desired category.
<img width="163" alt="image" src="https://user-images.githubusercontent.com/87084078/216338641-025efa25-26d9-4930-a597-002b049b7af4.png">


## Contributing

Instructions for upload photos - 
The photos you want to upload will be pulled from the "gallery" folder on your deviceץ
<img width="633" alt="image" src="https://user-images.githubusercontent.com/87084078/216337761-3376684d-9f9e-43a7-a67a-ba076e3fd647.png">

## DB - Firebase : 
every insperation will save in Storage -
<img width="494" alt="image" src="https://user-images.githubusercontent.com/87084078/216339128-d9d3fa37-019f-43c3-9f4f-49decba3b0c7.png">

and in RealTime dashboard - 
<img width="359" alt="image" src="https://user-images.githubusercontent.com/87084078/216339285-48f2a19f-9987-45c7-8f05-0a5934dc78a3.png">



