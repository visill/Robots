package visill.robot.save;

import javax.swing.*;
import java.awt.*;


public class WindowStatus {
    private int x, y, width, height;
    private boolean is_maxed;

    public WindowStatus() {
    }

    public WindowStatus(Component window) {
        this.x = window.getX();
        this.y = window.getY();
        this.width = window.getWidth();
        this.height = window.getHeight();
        if (window instanceof Frame frame) {
            is_maxed = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        } else is_maxed = false;
    }

    public void apply(Component window) {
        window.setLocation(x, y);
        window.setSize(width, height);
        if (window instanceof Frame frame && is_maxed) {
            SwingUtilities.invokeLater(() -> frame.setExtendedState(Frame.MAXIMIZED_BOTH));
        }
    }
}