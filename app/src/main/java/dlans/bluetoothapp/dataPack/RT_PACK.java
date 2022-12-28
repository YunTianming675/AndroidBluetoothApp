package dlans.bluetoothapp.dataPack;

import java.io.DataInputStream;
import java.io.IOException;

import dlans.bluetoothapp.LogUtil;

public class RT_PACK {
    private final static String TAG = "RT_PACK";
    private boolean startFlag = false;

    private char dataHead;
    private byte[] heartData = new byte[64];
    private char heartRate;
    private char spo2;
    private char bk;
    private char[] rsv = new char[8];
    private char spb;
    private char dbp;

    public void receiveData(DataInputStream dataInputStream) {
        receiveDataHead(dataInputStream);
        receiveHeartData(dataInputStream);
        receiveHeartRateAndSpo2(dataInputStream);
        receiveBk(dataInputStream);
        receiveRsv(dataInputStream);
    }

    private void receiveDataHead(DataInputStream dataInputStream) {
        try {
            dataHead = dataInputStream.readChar();
            if ((dataHead & 0xFF00) == 0xFF00) {
                startFlag = true;
                heartData[0] = (byte) (dataHead & 0x00FF);
                LogUtil.d(TAG, "--------------------start--------------------");
            }
        }
        catch (IOException e) {
            LogUtil.e(TAG, "receive dataHead IOException");
            e.printStackTrace();
        }
    }

    private void receiveHeartData(DataInputStream dataInputStream) {
        try {
            for (int i = 1; i < heartData.length; i++) {
                heartData[i] = dataInputStream.readByte();
            }
        }
        catch (IOException e) {
            LogUtil.e(TAG, "receive heart data IOException");
            e.printStackTrace();
        }
    }

    private void receiveHeartRateAndSpo2(DataInputStream dataInputStream) {
        try {
            heartRate = dataInputStream.readChar();
            spo2 = (char) (heartRate & 0x00FF);
            heartRate = (char) ((heartRate & 0xFF00) >> 8);
        } catch (IOException e) {
            LogUtil.e(TAG, "receive heart rate IOException");
            e.printStackTrace();
        }
    }

    private void receiveBk(DataInputStream dataInputStream) {
        try {
            bk = dataInputStream.readChar();
        } catch (IOException e) {
            LogUtil.e(TAG, "receive bk IOException");
            e.printStackTrace();
        }
    }

    private void receiveRsv(DataInputStream dataInputStream) {
        try {
            for (int i = 0; i < rsv.length; i++) {
                rsv[i] = dataInputStream.readChar();
            }
            spb = rsv[3];
            dbp = rsv[4];
        } catch (IOException e) {
            LogUtil.e(TAG, "receive rsv IOException");
            e.printStackTrace();
        }
    }

    public boolean isStartFlag() {
        return startFlag;
    }

    public byte[] getHeartData() {
        return heartData;
    }

    public char getHeartRate() {
        return heartRate;
    }

    public char getSpo2() {
        return spo2;
    }

    public char getBk() {
        return bk;
    }

    public char[] getRsv() {
        return rsv;
    }
}
