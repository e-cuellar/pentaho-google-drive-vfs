/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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

package org.pentaho.googledrive.vfs.test;

import com.google.api.services.drive.model.File;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

public class GoogleDriveTest {

  @Test public void testVfs() {
    try {

      //TODO convert this into a real unit test
      FileSystemManager manager = VFS.getManager();
      FileObject fileObject = manager.resolveFile( "googledrive://Running Logs" );

      System.out.println( "Name:" + fileObject.getName().getBaseName() );
      System.out.println( "IsFolder:" + fileObject.isFolder() );

      FileObject[] children = fileObject.getChildren();

      System.out.println( "Children:" );
      for ( FileObject file : children ) {
        System.out.println( file.getName() );
      }

      System.out.println( "Creating New Test Folder" );

      FileObject folderFileObject = manager.resolveFile( "googledrive://New Test Folder" );
      folderFileObject.createFolder();

      System.out.println( "Deleting New Test Folder" );

      FileObject testFolderFileObject = manager.resolveFile( "googledrive://New Test Folder" );
      testFolderFileObject.delete();

      FileObject newFileObject = manager.resolveFile( "googledrive://New File" );
      System.out.println( "Name:" + newFileObject.getName().getBaseName() );
      System.out.println( "IsFolder:" + newFileObject.isFolder() );
      System.out.println( "Mime Type:" + newFileObject.getType() );

      System.out.println( "Done..." );
    } catch ( FileSystemException e ) {
      e.printStackTrace();
    }
  }
}
