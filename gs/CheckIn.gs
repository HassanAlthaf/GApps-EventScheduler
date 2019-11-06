function doPost(e) {
  return handle(e);
}

function handle(request) {
  var formId = request.parameters.formId;
  var spreadsheetId = request.parameter.spreadsheetId;
  var employeeId = request.parameter.employeeId;
  var dateTime = request.parameter.dateTime;
  var latitude = request.parameter.latitude;
  var longitude = request.parameter.longitude;
  
  var form = FormApp.openById(formId);
  
  if (form == null) {
    return ContentService.createTextOutput(JSON.stringify({
       "status": 404,
       "error": "The QR Code provided is invalid!"
     }));
  }
  
  var formSheet = SpreadsheetApp.openById(form.getDestinationId()).getSheets()[0];
  
  var rows = [];
  
  if (formSheet.getLastRow() > 1) {
      var rows = formSheet.getRange(2, 1, formSheet.getLastRow() - 1, formSheet.getLastColumn()).getValues();
  }
  
  var found = false;
  var user = null;
  
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    
    // Check EmpID
    if (row[3] == employeeId) {
      found = true;
      user = row;
      
      break;
    }
  }
  
  if (!found) {
     return ContentService.createTextOutput(JSON.stringify({
       "status": 404,
       "error": "Registration was not found!"
     }));
  }
  
  var checkinsSpreadsheet = SpreadsheetApp.openById(spreadsheetId);
  
  if (checkinsSpreadsheet == null) {
    return ContentService.createTextOutput(JSON.stringify({
       "status": 404,
       "error": "The QR Code provided is invalid!"
     }));
  }
  
  var sheet = checkinsSpreadsheet.getSheets()[0];
  
  sheet.appendRow([
    employeeId,
    latitude,
    longitude,
    dateTime
  ]);
  
  return ContentService.createTextOutput(JSON.stringify({
    "status": 200,
    "data": JSON.stringify({
      name: user[1],
      email: user[2]
    })
  }));
}