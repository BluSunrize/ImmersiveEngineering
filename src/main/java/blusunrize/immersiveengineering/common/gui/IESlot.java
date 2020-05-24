/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheTileEntity.SLOT_FERTILIZER;

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

	public static class IEFurnaceSFuelSlot extends IESlot
	{

		public IEFurnaceSFuelSlot(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		public boolean isItemValid(ItemStack p_75214_1_)
		{
			return AbstractFurnaceTileEntity.isFuel(p_75214_1_)||isBucket(p_75214_1_);
		}

		public int getItemStackLimit(ItemStack p_178170_1_)
		{
			return isBucket(p_178170_1_)?1: super.getItemStackLimit(p_178170_1_);
		}

		public static boolean isBucket(ItemStack p_178173_0_)
		{
			return p_178173_0_.getItem()==Items.BUCKET;
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
				if(handler.getTanks() <= 0)
					return false;

				if(filter==1)
					return handler.getFluidInTank(0).isEmpty();
				else if(filter==2)
					return !handler.getFluidInTank(0).isEmpty();
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
			return BlastFurnaceFuel.isValidBlastFuel(itemStack);
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
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof BulletItem;
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

	public static class WithPredicate extends SlotItemHandler
	{
		final Predicate<ItemStack> predicate;
		public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate)
		{
			super(inv, id, x, y);
			this.predicate = predicate;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&this.predicate.test(itemStack);
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
			this.setBackground(PlayerContainer.LOCATION_BLOCKS_TEXTURE, new ResourceLocation("immersiveengineering", "item/shader_slot"));
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

		public ModWorkbench(ModWorkbenchContainer container, IInventory inv, int id, int x, int y, int size)
		{
			super(container, inv, id, x, y);
			this.size = size;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(itemStack.isEmpty())
				return false;
			if(itemStack.getItem() instanceof EngineersBlueprintItem)
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
			if(container instanceof ModWorkbenchContainer)
				((ModWorkbenchContainer)container).rebindSlots();
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
		public Maintenance(MaintenanceKitContainer container, IInventory inv, int id, int x, int y)
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
			if(container instanceof MaintenanceKitContainer)
				((MaintenanceKitContainer)container).updateSlots();
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
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof EngineersBlueprintItem;
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
			if(container instanceof AutoWorkbenchContainer)
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
		private final BlueprintInventory outputInventory;

		public BlueprintInput(Container container, IInventory inv, BlueprintInventory outputInventory, int id, int x, int y)
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

		public BlueprintOutput(Container container, BlueprintInventory inv, IInventory inputInventory, int id, int x, int y, BlueprintCraftingRecipe recipe)
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
			((BlueprintInventory)this.inventory).reduceIputs(this.inputInventory, recipe, stack);
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
			return !itemStack.isEmpty()&&Misc.graphiteElectrode.equals(itemStack.getItem());
		}
	}

	public static class Cloche extends IESlot
	{
		int type = 0;

		public Cloche(int type, Container container, IInventory inv, int id, int x, int y)
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
			if(type==SLOT_FERTILIZER)
				return ClocheRecipe.isValidFertilizer(itemStack);
			return true;
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