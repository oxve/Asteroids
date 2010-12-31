/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.*;

/**
 * @author exstac
 */
public class Asteroids extends MIDlet implements CommandListener {

    public Asteroids() {
        exitCommand = new Command("Exit", Command.EXIT, 2);
    }

    public void startApp() {
        runGame();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
    // Local variables
    private static AsteroidsWorld mgc;
    private Command exitCommand;

    private void runGame() {
        Display display = Display.getDisplay(this);
        mgc = new AsteroidsWorld(false);
        mgc.addCommand(exitCommand);
        mgc.setCommandListener(this);
        mgc.setFullScreenMode(true);
        mgc.start();
        display.setCurrent(mgc);
    }
}
