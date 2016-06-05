package blusunrize.immersiveengineering.common.blocks;

import java.util.ArrayList;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ILightValue;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFakeLight extends BlockIETileProvider
{
	public BlockFakeLight()
	{
		super("fakeLight", Material.air, PropertyEnum.create("type", BlockTypes_FakeLight.class), ItemBlockIEBase.class);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	@Override
	public int getRenderType()
	{
		return -1;
	}
	@Override
	public boolean isAir(IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		return null;
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		return null;
	}

	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return new ArrayList<ItemStack>();
	}
	
	@Override
	public boolean canCollideCheck(IBlockState state, boolean b)
	{
		return false;
	}
	@Override
	public boolean isCollidable()
	{
		return false;
	}
	@Override
	public MovingObjectPosition collisionRayTrace(World par1World, BlockPos pos, Vec3 par5Vec3, Vec3 par6Vec3)
	{
		return null;
	}
	@Override
	public int getMobilityFlag()
	{
		return 1;
	}
	@Override
	public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos)
	{
		return true;
	}


	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityFakeLight();
	}

	public static class TileEntityFakeLight extends TileEntityIEBase implements ITickable, ISpawnInterdiction, ILightValue
	{
		public int[] floodlightCoords = {-1,-1,-1};
		public TileEntityFakeLight()
		{
			if(Config.getBoolean("floodlight_spawnPrevent"))
				synchronized (EventHandler.interdictionTiles) {
					if (!EventHandler.interdictionTiles.contains(this))
						EventHandler.interdictionTiles.add(this);
				}
		}
		@Override
		public void update()
		{
			if(worldObj.getTotalWorldTime()%256==((getPos().getX()^getPos().getZ())&255))
			{
				if(floodlightCoords==null || floodlightCoords.length<3)
				{
					worldObj.setBlockToAir(getPos());
					return;
				}
				BlockPos floodlightPos = new BlockPos(floodlightCoords[0], floodlightCoords[1], floodlightCoords[2]);
				TileEntity tile = worldObj.getTileEntity(floodlightPos);
				if( !(tile instanceof TileEntityFloodlight) || !((TileEntityFloodlight)tile).active)
				{
					worldObj.setBlockToAir(getPos());
					return;
				}
			}

		}
		@Override
		public int getLightValue()
		{
			return 15;
		}
		@Override
		public double getInterdictionRangeSquared()
		{
			return 1024;
		}
		@Override
		public void invalidate()
		{
			synchronized (EventHandler.interdictionTiles) {
				if (EventHandler.interdictionTiles.contains(this))
					EventHandler.interdictionTiles.remove(this);
			}
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