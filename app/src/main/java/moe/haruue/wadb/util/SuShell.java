package moe.haruue.wadb.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuShell {

    /**
     * Result class.
     */
    public static class Result implements Parcelable {

        /**
         * Exit code of the last command
         */
        public final int exitCode;

        /**
         * Output of command(s), might be null
         */
        public final List<String> output;

        public Result(int exitCode, List<String> output) {
            this.exitCode = exitCode;
            this.output = output != null ? new ArrayList<>(output) : new ArrayList<String>();
        }

        public String getOutputString() {
            StringBuilder sb = new StringBuilder();
            for (String s : output) {
                sb.append(s).append('\n');
            }
            return sb.toString().trim();
        }

        protected Result(Parcel in) {
            exitCode = in.readInt();
            output = in.createStringArrayList();
        }

        public static final Creator<Result> CREATOR = new Creator<Result>() {
            @Override
            public Result createFromParcel(Parcel in) {
                return new Result(in);
            }

            @Override
            public Result[] newArray(int size) {
                return new Result[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(exitCode);
            dest.writeStringList(output);
        }
    }

    public synchronized static boolean available() {
        if (!Shell.rootAccess()) {
            try {
                Shell.getShell().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Shell.newInstance();
            Shell.Sync.su("echo test");
        }
        return Shell.rootAccess();
    }

    public static synchronized String version(boolean internal) {
        List<String> ret = Shell.Sync.su("su -v");
        return ret.size() > 0 ? ret.get(0) : "unknown";
    }

    public synchronized static Result run(String command) {
        return run(new String[]{command});
    }

    public synchronized static Result run(List<String> commands) {
        return run(commands.toArray(new String[commands.size()]));
    }

    public synchronized static Result run(String[] commands) {

        String newCommands[] = Arrays.copyOf(commands, commands.length + 1);
        newCommands[commands.length] = "echo $?";

        int exitCode = Integer.MIN_VALUE;
        List<String> stdout = new ArrayList<>();
        Shell.Sync.su(stdout, newCommands);

        if (stdout.size() > 0) {
            try {
                exitCode = Integer.valueOf(stdout.get(stdout.size() - 1));
            } catch (NumberFormatException e) {
                // should not happen
                e.printStackTrace();
            }

            stdout.remove(stdout.size() - 1);
        }

        return new Result(exitCode, stdout);
    }
}
