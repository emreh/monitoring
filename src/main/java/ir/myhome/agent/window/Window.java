package ir.myhome.agent.window;

public final class Window {

    private WindowState state = WindowState.OPEN;

    public WindowState state() {
        return state;
    }

    public void snapshot() {
        state = state.onSnapshot();
    }

    public void close() {
        state = state.onClose();
    }
}
