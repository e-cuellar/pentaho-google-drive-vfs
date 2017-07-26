package org.pentaho.googledrive.vfs.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Props;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Created by ecuellar on 4/1/2016.
 */
public class Test {

  public static void main( String args[] ) {
    new Test();
  }

  public Test() {
    Display display = new Display();
    Shell shell = new Shell( display );
    try {
      PropsUI.init( display, Props.TYPE_PROPERTIES_SPOON );

      GoogleAuthorizationDialog d = new GoogleAuthorizationDialog( shell, null );
      d.open(
          "https://accounts.google.com/o/oauth2/auth?client_id=1000221376954-jqi2nkokkdsgf2ldst3ovmuf9720vida.apps.googleusercontent.com&redirect_uri=http://localhost:63725/Callback&response_type=code&scope=https://www.googleapis.com/auth/drive" );

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }
}
