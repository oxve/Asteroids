/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * @author exstac
 */
public class Asteroids extends MIDlet {

    public Asteroids() {
    }

    public void startApp() {
        runGame();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
    // Local variables
    private static AsteroidsWorld mgc;

    private void runGame() {
        Display display = Display.getDisplay(this);
        mgc = new AsteroidsWorld(this);
        mgc.setFullScreenMode(true);
        mgc.start();
        display.setCurrent(mgc);
    }
}
