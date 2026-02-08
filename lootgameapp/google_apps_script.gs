/**
 * Google Apps Script — Amber Moon Loot Game Cloud Save
 *
 * This script acts as a bridge between the Android app and Google Drive.
 * It reads and writes per-user save files in the shared Drive folder.
 *
 * SETUP:
 * 1. Go to https://script.google.com and create a new project
 * 2. Paste this entire file into Code.gs
 * 3. Click Deploy > New deployment
 * 4. Choose "Web app" as the type
 * 5. Set "Execute as" to "Me"
 * 6. Set "Who has access" to "Anyone"
 * 7. Click Deploy and copy the Web App URL
 * 8. In the Android app, set the URL via GamePreferences.setWebAppUrl(url)
 *
 * Save files are stored as: save_<username>.json
 * in the Google Drive folder linked below.
 */

var FOLDER_ID = '1Ah9GEgg6wo39Xi7U6P-2wZ4M0C3EO-g0';

/**
 * GET handler — downloads a user's save file.
 * URL: <web_app_url>?username=<name>
 */
function doGet(e) {
  var username = e.parameter.username;
  if (!username) {
    return ContentService.createTextOutput('{"error":"no username"}')
      .setMimeType(ContentService.MimeType.JSON);
  }

  var folder = DriveApp.getFolderById(FOLDER_ID);
  var filename = 'save_' + username + '.json';
  var files = folder.getFilesByName(filename);

  if (files.hasNext()) {
    var content = files.next().getBlob().getDataAsString();
    return ContentService.createTextOutput(content)
      .setMimeType(ContentService.MimeType.JSON);
  }

  return ContentService.createTextOutput('{"not_found":true}')
    .setMimeType(ContentService.MimeType.JSON);
}

/**
 * POST handler — creates or updates a user's save file.
 * URL: <web_app_url>?username=<name>
 * Body: JSON save data
 */
function doPost(e) {
  var username = e.parameter.username;
  if (!username) {
    return ContentService.createTextOutput('{"error":"no username"}')
      .setMimeType(ContentService.MimeType.JSON);
  }

  var content = e.postData.contents;
  var folder = DriveApp.getFolderById(FOLDER_ID);
  var filename = 'save_' + username + '.json';
  var files = folder.getFilesByName(filename);

  if (files.hasNext()) {
    // Update existing file
    files.next().setContent(content);
  } else {
    // Create new file
    folder.createFile(filename, content, MimeType.PLAIN_TEXT);
  }

  return ContentService.createTextOutput('{"success":true}')
    .setMimeType(ContentService.MimeType.JSON);
}
