/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SawmillTileEntity extends PoweredMultiblockTileEntity<SawmillTileEntity, MultiblockRecipe>
		implements IConveyorAttachable, IBlockBounds, IPlayerInteraction
{
	public float animation_bladeRotation = 0;
	public ItemStack sawblade = ItemStack.EMPTY;
	public List<SawmillProcess> sawmillProcessQueue = new ArrayList<>();
	// this is a temporary counter to keep track of the "same" kind of log inserted. Allows combining them into threes.
	private int combinedLogs = 0;

	public SawmillTileEntity()
	{
		super(IEMultiblocks.SAWMILL, 32000, true, IETileTypes.SAWMILL.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		sawblade = ItemStack.read(nbt.getCompound("sawblade"));

		ListNBT processNBT = nbt.getList("sawmillQueue", 10);
		sawmillProcessQueue.clear();
		for(int i = 0; i < processNBT.size(); i++)
		{
			CompoundNBT tag = processNBT.getCompound(i);
			SawmillProcess process = SawmillProcess.readFromNBT(tag);
			sawmillProcessQueue.add(process);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.sawblade.isEmpty())
			nbt.put("sawblade", this.sawblade.write(new CompoundNBT()));
		ListNBT processNBT = new ListNBT();
		for(SawmillProcess process : this.sawmillProcessQueue)
			processNBT.add(process.writeToNBT());
		nbt.put("sawmillQueue", processNBT);
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	private final CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntityAt(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(4, 1, 1)).offset(outDir), outDir.getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	private final CapabilityReference<IItemHandler> secondaryOutputCap = CapabilityReference.forTileEntityAt(
			this, this::getSecondaryOutputCapPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy()||isRSDisabled())
			return;
		if(world.isRemote)
		{
			if(shouldRenderAsActive())
			{
				animation_bladeRotation += 36f;
				animation_bladeRotation %= 360f;

				if(!this.sawblade.isEmpty())
				{
					Optional<SawmillProcess> process = sawmillProcessQueue.stream()
							.filter(SawmillProcess::isSawing).findFirst();
					if(process.isPresent())
					{
						Direction particleDir = getIsMirrored()?getFacing().rotateY(): getFacing().rotateYCCW();
						AxisAlignedBB aabb = CACHED_SAWBLADE_AABB.apply(this);
						double posX = aabb.minX+world.rand.nextDouble()*(aabb.maxX-aabb.minX);
						double posY = aabb.minY+world.rand.nextDouble()*(aabb.maxY-aabb.minY);
						double posZ = aabb.minZ+world.rand.nextDouble()*(aabb.maxZ-aabb.minZ);
						double vX = world.rand.nextDouble()*particleDir.getXOffset()*0.3;
						double vY = world.rand.nextDouble()*0.3;
						double vZ = world.rand.nextDouble()*particleDir.getZOffset()*0.3;
						world.addParticle(new ItemParticleData(ParticleTypes.ITEM, process.get().getCurrentStack(true)),
								posX, posY, posZ, vX, vY, vZ);
					}
				}
			}
			return;
		}

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
				tickedProcesses++;
			if(process.processFinished)
			{
				doProcessOutput(process.getCurrentStack(!this.sawblade.isEmpty()).copy());
				processIterator.remove();
				if(this.sawblade.attemptDamageItem(IEServerConfig.MACHINES.sawmill_bladeDamage.get(), Utils.RAND, null))
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
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		SawmillTileEntity master = master();
		if(master!=null)
		{
			if(player.isSneaking()&&!master.sawblade.isEmpty())
			{
				if(heldItem.isEmpty())
					player.setHeldItem(hand, master.sawblade.copy());
				else if(!world.isRemote)
					player.entityDropItem(master.sawblade.copy(), 0);
				master.sawblade = ItemStack.EMPTY;
				this.updateMasterBlock(null, true);
				return true;
			}
			else if(IETags.sawblades.contains(heldItem.getItem()))
			{
				ItemStack tempBlade = !master.sawblade.isEmpty()?master.sawblade.copy(): ItemStack.EMPTY;
				master.sawblade = ItemHandlerHelper.copyStackWithSize(heldItem, 1);
				heldItem.shrink(1);
				if(heldItem.getCount() <= 0)
					heldItem = ItemStack.EMPTY;
				else
					player.setHeldItem(hand, heldItem);
				if(!tempBlade.isEmpty())
				{
					if(heldItem.isEmpty())
						player.setHeldItem(hand, tempBlade);
					else if(!world.isRemote)
						player.entityDropItem(tempBlade, 0);
				}
				this.updateMasterBlock(null, true);
				return true;
			}
		}
		return false;
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES
			= CachedShapesWithTransform.createForMultiblock(SawmillTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return SHAPES.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}

	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock)
	{
		// Slabs
		Set<BlockPos> slabs = ImmutableSet.of(
				new BlockPos(0, 0, 0),
				new BlockPos(4, 0, 0),
				new BlockPos(4, 0, 2)
		);
		if(slabs.contains(posInMultiblock))
			return VoxelShapes.create(0, 0, 0, 1, .5f, 1).toBoundingBoxList();
		// Redstone panel feet
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1));
			list.add(new AxisAlignedBB(.125, .5f, .625, .25, 1, .875));
			list.add(new AxisAlignedBB(.75, .5f, .625, .875, 1, .875));
			return list;
		}
		// Restone panel
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return VoxelShapes.create(0, 0, .5f, 1, 1, 1).toBoundingBoxList();
		// Stripper
		if(new BlockPos(1, 1, 1).equals(posInMultiblock))
			return VoxelShapes.create(.25, 0, 0, .875, 1, 1).toBoundingBoxList();
		// Vacuum
		if(new BlockPos(1, 1, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(.25, 0, 0, .875, 1, .125));
			list.add(new AxisAlignedBB(.25, 0, .125, .875, .875, .75));
			list.add(new AxisAlignedBB(.1875, 0, 0, .9375, .125, .8125));
			return list;
		}
		if(new BlockPos(1, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5, 1));
			list.add(new AxisAlignedBB(.1875, .5, 0, .9375, 1, .8125));
			list.add(new AxisAlignedBB(.9375, .5, .25, 1, .875, .625));
			return list;
		}
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5, 1));
			list.add(new AxisAlignedBB(0, .5, .25, 1, .875, .625));
			return list;
		}
		// Conveyors
		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==1)
			return VoxelShapes.create(0, 0, 0, 1, .125, 1).toBoundingBoxList();
		// Rest
		return VoxelShapes.create(0, 0, 0, 1, 1, 1).toBoundingBoxList();
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 0)
		);
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
		float damage = 1-(this.sawblade.getDamage()/(float)this.sawblade.getMaxDamage());
		return MathHelper.floor(damage*15);
	}

	@Override
	public boolean shouldRenderAsActive()
	{
		return getEnergyStored(null) > 0&&!isRSDisabled()&&!this.sawblade.isEmpty();
	}

	@Override
	public void replaceStructureBlock(BlockPos pos, BlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&l==1)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof ConveyorBeltTileEntity)
				((ConveyorBeltTileEntity)tile).setFacing(this.getIsMirrored()?this.getFacing().rotateYCCW(): this.getFacing().rotateY());
		}
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
					dist = p.getRelativeProcessStep();
					// either it's a different item or we have 3 together already
					if(!stack.isItemEqual(p.input)||combinedLogs > 2)
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
				this.markDirty();
				this.markContainingBlockForUpdate(null);
				combinedLogs++;
			}
			stack.shrink(1);
		}
	}

	private static final AxisAlignedBB SAWBLADE_AABB = new AxisAlignedBB(2.6875, 1, 1.375, 4.3125, 2, 1.625);
	private static final Function<SawmillTileEntity, AxisAlignedBB> CACHED_SAWBLADE_AABB = new Function<SawmillTileEntity, AxisAlignedBB>()
	{
		Map<Pair<Direction, Boolean>, AxisAlignedBB> cache = new ConcurrentHashMap<>();

		@Override
		public AxisAlignedBB apply(SawmillTileEntity tile)
		{
			return cache.computeIfAbsent(Pair.of(tile.getFacing(), tile.getIsMirrored()),
					key -> CachedShapesWithTransform.withFacingAndMirror(SAWBLADE_AABB, key.getLeft(), key.getRight()))
					.offset(tile.getOrigin());
		}
	};

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(!world.isRemote&&entity!=null&&entity.isAlive())
		{
			SawmillTileEntity master = master();
			if(master==null)
				return;
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&entity instanceof ItemEntity)
			{
				ItemStack stack = ((ItemEntity)entity).getItem();
				if(stack.isEmpty())
					return;
				master.insertItemToProcess(stack, false);
				if(stack.getCount() <= 0)
					entity.remove();
			}
			else if(entity instanceof LivingEntity&&!master.sawblade.isEmpty()
					&&CACHED_SAWBLADE_AABB.apply(master).intersects(entity.getBoundingBox()))
			{
				if(entity instanceof PlayerEntity&&((PlayerEntity)entity).abilities.disableDamage)
					return;

				int consumed = master.energyStorage.extractEnergy(80, true);
				if(consumed > 0)
				{
					master.energyStorage.extractEnergy(consumed, false);
					entity.attackEntityFrom(IEDamageSources.sawmill, 7);
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
			Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
			BlockPos pos = getPos().offset(outDir, 3);
			Utils.dropStackAtPos(world, pos, output, outDir);
		}
	}

	public void doSecondaryOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(secondaryOutputCap, output, false);
		if(!output.isEmpty())
		{
			DirectionalBlockPos secondaryPos = getSecondaryOutputCapPos();
			Utils.dropStackAtPos(world, secondaryPos.getPosition(), output, secondaryPos.getSide().getOpposite());
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
	public NonNullList<ItemStack> getDroppedItems()
	{
		return ListUtils.fromItems(sawblade);
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
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
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

	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new MultiblockInventoryHandler_DirectProcessing(this)
	{
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			stack = stack.copy();
			SawmillTileEntity.this.insertItemToProcess(stack, simulate);
			return stack;
		}
	});

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			SawmillTileEntity master = master();
			if(master==null)
				return LazyOptional.empty();
			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().rotateY(): this.getFacing().rotateYCCW()))
				return master.insertionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(new BlockPos(4, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY()};
		return new Direction[0];
	}

	private DirectionalBlockPos getSecondaryOutputCapPos()
	{
		Direction shiftDir = getFacing().getOpposite();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(3, 0, 2)).offset(shiftDir), shiftDir.getOpposite());
	}

	public static class SawmillProcess
	{
		private final ItemStack input;
		private final SawmillRecipe recipe;
		private final float maxProcessTicks;
		private final int energyPerTick;
		private int processTick;
		private boolean stripped = false;
		private boolean sawed = false;
		private boolean processFinished = false;

		public SawmillProcess(ItemStack input)
		{
			this.input = input;
			this.recipe = SawmillRecipe.findRecipe(input);
			if(this.recipe!=null)
			{
				this.maxProcessTicks = this.recipe.getTotalProcessTime();
				this.energyPerTick = this.recipe.getTotalProcessEnergy()/this.recipe.getTotalProcessTime();
			}
			else
			{
				this.maxProcessTicks = 80;
				this.energyPerTick = 40;
			}
		}

		public boolean processStep(SawmillTileEntity tile, Set<ItemStack> secondaries)
		{
			if(tile.energyStorage.extractEnergy(energyPerTick, true) >= energyPerTick)
			{
				tile.energyStorage.extractEnergy(energyPerTick, false);
				this.processTick++;
				float relative = getRelativeProcessStep();
				if(this.recipe!=null)
				{
					if(!this.stripped&&relative >= .3125)
					{
						this.stripped = true;
						secondaries.addAll(this.recipe.secondaryStripping);
					}
					if(!this.sawed&&relative >= .8625)
					{
						this.sawed = true;
						if(!tile.sawblade.isEmpty())
							secondaries.addAll(this.recipe.secondaryOutputs);
					}
				}
				if(relative >= 1)
					this.processFinished = true;
				return true;
			}
			return false;
		}

		public float getRelativeProcessStep()
		{
			return this.processTick/this.maxProcessTicks;
		}

		public ItemStack getCurrentStack(boolean sawblade)
		{
			if(this.recipe==null)
				return this.input;
			// Early exit before stripping
			if(!this.stripped)
				return this.input;
			// After stripping
			ItemStack stripped = this.recipe.stripped;
			if(stripped.isEmpty())
				stripped = this.input;
			// Before sawing
			if(!this.sawed)
				return stripped;
			// Finally, if there is a sawblade
			return sawblade?this.recipe.output: stripped;
		}

		public boolean isSawing()
		{
			return getRelativeProcessStep() > .5375&&!this.sawed;
		}

		public CompoundNBT writeToNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("input", this.input.write(new CompoundNBT()));
			nbt.putInt("processTick", this.processTick);
			nbt.putBoolean("stripped", this.stripped);
			nbt.putBoolean("sawed", this.sawed);
			return nbt;
		}

		public static SawmillProcess readFromNBT(CompoundNBT nbt)
		{
			ItemStack input = ItemStack.read(nbt.getCompound("input"));
			SawmillProcess process = new SawmillProcess(input);
			process.processTick = nbt.getInt("processTick");
			process.stripped = nbt.getBoolean("stripped");
			process.sawed = nbt.getBoolean("sawed");
			return process;
		}
	}
}