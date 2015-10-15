package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockExcavator;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityExcavator extends TileEntityMultiblockPart implements IEnergyReceiver
{
	public int facing = 2;
	public EnergyStorage energyStorage = new EnergyStorage(64000);
	public boolean active = false;

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		int h = pos%9/3;
		int l = pos/9;
		int w = pos%3;
		ItemStack s = MultiblockExcavator.instance.getStructureManual()[h][5-l][w];
		return s!=null?s.copy():null;
	}

	@Override
	public TileEntityExcavator master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityExcavator?(TileEntityExcavator)te : null;
	}

	@Override
	public void updateEntity()
	{
		if(!formed || pos!=4)
			return;

		int[] wheelAxis = {xCoord+(facing==5?-4:facing==4?4:0),yCoord,zCoord+(facing==3?-4:facing==2?4:0)};

		int ff = ForgeDirection.ROTATION_MATRIX[facing][1];
		float rot = 0;
		int target = -1;
		TileEntityBucketWheel wheel = null;
		TileEntity center = worldObj.getTileEntity(wheelAxis[0], wheelAxis[1], wheelAxis[2]);

		if(!worldObj.isRemote)
		{
			if(center instanceof TileEntityBucketWheel)
			{
				if(((TileEntityBucketWheel) center).facing==ff)
				{
					wheel = ((TileEntityBucketWheel) center);
					if(active!=wheel.active)
						worldObj.addBlockEvent(wheel.xCoord, wheel.yCoord, wheel.zCoord, wheel.getBlockType(), 0, active? 1: 0);
					rot = wheel.rotation;
					if(rot%45>40)
						target = Math.round(rot/360f*8)%8;
				}

				//Fix the wheel if necessary
				if(((TileEntityBucketWheel)center).facing!=ff || ((TileEntityBucketWheel)center).mirrored!=this.mirrored)
					for(int h=-3;h<=3;h++)
						for(int w=-3;w<=3;w++)
						{
							TileEntity te = worldObj.getTileEntity(wheelAxis[0]+(facing>3?w:0), wheelAxis[1]+h, wheelAxis[2]+(facing<4?w:0));
							if(te instanceof TileEntityBucketWheel)
							{
								((TileEntityBucketWheel)te).facing = ff;
								((TileEntityBucketWheel)te).mirrored = this.mirrored;
								te.markDirty();
								worldObj.markBlockForUpdate(te.xCoord,te.yCoord,te.zCoord);
							}
						}
			}

			boolean update = false;
			ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(worldObj, wheelAxis[0]>>4, wheelAxis[2]>>4);
			if(wheel!=null && !worldObj.isBlockIndirectlyGettingPowered(xCoord+(facing==3?-1:facing==2?1:0)*(mirrored?-1:1),yCoord,zCoord+(facing==4?-1:facing==5?1:0)*(mirrored?-1:1)))
			{
				int consumed = Config.getInt("excavator_consumption");
				int extracted = energyStorage.extractEnergy(consumed, true);
				if(extracted>=consumed)
				{
					energyStorage.extractEnergy(consumed, false);
					active = true;
					update = true;

					if(target>=0 && target<8)
					{
						if(wheel.digStacks[(target+4)%8]==null)
						{
							ItemStack blocking = this.digBlocksInTheWay(wheel);
							if(blocking!=null)
							{
								wheel.digStacks[(target+4)%8] = blocking;
								wheel.markDirty();
								worldObj.markBlockForUpdate(wheel.xCoord, wheel.yCoord, wheel.zCoord);
							}
							else if(mineral!=null 
									&& !worldObj.isAirBlock(wheel.xCoord+(facing==5?2:facing==4?-2:0),wheel.yCoord-5,wheel.zCoord+(facing==3?2:facing==2?-2:0))
									&& !worldObj.isAirBlock(wheel.xCoord+(facing==5?-2:facing==4?2:0),wheel.yCoord-5,wheel.zCoord+(facing==3?-2:facing==2?2:0))
									&& !worldObj.isAirBlock(wheel.xCoord+(facing==5?1:facing==4?-1:0),wheel.yCoord-5,wheel.zCoord+(facing==3?1:facing==2?-1:0))
									&& !worldObj.isAirBlock(wheel.xCoord+(facing==5?-1:facing==4?1:0),wheel.yCoord-5,wheel.zCoord+(facing==3?-1:facing==2?1:0))
									&& !worldObj.isAirBlock(wheel.xCoord,wheel.yCoord-5,wheel.zCoord))
							{
								ItemStack ore = mineral.getRandomOre(worldObj.rand);
								float configChance = worldObj.rand.nextFloat();
								float failChance = worldObj.rand.nextFloat();
								if(ore!=null && configChance>Config.getDouble("excavator_chance") && failChance>mineral.failChance)
								{
									wheel.digStacks[(target+4)%8] = ore;
									wheel.markDirty();
									worldObj.markBlockForUpdate(wheel.xCoord, wheel.yCoord, wheel.zCoord);
								}
								ExcavatorHandler.depleteMinerals(worldObj, wheelAxis[0]>>4, wheelAxis[2]>>4);
							}
						}
						if(wheel.digStacks[target]!=null)
						{
							this.outputItem(wheel.digStacks[target].copy());
							Block b = Block.getBlockFromItem(wheel.digStacks[target].getItem());
							if(b!=null&&b!=Blocks.air)
								wheel.particleStack = wheel.digStacks[target].copy();
							wheel.digStacks[target] = null;
							wheel.markDirty();
							worldObj.markBlockForUpdate(wheel.xCoord, wheel.yCoord, wheel.zCoord);
						}
					}
				}
				else
				{
					active= false;
					update = true;
				}
			}
			else if(active)
			{
				active=false;
				update = true;
			}
			if(update)
			{
				this.markDirty();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}

		}
	}

	public void outputItem(ItemStack stack)
	{
		ForgeDirection fd = ForgeDirection.getOrientation(facing);
		TileEntity inventory = this.worldObj.getTileEntity(xCoord+fd.offsetX, yCoord, zCoord+fd.offsetZ);
		if(isInventory(inventory, ForgeDirection.OPPOSITES[facing]))
			stack = Utils.insertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[facing]);

		if(stack != null)
		{
			EntityItem ei = new EntityItem(worldObj, xCoord+.5+fd.offsetX, yCoord+.5, zCoord+.5+fd.offsetZ, stack.copy());
			ei.motionX = (0.075F * fd.offsetX);
			ei.motionY = 0.025000000372529D;
			ei.motionZ = (0.075F * fd.offsetZ);
			this.worldObj.spawnEntityInWorld(ei);
		}
	}
	boolean isInventory(TileEntity tile, int side)
	{
		if(tile instanceof ISidedInventory && ((ISidedInventory)tile).getAccessibleSlotsFromSide(side).length>0)
			return true;
		if(tile instanceof IInventory && ((IInventory)tile).getSizeInventory()>0)
			return true;
		return false;
	}

	ItemStack digBlocksInTheWay(TileEntityBucketWheel wheel)
	{
		ItemStack s = digBlock(wheel.xCoord,wheel.yCoord-4,wheel.zCoord);
		if(s!=null)
			return s;
		//Backward 1
		s = digBlock(wheel.xCoord+(facing==5?1:facing==4?-1:0),wheel.yCoord-4,wheel.zCoord+(facing==3?1:facing==2?-1:0));
		if(s!=null)
			return s;
		//Backward 2
		s = digBlock(wheel.xCoord+(facing==5?2:facing==4?-2:0),wheel.yCoord-4,wheel.zCoord+(facing==3?2:facing==2?-2:0));
		if(s!=null)
			return s;
		//Forward 1
		s = digBlock(wheel.xCoord+(facing==5?-1:facing==4?1:0),wheel.yCoord-4,wheel.zCoord+(facing==3?-1:facing==2?1:0));
		if(s!=null)
			return s;
		//Forward 2
		s = digBlock(wheel.xCoord+(facing==5?-2:facing==4?2:0),wheel.yCoord-4,wheel.zCoord+(facing==3?-2:facing==2?2:0));
		if(s!=null)
			return s;

		//Backward+Sides
		s = digBlock(wheel.xCoord+(facing==5?1:facing==4?-1:1),wheel.yCoord-4,wheel.zCoord+(facing==3?1:facing==2?-1:1));
		if(s!=null)
			return s;
		s = digBlock(wheel.xCoord+(facing==5?1:facing==4?-1:-1),wheel.yCoord-4,wheel.zCoord+(facing==3?1:facing==2?-1:-1));
		if(s!=null)
			return s;
		//Center Sides
		s = digBlock(wheel.xCoord+(facing<4?1:0),wheel.yCoord-4,wheel.zCoord+(facing>3?1:0));
		if(s!=null)
			return s;
		s = digBlock(wheel.xCoord+(facing<4?-1:0),wheel.yCoord-4,wheel.zCoord+(facing>3?-1:0));
		if(s!=null)
			return s;
		//Forward+Sides
		s = digBlock(wheel.xCoord+(facing==5?-1:facing==4?1:1),wheel.yCoord-4,wheel.zCoord+(facing==3?-1:facing==2?1:1));
		if(s!=null)
			return s;
		s = digBlock(wheel.xCoord+(facing==5?-1:facing==4?1:-1),wheel.yCoord-4,wheel.zCoord+(facing==3?-1:facing==2?1:-1));
		if(s!=null)
			return s;
		return null;
	}


	ItemStack digBlock(int x, int y, int z)
	{
		if(!(worldObj instanceof WorldServer))
			return null;
		FakePlayer fakePlayer = FakePlayerUtil.getFakePlayer((WorldServer) worldObj);
		Block block = worldObj.getBlock(x,y,z);
		int meta = worldObj.getBlockMetadata(x, y, z);
		if(block!=null && !worldObj.isAirBlock(x, y, z) && block.getPlayerRelativeBlockHardness(fakePlayer, worldObj, x, y, z)!=0)
		{
			if(!block.canHarvestBlock(fakePlayer, meta))
				return null;
			block.onBlockHarvested(worldObj, x,y,z, meta, fakePlayer);
			if(block.removedByPlayer(worldObj, fakePlayer, x,y,z, true))
			{
				block.onBlockDestroyedByPlayer( worldObj, x,y,z, meta);
				if(block.canSilkHarvest(worldObj, fakePlayer, x, y, z, meta))
				{
					ArrayList<ItemStack> items = new ArrayList<ItemStack>();
					Item bitem = Item.getItemFromBlock(block);
					if(bitem==null)
						return null;
					int m = 0;
					if(bitem!=null && bitem.getHasSubtypes())
						m = meta;
					ItemStack itemstack = new ItemStack(bitem, 1, m);
					if (itemstack != null)
						items.add(itemstack);

					ForgeEventFactory.fireBlockHarvesting(items, worldObj, block, x, y, z, meta, 0, 1.0f, true, fakePlayer);

					for(int i=0; i<items.size(); i++)
						if(i!=0)
						{
							EntityItem ei = new EntityItem(worldObj, xCoord+.5, yCoord+.5, zCoord+.5, items.get(i).copy());
							this.worldObj.spawnEntityInWorld(ei);
						}
					worldObj.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
					if(items.size()>0)
						return items.get(0);
				}
				else
				{
					block.harvestBlock(worldObj, fakePlayer, x,y,z, meta);
					worldObj.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
				}
			}
		}
		return null;
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		active = nbt.getBoolean("active");
		energyStorage.readFromNBT(nbt);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setBoolean("active", active);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/9;
			int ih = (pos%9/3)-1;
			int iw = (pos%3)-1;
			if(mirrored)
				iw = -iw;
			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw : iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw : iw);

			int[] wheelAxis = {startX+(facing==5?-4:facing==4?4:0),startY,startZ+(facing==3?-4:facing==2?4:0)};
			TileEntity center = worldObj.getTileEntity(wheelAxis[0], wheelAxis[1], wheelAxis[2]);
			if(center instanceof TileEntityBucketWheel)
			{
				((TileEntityBucketWheel)center).active = false;
				center.markDirty();
				worldObj.markBlockForUpdate(wheelAxis[0], wheelAxis[1], wheelAxis[2]);
			}

			for(int l=0;l<6;l++)
				for(int w=-1;w<=1;w++)
					for(int h=-1;h<=1;h++)
					{
						int ww = mirrored?-w:w;
						int xx = (f==4?l: f==5?-l: f==2?-ww : ww);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-ww : ww);

						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityExcavator)
						{
							s = ((TileEntityExcavator)te).getOriginalBlock();
							((TileEntityExcavator)te).formed=false;
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
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && (pos==11||pos==14||pos==17) && from!=ForgeDirection.UP&&from!=ForgeDirection.DOWN;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && this.master()!=null && (pos==11||pos==14||pos==17) && from!=ForgeDirection.UP&&from!=ForgeDirection.DOWN)
		{
			TileEntityExcavator master = master();
			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
			master.markDirty();
			return rec;
		}
		return 0;
	}
	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getEnergyStored();
		return energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if(this.master()!=null)
			return this.master().energyStorage.getMaxEnergyStored();
		return energyStorage.getMaxEnergyStored();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==4)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==5?5:facing==4?0:1),yCoord-1,zCoord-(facing==3?5:facing==2?0:1), xCoord+(facing==4?6:facing==5?1:2),yCoord+2,zCoord+(facing==2?6:facing==3?1:2));
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
		int fl = facing;
		int fw = facing;
		if(mirrored)
			fw = ForgeDirection.OPPOSITES[fw];

		if(pos==42)
			return new float[]{fl==4||fl==5?-.5f:0,0,fl==2||fl==3?-.5f:0,  fl==4||fl==5?1.5f:1,.5f,fl==2||fl==3?1.5f:1};
		else if(pos==29||pos==38||pos==47)
			return new float[]{fw==2?.25f:0,0,fw==5?.25f:0, fw==3?.75f:1,1,fw==4?.75f:1};
		else if(pos==44)
			return new float[]{fw==3?.875f:0,0,fw==4?.875f:0, fw==2?.125f:1,1,fw==5?.125f:1};
		else if(pos==35)
			return new float[]{fl==5?.375f:fl==4?.5f:0,0,fl==3?.375f:fl==2?.5f:0,  fl==5?.5f:fl==4?.625f:1,1,fl==3?.5f:fl==2?.625f:1};
		else if(pos==53)
			return new float[]{fl==4?.375f: fl==5?.5f:0,0,fl==2?.375f:fl==3?.5f:0,  fl==4?.5f:fl==5?.625f:1,1,fl==2?.5f:fl==3?.625f:1};
		return new float[]{0,0,0,1,1,1};
	}

}