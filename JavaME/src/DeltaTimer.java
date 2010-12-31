/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author osve
 */
public class DeltaTimer {

    public DeltaTimer() {
        lastTime = System.currentTimeMillis();
    }

    public long getDelta() {
        return -lastTime + (lastTime = System.currentTimeMillis());
    }
    long lastTime;
}
