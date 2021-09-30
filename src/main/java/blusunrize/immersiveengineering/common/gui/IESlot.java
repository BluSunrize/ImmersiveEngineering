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
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.inventory.EmptyContainer;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity.SLOT_FERTILIZER;

// TODO test various onTake implementations. May need to move to tryRemove
public abstract class IESlot extends Slot
{
	final AbstractContainerMenu containerMenu;

	public IESlot(AbstractContainerMenu containerMenu, Container inv, int id, int x, int y)
	{
		super(inv, id, x, y);
		this.containerMenu = containerMenu;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack)
	{
		return true;
	}

	public static class Output extends IESlot
	{
		public Output(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return false;
		}
	}

	public static class IEFurnaceSFuelSlot extends IESlot
	{

		public IEFurnaceSFuelSlot(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		public boolean mayPlace(ItemStack stack)
		{
			return AbstractFurnaceBlockEntity.isFuel(stack)||isBucket(stack);
		}

		public int getMaxStackSize(ItemStack stack)
		{
			return isBucket(stack)?1: super.getMaxStackSize(stack);
		}

		public static boolean isBucket(ItemStack stack)
		{
			return stack.getItem()==Items.BUCKET;
		}

	}

	public static class FluidContainer extends IESlot
	{
		int filter; //0 = any, 1 = empty, 2 = full

		public FluidContainer(AbstractContainerMenu container, Container inv, int id, int x, int y, int filter)
		{
			super(container, inv, id, x, y);
			this.filter = filter;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
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
		public BlastFuel(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
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
		public boolean mayPlace(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof BulletItem;
		}

		@Override
		public int getMaxStackSize()
		{
			return limit;
		}

		@Override
		public int getMaxStackSize(@Nonnull ItemStack stack)
		{
			return limit;
		}
	}

	public static class WithPredicate extends SlotItemHandler
	{
		final Predicate<ItemStack> predicate;
		final Consumer<ItemStack> onChange;

		public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate)
		{
			this(inv, id, x, y, predicate, s -> {
			});
		}

		public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate, Consumer<ItemStack> onChange)
		{
			super(inv, id, x, y);
			this.predicate = predicate;
			this.onChange = onChange;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&this.predicate.test(itemStack);
		}

		@Override
		public int getMaxStackSize()
		{
			return 1;
		}

		@Override
		public void setChanged()
		{
			super.setChanged();
			onChange.accept(getItem());
		}
	}

	public static class Upgrades extends SlotItemHandler
	{
		final ItemStack toolStack;
		private final IUpgradeableTool upgradeableTool;
		private final String type;
		private final boolean preventDoubles;
		private final AbstractContainerMenu container;
		private final Level world;
		private final Supplier<Player> getPlayer;

		public Upgrades(AbstractContainerMenu container, IItemHandler inv, int id, int x, int y, String type, ItemStack toolStack,
						boolean preventDoubles, Level world, Supplier<Player> getPlayer)
		{
			super(inv, id, x, y);
			this.container = container;
			this.type = type;
			this.toolStack = toolStack;
			this.upgradeableTool = (IUpgradeableTool)toolStack.getItem();
			this.preventDoubles = preventDoubles;
			this.world = world;
			this.getPlayer = getPlayer;
		}

		@Override
		public boolean mayPlace(@Nonnull ItemStack itemStack)
		{
			if(preventDoubles)
				for(Slot slot : container.slots)
					if(this!=slot&&slot instanceof Upgrades&&ItemStack.isSame(slot.getItem(), itemStack))
						return false;
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof IUpgrade&&((IUpgrade)itemStack.getItem()).getUpgradeTypes(itemStack).contains(type)&&((IUpgrade)itemStack.getItem()).canApplyUpgrades(toolStack, itemStack);
		}

		@Override
		public int getMaxStackSize()
		{
			return 64;
		}

		@Nonnull
		@Override
		public ItemStack getItem()
		{
			return upgradeableTool.getUpgradeAfterRemoval(toolStack, super.getItem());
		}

