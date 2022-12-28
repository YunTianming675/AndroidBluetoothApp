package dlans.bluetoothapp.dataPack;

import java.io.DataInputStream;
import java.io.IOException;

import dlans.bluetoothapp.LogUtil;

public class TST30_PACK_S {
    private final static String TAG = "TST30_PACK_S";
    private boolean startFlag = false;

    private char dataHead;
    private char sequence;
    private char[] id = new char[16];
    private char[] tst30_data = new char[168];
    private char[] rsv1 = new char[6];
    private char CRC16;

    public char getDataHead() {
        return dataHead;
    }

    public char getSequence() {
        return sequence;
    }

    public char[] getId() {
        return id;
    }

    public char[] getTst30_data() {
        return tst30_data;
    }

    public char[] getRsv1() {
        return rsv1;
    }

    public char getCRC16() {
        return CRC16;
    }

    // TODO: 2022/12/28 需要获取更详细的文档手册。
}
