/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockMetalPress;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityMetalPress extends TileEntityMultiblockMetal<TileEntityMetalPress, MetalPressRecipe> implements IPlayerInteraction, IConveyorAttachable
{
	public TileEntityMetalPress()
	{
		super(MultiblockMetalPress.instance, new int[]{3, 3, 1}, 16000, true);
	}

	//	public ItemStack[] inventory = new ItemStack[3];
	//	public MetalPressRecipe[] curRecipes = new MetalPressRecipe[3];
	//	public int[] process = new int[3];
	public ItemStack mold = ItemStack.EMPTY;
	//	public boolean active;
	//	public static final int MAX_PROCESS = 120;
	//	public int stopped = -1;
	//	private int stoppedReqSize = -1;

	//	@Override
	//	public TileEntityMetalPress master()
	//	{
	//	}

	@Override
	public void update()
	{
		super.update();
		if(isDummy()||isRSDisabled()||world.isRemote)
			return;
		for(MultiblockProcess process : processQueue)
		{
			float tick = 1/(float)process.maxTicks;
			float transportTime = 52.5f/120f;
			float pressTime = 3.75f/120f;
			float fProcess = process.processTick*tick;
			if(fProcess >= transportTime&&fProcess < transportTime+tick)
				world.playSound(null, getPos(), IESounds.metalpress_piston, SoundCategory.BLOCKS, .3F, 1);
			if(fProcess >= (transportTime+pressTime)&&fProcess < (transportTime+pressTime+tick))
				world.playSound(null, getPos(), IESounds.metalpress_smash, SoundCategory.BLOCKS, .3F, 1);
			if(fProcess >= (1-transportTime)&&fProcess < (1-transportTime+tick))
				world.playSound(null, getPos(), IESounds.metalpress_piston, SoundCategory.BLOCKS, .3F, 1);
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		mold = new ItemStack(nbt.getCompoundTag("mold"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!this.mold.isEmpty())
			nbt.setTag("mold", this.mold.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		TileEntityMetalPress master = master();
		if(master!=null)
			if(player.isSneaking()&&!master.mold.isEmpty())
			{
				if(heldItem.isEmpty())
					player.setHeldItem(hand, master.mold.copy());
				else if(!world.isRemote)
					player.entityDropItem(master.mold.copy(), 0);
				master.mold = ItemStack.EMPTY;
				this.updateMasterBlock(null, true);
				return true;
			}
			else if(MetalPressRecipe.isValidMold(heldItem))
			{
				ItemStack tempMold = !master.mold.isEmpty()?master.mold.copy(): ItemStack.EMPTY;
				master.mold = Utils.copyStackWithAmount(heldItem, 1);
				heldItem.shrink(1);
				if(heldItem.getCount() <= 0)
					heldItem = ItemStack.EMPTY;
				else
					player.setHeldItem(hand, heldItem);
				if(!tempMold.isEmpty())
					if(heldItem.isEmpty())
						player.setHeldItem(hand, tempMold);
					else if(!world.isRemote)
						player.entityDropItem(tempMold, 0);
				this.updateMasterBlock(null, true);
				return true;
			}
		return false;
	}


	@Override
	public float[] getBlockBounds()
	{
		if(pos==3||pos==5)
			return new float[]{0, 0, 0, 1, .125f, 1};
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w)
	{
		super.replaceStructureBlock(pos, state, stack, h, l, w);
		if(h==1&&l!=1)
		{
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileEntityConveyorBelt)
				((TileEntityConveyorBelt)tile).setFacing(this.facing);
		}
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(pos==3&&!world.isRemote&&entity!=null&&!entity.isDead&&entity instanceof EntityItem&&!((EntityItem)entity).getItem().isEmpty())
		{
			TileEntityMetalPress master = master();
			if(master==null)
				return;
			ItemStack stack = ((EntityItem)entity).getItem();
			if(stack.isEmpty())
				return;
			IMultiblockRecipe recipe = master.findRecipeForInsertion(stack);
			if(recipe==null)
				return;
			ItemStack displayStack = recipe.getDisplayStack(stack);
			float transformationPoint = 56.25f/120f;
			MultiblockProcess process = new MultiblockProcessInWorld(recipe, transformationPoint, Utils.createNonNullItemStackListFromItemStack(displayStack));
			if(master.addProcessToQueue(process, true))
			{
				master.addProcessToQueue(process, false);
				stack.shrink(displayStack.getCount());
				if(stack.getCount() <= 0)
					entity.setDead();
			}
		}
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{7};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{1};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MetalPressRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().offset(facing, 2);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, pos, output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MetalPressRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 3;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 3;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MetalPressRecipe> process)
	{
		return 63.75f/120f;
	}


	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public NonNullList<ItemStack> getDroppedItems()
	{
		return ListUtils.fromItem(mold);
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return null;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityMetalPress master = master();
			if(master==null)
				return false;
			return pos==3&&facing==this.facing.getOpposite();
		}
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new MultiblockInventoryHandler_DirectProcessing(this);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityMetalPress master = master();
			if(master==null)
				return null;
			if(pos==3&&facing==this.facing.getOpposite())
				return (T)master.insertionHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public MetalPressRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return MetalPressRecipe.findRecipe(mold, inserting);
	}

	@Override
	protected MetalPressRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return MetalPressRecipe.loadFromNBT(tag);
	}

	@Override
	public EnumFacing[] sigOutputDirections()
	{
		if(pos==5)
			return new EnumFacing[]{this.facing};
		return new EnumFacing[0];
	}
	//	@Override
	//	public void updateEntity()
	//	{
	//		if(!formed || pos!=4)
	//			return;
	//		if (world.isRemote)
	//		{
	//			if (!active)
	//				return;
	//			for (int i = 0;i<process.length;i++)
	//			{
	//				if (process[i]>=0&&stopped!=i)
	//				{
	//					process[i]++;
	//					if (process[i]>MAX_PROCESS)
	//					{
	//						inventory[i] = null;
	//						process[i] = -1;
	//					}
	//				}
	//			}
	//			return;
	//		}
	//		boolean update = false;
	//		for(int i=0; i<inventory.length; i++)
	//			if(stopped!=i&&inventory[i]!=null)
	//			{
	//				if(process[i]>=MAX_PROCESS)
	//				{
	//					ItemStack output = inventory[i].copy();
	//					TileEntity inventoryTile = this.world.getTileEntity(xCoord+(facing==4?-2:facing==5?2:0),yCoord,zCoord+(facing==2?-2:facing==3?2:0));
	//					if(inventoryTile instanceof IInventory)
	//						output = Utils.insertStackIntoInventory((IInventory)inventoryTile, output, ForgeDirection.OPPOSITES[facing]);
	//					if(output!=null)
	//					{
	//						ForgeDirection fd = ForgeDirection.getOrientation(facing);
	//						EntityItem ei = new EntityItem(world, xCoord+.5+(facing==4?-2:facing==5?2:0),yCoord,zCoord+.5+(facing==2?-2:facing==3?2:0), output.copy());
	//						ei.motionX = (0.075F * fd.offsetX);
	//						ei.motionY = 0.025000000372529D;
	//						ei.motionZ = (0.075F * fd.offsetZ);
	//						this.world.spawnEntity(ei);
	//					}
	//					curRecipes[i] = null;
	//					process[i]=-1;
	//					inventory[i]=null;
	//					update = true;
	//				}
	//				if(curRecipes[i]==null)
	//					curRecipes[i] = MetalPressRecipe.findRecipe(mold, inventory[i], true);
	//				int perTick = curRecipes[i]!=null?curRecipes[i].energy/MAX_PROCESS:0;
	//				if((perTick==0 || this.energyStorage.extractEnergy(perTick, true)==perTick)&&process[i]>=0)
	//				{
	//					this.energyStorage.extractEnergy(perTick, false);
	//					if(process[i]++==60 && curRecipes[i]!=null)
	//					{
	//						this.inventory[i] = curRecipes[i].output.copy();
	//						update = true;
	//					}
	//					if (!active)
	//					{
	//						active = true;
	//						update = true;
	//					}
	//				}
	//				else if (active)
	//				{
	//					active = false;
	//					update = true;
	//				}
	//			}
	//			else if (stopped==i)
	//			{
	//				if (stoppedReqSize<0)
	//				{
	//					MetalPressRecipe recipe = MetalPressRecipe.findRecipe(mold, inventory[i], false);
	//					if (recipe!=null)
	//						stoppedReqSize = recipe.inputSize;
	//					else
	//					{
	//						stopped = -1;
	//						update = true;
	//						continue;
	//					}
	//				}
	//				if (stoppedReqSize<=inventory[i].stackSize)
	//					stopped = -1;
	//			}
	//		if(update)
	//		{
	//			this.markDirty();
	//			world.markBlockForUpdate(xCoord, yCoord, zCoord);
	//		}
	//	}
	//	public int getNextProcessID()
	//	{
	//		if(master()!=null)
	//			return master().getNextProcessID();
	//		int lowestProcess = Integer.MAX_VALUE;
	//		for(int i=0; i<inventory.length; i++)
	//			if(inventory[i]==null)
	//			{
	//				if (lowestProcess==Integer.MAX_VALUE)
	//				{
	//					lowestProcess = 200;
	//					for(int j=0; j<inventory.length; j++)
	//					{
	//						if(inventory[j]!=null && process[j]<lowestProcess && process[j]>=0)
	//							lowestProcess = process[j];
	//					}
	//				}
	//				if(lowestProcess>40)
	//					return i;
	//				else
	//					return -1;
	//			}
	//		return -1;
	//	}
	//	@Override
	//	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	//	{
	//		super.readCustomNBT(nbt, descPacket);
	//		int[] processTmp = nbt.getIntArray("process");
	//		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 3);
	//		for (int i = 0;i<processTmp.length;i++)
	//			if ((process[i]<0^processTmp[i]<0)||!descPacket)
	//				process[i] = processTmp[i];
	//		energyStorage.readFromNBT(nbt);
	//		mold = new ItemStack(nbt.getCompoundTag("mold"));
	//		if (descPacket)
	//			active = nbt.getBoolean("active");
	//		if (nbt.hasKey("stoppedSlot"))
	//			stopped = nbt.getInteger("stoppedSlot");
	//	}
	//	@Override
	//	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	//	{
	//		super.writeCustomNBT(nbt, descPacket);
	//		nbt.setIntArray("process", process);
	//		energyStorage.writeToNBT(nbt);
	//		nbt.setTag("inventory", Utils.writeInventory(inventory));
	//		if(this.mold!=null)
	//			nbt.setTag("mold", this.mold.writeToNBT(new NBTTagCompound()));
	//		if (descPacket)
	//			nbt.setBoolean("active", active);
	//		nbt.setInteger("stoppedSlot", stopped);
	//	}
	//	@Override
	//	public boolean receiveClientEvent(int id, int arg)
	//	{
	//		return false;
	//	}
	//	@SideOnly(Side.CLIENT)
	//	private AxisAlignedBB renderAABB;
	//	@Override
	//	@SideOnly(Side.CLIENT)
	//	public AxisAlignedBB getRenderBoundingBox()
	//	{
	//		if (!formed)
	//			return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	//		if(renderAABB==null)
	//			if(pos==4)
	//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-1,yCoord-1,zCoord-1, xCoord+2,yCoord+2,zCoord+2);
	//			else
	//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	//		return renderAABB;
	//	}
	//	@Override
	//	@SideOnly(Side.CLIENT)
	//	public double getMaxRenderDistanceSquared()
	//	{
	//		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	//	}
	//	@Override
	//	public float[] getBlockBounds()
	//	{
	//		if(pos<3)
	//			return new float[]{0,0,0,1,1,1};
	//		float xMin = 0;
	//		float yMin = 0;
	//		float zMin = 0;
	//		float xMax = 1;
	//		float yMax = 1;
	//		float zMax = 1;
	//		if(pos%3==0||pos%3==2)
	//			yMax = .125f;
	//		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
	//	}
	//	@Override
	//	public void disassemble()
	//	{
	//		if(!world.isRemote&&pos==4&&mold!=null)
	//		{
	//			EntityItem moldDrop = new EntityItem(world, getPos().getX()+.5, getPos().getY()+.5, getPos().getZ()+.5, mold);
	//			world.spawnEntity(moldDrop);
	//		}
	//		if(formed && !world.isRemote)
	//		{
	//			int f = facing;
	//			TileEntity master = master();
	//			if(master==null)
	//				master = this;
	//			int startX = master.xCoord;
	//			int startY = master.yCoord;
	//			int startZ = master.zCoord;
	//			for(int yy=-1;yy<=1;yy++)
	//				for(int l=-1; l<=1; l++)
	//				{
	//					int xx = f>3?l:0;
	//					int zz = f<4?l:0;
	//					ItemStack s = null;
	//					TileEntity te = world.getTileEntity(startX+xx,startY+yy,startZ+zz);
	//					if(te instanceof TileEntityMetalPress)
	//					{
	//						s = ((TileEntityMetalPress)te).getOriginalBlock();
	//						((TileEntityMetalPress)te).formed=false;
	//					}
	//					if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
	//						s = this.getOriginalBlock();
	//					if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
	//					{
	//						if(startX+xx==xCoord && startY+yy==yCoord && startZ+zz==zCoord)
	//							world.spawnEntity(new EntityItem(world, xCoord+.5,yCoord+.5,zCoord+.5, s));
	//						else
	//						{
	//							if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblocks)
	//								world.setBlockToAir(startX+xx,startY+yy,startZ+zz);
	//							int meta = s.getItemDamage();
	//							world.setBlock(startX+xx,startY+yy,startZ+zz, Block.getBlockFromItem(s.getItem()), meta, 0x3);
	//						}
	//						TileEntity tile = world.getTileEntity(startX+xx,startY+yy,startZ+zz);
	//						if(tile instanceof TileEntityConveyorBelt)
	//							((TileEntityConveyorBelt)tile).facing = ForgeDirection.OPPOSITES[f];
	//					}
	//				}
	//		}
	//	}
	//	
	//	@Override
	//	public boolean canConnectEnergy(@Nullable EnumFacing from)
	//	{
	//		return formed && pos==7 && from==ForgeDirection.UP;
	//	}
	//	@Override
	//	public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate)
	//	{
	//		TileEntityMetalPress master = master();
	//		if(formed && pos==7 && from==ForgeDirection.UP && master!=null)
	//		{
	//			int rec = master.energyStorage.receiveEnergy(maxReceive, simulate);
	//			master.markDirty();
	//			if(rec>0)
	//				world.markBlockForUpdate(master.xCoord, master.yCoord, master.zCoord);
	//			return rec;
	//		}
	//		return 0;
	//	}
	//	@Override
	//	public int getEnergyStored(@Nullable EnumFacing from)
	//	{
	//		TileEntityMetalPress master = master();
	//		if(master!=null)
	//			return master.energyStorage.getEnergyStored();
	//		return energyStorage.getEnergyStored();
	//	}
	//	@Override
	//	public int getMaxEnergyStored(@Nullable EnumFacing from)
	//	{
	//		TileEntityMetalPress master = master();
	//		if(master!=null)
	//			return master.energyStorage.getMaxEnergyStored();
	//		return energyStorage.getMaxEnergyStored();
	//	}
}