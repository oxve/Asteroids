
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;

/**
 *
 * @author exstac
 */
public class BluetoothConnection implements Runnable {

    public static final int WAITING = 0;
    public static final int CONNECTING = 1;
    public static final int FAILED = 2;
    public static final int CONNECTED = 3;

    public int state = WAITING;

    public static final int END_CONNECTION = 0;
    public static final int ADD_SMALL_ROCK = 1;
    public static final int ADD_MEDIUM_ROCK = 2;
    public static final int ADD_BIG_ROCK = 3;
    public static final int PAUSE_GAME = 4;
    public static final int RESUME_GAME = 5;
    private static final Object semIn = new Object();
    private static final Object semOut = new Object();
    private static final Object semConn = new Object();
    private Thread thread = null;
    // Bluetooth service name
    private static final String myServiceName = "Asteroids Server";
    // Bluetooth service UUID
    private UUID[] uuids = {new UUID("2d26618601fb47c28d9f10b8ec891363", false)};
    String connURL = "btspp://localhost:" + uuids[0].toString() +
            ";name=" + myServiceName + ";authorize=false";
    private boolean connected = false;
    private Vector inVec = null;
    private Vector outVec = null;
    private StreamConnectionNotifier scn = null;
    private StreamConnection conn = null;
    private boolean server;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public BluetoothConnection(boolean server) {
        this.server = server;
        /** Start this thread */
        thread = new Thread(this);
        thread.start();
    }

    public boolean isConnected() {
        boolean ret;
        synchronized (semConn) {
            ret = connected;
        }
        return ret;
    }

    private void initClient() throws Exception {
        // Select the service. Indicate no
        // authentication or encryption is required.
        state = WAITING;
        String serverURL = LocalDevice.getLocalDevice().getDiscoveryAgent().selectService(uuids[0],
                ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
        if (serverURL == null) {
            state = FAILED;
        }
        conn = (StreamConnection) Connector.open(serverURL);
        state = CONNECTED;
        //System.out.println("Client: Connection established, opening streams");
    }

    private void initServer() throws Exception {
        // Servers set the discoverable mode to GIAC
        LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
        // Create notifier (and service record)
        scn = (StreamConnectionNotifier) Connector.open(connURL);
        // Insert service record into SDDB and wait for an incoming client
        state = WAITING;
        conn = scn.acceptAndOpen();
        //System.out.println("Server: Connection established, opening streams");
        state = CONNECTED;
    }

    public void run() {
        try {
            if (server) {
                initServer();
            } else {
                initClient();
            }
            // A connection has been established, open the streams
            in = conn.openDataInputStream();
            out = conn.openDataOutputStream();

            // We are now connected
            synchronized (semConn) {
                connected = true;
            }

            synchronized (semIn) {
                inVec = new Vector();
            }
            synchronized (semOut) {
                outVec = new Vector();
            }

            //System.out.println("Server: Streams open, waiting for data");
            while (true) {
                while (in.available() > 0) {
                    // Read
                    //System.out.println("Server: Incoming data");
                    synchronized (semIn) {
                        while (in.available() > 0) {
                            int data = in.read();
                            inVec.addElement(new DataObj(data));
                        }
                    }
                }
                synchronized (semOut) {
                    if (outVec.size() > 0) {
                        // Write
                        DataObj data = (DataObj)outVec.elementAt(0);
                        outVec.removeElementAt(0);
                        out.write(data.msg);
                    }
                }
                synchronized (semConn) {
                    if (!connected) {
                        break;
                    }
                }

                Thread.sleep(50);
            }
        } catch (Exception e) {
            //System.out.println(e.toString());
            connected = false;
            state = FAILED;
        }
    }

    public void destroy() {
        synchronized (semConn) {
            connected = false;
        }
        try {
            thread.interrupt();
            thread.join();
        } catch (InterruptedException e) {
        }
        try {
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                // Send a message about closing the connection
                out.write(END_CONNECTION);
                out.close();
                out = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
            if (scn != null) {
                scn.close();
                scn = null;
            }
            // TODO: Add more?
        } catch (Exception e) {
        }
    }

    public void send(int data) {
        synchronized (semOut) {
            outVec.addElement(new DataObj(data));
        }
    }

    public int read() {
        synchronized (semIn) {
            if (inVec.size() > 0) {
                DataObj ret = (DataObj) inVec.firstElement();
                inVec.removeElementAt(0);
                return ret.msg;
            }
        }
        return -1;
    }

    private class DataObj {

        DataObj(int msg) {
            this.msg = msg;
        }
        public int msg;
    }
}
