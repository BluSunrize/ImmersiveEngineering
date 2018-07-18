/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.util.Utils;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class TileEntityRefinery extends TileEntityMultiblockMetal<TileEntityRefinery, RefineryRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile
{

	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000), new FluidTank(24000), new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

	public TileEntityRefinery()
	{
		super(MultiblockRefinery.instance, new int[]{3, 3, 5}, 16000, true);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 6);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update()
	{
		super.update();
		if(world.isRemote||isDummy())
			return;

		boolean update = false;
		if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
		{
			if(tanks[0].getFluidAmount() > 0||tanks[1].getFluidAmount() > 0)
			{
				RefineryRecipe recipe = RefineryRecipe.findRecipe(tanks[0].getFluid(), tanks[1].getFluid());
				if(recipe!=null)
				{
					MultiblockProcessInMachine<RefineryRecipe> process = new MultiblockProcessInMachine(recipe).setInputTanks((tanks[0].getFluidAmount() > 0&&tanks[1].getFluidAmount() > 0)?new int[]{0, 1}: tanks[0].getFluidAmount() > 0?new int[]{0}: new int[]{1});
					if(this.addProcessToQueue(process, true))
					{
						this.addProcessToQueue(process, false);
						update = true;
					}
				}
			}
		}

		if(this.tanks[2].getFluidAmount() > 0)
		{
			ItemStack filledContainer = Utils.fillFluidContainer(tanks[2], inventory.get(4), inventory.get(5), null);
			if(!filledContainer.isEmpty())
			{
				if(getInventory().get(4).getCount()==1&&!Utils.isFluidContainerFull(filledContainer))
					getInventory().set(4, filledContainer.copy());
				else
				{
					if(!inventory.get(5).isEmpty()&&OreDictionary.itemMatches(inventory.get(5), filledContainer, true))
						inventory.get(5).grow(filledContainer.getCount());
					else if(inventory.get(5).isEmpty())
						inventory.set(5, filledContainer.copy());
					inventory.get(4).shrink(1);
					if(inventory.get(4).getCount() <= 0)
						inventory.set(4, ItemStack.EMPTY);
				}
				update = true;
			}
			if(this.tanks[2].getFluidAmount() > 0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[2].getFluid(), Math.min(this.tanks[2].getFluidAmount(), 80), false);
				BlockPos outputPos = this.getPos().add(0, -1, 0).offset(facing.getOpposite());
				IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, facing);
				if(output!=null)
				{
					int accepted = output.fill(out, false);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
						this.tanks[2].drain(drained, true);
						update = true;
					}
				}
			}
		}

		int amount_prev = tanks[0].getFluidAmount();
		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if(amount_prev!=tanks[0].getFluidAmount())
		{
			if(!inventory.get(1).isEmpty()&&OreDictionary.itemMatches(inventory.get(1), emptyContainer, true))
				inventory.get(1).grow(emptyContainer.getCount());
			else if(inventory.get(1).isEmpty())
				inventory.set(1, emptyContainer.copy());
			inventory.get(0).shrink(1);
			if(inventory.get(0).getCount() <= 0)
				inventory.set(0, ItemStack.EMPTY);
			update = true;
		}
		amount_prev = tanks[1].getFluidAmount();
		emptyContainer = Utils.drainFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
		if(amount_prev!=tanks[1].getFluidAmount())
		{
			if(!inventory.get(3).isEmpty()&&OreDictionary.itemMatches(inventory.get(3), emptyContainer, true))
				inventory.get(3).grow(emptyContainer.getCount());
			else if(inventory.get(3).isEmpty())
				inventory.set(3, emptyContainer.copy());
			inventory.get(2).shrink(1);
			if(inventory.get(2).getCount() <= 0)
				inventory.set(2, ItemStack.EMPTY);
			update = true;
		}

		if(update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos==0||pos==1||pos==3)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(pos==19)
			return new float[]{facing==EnumFacing.WEST?.5f: 0, 0, facing==EnumFacing.NORTH?.5f: 0, facing==EnumFacing.EAST?.5f: 1, 1, facing==EnumFacing.SOUTH?.5f: 1};
		if(pos==17)
			return new float[]{.0625f, 0, .0625f, .9375f, 1, .9375f};

		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(pos==0||pos==4||pos==10||pos==14)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos >= 10)
				fl = fl.getOpposite();
			if(pos%10==0)
				fw = fw.getOpposite();

			float minX = fl==EnumFacing.WEST?0: fl==EnumFacing.EAST?.75f: fw==EnumFacing.WEST?.5f: .25f;
			float maxX = fl==EnumFacing.EAST?1: fl==EnumFacing.WEST?.25f: fw==EnumFacing.EAST?.5f: .75f;
			float minZ = fl==EnumFacing.NORTH?0: fl==EnumFacing.SOUTH?.75f: fw==EnumFacing.NORTH?.5f: .25f;
			float maxZ = fl==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.25f: fw==EnumFacing.SOUTH?.5f: .75f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(pos==4)
			{
				minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .125f;
				maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .25f;
				minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .125f;
				maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .25f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .75f;
				maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .875f;
				minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .75f;
				maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .875f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		if(pos==1||pos==3||pos==11||pos==13)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .0f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos >= 10)
				fl = fl.getOpposite();
			if(pos%10==1)
				fw = fw.getOpposite();

			float minX = fl==EnumFacing.WEST?0: fl==EnumFacing.EAST?.75f: fw==EnumFacing.WEST?.75f: 0;
			float maxX = fl==EnumFacing.EAST?1: fl==EnumFacing.WEST?.25f: fw==EnumFacing.EAST?.25f: 1;
			float minZ = fl==EnumFacing.NORTH?0: fl==EnumFacing.SOUTH?.75f: fw==EnumFacing.NORTH?.75f: 0;
			float maxZ = fl==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.25f: fw==EnumFacing.SOUTH?.25f: 1;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}

		if((pos==20||pos==24||pos==25||pos==29)||(pos==35||pos==39||pos==40||pos==44))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(pos%5==4)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?-.25f: fl==EnumFacing.EAST?-.25f: fw==EnumFacing.WEST?-1f: .5f;
			float maxX = fl==EnumFacing.EAST?1.25f: fl==EnumFacing.WEST?1.25f: fw==EnumFacing.EAST?2: .5f;
			float minZ = fl==EnumFacing.NORTH?-.25f: fl==EnumFacing.SOUTH?-.25f: fw==EnumFacing.NORTH?-1f: .5f;
			float maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?1.25f: fw==EnumFacing.SOUTH?2: .5f;
			float minY = pos < 35?.5f: -.5f;
			float maxY = pos < 35?2f: 1f;
			if(pos%15 >= 10)
			{
				minX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				maxX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				minZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
				maxZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if((pos==21||pos==23||pos==26||pos==28)||(pos==36||pos==38||pos==41||pos==43))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(pos%5==3)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?-.25f: fl==EnumFacing.EAST?-.25f: fw==EnumFacing.WEST?0f: -.5f;
			float maxX = fl==EnumFacing.EAST?1.25f: fl==EnumFacing.WEST?1.25f: fw==EnumFacing.EAST?1f: 1.5f;
			float minZ = fl==EnumFacing.NORTH?-.25f: fl==EnumFacing.SOUTH?-.25f: fw==EnumFacing.NORTH?0: -.5f;
			float maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?1.25f: fw==EnumFacing.SOUTH?1f: 1.5f;
			float minY = pos < 35?.5f: -.5f;
			float maxY = pos < 35?2f: 1f;
			if(pos%15 >= 10)
			{
				minX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				maxX += fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0;
				minZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
				maxZ += fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
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
		return new int[]{27};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{19};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<RefineryRecipe> process)
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
	public void onProcessFinish(MultiblockProcess<RefineryRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<RefineryRecipe> process)
	{
		return 0;
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
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{2};
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityRefinery master = this.master();
		if(master!=null)
		{
			if(pos==2&&(side==null||side==facing.getOpposite()))
				return new FluidTank[]{master.tanks[2]};
			if((pos==5||pos==9)&&(side==null||side.getAxis()==facing.rotateYCCW().getAxis()))
				return new FluidTank[]{master.tanks[0], master.tanks[1]};
		}
		return tanks;
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		if((pos==5||pos==9)&&(side==null||side.getAxis()==facing.rotateYCCW().getAxis()))
		{
			TileEntityRefinery master = this.master();
			if(master==null||master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity())
				return false;
			if(master.tanks[0].getFluid()==null&&master.tanks[1].getFluid()==null)
			{
				List<RefineryRecipe> incompleteRecipes = RefineryRecipe.findIncompleteRefineryRecipe(resource, null);
				return incompleteRecipes!=null&&!incompleteRecipes.isEmpty();
			}
			else
			{
				List<RefineryRecipe> incompleteRecipes = RefineryRecipe.findIncompleteRefineryRecipe(resource, master.tanks[iTank==0?1: 0].getFluid());
				return incompleteRecipes!=null&&!incompleteRecipes.isEmpty();
			}
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return pos==2&&(side==null||side==facing.getOpposite());
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public RefineryRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected RefineryRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return RefineryRecipe.loadFromNBT(tag);
	}

	@Override
	public boolean canOpenGui()
	{
		return formed;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Refinery;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}
}