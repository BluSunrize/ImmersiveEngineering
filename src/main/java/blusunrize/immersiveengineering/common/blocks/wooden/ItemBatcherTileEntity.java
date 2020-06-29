/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemBatcherTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInventory,
		IInteractionObjectIE, IStateBasedDirectional
{
	public static TileEntityType<ItemBatcherTileEntity> TYPE;

	private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
	public BatchMode batchMode = BatchMode.ALL;
	public NonNullList<DyeColor> redstoneColors = NonNullList.withSize(9, DyeColor.WHITE);

	public ItemBatcherTileEntity()
	{
		super(TYPE);
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return placer.isSneaking();
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(pos.offset(getFacing()), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		if(!world.isRemote&&world.getGameTime()%8==0&&output.isPresent()&&isActive())
		{
			boolean matched = true;
			if(this.batchMode==BatchMode.ALL)
			{
				for(int slot = 0; slot < 9; slot++)
					if(!this.inventory.get(slot).isEmpty())
						matched &= isFilterMatched(slot);
			}
			if(matched)
			{
				for(int slot = 0; slot < 9; slot++)
				{
					ItemStack filterStack = this.inventory.get(slot);
					if(filterStack.isEmpty())
						continue;
					if(this.batchMode==BatchMode.ALL||isFilterMatched(slot))
					{
						ItemStack outStack = inventory.get(slot+9);
						int outSize = filterStack.getCount();
						ItemStack stack = Utils.copyStackWithAmount(outStack, outSize);
						stack = Utils.insertStackIntoInventory(output, stack, false);
						if(!stack.isEmpty())
							outSize -= stack.getCount();
						outStack.shrink(outSize);
						if(outStack.getCount() <= 0)
							this.inventory.set(slot+9, ItemStack.EMPTY);
					}
				}
				redstoneCap.ifPresent(RedstoneBundleConnection::markDirty);
			}
		}
	}

	protected boolean isActive()
	{
		return !isRSPowered();
	}

	protected boolean isFilterMatched(int slot)
	{
		return ItemStack.areItemsEqualIgnoreDurability(this.inventory.get(slot), this.inventory.get(slot+9))
				&&this.inventory.get(slot+9).getCount() >= this.inventory.get(slot).getCount();
	}

	protected Set<DyeColor> calculateRedstoneOutputs()
	{
		Map<DyeColor, Boolean> map = new EnumMap<>(DyeColor.class);
		for(int slot = 0; slot < 9; slot++)
			if(!inventory.get(slot).isEmpty())
			{
				DyeColor dye = redstoneColors.get(slot);
				Boolean ex = map.get(dye);
				map.put(dye, ex!=null?ex&&isFilterMatched(slot): isFilterMatched(slot));
			}
		return map.keySet().stream().filter(map::get).collect(Collectors.toSet());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			inventory = NonNullList.withSize(18, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(nbt, this.inventory);
		}
		this.batchMode = BatchMode.values()[nbt.getByte("batchMode")];
		int[] redstoneConfig = nbt.getIntArray("redstoneColors");
		if(redstoneConfig.length >= 9)
			for(int i = 0; i < 9; i++)
				this.redstoneColors.set(i, DyeColor.byId(redstoneConfig[i]));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(!descPacket)
			ItemStackHelper.saveAllItems(nbt, this.inventory);
		nbt.putByte("batchMode", (byte)this.batchMode.ordinal());
		int[] redstoneConfig = new int[9];
		for(int i = 0; i < 9; i++)
			redstoneConfig[i] = this.redstoneColors.get(i).getId();
		nbt.putIntArray("redstoneColors", redstoneConfig);
	}

	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("batchMode"))
			this.batchMode = BatchMode.values()[message.getByte("batchMode")];
		if(message.contains("redstoneColor_slot")&&message.contains("redstoneColor_val"))
			this.redstoneColors.set(message.getInt("redstoneColor_slot"), DyeColor.byId(message.getInt("redstoneColor_val")));
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return this;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(slot >= 9)
			return ItemStack.areItemsEqualIgnoreDurability(this.inventory.get(slot-9), stack);
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		redstoneCap.ifPresent(RedstoneBundleConnection::markDirty);
	}

	private LazyOptional<IItemHandler> insertionCap = registerConstantCap(
			new IEInventoryHandler(18, this, 0, true, false).blockInsert(0, 1, 2, 3, 4, 5, 6, 7, 8)
	);

	private LazyOptional<RedstoneBundleConnection> redstoneCap = registerConstantCap(
			new RedstoneBundleConnection()
			{
				@Override
				public void updateInput(byte[] signals, ConnectionPoint cp, Direction side)
				{
					Set<DyeColor> outputMap = calculateRedstoneOutputs();
					for(DyeColor dye : outputMap)
						signals[dye.getId()] = (byte)Math.max(signals[dye.getId()], 15);
				}
			}
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY&&facing==getFacing().getOpposite())
			return insertionCap.cast();
		if(capability==CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION)
			return redstoneCap.cast();
		return super.getCapability(capability, facing);
	}

	public enum BatchMode
	{
		SINGLE,
		ALL;
	}
}