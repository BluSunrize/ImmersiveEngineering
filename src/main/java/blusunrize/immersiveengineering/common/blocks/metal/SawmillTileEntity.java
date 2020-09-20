/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

// Todo, replace all the commented out bottling machine code with proper code
public class SawmillTileEntity extends PoweredMultiblockTileEntity<SawmillTileEntity, MultiblockRecipe>
		implements IConveyorAttachable, IBlockBounds
{
	public float animation_bladeRotation = 0;

	public SawmillTileEntity()
	{
		super(IEMultiblocks.SAWMILL, 32000, true, IETileTypes.SAWMILL.get());
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		ListNBT processNBT = nbt.getList("bottlingQueue", 10);
//		bottlingProcessQueue.clear();
//		for(int i = 0; i < processNBT.size(); i++)
//		{
//			CompoundNBT tag = processNBT.getCompound(i);
//			BottlingProcess process = BottlingProcess.readFromNBT(tag);
//			bottlingProcessQueue.add(process);
//		}
//		tanks[0].readFromNBT(nbt.getCompound("tank"));
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
//		ListNBT processNBT = new ListNBT();
//		for(BottlingProcess process : this.bottlingProcessQueue)
//			processNBT.add(process.writeToNBT());
//		nbt.put("bottlingQueue", processNBT);
//		nbt.put("tank", tanks[0].writeToNBT(new CompoundNBT()));
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this, () -> {
		Direction outDir = getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY();
		return new DirectionalBlockPos(getBlockPosForPos(new BlockPos(2, 1, 1)).offset(outDir), outDir.getOpposite());
	}, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		super.tick();

		if(isDummy()||isRSDisabled()||world.isRemote)
			return;

		if(shouldRenderAsActive())
		{
			animation_bladeRotation += 36f;
			animation_bladeRotation %= 360f;
		}

//		tickedProcesses = 0;
//		int max = getMaxProcessPerTick();
//		int i = 0;
//		Iterator<BottlingProcess> processIterator = bottlingProcessQueue.iterator();
//		tickedProcesses = 0;
//		while(processIterator.hasNext()&&i++ < max)
//		{
//			BottlingProcess process = processIterator.next();
//			if(process.processStep(this))
//				tickedProcesses++;
//			if(process.processFinished)
//			{
//				ItemStack output = !process.items.get(1).isEmpty()?process.items.get(1): process.items.get(0);
//				doProcessOutput(output);
//				processIterator.remove();
//			}
//		}
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
				new BlockPos(1, 0, 1)
		);
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

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
//		if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&!world.isRemote&&entity!=null&&entity.isAlive()&&entity instanceof ItemEntity)
//		{
//			SawmillTileEntity master = master();
//			if(master==null)
//				return;
//			ItemStack stack = ((ItemEntity)entity).getItem();
//			if(stack.isEmpty())
//				return;
//
//			if(master.bottlingProcessQueue.size() < master.getProcessQueueMaxLength())
//			{
//				float dist = 1;
//				BottlingProcess p = null;
//				if(master.bottlingProcessQueue.size() > 0)
//				{
//					p = master.bottlingProcessQueue.get(master.bottlingProcessQueue.size()-1);
//					if(p!=null)
//						dist = p.processTick/(float)p.maxProcessTick;
//				}
//				if(p!=null&&dist < master.getMinProcessDistance(null))
//					return;
//
//				p = new BottlingProcess(Utils.copyStackWithAmount(stack, 1));
//				master.bottlingProcessQueue.add(p);
//				master.markDirty();
//				master.markContainingBlockForUpdate(null);
//				stack.shrink(1);
//				if(stack.getCount() <= 0)
//					entity.remove();
//			}
//		}
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
			BlockPos pos = getPos().offset(outDir, 2);
			Utils.dropStackAtPos(world, pos, output, outDir);
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
		return 2;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 2;
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

//	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new BottlingMachineInventoryHandler(this));

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
//			SawmillTileEntity master = master();
//			if(master==null)
//				return LazyOptional.empty();
//			if(new BlockPos(0, 1, 1).equals(posInMultiblock)&&facing==(getIsMirrored()?this.getFacing().rotateY(): this.getFacing().rotateYCCW()))
//				return master.insertionHandler.cast();
//			return LazyOptional.empty();
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
		if(new BlockPos(2, 1, 1).equals(posInMultiblock))
			return new Direction[]{getIsMirrored()?getFacing().rotateYCCW(): getFacing().rotateY()};
		return new Direction[0];
	}
}