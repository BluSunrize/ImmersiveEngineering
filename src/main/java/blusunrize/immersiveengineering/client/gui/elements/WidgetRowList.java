/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.client.gui.elements.WidgetRowList.WidgetRow;
import net.minecraft.client.gui.components.AbstractWidget;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class WidgetRowList<R extends WidgetRow>
{
	// Size values
	private int xPos;
	private final int yPos;

	private final int rowHeight;
	private final int rowWidth;
	private final int maxScroll;

	// Content values
	private final WidgetAssemblyFunction<?>[] widgetFunctions;

	private final LinkedList<WidgetRow> rows = new LinkedList<>();
	private int scrollIndex = 0;

	public WidgetRowList(int xPos, int yPos, int rowHeight, int maxScroll, WidgetAssemblyFunction<?>... widgets)
	{
		this.xPos = xPos;
		this.yPos = yPos;
		this.rowHeight = rowHeight;
		this.maxScroll = maxScroll;
		this.widgetFunctions = widgets;
		// calculate width
		AbstractWidget[] dummyRow = assembleWidgets(() -> 0);
		int maxX = dummyRow[dummyRow.length-1].getX()+dummyRow[dummyRow.length-1].getWidth();
		this.rowWidth = maxX-xPos;
	}

	private AbstractWidget[] assembleWidgets(IntSupplier rowIndex)
	{
		return assembleWidgets(this.xPos, this.yPos+rowIndex.getAsInt()*getRowHeight(), rowIndex);
	}

	private AbstractWidget[] assembleWidgets(int x, int y, IntSupplier rowIndex)
	{
		AbstractWidget[] widgets = new AbstractWidget[widgetFunctions.length];
		for(int iW = 0; iW < this.widgetFunctions.length; iW++)
		{
			widgets[iW] = this.widgetFunctions[iW].apply(x, y, rowIndex);
			x = widgets[iW].getX()+widgets[iW].getWidth();
		}
		return widgets;
	}

	public WidgetRow addRow(@Nullable Consumer<AbstractWidget> screenAddFunction)
	{
		int y = yPos;
		if(!rows.isEmpty())
			y = rows.getLast().widgets[0].getY()+getRowHeight();
		WidgetRow row = new WidgetRow(this, rows.size(), xPos, y);
		// add to screen
		if(screenAddFunction!=null)
			for(AbstractWidget widget : row.widgets)
				screenAddFunction.accept(widget);
		// hide if offscreen
		if((row.rowIndex-scrollIndex) >= maxScroll)
			row.hide();
		rows.add(row);
		return row;
	}

	/**
	 * @return array of widgets, to be removed by the calling Screen
	 */
	public AbstractWidget[] removeRow(final int rowIndex)
	{
		// remove from collections, reference widgets for return
		AbstractWidget[] ret = this.rows.remove(rowIndex).widgets;
		// shift index of remaining ones
		this.rows.forEach(row -> row.shiftIndex(rowIndex));
		// unhide potential offscreen button
		int nextOffscreen = scrollIndex+maxScroll-1;
		if(nextOffscreen < this.rows.size())
			this.rows.get(nextOffscreen).show();
		return ret;
	}

	public int getRowHeight()
	{
		return rowHeight;
	}

	public int getRowWidth()
	{
		return rowWidth;
	}

	public void setXPos(int newX)
	{
		this.xPos = newX;
	}

	public void scrollDown()
	{
		if(scrollIndex+1 < rows.size())
		{
			rows.get(scrollIndex).hide();
			rows.forEach(WidgetRow::shiftUp);
			if(rows.size() > maxScroll+scrollIndex)
				rows.get(maxScroll+scrollIndex).show();
			scrollIndex++;
		}
	}

	public void scrollUp()
	{
		if(scrollIndex > 0)
		{
			rows.get(--scrollIndex).show();
			rows.forEach(WidgetRow::shiftDown);
			if(rows.size() > maxScroll+scrollIndex)
				rows.get(maxScroll+scrollIndex).hide();
		}
	}

	public void scrollTo(int rowIndex)
	{
		int scrollsNeeded = rowIndex-scrollIndex-maxScroll;
		for(int i = 0; i <= scrollsNeeded; i++)
			scrollDown();
	}

	public interface WidgetAssemblyFunction<W extends AbstractWidget>
	{
		W apply(int x, int y, IntSupplier rowIndex);
	}

	public static class WidgetRow
	{
		private final WidgetRowList<?> list;
		private int rowIndex;
		private final AbstractWidget[] widgets;

		public WidgetRow(WidgetRowList<?> list, int initialIdx, int x, int y)
		{
			this.list = list;
			this.rowIndex = initialIdx;
			this.widgets = list.assembleWidgets(x, y, () -> this.rowIndex);
		}

		public int getRowIndex()
		{
			return rowIndex;
		}

		private void shiftIndex(int removed)
		{
			if(this.rowIndex > removed)
			{
				this.rowIndex--;
				shiftUp();
			}
		}

		public void shiftDown()
		{
			for(AbstractWidget button : widgets)
				button.setY(button.getY()+list.getRowHeight());
		}

		public void shiftUp()
		{
			for(AbstractWidget button : widgets)
				button.setY(button.getY()-list.getRowHeight());
		}

		public void hide()
		{
			for(AbstractWidget button : widgets)
				button.visible = false;
		}

		public void show()
		{
			for(AbstractWidget button : widgets)
				button.visible = true;
		}
	}
}
