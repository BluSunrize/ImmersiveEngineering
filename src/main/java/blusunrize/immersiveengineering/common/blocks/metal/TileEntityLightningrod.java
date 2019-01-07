/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningrod;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityLightningrod extends TileEntityMultiblockPart<TileEntityLightningrod> implements IIEInternalFluxHandler
{
	FluxStorage energyStorage = new FluxStorage(IEConfig.Machines.lightning_output);

	@Nullable
	List<BlockPos> fenceNet = null;
	int height;
	private static final int[] size = {3, 3, 3};

	public TileEntityLightningrod()
	{
		super(size);
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(!world.isRemote&&formed&&pos==13)
		{
			if(energyStorage.getEnergyStored() > 0)
			{
				TileEntity tileEntity;
				for(EnumFacing f : EnumFacing.HORIZONTALS)
				{
					tileEntity = Utils.getExistingTileEntity(world, getPos().offset(f, 2));
					int output = EnergyHelper.insertFlux(tileEntity, f.getOpposite(), energyStorage.getLimitExtract(), true);
					output = energyStorage.extractEnergy(output, false);
					EnergyHelper.insertFlux(tileEntity, f.getOpposite(), output, false);
				}
			}

			if(world.getTotalWorldTime()%256==((getPos().getX()^getPos().getZ())&255))
				fenceNet = null;
			if(fenceNet==null)
				fenceNet = this.getFenceNet();
			if(fenceNet!=null&&fenceNet.size() > 0
					&&world.getTotalWorldTime()%128==((getPos().getX()^getPos().getZ())&127)
					&&(world.isThundering()||(world.isRaining()&&Utils.RAND.nextInt(10)==0)))
			{
				int i = this.height+this.fenceNet.size();
				if(Utils.RAND.nextInt(4096*world.getHeight()) < i*(getPos().getY()+i))
				{
					this.energyStorage.setEnergy(IEConfig.Machines.lightning_output);
					BlockPos pos = fenceNet.get(Utils.RAND.nextInt(fenceNet.size()));
					EntityLightningBolt entityLightningBolt = new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), true);
					world.addWeatherEffect(entityLightningBolt);
					world.spawnEntity(entityLightningBolt);
				}
			}
		}
	}

	@Nullable
	private List<BlockPos> getFenceNet()
	{
		this.height = 0;
		boolean broken = false;
		for(int i = getPos().getY()+2; i < world.getHeight()-1; i++)
		{
			BlockPos pos = new BlockPos(getPos().getX(), i, getPos().getZ());
			if(!broken&&isFence(pos))
				this.height++;
			else if(!world.isAirBlock(pos))
				return null;
			else
			{
				if(!broken)
					broken = true;
			}
		}

		ArrayList<BlockPos> openList = new ArrayList();
		ArrayList<BlockPos> closedList = new ArrayList();
		openList.add(getPos().add(0, height, 0));
		while(!openList.isEmpty()&&closedList.size() < 256)
		{
			BlockPos next = openList.get(0);
			if(!closedList.contains(next)&&isFence(next))
			{
				closedList.add(next);
				openList.add(next.offset(EnumFacing.WEST));
				openList.add(next.offset(EnumFacing.EAST));
				openList.add(next.offset(EnumFacing.NORTH));
				openList.add(next.offset(EnumFacing.SOUTH));
				openList.add(next.offset(EnumFacing.UP));
			}
			openList.remove(0);
		}
		return closedList;
	}

	boolean isFence(BlockPos pos)
	{
		return Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos==22)
			return new float[]{-.125f, 0, -.125f, 1.125f, 1, 1.125f};
		if(pos%9==4||(pos < 18&&pos%9%2==1))
			return new float[]{0, 0, 0, 1, 1, 1};
		if(pos < 9)
			return new float[]{0, 0, 0, 1, .5f, 1};
		float xMin = 0;
		float xMax = 1;
		float yMin = 0;
		float yMax = 1;
		float zMin = 0;
		float zMax = 1;
		if(pos%9==0||pos%9==2||pos%9==6||pos%9==8)
		{
			if(pos < 18)
			{
				yMin = -.5f;
				yMax = 1.25f;
				xMin = (facing.getAxis()==Axis.X?(pos%9 > 2^facing==EnumFacing.EAST): (pos%3==2^facing==EnumFacing.NORTH))?.8125f: .4375f;
				xMax = (facing.getAxis()==Axis.X?(pos%9 < 3^facing==EnumFacing.EAST): (pos%3==0^facing==EnumFacing.NORTH))?.1875f: .5625f;
				zMin = (facing.getAxis()==Axis.X?(pos%3==2^facing==EnumFacing.EAST): (pos%9 < 3^facing==EnumFacing.NORTH))?.8125f: .4375f;
				zMax = (facing.getAxis()==Axis.X?(pos%3==0^facing==EnumFacing.EAST): (pos%9 > 2^facing==EnumFacing.NORTH))?.1875f: .5625f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = (facing.getAxis()==Axis.X?(pos%9 > 2^facing==EnumFacing.EAST): (pos%3==2^facing==EnumFacing.NORTH))?1: .625f;
				xMax = (facing.getAxis()==Axis.X?(pos%9 < 3^facing==EnumFacing.EAST): (pos%3==0^facing==EnumFacing.NORTH))?0: .375f;
				zMin = (facing.getAxis()==Axis.X?(pos%3==2^facing==EnumFacing.EAST): (pos%9 < 3^facing==EnumFacing.NORTH))?1: .625f;
				zMax = (facing.getAxis()==Axis.X?(pos%3==0^facing==EnumFacing.EAST): (pos%9 > 2^facing==EnumFacing.NORTH))?0: .375f;
			}
		}
		else if(pos > 17)
		{
			yMin = .25f;
			yMax = .75f;
			xMin = offset[0] < 0?.375f: 0;
			xMax = offset[0] > 0?.625f: 1;
			zMin = offset[2] < 0?.375f: 0;
			zMax = offset[2] > 0?.625f: 1;
		}
		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos < 0)
			return ItemStack.EMPTY;
		ItemStack s = ItemStack.EMPTY;
		try
		{
			s = MultiblockLightningrod.instance.getStructureManual()[pos/9][pos%9/3][pos%3];
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return s.copy();
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(pos==4)
				renderAABB = new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 5, 2));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		TileEntityLightningrod master = this.master();
		if(master!=null)
			return master.energyStorage;
		return energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable EnumFacing facing)
	{
		return this.formed&&this.isEnergyPos()?SideConfig.OUTPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(this.formed&&this.isEnergyPos())
			return wrapper;
		return null;
	}

	private boolean isEnergyPos()
	{
		return pos==10||pos==12||pos==14||pos==16;
	}

	//	@Override
//	public int extractEnergy(@Nullable EnumFacing from, int energy, boolean simulate)
//	{
//		if(pos!=10&&pos!=12&&pos!=14&pos!=16)
//			return 0;
//		TileEntityLightningrod master = master();
//		return master==null?0: master.energyStorage.extractEnergy(energy, simulate);
//	}
//
//	@Override
//	public int getEnergyStored(@Nullable EnumFacing from)
//	{
//		TileEntityLightningrod master = master();
//		return master==null?0: master.energyStorage.getEnergyStored();
//	}
//
//	@Override
//	public int getMaxEnergyStored(@Nullable EnumFacing from)
//	{
//		TileEntityLightningrod master = master();
//		return master==null?0: master.energyStorage.getMaxEnergyStored();
//	}
//
//	@Override
//	public boolean canConnectEnergy(@Nullable EnumFacing from)
//	{
//		return ;
//	}
}