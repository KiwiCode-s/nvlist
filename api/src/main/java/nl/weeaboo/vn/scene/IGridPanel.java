package nl.weeaboo.vn.scene;

import nl.weeaboo.vn.layout.GridCellConstraints;

public interface IGridPanel extends IPanel {

    void add(IVisualElement elem, GridCellConstraints constraints);

    void endRow();

}