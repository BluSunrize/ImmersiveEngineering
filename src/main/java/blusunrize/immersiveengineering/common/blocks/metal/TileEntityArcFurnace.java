package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityArcFurnace extends TileEntityMultiblockMetal<TileEntityArcFurnace,ArcFurnaceRecipe> implements ISoundTile,IGuiTile, IAdvancedSelectionBounds,IAdvancedCollisionBounds
{
	public TileEntityArcFurnace()
	{
		super(MultiblockArcFurnace.instance, new int[]{5,5,5}, 64000, true);
	}
	public ItemStack[] inventory = new ItemStack[26];

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 26);
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update()
	{
		super.update();

		if(!worldObj.isRemote && !isDummy() && !isRSDisabled() && energyStorage.getEnergyStored()>0 && this.processQueue.size()<this.getProcessQueueMaxLength())
		{
			if(this.tickedProcesses>0)
				for(int i=23; i<26; i++)
					if(this.inventory[i].attemptDamageItem(1, worldObj.rand))
					{
						this.inventory[i] = null;
						//						updateClient = true;
						//						update = true;
					}

			Set<Integer> usedInvSlots = new HashSet<Integer>();
			//			final int[] usedInvSlots = new int[8];
			for(MultiblockProcess<ArcFurnaceRecipe> process : processQueue)
				if(process instanceof MultiblockProcessInMachine)
					for(int i : ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).inputSlots)
						usedInvSlots.add(i);

			//			Integer[] preferredSlots = new Integer[]{0,1,2,3,4,5,6,7};
			//			Arrays.sort(preferredSlots, 0,8, new Comparator<Integer>(){
			//				@Override
			//				public int compare(Integer arg0, Integer arg1)
			//				{
			//					return Integer.compare(usedInvSlots[arg0],usedInvSlots[arg1]);
			//				}});
			ItemStack[] additives = new ItemStack[4];
			for(int i=0; i<4; i++)
				additives[i] = (inventory[12+i]!=null?inventory[12+i].copy():null);
			for(int slot=0; slot<12; slot++)
				if(!usedInvSlots.contains(slot))
				{
					ItemStack stack = this.getInventory()[slot];
					//				if(stack!=null)
					//				{
					//					stack = stack.copy();
					////					stack.stackSize-=usedInvSlots[slot];
					//				}
					if(stack!=null && stack.stackSize>0)
					{
						ArcFurnaceRecipe recipe = ArcFurnaceRecipe.findRecipe(stack, additives);

						if(recipe!=null)
						{
							MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace(recipe, slot,12,13,14,15);
							if(this.addProcessToQueue(process, true))
							{
								this.addProcessToQueue(process, false);
								usedInvSlots.add(slot);
								//							update = true;
							}
						}
					}
				}


			if(worldObj.getTotalWorldTime()%8==0)
			{
				BlockPos outputPos = this.getBlockPosForPos(2).offset(facing,-1);
				TileEntity outputTile = this.worldObj.getTileEntity(outputPos);
				if(outputTile!=null)
					for(int j=16; j<22; j++)
						if(inventory[j]!=null)
						{
							ItemStack stack = Utils.copyStackWithAmount(inventory[j],1);
							stack = Utils.insertStackIntoInventory(outputTile, stack, facing.getOpposite());
							if(stack==null)
								this.inventory[j].stackSize--;
							if(this.inventory[j].stackSize<=0)
								this.inventory[j] = null;
						}

				outputPos = this.getBlockPosForPos(22).offset(facing);
				outputTile = this.worldObj.getTileEntity(outputPos);
				if(outputTile!=null)
					if(inventory[22]!=null)
					{
						ItemStack stack = Utils.copyStackWithAmount(inventory[22],1);
						stack = Utils.insertStackIntoInventory(outputTile, stack, facing);
						if(stack==null)
							this.inventory[22].stackSize--;
						if(this.inventory[22].stackSize<=0)
							this.inventory[22] = null;
					}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//			if(pos==17)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		//		return renderAABB;
		return AxisAlignedBB.fromBounds(getPos().getX()-(facing.getAxis()==Axis.Z?2:1),getPos().getY(),getPos().getZ()-(facing.getAxis()==Axis.X?2:1), getPos().getX()+(facing.getAxis()==Axis.Z?3:2),getPos().getY()+3,getPos().getZ()+(facing.getAxis()==Axis.X?3:2));
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
		if(pos==1||pos==3)
			return new float[]{facing==EnumFacing.EAST?.4375f:0,0,facing==EnumFacing.SOUTH?.4375f:0, facing==EnumFacing.WEST?.5625f:1,.5f,facing==EnumFacing.NORTH?.5625f:1};
		else if(pos<20 && pos!=2)
			return new float[]{0,0,0,1,.5f,1};
		else if(pos==25)
			return new float[]{facing==EnumFacing.WEST?.5f:0,0,facing==EnumFacing.NORTH?.5f:0, facing==EnumFacing.EAST?.5f:1,1,facing==EnumFacing.SOUTH?.5f:1};
		else if((pos>=36&&pos<=38) || (pos>=41&&pos<=43))
		{
			EnumFacing fw = facing.rotateY();
			if(mirrored|pos%5==3)
				fw = fw.getOpposite();
			if(pos%5==2)
				fw=null;
			float minX = fw==EnumFacing.EAST?.125f: 0;
			float maxX = fw==EnumFacing.WEST?.875f: 1;
			float minZ = fw==EnumFacing.SOUTH?.125f: 0;
			float maxZ = fw==EnumFacing.NORTH?.875f: 1;
			if(pos<=38)
			{
				minX -= facing==EnumFacing.EAST?.875f:0;
				maxX += facing==EnumFacing.WEST?.875f:0;
				minZ -= facing==EnumFacing.SOUTH?.875f:0;
				maxZ += facing==EnumFacing.NORTH?.875f:0;
			}
			return new float[]{minX,.5f,minZ, maxX,1,maxZ};
		}
		else if(pos==40||pos==44)
		{
			EnumFacing fl = pos==44?facing.getOpposite():facing;
			return new float[]{fl==EnumFacing.NORTH?.125f:fl==EnumFacing.SOUTH?.625f:0,.125f,fl==EnumFacing.EAST?.125f:fl==EnumFacing.WEST?.625f:0, fl==EnumFacing.SOUTH?.875f:fl==EnumFacing.NORTH?.375f:1,.375f,fl==EnumFacing.WEST?.875f:fl==EnumFacing.EAST?.375f:1};
		}
		else if(pos>=46&&pos<=48)
			return new float[]{facing==EnumFacing.WEST?.25f:0,0,facing==EnumFacing.NORTH?.25f:0, facing==EnumFacing.EAST?.75f:1,1,facing==EnumFacing.SOUTH?.75f:1};
		else if(pos==97)
			return new float[]{facing.getAxis()==Axis.X?.375f:0,0,facing.getAxis()==Axis.Z?.375f:0,facing.getAxis()==Axis.X?.625f:1,1,facing.getAxis()==Axis.Z?.625f:1};
		else if(pos==122)
			return new float[]{facing==EnumFacing.WEST?.3125f:0,0,facing==EnumFacing.NORTH?.3125f:0,facing==EnumFacing.EAST?.6875f:1,.9375f,facing==EnumFacing.SOUTH?.6875f:1};
		else if(pos==117)
			return new float[]{0,.625f,0,1,.9375f,1};
		else if(pos==112)
			return new float[]{facing==EnumFacing.EAST?.125f:0,0,facing==EnumFacing.SOUTH?.125f:0,facing==EnumFacing.WEST?.875f:1,.9375f,facing==EnumFacing.NORTH?.875f:1};
		else if(pos==51||pos==53 || pos==96||pos==98||pos==121||pos==123)
		{
			EnumFacing fw = facing.rotateY();
			if(mirrored|pos%5==3)
				fw = fw.getOpposite();
			return new float[]{fw==EnumFacing.EAST?.5f:0,0,fw==EnumFacing.SOUTH?.5f:0, fw==EnumFacing.WEST?.5f:1,1,fw==EnumFacing.NORTH?.5f:1};
		}
		return new float[]{0,0,0,1,1,1};
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
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(pos%15==7)
			return null;
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(pos==0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(AxisAlignedBB.fromBounds(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			float minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .125f;
			float maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .25f;
			float minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .125f;
			float maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .25f;
			list.add(AxisAlignedBB.fromBounds(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .75f;
			maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .75f;
			maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .875f;
			list.add(AxisAlignedBB.fromBounds(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		else if(pos>=46&&pos<=48)
		{
			float minX = fl==EnumFacing.WEST?.25f:0;
			float maxX = fl==EnumFacing.EAST?.75f:1;
			float minZ = fl==EnumFacing.NORTH?.25f:0;
			float maxZ = fl==EnumFacing.SOUTH?.75f:1;
			List<AxisAlignedBB> list = Lists.newArrayList(AxisAlignedBB.fromBounds(minX,0,minZ,maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST?0: fl==EnumFacing.EAST?.75f: .25f;
			maxX = fl==EnumFacing.EAST?1: fl==EnumFacing.WEST?.25f: .75f;
			minZ = fl==EnumFacing.NORTH?0: fl==EnumFacing.SOUTH?.75f: .25f;
			maxZ = fl==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.25f: .75f;
			list.add(AxisAlignedBB.fromBounds(minX,.25f,minZ, maxX,.75,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			return list;
		}
		else if(pos%25>=10 && (pos%5==0||pos%5==4))
		{
			List<AxisAlignedBB> list = pos<25?Lists.newArrayList(AxisAlignedBB.fromBounds(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ())):new ArrayList(2);
			if(pos%5==4)
				fw = fw.getOpposite();
			float minX = fw==EnumFacing.EAST?.5f: 0;
			float maxX = fw==EnumFacing.WEST?.5f: 1;
			float minZ = fw==EnumFacing.SOUTH?.5f: 0;
			float maxZ = fw==EnumFacing.NORTH?.5f: 1;
			if(pos%25/5!=3)
				list.add(AxisAlignedBB.fromBounds(minX,pos<25?.5:0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			if(pos<25)
			{
				minX = fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.375f:-1.625f;
				maxX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: fl==EnumFacing.WEST?.625f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.375f:-1.625f;
				maxZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: fl==EnumFacing.NORTH?.625f: 2.625f;
				AxisAlignedBB aabb = AxisAlignedBB.fromBounds(minX,.6875,minZ, maxX,.9375,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
				aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
				list.add(aabb);

				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.375f: .375f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?.625f: .625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.375f: .375f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?.625f: .625f;
				aabb = AxisAlignedBB.fromBounds(minX,.6875,minZ, maxX,.9375,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
				aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
				list.add(aabb);

				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?2.375f:-1.625f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?-1.375f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?2.375f:-1.625f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?-1.375f: 2.625f;
				aabb = AxisAlignedBB.fromBounds(minX,.6875,minZ, maxX,.9375,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
				aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
				list.add(aabb);
			}
			else if(pos<50)
			{
				minX = fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.375f:-1.625f;
				maxX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: fl==EnumFacing.WEST?.625f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.375f:-1.625f;
				maxZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: fl==EnumFacing.NORTH?.625f: 2.625f;
				AxisAlignedBB aabb = AxisAlignedBB.fromBounds(minX,.125,minZ, maxX,.375,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
				aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
				list.add(aabb);

				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.375f: .375f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?.625f: .625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.375f: .375f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?.625f: .625f;
				aabb = AxisAlignedBB.fromBounds(minX,.125,minZ, maxX,.375,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
				aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
				if(pos%5==0)
					aabb = aabb.offset(0,.6875,0);
				list.add(aabb);
				if(pos%5==0)
				{
					minX = fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.375f: .375f;
					maxX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: fl==EnumFacing.WEST?.625f: .625f;
					minZ = fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.375f: .375f;
					maxZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: fl==EnumFacing.NORTH?.625f: .625f;
					aabb = AxisAlignedBB.fromBounds(minX,.375,minZ, maxX,1.0625,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
					aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
					list.add(aabb);
				}
				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?2.375f:-1.625f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?-1.375f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?2.375f:-1.625f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?-1.375f: 2.625f;
				aabb = AxisAlignedBB.fromBounds(minX,.125,minZ, maxX,.375,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ());
				aabb = aabb.offset(-fl.getFrontOffsetX()*(pos%25-10)/5,0,-fl.getFrontOffsetZ()*(pos%25-10)/5);
				list.add(aabb);
			}
			else if(pos==60||pos==64)
			{
				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: .25f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: .75f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: .25f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: .75f;
				list.add(AxisAlignedBB.fromBounds(minX,.25,minZ, maxX,.75,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			}
			return list;
		}
		return null;
	}
	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, MovingObjectPosition mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}
	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{46,47,48};
	}
	@Override
	public int[] getRedstonePos()
	{
		return new int[]{25};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}
	@Override
	public boolean shouldRenderAsActive()
	{
		return hasElectrodes()&&super.shouldRenderAsActive();
	}
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if (!hasElectrodes())
			return false;
		if(process.recipe!=null && process.recipe.slag!=null)
		{
			if(this.inventory[22]==null)
				return true;
			if(!ItemHandlerHelper.canItemStacksStack(this.inventory[22], process.recipe.slag) || inventory[22].stackSize+process.recipe.slag.stackSize>getSlotLimit(22))
				return false;
		}
		return true;
	}
	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().add(0,-1,0).offset(facing,-2);
		TileEntity inventoryTile = this.worldObj.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output!=null)
			Utils.dropStackAtPos(worldObj, pos, output, facing);
	}
	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}
	@Override
	public void onProcessFinish(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if(process.recipe.slag!=null)
		{
			if(this.inventory[22]==null)
				this.inventory[22] = process.recipe.slag.copy();
			else if(ItemHandlerHelper.canItemStacksStack(this.inventory[22], process.recipe.slag) || inventory[22].stackSize+process.recipe.slag.stackSize>getSlotLimit(22))
				this.inventory[22].stackSize += process.recipe.slag.stackSize;
		}
	}
	@Override
	public int getMaxProcessPerTick()
	{
		return 12;
	}
	@Override
	public int getProcessQueueMaxLength()
	{
		return 12;
	}
	@Override
	public float getMinProcessDistance(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		return 0;
	}


	@Override
	public ItemStack[] getInventory()
	{
		return this.inventory;
	}
	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}
	@Override
	public int getSlotLimit(int slot)
	{
		return slot>22?1:64;
	}
	static int[] outputSlots = {16,17,18,19,20,21};
	@Override
	public int[] getOutputSlots()
	{
		return outputSlots;
	}
	@Override
	public int[] getOutputTanks()
	{
		return null;
	}
	@Override
	public FluidTank[] getInternalTanks()
	{
		return null;
	}
	@Override
	public void doGraphicalUpdates(int slot)
	{
	}


	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if((pos==2||pos==22||pos==86||pos==88||facing==null) && capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler inputHandler = new IEInventoryHandler(12, this, 0, true,false);
	IItemHandler additiveHandler = new IEInventoryHandler(4, this, 12, true,false);
	IItemHandler outputHandler = new IEInventoryHandler(6, this, 16, false,true);
	IItemHandler slagHandler = new IEInventoryHandler(1, this, 22, false,true);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityArcFurnace master = master();
			if(master==null)
				return null;
			if(pos==2)
				return (T)master.outputHandler;
			else if(pos==22)
				return (T)master.slagHandler;
			else if(pos==(mirrored?88:86))
				return (T)master.inputHandler;
			else if(pos==(mirrored?86:88))
				return (T)master.additiveHandler;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public ArcFurnaceRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}
	@Override
	protected ArcFurnaceRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return ArcFurnaceRecipe.loadFromNBT(tag);
	}
	@Override
	protected MultiblockProcess loadProcessFromNBT(NBTTagCompound tag)
	{
		IMultiblockRecipe recipe = readRecipeFromNBT(tag);
		if(recipe!=null && recipe instanceof ArcFurnaceRecipe)
			return new MultiblockProcessArcFurnace((ArcFurnaceRecipe)recipe, tag.getIntArray("process_inputSlots"));
		return null;
	}

	@Override
	public boolean canOpenGui()
	{
		return formed && (pos==2||pos==25|| (pos>25 && pos%5>0 && pos%5<4 && pos%25/5<4));
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_ArcFurnace;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return false;
	}

	public static class MultiblockProcessArcFurnace extends MultiblockProcessInMachine<ArcFurnaceRecipe>
	{
		public MultiblockProcessArcFurnace(ArcFurnaceRecipe recipe, int... inputSlots)
		{
			super(recipe, inputSlots);
		}

		@Override
		protected List<ItemStack> getRecipeItemOutputs(TileEntityMultiblockMetal multiblock)
		{
			ItemStack input = multiblock.getInventory()[this.inputSlots[0]];
			ItemStack[] additives = new ItemStack[4];
			for(int i=0; i<4; i++)
				additives[i] = (multiblock.getInventory()[12+i]!=null?multiblock.getInventory()[12+i].copy():null);
			return recipe.getOutputs(input, additives);
		}

		@Override
		protected void writeExtraDataToNBT(NBTTagCompound nbt)
		{
			super.writeExtraDataToNBT(nbt);
		}
	}

	public boolean hasElectrodes()
	{
		for (int i = 23;i<26;i++)
			if (inventory[i]==null)
				return false;
		return true;
	}
}