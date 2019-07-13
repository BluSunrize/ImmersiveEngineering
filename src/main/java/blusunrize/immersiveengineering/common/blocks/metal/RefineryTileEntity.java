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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockRefinery;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;

import java.util.ArrayList;
import java.util.List;

public class RefineryTileEntity extends PoweredMultiblockTileEntity<RefineryTileEntity, RefineryRecipe> implements
		IAdvancedSelectionBounds, IAdvancedCollisionBounds, IInteractionObjectIE
{
	public static TileEntityType<RefineryTileEntity> TYPE;

	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000), new FluidTank(24000), new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

	public RefineryTileEntity()
	{
		super(MultiblockRefinery.instance, 16000, true, TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		tanks[2].readFromNBT(nbt.getCompound("tank2"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 6);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", tanks[1].writeToNBT(new CompoundNBT()));
		nbt.put("tank2", tanks[2].writeToNBT(new CompoundNBT()));
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void tick()
	{
		super.tick();
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
					MultiblockProcessInMachine<RefineryRecipe> process = new MultiblockProcessInMachine<>(recipe)
							.setInputTanks((tanks[0].getFluidAmount() > 0&&tanks[1].getFluidAmount() > 0)?new int[]{0, 1}: tanks[0].getFluidAmount() > 0?new int[]{0}: new int[]{1});
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
				if(inventory.get(4).getCount()==1&&!Utils.isFluidContainerFull(filledContainer))
					inventory.set(4, filledContainer.copy());
				else
				{
					if(!inventory.get(5).isEmpty()&&ItemStack.areItemStacksEqual(inventory.get(5), filledContainer))
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
				update |= FluidUtil.getFluidHandler(world, outputPos, facing).map(output -> {
					int accepted = output.fill(out, false);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
						this.tanks[2].drain(drained, true);
						return true;
					}
					return false;
				}).orElse(false);
			}
		}

		int amount_prev = tanks[0].getFluidAmount();
		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if(amount_prev!=tanks[0].getFluidAmount())
		{
			if(!inventory.get(1).isEmpty()&&ItemStack.areItemStacksEqual(inventory.get(1), emptyContainer))
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
			if(!inventory.get(3).isEmpty()&&ItemStack.areItemStacksEqual(inventory.get(3), emptyContainer))
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
		if(posInMultiblock==0||posInMultiblock==1||posInMultiblock==3)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(posInMultiblock==19)
			return new float[]{facing==Direction.WEST?.5f: 0, 0, facing==Direction.NORTH?.5f: 0, facing==Direction.EAST?.5f: 1, 1, facing==Direction.SOUTH?.5f: 1};
		if(posInMultiblock==17)
			return new float[]{.0625f, 0, .0625f, .9375f, 1, .9375f};

		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		Direction fl = facing;
		Direction fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(posInMultiblock==0||posInMultiblock==4||posInMultiblock==10||posInMultiblock==14)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock >= 10)
				fl = fl.getOpposite();
			if(posInMultiblock%10==0)
				fw = fw.getOpposite();

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: fw==Direction.WEST?.5f: .25f;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: fw==Direction.EAST?.5f: .75f;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: fw==Direction.NORTH?.5f: .25f;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?.5f: .75f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(posInMultiblock==4)
			{
				minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
				minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
				minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		if(posInMultiblock==1||posInMultiblock==3||posInMultiblock==11||posInMultiblock==13)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .0f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock >= 10)
				fl = fl.getOpposite();
			if(posInMultiblock%10==1)
				fw = fw.getOpposite();

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: fw==Direction.WEST?.75f: 0;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: fw==Direction.EAST?.25f: 1;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: fw==Direction.NORTH?.75f: 0;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?.25f: 1;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}

		if((posInMultiblock==20||posInMultiblock==24||posInMultiblock==25||posInMultiblock==29)||(posInMultiblock==35||posInMultiblock==39||posInMultiblock==40||posInMultiblock==44))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(posInMultiblock%5==4)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?-.25f: fw==Direction.WEST?-1f: .5f;
			float maxX = fl==Direction.EAST?1.25f: fl==Direction.WEST?1.25f: fw==Direction.EAST?2: .5f;
			float minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?-.25f: fw==Direction.NORTH?-1f: .5f;
			float maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?1.25f: fw==Direction.SOUTH?2: .5f;
			float minY = posInMultiblock < 35?.5f: -.5f;
			float maxY = posInMultiblock < 35?2f: 1f;
			if(posInMultiblock%15 >= 10)
			{
				minX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				maxX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				minZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
				maxZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if((posInMultiblock==21||posInMultiblock==23||posInMultiblock==26||posInMultiblock==28)||(posInMultiblock==36||posInMultiblock==38||posInMultiblock==41||posInMultiblock==43))
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(posInMultiblock%5==3)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?-.25f: fw==Direction.WEST?0f: -.5f;
			float maxX = fl==Direction.EAST?1.25f: fl==Direction.WEST?1.25f: fw==Direction.EAST?1f: 1.5f;
			float minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?-.25f: fw==Direction.NORTH?0: -.5f;
			float maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?1.25f: fw==Direction.SOUTH?1f: 1.5f;
			float minY = posInMultiblock < 35?.5f: -.5f;
			float maxY = posInMultiblock < 35?2f: 1f;
			if(posInMultiblock%15 >= 10)
			{
				minX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				maxX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				minZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
				maxZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
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
		throw new UnsupportedOperationException();
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
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		RefineryTileEntity master = this.master();
		if(master!=null)
		{
			if(posInMultiblock==2&&(side==null||side==facing.getOpposite()))
				return new FluidTank[]{master.tanks[2]};
			if((posInMultiblock==5||posInMultiblock==9)&&(side==null||side.getAxis()==facing.rotateYCCW().getAxis()))
				return new FluidTank[]{master.tanks[0], master.tanks[1]};
		}
		return tanks;
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		if((posInMultiblock==5||posInMultiblock==9)&&(side==null||side.getAxis()==facing.rotateYCCW().getAxis()))
		{
			RefineryTileEntity master = this.master();
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
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return posInMultiblock==2&&(side==null||side==facing.getOpposite());
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
	protected RefineryRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return RefineryRecipe.loadFromNBT(tag);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
	}

	@Override
	public ResourceLocation getGuiName()
	{
		return Lib.GUIID_Refinery;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}
}