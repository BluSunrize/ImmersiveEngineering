/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.tool.assembler.RecipeQuery;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControlState;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControllable;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class AssemblerBlockEntity extends PoweredMultiblockBlockEntity<AssemblerBlockEntity, MultiblockRecipe>
		implements IInteractionObjectIE<AssemblerBlockEntity>, IConveyorAttachable, IBlockBounds, ComputerControllable,
		IEClientTickableBE, ISoundBE
{
	public static final int NUM_PATTERNS = 3;
	public static final int NUM_TANKS = 3;
	public static final int TANK_CAPACITY = 8*FluidType.BUCKET_VOLUME;
	public static final int ENERGY_CAPACITY = 32000;
	public static final int INVENTORY_SIZE = 18+NUM_PATTERNS;

	public ComputerControlState[] computerControlByRecipe = {
			new ComputerControlState(), new ComputerControlState(), new ComputerControlState(),
	};

	public AssemblerBlockEntity(BlockEntityType<AssemblerBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.ASSEMBLER, ENERGY_CAPACITY, true, type, pos, state);
	}

	public FluidTank[] tanks = {
			new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY), new FluidTank(TANK_CAPACITY)
	};
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	public CrafterPatternInventory[] patterns = {
			new CrafterPatternInventory(), new CrafterPatternInventory(), new CrafterPatternInventory()
	};
	public boolean recursiveIngredients = false;

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			tanks[0].readFromNBT(nbt.getCompound("tank0"));
			tanks[1].readFromNBT(nbt.getCompound("tank1"));
			tanks[2].readFromNBT(nbt.getCompound("tank2"));
			recursiveIngredients = nbt.getBoolean("recursiveIngredients");
			ContainerHelper.loadAllItems(nbt, inventory);
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				ListTag patternList = nbt.getList("pattern"+iPattern, 10);
				patterns[iPattern] = new CrafterPatternInventory();
				patterns[iPattern].readFromNBT(patternList, level);
			}
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			nbt.put("tank0", tanks[0].writeToNBT(new CompoundTag()));
			nbt.put("tank1", tanks[1].writeToNBT(new CompoundTag()));
			nbt.put("tank2", tanks[2].writeToNBT(new CompoundTag()));
			nbt.putBoolean("recursiveIngredients", recursiveIngredients);
			ContainerHelper.saveAllItems(nbt, inventory);
			for(int iPattern = 0; iPattern < patterns.length; iPattern++)
			{
				ListTag patternList = new ListTag();
				patterns[iPattern].writeToNBT(patternList);
				nbt.put("pattern"+iPattern, patternList);
			}
		}
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(worldPosition.relative(getFacing(), 2), getFacing().getOpposite()),
			ForgeCapabilities.ITEM_HANDLER);

	@Override
	public void tickClient()
	{
		// Note: This sound is currently never played, since "shouldRenderAsActive" checks for a non-empty process
		// queue. See https://discord.com/channels/270295681829306369/539145317547900938/1055049673653633095 and related
		// discussion.
		ImmersiveEngineering.proxy.handleTileSound(IESounds.assembler, this, shouldRenderAsActive(), .5f, 1);
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		if(isDummy()||isRSDisabled()||level.getGameTime()%16!=((getBlockPos().getX()^getBlockPos().getZ())&15))
			return;
		final List<OutputBuffer> outputs = craftRecipes();
		for(OutputBuffer buffer : outputs)
			for(int i = 0; i < buffer.results.size(); ++i)
				outputStack(buffer.results.get(i), buffer.id, i==0);
		for(int i = 0; i < 3; i++)
			if(!isRecipeIngredient(this.inventory.get(18+i), i))
				this.inventory.set(18+i, Utils.insertStackIntoInventory(output, this.inventory.get(18+i), false));
	}

	private List<OutputBuffer> craftRecipes()
	{
		List<OutputBuffer> outputBuffer = new ArrayList<>();
		for(int patternId = 0; patternId < patterns.length; patternId++)
		{
			CrafterPatternInventory pattern = patterns[patternId];
			ComputerControlState state = computerControlByRecipe[patternId];
			if(state.isAttached()&&!state.isEnabled())
				continue;
			ItemStack output = pattern.inv.get(9).copy();
			if(output.isEmpty()||!canOutput(output, patternId))
				continue;
			ArrayList<ItemStack> availableStacks = new ArrayList<>();//List of all available inputs in the inventory
			for(OutputBuffer bufferedStacks : outputBuffer)
				availableStacks.addAll(bufferedStacks.results);
			for(ItemStack stack : this.inventory)
				if(!stack.isEmpty())
					availableStacks.add(stack);
			List<RecipeQuery> queries = pattern.getQueries(getLevelNonnull());
			if(queries==null)
				continue;

			int consumed = IEServerConfig.MACHINES.assembler_consumption.get();
			if(!this.consumeIngredients(queries, availableStacks, false, null))
				continue;
			if(this.energyStorage.extractEnergy(consumed, false)!=consumed)
				continue;
			NonNullList<ItemStack> outputList = NonNullList.create();//List of all outputs for the current recipe. This includes discarded containers
			outputList.add(output);

			RecipeInputSources sources = new RecipeInputSources(pattern);
			this.consumeIngredients(queries, availableStacks, true, sources);

			NonNullList<ItemStack> remainingItems = pattern.recipe.getRemainingItems(Utils.InventoryCraftingFalse.createFilledCraftingInventory(3, 3, sources.gridItems));
			for(int i = 0; i < remainingItems.size(); i++)
			{
				ItemStack rem = remainingItems.get(i);
				if(!sources.providedByNonItem.getBoolean(i)&&!rem.isEmpty())
					outputList.add(rem);
			}

			outputBuffer.add(new OutputBuffer(outputList, patternId));
			markChunkDirty();
		}
		return outputBuffer;
	}

	private void outputStack(ItemStack output, int patternId, boolean isMainOutput)
	{
		if(!isRecipeIngredient(output, patternId))
		{
			output = Utils.insertStackIntoInventory(this.output, output, false);
			if(output.isEmpty()||output.getCount() <= 0)
				return;
		}
		if(isMainOutput)
			tryInsertOnto(18+patternId, output);
		else
		{
			boolean inserted = false;
			for(int i = 0; i < this.inventory.size(); i++)
				if(tryInsertOnto(i, output))
				{
					inserted = true;
					break;
				}
			if(!inserted)
				for(int i = 0; i < this.inventory.size(); i++)
					if(inventory.get(i).isEmpty())
						this.inventory.set(i, output.copy());
		}
	}

	public boolean consumeIngredients(
			List<RecipeQuery> queries, ArrayList<ItemStack> itemStacks, boolean doConsume,
			@Nullable RecipeInputSources sources
	)
	{
		if(!doConsume)
		{
			ArrayList<ItemStack> dupeList = new ArrayList<>(itemStacks.size());
			for(ItemStack stack : itemStacks)
				dupeList.add(stack.copy());
			itemStacks = dupeList;
		}
		List<FluidStack> tankFluids = Arrays.stream(tanks)
				.map(tank -> doConsume?tank.getFluid(): tank.getFluid().copy())
				.toList();
		for(int i = 0; i < queries.size(); i++)
		{
			RecipeQuery recipeQuery = queries.get(i);
			int querySize = recipeQuery.getItemCount();
			if(recipeQuery.isFluid())
			{
				if(consumeFluid(tankFluids, i, recipeQuery, sources))
					continue;
				else
					querySize = 1;
			}
			for(ItemStack next : itemStacks)
				querySize -= consumeItem(querySize, i, next, recipeQuery, sources);
			if(querySize > 0)
				return false;
		}
		return true;
	}

	private boolean consumeFluid(
			List<FluidStack> tankFluids, int slot, RecipeQuery query, @Nullable RecipeInputSources sources
	)
	{
		for(FluidStack tankFluid : tankFluids)
			if(query.matchesFluid(tankFluid))
			{
				tankFluid.shrink(query.getFluidSize());
				if(sources!=null)
					sources.providedByNonItem.set(slot, true);
				return true;
			}
		return false;
	}

	private int consumeItem(
			int maxConsume, int slot, ItemStack next, RecipeQuery query, @Nullable RecipeInputSources sources
	)
	{
		if(maxConsume <= 0||next.isEmpty()||!query.matchesIgnoringSize(next))
			return 0;
		int taken = Math.min(maxConsume, next.getCount());
		ItemStack forGrid = next.split(taken);
		if(sources!=null)
			sources.gridItems.set(slot, forGrid);
		return taken;
	}

	private boolean tryInsertOnto(int slot, ItemStack toAdd)
	{
		if(!canInsertOnto(slot, toAdd))
			return false;
		final var present = this.inventory.get(slot);
		if(present.isEmpty())
			this.inventory.set(slot, toAdd);
		else
			present.grow(toAdd.getCount());
		return true;
	}

	public boolean canInsertOnto(int slot, ItemStack output)
	{
		final ItemStack existing = this.inventory.get(slot);
		if(existing.isEmpty())
			return true;
		else if(!ItemHandlerHelper.canItemStacksStack(output, existing))
			return false;
		else
			return existing.getCount()+output.getCount() <= existing.getMaxStackSize();
	}

	public boolean canOutput(ItemStack output, int iPattern)
	{
		return canInsertOnto(18+iPattern, output);
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
					if(!pattern.inv.get(i).isEmpty()&&ItemStack.isSame(pattern.inv.get(i), stack))
						return true;
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
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(1, 2, 1, RelativeBlockFace.UP));
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
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, AssemblerBlockEntity::master,
			registerCapability(new IEInventoryHandler(18, this, 0, true, false))
	);
	private final MultiblockCapability<IItemHandler> extractionHandler = MultiblockCapability.make(
			this, be -> be.extractionHandler, AssemblerBlockEntity::master,
			registerCapability(new IEInventoryHandler(3, this, 18, false, true))
	);

	private static final BlockPos inputPos = new BlockPos(1, 1, 2);
	private static final BlockPos outputPos = new BlockPos(1, 1, 0);
	private static final Set<BlockPos> itemConnections = ImmutableSet.of(inputPos, outputPos);
	private static final MultiblockFace fluidInputPos = new MultiblockFace(1, 0, 2, RelativeBlockFace.FRONT);

	private final MultiblockCapability<IFluidHandler> fluidCap = MultiblockCapability.make(
			this, be -> be.fluidCap, AssemblerBlockEntity::master, registerFluidHandler(tanks)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==ForgeCapabilities.FLUID_HANDLER)
			if(facing==null||fluidInputPos.equals(asRelativeFace(facing)))
				return fluidCap.getAndCast();
		if(itemConnections.contains(posInMultiblock)&&capability==ForgeCapabilities.ITEM_HANDLER)
		{
			if(inputPos.equals(posInMultiblock)&&facing==this.getFacing().getOpposite())
				return insertionHandler.getAndCast();
			if(outputPos.equals(posInMultiblock)&&facing==this.getFacing())
				return extractionHandler.getAndCast();
		}
		return super.getCapability(capability, facing);
	}


	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MultiblockRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return null;
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public AssemblerBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public ArgContainer<AssemblerBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.ASSEMBLER;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		return new Direction[]{this.getFacing()};
	}

	@Override
	public void setLevel(Level world)
	{
		super.setLevel(world);
		if(getLevel()!=null)
			for(CrafterPatternInventory pattern : patterns)
				pattern.recalculateOutput(level);
	}

	@Override
	public Stream<ComputerControlState> getAllComputerControlStates()
	{
		return Arrays.stream(computerControlByRecipe);
	}

	private record OutputBuffer(NonNullList<ItemStack> results, int id)
	{
	}

	private record RecipeInputSources(List<ItemStack> gridItems, BooleanList providedByNonItem)
	{
		public RecipeInputSources(CrafterPatternInventory pattern)
		{
			this(List.copyOf(pattern.inv), new BooleanArrayList(new boolean[9]));
		}
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return shouldRenderAsActive();
	}
}
