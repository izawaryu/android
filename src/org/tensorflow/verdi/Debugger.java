package org.tensorflow.verdi;

/**
 * Created by rizawa on 6/24/2017.
 */

public class Debugger {
    public static boolean IsEnabled() {
        return true;
    }

    public static void log(Object o) {
        if (Debugger.IsEnabled()) {
            System.out.println(o.toString());
        }
    }
}
