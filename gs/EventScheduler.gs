function doGet(e) {
    return createForm(e.parameter.title);
}

function createForm(title) {
  var form = FormApp.create(title);
  
  var employeeIdValidation = FormApp.createTextValidation().requireWholeNumber().build();
  var emailValidation = FormApp.createTextValidation().requireTextIsEmail().build();
  
  var fullNameField = form.addTextItem();
  fullNameField.setTitle('Your Full Name');
  fullNameField.setRequired(true);
  fullNameField.isRequired();
  
  var emailField = form.addTextItem();
  emailField.setTitle('Your Email Address');
  emailField.setRequired(true);
  emailField.setValidation(emailValidation);
  
  var employeeIdField = form.addTextItem();
  employeeIdField.setTitle('Your Employee ID');
  employeeIdField.setRequired(true);
  employeeIdField.setHelpText("Must contain only a valid employee id, which only contains a valid whole number.");
  employeeIdField.setValidation(employeeIdValidation);
  
  var prefilledFormId = form.addMultipleChoiceItem();
  
  prefilledFormId.setTitle("Code")
  .setChoices([
    prefilledFormId.createChoice(form.getId())
  ])
  .showOtherOption(false)
  .setRequired(true);
  
  var storageFolder = DriveApp.getRootFolder().createFolder(title);
  
  var spreadsheetId = SpreadsheetApp.create(title).getId();
  var file = DriveApp.getFileById(spreadsheetId);
  var newFile = file.makeCopy(title, storageFolder);
  var destinationId = newFile.getId();
  
  form.setDestination(FormApp.DestinationType.SPREADSHEET, file.getId());
 
  
  var destinationSheet = SpreadsheetApp.openById(newFile.getId()).getSheets()[0];
  destinationSheet.appendRow([
    "Timestamp",
    "Full Name",
    "Email Address",
    "Employee ID"
  ]);
  
  var newFileName = title + " Check-ins";
  
  var checkinsSpreadsheetId = SpreadsheetApp.create(newFileName).getId();
  file = DriveApp.getFileById(checkinsSpreadsheetId);
  newFile = file.makeCopy(newFileName, storageFolder);
  
  var checkinSheet = SpreadsheetApp.openById(newFile.getId()).getSheets()[0];
  
  checkinSheet.appendRow([
    "EmployeeID",
    "Latitude",
    "Longitude",
    "Checkin"
  ]);
  

  file.setTrashed(true);
  
  var docLink = "<<Create a DB Spreadsheet on Google Drive, Example File Included: Sheets/FormsDB.xlsx. Include Edit URL Here>>";
  var dbSheet = SpreadsheetApp.openByUrl(docLink).getSheets()[0];
  dbSheet.appendRow([
    form.getId(),
    title,
    newFile.getId(),
    storageFolder.getId(),
    destinationId
  ]);
  
  ScriptApp.newTrigger('onFormSubmit')
  .forForm(form)
  .onFormSubmit()
  .create();
  
  var qrCode = UrlFetchApp.fetch("https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=" + form.getPublishedUrl()).getAs(MimeType.PNG).setName("QRCode.png");
  
  GmailApp.sendEmail("<your_email>", title + ": Registration QR Code", "Hello Organizer,\n\nPlease check your attachment for the QRCode you need to share for people to be able to register.\n\nThank you.", {
                     attachments: [qrCode],
                     name: 'EventScheduler'
  });
  
  return ContentService.createTextOutput(JSON.stringify({
    "status": 200,
    "form": form.getId()
  }));
}

function onFormSubmit(e) {
  
  // Documentation: https://stackoverflow.com/questions/43429161/how-to-get-form-values-in-the-submit-event-handler
  var items = e.response.getItemResponses();

  var name = items[0].getResponse();
  var email = items[1].getResponse();
  var employeeId = items[2].getResponse();
  var formId = items[3].getResponse();
  
  var row = getFormDetails(formId);
  var form = FormApp.openById(formId);
  var spreadsheet = SpreadsheetApp.openById(form.getDestinationId()).getSheets()[0];
  
  var submissions = [];
  
  if (spreadsheet.getLastRow() > 1) {
    submissions = spreadsheet.getRange(2, 1, spreadsheet.getLastRow(), spreadsheet.getLastColumn()).getValues();
  }
  
  var hasErrors = false;
  
  if (submissions.length > 0) {
    var emailRepeatCount = 0;
    var employeeIdRepeatCount = 0;
    
    for (var i = 0; i < submissions.length; i++) {
      var submission = submissions[i];
      
      if (submission[2] == email) {
        emailRepeatCount++; 
      }
      
      if (submission[3] == employeeId) {
        employeeIdRepeatCount++;
      }
    }
    
    if (emailRepeatCount > 1 || employeeIdRepeatCount > 1) {
        hasErrors = true;
        GmailApp.sendEmail(email, row[1] + ": Failed Registration", "Hello,\n\nYour registration failed because you have already registered previously.\n\nThank you.", {
                     name: 'EventScheduler'
        });
    }
  } 
  
  if (!hasErrors) {
    var qrCodeData = encryptQrCode(formId, row[1], employeeId, row[2]);
  
    var qrCode = UrlFetchApp.fetch("https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=" + qrCodeData).getAs(MimeType.PNG).setName("QRCode.png");
  
    var destinationSheet = SpreadsheetApp.openById(row[4]).getSheets()[0];
    
    destinationSheet.appendRow([
      Utilities.formatDate(new Date(), "GMT+8", "MM-dd-yyyy HH:mm:ss"),
      name,
      email,
      employeeId
    ]);
  
    GmailApp.sendEmail(email, row[1] + ": Access Code", "Hello " + name + ",\n\nPlease check your attachment for the QRCode you need to present at the event.\n\nThank you.", {
                       attachments: [qrCode],
                       name: 'EventScheduler'
    });
  }
}

function getFormDetails(formId) {
  var docLink = "<<Create a DB Spreadsheet on Google Drive, Example File Included: Sheets/FormsDB.xlsx. Include Edit URL Here>>";
  var dbSheet = SpreadsheetApp.openByUrl(docLink).getSheets()[0];
  var rows = [];
  
  if (dbSheet.getLastRow() > 1) {
    rows = dbSheet.getRange(2, 1, dbSheet.getLastRow() - 1, dbSheet.getLastColumn()).getValues();
  } else {
    return null;
  }
  
  var rowToReturn = null;
  
  rows.forEach(function (row) {
    if (row[0] == formId) {  
      rowToReturn = row;
    }
  }); 
  
  return rowToReturn;
}

function encryptQrCode(formId, eventName, employeeId, checkinsSpreadsheetId) {
  var dataObject = {
    formId: formId,
    eventName: eventName,
    employeeId: employeeId,
    checkinsSpreadsheetId: checkinsSpreadsheetId
  };
  
  var blob = Utilities.newBlob(JSON.stringify(dataObject));
  return Utilities.base64EncodeWebSafe(blob.getBytes());
}