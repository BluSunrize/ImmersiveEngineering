/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.common.IETileTypes;
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
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemBatcherTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInventory,
		IInteractionObjectIE, IStateBasedDirectional
{
	public static final int NUM_SLOTS = 9;
	private final NonNullList<ItemStack> filters = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	private final NonNullList<ItemStack> buffers = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	public BatchMode batchMode = BatchMode.ALL;
	public NonNullList<DyeColor> redstoneColors = NonNullList.withSize(NUM_SLOTS, DyeColor.WHITE);

	public ItemBatcherTileEntity()
	{
		super(IETileTypes.ITEM_BATCHER.get());
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

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntityAt(this,
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
				for(int slot = 0; slot < NUM_SLOTS; slot++)
					if(!this.filters.get(slot).isEmpty())
						matched &= isFilterMatched(slot);
			}
			if(matched)
			{
				for(int slot = 0; slot < NUM_SLOTS; slot++)
				{
					ItemStack filterStack = this.filters.get(slot);
					if(filterStack.isEmpty())
						continue;
					if(this.batchMode==BatchMode.ALL||isFilterMatched(slot))
					{
						ItemStack outStack = buffers.get(slot);
						int outSize = filterStack.getCount();
						ItemStack stack = ItemHandlerHelper.copyStackWithSize(outStack, outSize);
						stack = Utils.insertStackIntoInventory(output, stack, false);
						if(!stack.isEmpty())
							outSize -= stack.getCount();
						outStack.shrink(outSize);
						if(outStack.getCount() <= 0)
							this.buffers.set(slot, ItemStack.EMPTY);
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
		return ItemStack.areItemsEqualIgnoreDurability(this.filters.get(slot), this.buffers.get(slot))
				&&this.buffers.get(slot).getCount() >= this.filters.get(slot).getCount();
	}

	protected Set<DyeColor> calculateRedstoneOutputs()
	{
		Map<DyeColor, Boolean> map = new EnumMap<>(DyeColor.class);
		for(int slot = 0; slot < NUM_SLOTS; slot++)
			if(!filters.get(slot).isEmpty())
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
			NonNullList<ItemStack> merged = NonNullList.withSize(2*NUM_SLOTS, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(nbt, merged);
			for(int i = 0; i < NUM_SLOTS; ++i)
			{
				this.buffers.set(i, merged.get(i+NUM_SLOTS));
				this.filters.set(i, merged.get(i));
			}
		}
		this.batchMode = BatchMode.values()[nbt.getByte("batchMode")];
		int[] redstoneConfig = nbt.getIntArray("redstoneColors");
		if(redstoneConfig.length >= NUM_SLOTS)
			for(int i = 0; i < NUM_SLOTS; i++)
				this.redstoneColors.set(i, DyeColor.byId(redstoneConfig[i]));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> merged = NonNullList.withSize(2*NUM_SLOTS, ItemStack.EMPTY);
			for(int i = 0; i < NUM_SLOTS; ++i)
			{
				merged.set(i+NUM_SLOTS, this.buffers.get(i));
				merged.set(i, this.filters.get(i));
			}
			ItemStackHelper.saveAllItems(nbt, merged);
		}
		nbt.putByte("batchMode", (byte)this.batchMode.ordinal());
		int[] redstoneConfig = new int[NUM_SLOTS];
		for(int i = 0; i < NUM_SLOTS; i++)
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
		return buffers;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return ItemStack.areItemsEqualIgnoreDurability(this.filters.get(slot), stack);
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

	private final LazyOptional<IItemHandler> insertionCap = registerConstantCap(
			new IEInventoryHandler(NUM_SLOTS, this, 0, true, false)
	);

	private final LazyOptional<RedstoneBundleConnection> redstoneCap = registerConstantCap(
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

	public NonNullList<ItemStack> getFilters()
	{
		return filters;
	}

	public enum BatchMode
	{
		SINGLE,
		ALL;
	}
}