/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process_old.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

public class SawmillBlockEntity extends PoweredMultiblockBlockEntity<SawmillBlockEntity, MultiblockRecipe>
		implements IConveyorAttachable, IBlockBounds, IPlayerInteraction, IEClientTickableBE, ISoundBE
{
	public float animation_bladeRotation = 0;
	public ItemStack sawblade = ItemStack.EMPTY;
	public List<SawmillProcess> sawmillProcessQueue = new ArrayList<>();
	// this is a temporary counter to keep track of the "same" kind of log inserted. Allows combining them into threes.
	private int combinedLogs = 0;
	//Temporary counters for making sure sounds tick properly
	private int count = 0;

	public SawmillBlockEntity(BlockEntityType<SawmillBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.SAWMILL, 32000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		sawblade = ItemStack.of(nbt.getCompound("sawblade"));

		ListTag processNBT = nbt.getList("sawmillQueue", 10);
		sawmillProcessQueue.clear();
		for(int i = 0; i < processNBT.size(); i++)
		{
			CompoundTag tag = processNBT.getCompound(i);
			SawmillProcess process = SawmillProcess.readFromNBT(tag);
			sawmillProcessQueue.add(process);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.sawblade.isEmpty())
			nbt.put("sawblade", this.sawblade.save(new CompoundTag()));
		ListTag processNBT = new ListTag();
		for(SawmillProcess process : this.sawmillProcessQueue)
			processNBT.add(process.writeToNBT());
		nbt.put("sawmillQueue", processNBT);
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
	}

	private final CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(4, 1, 1)).relative(outDir), outDir.getOpposite());
	}, ForgeCapabilities.ITEM_HANDLER);

	private final CapabilityReference<IItemHandler> secondaryOutputCap = CapabilityReference.forBlockEntityAt(
			this, this::getSecondaryOutputCapPos, ForgeCapabilities.ITEM_HANDLER
	);

	@Override
	public boolean canTickAny()
	{
		return super.canTickAny()&&!isRSDisabled();
	}

	@Override
	public void tickClient()
	{
		if(shouldRenderAsActive())
		{
			if(!isRSDisabled())
			{
				animation_bladeRotation += 36f;
				animation_bladeRotation %= 360f;
			}

			if(!this.sawblade.isEmpty())
			{
				Optional<SawmillProcess> process = sawmillProcessQueue.stream()
						.filter(p -> p.isSawing(level))
						.findFirst();
				//Handle empty sound
				ImmersiveEngineering.proxy.handleTileSound(IESounds.saw_empty, this, !isRSDisabled()&&!sawblade.isEmpty()&&!process.isPresent(), .4f, 1);
				//Handle particles & full sound
				if(process.isPresent())
				{
					Direction particleDir = getIsMirrored()?getFacing().getClockWise(): getFacing().getCounterClockWise();
					AABB aabb = CACHED_SAWBLADE_AABB.apply(this);
					double posX = aabb.minX+level.random.nextDouble()*(aabb.maxX-aabb.minX);
					double posY = aabb.minY+level.random.nextDouble()*(aabb.maxY-aabb.minY);
					double posZ = aabb.minZ+level.random.nextDouble()*(aabb.maxZ-aabb.minZ);
					double vX = level.random.nextDouble()*particleDir.getStepX()*0.3;
					double vY = level.random.nextDouble()*0.3;
					double vZ = level.random.nextDouble()*particleDir.getStepZ()*0.3;
					level.addAlwaysVisibleParticle(
							new ItemParticleOption(ParticleTypes.ITEM, process.get().getCurrentStack(level, true)),
							posX, posY, posZ, vX, vY, vZ
					);
					//Arbitrary constant is arbitrary, but it's what sounded good in game, so we keep it. Actual length is supposed to be 10t...
					count++;
					if(count%21==0)
						level.playSound(ImmersiveEngineering.proxy.getClientPlayer(), getBlockPos(), IESounds.saw_full.get(), SoundSource.BLOCKS, .4F, 1);
				}
				else if(count!=-1)
				{
					count = -1;
				}
			}
		}

	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		tickedProcesses = 0;
		int max = getMaxProcessPerTick();
		int i = 0;
		Iterator<SawmillProcess> processIterator = sawmillProcessQueue.iterator();
		tickedProcesses = 0;
		Set<ItemStack> secondaries = new HashSet<>();
		while(processIterator.hasNext()&&i++ < max)
		{
			SawmillProcess process = processIterator.next();
			if(process.processStep(this, secondaries))
			{
				tickedProcesses++;
				this.updateMasterBlock(null, true);
			}
			if(process.processFinished)
			{
				doProcessOutput(process.getCurrentStack(level, !this.sawblade.isEmpty()).copy());
				processIterator.remove();
				if(this.sawblade.hurt(IEServerConfig.MACHINES.sawmill_bladeDamage.get(), ApiUtils.RANDOM_SOURCE, null))
				{
					this.sawblade = ItemStack.EMPTY;
					this.updateMasterBlock(null, true);
				}
			}
		}
		for(ItemStack output : secondaries)
			doSecondaryOutput(output.copy());
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		SawmillBlockEntity master = master();
		if(master!=null)
		{
			if(!isRSDisabled()&&!master.sawblade.isEmpty())
			{
				if(!player.getAbilities().invulnerable)
				{
					int consumed = master.energyStorage.extractEnergy(80, true);
					if(consumed > 0)
					{
						master.energyStorage.extractEnergy(consumed, false);
						player.hurt(IEDamageSources.sawmill, 7);
					}
				}
			}
			else
			{
				if(player.isShiftKeyDown()&&!master.sawblade.isEmpty())
				{
					if(heldItem.isEmpty())
						player.setItemInHand(hand, master.sawblade.copy());
					else if(!level.isClientSide)
						player.spawnAtLocation(master.sawblade.copy(), 0);
					master.sawblade = ItemStack.EMPTY;
					this.updateMasterBlock(null, true);
					return true;
				}
				else if(heldItem.is(IETags.sawblades))
				{
					ItemStack tempBlade = !master.sawblade.isEmpty()?master.sawblade.copy(): ItemStack.EMPTY;
					master.sawblade = ItemHandlerHelper.copyStackWithSize(heldItem, 1);
					heldItem.shrink(1);
					if(heldItem.getCount() <= 0)
						heldItem = ItemStack.EMPTY;
					else
						player.setItemInHand(hand, heldItem);
					if(!tempBlade.isEmpty())
					{
						if(heldItem.isEmpty())
							player.setItemInHand(hand, tempBlade);
						else if(!level.isClientSide)
							player.spawnAtLocation(tempBlade, 0);
					}
					this.updateMasterBlock(null, true);
					return true;
				}
			}
		}
		return false;
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES
			= CachedShapesWithTransform.createForMultiblock(SawmillBlockEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		// Slabs
		Set<BlockPos> slabs = ImmutableSet.of(
				new BlockPos(0, 0, 0),
				new BlockPos(4, 0, 0),
				new BlockPos(4, 0, 2)
		);
		if(slabs.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1).toAabbs();
		// Redstone panel feet
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
		{
			List<AABB> list = Lists.newArrayList(new AABB(0, 0, 0, 1, .5f, 1));
			list.add(new AABB(.125, .5f, .625, .25, 1, .875));
			list.add(new AABB(.75, .5f, .625, .875, 1, .875));
			return list;
		}
		// Restone panel
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, .5f, 1, 1, 1).toAabbs();
		// Stripper
		if(new BlockPos(1, 1, 1).equals(posInMultiblock))
			return Shapes.box(.25, 0, 0, .875, 1, 1).toAabbs();
		// Vacuum
		if(new BlockPos(1, 1, 2).equals(posInMultiblock))
		{
			List<AABB> list = Lists.newArrayList(new AABB(.25, 0, 0, .875, 1, .125));
			list.add(new AABB(.25, 0, .125, .875, .875, .75));
			list.add(new AABB(.1875, 0, 0, .9375, .125, .8125));
			return list;
		}
		if(new BlockPos(1, 0, 2).equals(posInMultiblock))
		{
			List<AABB> list = Lists.newArrayList(new AABB(0, 0, 0, 1, .5, 1));
			list.add(new AABB(.1875, .5, 0, .9375, 1, .8125));
			list.add(new AABB(.9375, .5, .25, 1, .875, .625));
			return list;
		}
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
		{
			List<AABB> list = Lists.newArrayList(new AABB(0, 0, 0, 1, .5, 1));
			list.add(new AABB(0, .5, .25, 1, .875, .625));
			return list;
		}
		// Conveyors
		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==1)
			return Shapes.box(0, 0, 0, 1, .125, 1).toAabbs();
		// Rest
		return Shapes.box(0, 0, 0, 1, 1, 1).toAabbs();
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(2, 1, 0, RelativeBlockFace.UP));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 2)
		);
	}

	@Override
	protected int getComparatorValueOnMaster()
	{
		float damage = 1-(this.sawblade.getDamageValue()/(float)this.sawblade.getMaxDamage());
		return Mth.ceil(damage*15);
	}

	@Override
	public boolean shouldRenderAsActiveImpl()
	{
		return energyStorage.getEnergyStored() > 0&&!isRSDisabled()&&!this.sawblade.isEmpty();
	}

	private void insertItemToProcess(ItemStack stack, boolean simulate)
	{
		if(this.sawmillProcessQueue.size() < this.getProcessQueueMaxLength())
		{
			float dist = 1;
			float minProcessDist = 0.1f;
			SawmillProcess p = null;
			if(this.sawmillProcessQueue.size() > 0)
			{
				p = this.sawmillProcessQueue.get(this.sawmillProcessQueue.size()-1);
				if(p!=null)
				{
					dist = p.getRelativeProcessStep(level);
					// either it's a different item or we have 3 together already
					if(!stack.sameItem(p.input)||combinedLogs > 2)
					{
						if(!simulate)
							combinedLogs = 0;
						minProcessDist = 0.5f;
					}
				}
			}
			else if(combinedLogs > 0)
				combinedLogs = 0;

			if(p!=null&&dist < minProcessDist)
				return;
			if(!simulate)
			{
				p = new SawmillProcess(ItemHandlerHelper.copyStackWithSize(stack, 1));
				this.sawmillProcessQueue.add(p);
				this.setChanged();
				this.markContainingBlockForUpdate(null);
				combinedLogs++;
			}
			stack.shrink(1);
		}
	}

	private static final AABB SAWBLADE_AABB = new AABB(2.6875, 1, 1.375, 4.3125, 2, 1.625);
	private static final Function<SawmillBlockEntity, AABB> CACHED_SAWBLADE_AABB = new Function<>()
	{
		final Map<Pair<Direction, Boolean>, AABB> cache = new ConcurrentHashMap<>();

		@Override
		public AABB apply(SawmillBlockEntity tile)
		{
			return cache.computeIfAbsent(Pair.of(tile.getFacing(), tile.getIsMirrored()),
							key -> CachedShapesWithTransform.withFacingAndMirror(SAWBLADE_AABB, key.getFirst(), key.getSecond()))
					.move(tile.getOrigin());
		}
	};

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(!world.isClientSide&&entity!=null&&entity.isAlive()&&!isRSDisabled())
		{
			SawmillBlockEntity master = master();
			if(master==null)
				return;
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&entity instanceof ItemEntity itemEntity)
			{
				ItemStack stack = itemEntity.getItem();
				if(stack.isEmpty())
					return;
				stack = stack.copy();
				master.insertItemToProcess(stack, false);
				if(stack.getCount() <= 0)
					entity.discard();
				else
					itemEntity.setItem(stack);
			}
			else if(entity instanceof LivingEntity&&!master.sawblade.isEmpty()
					&&CACHED_SAWBLADE_AABB.apply(master).intersects(entity.getBoundingBox()))
			{
				if(entity instanceof Player&&((Player)entity).getAbilities().invulnerable)
					return;

				int consumed = master.energyStorage.extractEnergy(80, true);
				if(consumed > 0)
				{
					master.energyStorage.extractEnergy(consumed, false);
					entity.hurt(IEDamageSources.sawmill, 7);
				}
			}
		}
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
		{
			Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
			BlockPos pos = getBlockPos().relative(outDir, 3);
			Utils.dropStackAtPos(level, pos, output, outDir);
		}
	}

	public void doSecondaryOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(secondaryOutputCap, output, false);
		if(!output.isEmpty())
		{
			DirectionalBlockPos secondaryPos = getSecondaryOutputCapPos();
			Utils.dropStackAtPos(level, secondaryPos.position(), output, secondaryPos.side().getOpposite());
		}
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
		return 6;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 6;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process)
	{
		return .5f;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public Stream<ItemStack> getDroppedItems()
	{
		return Stream.of(sawblade);
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
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return new IFluidTank[0];
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
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

	final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, SawmillBlockEntity::master,
			registerCapability(new MultiblockInventoryHandler_DirectProcessing<>(this)
			{
				@Nonnull
				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
				{
					stack = stack.copy();
					SawmillBlockEntity.this.insertItemToProcess(stack, simulate);
					return stack;
				}
			}));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==ForgeCapabilities.ITEM_HANDLER)
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().getClockWise(): this.getFacing().getCounterClockWise()))
				return insertionHandler.getAndCast();
		return super.getCapability(capability, facing);
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(new BlockPos(4, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()};
		return new Direction[0];
	}

	private DirectionalBlockPos getSecondaryOutputCapPos()
	{
		Direction shiftDir = getFacing().getOpposite();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(3, 0, 2)).relative(shiftDir), shiftDir.getOpposite());
	}

	public static class SawmillProcess
	{
		private final ItemStack input;
		private RecipeDependentData recipeDependentData;
		private int processTick;
		private boolean stripped = false;
		private boolean sawed = false;
		private boolean processFinished = false;

		public SawmillProcess(ItemStack input)
		{
			this.input = input;
		}

		private RecipeDependentData getRecipeDependentData(Level level)
		{
			if(this.recipeDependentData==null)
			{
				SawmillRecipe recipe = SawmillRecipe.findRecipe(level, input);
				if(recipe!=null)
					this.recipeDependentData = new RecipeDependentData(
							recipe,
							recipe.getTotalProcessTime(),
							recipe.getTotalProcessEnergy()/recipe.getTotalProcessTime()
					);
				else
					this.recipeDependentData = new RecipeDependentData(null, 80, 40);
			}
			return this.recipeDependentData;
		}

		public boolean processStep(SawmillBlockEntity tile, Set<ItemStack> secondaries)
		{
			RecipeDependentData data = getRecipeDependentData(tile.level);
			if(tile.energyStorage.extractEnergy(data.energyPerTick, true) >= data.energyPerTick)
			{
				tile.energyStorage.extractEnergy(data.energyPerTick, false);
				this.processTick++;
				float relative = getRelativeProcessStep(tile.level);
				if(data.recipe!=null)
				{
					if(!this.stripped&&relative >= .3125)
					{
						this.stripped = true;
						data.recipe.secondaryStripping.stream()
								.map(Lazy::get)
								.forEach(secondaries::add);
					}
					if(!this.sawed&&relative >= .8625)
					{
						this.sawed = true;
						if(!tile.sawblade.isEmpty())
							data.recipe.secondaryOutputs.stream()
									.map(Lazy::get)
									.forEach(secondaries::add);
					}
				}
				if(relative >= 1)
					this.processFinished = true;
				return true;
			}
			return false;
		}

		public float getRelativeProcessStep(Level level)
		{
			return this.processTick/getRecipeDependentData(level).maxProcessTicks;
		}

		public ItemStack getCurrentStack(Level level, boolean sawblade)
		{
			RecipeDependentData data = getRecipeDependentData(level);
			if(data.recipe==null)
				return this.input;
			// Early exit before stripping
			if(!this.stripped)
				return this.input;
			// After stripping
			ItemStack stripped = data.recipe.stripped.get();
			if(stripped.isEmpty())
				stripped = this.input;
			// Before sawing
			if(!this.sawed)
				return stripped;
			// Finally, if there is a sawblade
			return sawblade?data.recipe.output.get(): stripped;
		}

		public boolean isSawing(Level level)
		{
			return getRelativeProcessStep(level) > .5375&&!this.sawed;
		}

		public CompoundTag writeToNBT()
		{
			CompoundTag nbt = new CompoundTag();
			nbt.put("input", this.input.save(new CompoundTag()));
			nbt.putInt("processTick", this.processTick);
			nbt.putBoolean("stripped", this.stripped);
			nbt.putBoolean("sawed", this.sawed);
			return nbt;
		}

		public static SawmillProcess readFromNBT(CompoundTag nbt)
		{
			ItemStack input = ItemStack.of(nbt.getCompound("input"));
			SawmillProcess process = new SawmillProcess(input);
			process.processTick = nbt.getInt("processTick");
			process.stripped = nbt.getBoolean("stripped");
			process.sawed = nbt.getBoolean("sawed");
			return process;
		}

		private record RecipeDependentData(SawmillRecipe recipe, float maxProcessTicks, int energyPerTick)
		{
		}
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return !isRSDisabled()&&!this.sawblade.isEmpty();
	}
}