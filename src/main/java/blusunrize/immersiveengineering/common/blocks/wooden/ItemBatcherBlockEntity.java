/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork;
import blusunrize.immersiveengineering.api.wires.redstone.CapabilityRedstoneNetwork.RedstoneBundleConnection;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemBatcherBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IIEInventory,
		IInteractionObjectIE<ItemBatcherBlockEntity>, IStateBasedDirectional
{
	public static final int NUM_SLOTS = 9;
	private final NonNullList<ItemStack> filters = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	private final NonNullList<ItemStack> buffers = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	public BatchMode batchMode = BatchMode.ALL;
	public NonNullList<DyeColor> redstoneColors = NonNullList.withSize(NUM_SLOTS, DyeColor.WHITE);

	public ItemBatcherBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.ITEM_BATCHER.get(), pos, state);
	}

	@Override
	public Property<Direction> getFacingProperty()
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
		return placer.isShiftKeyDown();
	}

	private final IEBlockCapabilityCache<IItemHandler> output = IEBlockCapabilityCaches.forNeighbor(
			ItemHandler.BLOCK, this, this::getFacing
	);

	@Override
	public void tickServer()
	{
		if(level.getGameTime()%8!=0||!isActive())
			return;
		IItemHandler outputHandler = output.getCapability();
		if(outputHandler!=null)
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
				boolean anySent = false;
				for(int slot = 0; slot < NUM_SLOTS; slot++)
				{
					ItemStack filterStack = this.filters.get(slot);
					if(filterStack.isEmpty())
						continue;
					if(this.batchMode==BatchMode.ALL||isFilterMatched(slot))
					{
						ItemStack outStack = buffers.get(slot);
						int outSize = filterStack.getCount();
						ItemStack stack = outStack.copyWithCount(outSize);
						stack = ItemHandlerHelper.insertItem(outputHandler, stack, false);
						if(!stack.isEmpty())
							outSize -= stack.getCount();
						outStack.shrink(outSize);
						if(outStack.getCount() <= 0)
							this.buffers.set(slot, ItemStack.EMPTY);
						anySent = true;
					}
				}
				if(anySent)
					redstoneCap.markDirty();
			}
		}
	}

	protected boolean isActive()
	{
		return !isRSPowered();
	}

	protected boolean isFilterMatched(int slot)
	{
		return ItemUtils.isSameIgnoreDurability(this.filters.get(slot), this.buffers.get(slot))
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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> merged = NonNullList.withSize(2*NUM_SLOTS, ItemStack.EMPTY);
			ContainerHelper.loadAllItems(nbt, merged, provider);
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
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		if(!descPacket)
		{
			NonNullList<ItemStack> merged = NonNullList.withSize(2*NUM_SLOTS, ItemStack.EMPTY);
			for(int i = 0; i < NUM_SLOTS; ++i)
			{
				merged.set(i+NUM_SLOTS, this.buffers.get(i));
				merged.set(i, this.filters.get(i));
			}
			ContainerHelper.saveAllItems(nbt, merged, provider);
		}
		nbt.putByte("batchMode", (byte)this.batchMode.ordinal());
		int[] redstoneConfig = new int[NUM_SLOTS];
		for(int i = 0; i < NUM_SLOTS; i++)
			redstoneConfig[i] = this.redstoneColors.get(i).getId();
		nbt.putIntArray("redstoneColors", redstoneConfig);
	}

	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("batchMode"))
			this.batchMode = BatchMode.values()[message.getByte("batchMode")];
		if(message.contains("redstoneColor_slot")&&message.contains("redstoneColor_val"))
			this.redstoneColors.set(message.getInt("redstoneColor_slot"), DyeColor.byId(message.getInt("redstoneColor_val")));
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public ItemBatcherBlockEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ArgContainer<ItemBatcherBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.ITEM_BATCHER;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return buffers;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return ItemUtils.isSameIgnoreDurability(this.filters.get(slot), stack);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		redstoneCap.markDirty();
	}

	private final IItemHandler insertionCap = new IEInventoryHandler(NUM_SLOTS, this, 0, true, false);

	private final RedstoneBundleConnection redstoneCap = new RedstoneBundleConnection()
	{
		@Override
		public void updateInput(byte[] signals, Direction side)
		{
			Set<DyeColor> outputMap = calculateRedstoneOutputs();
			for(DyeColor dye : outputMap)
				signals[dye.getId()] = (byte)Math.max(signals[dye.getId()], 15);
		}
	};

	public static void registerCapabilities(BECapabilityRegistrar<ItemBatcherBlockEntity> registrar)
	{
		registrar.registerAllContexts(CapabilityRedstoneNetwork.REDSTONE_BUNDLE_CONNECTION, be -> be.redstoneCap);
		registrar.register(
				ItemHandler.BLOCK,
				(be, facing) -> facing==be.getFacing().getOpposite()?be.insertionCap: null
		);
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