function doGet(e) {
  return handle(e);
}

function handle(request) {
  var docLink = "<<Create a DB Spreadsheet on Google Drive, Example File Included: Sheets/FormsDB.xlsx. Include Edit URL Here>>";
  var dbSheet = SpreadsheetApp.openByUrl(docLink).getSheets()[0];
  
  
  var rawRows = [];

  
  if (dbSheet.getLastRow() > 1) {
    rawRows = dbSheet.getRange(2, 1, dbSheet.getLastRow() - 1, dbSheet.getLastColumn()).getValues();
  }
  
  var rows = [];
  var count = 0;
  
  for (var i = 0; i < rawRows.length; i++) {
    count++;
    
    rows.push({
      formId: rawRows[i][0],
      title: rawRows[i][1],
      checkinsSheet: rawRows[i][2],
      folderId: rawRows[i][3],
      destinationId: rawRows[i][4]
    });
  }
  
  return ContentService.createTextOutput(JSON.stringify({
    "status": 200,
    "data": rows,
    "count": count
  }));
}