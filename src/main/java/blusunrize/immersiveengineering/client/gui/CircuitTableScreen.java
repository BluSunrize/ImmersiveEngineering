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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonLogicCircuitRegister;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.client.gui.elements.GuiSelectingList;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableBlockEntity;
import blusunrize.immersiveengineering.common.gui.CircuitTableMenu;
import blusunrize.immersiveengineering.common.items.LogicCircuitBoardItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.common.blocks.wooden.CircuitTableBlockEntity.SLOT_TYPES;

public class CircuitTableScreen extends IEContainerScreen<CircuitTableMenu>
{
	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("circuit_table");

	// Buttons
	private GuiSelectingList operatorList;
	private final List<GuiButtonLogicCircuitRegister> inputButtons = new ArrayList<>(LogicCircuitOperator.TOTAL_MAX_INPUTS);
	private GuiButtonLogicCircuitRegister outputButton;
	private final Rect2i copyArea;

	private final ResettableLazy<Optional<LogicCircuitInstruction>> instruction = new ResettableLazy<>(() -> {
		LogicCircuitOperator operator = getSelectedOperator();
		if(operator==null)
			return Optional.empty();
		// collect inputs
		LogicCircuitRegister[] inputs = inputButtons.stream()
				.map(GuiButtonState::getState)
				.filter(Objects::nonNull)
				.limit(operator.getArgumentCount())
				.toArray(LogicCircuitRegister[]::new);
		// if input array is too short, can't make an instruction
		if(inputs.length < operator.getArgumentCount())
			return Optional.empty();
		// else, build instruction
		return Optional.of(new LogicCircuitInstruction(operator, outputButton.getState(), inputs));
	});