		@Override
		public void onTake(Player thePlayer, ItemStack stack)
		{
			if(!world.isClientSide)
				upgradeableTool.removeUpgrade(toolStack, thePlayer, stack);
			super.onTake(thePlayer, stack);
		}

		@Override
		public void setChanged()
		{
			super.setChanged();
			if(!world.isClientSide)
			{
				upgradeableTool.recalculateUpgrades(toolStack, world, getPlayer.get());
				if(container instanceof ModWorkbenchContainer)
					((ModWorkbenchContainer)container).rebindSlots();
				else if(container instanceof MaintenanceKitContainer)
					((MaintenanceKitContainer)container).updateSlots();
			}
		}
	}

	public static class Shader extends IESlot
	{
		ItemStack tool;

		public Shader(AbstractContainerMenu container, Container inv, int id, int x, int y, ItemStack tool)
		{
			super(container, inv, id, x, y);
			this.tool = tool;
			this.setBackground(InventoryMenu.BLOCK_ATLAS, ImmersiveEngineering.rl("item/shader_slot"));
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			if(itemStack.isEmpty()||!(itemStack.getItem() instanceof IShaderItem)||tool.isEmpty())
				return false;
			return tool.getCapability(CapabilityShader.SHADER_CAPABILITY).map(
					wrapper -> ((IShaderItem)itemStack.getItem()).getShaderCase(itemStack, wrapper.getShaderType())!=null
			).orElse(false);
		}

		@Override
		public int getMaxStackSize()
		{
			return 1;
		}
	}

	public static class ModWorkbench extends IESlot
	{
		int size;

		public ModWorkbench(ModWorkbenchContainer container, Container inv, int id, int x, int y, int size)
		{
			super(container, inv, id, x, y);
			this.size = size;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
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
		public int getMaxStackSize()
		{
			return size;
		}

		@Override
		public void setChanged()
		{
			super.setChanged();
			if(containerMenu instanceof ModWorkbenchContainer workbench)
				workbench.rebindSlots();
		}

		@Override
		public boolean mayPickup(Player player)
		{
			return !(getItem().getItem() instanceof IUpgradeableTool tool&&!tool.canTakeFromWorkbench(getItem()));
		}

		@Override
		public void onTake(Player player, ItemStack stack)
		{
			super.onTake(player, stack);
			if(!stack.isEmpty()&&stack.getItem() instanceof IUpgradeableTool upgradeableTool)
				upgradeableTool.removeFromWorkbench(player, stack);
			stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
				if(handler instanceof IEItemStackHandler ieHandler)
					ieHandler.setTile(null);
			});
		}
	}

	public static class Maintenance extends IESlot
	{
		public Maintenance(MaintenanceKitContainer container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
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
		public void setChanged()
		{
			super.setChanged();
			if(containerMenu instanceof MaintenanceKitContainer)
				((MaintenanceKitContainer)containerMenu).updateSlots();
		}

		@Override
		public boolean mayPickup(Player player)
		{
			return !(!this.getItem().isEmpty()&&getItem().getItem() instanceof IUpgradeableTool&&!((IUpgradeableTool)getItem().getItem()).canTakeFromWorkbench(getItem()));
		}

		@Override
		public void onTake(Player player, ItemStack stack)
		{
			super.onTake(player, stack);
			if(!stack.isEmpty()&&stack.getItem() instanceof IUpgradeableTool)
				((IUpgradeableTool)stack.getItem()).removeFromWorkbench(player, stack);
		}
	}

	public static class AutoBlueprint extends IESlot
	{
		public AutoBlueprint(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&itemStack.getItem() instanceof EngineersBlueprintItem;
		}

		@Override
		public int getMaxStackSize()
		{
			return 1;
		}

		@Override
		public void setChanged()
		{
			super.setChanged();
			if(containerMenu instanceof AutoWorkbenchContainer)
				ImmersiveEngineering.proxy.reInitGui();
		}
	}

	public static class ItemHandlerGhost extends SlotItemHandler
	{

		public ItemHandlerGhost(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPickup(Player playerIn)
		{
			return false;
		}
	}

	public static class ItemDisplay extends IESlot
	{
		public ItemDisplay(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return false;
		}

