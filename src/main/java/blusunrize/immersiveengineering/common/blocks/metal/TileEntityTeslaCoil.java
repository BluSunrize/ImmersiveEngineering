package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTeslaCoil extends TileEntityIEBase implements ITickable, IFluxReceiver,IEnergyReceiver, IHasDummyBlocks, IBlockBounds
{
	public boolean dummy = false;
	public FluxStorage energyStorage = new FluxStorage(16000);
	public boolean redstoneInverted = false;
	@SideOnly(Side.CLIENT)
	public static ArrayListMultimap<BlockPos,LightningAnimation> effectMap = ArrayListMultimap.create();
	
	@Override
	public void update()
	{
		if(!dummy && !worldObj.isRemote && (worldObj.isBlockIndirectlyGettingPowered(getPos())>0|redstoneInverted))
		{
			double radius = 6;
			AxisAlignedBB aabb = AxisAlignedBB.fromBounds(getPos().getX()-radius,getPos().getY()-1,getPos().getZ()-radius, getPos().getX()+radius,getPos().getY()+radius,getPos().getZ()+radius);
			List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
			DamageSource dmgsrc = IEDamageSources.causeCrusherDamage();
			for(EntityLivingBase entity : targets)
				if(!entity.isEntityInvulnerable(dmgsrc))
				{
					entity.attackEntityFrom(dmgsrc, 4);
				}
		}
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getBoolean("dummy");
		redstoneInverted = nbt.getBoolean("redstoneInverted");
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("dummy", dummy);
		nbt.setBoolean("redstoneInverted", redstoneInverted);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public float[] getBlockBounds()
	{
		return dummy?new float[]{.125f,0,.125f, .875f,.875f,.875f}:null;
	}
	@Override
	public float[] getSpecialCollisionBounds()
	{
		return null;
	}
	@Override
	public float[] getSpecialSelectionBounds()
	{
		return null;
	}

	@Override
	public boolean isDummy()
	{
		return dummy;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		worldObj.setBlockState(pos.add(0,1,0), state);
		((TileEntityTeslaCoil)worldObj.getTileEntity(pos.add(0,1,0))).dummy = true;
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i=0; i<=1; i++)
			if(worldObj.getTileEntity(getPos().add(0,dummy?-1:0,0).add(0,i,0)) instanceof TileEntityTeslaCoil)
				worldObj.setBlockToAir(getPos().add(0,dummy?-1:0,0).add(0,i,0));
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from)
	{
		return !dummy;
	}
	@Override
	public int receiveEnergy(EnumFacing from, int energy, boolean simulate)
	{
		return energyStorage.receiveEnergy(energy, simulate);
	}
	@Override
	public int getEnergyStored(EnumFacing from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-1,0));
			if(te instanceof TileEntityTeslaCoil)	
				return ((TileEntityTeslaCoil)te).getEnergyStored(from);
			return 0;
		}
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(EnumFacing from)
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().add(0,-1,0));
			if(te instanceof TileEntityTeslaCoil)	
				return ((TileEntityTeslaCoil)te).getMaxEnergyStored(from);
			return 0;
		}
		return energyStorage.getMaxEnergyStored();
	}

	public static class LightningAnimation
	{
		Vec3 startPos;
		EntityLivingBase targetEntity;
		Vec3 targetPos;
		int timer;
		
		
	}
}