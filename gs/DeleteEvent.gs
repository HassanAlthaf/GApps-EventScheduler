function doGet(e) {
  handle(e);
  
  return ContentService.createTextOutput(JSON.stringify({
    "status": 200
  }));
}

function handle(request) {
  var formId = request.parameter.formId;
  
  var form = FormApp.openById(formId);
  
  /* Deletion of Triggers not Supported: https://issuetracker.google.com/issues/36762427 */
  var triggers = ScriptApp.getUserTriggers(form);
  
  triggers.forEach(function (trigger) {
    try{
      ScriptApp.deleteTrigger(trigger);
    } catch(e) {
      Logger.log("ERROR!");
    }
  });
  
  var docLink = "<<Create a DB Spreadsheet on Google Drive, Example File Included: Sheets/FormsDB.xlsx. Include Edit URL Here>>";
  var dbSheet = SpreadsheetApp.openByUrl(docLink).getSheets()[0];
  
  var rows = [];
  
  if (dbSheet.getLastRow() > 1) {
    rows = dbSheet.getRange(2, 1, dbSheet.getLastRow() - 1, dbSheet.getLastColumn()).getValues();
  } else {
    return; 
  }
  
  var toDelete = null;
  var count = 2;
  
  rows.forEach(function (row) {
    if (row[0] == formId) {  
      toDelete = row;
      
      dbSheet.deleteRow(count);
    }
    
    count++;
  });
  
  if (toDelete == null) {
    return;
  }
  
  DriveApp.removeFile(DriveApp.getFileById(form.getDestinationId()));
  DriveApp.removeFile(DriveApp.getFileById(toDelete[2]));
  DriveApp.removeFile(DriveApp.getFileById(toDelete[4]));
  DriveApp.removeFile(DriveApp.getFileById(toDelete[0]));
  DriveApp.removeFolder(DriveApp.getFolderById(toDelete[3]));
}
