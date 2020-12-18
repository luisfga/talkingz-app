package br.com.luisfga.talkingz.services.messaging.handling;

import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

class UserSessionPartialsBuffer {

    private final String TAG = "UserSessionPrtBuffer";

    private byte[] jointFrames;

    private ExecutorService cachedThreadPool;

    void appendBytes(byte[] frame){

        if (jointFrames == null) {
            jointFrames = frame;
        } else {
            Log.d(TAG, "Buildando 'jointFrames' (previous size: "+jointFrames.length+ " / frameSize: "+frame.length);
            byte[] destination = new byte[jointFrames.length+frame.length];
            System.arraycopy(jointFrames, 0, destination, 0, jointFrames.length);
            System.arraycopy(frame, 0, destination, jointFrames.length, frame.length);
            jointFrames = destination;
            Log.d(TAG, "New 'jointFrames' size: "+jointFrames.length);
        }

    }

    Object getMessageObject() {
        if (jointFrames != null && jointFrames.length > 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(jointFrames);
                ObjectInput in = null;
                in = new ObjectInputStream(bis);

                return in.readObject();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    void clear(){
        jointFrames = null;
    }
}
