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

package org.cirqwizard.fx.traces.bottom;

import org.cirqwizard.fx.Context;
import org.cirqwizard.fx.PCBPaneFX;
import org.cirqwizard.fx.traces.TraceMilling;
import org.cirqwizard.layers.Layer;

public class BottomTraceMilling extends TraceMilling
{
    @Override
    public void refresh()
    {
        pcbPane.setGerberColor(PCBPaneFX.BOTTOM_TRACE_COLOR);
        super.refresh();
    }

    @Override
    protected boolean isEnabled()
    {
        return super.isEnabled() && Context.PcbPlacement.FACE_DOWN.equals(getMainApplication().getContext().getPcbPlacement());
    }

    protected Layer getCurrentLayer()
    {
        return getMainApplication().getContext().getPcbLayout().getBottomTracesLayer();
    }

    @Override
    protected boolean mirror()
    {
        return true;
    }

    @Override
    protected int getCacheId()
    {
        return 1;
    }

    @Override
    protected long getLayerModificationDate()
    {
        return getMainApplication().getContext().getPcbLayout().getBottomLayerModificationDate();
    }
}
