package blusunrize.immersiveengineering.common.util;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DirectionalChunkCoords;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;

public class Utils
{
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		if(!ApiUtils.isExistingOreName(oreName))
			return false;
		ItemStack comp = copyStackWithAmount(stack, 1);
		ArrayList<ItemStack> s = OreDictionary.getOres(oreName);
		for (ItemStack st:s)
			if (ItemStack.areItemStacksEqual(comp, st))
				return true;
		return false;
	}
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		return stackMatchesObject(stack, o, false);
	}
	public static boolean stackMatchesObject(ItemStack stack, Object o, boolean checkNBT)
	{
		if(o instanceof ItemStack)
			return OreDictionary.itemMatches((ItemStack)o, stack, false) && (!checkNBT || ((ItemStack)o).getItemDamage()==OreDictionary.WILDCARD_VALUE || ItemStack.areItemStackTagsEqual((ItemStack)o, stack));
		else if(o instanceof ArrayList)
		{
			for(Object io : (ArrayList)o)
				if(io instanceof ItemStack && OreDictionary.itemMatches((ItemStack)io, stack, false) && (!checkNBT || ((ItemStack)io).getItemDamage()==OreDictionary.WILDCARD_VALUE || ItemStack.areItemStackTagsEqual((ItemStack)io, stack)))
					return true;
		}
		else if(o instanceof ItemStack[])
		{
			for(ItemStack io : (ItemStack[])o)
				if(OreDictionary.itemMatches(io, stack, false) && (!checkNBT || io.getItemDamage()==OreDictionary.WILDCARD_VALUE || ItemStack.areItemStackTagsEqual(io, stack)))
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

	public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount, boolean stripPressure)
	{
		if(stack==null)
			return null;
		FluidStack fs = new FluidStack(stack, amount);
		if(stripPressure && fs.tag!=null && fs.tag.hasKey("pressurized"))
		{
			fs.tag.removeTag("pressurized");
			if(fs.tag.hasNoTags())
				fs.tag = null;
		}
		return fs;
	}

	public static ChunkCoordinates toCC(Object object)
	{
		if(object instanceof ChunkCoordinates)
			return (ChunkCoordinates)object;
		if(object instanceof TileEntity)
			return new ChunkCoordinates(((TileEntity)object).xCoord,((TileEntity)object).yCoord,((TileEntity)object).zCoord);
		return null;
	}

	public static DirectionalChunkCoords toDirCC(Object object, ForgeDirection direction)
	{
		if(object instanceof ChunkCoordinates)
			return new DirectionalChunkCoords((ChunkCoordinates)object, direction);
		if(object instanceof TileEntity)
			return new DirectionalChunkCoords(((TileEntity)object).xCoord,((TileEntity)object).yCoord,((TileEntity)object).zCoord, direction);
		return null;
	}

	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof ChunkCoordinates && world!=null && world.blockExists(((ChunkCoordinates) object).posX, ((ChunkCoordinates) object).posY, ((ChunkCoordinates) object).posZ))
		{
			TileEntity te = world.getTileEntity(((ChunkCoordinates) object).posX, ((ChunkCoordinates) object).posY, ((ChunkCoordinates) object).posZ);
			if(te instanceof IImmersiveConnectable)
				return (IImmersiveConnectable) te;
		}
		return null;
	}

	public static String formatDouble(double d, String s)
	{
		DecimalFormat df = new DecimalFormat(s);
		return df.format(d);
	}
	public static String toScientificNotation(int value, String decimalPrecision, int useKilo)
	{
		float formatted = value>=1000000000?value/1000000000f : value>=1000000?value/1000000f: value>=useKilo?value/1000f: value;
		String notation = value>=1000000000?"G" : value>=1000000?"M": value>=useKilo?"K": "";
		return formatDouble(formatted, "0."+decimalPrecision)+notation;
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

	public static String getModVersion(String modid)
	{
		for(ModContainer container : Loader.instance().getActiveModList())
			if(container.getModId().equalsIgnoreCase(modid))
				return container.getVersion();
		return "";
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
		HashSet<ChunkCoordinates> inter = rayTrace(pos0, pos1, world);
		Iterator<ChunkCoordinates> it = inter.iterator();
		while (it.hasNext()) {
			ChunkCoordinates cc = it.next();
			if (!cc.equals(cc0)&&!cc.equals(cc1))
				return false;
		}
		return true;
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
	public static Vec3 rotateVector(Vec3 vec0, double angleX, double angleY, double angleZ)
	{
		Vec3 vec1 = Vec3.createVectorHelper(vec0.xCoord,vec0.yCoord,vec0.zCoord);
		if(angleX!=0)
			vec1.rotateAroundX((float)angleX);
		if(angleY!=0)
			vec1.rotateAroundY((float)angleY);
		if(angleZ!=0)
			vec1.rotateAroundZ((float)angleZ);
		return vec1;
	}

	public static boolean isVecInEntityHead(EntityLivingBase entity, Vec3 vec)
	{
		if(entity.height/entity.width<2)//Crude check to see if the entity is bipedal or at least upright (this should work for blazes)
			return false;
		double d = vec.yCoord-(entity.posY+entity.getEyeHeight());
		if(Math.abs(d)<.25)
			return true;
		return false;
	}

	public static NBTTagCompound getRandomFireworkExplosion(Random rand, int preType)
	{
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound expl = new NBTTagCompound();
		expl.setBoolean("Flicker", true);
		expl.setBoolean("Trail", true);
		int[] colors = new int[rand.nextInt(8) + 1];
		for (int i = 0; i < colors.length; i++)
		{
			int j = rand.nextInt(11)+1;
			if(j>2)
				j++;
			if(j>6)
				j+=2;
			//no black, brown, light grey, grey or white
			colors[i] = ItemDye.field_150922_c[j];
		}
		expl.setIntArray("Colors", colors);
		int type = preType>=0?preType: rand.nextInt(4);
		if(preType<0 && type==3)
			type = 4;
		expl.setByte("Type", (byte) type);
		NBTTagList list = new NBTTagList();
		list.appendTag(expl);
		tag.setTag("Explosions", list);

		return tag;
	}

	public static FluidStack drainFluidBlock(World world, int x, int y, int z, boolean doDrain)
	{
		Block b = world.getBlock(x, y, z);
		Fluid f = FluidRegistry.lookupFluidForBlock(b);

		if(f!=null)
		{
			if(b instanceof IFluidBlock)
			{
				if(((IFluidBlock)b).canDrain(world, x, y, z))
					return ((IFluidBlock) b).drain(world, x, y, z, doDrain);
				else
					return null;
			}
			else
			{
				if(world.getBlockMetadata(x,y,z)==0)
				{
					if(doDrain)
						world.setBlockToAir(x, y, z);
					return new FluidStack(f, 1000);
				}
				return null;
			}
		}
		return null;
	}

	public static boolean placeFluidBlock(World world, int x, int y, int z, FluidStack fluid)
	{
		if(fluid==null || fluid.getFluid()==null)
			return false;
		Block b = world.getBlock(x, y, z);
		Block fluidBlock = fluid.getFluid().getBlock();

		if(Blocks.water.equals(fluidBlock))
			fluidBlock = Blocks.flowing_water;
		else if(Blocks.lava.equals(fluidBlock))
			fluidBlock = Blocks.flowing_lava;

		boolean canPlace = b==null||b.isAir(world,x,y,z)||b.isReplaceable(world,x,y,z);

		if(fluidBlock!=null && canPlace && fluid.amount>=1000)
		{
			boolean placed = false;
			if ((fluidBlock instanceof BlockFluidBase))
			{
				BlockFluidBase blockFluid = (BlockFluidBase)fluidBlock;
				placed = world.setBlock(x, y, z, fluidBlock, blockFluid.getMaxRenderHeightMeta(), 3);
			}
			else
				placed = world.setBlock(x, y, z, fluidBlock);
			if(placed)
				fluid.amount -= 1000;
			return placed;
		}
		return false;
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
					if(fs.amount<=tank.getFluidAmount() && (containerOut==null || (OreDictionary.itemMatches(containerOut, filledContainer, true)&&ItemStack.areItemStackTagsEqual(containerOut, filledContainer))))
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
					if(containerIn.stackSize==1)
					{
						int filled = iContainer.fill(containerIn, tank.getFluid(), true);
						tank.drain(filled, true);
					}
					else
					{
						ItemStack filledContainer = copyStackWithAmount(containerIn,1);
						int filled = iContainer.fill(filledContainer, tank.getFluid(), true);
						if(containerOut==null || (OreDictionary.itemMatches(containerOut, filledContainer, true) && ItemStack.areItemStackTagsEqual(filledContainer, containerOut) && containerOut.stackSize+1<containerOut.getMaxStackSize() ))
						{
							tank.drain(filled, true);
							return filledContainer;
						}
					}
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
			if(world.isRemote)
				return true;

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
					equipped.stackSize -= 1;
					if(!player.inventory.addItemStackToInventory(filledStack))
						player.func_146097_a(filledStack, false, true);
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
				if(world.isRemote)
					return true;

				int fill;
				if(equipped.stackSize > 1)
				{
					ItemStack filled = copyStackWithAmount(equipped, 1);
					equipped.stackSize -= 1;
					fill = container.fill(filled, tankFluid, true);
					if(!player.inventory.addItemStackToInventory(filled))
						player.func_146097_a(filled, false, true);
				}
				else
				{
					fill = container.fill(equipped, tankFluid, true);
				}
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
				if(world.isRemote)
					return true;

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
				if(world.isRemote)
					return true;

				int fill = handler.fill(ForgeDirection.UNKNOWN, fluid, true);
				if(equipped.stackSize > 1)
				{
					ItemStack emptied = copyStackWithAmount(equipped, 1);
					equipped.stackSize -= 1;
					container.drain(emptied, fill, true);
					if(!player.inventory.addItemStackToInventory(emptied))
						player.func_146097_a(emptied, false, true);
				}
				else
				{
					container.drain(equipped, fill, true);
				}
				player.openContainer.detectAndSendChanges();
				((EntityPlayerMP) player).sendContainerAndContentsToPlayer(player.openContainer, player.openContainer.getInventory());
				return true;
			}
		}
		return false;
	}

	public static IRecipe findRecipe(InventoryCrafting crafting, World world)
	{
		for (int i=0; i<CraftingManager.getInstance().getRecipeList().size(); i++)
		{
			IRecipe irecipe = (IRecipe)CraftingManager.getInstance().getRecipeList().get(i);
			if(irecipe.matches(crafting, world))
				return irecipe;
		}
		return null;
	}
	public static class InventoryCraftingFalse extends InventoryCrafting
	{
		private static final Container nullContainer = new Container()
		{
			@Override
			public void onCraftMatrixChanged(IInventory paramIInventory){}
			@Override
			public boolean canInteractWith(EntityPlayer p_75145_1_)
			{
				return false;
			}
		};
		public InventoryCraftingFalse(int w, int h)
		{
			super(nullContainer, w, h);
		}
	}

	public static HashSet<ChunkCoordinates> rayTrace(Vec3 start, Vec3 end, World world)
	{
		HashSet<ChunkCoordinates> ret = new HashSet<ChunkCoordinates>();
		HashSet<ChunkCoordinates> checked = new HashSet<ChunkCoordinates>();
		// x
		if (start.xCoord>end.xCoord)
		{
			Vec3 tmp = start;
			start = end;
			end = tmp;
		}
		double min = start.xCoord;
		double dif =end.xCoord-min;
		double lengthAdd = Math.ceil(min)-start.xCoord;
		Vec3 mov = start.subtract(end);
		if (mov.xCoord!=0)
		{
			mov = scalarProd(mov, 1 / mov.xCoord);
			ray(dif, mov, start, lengthAdd, ret, world, checked, Blocks.diamond_ore);
		}
		// y
		if (mov.yCoord!=0)
		{
			if (start.yCoord>end.yCoord)
			{
				Vec3 tmp = start;
				start = end;
				end = tmp;
			}
			min = start.yCoord;
			dif = end.yCoord-min;
			lengthAdd = Math.ceil(min)-start.yCoord;
			mov = start.subtract(end);
			mov = scalarProd(mov, 1/mov.yCoord);

			ray(dif, mov, start, lengthAdd, ret, world, checked, Blocks.iron_ore);
		}

		// z
		if (mov.zCoord!=0)
		{
			if (start.zCoord>end.zCoord)
			{
				Vec3 tmp = start;
				start = end;
				end = tmp;
			}
			min = start.zCoord;
			dif = end.zCoord - min;
			lengthAdd = Math.ceil(min)-start.zCoord;
			mov = start.subtract(end);
			mov = scalarProd(mov, 1 / mov.zCoord);

			ray(dif, mov, start, lengthAdd, ret, world, checked, Blocks.gold_ore);
		}
		return ret;
	}
	private static void ray(double dif, Vec3 mov, Vec3 start, double lengthAdd, HashSet<ChunkCoordinates> ret, World world, HashSet<ChunkCoordinates> checked, Block tmp)
	{
		//Do NOT set this to true unless for debugging. Causes blocks to be placed along the traced ray
		boolean place = false;
		double standartOff = .0625;
		for (int i = 0; i < dif; i++)
		{
			Vec3 pos = addVectors(start, scalarProd(mov, i + lengthAdd+standartOff));
			Vec3 posNext = addVectors(start,
					scalarProd(mov, i + 1 + lengthAdd+standartOff));
			Vec3 posPrev = addVectors(start,
					scalarProd(mov, i + lengthAdd-standartOff));
			Vec3 posVeryPrev = addVectors(start,
					scalarProd(mov, i - 1 + lengthAdd-standartOff));

			ChunkCoordinates cc = new ChunkCoordinates((int) Math.floor(pos.xCoord),
					(int) Math.floor(pos.yCoord), (int) Math.floor(pos.zCoord));
			Block b;
			int meta;
			if (!checked.contains(cc)&&i + lengthAdd+standartOff<dif)
			{
				b = world.getBlock(cc.posX, cc.posY, cc.posZ);
				meta = world.getBlockMetadata(cc.posX, cc.posY, cc.posZ);
				if (b.canCollideCheck(meta, false)
						&& b.collisionRayTrace(world, cc.posX, cc.posY,
								cc.posZ, pos, posNext) != null)
					ret.add(cc);
				if (place)
					world.setBlock(cc.posX, cc.posY, cc.posZ, tmp);
				checked.add(cc);
			}
			cc = new ChunkCoordinates((int) Math.floor(posPrev.xCoord),
					(int) Math.floor(posPrev.yCoord), (int) Math.floor(posPrev.zCoord));
			if (!checked.contains(cc)&&i + lengthAdd-standartOff<dif)
			{
				b = world.getBlock(cc.posX, cc.posY, cc.posZ);
				meta = world.getBlockMetadata(cc.posX, cc.posY, cc.posZ);
				if (b.canCollideCheck(meta, false)
						&& b.collisionRayTrace(world, cc.posX, cc.posY,
								cc.posZ, posVeryPrev, posPrev) != null)
					ret.add(cc);
				if (place)
					world.setBlock(cc.posX, cc.posY, cc.posZ, tmp);
				checked.add(cc);
			}
		}
	}
	public static Vec3 scalarProd(Vec3 v, double s) {
		return Vec3.createVectorHelper(v.xCoord*s, v.yCoord*s, v.zCoord*s);
	}
	public static ChunkCoordinates rayTraceForFirst(Vec3 start, Vec3 end, World w, HashSet<ChunkCoordinates> ignore)
	{
		HashSet<ChunkCoordinates> trace = rayTrace(start, end, w);
		for (ChunkCoordinates cc:ignore)
			trace.remove(cc);
		if (start.xCoord!=end.xCoord)
			trace = findMinOrMax(trace, start.xCoord>end.xCoord, 0);
		if (start.yCoord!=end.yCoord)
			trace = findMinOrMax(trace, start.yCoord>end.yCoord, 0);
		if (start.zCoord!=end.zCoord)
			trace = findMinOrMax(trace, start.zCoord>end.zCoord, 0);
		if (trace.size()>0)
		{
			ChunkCoordinates ret = trace.iterator().next();
			return ret;
		}
		return null;
	}
	public static HashSet<ChunkCoordinates> findMinOrMax(HashSet<ChunkCoordinates> in, boolean max, int coord) {
		HashSet<ChunkCoordinates> ret = new HashSet<ChunkCoordinates>();
		int currMinMax = max?Integer.MIN_VALUE:Integer.MAX_VALUE;
		//find minimum
		for (ChunkCoordinates cc:in)
		{
			int curr = (coord==0?cc.posX:(coord==1?cc.posY:cc.posZ));
			if (max^(curr<currMinMax))
				currMinMax = curr;
		}
		//fill ret set
		for (ChunkCoordinates cc:in)
		{
			int curr = (coord==0?cc.posX:(coord==1?cc.posY:cc.posZ));
			if (curr==currMinMax)
				ret.add(cc);
		}
		return ret;
	}

	/**
	 * get tile entity without loading currently unloaded chunks
	 * @return return value of {@link net.minecraft.world.IBlockAccess#getTileEntity(int, int, int)} or always null if chunk is not loaded
	 */
	public static TileEntity getExistingTileEntity(World world, int x, int y, int z)
	{
		if(world.blockExists(x, y, z))
			return world.getTileEntity(x, y, z);
		return null;
	}
	public static ItemStack[] readInventory(NBTTagList nbt, int size)
	{
		ItemStack[] inv = new ItemStack[size];
		int max = nbt.tagCount();
		for (int i = 0;i<max;i++)
		{
			NBTTagCompound itemTag = nbt.getCompoundTagAt(i);
			int slot = itemTag.getByte("Slot") & 255;
			if(slot>=0 && slot<size)
				inv[slot] = ItemStack.loadItemStackFromNBT(itemTag);
		}
		return inv;
	}
	public static NBTTagList writeInventory(ItemStack[] inv)
	{
		NBTTagList invList = new NBTTagList();
		for(int i=0; i<inv.length; i++)
			if(inv[i] != null)
			{
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				inv[i].writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
		return invList;
	}

	public static Map<String, Object> saveStack(ItemStack stack)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if (stack!=null&&stack.getItem()!=null)
		{
			ret.put("size", stack.stackSize);
			ret.put("name", Item.itemRegistry.getNameForObject(stack.getItem()));
			ret.put("nameUnlocalized", stack.getUnlocalizedName());
			ret.put("label", stack.getDisplayName());
			ret.put("damage", stack.getItemDamage());
			ret.put("maxDamage", stack.getMaxDamage());
			ret.put("maxSize", stack.getMaxStackSize());
			ret.put("hasTag", stack.hasTagCompound());
		}
		return ret;
	}
	public static Map<String, Object> saveFluidTank(FluidTank tank)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if (tank!=null&&tank.getFluid()!=null)
		{
			ret.put("name", tank.getFluid().getFluid().getUnlocalizedName());
			ret.put("amount", tank.getFluidAmount());
			ret.put("capacity", tank.getCapacity());
			ret.put("hasTag", tank.getFluid().tag!=null);
		}
		return ret;
	}
	public static Map<String, Object> saveFluidStack(FluidStack tank)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if (tank!=null&&tank.getFluid()!=null)
		{
			ret.put("name", tank.getFluid().getUnlocalizedName());
			ret.put("amount", tank.amount);
			ret.put("hasTag", tank.tag!=null);
		}
		return ret;
	}
}