/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;

public class TileEntityArcFurnace extends TileEntityMultiblockMetal<TileEntityArcFurnace, ArcFurnaceRecipe> implements ISoundTile, IGuiTile, IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	public NonNullList<ItemStack> inventory = NonNullList.withSize(26, ItemStack.EMPTY);
	public int pouringMetal = 0;

	public TileEntityArcFurnace()
	{
		super(MultiblockArcFurnace.instance, new int[]{5, 5, 5}, 64000, true);
	}

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
		if(isDummy())
			return;
		if(world.isRemote)
		{
			if(pouringMetal > 0)
				pouringMetal--;
			if(shouldRenderAsActive())
				for(int i = 0; i < 4; i++)
				{
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5-.25*facing.getXOffset(),
								getPos().getY()+2.9, getPos().getZ()+.5-.25*facing.getZOffset(),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5+(facing==EnumFacing.EAST?-.25: .25),
								getPos().getY()+2.9, getPos().getZ()+.5+(facing==EnumFacing.SOUTH?.25: -.25),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5+(facing==EnumFacing.WEST?.25: -.25),
								getPos().getY()+2.9, getPos().getZ()+.5+(facing==EnumFacing.NORTH?-.25: .25),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
				}
		}
		else if(!isRSDisabled()&&energyStorage.getEnergyStored() > 0)
		{
			if(this.tickedProcesses > 0)
				for(int i = 23; i < 26; i++)
					if(this.inventory.get(i).attemptDamageItem(1, Utils.RAND, null))
					{
						this.inventory.set(i, ItemStack.EMPTY);
						//						updateClient = true;
						//						update = true;
					}

			if(this.processQueue.size() < this.getProcessQueueMaxLength())
			{
				Map<Integer, Integer> usedInvSlots = new HashMap<Integer, Integer>();
				for(MultiblockProcess<ArcFurnaceRecipe> process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
					{
						int[] inputSlots = ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).getInputSlots();
						int[] inputAmounts = ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).getInputAmounts();
						if(inputAmounts!=null)
							for(int i = 0; i < inputSlots.length; i++)
								if(usedInvSlots.containsKey(inputSlots[i]))
									usedInvSlots.put(inputSlots[i], usedInvSlots.get(inputSlots[i])+inputAmounts[i]);
								else
									usedInvSlots.put(inputSlots[i], inputAmounts[i]);
					}

				NonNullList<ItemStack> additives = NonNullList.withSize(4, ItemStack.EMPTY);
				for(int i = 0; i < 4; i++)
					if(!inventory.get(12+i).isEmpty())
					{
						additives.set(i, inventory.get(12+i).copy());
						if(usedInvSlots.containsKey(12+i))
							additives.get(i).shrink(usedInvSlots.get(12+i));
					}

				for(int slot = 0; slot < 12; slot++)
					if(!usedInvSlots.containsKey(slot))
					{
						ItemStack stack = this.getInventory().get(slot);
						if(!stack.isEmpty()&&stack.getCount() > 0)
						{
							ArcFurnaceRecipe recipe = ArcFurnaceRecipe.findRecipe(stack, additives);
							if(recipe!=null)
							{
								MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace(recipe, slot, 12, 13, 14, 15);
								if(this.addProcessToQueue(process, true))
								{
									this.addProcessToQueue(process, false);
									int[] consumedAdditives = recipe.getConsumedAdditives(additives, true);
									if(consumedAdditives!=null)
										process.setInputAmounts(1, consumedAdditives[0], consumedAdditives[1], consumedAdditives[2], consumedAdditives[3]);
									//							update = true;
								}
							}
						}
					}
			}

			if(world.getTotalWorldTime()%8==0)
			{
				BlockPos outputPos = this.getBlockPosForPos(2).offset(facing, -1);
				TileEntity outputTile = Utils.getExistingTileEntity(world, outputPos);
				if(outputTile!=null)
					for(int j = 16; j < 22; j++)
						if(!inventory.get(j).isEmpty())
						{
							ItemStack stack = Utils.copyStackWithAmount(inventory.get(j), 1);
							stack = Utils.insertStackIntoInventory(outputTile, stack, facing.getOpposite());
							if(stack.isEmpty())
							{
								this.inventory.get(j).shrink(1);
								if(this.inventory.get(j).getCount() <= 0)
									this.inventory.set(j, ItemStack.EMPTY);
							}
						}
				outputPos = this.getBlockPosForPos(22).offset(facing);
				outputTile = Utils.getExistingTileEntity(world, outputPos);
				if(outputTile!=null)
					if(!inventory.get(22).isEmpty())
					{
						int out = Math.min(inventory.get(22).getCount(), 16);
						ItemStack stack = Utils.copyStackWithAmount(inventory.get(22), out);
						stack = Utils.insertStackIntoInventory(outputTile, stack, facing);
						if(!stack.isEmpty())
							out -= stack.getCount();
						this.inventory.get(22).shrink(out);
						if(this.inventory.get(22).getCount() <= 0)
							this.inventory.set(22, ItemStack.EMPTY);
					}
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type)
	{
		if(id==0)
			pouringMetal = type;
		return super.receiveClientEvent(id, type);
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
		return new AxisAlignedBB(getPos().getX()-(facing.getAxis()==Axis.Z?2: 1), getPos().getY(), getPos().getZ()-(facing.getAxis()==Axis.X?2: 1), getPos().getX()+(facing.getAxis()==Axis.Z?3: 2), getPos().getY()+3, getPos().getZ()+(facing.getAxis()==Axis.X?3: 2));
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos==1||pos==3)
			return new float[]{facing==EnumFacing.EAST?.4375f: 0, 0, facing==EnumFacing.SOUTH?.4375f: 0, facing==EnumFacing.WEST?.5625f: 1, .5f, facing==EnumFacing.NORTH?.5625f: 1};
		else if(pos < 20&&pos!=2)
			return new float[]{0, 0, 0, 1, .5f, 1};
		else if(pos==25)
			return new float[]{facing==EnumFacing.WEST?.5f: 0, 0, facing==EnumFacing.NORTH?.5f: 0, facing==EnumFacing.EAST?.5f: 1, 1, facing==EnumFacing.SOUTH?.5f: 1};
		else if((pos >= 36&&pos <= 38)||(pos >= 41&&pos <= 43))
		{
			EnumFacing fw = facing.rotateY();
			if(mirrored|pos%5==3)
				fw = fw.getOpposite();
			if(pos%5==2)
				fw = null;
			float minX = fw==EnumFacing.EAST?.125f: 0;
			float maxX = fw==EnumFacing.WEST?.875f: 1;
			float minZ = fw==EnumFacing.SOUTH?.125f: 0;
			float maxZ = fw==EnumFacing.NORTH?.875f: 1;
			if(pos <= 38)
			{
				minX -= facing==EnumFacing.EAST?.875f: 0;
				maxX += facing==EnumFacing.WEST?.875f: 0;
				minZ -= facing==EnumFacing.SOUTH?.875f: 0;
				maxZ += facing==EnumFacing.NORTH?.875f: 0;
			}
			return new float[]{minX, .5f, minZ, maxX, 1, maxZ};
		}
		else if(pos==40||pos==44)
		{
			EnumFacing fl = pos==44?facing.getOpposite(): facing;
			return new float[]{fl==EnumFacing.NORTH?.125f: fl==EnumFacing.SOUTH?.625f: 0, .125f, fl==EnumFacing.EAST?.125f: fl==EnumFacing.WEST?.625f: 0, fl==EnumFacing.SOUTH?.875f: fl==EnumFacing.NORTH?.375f: 1, .375f, fl==EnumFacing.WEST?.875f: fl==EnumFacing.EAST?.375f: 1};
		}
		else if(pos >= 46&&pos <= 48)
			return new float[]{facing==EnumFacing.WEST?.25f: 0, 0, facing==EnumFacing.NORTH?.25f: 0, facing==EnumFacing.EAST?.75f: 1, 1, facing==EnumFacing.SOUTH?.75f: 1};
		else if(pos==97)
			return new float[]{facing.getAxis()==Axis.X?.375f: 0, 0, facing.getAxis()==Axis.Z?.375f: 0, facing.getAxis()==Axis.X?.625f: 1, 1, facing.getAxis()==Axis.Z?.625f: 1};
		else if(pos==122)
			return new float[]{facing==EnumFacing.WEST?.3125f: 0, 0, facing==EnumFacing.NORTH?.3125f: 0, facing==EnumFacing.EAST?.6875f: 1, .9375f, facing==EnumFacing.SOUTH?.6875f: 1};
		else if(pos==117)
			return new float[]{0, .625f, 0, 1, .9375f, 1};
		else if(pos==112)
			return new float[]{facing==EnumFacing.EAST?.125f: 0, 0, facing==EnumFacing.SOUTH?.125f: 0, facing==EnumFacing.WEST?.875f: 1, .9375f, facing==EnumFacing.NORTH?.875f: 1};
		else if(pos==51||pos==53||pos==96||pos==98||pos==121||pos==123)
		{
			EnumFacing fw = facing.rotateY();
			if(mirrored^pos%5==3)
				fw = fw.getOpposite();
			return new float[]{fw==EnumFacing.EAST?.5f: 0, 0, fw==EnumFacing.SOUTH?.5f: 0, fw==EnumFacing.WEST?.5f: 1, 1, fw==EnumFacing.NORTH?.5f: 1};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
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
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .125f;
			float maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .25f;
			float minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .125f;
			float maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .75f;
			maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .75f;
			maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(pos >= 46&&pos <= 48)
		{
			float minX = fl==EnumFacing.WEST?.25f: 0;
			float maxX = fl==EnumFacing.EAST?.75f: 1;
			float minZ = fl==EnumFacing.NORTH?.25f: 0;
			float maxZ = fl==EnumFacing.SOUTH?.75f: 1;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?0: fl==EnumFacing.EAST?.75f: .25f;
			maxX = fl==EnumFacing.EAST?1: fl==EnumFacing.WEST?.25f: .75f;
			minZ = fl==EnumFacing.NORTH?0: fl==EnumFacing.SOUTH?.75f: .25f;
			maxZ = fl==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.25f: .75f;
			list.add(new AxisAlignedBB(minX, .25f, minZ, maxX, .75, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(pos%25 >= 10&&(pos%5==0||pos%5==4))
		{
			List<AxisAlignedBB> list = pos < 25?Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ())): new ArrayList(2);
			if(pos%5==4)
				fw = fw.getOpposite();
			float minX = fw==EnumFacing.EAST?.5f: 0;
			float maxX = fw==EnumFacing.WEST?.5f: 1;
			float minZ = fw==EnumFacing.SOUTH?.5f: 0;
			float maxZ = fw==EnumFacing.NORTH?.5f: 1;
			if(pos%25/5!=3)
				list.add(new AxisAlignedBB(minX, pos < 25?.5: 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos < 25)
			{
				minX = fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.375f: -1.625f;
				maxX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: fl==EnumFacing.WEST?.625f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.375f: -1.625f;
				maxZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: fl==EnumFacing.NORTH?.625f: 2.625f;
				AxisAlignedBB aabb = new AxisAlignedBB(minX, .6875, minZ, maxX, .9375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
				list.add(aabb);

				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.375f: .375f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?.625f: .625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.375f: .375f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?.625f: .625f;
				aabb = new AxisAlignedBB(minX, .6875, minZ, maxX, .9375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
				list.add(aabb);

				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?2.375f: -1.625f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?-1.375f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?2.375f: -1.625f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?-1.375f: 2.625f;
				aabb = new AxisAlignedBB(minX, .6875, minZ, maxX, .9375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
				list.add(aabb);
			}
			else if(pos < 50)
			{
				minX = fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.375f: -1.625f;
				maxX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: fl==EnumFacing.WEST?.625f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.375f: -1.625f;
				maxZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: fl==EnumFacing.NORTH?.625f: 2.625f;
				AxisAlignedBB aabb = new AxisAlignedBB(minX, .125, minZ, maxX, .375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
				list.add(aabb);

				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.375f: .375f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?.625f: .625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.375f: .375f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?.625f: .625f;
				aabb = new AxisAlignedBB(minX, .125, minZ, maxX, .375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
				if(pos%5==0)
					aabb = aabb.offset(0, .6875, 0);
				list.add(aabb);
				if(pos%5==0)
				{
					minX = fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.375f: .375f;
					maxX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: fl==EnumFacing.WEST?.625f: .625f;
					minZ = fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.375f: .375f;
					maxZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: fl==EnumFacing.NORTH?.625f: .625f;
					aabb = new AxisAlignedBB(minX, .375, minZ, maxX, 1.0625, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
					aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
					list.add(aabb);
				}
				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?2.375f: -1.625f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: fl==EnumFacing.WEST?-1.375f: 2.625f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?2.375f: -1.625f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: fl==EnumFacing.NORTH?-1.375f: 2.625f;
				aabb = new AxisAlignedBB(minX, .125, minZ, maxX, .375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(pos%25-10)/5, 0, -fl.getZOffset()*(pos%25-10)/5);
				list.add(aabb);
			}
			else if(pos==60||pos==64)
			{
				minX = fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.5f: .25f;
				maxX = fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?.625f: .75f;
				minZ = fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.5f: .25f;
				maxZ = fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?.625f: .75f;
				list.add(new AxisAlignedBB(minX, .25, minZ, maxX, .75, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
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
		return new int[]{46, 47, 48};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{25};
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(this.pos==112)
		{
			TileEntityArcFurnace master = master();
			if(master!=null)
			{
				float f = 0;
				for(int i = 23; i < 26; i++)
					if(!master.inventory.get(i).isEmpty())
						f += 1-(master.inventory.get(i).getItemDamage()/(float)master.inventory.get(i).getMaxDamage());
				return MathHelper.floor(Math.max(f/3f, 0)*15);
			}
		}
		return super.getComparatorInputOverride();
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
		if(!hasElectrodes())
			return false;
		if(process.recipe!=null&&!process.recipe.slag.isEmpty())
		{
			if(this.inventory.get(22).isEmpty())
				return true;
			return ItemHandlerHelper.canItemStacksStack(this.inventory.get(22), process.recipe.slag)&&inventory.get(22).getCount()+process.recipe.slag.getCount() <= getSlotLimit(22);
		}
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		BlockPos pos = getPos().add(0, -1, 0).offset(facing, -2);
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
	public void onProcessFinish(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if(!process.recipe.slag.isEmpty())
		{
			if(this.inventory.get(22).isEmpty())
				this.inventory.set(22, process.recipe.slag.copy());
			else if(ItemHandlerHelper.canItemStacksStack(this.inventory.get(22), process.recipe.slag)||inventory.get(22).getCount()+process.recipe.slag.getCount() > getSlotLimit(22))
				this.inventory.get(22).grow(process.recipe.slag.getCount());
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
	public int getComparatedSize()
	{
		return 12;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
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
		return slot > 22?1: 64;
	}

	static int[] outputSlots = {16, 17, 18, 19, 20, 21};

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
	}


	IItemHandler inputHandler = new IEInventoryHandler(12, this, 0, true, false)
	{
		//ignore the given slot and spread it out
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(stack.isEmpty())
				return stack;
			stack = stack.copy();
			List<Integer> possibleSlots = new ArrayList<>(12);
			for(int i = 0; i < 12; i++)
			{
				ItemStack here = inventory.get(i);
				if(here.isEmpty())
				{
					if(!simulate)
						inventory.set(i, stack);
					return ItemStack.EMPTY;
				}
				else if(ItemHandlerHelper.canItemStacksStack(stack, here)&&here.getCount() < here.getMaxStackSize())
				{
					possibleSlots.add(i);
				}
			}
			Collections.sort(possibleSlots, (a, b) -> Integer.compare(inventory.get(a).getCount(), inventory.get(b).getCount()));
			for(int i : possibleSlots)
			{
				ItemStack here = inventory.get(i);
				int fillCount = Math.min(here.getMaxStackSize()-here.getCount(), stack.getCount());
				if(!simulate)
					here.grow(fillCount);
				stack.shrink(fillCount);
				if(stack.isEmpty())
					return ItemStack.EMPTY;
			}
			return stack;
		}
	};
	IItemHandler additiveHandler = new IEInventoryHandler(4, this, 12, true, false);
	IItemHandler outputHandler = new IEInventoryHandler(6, this, 16, false, true);
	IItemHandler slagHandler = new IEInventoryHandler(1, this, 22, false, true);

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if((pos==2||pos==22||pos==86||pos==88)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master()!=null;
		return super.hasCapability(capability, facing);
	}

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
			else if(pos==(mirrored?88: 86))
				return (T)master.inputHandler;
			else if(pos==(mirrored?86: 88))
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
		if(recipe!=null&&recipe instanceof ArcFurnaceRecipe)
		{
			MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace((ArcFurnaceRecipe)recipe, tag.getIntArray("process_inputSlots"));
			if(tag.hasKey("process_inputAmounts"))
				process.setInputAmounts(tag.getIntArray("process_inputAmounts"));
			return process;
		}
		return null;
	}

	@Override
	public boolean canOpenGui()
	{
		return formed&&(pos==2||pos==25||(pos > 25&&pos%5 > 0&&pos%5 < 4&&pos%25/5 < 4));
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
		protected NonNullList<ItemStack> getRecipeItemOutputs(TileEntityMultiblockMetal multiblock)
		{
			ItemStack input = multiblock.getInventory().get(this.inputSlots[0]);
			NonNullList<ItemStack> additives = NonNullList.withSize(4, ItemStack.EMPTY);
			for(int i = 0; i < 4; i++)
				additives.set(i, !multiblock.getInventory().get(12+i).isEmpty()?multiblock.getInventory().get(12+i).copy(): ItemStack.EMPTY);
			return recipe.getOutputs(input, additives);
		}

		@Override
		protected void processFinish(TileEntityMultiblockMetal te)
		{
			super.processFinish(te);
			te.getWorld().addBlockEvent(te.getPos(), te.getBlockType(), 0, 40);
		}
	}

	public boolean hasElectrodes()
	{
		for(int i = 23; i < 26; i++)
			if(inventory.get(i).isEmpty())
				return false;
		return true;
	}
}