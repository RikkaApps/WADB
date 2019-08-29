package moe.haruue.wadb.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.topjohnwu.superuser.CallbackList;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import moe.haruue.wadb.BuildConfig;

public class SuShell {

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

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
            Shell.su("echo test").exec();
        }
        return Shell.rootAccess();
    }

    public synchronized static void close() {
        Shell cached = Shell.getCachedShell();
        if (cached != null) {
            try {
                cached.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized String version(boolean internal) {
        Result result = run(internal ? "su -V" : "su -v");
        return result.output.size() > 0 ? result.output.get(0) : "unknown";
    }

    public synchronized static Result run(String command) {
        return run(new String[]{command});
    }

    public synchronized static Result run(List<String> commands) {
        return run(commands.toArray(new String[0]));
    }

    public synchronized static Result run(String... commands) {
        Shell.Result result = Shell.su(commands).exec();
        return new Result(result.getCode(), result.getOut());
    }

    public static class Interactive {

        public interface Callback {
            void onLine(String line);

            void onResult(Result result);
        }

        public static void run(String command, Callback callback) {
            run(new String[]{command}, callback);
        }

        public static void run(List<String> commands, Callback callback) {
            run(commands.toArray(new String[0]), callback);
        }

        public static void run(String[] commands, Callback callback) {
            Shell.su(commands).to(new CallbackList<String>() {
                @Override
                public void onAddElement(String s) {
                    if (callback != null) {
                        callback.onLine(s);
                    }
                }
            }).submit(result -> {
                if (callback != null) {
                    callback.onResult(new Result(result.getCode(), result.getOut()));
                }
            });
        }

        public static void close() {
            SuShell.close();
        }
    }
}
