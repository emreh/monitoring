package test.ir.myhome.agent;

import ir.myhome.agent.window.Window;
import ir.myhome.agent.window.WindowState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WindowTest {

    @Test
    void valid_lifecycle() {
        Window w = new Window();

        assertEquals(WindowState.OPEN, w.state());

        w.snapshot();
        assertEquals(WindowState.SNAPSHOT_TAKEN, w.state());

        w.close();
        assertEquals(WindowState.CLOSED, w.state());
    }

    @Test
    void snapshot_twice_is_illegal() {
        Window w = new Window();
        w.snapshot();

        assertThrows(IllegalStateException.class, w::snapshot);
    }

    @Test
    void close_without_snapshot_is_illegal() {
        Window w = new Window();

        assertThrows(IllegalStateException.class, w::close);
    }
}
