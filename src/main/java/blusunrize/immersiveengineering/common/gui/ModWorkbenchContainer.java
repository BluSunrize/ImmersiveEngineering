/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.upgrade.IUpgradeableTool;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.mixin.accessors.ContainerAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class ModWorkbenchContainer extends IEBaseContainerOld<ModWorkbenchBlockEntity>
{
	public static final int MAX_NUM_DYNAMIC_SLOTS = 36;
	public static final int OUTPUTS_PER_PAGE = 9;

	private final Level world;
	public Inventory inventoryPlayer;
	private BlueprintInventory inventoryBPoutput;
	public ShaderInventory shaderInv;
	private final ItemStackHandler clientInventory = new ItemStackHandler(MAX_NUM_DYNAMIC_SLOTS+1);

	private int pages = 0;
	private int currentPage = 0;

	public ModWorkbenchContainer(MenuType<?> type, int id, Inventory inventoryPlayer, ModWorkbenchBlockEntity tile)
	{
		super(type, tile, id);
		this.world = tile.getLevelNonnull();
		this.inventoryPlayer = inventoryPlayer;
		rebindSlots();
	}

	private void bindPlayerInv(Inventory inventoryPlayer)
	{
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}

	public void rebindSlots()
	{
		this.slots.clear();
		((ContainerAccess)this).getLastSlots().clear();
		((ContainerAccess)this).getRemoteSlots().clear();
		this.addSlot(new IESlot.ModWorkbench(this, this.inv, 0, 24, 22, 1));
		ownSlotCount = 1;

		ItemStack tool = this.getSlot(0).getItem();
		if(tool.getItem() instanceof IUpgradeableTool upgradeableTool)
		{
			IItemHandler toolInv = Objects.requireNonNull(tool.getCapability(ItemHandler.ITEM));

			// Use a "simple" inventory on the client rather than the one for the tool stack. The server always syncs an
			// "empty" tool to the client, so if the slots use the tool inventory the behavior become highly dependent
			// on slot update order
			Slot[] slots = upgradeableTool.getWorkbenchSlots(
					this, tool, world, () -> inventoryPlayer.player, world.isClientSide?clientInventory: toolInv
			);
			if(slots!=null)
				for(Slot s : slots)
				{
					this.addSlot(s);
					ownSlotCount++;
				}

			ShaderWrapper wrapper = tool.getCapability(CapabilityShader.ITEM);
			if(wrapper!=null)
			{
				this.shaderInv = new ShaderInventory(this, wrapper);
				this.addSlot(new IESlot.Shader(this, shaderInv, 0, 130, 32, tool));
				ownSlotCount++;
				this.shaderInv.shader = ShaderRegistry.makeShaderStack(wrapper.getShader());
			}
		}
		else if(!(tool.getItem() instanceof IConfigurableTool))
		{
			boolean blueprint = false;
			if(tool.getItem() instanceof EngineersBlueprintItem)
			{
				//Init the output inventory
				blueprint = true;
				List<RecipeHolder<BlueprintCraftingRecipe>> recipes = EngineersBlueprintItem.getRecipes(world, tool);
				inventoryBPoutput = new BlueprintInventory(this, recipes);
				this.currentPage = 0;
				this.pages = (recipes.size()/OUTPUTS_PER_PAGE)+1;

				//Add output slots
				for(int i = 0; i < recipes.size(); i++)
				{
					int y = //21+(i < 9?i/3: (-(i-6)/3))*18-(i>=9?12:0);
							21+(i%OUTPUTS_PER_PAGE)/3*18;
					this.addSlot(new IESlot.BlueprintOutput(
							this, inventoryBPoutput, this.inv, i, 118+(i%3*18), y, recipes.get(i).value()
					));
					ownSlotCount++;
				}
			}
			//Add input slots, these are always here if no tool is in
			for(int i = 0; i < 6; i++)
			{
				if(blueprint)
					this.addSlot(new IESlot.BlueprintInput(this, this.inv, this.inventoryBPoutput, i+1, i%2==0?74: 92, 21+(i/2)*18));
				else
					this.addSlot(new Slot(this.inv, i+1, i%2==0?74: 92, 21+(i/2)*18));
				ownSlotCount++;
			}
			if(inventoryBPoutput!=null)
				inventoryBPoutput.updateOutputs(inv);
		}
		// Add "useless" slots to keep the number of slots (and therefore the IDs of the player inventory slots)
		// constant. MC doesn't handle changing slot IDs well, causing desyncs
		for(; ownSlotCount < MAX_NUM_DYNAMIC_SLOTS; ++ownSlotCount)
			addSlot(new IESlot.AlwaysEmptySlot(this));
		bindPlayerInv(inventoryPlayer);
		ImmersiveEngineering.proxy.reInitGui();
	}

	public boolean isOutputSlotOnPage(IESlot.BlueprintOutput slot)
	{
		return slot.getSlotIndex() >= this.currentPage*OUTPUTS_PER_PAGE&&slot.getSlotIndex() < (this.currentPage+1)*OUTPUTS_PER_PAGE;
	}

	@Override
	public boolean clickMenuButton(Player player, int buttonId)
	{
		if(buttonId==1)
			this.currentPage = Math.floorMod(this.currentPage-1, this.pages);
		else
			this.currentPage = Math.floorMod(this.currentPage+1, this.pages);
		return true;
	}

	@Nonnull
	@Override
	// TODO this is broken when moving from an ISI slot to the player inv for some reason???
	//  I think we're modifying the stack we got from the slot, which doesn't work at all with components
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack resultStack = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject==null||!slotObject.hasItem())
			return resultStack;

		ItemStack stackInSlot = slotObject.getItem();
		resultStack = stackInSlot.copy();

		if(slot < ownSlotCount)
		{
			if(!this.moveItemStackTo(stackInSlot, ownSlotCount, (ownSlotCount+36), true))
				return ItemStack.EMPTY;
		}
		else if(!stackInSlot.isEmpty())
		{
			boolean singleSlot = ownSlotCount==1;
			if(stackInSlot.getItem() instanceof EngineersBlueprintItem
					||(stackInSlot.getItem() instanceof IUpgradeableTool uTool&&uTool.canModify(stackInSlot))
					||(stackInSlot.getItem() instanceof IConfigurableTool cTool&&cTool.canConfigure(stackInSlot)))
			{
				if(!this.moveItemStackTo(stackInSlot, 0, 1, false)&&singleSlot)
					return ItemStack.EMPTY;
			}

			if(!singleSlot)
			{
				if(!this.moveItemStackTo(stackInSlot, 1, ownSlotCount, false))
					return ItemStack.EMPTY;
			}
		}

		slotObject.setChanged();
		// TODO hack
		slotObject.set(stackInSlot);

		// TODO that looks weird as well
		if(stackInSlot.getCount()==resultStack.getCount())
			resultStack = ItemStack.EMPTY;
		slotObject.onTake(player, resultStack);
		if(slotObject.hasItem())
			player.drop(slotObject.getItem(), false);
		return resultStack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
	{
		// TODO I think super::moveItemStackTo just assumes it can mutate the stacks in slots and it will propagate
		//  correctly?
		return IEContainerMenu.moveItemStackToWithMayPlace(slots, super::moveItemStackTo, stack, startIndex, endIndex);
	}

	@Override
	public void clicked(int id, int dragType, ClickType clickType, Player player)
	{
		super.clicked(id, dragType, clickType, player);
		tile.markContainingBlockForUpdate(null);
		if(!world.isClientSide)
			broadcastChanges();
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot)
	{
		return pSlot.container!=this.inventoryBPoutput&&super.canTakeItemForPickAll(pStack, pSlot);
	}
}