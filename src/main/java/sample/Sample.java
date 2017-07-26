package sample;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
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
import java.util.Arrays;
import java.util.List;

public class Sample {

  private static final java.io.File UPLOAD_FILE = new java.io.File( "sample.jpeg" );

  private static final String APPLICATION_NAME = "";

  private static final java.io.File
      DATA_STORE_DIR =
      new java.io.File( System.getProperty( "user.home" ), ".credentials/google-drive" );

  private static FileDataStoreFactory DATA_STORE_FACTORY;

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static HttpTransport HTTP_TRANSPORT;

  private static final List<String> SCOPES = Arrays.asList( DriveScopes.DRIVE );

  private static Drive service;

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory( DATA_STORE_DIR );
    } catch ( Throwable t ) {
      t.printStackTrace();
      System.exit( 1 );
    }
  }

  public static Credential authorize() throws IOException {
    InputStream in = Sample.class.getResourceAsStream( "/client_secret.json" );
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JSON_FACTORY, new InputStreamReader( in ) );

    GoogleAuthorizationCodeFlow
        flow =
        new GoogleAuthorizationCodeFlow.Builder( HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES )
            .setDataStoreFactory( DATA_STORE_FACTORY ).build();

    Credential credential = new AuthorizationCodeInstalledApp( flow, new LocalServerReceiver() ).authorize( "user" );
    System.out.println( "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath() );
    return credential;
  }

  public static Drive getDriveService() throws IOException {
    Credential credential = authorize();
    return new Drive.Builder( HTTP_TRANSPORT, JSON_FACTORY, credential ).setApplicationName( APPLICATION_NAME ).build();
  }

  public static void main( String[] args ) throws IOException {
    service = getDriveService();
    // uploadFile(true);

    FileList result = service.files().list()
        //Retrieve a folder
        .setQ( "mimeType = 'application/vnd.google-apps.folder'" )
        //Find a file by name
        .setQ( "name = 'Accts'" )
        //.setPageSize(10)
        //.setFields("nextPageToken, files(id, name)")
        .execute();
    List<File> files = result.getFiles();
    if ( files == null || files.size() == 0 ) {
      System.out.println( "No files found." );
    } else {
      System.out.println( "Files:" );
      for ( File file : files ) {
        System.out.printf( "%s (%s)\n", file.getName(), file.getId() );
      }
    }
  }

  private static File uploadFile( boolean useDirectUpload ) throws IOException {
    File fileMetadata = new File();
    fileMetadata.setName( UPLOAD_FILE.getName() );

    FileContent mediaContent = new FileContent( "image/jpeg", UPLOAD_FILE );

    Drive.Files.Create insert = service.files().create( fileMetadata, mediaContent );
    MediaHttpUploader uploader = insert.getMediaHttpUploader();
    uploader.setDirectUploadEnabled( useDirectUpload );
    return insert.execute();
  }
}
