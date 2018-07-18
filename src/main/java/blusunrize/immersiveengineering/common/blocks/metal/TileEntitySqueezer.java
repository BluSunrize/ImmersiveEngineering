/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TileEntitySqueezer extends TileEntityMultiblockMetal<TileEntitySqueezer, SqueezerRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile
{
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(11, ItemStack.EMPTY);
	public float animation_piston = 0;
	public boolean animation_down = true;

	public TileEntitySqueezer()
	{
		super(MultiblockSqueezer.instance, new int[]{3, 3, 3}, 16000, true);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 11);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		NBTTagCompound tankTag = tanks[0].writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update()
	{
		super.update();
		if(isDummy()||isRSDisabled())
			return;

		if(world.isRemote)
		{
			if(this.processQueue.isEmpty()&&animation_piston < .6875)
				animation_piston = Math.min(.6875f, animation_piston+.03125f);
			else if(shouldRenderAsActive())
			{
				if(animation_down)
					animation_piston = Math.max(0, animation_piston-.03125f);
				else
					animation_piston = Math.min(.6875f, animation_piston+.03125f);
				if(animation_piston <= 0&&animation_down)
					animation_down = false;
				else if(animation_piston >= .6875&&!animation_down)
					animation_down = true;
			}
		}
		else
		{
			boolean update = false;
			if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
			{
				final int[] usedInvSlots = new int[8];
				for(MultiblockProcess process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
						for(int i : ((MultiblockProcessInMachine)process).inputSlots)
							usedInvSlots[i]++;

				Integer[] preferredSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
				Arrays.sort(preferredSlots, 0, 8, new Comparator<Integer>()
				{
					@Override
					public int compare(Integer arg0, Integer arg1)
					{
						return Integer.compare(usedInvSlots[arg0], usedInvSlots[arg1]);
					}
				});
				for(int slot : preferredSlots)
				{
					ItemStack stack = this.getInventory().get(slot);
					if(!stack.isEmpty())
					{
						stack = stack.copy();
						stack.shrink(usedInvSlots[slot]);
					}
					if(!stack.isEmpty()&&stack.getCount() > 0)
					{
						SqueezerRecipe recipe = this.findRecipeForInsertion(stack);
						if(recipe!=null)
						{
							MultiblockProcessInMachine<SqueezerRecipe> process = new MultiblockProcessInMachine(recipe, slot);
							if(this.addProcessToQueue(process, true))
							{
								this.addProcessToQueue(process, false);
								update = true;
							}
						}
					}
				}
			}

			EnumFacing fw = mirrored?facing.rotateYCCW(): facing.rotateY();
			if(this.tanks[0].getFluidAmount() > 0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[0].getFluid(), Math.min(this.tanks[0].getFluidAmount(), 80), false);
				BlockPos outputPos = this.getPos().add(0, -1, 0).offset(fw, 2);
				IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, fw.getOpposite());
				if(output!=null)
				{
					int accepted = output.fill(out, false);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
						this.tanks[0].drain(drained, true);
						update = true;
					}
				}
				ItemStack empty = getInventory().get(9);
				if(!empty.isEmpty()&&tanks[0].getFluidAmount() > 0)
				{
					ItemStack full = Utils.fillFluidContainer(tanks[0], empty, getInventory().get(10), null);
					if(!full.isEmpty())
					{
						if(getInventory().get(9).getCount()==1&&!Utils.isFluidContainerFull(full))
							getInventory().set(9, full.copy());
						else
						{
							if(!getInventory().get(10).isEmpty()&&OreDictionary.itemMatches(full, getInventory().get(10), true))
								getInventory().get(10).grow(full.getCount());
							else
								getInventory().set(10, full);
							inventory.get(9).shrink(1);
							if(inventory.get(9).getCount() <= 0)
								inventory.set(9, ItemStack.EMPTY);
						}
						update = true;
					}
				}
			}
			if(!inventory.get(8).isEmpty()&&world.getTotalWorldTime()%8==0)
			{
				BlockPos outputPos = this.getPos().offset(fw);
				TileEntity outputTile = Utils.getExistingTileEntity(world, outputPos);
				if(outputTile!=null)
				{
					ItemStack stack = Utils.copyStackWithAmount(inventory.get(8), 1);
					stack = Utils.insertStackIntoInventory(outputTile, stack, fw.getOpposite());
					if(stack.isEmpty())
					{
						this.inventory.get(8).shrink(1);
						if(this.inventory.get(8).getCount() <= 0)
							this.inventory.set(8, ItemStack.EMPTY);
					}
				}
			}

			if(update)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos > 0&&pos < 9&&pos!=5)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(pos==11)
			return new float[]{facing==EnumFacing.WEST?.5f: 0, 0, facing==EnumFacing.NORTH?.5f: 0, facing==EnumFacing.EAST?.5f: 1, 1, facing==EnumFacing.SOUTH?.5f: 1};


		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(pos==2)
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
		if(pos==3||pos==4||pos==6||pos==7)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos > 5)
				fl = fl.getOpposite();
			if(pos%3==1)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?.6875f: fl==EnumFacing.EAST?.0625f: fw==EnumFacing.EAST?.0625f: .6875f;
			float maxX = fl==EnumFacing.EAST?.3125f: fl==EnumFacing.WEST?.9375f: fw==EnumFacing.EAST?.3125f: .9375f;
			float minZ = fl==EnumFacing.NORTH?.6875f: fl==EnumFacing.SOUTH?.0625f: fw==EnumFacing.SOUTH?.0625f: .6875f;
			float maxZ = fl==EnumFacing.SOUTH?.3125f: fl==EnumFacing.NORTH?.9375f: fw==EnumFacing.SOUTH?.3125f: .9375f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(pos==4)
			{
				minX = fl==EnumFacing.WEST?.375f: fl==EnumFacing.EAST?.625f: fw==EnumFacing.WEST?-.125f: 0;
				maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?1.125f: 1;
				minZ = fl==EnumFacing.NORTH?.375f: fl==EnumFacing.SOUTH?.625f: fw==EnumFacing.NORTH?-.125f: 0;
				maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?1.125f: 1;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==EnumFacing.WEST?-.125f: fl==EnumFacing.EAST?.625f: fw==EnumFacing.WEST?-.125f: .875f;
				maxX = fl==EnumFacing.EAST?1.125f: fl==EnumFacing.WEST?.375f: fw==EnumFacing.EAST?1.125f: .125f;
				minZ = fl==EnumFacing.NORTH?-.125f: fl==EnumFacing.SOUTH?.625f: fw==EnumFacing.NORTH?-.125f: .875f;
				maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?.375f: fw==EnumFacing.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==EnumFacing.WEST?-.125f: fl==EnumFacing.EAST?.875f: fw==EnumFacing.WEST?-.125f: .875f;
				maxX = fl==EnumFacing.EAST?1.125f: fl==EnumFacing.WEST?.125f: fw==EnumFacing.EAST?1.125f: .125f;
				minZ = fl==EnumFacing.NORTH?-.125f: fl==EnumFacing.SOUTH?.875f: fw==EnumFacing.NORTH?-.125f: .875f;
				maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?.125f: fw==EnumFacing.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .75f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		if((pos==12||pos==13||pos==15||pos==16)||(pos==21||pos==22||pos==24||pos==25))
		{
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>(2);
			if(pos < 18)
				list.add(new AxisAlignedBB(0, 0, 0, 1, .125f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos%9 > 5)
				fl = fl.getOpposite();
			if(pos%3==1)
				fw = fw.getOpposite();
			float minY = pos < 18?.125f: -.875f;
			float maxY = pos < 18?1.125f: .125f;

			float minX = fl==EnumFacing.WEST?.84375f: fl==EnumFacing.EAST?0f: fw==EnumFacing.EAST?0f: .84375f;
			float maxX = fl==EnumFacing.EAST?.15625f: fl==EnumFacing.WEST?1f: fw==EnumFacing.EAST?.15625f: 1;
			float minZ = fl==EnumFacing.NORTH?.84375f: fl==EnumFacing.SOUTH?0f: fw==EnumFacing.SOUTH?0f: .84375f;
			float maxZ = fl==EnumFacing.SOUTH?.15625f: fl==EnumFacing.NORTH?1f: fw==EnumFacing.SOUTH?.15625f: 1;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?0f: fl==EnumFacing.EAST?.15625f: fw==EnumFacing.EAST?.0625f: .8125f;
			maxX = fl==EnumFacing.EAST?1f: fl==EnumFacing.WEST?.84375f: fw==EnumFacing.EAST?.1875f: .9375f;
			minZ = fl==EnumFacing.NORTH?0f: fl==EnumFacing.SOUTH?.15625f: fw==EnumFacing.SOUTH?.0625f: .8125f;
			maxZ = fl==EnumFacing.SOUTH?1f: fl==EnumFacing.NORTH?.84375f: fw==EnumFacing.SOUTH?.1875f: .9375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?.8125f: fl==EnumFacing.EAST?.0625f: fw==EnumFacing.EAST?.15625f: 0f;
			maxX = fl==EnumFacing.EAST?.1875f: fl==EnumFacing.WEST?.9375f: fw==EnumFacing.EAST?1f: .84375f;
			minZ = fl==EnumFacing.NORTH?.8125f: fl==EnumFacing.SOUTH?.0625f: fw==EnumFacing.SOUTH?.15625f: 0f;
			maxZ = fl==EnumFacing.SOUTH?.1875f: fl==EnumFacing.NORTH?.9375f: fw==EnumFacing.SOUTH?1f: .84375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(pos > 18)
			{
				minX = fl==EnumFacing.WEST?-.25f: fl==EnumFacing.EAST?1.25f: fw==EnumFacing.EAST?.75f: -.25f;
				maxX = fl==EnumFacing.EAST?.75f: fl==EnumFacing.WEST?.25f: fw==EnumFacing.EAST?1.25f: .25f;
				minZ = fl==EnumFacing.NORTH?-.25f: fl==EnumFacing.SOUTH?1.25f: fw==EnumFacing.SOUTH?.75f: -.25f;
				maxZ = fl==EnumFacing.SOUTH?.75f: fl==EnumFacing.NORTH?.25f: fw==EnumFacing.SOUTH?1.25f: .25f;
				list.add(new AxisAlignedBB(minX, .375f, minZ, maxX, .9375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
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

	//	@Override
	//	public void onEntityCollision(World world, Entity entity)
	//	{
	//		if(pos==3 && !world.isRemote && entity!=null && !entity.isDead && entity instanceof EntityItem && ((EntityItem)entity).getEntityItem()!=null)
	//		{
	//			TileEntitySqueezer master = master();
	//			if(master==null)
	//				return;
	//			ItemStack stack = ((EntityItem)entity).getEntityItem();
	//			if(stack==null)
	//				return;
	//			IMultiblockRecipe recipe = master.findRecipeForInsertion(stack);
	//			if(recipe==null)
	//				return;
	//			ItemStack displayStack = null;
	//			for(IngredientStack ingr : recipe.getItemInputs())
	//				if(ingr.matchesItemStack(stack))
	//				{
	//					displayStack = Utils.copyStackWithAmount(stack, ingr.inputSize);
	//					break;
	//				}
	//			MultiblockProcess process = new MultiblockProcessInWorld(recipe, displayStack, .5f);
	//			if(master.addProcessToQueue(process, true))
	//			{
	//				master.addProcessToQueue(process, false);
	//				stack.stackSize -= displayStack.stackSize;
	//				if(stack.stackSize<=0)
	//					entity.setDead();
	//			}
	//		}
	//	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{9};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{11};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<SqueezerRecipe> process)
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
	public void onProcessFinish(MultiblockProcess<SqueezerRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 8;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 8;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<SqueezerRecipe> process)
	{
		return 0;
	}


	@Override
	public int getComparatedSize()
	{
		return 8;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[]{8};
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{0};
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntitySqueezer master = master();
		if(master!=null&&pos==5&&(side==null||side==(mirrored?facing.rotateYCCW(): facing.rotateY())))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return true;
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
		if((pos==15||pos==13)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master()!=null;
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8]);
	IItemHandler extractionHandler = new IEInventoryHandler(1, this, 8, new boolean[1], new boolean[]{true});

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if((pos==15||pos==13)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntitySqueezer master = master();
			if(master==null)
				return null;
			if(pos==15)
				return (T)master.insertionHandler;
			if(pos==13)
				return (T)master.extractionHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public SqueezerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return SqueezerRecipe.findRecipe(inserting);
	}

	@Override
	protected SqueezerRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return SqueezerRecipe.loadFromNBT(tag);
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
	@Override
	public boolean canOpenGui()
	{
		return formed;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Squeezer;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}
}