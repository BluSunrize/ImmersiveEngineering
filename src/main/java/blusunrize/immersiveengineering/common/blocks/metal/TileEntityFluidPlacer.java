/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityFluidPlacer extends TileEntityIEBase implements ITickable, IConfigurableSides, IBlockOverlayText
{
	public int[] sideConfig = new int[]{1, 0, 1, 1, 1, 1};
	public FluidTank tank = new FluidTank(4000);

	private int tickCount = 0;

	HashSet<BlockPos> checkedPositions = new HashSet<BlockPos>();
	TreeMap<Integer, Queue<BlockPos>> layeredPlacementQueue = new TreeMap<Integer, Queue<BlockPos>>();
	HashSet<BlockPos> tempFluids = new HashSet<BlockPos>();

	@Override
	public void update()
	{
		if(getWorld().isRemote||getWorld().getRedstonePowerFromNeighbors(getPos())!=0)
			return;

		if(tickCount%16==0)
		{
			if(tickCount%512==0)//Initial placement
				prepareAreaCheck();
			if(tank.getFluidAmount() >= Fluid.BUCKET_VOLUME&&tank.getFluid().getFluid().getBlock()!=null&&!layeredPlacementQueue.isEmpty())
			{
				Queue<BlockPos> lowestLayer = layeredPlacementQueue.firstEntry().getValue();
				if(lowestLayer==null||lowestLayer.isEmpty())
					layeredPlacementQueue.pollFirstEntry();
				else
				{
					BlockPos targetPos = lowestLayer.poll();
					IBlockState state = getWorld().getBlockState(targetPos);
					if((state.getBlock().isAir(state, getWorld(), targetPos)||!state.getMaterial().isSolid())&&!isFullFluidBlock(targetPos, state))
						if(tryPlaceFluid(null, getWorld(), tank.getFluid(), targetPos))
						{
							tank.drain(Fluid.BUCKET_VOLUME, true);
							addConnectedSpaces(targetPos);
							handleTempFluids();
						}
				}
			}
		}
		tickCount++;
	}

	//FIXME: Blatantly stolen from Forge. I'm going to do a PR to return this method back to forge, but for now...
	//Forge changed this method to require an ItemStack which isn't appropriate here.
	//Mezz reported he was doing further work in this space for us, we should be able to remove this soon.
	public static boolean tryPlaceFluid(@Nullable EntityPlayer player, World worldIn, FluidStack fluidStack, BlockPos pos)
	{
		if(worldIn==null||fluidStack==null||pos==null)
		{
			return false;
		}

		Fluid fluid = fluidStack.getFluid();
		if(fluid==null||!fluid.canBePlacedInWorld())
		{
			return false;
		}

		// check that we can place the fluid at the destination
		IBlockState destBlockState = worldIn.getBlockState(pos);
		Material destMaterial = destBlockState.getMaterial();
		boolean isDestNonSolid = !destMaterial.isSolid();
		boolean isDestReplaceable = destBlockState.getBlock().isReplaceable(worldIn, pos);
		if(!worldIn.isAirBlock(pos)&&!isDestNonSolid&&!isDestReplaceable)
		{
			return false; // Non-air, solid, unreplacable block. We can't put fluid here.
		}

		if(worldIn.provider.doesWaterVaporize()&&fluid.doesVaporize(fluidStack))
		{
			fluid.vaporize(player, worldIn, pos, fluidStack);
		}
		else
		{
			if(!worldIn.isRemote&&(isDestNonSolid||isDestReplaceable)&&!destMaterial.isLiquid())
			{
				worldIn.destroyBlock(pos, true);
			}

			SoundEvent soundevent = fluid.getEmptySound(fluidStack);
			worldIn.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);

			IBlockState fluidBlockState = fluid.getBlock().getDefaultState();
			worldIn.setBlockState(pos, fluidBlockState, 11);
		}
		return true;
	}

	private void prepareAreaCheck()
	{
		checkedPositions.clear();
		layeredPlacementQueue.clear();
		tempFluids.clear();

		addConnectedSpaces(getPos());
		handleTempFluids();
	}

	private Queue<BlockPos> getQueueForYLevel(int yLevel)
	{
		Queue<BlockPos> queue = layeredPlacementQueue.get(yLevel);
		if(queue==null)
		{
			queue = new LinkedList<BlockPos>();
			layeredPlacementQueue.put(yLevel, queue);
		}
		return queue;
	}

	private void addConnectedSpaces(BlockPos pos)
	{
		for(EnumFacing facing : EnumFacing.values())
			if(facing!=EnumFacing.UP&&(pos!=getPos()||sideConfig[facing.ordinal()]==1))
				addToQueue(pos.offset(facing));
	}

	private void addToQueue(BlockPos pos)
	{
		if(pos.getY() >= 0&&pos.getY() <= 255)//Within world borders
			if(checkedPositions.add(pos))//Don't add checked positions
				if(pos.distanceSq(getPos()) < 64*64)//Within max range
					if(getWorld().isBlockLoaded(pos))
					{
						IBlockState state = getWorld().getBlockState(pos);
						if(tank.getFluid()!=null&&tank.getFluid().getFluid()==FluidRegistry.lookupFluidForBlock(state.getBlock()))
							tempFluids.add(pos);
						if((state.getBlock().isAir(state, getWorld(), pos)||!state.getMaterial().isSolid())&&!isFullFluidBlock(pos, state))
							getQueueForYLevel(pos.getY()).add(pos);
					}
	}

	private void handleTempFluids()
	{
		Set<BlockPos> tempFluidsCopy = tempFluids;//preventing CMEs >_>
		tempFluids = new HashSet<>();
		for(BlockPos pos : tempFluidsCopy)
			addConnectedSpaces(pos);
	}

	private boolean isFullFluidBlock(BlockPos pos, IBlockState state)
	{
		if(state.getBlock() instanceof IFluidBlock)
			return Math.abs(((IFluidBlock)state.getBlock()).getFilledPercentage(getWorld(), pos))==1;
		else if(state.getBlock() instanceof BlockLiquid)
			return state.getBlock().getMetaFromState(state)==0;
		return false;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null||sideConfig.length!=6)
			sideConfig = new int[]{1, 0, 1, 1, 1, 1};
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setIntArray("sideConfig", sideConfig);
		nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public SideConfig getSideConfig(int side)
	{
		return (side >= 0&&side < 6)?SideConfig.values()[this.sideConfig[side]+1]: SideConfig.NONE;
	}

	@Override
	public boolean toggleSide(int side, EntityPlayer p)
	{
		sideConfig[side]++;
		if(sideConfig[side] > 1)
			sideConfig[side] = -1;
		prepareAreaCheck();
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		getWorld().addBlockEvent(getPos(), this.getBlockType(), 0, 0);
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||sideConfig[facing.ordinal()]==0))
			return true;
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&(facing==null||sideConfig[facing.ordinal()]==0))
			return (T)tank;
		return super.getCapability(capability, facing);
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		if(hammer&&IEConfig.colourblindSupport)
		{
			int i = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.ordinal())];
			int j = sideConfig[Math.min(sideConfig.length-1, mop.sideHit.getOpposite().ordinal())];
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+i),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+j)
			};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}
}