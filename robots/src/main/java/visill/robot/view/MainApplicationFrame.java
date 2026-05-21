package visill.robot.view;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import visill.robot.model.RobotModel;
import visill.robot.model.RobotObserver;
import visill.robot.model.log.Logger;
import visill.robot.save.SaveManager;

public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final SaveManager saveManager = new SaveManager("WindowsPosition.json");
    private final HashMap<String, JInternalFrame> internalFrames = new HashMap<>();
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow,"logger");
        RobotModel robot = new RobotModel();
        robot.addObserver(new RobotObserver() {
            private int c = 0;
            @Override
            public void onRobotMoved() {
                var coords = robot.GetCords();
                if (c%10 == 0){
                    Logger.debug("robot coords is x:%d y:%d".formatted(coords.x,coords.y));
                }
                c+=1;
            }
        });
        GameWindow gameWindow = new GameWindow(robot);
        tryToLoad(gameWindow, "gameWindow", 400, 400, 320, 10, false);
        addWindow(gameWindow, "gameWindow");

        setJMenuBar(generateMenuBar());
        SwingUtilities.invokeLater(() -> tryToLoad(this, "main", screenSize.width, screenSize.height, 0, 0, false));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        for (String title : internalFrames.keySet()) {
            SaveInternalFrameListener(internalFrames.get(title), title);
        }
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
    }
    private void closeWindow() {
        Logger.debug("event close");
        var v = JOptionPane.showConfirmDialog(this, "Вы точно хотите выйти?");
        if (v == JOptionPane.OK_OPTION) {
            for (JInternalFrame window : internalFrames.values()) {
                window.doDefaultCloseAction();
            }
            saveManager.saveWindow(this, "main");
            System.exit(0);
        }
    }
    protected void addWindow(JInternalFrame frame, String title) {
        desktopPane.add(frame);
        internalFrames.put(title, frame);
        frame.setVisible(true);
    }
    protected void tryToLoad(Component window, String title, int defaultWidth, int defaultHeight, int locX, int locY, boolean defaultMaximized) {
        if (!saveManager.loadWindow(window, title)) {
            if (defaultMaximized && window instanceof Frame frame) {
                SwingUtilities.invokeLater(() -> frame.setExtendedState(Frame.MAXIMIZED_BOTH));
            } else {
                window.setLocation(locX, locY);
                window.setSize(defaultWidth, defaultHeight);
            }
        }
    }
    protected void SaveInternalFrameListener(JInternalFrame frame, String title) {
        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                saveManager.saveWindow(frame, title);
            }
        });
    }
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }
//    protected JMenuBar createMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
// 
//        //Set up the lone menu.
//        JMenu menu = new JMenu("Document");
//        menu.setMnemonic(KeyEvent.VK_D);
//        menuBar.add(menu);
// 
//        //Set up the first menu item.
//        JMenuItem menuItem = new JMenuItem("New");
//        menuItem.setMnemonic(KeyEvent.VK_N);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_N, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("new");
////        menuItem.addActionListener(this);
//        menu.add(menuItem);
// 
//        //Set up the second menu item.
//        menuItem = new JMenuItem("Quit");
//        menuItem.setMnemonic(KeyEvent.VK_Q);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("quit");
////        menuItem.addActionListener(this);
//        menu.add(menuItem);
// 
//        return menuBar;
//    }
    
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");
        
        {
            JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
            systemLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(systemLookAndFeel);
        }

        {
            JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
            crossplatformLookAndFeel.addActionListener((event) -> {
                setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                this.invalidate();
            });
            lookAndFeelMenu.add(crossplatformLookAndFeel);
        }

        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");
        
        {
            JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
            addLogMessageItem.addActionListener((event) -> Logger.debug("Новая строка"));
            testMenu.add(addLogMessageItem);
        }

        JMenu closeMenu = new JMenu("Robot");
        closeMenu.setMnemonic(KeyEvent.VK_Q);
        {

            JMenuItem addLogMessageItem = new JMenuItem("Выйти", KeyEvent.VK_Q);
            addLogMessageItem.addActionListener((event) -> closeWindow());

            closeMenu.add(addLogMessageItem);
        }

        menuBar.add(lookAndFeelMenu);
        menuBar.add(testMenu);
        menuBar.add(closeMenu);
        return menuBar;
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }


}