	public CircuitTableScreen(CircuitTableMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageWidth = 234;
		this.imageHeight = 182;
		this.copyArea = new Rect2i(52, 7, 48, 63);
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new EnergyInfoArea(leftPos+217, topPos+16, menu.energyStorage),
				new TooltipArea(copyArea, l -> {
					if(this.menu.getCarried().getItem() instanceof LogicCircuitBoardItem)
						l.add(TextUtils.applyFormat(
								Component.translatable(Lib.DESC_INFO+"circuit_table.copy"), ChatFormatting.GRAY
						));
				})
		);
	}

	@Override
	public void init()
	{
		super.init();

		this.operatorList = (GuiSelectingList)this.addRenderableWidget(new GuiSelectingList(leftPos+58, topPos+16, 36, 56, btn -> {
			this.minecraft.tell(this::updateButtons);
			this.minecraft.tell(this::updateInstruction);
		}, Arrays.stream(LogicCircuitOperator.values()).map(Enum::name).toArray(String[]::new)).setPadding(1, 1, 2, 0));

		this.outputButton = this.addRenderableWidget(GuiButtonLogicCircuitRegister.create(
				leftPos+121, topPos+56,
				Component.literal("Output"), btn -> this.minecraft.tell(this::updateInstruction))
		);
		this.updateButtons();
	}

	private LogicCircuitInstruction getEditInstruction()
	{
		return this.menu.slots.get(CircuitTableBlockEntity.getEditSlot()).getItem().get(IEDataComponents.CIRCUIT_INSTRUCTION);
	}

	@Nullable
	private LogicCircuitOperator getSelectedOperator()
	{
		LogicCircuitInstruction editInstr = getEditInstruction();
		if(editInstr!=null)
			return editInstr.getOperator();
		return LogicCircuitOperator.getByString(operatorList.getSelectedString());
	}

	private void updateInstruction()
	{
		this.instruction.reset();
		this.instruction.get().ifPresentOrElse(instr -> {
					this.menu.instruction = instr;
					sendUpdateToServer(instr.serialize());
				},
				() -> {
					this.menu.instruction = null;
					sendUpdateToServer(new CompoundTag());
				});
	}

	@Override
	protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType)
	{
		// withdrawing from edit slot, or quick-moving a circuit into it
		boolean editCircuit = pSlotId==CircuitTableBlockEntity.getEditSlot()||(
				pType==ClickType.QUICK_MOVE&&pSlotId >= this.menu.ownSlotCount
						&&pSlot!=null&&pSlot.getItem().is(IEItems.Misc.LOGIC_CIRCUIT_BOARD.get())
		);

		super.slotClicked(pSlot, pSlotId, pMouseButton, pType);

		if(editCircuit)
		{
			this.minecraft.tell(this::updateButtons);
			this.minecraft.tell(this::updateInstruction);
		}
	}

	private void updateButtons()
	{
		LogicCircuitOperator operator = getSelectedOperator();
		if(operator!=null)
		{
			int inputCount = operator.getArgumentCount();
			int inputStart = 130-(inputCount*10-1);
			this.inputButtons.forEach(this::removeWidget);
			this.inputButtons.clear();
			for(int i = 0; i < inputCount; i++)
				this.inputButtons.add(this.addRenderableWidget(GuiButtonLogicCircuitRegister.create(
						leftPos+inputStart+20*i, topPos+18,
						Component.literal("Input "+(i+1)), btn -> this.minecraft.tell(this::updateInstruction))
				));
		}
		LogicCircuitInstruction editInstr = getEditInstruction();
		if(editInstr!=null)
		{
			this.operatorList.active = false;
			this.operatorList.setSelectedString(editInstr.getOperator().name());
			for(int i = 0; i < editInstr.getInputs().length; i++)
				this.inputButtons.get(i).setState(editInstr.getInputs()[i].ordinal());
			this.outputButton.setState(editInstr.getOutput().ordinal());
		}
		else
			this.operatorList.active = true;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		if(this.hoveredSlot!=null&&!this.hoveredSlot.hasItem())
		{
			if(this.hoveredSlot.index < CircuitTableBlockEntity.getEditSlot())
			{
				int slotNum = this.hoveredSlot.index;
				addGray.accept(Component.translatable(Lib.DESC_INFO+"circuit_table.slot."+SLOT_TYPES[slotNum]));
			}
			else if(this.hoveredSlot.index==CircuitTableBlockEntity.getEditSlot())
				addGray.accept(Component.translatable(Lib.DESC_INFO+"circuit_table.slot.edit"));
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawCenteredString(this.font, Component.translatable(Lib.GUI_CONFIG+"circuit_table.operator"), 76, 4, DyeColor.LIGHT_GRAY.getTextColor());
		graphics.drawCenteredString(this.font, Component.translatable(Lib.GUI_CONFIG+"circuit_table.inputs"), 130, 8, DyeColor.LIGHT_GRAY.getTextColor());
		graphics.drawCenteredString(this.font, Component.translatable(Lib.GUI_CONFIG+"circuit_table.outputs"), 130, 42, DyeColor.LIGHT_GRAY.getTextColor());

		for(int i = 0; i < SLOT_TYPES.length; i++)
		{
			int amount = 0;
			DyeColor col = DyeColor.LIGHT_GRAY;
			if(this.instruction.get().isPresent()&&getEditInstruction()==null)
			{
				amount = CircuitTableBlockEntity.getIngredientAmount(this.instruction.get().get(), i);
				if(this.menu.slots.get(i).getItem().getCount() >= amount)
					col = DyeColor.GREEN;
				else
					col = DyeColor.RED;
			}
			graphics.drawString(this.font, "x "+amount, 30, 18+20*i, col.getTextColor());
		}
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers)
	{
		for(GuiButtonState<?> input : this.inputButtons)
			if(input.isHoveredOrFocused())
				return input.charTyped(codePoint, modifiers);
		if(this.outputButton.isHoveredOrFocused())
			return this.outputButton.charTyped(codePoint, modifiers);
		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(isMouseIn((int)mouseX, (int)mouseY, 52, 7, 100, 70)&&this.menu.getCarried().getItem() instanceof LogicCircuitBoardItem)
		{
			LogicCircuitInstruction instr = this.menu.getCarried().get(IEDataComponents.CIRCUIT_INSTRUCTION);
			if(instr!=null)
			{
				this.operatorList.setSelectedString(instr.getOperator().name());
				this.updateButtons();
				this.outputButton.setState(instr.getOutput().ordinal());
				LogicCircuitRegister[] inputs = instr.getInputs();
				for(int i = 0; i < inputs.length; i++)
					this.inputButtons.get(i).setState(inputs[i].ordinal());
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}