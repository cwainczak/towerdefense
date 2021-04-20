package com.wsu.towerdefense.Model.save;

import android.content.Context;
import android.util.Log;
import com.wsu.towerdefense.Model.Game;
import com.wsu.towerdefense.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

/**
 * Saves and loads {@link Game} state via {@link SaveState} in internal storage
 */
public class Serializer {

    /**
     * Constant save file string; use by default unless multiple saves are needed in the future
     */
    public static final String SAVEFILE = "savefile";

    public static void save(Context context, String saveFile, Game game) throws IOException {
        Log.i(context.getString(R.string.logcatKey), "Saving game to save file '" + saveFile + "'");

        SaveState saveState = new SaveState(saveFile, LocalDateTime.now(), game);

        try (
            FileOutputStream fileOutputStream =
                context.openFileOutput(saveFile, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(saveState);
        }
    }

    public static SaveState load(Context context, String saveFile)
        throws IOException, ClassNotFoundException {
        try (
            FileInputStream fileInputStream = context.openFileInput(saveFile);
            ObjectInputStream objectInputStream = new ObjectInputStream((fileInputStream))
        ) {
            SaveState saveState = (SaveState) objectInputStream.readObject();
            return saveState;
        }
    }

    public static boolean exists(Context context, String saveFile) {
        File file = context.getFileStreamPath(saveFile);
        return file.exists();
    }

    public static void delete(Context context, String saveFile) {
        context.deleteFile(saveFile);
    }
}
