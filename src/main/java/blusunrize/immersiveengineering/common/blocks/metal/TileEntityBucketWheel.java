package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockBucketWheel;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityBucketWheel extends TileEntityMultiblockPart
{
	public int facing = 2;
	public float rotation = 0;
	public ItemStack[] digStacks = new ItemStack[8];
	public boolean active = false;

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = pos<0?null: MultiblockBucketWheel.instance.getStructureManual()[pos/7][pos%7][0];
		return s!=null?s.copy():null;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		rotation = nbt.getFloat("rotation");
		NBTTagList invList = nbt.getTagList("digStacks", 10);
		digStacks = new ItemStack[8];
		for (int i=0; i<invList.tagCount(); i++)
		{
			NBTTagCompound itemTag = invList.getCompoundTagAt(i);
			int slot = itemTag.getByte("Slot") & 255;
			if(slot>=0 && slot<this.digStacks.length)
				this.digStacks[slot] = ItemStack.loadItemStackFromNBT(itemTag);
		}
		active = nbt.getBoolean("active");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setFloat("rotation", rotation);
		NBTTagList invList = new NBTTagList();
		for(int i=0; i<this.digStacks.length; i++)
			if(this.digStacks[i] != null)
			{
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				this.digStacks[i].writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
		nbt.setTag("digStacks", invList);
		nbt.setBoolean("active", active);
	}


	@Override
	public void updateEntity()
	{
		if(!formed || pos!=24)
			return;

		if(active)
		{
			rotation+=(float)Config.getDouble("excavator_speed");
			rotation%=360;
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int startX = xCoord-offset[0];
			int startY = yCoord-offset[1];
			int startZ = zCoord-offset[2];
			
			for(int w=-3;w<=3;w++)
				for(int h=-3;h<=3;h++)
				{
					int xx = (f==3?-w: f==2?w: 0);
					int yy = h;
					int zz = (f==5?-w: f==4?w: 0);

					ItemStack s = null;
					if(worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz) instanceof TileEntityBucketWheel)
					{
						s = ((TileEntityBucketWheel)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).getOriginalBlock();
						((TileEntityBucketWheel)worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz)).formed=false;
					}
					if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
						s = this.getOriginalBlock();
					if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
					{
						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
							worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+.5,yCoord+.5,zCoord+.5, s));
						else
						{
							if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
								worldObj.setBlockToAir(startX+xx,startY+yy,startZ+zz);
							worldObj.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), s.getItemDamage(), 0x3);
						}
					}
				}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		try{
			if(id<1)
			{
				this.active = arg==1;
			}
			else
				if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
				{
					if(id==1)
					{
						Block block = Block.getBlockById(arg & 4095);
						int meta = (arg>>12) & 255;
						ItemStack ss = new ItemStack(block, 1, meta);
						if(ss!=null)
							ImmersiveEngineering.proxy.spawnBucketWheelFX(this, ss);
					}
					else if(id<10)
					{
						int target = id-2;
						if(arg<=0)
							this.digStacks[target] = null;
						else
						{
							Block block = Block.getBlockById(arg & 4095);
							int meta = (arg>>12) & 255;
							ItemStack ss = new ItemStack(block, 1, meta);
							this.digStacks[target] = ss;
						}
					}
				}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==24)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing<4?3:0),yCoord-3,zCoord-(facing>3?3:0), xCoord+(facing<4?4:1),yCoord+4,zCoord+(facing>3?4:1));
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	@Override
	public float[] getBlockBounds()
	{
		if(pos==3||pos==9||pos==11)
			return new float[]{0,.25f,0, 1,1,1};
		else if(pos==45||pos==37||pos==39)
			return new float[]{0,0,0, 1,.75f,1};
		else if(pos==21)
			return new float[]{facing==2?.25f:0,0,facing==4?.25f:0, facing==3?.75f:1,1,facing==5?.75f:1};
		else if(pos==27)
			return new float[]{facing==3?.25f:0,0,facing==5?.25f:0, facing==2?.75f:1,1,facing==4?.75f:1};
		else if(pos==15||pos==29)
			return new float[]{facing==2?.25f:0,0,facing==4?.25f:0, facing==3?.75f:1,1,facing==5?.75f:1};
		else if(pos==19||pos==33)
			return new float[]{facing==3?.25f:0,0,facing==5?.25f:0, facing==2?.75f:1,1,facing==4?.75f:1};
		return new float[]{0,0,0,1,1,1};
	}

}