package blusunrize.immersiveengineering.common.util;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameData;

public class Utils
{
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		for(int oid : OreDictionary.getOreIDs(stack))
			if(OreDictionary.getOreName(oid).equals(oreName))
				return true;
		return false;
	}
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof ItemStack)
			return OreDictionary.itemMatches((ItemStack)o, stack, false);
		else if(o instanceof ArrayList)
		{
			for(Object io : (ArrayList)o)
				if(io instanceof ItemStack && OreDictionary.itemMatches((ItemStack)io, stack, false))
					return true;
		}
		else if(o instanceof ItemStack[])
		{
			for(ItemStack io : (ItemStack[])o)
				if(OreDictionary.itemMatches((ItemStack)io, stack, false))
					return true;
		}
		else if(o instanceof String)
			return compareToOreName(stack, (String)o);
		return false;
	}
	public static ItemStack copyStackWithAmount(ItemStack stack, int amount)
	{
		if(stack==null)
			return null;
		ItemStack s2 = stack.copy();
		s2.stackSize=amount;
		return s2;
	}
	static String[] dyeNames = {"Black","Red","Green","Brown","Blue","Purple","Cyan","LightGray","Gray","Pink","Lime","Yellow","LightBlue","Magenta","Orange","White"};
	public static int getDye(ItemStack stack)
	{
		if(stack==null)
			return -1;
		if(stack.getItem().equals(Items.dye))
			return stack.getItemDamage();
		for(int dye=0;dye<dyeNames.length;dye++)
			if(compareToOreName(stack,"dye"+dyeNames[dye]))
				return dye;
		return -1;
	}

	public static ChunkCoordinates toCC(Object object)
	{
		if(object instanceof ChunkCoordinates)
			return (ChunkCoordinates)object;
		if(object instanceof TileEntity)
			return new ChunkCoordinates(((TileEntity)object).xCoord,((TileEntity)object).yCoord,((TileEntity)object).zCoord);
		return null;
	}
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof ChunkCoordinates && world!=null && world.blockExists( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ) && world.getTileEntity( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ) instanceof IImmersiveConnectable)
		{
			return (IImmersiveConnectable)world.getTileEntity( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ);
		}
		return null;
	}

	public static String formatDouble(double d, String s)
	{
		DecimalFormat df = new DecimalFormat(s);
		return df.format(d);
	}
	public static String toCamelCase(String s)
	{
		return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
	}
	static Method m_getHarvestLevel = null;
	public static String getHarvestLevelName(int lvl)
	{
		if(Loader.isModLoaded("TConstruct"))
		{
			try{
				if(m_getHarvestLevel==null)
				{
					Class clazz = Class.forName("tconstruct.library.util");
					if(clazz!=null)
						m_getHarvestLevel = clazz.getDeclaredMethod("getHarvestLevelName", int.class);
				}
				if(m_getHarvestLevel!=null)
					return (String)m_getHarvestLevel.invoke(null, lvl);
			}catch(Exception e){}
		}
		return StatCollector.translateToLocal(Lib.DESC_INFO+"mininglvl."+Math.max(-1, Math.min(lvl, 6)));
	}

	public static boolean tilePositionMatch(TileEntity tile0, TileEntity tile1)
	{
		return tile0.xCoord==tile1.xCoord && tile0.yCoord==tile1.yCoord && tile0.zCoord==tile1.zCoord;
	}

	public static MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityLivingBase living, boolean bool)
	{
		float f = 1.0F;
		float f1 = living.prevRotationPitch + (living.rotationPitch - living.prevRotationPitch) * f;
		float f2 = living.prevRotationYaw + (living.rotationYaw - living.prevRotationYaw) * f;
		double d0 = living.prevPosX + (living.posX - living.prevPosX) * (double)f;
		double d1 = living.prevPosY + (living.posY - living.prevPosY) * (double)f + (double)(world.isRemote ? living.getEyeHeight() - (living instanceof EntityPlayer?((EntityPlayer)living).getDefaultEyeHeight():0) : living.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
		double d2 = living.prevPosZ + (living.posZ - living.prevPosZ) * (double)f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if (living instanceof EntityPlayerMP)
			d3 = ((EntityPlayerMP)living).theItemInWorldManager.getBlockReachDistance();

		Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
		return world.func_147447_a(vec3, vec31, bool, !bool, false);
	}
	public static boolean canBlocksSeeOther(World world, ChunkCoordinates cc0, ChunkCoordinates cc1, Vec3 pos0, Vec3 pos1)
	{
		MovingObjectPosition mop = world.rayTraceBlocks(pos0, pos1);
		return mop==null || (mop.blockX==cc1.posX&&mop.blockY==cc1.posY&&mop.blockZ==cc1.posZ);
	}

	public static boolean isHammer(ItemStack stack)
	{
		if(stack==null)
			return false;
		return stack.getItem().getToolClasses(stack).contains(Lib.TOOL_HAMMER);
	}

	public static Vec3 getFlowVector(World world, int x, int y, int z)
	{
		if(world.getBlock(x, y, z) instanceof BlockFluidBase)
			return ((BlockFluidBase)world.getBlock(x, y, z)).getFlowVector(world, x, y, z);
		else if( !(world.getBlock(x, y, z) instanceof BlockLiquid))
			return Vec3.createVectorHelper(0, 0, 0);

		BlockLiquid block = (BlockLiquid)world.getBlock(x, y, z);
		Vec3 vec3 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
		Material mat = block.getMaterial();
		int l = getEffectiveFlowDecay(world, x, y, z, mat);

		for (int i1 = 0; i1 < 4; ++i1)
		{
			int j1 = x;
			int k1 = z;

			if (i1 == 0)
				j1 = x - 1;
			if (i1 == 1)
				k1 = z - 1;
			if (i1 == 2)
				++j1;
			if (i1 == 3)
				++k1;
			int l1 = getEffectiveFlowDecay(world, j1, y, k1, mat);
			int i2;

			if (l1 < 0)
			{
				if (!world.getBlock(j1, y, k1).getMaterial().blocksMovement())
				{
					l1 = getEffectiveFlowDecay(world, j1, y - 1, k1, mat);

					if (l1 >= 0)
					{
						i2 = l1 - (l - 8);
						vec3 = vec3.addVector((double)((j1 - x) * i2), (double)((y - y) * i2), (double)((k1 - z) * i2));
					}
				}
			}
			else if (l1 >= 0)
			{
				i2 = l1 - l;
				vec3 = vec3.addVector((double)((j1 - x) * i2), (double)((y - y) * i2), (double)((k1 - z) * i2));
			}
		}

		if (world.getBlockMetadata(x, y, z) >= 8)
		{
			boolean flag = false;

			if (flag || block.isBlockSolid(world, x, y, z - 1, 2))
				flag = true;
			if (flag || block.isBlockSolid(world, x, y, z + 1, 3))
				flag = true;
			if (flag || block.isBlockSolid(world, x - 1, y, z, 4))
				flag = true;
			if (flag || block.isBlockSolid(world, x + 1, y, z, 5))
				flag = true;
			if (flag || block.isBlockSolid(world, x, y + 1, z - 1, 2))
				flag = true;
			if (flag || block.isBlockSolid(world, x, y + 1, z + 1, 3))
				flag = true;
			if (flag || block.isBlockSolid(world, x - 1, y + 1, z, 4))
				flag = true;
			if (flag || block.isBlockSolid(world, x + 1, y + 1, z, 5))
				flag = true;
			if (flag)
				vec3 = vec3.normalize().addVector(0.0D, -6.0D, 0.0D);
		}
		vec3 = vec3.normalize();
		return vec3;
	}
	static int getEffectiveFlowDecay(IBlockAccess world, int x, int y, int z, Material mat)
	{
		if (world.getBlock(x, y, z).getMaterial() != mat)
			return -1;
		int l = world.getBlockMetadata(x, y, z);
		if (l >= 8)
			l = 0;
		return l;
	}
	public static Vec3 addVectors(Vec3 vec0, Vec3 vec1)
	{
		return vec0.addVector(vec1.xCoord,vec1.yCoord,vec1.zCoord);
	}

	public static Collection<ItemStack> getContainersFilledWith(FluidStack fluidStack)
	{
		List<ItemStack> containers = new ArrayList();
		for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData())
			if(data.fluid.containsFluid(fluidStack))
				containers.add(data.filledContainer);
		return containers;
	}

	public static String nameFromStack(ItemStack stack)
	{
		if(stack==null)
			return "";
		try
		{
			return GameData.getItemRegistry().getNameForObject(stack.getItem());
		}
		catch (NullPointerException e) {}
		return "";
	}

	public static ItemStack insertStackIntoInventory(IInventory inventory, ItemStack stack, int side)
	{
		if (stack == null || inventory == null)
			return null;
		int stackSize = stack.stackSize;
		if (inventory instanceof ISidedInventory)
		{
			ISidedInventory sidedInv = (ISidedInventory) inventory;
			int slots[] = sidedInv.getAccessibleSlotsFromSide(side);
			if (slots == null)
				return stack;
			for (int i=0; i<slots.length && stack!=null; i++)
			{
				if (sidedInv.canInsertItem(slots[i], stack, side))
				{
					ItemStack existingStack = inventory.getStackInSlot(slots[i]);
					if(OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
						stack = addToOccupiedSlot(sidedInv, slots[i], stack, existingStack);
				}
			}
			for (int i=0; i<slots.length && stack!=null; i++)
				if (inventory.getStackInSlot(slots[i]) == null && sidedInv.canInsertItem(slots[i], stack, side))
					stack = addToEmptyInventorySlot(sidedInv, slots[i], stack);
		}
		else
		{
			int invSize = inventory.getSizeInventory();
			for (int i=0; i<invSize && stack!=null; i++)
			{
				ItemStack existingStack = inventory.getStackInSlot(i);
				if (OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
					stack = addToOccupiedSlot(inventory, i, stack, existingStack);
			}
			for (int i=0; i<invSize && stack!=null; i++)
				if (inventory.getStackInSlot(i) == null)
					stack = addToEmptyInventorySlot(inventory, i, stack);
		}
		if (stack == null || stack.stackSize != stackSize)
			inventory.markDirty();
		return stack;
	}

	public static ItemStack addToEmptyInventorySlot(IInventory inventory, int slot, ItemStack stack)
	{
		if (!inventory.isItemValidForSlot(slot, stack)) {
			return stack;
		}
		int stackLimit = inventory.getInventoryStackLimit();
		inventory.setInventorySlotContents(slot, copyStackWithAmount(stack, Math.min(stack.stackSize, stackLimit)));
		return stackLimit >= stack.stackSize ? null : stack.splitStack(stack.stackSize - stackLimit);
	}
	public static ItemStack addToOccupiedSlot(IInventory inventory, int slot, ItemStack stack, ItemStack existingStack)
	{
		int stackLimit = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
		if (stack.stackSize + existingStack.stackSize > stackLimit) {
			int stackDiff = stackLimit - existingStack.stackSize;
			existingStack.stackSize = stackLimit;
			stack.stackSize -= stackDiff;
			inventory.setInventorySlotContents(slot, existingStack);
			return stack;
		}
		existingStack.stackSize += Math.min(stack.stackSize, stackLimit);
		inventory.setInventorySlotContents(slot, existingStack);
		return stackLimit >= stack.stackSize ? null : stack.splitStack(stack.stackSize - stackLimit);
	}


	public static boolean canInsertStackIntoInventory(IInventory inventory, ItemStack stack, int side)
	{
		if(stack == null || inventory == null)
			return false;
		if(inventory instanceof ISidedInventory)
		{
			ISidedInventory sidedInv = (ISidedInventory) inventory;
			int slots[] = sidedInv.getAccessibleSlotsFromSide(side);
			if(slots == null)
				return false;
			for(int i=0; i<slots.length && stack!=null; i++)
			{
				if(sidedInv.canInsertItem(slots[i], stack, side) && sidedInv.isItemValidForSlot(slots[i], stack))
				{
					ItemStack existingStack = inventory.getStackInSlot(slots[i]);
					if(existingStack==null)
						return true;
					else
						if(OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
							if(existingStack.stackSize+stack.stackSize<inventory.getInventoryStackLimit() && existingStack.stackSize+stack.stackSize<existingStack.getMaxStackSize())
								return true;
				}
			}
		}
		else
		{
			int invSize = inventory.getSizeInventory();
			for(int i=0; i<invSize && stack!=null; i++)
				if(inventory.isItemValidForSlot(i, stack))
				{
					ItemStack existingStack = inventory.getStackInSlot(i);
					if(existingStack==null)
						return true;
					else
						if(OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
							if(existingStack.stackSize+stack.stackSize<inventory.getInventoryStackLimit() && existingStack.stackSize+stack.stackSize<existingStack.getMaxStackSize())
								return true;
				}
		}
		return false;
	}

	public static ItemStack fillFluidContainer(FluidTank tank, ItemStack containerIn, ItemStack containerOut)
	{
		if(tank.getFluidAmount()>0 && containerIn!=null)
		{
			if(FluidContainerRegistry.isEmptyContainer(containerIn))
			{
				ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(tank.getFluid(), containerIn);
				if(filledContainer!=null)
				{
					FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(filledContainer);
					if(fs.amount<=tank.getFluidAmount() && (containerOut==null || OreDictionary.itemMatches(containerOut, filledContainer, true)))
					{
						tank.drain(fs.amount, true);
						return filledContainer;
					}
				}
			}
			else if(containerIn.getItem() instanceof IFluidContainerItem)
			{
				IFluidContainerItem iContainer = (IFluidContainerItem)containerIn.getItem();
				int available = tank.getFluidAmount();
				int space = iContainer.getCapacity(containerIn)-(iContainer.getFluid(containerIn)==null?0:iContainer.getFluid(containerIn).amount);
				if(available>=space && iContainer.fill(containerIn, tank.getFluid(), false)==space)//Fill in one go
				{
					ItemStack filledContainer = copyStackWithAmount(containerIn,1);
					int filled = iContainer.fill(filledContainer, tank.getFluid(), true);
					if(containerOut==null || (OreDictionary.itemMatches(containerOut, filledContainer, true) && ItemStack.areItemStackTagsEqual(filledContainer, containerOut) ))
					{
						tank.drain(filled, true);
						return filledContainer;
					}
				}
				else
				{
					int filled = iContainer.fill(containerIn, tank.getFluid(), true);
					tank.drain(filled, true);
				}
			}
		}
		return null;
	}
	public static ItemStack drainFluidContainer(FluidTank tank, ItemStack containerIn)
	{
		if(containerIn!=null)
			if(FluidContainerRegistry.isFilledContainer(containerIn))
			{
				FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(containerIn);
				if(fs!=null && tank.getFluidAmount()+fs.amount <= tank.getCapacity())
				{
					ItemStack emptyContainer = FluidContainerRegistry.drainFluidContainer(containerIn);
					if(emptyContainer!=null && tank.fill(fs, true)==fs.amount)
						return emptyContainer;
				}
			}
		return null;
	}

	public static FluidStack getFluidFromItemStack(ItemStack stack)
	{
		if(stack==null)
			return null;
		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
		if(fluid != null)
			return fluid;
		else if(stack.getItem() instanceof IFluidContainerItem)
			return ((IFluidContainerItem)stack.getItem()).getFluid(stack);
		return null;
	}

	public static boolean isFluidRelatedItemStack(ItemStack stack)
	{
		if(stack==null)
			return false;
		return FluidContainerRegistry.isContainer(stack)||stack.getItem() instanceof IFluidContainerItem;
	}
	
	public static boolean fillPlayerItemFromFluidHandler(World world, IFluidHandler handler, EntityPlayer player, FluidStack tankFluid)
	{
		ItemStack equipped = player.getCurrentEquippedItem();
		if(equipped==null)
			return false;
		if(FluidContainerRegistry.isEmptyContainer(equipped))
		{
			ItemStack filledStack = FluidContainerRegistry.fillFluidContainer(tankFluid, equipped);
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(filledStack);
			if(fluid==null || filledStack==null)
				return false;
			if(!player.capabilities.isCreativeMode)
				if(equipped.stackSize == 1)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, filledStack);
					equipped.stackSize -= 1;
					if (equipped.stackSize <= 0)
						equipped = null;
				}
				else 
				{
					if(equipped.stackSize==1)
					{
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
						player.inventory.addItemStackToInventory(filledStack);
					}
					else
					{
						equipped.stackSize -= 1;
						if(filledStack!=null && !player.inventory.addItemStackToInventory(filledStack))
							player.func_146097_a(filledStack, false, true);
					}
					player.openContainer.detectAndSendChanges();
					((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
				}
			handler.drain(ForgeDirection.UNKNOWN, fluid.amount, true);
			return true;
		}
		else if(equipped.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)equipped.getItem();
			if(container.fill(equipped, tankFluid, false)>0)
			{
				int fill = container.fill(equipped, tankFluid, true);
				handler.drain(ForgeDirection.UNKNOWN, fill, true);
				player.openContainer.detectAndSendChanges();
				((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
				return true;
			}
		}
		return false;
	}

	public static boolean fillFluidHandlerWithPlayerItem(World world, IFluidHandler handler, EntityPlayer player)
	{
		ItemStack equipped = player.getCurrentEquippedItem();
		if(equipped==null)
			return false;
		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(equipped);
		if(fluid != null)
		{
			if(handler.fill(ForgeDirection.UNKNOWN, fluid, false) == fluid.amount || player.capabilities.isCreativeMode)
			{
				ItemStack filledStack = FluidContainerRegistry.drainFluidContainer(equipped);
				if (!player.capabilities.isCreativeMode)
				{
					if(equipped.stackSize==1)
					{
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
						player.inventory.addItemStackToInventory(filledStack);
					}
					else
					{
						equipped.stackSize -= 1;
						if(filledStack!=null && !player.inventory.addItemStackToInventory(filledStack))
							player.func_146097_a(filledStack, false, true);
					}
					player.openContainer.detectAndSendChanges();
					((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
				}
				handler.fill(ForgeDirection.UNKNOWN, fluid, true);
				return true;
			}
		}
		else if(equipped.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)equipped.getItem();
			fluid = container.getFluid(equipped);
			if(handler.fill(ForgeDirection.UNKNOWN, fluid, false)>0)
			{
				int fill = handler.fill(ForgeDirection.UNKNOWN, fluid, true);
				container.drain(equipped, fill, true);
				player.openContainer.detectAndSendChanges();
				((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
				return true;
			}
		}
		return false;
	}
}