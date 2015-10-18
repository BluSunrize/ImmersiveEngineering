package blusunrize.immersiveengineering.common.blocks;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;

public class BlockFakeLight extends BlockIEBase
{
	public BlockFakeLight()
	{
		super("fakeLight", Material.air, 0, ItemBlockIEBase.class);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	@Override
	public int getRenderType()
	{
		return -1;
	}
	@Override
	public boolean isAir(IBlockAccess world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB box, List list, Entity entity)
	{
	}
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return null;
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public boolean canCollideCheck(int meta, boolean b)
	{
		return false;
	}
	@Override
	public boolean isCollidable()
	{
		return false;
	}
	@Override
	public MovingObjectPosition collisionRayTrace(World par1World, int par2, int par3, int par4, Vec3 par5Vec3, Vec3 par6Vec3)
	{
		return null;
	}
	@Override
	public int getMobilityFlag()
	{
		return 1;
	}
	@Override
	public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z)
	{
		return true;
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		if(meta==0)
			return new TileEntityFakeLight();
		return null;
	}

	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		return world.getBlockMetadata(x,y,z)==0?15:0;
	}

	public static class TileEntityFakeLight extends TileEntityIEBase implements ISpawnInterdiction
	{
		public int[] floodlightCoords = {-1,-1,-1};
		public TileEntityFakeLight()
		{
			if(!EventHandler.interdictionTiles.contains(this))
				EventHandler.interdictionTiles.add(this);
		}
		@Override
		public void updateEntity()
		{
			if(worldObj.getTotalWorldTime()%256==((xCoord^zCoord)&255))
			{
				if(floodlightCoords==null || floodlightCoords.length<3)
				{
					worldObj.setBlockToAir(xCoord, yCoord, zCoord);
					return;
				}
				TileEntity tile = worldObj.getTileEntity(floodlightCoords[0], floodlightCoords[1], floodlightCoords[2]);
				if( !(tile instanceof TileEntityFloodlight) || !((TileEntityFloodlight)tile).active)
				{
					worldObj.setBlockToAir(xCoord, yCoord, zCoord);
					return;
				}
			}

		}
		@Override
		public double getInterdictionRangeSquared()
		{
			return 1024;
		}
		@Override
		public void invalidate()
		{
			if(EventHandler.interdictionTiles.contains(this))
				EventHandler.interdictionTiles.remove(this);
			super.invalidate();
		}
		@Override
		public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
		{
			floodlightCoords = nbt.getIntArray("floodlightCoords");
		}
		@Override
		public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
		{
			nbt.setIntArray("floodlightCoords",floodlightCoords);

		}
	}
}