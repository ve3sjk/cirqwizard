/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.layers;

import org.cirqwizard.geom.Point;
import org.cirqwizard.math.RealNumber;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.toolpath.PPPoint;
import org.cirqwizard.toolpath.Toolpath;

import java.util.ArrayList;
import java.util.List;


public class ComponentsLayer extends Layer
{
    private List<PPPoint> points;

    public List<PPPoint> getPoints()
    {
        return points;
    }

    public void setPoints(List<PPPoint> points)
    {
        this.points = points;
    }

    public List<ComponentId> getComponentIds()
    {
        List<ComponentId> ids = new ArrayList<ComponentId>();
        for (PPPoint p : points)
            if (!ids.contains(p.getId()))
                ids.add(p.getId());
        return ids;
    }

    private int bindAngle(int angle)
    {
        if (angle >= 360)
            return angle - 360;
        if (angle < 0)
            return angle + 360;
        return angle;
    }

    @Override
    public void rotate(boolean clockwise)
    {
        for (PPPoint p : points)
        {
            if (clockwise)
            {
                p.setPoint(new Point(p.getPoint().getY(), -p.getPoint().getX()));
                p.setAngle(bindAngle(p.getAngle() + 90));
            }
            else
            {
                p.setPoint(new Point(-p.getPoint().getY(), p.getPoint().getX()));
                p.setAngle(bindAngle(p.getAngle() - 90));
            }
        }
    }

    @Override
    public void move(Point point)
    {
        for (PPPoint p : points)
            p.setPoint(p.getPoint().add(point));
    }

    @Override
    public Point getMinPoint()
    {
        Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (PPPoint p : points)
        {
            if (p.getPoint().getX() < min.getX())
                min = new Point(p.getPoint().getX(), min.getY());
            if (p.getPoint().getY() < min.getY())
                min = new Point(min.getX(), p.getPoint().getY());
        }
        return min;
    }

    @Override
    public List<? extends Toolpath> getToolpaths()
    {
        return points;
    }

    @Override
    public void clearSelection()
    {
    }
}
