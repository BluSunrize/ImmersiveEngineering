package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import mods.railcraft.api.crafting.IRockCrusherRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESound;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityCrusher extends TileEntityMultiblockPart implements IEnergyReceiver, ISidedInventory, ISoundTile
{
	public int facing = 2;
	public EnergyStorage energyStorage = new EnergyStorage(32000);
	public List<ItemStack> inputs = new ArrayList();
	public int process = 0;

	public float barrelRotation=0;
	public boolean active = false;
	public boolean hasPower = false;
	public boolean mobGrinding = false;
	public int grindingTimer = 0;
	@SideOnly(Side.CLIENT)
	ItemStack particleStack;

	@Override
	public TileEntityCrusher master()
	{
		if(offset[0]==0&&offset[1]==0&&offset[2]==0)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityCrusher?(TileEntityCrusher)te : null;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = MultiblockCrusher.instance.getStructureManual()[pos%15/5][pos%5][pos/15];
		return s!=null?s.copy():null;
	}

	static IESound sound;
	@Override
	public void updateEntity()
	{
		if(!formed || pos!=17)
			return;

		if(hasPower&&(active&&process>0)||mobGrinding||grindingTimer>0)
		{
			if(grindingTimer>0)
				grindingTimer--;
			barrelRotation += 18f;
			barrelRotation %= 360f;
		}

		if(worldObj.isRemote)
		{
			ImmersiveEngineering.proxy.handleTileSound("crusher", this, hasPower&&((active&&process>0)||mobGrinding||grindingTimer>0), 1,1);
			if(particleStack!=null && active&&hasPower&&process>0)
				ImmersiveEngineering.proxy.spawnCrusherFX(this, particleStack);
			else if(particleStack!=null)
				particleStack=null;
		}
		else
		{
			boolean update = false;
			if(hasPower!=(energyStorage.getEnergyStored()>0))
			{
				hasPower = !hasPower;
				update = true;
			}
			if(!worldObj.isBlockIndirectlyGettingPowered(xCoord+(facing==4?-1:facing==5?1:facing==(mirrored?2:3)?2:-2),yCoord+1,zCoord+(facing==2?-1:facing==3?1:facing==(mirrored?5:4)?2:-2)))
			{
				int power = Config.getInt("crusher_consumption");
				AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xCoord-.5625,yCoord+1.5,zCoord-.5625, xCoord+1.5625,yCoord+2.875,zCoord+1.5625);
				List<EntityItem> itemList = worldObj.getEntitiesWithinAABB(EntityItem.class, aabb);
				if(!itemList.isEmpty() && hasPower)
					for(EntityItem e : itemList)
					{
						ItemStack input = ((EntityItem)e).getEntityItem();
						if(!isValidInput(input))
						{
							e.setDead();
							grindingTimer = 10;
							update = true;
							continue;
						}
						addStackToInputs(input);
						update = true;
						e.setDead();
					}
				List<EntityLivingBase> livingList = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
				if(!livingList.isEmpty())
				{
					for(EntityLivingBase e : livingList)
						if(!e.isDead && e.getHealth()>0)
						{
							int consumed = this.energyStorage.extractEnergy(power, false);
							if(consumed>0)
							{
								e.attackEntityFrom(IEDamageSources.causeCrusherDamage(), consumed/20f);
								EventHandler.crusherMap.put(e.getUniqueID(), this);
								mobGrinding = true;
							}
							update = true;
						}
				}
				else if(process<=0&&mobGrinding)
				{
					mobGrinding = false;
					update = true;
				}


				if(process>0)
				{
					int consumed = this.energyStorage.extractEnergy(power, false);
					if(consumed>0){
						process -= consumed;
						if(!active)
						{
							active = true;
							update = true;
						}
					} else if(active) {
						active = false;
						update = true;
					}
				}

				if(process<=0 && !inputs.isEmpty())
					if(active)
					{
						ItemStack inputStack = inputs.get(0);

						CrusherRecipe recipe = CrusherRecipe.findRecipe(inputStack);
						if(recipe!=null)
						{
							ItemStack outputStack = recipe.output;
							if(outputStack!=null)
								outputItem(outputStack.copy());
							if(recipe.secondaryOutput!=null)
								for(int i=0; i<recipe.secondaryOutput.length; i++)
									if(worldObj.rand.nextFloat()<recipe.secondaryChance[i])
										outputItem(recipe.secondaryOutput[i]);

							inputStack.stackSize-= (recipe.input instanceof ItemStack)? ((ItemStack)recipe.input).stackSize: 1;
							if(inputStack.stackSize>0)
								inputs.set(0, inputStack);
							else
								inputs.remove(0);
							active = false;
							update = true;
						}
						else if(RailcraftCraftingManager.rockCrusher!=null && RailcraftCraftingManager.rockCrusher.getRecipe(inputStack)!=null)
						{
							IRockCrusherRecipe rcrecipe = RailcraftCraftingManager.rockCrusher.getRecipe(inputStack);
							List<ItemStack> outputs = rcrecipe.getRandomizedOuputs();
							for(ItemStack out : outputs)
								if(out!=null)
									outputItem(out.copy());
							inputStack.stackSize-= rcrecipe.getInput().stackSize;
							if(inputStack.stackSize>0)
								inputs.set(0, inputStack);
							else
								inputs.remove(0);
							active = false;
							update = true;
						}
						else
						{
							inputs.remove(0);
							active = false;
							update = true;
						}
					}
					else
					{
						ItemStack inputStack = inputs.get(0);
						CrusherRecipe recipe = CrusherRecipe.findRecipe(inputStack);
						if(recipe!=null)
							this.process = recipe.energy;
						else if(RailcraftCraftingManager.rockCrusher!=null && RailcraftCraftingManager.rockCrusher.getRecipe(inputStack)!=null)
							this.process = 2400;
						else
							inputs.remove(0);
						active = true;
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

	boolean isValidInput(ItemStack stack)
	{
		return CrusherRecipe.findRecipe(stack)!=null || (RailcraftCraftingManager.rockCrusher!=null && RailcraftCraftingManager.rockCrusher.getRecipe(stack)!=null);
	}
	public boolean addStackToInputs(ItemStack stack)
	{
		for(int i=0;i<inputs.size();i++)
			if(this.inputs.get(i)!=null && this.inputs.get(i).isItemEqual(stack) && (this.inputs.get(i).stackSize+stack.stackSize <= stack.getMaxStackSize()))
			{
				this.inputs.get(i).stackSize+=stack.stackSize;
				return true;
			}
		this.inputs.add(stack);
		return true;
	}
	public void outputItem(ItemStack stack)
	{
		TileEntity inventory = this.worldObj.getTileEntity(xCoord+(facing==4?-2:facing==5?2:0),yCoord,zCoord+(facing==2?-2:facing==3?2:0));
		if(isInventory(inventory, ForgeDirection.OPPOSITES[facing]))
		{
			stack = Utils.insertStackIntoInventory((IInventory)inventory, stack, ForgeDirection.OPPOSITES[facing]);
		}

		if(stack != null)
		{
			ForgeDirection fd = ForgeDirection.getOrientation(facing);
			EntityItem ei = new EntityItem(worldObj, xCoord+.5+fd.offsetX*2, yCoord+.5, zCoord+.5+fd.offsetZ*2, stack.copy());
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


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		hasPower = nbt.getBoolean("hasPower");
		if(hasPower)
			barrelRotation = nbt.getFloat("barrelRotation");
		active = nbt.getBoolean("active");
		mobGrinding = nbt.getBoolean("mobGrinding");
		grindingTimer = nbt.getInteger("grindingTimer");
		process = nbt.getInteger("process");
		energyStorage.readFromNBT(nbt);
		if(!descPacket)
		{
			NBTTagList invList = nbt.getTagList("inputs", 10);
			inputs.clear();
			for(int i=0;i<invList.tagCount();i++)
				inputs.add( ItemStack.loadItemStackFromNBT(invList.getCompoundTagAt(i)));
		} else {
			particleStack = nbt.hasKey("particleStack")? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("particleStack")): null;
		}
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setBoolean("hasPower", hasPower);
		nbt.setFloat("barrelRotation", barrelRotation);
		nbt.setBoolean("active", active);
		nbt.setBoolean("mobGrinding", mobGrinding);
		nbt.setInteger("grindingTimer", grindingTimer);
		nbt.setInteger("process", process);
		energyStorage.writeToNBT(nbt);
		if(!descPacket)
		{
			NBTTagList invList = new NBTTagList();
			for(ItemStack s : inputs)
				invList.appendTag(s.writeToNBT(new NBTTagCompound()));
			nbt.setTag("inputs", invList);
		} else {
			if(!inputs.isEmpty()){
				NBTTagCompound t = inputs.get(0).writeToNBT(new NBTTagCompound());
				nbt.setTag("particleStack", t);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(pos==17)
			return AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
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

		if(pos%15>=6&&pos%15<=8)
		{
			if(pos/15==0)
			{
				if(pos%5==1)
					return new float[]{fw==3||fl==4?.1875f:0, .5f, fl==2||fw==4?.1875f:0, fw==2||fl==5?.8125f:1, 1, fl==3||fw==5?.8125f:1};
				else if(pos%5==2)
					return new float[]{fl==4?.1875f:0, .5f, fl==2?.1875f:0, fl==5?.8125f:1, 1, fl==3?.8125f:1};
				else if(pos%5==3)
					return new float[]{fw==2||fl==4?.1875f:0, .5f, fl==2||fw==5?.1875f:0, fw==3||fl==5?.8125f:1, 1, fl==3||fw==4?.8125f:1};
			}
			else if(pos/15==1)
			{
				if(pos%5==1)
					return new float[]{fw==3?.1875f:0, .5f, fw==4?.1875f:0, fw==2?.8125f:1, 1, fw==5?.8125f:1};
				else if(pos%5==2)
					return new float[]{0,0,0, 1,1,1};
				else if(pos%5==3)
					return new float[]{fw==2?.1875f:0, .5f, fw==5?.1875f:0, fw==3?.8125f:1, 1, fw==4?.8125f:1};
			}
			else if(pos/15==2)
			{
				if(pos%5==1)
					return new float[]{fw==3||fl==5?.1875f:0, .5f, fl==3||fw==4?.1875f:0, fw==2||fl==4?.8125f:1, 1, fl==2||fw==5?.8125f:1};
				else if(pos%5==2)
					return new float[]{fl==5?.1875f:0, .5f, fl==3?.1875f:0, fl==4?.8125f:1, 1, fl==2?.8125f:1};
				else if(pos%5==3)
					return new float[]{fw==2||fl==5?.1875f:0, .5f, fl==3||fw==5?.1875f:0, fw==3||fl==4?.8125f:1, 1, fl==2||fw==4?.8125f:1};
			}
		}
		else if(pos%15>=11&&pos%15<=13) 
		{
			if(pos/15==0)
			{
				if(pos%5==2)
					return new float[]{fl==4?.1875f:fl==5?.5625f:0, 0, fl==2?.1875f:fl==3?.5625f:0, fl==5?.8125f:fl==4?.4375f:1, 1, fl==3?.8125f:fl==2?.4375f:1};
			}
			else if(pos/15==1)
			{
				if(pos%5==1)
					return new float[]{fw==3?.1875f:fw==2?.5625f:0, 0, fw==4?.1875f:fw==5?.5625f:0, fw==2?.8125f:fw==3?.4375f:1, 1, fw==5?.8125f:fw==4?.4375f:1};
				else if(pos%5==2)
					return new float[]{0,0,0, 0,0,0};
				else if(pos%5==3)
					return new float[]{fw==2?.1875f:fw==3?.5625f:0, 0, fw==5?.1875f:fw==4?.5625f:0, fw==3?.8125f:fw==2?.4375f:1, 1, fw==4?.8125f:fw==5?.4375f:1};
			}
			else if(pos/15==2)
			{
				if(pos%5==2)
					return new float[]{fl==5?.1875f:fl==4?.5625f:0, 0, fl==3?.1875f:fl==2?.5625f:0, fl==4?.8125f:fl==5?.4375f:1, 1, fl==2?.8125f:fl==3?.4375f:1};
			}
		}
		else if(pos==9)
			return new float[]{fl==5?.5f:0,0,fl==3?.5f:0,  fl==4?.5f:1,1,fl==2?.5f:1};
		else if(pos==1||pos==3 || pos==16||pos==18||pos==24 || (pos>=31&&pos<=34))
			return new float[]{0,0,0,1,.5f,1};
		return new float[]{0,0,0,1,1,1};
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(formed && !worldObj.isRemote)
		{
			int f = facing;
			int il = pos/15;
			int ih = (pos%15/5)-1;
			int iw = (pos%5)-2;
			if(mirrored)
				iw = -iw;
			int startX = xCoord-(f==4?il: f==5?-il: f==2?-iw : iw);
			int startY = yCoord-ih;
			int startZ = zCoord-(f==2?il: f==3?-il: f==5?-iw : iw);

			for(int l=0;l<3;l++)
				for(int w=-2;w<=2;w++)
					for(int h=-1;h<=1;h++)
					{
						int ww = mirrored?-w:w;
						int xx = (f==4?l: f==5?-l: f==2?-ww : ww);
						int yy = h;
						int zz = (f==2?l: f==3?-l: f==5?-ww : ww);

						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startX+xx,startY+yy,startZ+zz);
						if(te instanceof TileEntityCrusher)
						{
							s = ((TileEntityCrusher)te).getOriginalBlock();
							((TileEntityCrusher)te).formed=false;
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
	public int getSizeInventory()
	{
		if(!formed || pos!=27)
			return 0;
		return 1;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		return null;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		if(!formed || pos!=27)
			return;
		if(master()!=null)
			master().addStackToInputs(stack);
	}
	@Override
	public String getInventoryName()
	{
		return "IECrusher";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
	}
	@Override
	public void openInventory()
	{
	}
	@Override
	public void closeInventory()
	{
	}
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		if(!formed || pos!=27)
			return false;
		return CrusherRecipe.findRecipe(stack)!=null;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return formed&&pos==27&&side==ForgeDirection.UP.ordinal()?new int[]{0}: new int[0];
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return formed&&pos==27&&side==ForgeDirection.UP.ordinal();
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return false;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==20 && from==ForgeDirection.UP;
	}
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(formed && this.master()!=null && pos==20 && from==ForgeDirection.UP)
		{
			TileEntityCrusher master = master();
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
}