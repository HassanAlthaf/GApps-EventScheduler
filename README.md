# GApps-EventScheduler
 A simple yet powerful Event Scheduling tool using Google Apps Suite with Google Apps Script with Android App integration.
 
 - [Installation](#installation)
 
 ## Installation
 1. Download the files.
 2. Goto Google Drive and upload the Excel file provided at: `/gs/sheets/FormDB.xlsx'
 3. Double click the file once it is done uploading on Google Drive and copy the URL from the browser when it opens the file editor.
 4. Create a new project on the [Google Scripts Dashboard.](https://script.google.com/home)
 5. Open any file in the "/gs" folder and copy paste the source code.
 6. Look for "docLink" identifiers within the file and set the value to the link acquired in step 3.
 7. Save the file, then click on Publish -> Deploy as Web App -> **Set Project Version to "New"** -> Access to anyone, anonymous -> Publish. Keep track of the link provided after publishing as we will need this later.
 8. Do the same for all the files in "/gs" folder.
 9. Find `GmailApp.sendEmail("<your_email>",` in the `EventScheduler.gs` file and insert your email.
 10. Open the Android Application on Android Studio.
 11. Update `HomeFragment.java` class with the URLs acquried in step 7. Every place required mentions which URL is required. For example, if the URL for EventScheduler.gs is required, then paste the URL there from Step 7.
 12. Update `CompareStatisticsFragment.java` like in step 11.
 13. Update `StatisticsActivity.java` like in step 11.
 14. Update `ViewEventActivtiy.java` like in step 11.
 15. Build the project and run.
 
 ## Screenshots
 