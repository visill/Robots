package visill.robot.controller;

import visill.robot.view.MainApplicationFrame;

import java.awt.*;

public class MainController {
    public static void run() {
        MainApplicationFrame frame = new MainApplicationFrame();
        frame.pack();
        frame.setVisible(true);
    }
}
