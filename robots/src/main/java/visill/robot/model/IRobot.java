package visill.robot.model;

import java.awt.*;

public interface IRobot {
    Point GetCords();
    Point GetTarget();
    double GetDirection();
    void SetTarget(Point p);
    void Tick();
}
