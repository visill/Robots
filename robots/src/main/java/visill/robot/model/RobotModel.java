package visill.robot.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RobotModel implements IRobot {
    private final Timer m_timer = initTimer();

    private static Timer initTimer()
    {
        return new Timer("events generator", true);
    }
    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;

    private static final double maxVelocity = 0.5;
    private static final double maxAngularVelocity = 0.005;
    private final List<RobotObserver> m_observers = new ArrayList<>();
    public void addObserver(RobotObserver observer) {
        if (observer != null) {
            m_observers.add(observer);
        }
    }
    private void notifyObservers() {
        for (RobotObserver observer : m_observers) {
            observer.onRobotMoved();
        }
    }
    public RobotModel() {
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Tick();
            }
        }, 100, 10);
    }

    public Point GetCords() {
        return new Point(round(m_robotPositionX),round(m_robotPositionY));
    }
    public Point GetTarget() {
        return new Point(m_targetPositionX, m_targetPositionY);
    }
    public double GetDirection() {
        return m_robotDirection;
    }
    public void SetTarget(Point p)
    {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }


    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    public void Tick()
    {
        double distance = distance(m_targetPositionX, m_targetPositionY,
                m_robotPositionX, m_robotPositionY);

        if (distance < 10)
        {
            return;
        }
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double diff = Math.atan2(Math.sin(angleToTarget - m_robotDirection), Math.cos(angleToTarget - m_robotDirection));

        double angularVelocity = 0;
        angularVelocity = Math.copySign(maxAngularVelocity, diff);


        moveRobot(maxVelocity, angularVelocity, 5);
        notifyObservers();
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        velocity = applyLimits(velocity, 0, maxVelocity);

        angularVelocity = applyLimits(angularVelocity, -maxAngularVelocity, maxAngularVelocity);

        double min = -0.03;
        double max = 0.03;
        double randomValue = ThreadLocalRandom.current().nextDouble(min, max);
        angularVelocity += randomValue;
        double newX;
        double newY;
        if (Math.abs(angularVelocity) < 0.000001)
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }else {
            double angleDelta = angularVelocity * duration;
            newX = m_robotPositionX + (velocity / angularVelocity) *
                    (Math.sin(m_robotDirection + angleDelta) - Math.sin(m_robotDirection));

            newY = m_robotPositionY - (velocity / angularVelocity) *
                    (Math.cos(m_robotDirection + angleDelta) - Math.cos(m_robotDirection));
        }
        if (Double.isFinite(newX) && Double.isFinite(newY)) {
            m_robotPositionX = newX;
            m_robotPositionY = newY;
        }
        m_robotDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }
}
