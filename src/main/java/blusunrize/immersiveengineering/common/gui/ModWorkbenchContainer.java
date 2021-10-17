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
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.BlueprintOutput;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import blusunrize.immersiveengineering.mixin.accessors.ContainerAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ModWorkbenchContainer extends IEBaseContainer<ModWorkbenchTileEntity>
{
	public static final int MAX_NUM_DYNAMIC_SLOTS = 20;

	private final Level world;
	public Inventory inventoryPlayer;
	private BlueprintInventory inventoryBPoutput;
	public ShaderInventory shaderInv;
	private final ItemStackHandler clientInventory = new ItemStackHandler(MAX_NUM_DYNAMIC_SLOTS+1);

	public ModWorkbenchContainer(int id, Inventory inventoryPlayer, ModWorkbenchTileEntity tile)
	{
		super(tile, id);
		this.world = tile.getWorldNonnull();
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
		this.addSlot(new IESlot.ModWorkbench(this, this.inv, 0, 24, 22, 1));
		slotCount = 1;

		ItemStack tool = this.getSlot(0).getItem();
		if(tool.getItem() instanceof IUpgradeableTool)
		{
			final IUpgradeableTool upgradeableTool = (IUpgradeableTool)tool.getItem();
			IItemHandler toolInv = tool.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(RuntimeException::new);
			if(toolInv instanceof IEItemStackHandler)
				((IEItemStackHandler) toolInv).setTile(tile);

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
					slotCount++;
				}

			tool.getCapability(CapabilityShader.SHADER_CAPABILITY, null).ifPresent(wrapper ->
			{
				this.shaderInv = new ShaderInventory(this, wrapper);
				this.addSlot(new IESlot.Shader(this, shaderInv, 0, 130, 32, tool));
				slotCount++;
				this.shaderInv.shader = wrapper.getShaderItem();
			});
		}
		else if(!(tool.getItem() instanceof IConfigurableTool))
		{
			boolean blueprint = false;
			if(tool.getItem() instanceof EngineersBlueprintItem)
			{
				//Init the output inventory
				blueprint = true;
				BlueprintCraftingRecipe[] recipes = EngineersBlueprintItem.getRecipes(tool);
				inventoryBPoutput = new BlueprintInventory(this, recipes);

				//Add output slots
				for(int i = 0; i < recipes.length; i++)
				{
					int y = 21+(i < 9?i/3: (-(i-6)/3))*18;
					this.addSlot(new IESlot.BlueprintOutput(this, inventoryBPoutput, this.inv, i, 118+(i%3*18), y, recipes[i]));
					slotCount++;
				}
			}
			//Add input slots, these are always here if no tool is in
			for(int i = 0; i < 6; i++)
			{
				if(blueprint)
					this.addSlot(new IESlot.BlueprintInput(this, this.inv, this.inventoryBPoutput, i+1, i%2==0?74: 92, 21+(i/2)*18));
				else
					this.addSlot(new Slot(this.inv, i+1, i%2==0?74: 92, 21+(i/2)*18));
				slotCount++;
			}
			if(inventoryBPoutput!=null)
				inventoryBPoutput.updateOutputs(inv);
		}
		// Add "useless" slots to keep the number of slots (and therefore the IDs of the player inventory slots)
		// constant. MC doesn't handle changing slot IDs well, causing desyncs
		for(; slotCount < MAX_NUM_DYNAMIC_SLOTS; ++slotCount)
			addSlot(new IESlot.AlwaysEmptySlot(this));
		bindPlayerInv(inventoryPlayer);
		ImmersiveEngineering.proxy.reInitGui();
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(Player player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = slots.get(slot);

		if(slotObject!=null&&slotObject.hasItem())
		{
			ItemStack stackInSlot = slotObject.getItem();
			stack = stackInSlot.copy();

			if(slot < slotCount)
			{
				if(!this.moveItemStackTo(stackInSlot, slotCount, (slotCount+36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof EngineersBlueprintItem)
				{
					if(!this.moveItemStackTo(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IUpgradeableTool&&((IUpgradeableTool)stackInSlot.getItem()).canModify(stackInSlot))
				{
					if(!this.moveItemStackTo(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IConfigurableTool&&((IConfigurableTool)stackInSlot.getItem()).canConfigure(stackInSlot))
				{
					if(!this.moveItemStackTo(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(slotCount > 1)
				{
					boolean b = true;
					for(int i = 1; i < slotCount; i++)
					{
						Slot s = slots.get(i);
						if(s!=null&&s.mayPlace(stackInSlot))
							if(this.moveItemStackTo(stackInSlot, i, i+1, true))
							{
								b = false;
								break;
							}
					}
					if(b)
						return ItemStack.EMPTY;
				}
			}

			if(stackInSlot.getCount()==0)
				slotObject.set(ItemStack.EMPTY);
			else
				slotObject.setChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			ItemStack remainderStack = slotObject.onTake(player, stackInSlot);
			if(slotObject instanceof BlueprintOutput)
				player.drop(remainderStack, false);
		}
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack clicked(int id, int dragType, ClickType clickType, Player player)
	{
		ItemStack ret = super.clicked(id, dragType, clickType, player);
		tile.markContainingBlockForUpdate(null);
		if(!world.isClientSide)
			broadcastChanges();
		return ret;
	}
}