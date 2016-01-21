package nl.weeaboo.vn.core.impl;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.core.IRenderEnv;
import nl.weeaboo.vn.core.KeyCode;

public final class InputAccumulator {

    private final List<Event> inputEvents = Lists.newArrayList();

    public synchronized void addEvent(Event event) {
        inputEvents.add(Checks.checkNotNull(event));
    }

    public synchronized List<Event> drainEvents() {
        ImmutableList<Event> result = ImmutableList.copyOf(inputEvents);
        inputEvents.clear();
        return result;
    }

    public enum PressState {
        PRESS, RELEASE;
    }

    public static class Event {

        public long timestampMs;

        public Event(long timestampMs) {
            this.timestampMs = timestampMs;
        }

    }

    public static class ButtonEvent extends Event {

        public final KeyCode key;
        public final PressState pressState;

        public ButtonEvent(long timestampMs, KeyCode key, PressState pressState) {
            super(timestampMs);

            this.key = Checks.checkNotNull(key);
            this.pressState = Checks.checkNotNull(pressState);
        }

    }

    public static class MousePositionEvent extends Event {

        public final double x;
        public final double y;

        /**
         * The pointer position should be passed in virtual coordinates and not in physical screen
         * coordinates, see {@link IRenderEnv#getVirtualSize()}.
         *
         * @param x Pointer X-coordinate in virtual coordinates.
         * @param x Pointer Y-coordinate in virtual coordinates.
         */
        public MousePositionEvent(long timestampMs, double x, double y) {
            super(timestampMs);

            this.x = x;
            this.y = y;
        }

    }

    public static class MouseScrollEvent extends Event {

        public final int scrollAmount;

        /**
         * @param scrollAmount The number of clicks scrolled by the scroll wheel. Positive values indicate a
         *        downward scroll, negative values an upward scroll.
         */
        public MouseScrollEvent(long timestampMs, int scrollAmount) {
            super(timestampMs);

            this.scrollAmount = scrollAmount;
        }

    }

}