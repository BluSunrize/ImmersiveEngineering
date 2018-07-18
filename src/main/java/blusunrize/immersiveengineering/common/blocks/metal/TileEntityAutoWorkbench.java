/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAutoWorkbench;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityAutoWorkbench extends TileEntityMultiblockMetal<TileEntityAutoWorkbench, IMultiblockRecipe> implements IGuiTile, IConveyorAttachable// IAdvancedSelectionBounds,IAdvancedCollisionBounds
{
	public TileEntityAutoWorkbench()
	{
		super(MultiblockAutoWorkbench.instance, new int[]{2, 3, 3}, 32000, true);
	}

	public NonNullList<ItemStack> inventory = NonNullList.withSize(17, ItemStack.EMPTY);
	public int selectedRecipe = -1;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		selectedRecipe = nbt.getInteger("selectedRecipe");
		if(!descPacket)
		{
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 17);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("selectedRecipe", selectedRecipe);
//		if(!descPacket) Disabled because blueprint. Have yet to see issue because of this
		{
			nbt.setTag("inventory", Utils.writeInventory(inventory));
		}
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(message.hasKey("recipe"))
		{
			this.selectedRecipe = message.getInteger("recipe");
		}
	}

	@Override
	public void update()
	{
		super.update();

		if(isDummy()||isRSDisabled()||world.isRemote||world.getTotalWorldTime()%16!=((getPos().getX()^getPos().getZ())&15)||inventory.get(0).isEmpty())
			return;

		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(inventory.get(0), "blueprint"));
		if(recipes.length > 0&&(this.selectedRecipe >= 0&&this.selectedRecipe < recipes.length))
		{
			BlueprintCraftingRecipe recipe = recipes[this.selectedRecipe];
			if(recipe!=null&&!recipe.output.isEmpty())
			{
				NonNullList<ItemStack> query = NonNullList.withSize(16, ItemStack.EMPTY);
				for(int i = 0; i < query.size(); i++)
					query.set(i, inventory.get(i+1));
				int crafted = recipe.getMaxCrafted(query);
				if(crafted > 0)
				{
					if(this.addProcessToQueue(new MultiblockProcessInWorld(recipe, 0.78f, NonNullList.create()), true))
					{
						this.addProcessToQueue(new MultiblockProcessInWorld(recipe, 0.78f, recipe.consumeInputs(query, 1)), false);
						for(int i = 0; i < query.size(); i++)
							inventory.set(i+1, query.get(i));
						this.markDirty();
						this.markContainingBlockForUpdate(null);
					}
				}
			}
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos < 10||pos==12)
			return new float[]{0, 0, 0, 1, 1, 1};
		if(pos >= 13&&pos <= 16)
			return new float[]{0, 0, 0, 1, .125f, 1};
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if(pos==10||pos==11)
		{
			yMax = .8125f;
			if(facing==EnumFacing.NORTH)
			{
				zMin = .1875f;
				if(pos==11)
					xMax = .875f;
			}
			else if(facing==EnumFacing.SOUTH)
			{
				zMax = .8125f;
				if(pos==11)
					xMin = .125f;
			}
			else if(facing==EnumFacing.WEST)
			{
				xMin = .1875f;
				if(pos==11)
					zMin = .125f;
			}
			else if(facing==EnumFacing.EAST)
			{
				xMax = .8125f;
				if(pos==11)
					zMax = .875f;
			}
		}
		if(pos==17)
		{
			yMax = .3125f;
			if(facing==EnumFacing.NORTH)
			{
				zMin = .25f;
				xMax = .875f;
			}
			else if(facing==EnumFacing.SOUTH)
			{
				zMax = .75f;
				xMin = .125f;
			}
			else if(facing==EnumFacing.WEST)
			{
				xMin = .25f;
				zMin = .125f;
			}
			else if(facing==EnumFacing.EAST)
			{
				xMax = .75f;
				zMax = .875f;
			}
		}
		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{9};
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
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		EnumFacing outDir = mirrored?facing.rotateYCCW(): facing.rotateY();
		BlockPos pos = getPos().offset(outDir, 2);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile!=null)
			output = Utils.insertStackIntoInventory(inventoryTile, output, outDir.getOpposite());
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, pos, output, outDir);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return .4375f;
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
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
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
		if(pos==9&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master()!=null;
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new IEInventoryHandler(16, this, 1, true, false);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(pos==9&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityAutoWorkbench master = master();
			if(master==null)
				return null;
			return (T)master.insertionHandler;
		}
		return super.getCapability(capability, facing);
	}


	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return BlueprintCraftingRecipe.loadFromNBT(tag);
	}


	@Override
	public boolean canOpenGui()
	{
		return formed;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_AutoWorkbench;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}


	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return true;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return true;
	}

	@Override
	public EnumFacing[] sigOutputDirections()
	{
		if(pos==14)
			return new EnumFacing[]{this.facing.rotateY()};
		return new EnumFacing[0];
	}

	@Override
	public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w)
	{
		if(state.getBlock()==IEContent.blockConveyor)
		{
			if((l==2&&w==0)||l==1)
				state = state.withProperty(IEProperties.FACING_ALL, facing.rotateY());
			else
				state = state.withProperty(IEProperties.FACING_ALL, facing.getOpposite());
		}
		super.replaceStructureBlock(pos, state, stack, h, l, w);
	}
}