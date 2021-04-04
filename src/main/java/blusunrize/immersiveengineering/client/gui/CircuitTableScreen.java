/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitRegister;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonLogicCircuitRegister;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.client.gui.elements.GuiSelectingList;
import blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableTileEntity;
import blusunrize.immersiveengineering.common.gui.CircuitTableContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import javax.annotation.Nullable;
import java.util.*;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class CircuitTableScreen extends IEContainerScreen<CircuitTableContainer>
{
	private GuiSelectingList operatorList;
	private final List<GuiButtonState<LogicCircuitRegister>> inputs = new ArrayList<>(LogicCircuitOperator.TOTAL_MAX_INPUTS);
	private GuiButtonState<LogicCircuitRegister> output;

	private final ResettableLazy<Optional<LogicCircuitInstruction>> instruction = new ResettableLazy<>(() -> {
		LogicCircuitOperator operator = getSelectedOperator();
		if(operator==null)
			return Optional.empty();
		return Optional.of(
				new LogicCircuitInstruction(
						operator,
						output.getState(),
						inputs.stream().map(GuiButtonState::getState).filter(Objects::nonNull)
								.limit(operator.getArgumentCount()).toArray(LogicCircuitRegister[]::new))
		);
	});

	public CircuitTableScreen(CircuitTableContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.xSize = 234;
		this.ySize = 182;
	}

	@Override
	public void init()
	{
		super.init();
		mc().keyboardListener.enableRepeatEvents(true);

		this.operatorList = (GuiSelectingList)this.addButton(new GuiSelectingList(this, guiLeft+58, guiTop+16, 36, 56, btn -> {
			this.instruction.reset();
			this.minecraft.enqueue(this::updateButtons);
		}, Arrays.stream(LogicCircuitOperator.values()).map(Enum::name).toArray(String[]::new)).setPadding(1, 1, 2, 0));

		this.output = this.addButton(new GuiButtonLogicCircuitRegister(
				guiLeft+121, guiTop+56,
				new StringTextComponent("Output"), btn -> this.instruction.reset())
		);
		this.updateButtons();
	}

	@Nullable
	private LogicCircuitOperator getSelectedOperator()
	{
		return LogicCircuitOperator.getByString(operatorList.getSelectedString());
	}

	private void updateButtons()
	{
		LogicCircuitOperator operator = getSelectedOperator();
		if(operator!=null)
		{
			int inputCount = operator.getArgumentCount();
			int inputStart = 130-(inputCount*10-1);
			if(inputCount < this.inputs.size())
			{
				Iterator<GuiButtonState<LogicCircuitRegister>> it = this.inputs.iterator();
				int i = 0;
				// Reposition buttons and remove excess
				while(it.hasNext())
				{
					GuiButtonState btn = it.next();
					btn.x = guiLeft+inputStart+20*i;
					if(++i > inputCount)
					{
						this.buttons.remove(btn);
						this.children.remove(btn);
						it.remove();
					}
				}
			}
			else
			{
				for(int i = 0; i < inputCount; i++)
				{
					if(i < this.inputs.size()) // Reposition buttons
						this.inputs.get(i).x = guiLeft+inputStart+20*i;
					else // Add new ones
						this.inputs.add(this.addButton(new GuiButtonLogicCircuitRegister(
								guiLeft+inputStart+20*i, guiTop+18,
								new StringTextComponent("Input "+(i+1)), btn -> this.instruction.reset())
						));
				}
			}
		}
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);

		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(this.hoveredSlot!=null&&this.hoveredSlot.slotNumber < 4&&!this.hoveredSlot.getHasStack())
		{
			int slotNum = this.hoveredSlot.slotNumber;
			tooltip.add(TextUtils.applyFormat(
					new TranslationTextComponent(Lib.DESC_INFO+"circuit_table."+CircuitTableTileEntity.SLOT_TYPES[slotNum]),
					TextFormatting.GRAY
			));
		}
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
		drawCenteredString(transform, this.font, "Operator:", 76, 4, DyeColor.LIGHT_GRAY.getColorValue());
		drawCenteredString(transform, this.font, "Inputs:", 130, 8, DyeColor.LIGHT_GRAY.getColorValue());
		drawCenteredString(transform, this.font, "Outputs:", 130, 42, DyeColor.LIGHT_GRAY.getColorValue());

		for(int i = 0; i < 4; i++)
		{
			int amount = 0;
			DyeColor col = DyeColor.LIGHT_GRAY;
			if(this.instruction.get().isPresent())
			{
				amount = CircuitTableTileEntity.getIngredientAmount(this.instruction.get().get(), i);
				if(this.container.inventorySlots.get(i).getStack().getCount() >= amount)
					col = DyeColor.GREEN;
				else
					col = DyeColor.RED;
			}
			this.font.drawString(transform, "x "+amount, 30, 12+18*i, col.getColorValue());
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/circuit_table.png");
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}