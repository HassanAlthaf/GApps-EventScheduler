function doGet(e) {
  if (e.parameter.formId !== undefined) {
    return handle(e);
  }
  
  if (e.parameter.forms !== undefined) {
    return handleMultipleForms(e); 
  }
  
 
  return ContentService.createTextOutput(JSON.stringify({
    "status": 403,
    "error": "Permission Denied"
  })); 
  
}

function getFormRelatedDocuments(formId) {
  var docLink = "<<Create a DB Spreadsheet on Google Drive, Example File Included: Sheets/FormsDB.xlsx. Include Edit URL Here>>";
  var dbSheet = SpreadsheetApp.openByUrl(docLink).getSheets()[0];
  
  var rows = [];
  var toReturn = null;
  
  if (dbSheet.getLastRow() > 1) {
    rows = dbSheet.getRange(2, 1, dbSheet.getLastRow() - 1, dbSheet.getLastColumn()).getValues();
  } else {
    return toReturn;
  }
  
  rows.forEach(function (row) {
    if (row[0] == formId) {  
      toReturn = row;
    }
  });
  
  return toReturn;
}

function handleMultipleForms(request) {
  var forms = JSON.parse(request.parameter.forms);
  
  var data = [];
  
  for (var i = 0; i < forms.length; i++) {
    var formId = forms[i];
    
    var formDocuments = getFormRelatedDocuments(formId);
    
    if (formDocuments == null) {
      return ContentService.createTextOutput(JSON.stringify({
        "status": 404,
        "error": "We failed to load the Graph because a certain requested event was not found."
      })); 
    }
    
    data.push({
      registrations: getRegistrationCount(formDocuments[4]),
      checkins: getCheckinsCount(formDocuments[2]),
      title: formDocuments[1]
    });
  }
  
  return ContentService.createTextOutput(JSON.stringify({
    "status": 200,
    "data": data
  }));
}

function handle(request) {
  var formId = request.parameter.formId;
  
  var formDocuments = getFormRelatedDocuments(formId);
  
  if (formDocuments == null) {
    return ContentService.createTextOutput(JSON.stringify({
      "status": 404,
      "error": "Requested Event does not exist"
    })); 
  }
  
  return ContentService.createTextOutput(JSON.stringify({
    "status": 200,
    "data": {
      registrations: getRegistrationCount(formDocuments[4]),
      checkins: getCheckinsCount(formDocuments[2])
    }
  }));
}

function getRegistrationCount(destinationId) {
  var sheet = SpreadsheetApp.openById(destinationId).getSheets()[0];
  
  var rows = [];
  
  if (sheet.getLastRow() > 1) {
    rows = sheet.getRange(2, 1, sheet.getLastRow() - 1, sheet.getLastColumn()).getValues();
  }
  
  return rows.length;
}
                                         
function getCheckinsCount(checkinsSheet) {
  var sheet = SpreadsheetApp.openById(checkinsSheet).getSheets()[0];
  
  var rows = [];
  var filtered = [];
  
  if (sheet.getLastRow() > 1) {
    rows = sheet.getRange(2, 1, sheet.getLastRow() - 1, sheet.getLastColumn()).getValues();
  }
  
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    
    if (filtered.indexOf(row[0]) == -1) {
      filtered.push(row[0]); 
    }
  }
  
  return filtered.length;
}