
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author osve
 */
public class HighScore {

    public HighScore(final String recordStoreName) {
        this.recordStoreName = recordStoreName;
        names = new String[10];
        scores = new int[10];
        for (int i = 0; i < names.length; ++i) {
            names[i] = "none";
            scores[i] = 0;
        }
        try {
            rs = RecordStore.openRecordStore(this.recordStoreName, true);
            readRecords();
            rs.closeRecordStore();
        } catch (RecordStoreException rse) {
            rse.printStackTrace();
            return;
        }
    }

    private void readRecords() throws RecordStoreException {
        byte[] data;
        int len;
        for (int i = 1; i <= rs.getNumRecords() && i <= 10; i++) {
            data = new byte[rs.getRecordSize(i)];
            len = rs.getRecord(i, data, 0);
            String tmp = new String(data, 0, len);
            names[i - 1] = tmp.substring(0, tmp.indexOf(","));
            scores[i - 1] = Integer.parseInt(tmp.substring(tmp.indexOf(",") + 1, tmp.length()));
        }
    }

    public void addRecord(String name, int score) {

        for (int i = 0; i < names.length; ++i) {
            if (scores[i] < score) {
                for (int j = names.length - 1; j > i; --j) {
                    names[j] = names[j - 1];
                    scores[j] = scores[j - 1];
                }
                names[i] = name;
                scores[i] = score;
                indexOfLastEntry = i;
                break;
            }
        }
        try {
            rs = RecordStore.openRecordStore(recordStoreName, true);
            writeRecords(rs);
            rs.closeRecordStore();
        } catch (RecordStoreException rse) {
            rse.printStackTrace();
        }
        rs = null;
    }

    private void writeRecords(RecordStore recStore) {
        int i = 1;
        for (int p = 0; p < names.length; p++) {
            byte[] rec = (names[p] + "," + scores[p]).getBytes();
            try {
                if (recStore.getNextRecordID() > i) {
                    recStore.setRecord(i++, rec, 0, rec.length);
                } else {
                    recStore.addRecord(rec, 0, rec.length);
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void draw(Graphics g, int width, int height) {
        final Font def = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        final Font latest = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
        g.setFont(def);
        g.setColor(0xFFFFFF);
        g.drawString("High Score", width / 2, height / 12, Graphics.HCENTER | Graphics.BASELINE);
        for (int i = 0; i < names.length; ++i) {
            if (i == indexOfLastEntry) {
                g.setFont(latest);
            }
            g.drawString(names[i], 30, height / 12 * (i + 2), Graphics.LEFT | Graphics.BASELINE);
            g.drawString(scores[i] + "", width - 30, height / 12 * (i + 2), Graphics.RIGHT | Graphics.BASELINE);
            if (i == indexOfLastEntry) {
                g.setFont(def);
            }
        }
    }
    // Local variables
    private String[] names;
    private int[] scores;
    private RecordStore rs = null;
    private final String recordStoreName;
    private int indexOfLastEntry = 10;
}
