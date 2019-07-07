/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.*;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemBullet;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar.SLOT_SEED;
import static blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar.SLOT_SOIL;

public abstract class IESlot extends Slot
{
	final Container container;

	public IESlot(Container container, IInventory inv, int id, int x, int y)
	{
		super(inv, id, x, y);
		this.container = container;
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
		int filter; //0 = any, 1 = empty, 2 = full

		public FluidContainer(Container container, IInventory inv, int id, int x, int y, int filter)
		{
			super(container, inv, id, x, y);
			this.filter = filter;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			LazyOptional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(itemStack);
			return handlerCap.map(handler -> {
				if(handler.getTankProperties()==null)
					return false;
				IFluidTankProperties[] tank = handler.getTankProperties();
				if(tank==null||tank.length < 1||tank[0]==null)
					return false;

				if(filter==1)
					return tank[0].getContents()==null;
				else if(filter==2)
					return tank[0].getContents()!=null;
				return true;
			}).orElse(false);
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
			this.limit = limit;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof ItemBullet;
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
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof IDrillHead;
		}

		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}

	public static class Upgrades extends SlotItemHandler
	{
		final ItemStack upgradeableTool;
		final String type;
		final boolean preventDoubles;
		final Container container;
		final Supplier<World> getWorld;

		public Upgrades(Container container, IItemHandler inv, int id, int x, int y, String type, ItemStack upgradeableTool,
						boolean preventDoubles, Supplier<World> getWorld)
		{
			super(inv, id, x, y);
			this.container = container;
			this.type = type;
			this.upgradeableTool = upgradeableTool;
			this.preventDoubles = preventDoubles;
			this.getWorld = getWorld;
		}

		@Override
		public boolean isItemValid(@Nonnull ItemStack itemStack)
		{
			if(preventDoubles)
				for(Slot slot : container.inventorySlots)
					if(slot instanceof Upgrades&&((Upgrades)slot).preventDoubles&&ItemStack.areItemsEqual(slot.getStack(), itemStack))
						return false;
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof IUpgrade&&((IUpgrade)itemStack.getItem()).getUpgradeTypes(itemStack).contains(type)&&((IUpgrade)itemStack.getItem()).canApplyUpgrades(upgradeableTool, itemStack);
		}

		@Override
		public int getSlotStackLimit()
		{
			return 64;
		}

		@Override
		public void onSlotChanged()
		{
			((IUpgradeableTool)upgradeableTool.getItem()).recalculateUpgrades(upgradeableTool, getWorld.get());
		}
	}

	public static class Shader extends IESlot
	{
		ItemStack tool;

		public Shader(Container container, IInventory inv, int id, int x, int y, ItemStack tool)
		{
			super(container, inv, id, x, y);
			this.tool = tool;
			this.setBackgroundName("immersiveengineering:items/shader_slot");
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(itemStack.isEmpty()||!(itemStack.getItem() instanceof IShaderItem)||tool.isEmpty())
				return false;
			return tool.getCapability(CapabilityShader.SHADER_CAPABILITY).map(
					wrapper -> ((IShaderItem)itemStack.getItem()).getShaderCase(itemStack, tool, wrapper.getShaderType())!=null
			).orElse(false);
		}

		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}

	public static class ModWorkbench extends IESlot
	{
		int size;

		public ModWorkbench(ContainerModWorkbench container, IInventory inv, int id, int x, int y, int size)
		{
			super(container, inv, id, x, y);
			this.size = size;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(itemStack.isEmpty())
				return false;
			if(itemStack.getItem() instanceof ItemEngineersBlueprint)
				return true;
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
		public boolean canTakeStack(PlayerEntity player)
		{
			return !(!this.getStack().isEmpty()&&getStack().getItem() instanceof IUpgradeableTool&&!((IUpgradeableTool)getStack().getItem()).canTakeFromWorkbench(getStack()));
		}

		@Override
		public ItemStack onTake(PlayerEntity player, ItemStack stack)
		{
			ItemStack result = super.onTake(player, stack);
			if(!stack.isEmpty()&&stack.getItem() instanceof IUpgradeableTool)
				((IUpgradeableTool)stack.getItem()).removeFromWorkbench(player, stack);
			stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler -> {
				if(handler instanceof IEItemStackHandler)
					((IEItemStackHandler)handler).setTile(null);
			});
			return result;
		}
	}

	public static class Maintenance extends IESlot
	{
		public Maintenance(ContainerMaintenanceKit container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
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
		public void onSlotChanged()
		{
			super.onSlotChanged();
			if(container instanceof ContainerMaintenanceKit)
				((ContainerMaintenanceKit)container).updateSlots();
		}

		@Override
		public boolean canTakeStack(PlayerEntity player)
		{
			return !(!this.getStack().isEmpty()&&getStack().getItem() instanceof IUpgradeableTool&&!((IUpgradeableTool)getStack().getItem()).canTakeFromWorkbench(getStack()));
		}

		@Override
		public ItemStack onTake(PlayerEntity player, ItemStack stack)
		{
			ItemStack result = super.onTake(player, stack);
			if(!stack.isEmpty()&&stack.getItem() instanceof IUpgradeableTool)
				((IUpgradeableTool)stack.getItem()).removeFromWorkbench(player, stack);
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
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof ItemEngineersBlueprint;
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
		public boolean canTakeStack(PlayerEntity player)
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
		public boolean canTakeStack(PlayerEntity player)
		{
			return false;
		}
	}

	public static class BlueprintInput extends IESlot
	{
		private final InventoryBlueprint outputInventory;

		public BlueprintInput(Container container, IInventory inv, InventoryBlueprint outputInventory, int id, int x, int y)
		{
			super(container, inv, id, x, y);
			this.outputInventory = outputInventory;
		}

		@Override
		public void onSlotChanged()
		{
			outputInventory.updateOutputs(this.inventory);
			super.onSlotChanged();
		}
	}

	public static class BlueprintOutput extends IESlot
	{
		private final IInventory inputInventory;
		public final BlueprintCraftingRecipe recipe;

		public BlueprintOutput(Container container, InventoryBlueprint inv, IInventory inputInventory, int id, int x, int y, BlueprintCraftingRecipe recipe)
		{
			super(container, inv, id, x, y);
			this.inputInventory = inputInventory;
			this.recipe = recipe;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return false;
		}


		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean isEnabled()
		{
			return this.getHasStack();
		}

		@Override
		public ItemStack onTake(PlayerEntity player, ItemStack stack)
		{
			((InventoryBlueprint)this.inventory).reduceIputs(this.inputInventory, recipe, stack);
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
			return !itemStack.isEmpty()&&ArcFurnaceRecipe.isValidRecipeInput(itemStack);
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
			return !itemStack.isEmpty()&&ArcFurnaceRecipe.isValidRecipeAdditive(itemStack);
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
			return !itemStack.isEmpty()&&IEContent.itemGraphiteElectrode.equals(itemStack.getItem());
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
			return type < 2?1: 64;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(itemStack.isEmpty())
				return false;
			if(type==SLOT_SEED)
				return BelljarHandler.getHandler(itemStack)!=null;
			else
				return type==SLOT_SOIL||BelljarHandler.getItemFertilizerHandler(itemStack)!=null;
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
		public boolean canTakeStack(PlayerEntity player)
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