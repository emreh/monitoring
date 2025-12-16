package ir.myhome.agent.window;

public enum WindowState {
    OPEN {
        @Override
        WindowState onSnapshot() {
            return SNAPSHOT_TAKEN;
        }
    }, SNAPSHOT_TAKEN {
        @Override
        WindowState onClose() {
            return CLOSED;
        }
    }, CLOSED;

    WindowState onSnapshot() {
        throw new IllegalStateException("Cannot take snapshot from state: " + this);
    }

    WindowState onClose() {
        throw new IllegalStateException("Cannot close window from state: " + this);
    }
}
