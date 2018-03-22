/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEItems;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.*;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemBullet;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public abstract class IESlot extends Slot
{
	final Container container;
	public IESlot(Container container, IInventory inv, int id, int x, int y)
	{
		super(inv, id, x, y);
		this.container=container;
	}

	@Override
	public boolean isItemValid(ItemStack itemStack)
	{
		return true;
	}

	public static class Output extends IESlot
	{
		public Output(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return false;
		}
	}
	public static class FluidContainer extends IESlot
	{
		boolean empty;
		public FluidContainer(Container container, IInventory inv, int id, int x, int y, boolean empty)
		{
			super(container, inv, id, x, y);
			this.empty=empty;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			IFluidHandler handler = FluidUtil.getFluidHandler(itemStack);
			if(handler == null || handler.getTankProperties() == null)
				return false;
			IFluidTankProperties[] tank = handler.getTankProperties();
			if(tank == null || tank.length < 1 || tank[0] == null)
				return false;
			if(empty)
				return tank[0].getContents() == null;
			else
				return tank[0].getContents() != null;
		}
	}
	public static class BlastFuel extends IESlot
	{
		public BlastFuel(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return BlastFurnaceRecipe.isValidBlastFuel(itemStack);
		}
	}
	public static class Bullet extends SlotItemHandler
	{
		int limit;
		public Bullet(IItemHandler inv, int id, int x, int y, int limit)
		{
			super(inv, id, x, y);
			this.limit=limit;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBullet;
		}
		@Override
		public int getSlotStackLimit()
		{
			return limit;
		}

		@Override
		public int getItemStackLimit(@Nonnull ItemStack stack)
		{
			return limit;
		}
	}
	public static class DrillHead extends SlotItemHandler
	{
		public DrillHead(IItemHandler inv, int id, int x, int y)
		{
			super(inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && itemStack.getItem() instanceof IDrillHead;
		}
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}
	public static class Upgrades extends SlotItemHandler
	{
		ItemStack upgradeableTool;
		String type;
		boolean preventDoubles;
		Container container;
		public Upgrades(Container container, IItemHandler inv, int id, int x, int y, String type, ItemStack upgradeableTool, boolean preventDoubles)
		{
			super(inv, id, x, y);
			this.container = container;
			this.type = type;
			this.upgradeableTool = upgradeableTool;
			this.preventDoubles = preventDoubles;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(preventDoubles)
				for(Slot slot : container.inventorySlots)
					if(slot instanceof Upgrades && ((Upgrades)slot).preventDoubles && OreDictionary.itemMatches(slot.getStack(), itemStack, true))
						return false;
			return !itemStack.isEmpty() && itemStack.getItem() instanceof IUpgrade && ((IUpgrade)itemStack.getItem()).getUpgradeTypes(itemStack).contains(type) && ((IUpgrade)itemStack.getItem()).canApplyUpgrades(upgradeableTool, itemStack);
		}
		@Override
		public int getSlotStackLimit()
		{
			return 64;
		}

		@Override
		public void onSlotChanged()
		{
			((IUpgradeableTool)upgradeableTool.getItem()).recalculateUpgrades(upgradeableTool);
		}
	}
	public static class Shader extends IESlot
	{
		ItemStack tool;
		public Shader(Container container, IInventory inv, int id, int x, int y, ItemStack tool)
		{
			super(container, inv, id, x, y);
			this.tool = tool;
			if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
				this.setBackgroundName("immersiveengineering:items/shader_slot");
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(itemStack.isEmpty() || !(itemStack.getItem() instanceof IShaderItem) || tool.isEmpty() || !tool.hasCapability(CapabilityShader.SHADER_CAPABILITY,null))
				return false;
			ShaderWrapper wrapper = tool.getCapability(CapabilityShader.SHADER_CAPABILITY,null);
			if(wrapper==null)
				return false;
			return ((IShaderItem)itemStack.getItem()).getShaderCase(itemStack, tool, wrapper.getShaderType())!=null;
		}
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}
	public static class UpgradeableItem extends IESlot
	{
		int size;
		public UpgradeableItem(Container container, IInventory inv, int id, int x, int y, int size)
		{
			super(container, inv, id, x, y);
			this.size = size;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(itemStack.isEmpty())
				return false;
			if(itemStack.getItem() instanceof IUpgradeableTool)
				return ((IUpgradeableTool)itemStack.getItem()).canModify(itemStack);
			if(itemStack.getItem() instanceof IConfigurableTool)
				return ((IConfigurableTool)itemStack.getItem()).canConfigure(itemStack);
			return false;
		}
		@Override
		public int getSlotStackLimit()
		{
			return size;
		}
		@Override
		public void onSlotChanged()
		{
			super.onSlotChanged();
			if(container instanceof ContainerModWorkbench)
				((ContainerModWorkbench)container).rebindSlots();
		}
		@Override
		public boolean canTakeStack(EntityPlayer player)
		{
			return !(!this.getStack().isEmpty() && getStack().getItem() instanceof IUpgradeableTool && !((IUpgradeableTool) getStack().getItem()).canTakeFromWorkbench(getStack()));
		}
		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack)
		{
			ItemStack result = super.onTake(player, stack);
			if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeableTool)
				((IUpgradeableTool) stack.getItem()).removeFromWorkbench(player, stack);
			IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (handler instanceof IEItemStackHandler)
				((IEItemStackHandler) handler).setTile(null);
			return result;
		}
	}
	public static class AutoBlueprint extends IESlot
	{
		public AutoBlueprint(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && itemStack.getItem() instanceof ItemEngineersBlueprint;
		}
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
		@Override
		public void onSlotChanged()
		{
			super.onSlotChanged();
			if(container instanceof ContainerAutoWorkbench)
				ImmersiveEngineering.proxy.reInitGui();
//				((ContainerAutoWorkbench)container).rebindSlots();
		}
	}
	public static class Ghost extends IESlot
	{
		public Ghost(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public void putStack(ItemStack itemStack)
		{
			super.putStack(itemStack);
		}
		@Override
		public boolean canTakeStack(EntityPlayer player)
		{
			return false;
		}
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}
	public static class ItemDisplay extends IESlot
	{
		public ItemDisplay(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return false;
		}
		@Override
		public boolean canTakeStack(EntityPlayer player)
		{
			return false;
		}
	}

	public static class BlueprintInput extends SlotItemHandler
	{
		ItemStack upgradeableTool;
		Container container;
		public BlueprintInput(Container container, IItemHandler inv, int id, int x, int y, ItemStack upgradeableTool)
		{
			super(inv, id, x, y);
			this.upgradeableTool = upgradeableTool;
		}
		@Override
		public void onSlotChanged()
		{
			if(!upgradeableTool.isEmpty() && upgradeableTool.getItem() instanceof ItemEngineersBlueprint)
				((ItemEngineersBlueprint)upgradeableTool.getItem()).updateOutputs(upgradeableTool);
			if(container instanceof ContainerModWorkbench)
				((ContainerModWorkbench)container).rebindSlots();
			super.onSlotChanged();
		}
		@Override
		public int getSlotStackLimit()
		{
			return 64;
		}
	}
	public static class BlueprintOutput extends SlotItemHandler
	{
		public BlueprintCraftingRecipe recipe;
		ItemStack upgradeableTool;
		Container container;
		public BlueprintOutput(Container container, IItemHandler inv, int id, int x, int y, ItemStack upgradeableTool, BlueprintCraftingRecipe recipe)
		{
			super(inv, id, x, y);
			this.container = container;
			this.upgradeableTool = upgradeableTool;
			this.recipe = recipe;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return false;
		}


		@Override
		@SideOnly(Side.CLIENT)
		public boolean isEnabled()
		{
			return this.getHasStack();
		}

		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack)
		{
			if(!upgradeableTool.isEmpty() && upgradeableTool.getItem() instanceof ItemEngineersBlueprint)
			{
				((ItemEngineersBlueprint)upgradeableTool.getItem()).reduceInputs(recipe, upgradeableTool, stack, this.container);
				((ItemEngineersBlueprint)upgradeableTool.getItem()).updateOutputs(upgradeableTool);
			}
			this.inventory.markDirty();
			return super.onTake(player, stack);
		}
	}

	public static class ArcInput extends IESlot
	{
		public ArcInput(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && ArcFurnaceRecipe.isValidRecipeInput(itemStack);
		}
	}
	public static class ArcAdditive extends IESlot
	{
		public ArcAdditive(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && ArcFurnaceRecipe.isValidRecipeAdditive(itemStack);
		}
	}
	public static class ArcElectrode extends IESlot
	{
		public ArcElectrode(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && IEItems.graphiteElectrode.equals(itemStack.getItem());
		}
	}

	public static class Belljar extends IESlot
	{
		int type = 0;
		public Belljar(int type, Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
			this.type = type;
		}

		@Override
		public int getSlotStackLimit()
		{
			return type<2?1:64;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty() && (type==1?BelljarHandler.getHandler(itemStack)!=null: type!=2||BelljarHandler.getItemFertilizerHandler(itemStack)!=null);
		}
	}

	public static class ContainerCallback extends SlotItemHandler
	{
		Container container;
		public ContainerCallback(Container container, IItemHandler inv, int id, int x, int y)
		{
			super(inv, id, x, y);
			this.container = container;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(this.container instanceof ICallbackContainer)
				return ((ICallbackContainer)this.container).canInsert(itemStack, slotNumber, this);
			return true;
		}
		@Override
		public boolean canTakeStack(EntityPlayer player)
		{
			if(this.container instanceof ICallbackContainer)
				return ((ICallbackContainer)this.container).canTake(this.getStack(), slotNumber, this);
			return true;
		}
	}
	public interface ICallbackContainer
	{
		boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject);
		boolean canTake(ItemStack stack, int slotNumer, Slot slotObject);
	}
}