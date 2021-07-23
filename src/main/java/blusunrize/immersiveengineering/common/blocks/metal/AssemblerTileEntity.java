/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.assembler.RecipeQuery;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class AssemblerTileEntity extends PoweredMultiblockTileEntity<AssemblerTileEntity, MultiblockRecipe>
		implements IInteractionObjectIE<AssemblerTileEntity>, IConveyorAttachable, IBlockBounds
{
	public ComputerControlState[] computerControlByRecipe = {
			ComputerControlState.NO_COMPUTER,
			ComputerControlState.NO_COMPUTER,
			ComputerControlState.NO_COMPUTER,
	};

	public AssemblerTileEntity(BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.ASSEMBLER, 32000, true, IETileTypes.ASSEMBLER.get(), pos, state);
	}

	public FluidTank[] tanks = {
			new FluidTank(8*FluidAttributes.BUCKET_VOLUME),
			new FluidTank(8*FluidAttributes.BUCKET_VOLUME),
			new FluidTank(8*FluidAttributes.BUCKET_VOLUME)
	};
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(18+3, ItemStack.EMPTY);
	public CrafterPatternInventory[] patterns = {new CrafterPatternInventory(this), new CrafterPatternInventory(this), new CrafterPatternInventory(this)};
	public boolean recursiveIngredients = false;

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		tanks[2].readFromNBT(nbt.getCompound("tank2"));
		recursiveIngredients = nbt.getBoolean("recursiveIngredients");
		if(!descPacket)
		{
			ContainerHelper.loadAllItems(nbt, inventory);
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				ListTag patternList = nbt.getList("pattern"+iPattern, 10);
				patterns[iPattern] = new CrafterPatternInventory(this);
				patterns[iPattern].readFromNBT(patternList);
			}
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundTag()));
		nbt.put("tank1", tanks[1].writeToNBT(new CompoundTag()));
		nbt.put("tank2", tanks[2].writeToNBT(new CompoundTag()));
		nbt.putBoolean("recursiveIngredients", recursiveIngredients);
		if(!descPacket)
		{
			ContainerHelper.saveAllItems(nbt, inventory);
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				ListTag patternList = new ListTag();
				patterns[iPattern].writeToNBT(patternList);
				nbt.put("pattern"+iPattern, patternList);
			}
		}
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		Preconditions.checkState(!level.isClientSide);
		if(message.contains("buttonID", NBT.TAG_INT))
		{
			int id = message.getInt("buttonID");
			if(id >= 0&&id < patterns.length)
			{
				CrafterPatternInventory pattern = patterns[id];
				for(int i = 0; i < pattern.inv.size(); i++)
					pattern.inv.set(i, ItemStack.EMPTY);
			}
			else if(id==3)
			{
				recursiveIngredients = !recursiveIngredients;
			}
		}
		else if(message.contains("patternSync", NBT.TAG_INT))
		{
			int r = message.getInt("recipe");
			ListTag list = message.getList("patternSync", 10);
			CrafterPatternInventory pattern = patterns[r];
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag itemTag = list.getCompound(i);
				pattern.inv.set(itemTag.getInt("slot"), ItemStack.of(itemTag));
			}
		}
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntityAt(this,
			() -> new DirectionalBlockPos(worldPosition.relative(getFacing(), 2), getFacing().getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tickServer()
	{
		super.tickServer();
		if(isDummy()||isRSDisabled()||level.getGameTime()%16!=((getBlockPos().getX()^getBlockPos().getZ())&15))
			return;
		boolean update = false;
		NonNullList<ItemStack>[] outputBuffer = new NonNullList[patterns.length];
		for(int p = 0; p < patterns.length; p++)
		{
			CrafterPatternInventory pattern = patterns[p];
			ComputerControlState state = computerControlByRecipe[p];
			if(state.isStillAttached()&&!state.isEnabled())
				continue;
			if(!pattern.inv.get(9).isEmpty()&&canOutput(pattern.inv.get(9), p))
			{
				ItemStack output = pattern.inv.get(9).copy();
				ArrayList<ItemStack> availableStacks = new ArrayList<>();//List of all available inputs in the inventory
				for(NonNullList<ItemStack> bufferedStacks : outputBuffer)
					if(bufferedStacks!=null)
						for(ItemStack stack : bufferedStacks)
							if(!stack.isEmpty())
								availableStacks.add(stack);
				for(ItemStack stack : this.inventory)
					if(!stack.isEmpty())
						availableStacks.add(stack);
				int consumed = IEServerConfig.MACHINES.assembler_consumption.get();

				AssemblerHandler.IRecipeAdapter adapter = AssemblerHandler.findAdapter(pattern.recipe);
				RecipeQuery[] queries = adapter.getQueriedInputs(pattern.recipe, pattern.inv, level);
				if(queries==null)
					continue;
				if(this.energyStorage.extractEnergy(consumed, true)==consumed&&
						this.consumeIngredients(queries, availableStacks, false, null, null))
				{
					this.energyStorage.extractEnergy(consumed, false);
					NonNullList<ItemStack> outputList = NonNullList.create();//List of all outputs for the current recipe. This includes discarded containers
					outputList.add(output);

					BooleanList providedByNonItem = new BooleanArrayList(new boolean[9]);
					NonNullList<ItemStack> gridItems = NonNullList.of(ItemStack.EMPTY, pattern.inv.toArray(new ItemStack[0]));
					this.consumeIngredients(queries, availableStacks, true, gridItems, providedByNonItem);

					NonNullList<ItemStack> remainingItems = pattern.recipe.getRemainingItems(Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, gridItems));
					for(int i = 0; i < remainingItems.size(); i++)
					{
						ItemStack rem = remainingItems.get(i);
						if(!providedByNonItem.getBoolean(i)&&!rem.isEmpty())
							outputList.add(rem);
					}

					outputBuffer[p] = outputList;
					update = true;
				}
			}
		}
		for(int buffer = 0; buffer < outputBuffer.length; buffer++)
			if(outputBuffer[buffer]!=null&&outputBuffer[buffer].size() > 0)
				for(int iOutput = 0; iOutput < outputBuffer[buffer].size(); iOutput++)
				{
					ItemStack output = outputBuffer[buffer].get(iOutput);
					if(!output.isEmpty()&&output.getCount() > 0)
					{
						if(!isRecipeIngredient(output, buffer))
						{
							output = Utils.insertStackIntoInventory(this.output, output, false);
							if(output.isEmpty()||output.getCount() <= 0)
								continue;
						}
						int free = -1;
						if(iOutput==0)//Main recipe output
						{
							if(this.inventory.get(18+buffer).isEmpty()&&free < 0)
								free = 18+buffer;
							else if(!this.inventory.get(18+buffer).isEmpty()&&
									ItemHandlerHelper.canItemStacksStack(output, this.inventory.get(18+buffer))&&
									this.inventory.get(18+buffer).getCount()+output.getCount() <= this.inventory.get(18+buffer).getMaxStackSize())
							{
								this.inventory.get(18+buffer).grow(output.getCount());
								free = -1;
								continue;
							}
						}
						else
							for(int i = 0; i < this.inventory.size(); i++)
							{
								if(this.inventory.get(i).isEmpty()&&free < 0)
									free = i;
								else if(!this.inventory.get(i).isEmpty()&&
										ItemHandlerHelper.canItemStacksStack(output, this.inventory.get(i))&&
										this.inventory.get(i).getCount()+output.getCount() <= this.inventory.get(i).getMaxStackSize())
								{
									this.inventory.get(i).grow(output.getCount());
									free = -1;
									break;
								}
							}
						if(free >= 0)
							this.inventory.set(free, output.copy());
					}
				}
		for(int i = 0; i < 3; i++)
			if(!isRecipeIngredient(this.inventory.get(18+i), i))
				this.inventory.set(18+i, Utils.insertStackIntoInventory(output, this.inventory.get(18+i), false));

		if(update)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	public boolean consumeIngredients(
			RecipeQuery[] queries, ArrayList<ItemStack> itemStacks, boolean doConsume,
			@Nullable NonNullList<ItemStack> gridItems, @Nullable BooleanList providedByNonItem
	)
	{
		Preconditions.checkArgument((gridItems==null)==(providedByNonItem==null));
		if(!doConsume)
		{
			ArrayList<ItemStack> dupeList = new ArrayList<>(itemStacks.size());
			for(ItemStack stack : itemStacks)
				dupeList.add(stack.copy());
			itemStacks = dupeList;
		}
		for(int i = 0; i < queries.length; i++)
		{
			RecipeQuery recipeQuery = queries[i];
			if(recipeQuery!=null)
			{
				int querySize = recipeQuery.getItemCount();
				if(recipeQuery.isFluid())
				{
					boolean hasFluid = false;
					for(FluidTank tank : tanks)
						if(recipeQuery.matchesFluid(tank.getFluid()))
						{
							hasFluid = true;
							if(doConsume)
								tank.drain(recipeQuery.getFluidSize(), FluidAction.EXECUTE);
							break;
						}
					if(hasFluid)
					{
						if(providedByNonItem!=null)
							providedByNonItem.set(i, true);
						continue;
					}
					else
						querySize = 1;
				}
				Iterator<ItemStack> it = itemStacks.iterator();
				while(it.hasNext())
				{
					ItemStack next = it.next();
					if(!next.isEmpty()&&recipeQuery.matchesIgnoringSize(next))
					{
						int taken = Math.min(querySize, next.getCount());
						ItemStack forGrid = next.split(taken);
						if(gridItems!=null)
						{
							gridItems.set(i, forGrid);
							providedByNonItem.set(i, false);
						}
						if(next.getCount() <= 0)
							it.remove();
						querySize -= taken;
						if(querySize <= 0)
							break;
					}
				}
				if(querySize > 0)
					return false;
			}
		}
		return true;
	}

	public boolean canOutput(ItemStack output, int iPattern)
	{
		if(this.inventory.get(18+iPattern).isEmpty())
			return true;
		else
			return ItemHandlerHelper.canItemStacksStack(output, this.inventory.get(18+iPattern))&&
					this.inventory.get(18+iPattern).getCount()+output.getCount() <= this.inventory.get(18+iPattern).getMaxStackSize();
	}

	public boolean isRecipeIngredient(ItemStack stack, int slot)
	{
		if(stack.isEmpty())
			return false;
		if(slot-1 < patterns.length||recursiveIngredients)
			for(int p = recursiveIngredients?0: slot; p < patterns.length; p++)
			{
				CrafterPatternInventory pattern = patterns[p];
				for(int i = 0; i < 9; i++)
					if(!pattern.inv.get(i).isEmpty())
					{
						if(ItemStack.isSame(pattern.inv.get(i), stack))
							return true;
					}
			}
		return false;
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		Set<BlockPos> fullBlocks = ImmutableSet.of(
				new BlockPos(1, 1, 2),
				new BlockPos(1, 1, 1),
				new BlockPos(1, 1, 0),
				new BlockPos(1, 2, 1)
		);
		if(posInMultiblock.getY()==0||fullBlocks.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, 1, 1);
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if((posInMultiblock.getZ()==2&&getFacing()==Direction.SOUTH)||(posInMultiblock.getZ()==0&&getFacing()==Direction.NORTH))
			zMin = .25f;
		else if((posInMultiblock.getZ()==2&&getFacing()==Direction.NORTH)||(posInMultiblock.getZ()==0&&getFacing()==Direction.SOUTH))
			zMax = .75f;
		else if((posInMultiblock.getZ()==2&&getFacing()==Direction.EAST)||(posInMultiblock.getZ()==0&&getFacing()==Direction.WEST))
			xMin = .25f;
		else if((posInMultiblock.getZ()==2&&getFacing()==Direction.WEST)||(posInMultiblock.getZ()==0&&getFacing()==Direction.EAST))
			xMax = .75f;
		if((posInMultiblock.getX()==0&&getFacing()==Direction.EAST)||(posInMultiblock.getX()==2&&getFacing()==Direction.WEST))
			zMin = .1875f;
		else if((posInMultiblock.getX()==0&&getFacing()==Direction.WEST)||(posInMultiblock.getX()==2&&getFacing()==Direction.EAST))
			zMax = .8125f;
		else if((posInMultiblock.getX()==0&&getFacing()==Direction.NORTH)||(posInMultiblock.getX()==2&&getFacing()==Direction.SOUTH))
			xMin = .1875f;
		else if((posInMultiblock.getX()==0&&getFacing()==Direction.SOUTH)||(posInMultiblock.getX()==2&&getFacing()==Direction.NORTH))
			xMax = .8125f;
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(1, 2, 1)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 0, 1),
				new BlockPos(2, 0, 1)
		);
	}

	@Override
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&w==1&&l!=1)
		{
			BlockEntity tile = getWorldNonnull().getBlockEntity(pos);
			if(tile instanceof ConveyorBeltTileEntity)
				((ConveyorBeltTileEntity)tile).setFacing(this.getFacing());
		}
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process)
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		//TODO this seems both unnecessary and wrong
		//BlockPos pos = getPos().offset(facing, -1);
		//TileEntity inventoryTile = this.world.getTileEntity(pos);
		//if(inventoryTile!=null)
		//	output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		//if(!output.isEmpty())
		//	Utils.dropStackAtPos(world, pos, output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process)
	{
		return 0;
	}


	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return this.tanks;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new IEInventoryHandler(18, this, 0, true, false));
	private LazyOptional<IItemHandler> extractionHandler = registerConstantCap(
			new IEInventoryHandler(3, this, 18, false, true));

	private static final BlockPos inputPos = new BlockPos(1, 1, 2);
	private static final BlockPos outputPos = new BlockPos(1, 1, 0);
	private static final Set<BlockPos> itemConnections = ImmutableSet.of(inputPos, outputPos);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(itemConnections.contains(posInMultiblock)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			AssemblerTileEntity master = master();
			if(master==null)
				return LazyOptional.empty();
			if(inputPos.equals(posInMultiblock)&&facing==this.getFacing().getOpposite())
				return master.insertionHandler.cast();
			if(outputPos.equals(posInMultiblock)&&facing==this.getFacing())
				return master.extractionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}


	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MultiblockRecipe getRecipeForId(ResourceLocation id)
	{
		return null;
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public AssemblerTileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public TileContainer<AssemblerTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.ASSEMBLER;
	}

	private static final BlockPos fluidInputPos = new BlockPos(1, 0, 2);

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		AssemblerTileEntity master = master();
		if(master!=null&&fluidInputPos.equals(posInMultiblock)&&(side==null||side==getFacing().getOpposite()))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return true;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return true;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		/*TODO if(posInMultiblock==16)*/
		return new Direction[]{this.getFacing()};
		//return new Direction[0];
	}

	@Override
	public void setLevelAndPosition(Level world, BlockPos blockPos) {
		super.setLevelAndPosition(world, blockPos);
		if(getLevel()!=null)
			for(CrafterPatternInventory pattern : patterns)
				pattern.recalculateOutput();
	}

	public static class CrafterPatternInventory implements Container
	{
		public NonNullList<ItemStack> inv = NonNullList.withSize(10, ItemStack.EMPTY);
		public Recipe recipe;
		final AssemblerTileEntity tile;

		public CrafterPatternInventory(AssemblerTileEntity tile)
		{
			this.tile = tile;
		}

		@Override
		public int getContainerSize()
		{
			return 10;
		}

		@Override
		public boolean isEmpty()
		{
			for(ItemStack stack : inv)
			{
				if(!stack.isEmpty())
					return false;
			}
			return true;
		}

		@Override
		public ItemStack getItem(int slot)
		{
			return inv.get(slot);
		}

		@Override
		public ItemStack removeItem(int slot, int amount)
		{
			ItemStack stack = getItem(slot);
			if(slot < 9&&!stack.isEmpty())
				if(stack.getCount() <= amount)
					setItem(slot, ItemStack.EMPTY);
				else
				{
					stack = stack.split(amount);
					if(stack.getCount()==0)
						setItem(slot, ItemStack.EMPTY);
				}
			return stack;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot)
		{
			ItemStack stack = getItem(slot);
			if(!stack.isEmpty())
				setItem(slot, ItemStack.EMPTY);
			return stack;
		}

		@Override
		public void setItem(int slot, ItemStack stack)
		{
			if(slot < 9)
			{
				inv.set(slot, stack);
				if(!stack.isEmpty()&&stack.getCount() > getMaxStackSize())
					stack.setCount(getMaxStackSize());
			}
			recalculateOutput();
		}

		@Override
		public void clearContent()
		{
			for(int i = 0; i < this.inv.size(); i++)
				this.inv.set(i, ItemStack.EMPTY);
		}

		public void recalculateOutput()
		{
			if(tile.getLevel()!=null)
			{
				CraftingContainer invC = Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, inv);
				this.recipe = Utils.findCraftingRecipe(invC, tile.getWorldNonnull()).orElse(null);
				this.inv.set(9, recipe!=null?recipe.assemble(invC): ItemStack.EMPTY);
			}
		}


		@Override
		public int getMaxStackSize()
		{
			return 1;
		}

		@Override
		public boolean stillValid(Player player)
		{
			return true;
		}

		@Override
		public void startOpen(Player player)
		{
		}

		@Override
		public void stopOpen(Player player)
		{
		}

		@Override
		public boolean canPlaceItem(int slot, ItemStack stack)
		{
			return true;
		}

		@Override
		public void setChanged()
		{
			this.tile.setChanged();
		}

		public void writeToNBT(ListTag list)
		{
			for(int i = 0; i < this.inv.size(); i++)
				if(!this.inv.get(i).isEmpty())
				{
					CompoundTag itemTag = new CompoundTag();
					itemTag.putByte("Slot", (byte)i);
					this.inv.get(i).save(itemTag);
					list.add(itemTag);
				}
		}

		public void readFromNBT(ListTag list)
		{
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag itemTag = list.getCompound(i);
				int slot = itemTag.getByte("Slot")&255;
				if(slot < getContainerSize())
					this.inv.set(slot, ItemStack.of(itemTag));
			}
			recalculateOutput();
		}
	}
}