		@Override
		public boolean mayPickup(Player player)
		{
			return false;
		}
	}

	public static class BlueprintInput extends IESlot
	{
		private final BlueprintInventory outputInventory;

		public BlueprintInput(AbstractContainerMenu container, Container inv, BlueprintInventory outputInventory, int id, int x, int y)
		{
			super(container, inv, id, x, y);
			this.outputInventory = outputInventory;
		}

		@Override
		public void setChanged()
		{
			outputInventory.updateOutputs(this.container);
			super.setChanged();
		}
	}

	public static class BlueprintOutput extends IESlot
	{
		private final Container inputInventory;
		public final BlueprintCraftingRecipe recipe;

		public BlueprintOutput(AbstractContainerMenu container, BlueprintInventory inv, Container inputInventory, int id, int x, int y, BlueprintCraftingRecipe recipe)
		{
			super(container, inv, id, x, y);
			this.inputInventory = inputInventory;
			this.recipe = recipe;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return false;
		}


		@Override
		public boolean isActive()
		{
			return this.hasItem();
		}

		@Override
		public void onTake(Player player, ItemStack stack)
		{
			((BlueprintInventory)this.container).reduceIputs(this.inputInventory, recipe, stack);
			super.onTake(player, stack);
		}
	}

	public static class ArcInput extends IESlot
	{
		public ArcInput(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&ArcFurnaceRecipe.isValidRecipeInput(itemStack);
		}
	}

	public static class ArcAdditive extends IESlot
	{
		public ArcAdditive(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return !itemStack.isEmpty()&&ArcFurnaceRecipe.isValidRecipeAdditive(itemStack);
		}
	}

	public static class ArcElectrode extends IESlot
	{
		public ArcElectrode(AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public int getMaxStackSize()
		{
			return 1;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return Misc.GRAPHITE_ELECTRODE.get().equals(itemStack.getItem());
		}
	}

	public static class Cloche extends IESlot
	{
		int type = 0;

		public Cloche(int type, AbstractContainerMenu container, Container inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
			this.type = type;
		}

		@Override
		public int getMaxStackSize()
		{
			return type < 2?1: 64;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			if(itemStack.isEmpty())
				return false;
			if(type==SLOT_FERTILIZER)
				return ClocheFertilizer.isValidFertilizer(itemStack);
			return true;
		}
	}

	public static class Tagged extends IESlot
	{
		private final Named<Item> tag;

		public Tagged(AbstractContainerMenu container, Container inv, int id, int x, int y, Named<Item> tag)
		{
			super(container, inv, id, x, y);
			this.tag = tag;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			if(itemStack.isEmpty())
				return false;
			return this.tag.contains(itemStack.getItem());
		}
	}

	// Only used to "fill up slot IDs" to keep the IDs of later slots stable when adding/removing "real" slots
	public static class AlwaysEmptySlot extends IESlot
	{
		public AlwaysEmptySlot(AbstractContainerMenu containerMenu)
		{
			super(containerMenu, EmptyContainer.INSTANCE, 0, 0, 0);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			return false;
		}

		@Override
		public boolean isActive()
		{
			return false;
		}
	}

	public static class ContainerCallback extends SlotItemHandler
	{
		AbstractContainerMenu container;

		public ContainerCallback(AbstractContainerMenu container, IItemHandler inv, int id, int x, int y)
		{
			super(inv, id, x, y);
			this.container = container;
		}

		@Override
		public boolean mayPlace(ItemStack itemStack)
		{
			if(this.container instanceof ICallbackContainer)
				return ((ICallbackContainer)this.container).canInsert(itemStack, getSlotIndex(), this);
			return true;
		}

		@Override
		public boolean mayPickup(Player player)
		{
			if(this.container instanceof ICallbackContainer)
				return ((ICallbackContainer)this.container).canTake(this.getItem(), getSlotIndex(), this);
			return true;
		}
	}

	public interface ICallbackContainer
	{
		boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject);

		boolean canTake(ItemStack stack, int slotNumer, Slot slotObject);
	}
}