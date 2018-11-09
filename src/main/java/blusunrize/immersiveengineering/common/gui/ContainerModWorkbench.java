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
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ContainerModWorkbench extends ContainerIEBase<TileEntityModWorkbench>
{
	private final World world;
	public InventoryPlayer inventoryPlayer;
	private InventoryBlueprint inventoryBPoutput;
	public InventoryShader shaderInv;

	public ContainerModWorkbench(InventoryPlayer inventoryPlayer, World world, TileEntityModWorkbench tile)
	{
		super(inventoryPlayer, tile);
		this.world = world;
		this.inventoryPlayer = inventoryPlayer;
		rebindSlots();
	}

	private void bindPlayerInv(InventoryPlayer inventoryPlayer)
	{
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 87+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 145));
	}

	public void rebindSlots()
	{
		//Don't rebind if the tool didn't change
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			for(Slot slot : inventorySlots)
				if(slot instanceof IESlot.Upgrades)
					if(ItemStack.areItemsEqual(((IESlot.Upgrades)slot).upgradeableTool, inv.getStackInSlot(0)))
						return;
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();
		this.addSlotToContainer(new IESlot.ModWorkbench(this, this.inv, 0, 24, 22, 1));
		slotCount = 1;

		ItemStack tool = this.getSlot(0).getStack();
		if(tool.getItem() instanceof IUpgradeableTool)
		{
			IItemHandler handler = tool.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if(handler instanceof IEItemStackHandler)
				((IEItemStackHandler)handler).setTile(tile);
			Slot[] slots = ((IUpgradeableTool)tool.getItem()).getWorkbenchSlots(this, tool);
			if(slots!=null)
				for(Slot s : slots)
				{
					this.addSlotToContainer(s);
					slotCount++;
				}

			if(tool.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
			{
				ShaderWrapper wrapper = tool.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
				if(wrapper!=null)
				{
					this.shaderInv = new InventoryShader(this, wrapper);
					this.addSlotToContainer(new IESlot.Shader(this, shaderInv, 0, 130, 32, tool));
					slotCount++;
					this.shaderInv.shader = wrapper.getShaderItem();
				}
			}
		}
		else if(!(tool.getItem() instanceof IConfigurableTool))
		{
			boolean blueprint = false;
			if(tool.getItem() instanceof ItemEngineersBlueprint)
			{
				//Init the output inventory
				blueprint = true;
				BlueprintCraftingRecipe[] recipes = ((ItemEngineersBlueprint)tool.getItem()).getRecipes(tool);
				inventoryBPoutput = new InventoryBlueprint(this, recipes);

				//Add output slots
				for(int i = 0; i < recipes.length; i++)
				{
					int y = 21+(i < 9?i/3: (-(i-6)/3))*18;
					this.addSlotToContainer(new IESlot.BlueprintOutput(this, inventoryBPoutput, this.inv, i, 118+(i%3*18), y, recipes[i]));
					slotCount++;
				}
			}
			//Add input slots, these are always here if no tool is in
			for(int i = 0; i < 6; i++)
			{
				if(blueprint)
					this.addSlotToContainer(new IESlot.BlueprintInput(this, this.inv, this.inventoryBPoutput, i+1, i%2==0?74: 92, 21+(i/2)*18));
				else
					this.addSlotToContainer(new Slot(this.inv, i+1, i%2==0?74: 92, 21+(i/2)*18));
				slotCount++;
			}
		}
		bindPlayerInv(inventoryPlayer);
		ImmersiveEngineering.proxy.reInitGui();
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot slotObject = inventorySlots.get(slot);

		if(slotObject!=null&&slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount+36), true))
					return ItemStack.EMPTY;
			}
			else if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.getItem() instanceof ItemEngineersBlueprint)
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IUpgradeableTool&&((IUpgradeableTool)stackInSlot.getItem()).canModify(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(stackInSlot.getItem() instanceof IConfigurableTool&&((IConfigurableTool)stackInSlot.getItem()).canConfigure(stackInSlot))
				{
					if(!this.mergeItemStack(stackInSlot, 0, 1, true))
						return ItemStack.EMPTY;
				}
				else if(slotCount > 1)
				{
					boolean b = true;
					for(int i = 1; i < slotCount; i++)
					{
						Slot s = inventorySlots.get(i);
						if(s!=null&&s.isItemValid(stackInSlot))
							if(this.mergeItemStack(stackInSlot, i, i+1, true))
							{
								b = false;
								break;
							}
							else
								continue;
					}
					if(b)
						return ItemStack.EMPTY;
				}
			}

			if(stackInSlot.getCount()==0)
				slotObject.putStack(ItemStack.EMPTY);
			else
				slotObject.onSlotChanged();

			if(stackInSlot.getCount()==stack.getCount())
				return ItemStack.EMPTY;
			slotObject.onTake(player, stack);
		}
		return stack;
	}

	@Nonnull
	@Override
	public ItemStack slotClick(int id, int button, ClickType clickType, EntityPlayer player)
	{
		ItemStack ret = super.slotClick(id, button, clickType, player);
		tile.markContainingBlockForUpdate(null);
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
			detectAndSendChanges();
		return ret;
	}
}