/*!
* Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.pentaho.googledrive.vfs;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoogleDriveFileObject extends AbstractFileObject {

  public static final String CREDENTIALS = "/client_secret.json";

  private String APPLICATION_NAME = "";

  private static final List<String> SCOPES = Arrays.asList( DriveScopes.DRIVE );

  private FileDataStoreFactory DATA_STORE_FACTORY;

  private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private HttpTransport HTTP_TRANSPORT;

  private Drive driveService;

  private FileType mimeType;

  private String id;

  public enum MIME_TYPES {
    FILE( "application/vnd.google-apps.file", FileType.FILE ), FOLDER( "application/vnd.google-apps.folder",
        FileType.FOLDER );
    private final String mimeType;
    private final FileType fileType;

    private MIME_TYPES( String mimeType, FileType fileType ) {
      this.mimeType = mimeType;
      this.fileType = fileType;
    }

    public static FileType get( String type ) {
      FileType fileType = null;
      if ( FILE.mimeType.equals( type ) ) {
        fileType = FILE.fileType;
      }
      if ( FOLDER.mimeType.equals( type ) ) {
        fileType = FOLDER.fileType;
      }
      return fileType;
    }
  }

  private final java.io.File
      DATA_STORE_DIR =
      new java.io.File( System.getProperty( "user.home" ), ".credentials/google-drive" );

  protected GoogleDriveFileObject( final AbstractFileName fileName, final GoogleDriveFileSystem fileSystem )
      throws FileSystemException {
    super( fileName, fileSystem );
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory( DATA_STORE_DIR );
      driveService = getDriveService();
      resolveMetadata();
    } catch ( Exception e ) {
      throw new FileSystemException( e );
    }
  }

  protected String[] doListChildren() throws Exception {
    String[] childrenNames = null;
    if ( isFolder() ) {
      String query = "'" + id + "' in parents";
      FileList children = driveService.files().list().setQ( query ).execute();
      List<String> fileNames = new ArrayList<String>();
      for ( File fileMetadata : children.getFiles() ) {
        fileNames.add( fileMetadata.getName() );
      }
      childrenNames = fileNames.toArray( new String[0] );
    }
    return childrenNames;
  }

  protected void doCreateFolder() throws Exception {
    if ( !getName().getBaseName().isEmpty() ) {
      File folder = new File();
      folder.setName( getName().getBaseName() );
      folder.setMimeType( MIME_TYPES.FOLDER.mimeType );
      folder = driveService.files().create( folder ).execute();

      id = folder.getId();
      mimeType = MIME_TYPES.get( folder.getMimeType() );
    }
  }

  protected void doDelete() throws Exception {
    driveService.files().delete( id ).execute();
    id = null;
    mimeType = null;
  }

  protected long doGetContentSize() throws Exception {
    return 0;
  }

  protected FileType doGetType() throws Exception {
    return mimeType;
  }

  protected InputStream doGetInputStream() throws Exception {
    return null;
  }

  private void resolveMetadata() throws Exception {

    String fileName = getName().getBaseName();
    String query = "name = " + "'" + fileName + "'";
    FileList result = driveService.files().list().setQ( query ).execute();

    if ( !result.getFiles().isEmpty() ) {
      //TODO WORK ON THIS... HOW DO WE KNOW THE EXACT FILE BY JUST THE NAME....
      File fileMetadata = result.getFiles().get( 0 );
      mimeType = MIME_TYPES.get( fileMetadata.getMimeType() );
      id = fileMetadata.getId();
    }
  }

  private Credential authorize() throws IOException {
    InputStream in = GoogleDriveFileObject.class.getResourceAsStream( CREDENTIALS );
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JSON_FACTORY, new InputStreamReader( in ) );
    GoogleAuthorizationCodeFlow
        flow =
        new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES )
            .setDataStoreFactory( DATA_STORE_FACTORY ).build();

    Credential credential = new AuthorizationCodeInstalledApp( flow, new LocalServerReceiver() ).authorize( "user" );
    System.out.println( "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath() );
    return credential;
  }

  private Drive getDriveService() throws IOException {
    Credential credential = authorize();
    return new Drive.Builder( HTTP_TRANSPORT, JSON_FACTORY, credential ).setApplicationName( APPLICATION_NAME ).build();
  }
}
