package com.bravo.rungps;

import java.util.Observable;
import java.util.Observer;

/**
 * Create on 2018/7/22 on 17:30
 * Description:
 * Coder by lilee
 */
public class MsgObservable extends Observable {

    static class Inner {
        static MsgObservable instance = new MsgObservable();
    }

    public static MsgObservable getInstance() {
        return Inner.instance;
    }


    public synchronized void sendMsgs(Object o) {
        setChanged();
        notifyObservers(o);
    }
}
