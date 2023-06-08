/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitRegister;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Locale;

public class GuiButtonLogicCircuitRegister extends GuiButtonState<LogicCircuitRegister> implements ITooltipWidget
{
	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("circuit_table");
	private final MutableInt state;

	public static GuiButtonLogicCircuitRegister create(
			int x, int y, Component name, IIEPressable<GuiButtonState<LogicCircuitRegister>> handler
	)
	{
		final MutableInt state = new MutableInt();
		return new GuiButtonLogicCircuitRegister(x, y, name, btn -> {
			state.setValue(btn.getNextStateInt());
			handler.onIEPress(btn);
		}, state);
	}

	private GuiButtonLogicCircuitRegister(
			int x, int y, Component name, IIEPressable<GuiButtonState<LogicCircuitRegister>> handler, MutableInt state
	)
	{
		super(x, y, 18, 18, name, LogicCircuitRegister.values(), state::getValue, TEXTURE, 234, 0, -1, handler);
		this.textOffset = new int[]{3, 5};
		this.state = state;
	}

	@Override
	public Component getMessage()
	{
		LogicCircuitRegister state = getState();
		if(state.ordinal() >= 16)
			return Component.literal(state.name());
		return Component.empty();
	}

	@Override
	public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
	{
		tooltip.add(getState().getDescription());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		super.render(graphics, mouseX, mouseY, partialTicks);
		if(this.visible)
		{
			LogicCircuitRegister state = getState();
			if(state.ordinal() < 16)
				GuiHelper.drawColouredRect(graphics.pose(), getX()+3, getY()+3, 12, 12, DyeColor.byId(state.ordinal()));
		}
	}

	public void setState(int state)
	{
		this.state.setValue(state);
	}

	private static final ListMultimap<Character, Integer> SPLIT_BY_INITIAL = ArrayListMultimap.create(11, 2);

	static
	{
		for(DyeColor dye : DyeColor.values())
		{
			String transl = I18n.get("color.minecraft."+dye.getName()).toLowerCase(Locale.ROOT);
			SPLIT_BY_INITIAL.get(transl.charAt(0)).add(dye.getId());
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers)
	{
		codePoint = Character.toLowerCase(codePoint);
		if(Character.isDigit(codePoint))
		{
			int number = Character.digit(codePoint, 10);
			if(number>=0 && number<8)
			{
				this.state.setValue(number+16); // plus 16 colors
				this.onPress.onPress(this);
				return true;
			}
		}
		else if(Character.isAlphabetic(codePoint))
		{
			List<Integer> options = SPLIT_BY_INITIAL.get(codePoint);
			if(!options.isEmpty())
			{
				int next = (options.indexOf(this.getStateAsInt())+1)%options.size();
				this.state.setValue(options.get(next));
				this.onPress.onPress(this);
				return true;
			}
		}
		return false;
	}
}
