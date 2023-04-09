package dlans.bluetoothapp.utils;

public class BTManager {

    private static volatile BTManager btManager = new BTManager();

    private BTManager() {}

    public static BTManager getInstance() {
        if (btManager == null) {
            synchronized (BTManager.class) {
                if (btManager == null) {
                    btManager = new BTManager();
                }
            }
        }
        return btManager;
    }
}
