package moe.haruue.wadb.events;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Events {

    private static class Receiver<T> {

        public T event;
        public Object[] tags;

        private Receiver(T event, Object[] tags) {
            this.event = event;
            this.tags = tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Receiver<?> receiver1 = (Receiver<?>) o;
            return event.equals(receiver1.event) &&
                    Arrays.equals(tags, receiver1.tags);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(event);
            result = 31 * result + Arrays.hashCode(tags);
            return result;
        }
    }

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private static <T> void post(Function<T> consumer, Collection<Receiver<T>> receivers, Object... tags) {
        for (Receiver<T> receiver : receivers) {
            if (!Arrays.equals(tags, receiver.tags)) {
                continue;
            }

            /*if (receiver.event instanceof LifecycleOwner) {
                LifecycleOwner lifecycleOwner = (LifecycleOwner) receiver.event;
                lifecycleOwner.getLifecycle().getCurrentState();
            }*/

            if (Looper.myLooper() != Looper.getMainLooper()) {
                mainThreadHandler.post(() -> consumer.invoke(receiver.event));
            } else {
                consumer.invoke(receiver.event);
            }
        }
    }

    private static Set<Receiver<WadbStateChangedEvent>> wadbStateChangedEventReceivers = new CopyOnWriteArraySet<>();

    private static Set<Receiver<WadbFailureEvent>> wadbFailureEventReceivers = new CopyOnWriteArraySet<>();

    public static void registerAll(@NonNull Event receiver, Object... tags) {
        if (receiver instanceof WadbStateChangedEvent) {
            wadbStateChangedEventReceivers.add(new Receiver<>((WadbStateChangedEvent) receiver, tags));
        }
        if (receiver instanceof WadbFailureEvent) {
            wadbFailureEventReceivers.add(new Receiver<>((WadbFailureEvent) receiver, tags));
        }
    }

    public static void unregisterAll(@NonNull Event receiver, Object... tags) {
        if (receiver instanceof WadbStateChangedEvent) {
            wadbStateChangedEventReceivers.remove(new Receiver<>((WadbStateChangedEvent) receiver, tags));
        }
        if (receiver instanceof WadbFailureEvent) {
            wadbFailureEventReceivers.remove(new Receiver<>((WadbFailureEvent) receiver, tags));
        }
    }

    public static void postWadbStateChangedEvent(Function<WadbStateChangedEvent> consumer) {
        post(consumer, wadbStateChangedEventReceivers);
    }

    public static void postWadbFailureEvent(Function<WadbFailureEvent> consumer) {
        post(consumer, wadbFailureEventReceivers);
    }
}
