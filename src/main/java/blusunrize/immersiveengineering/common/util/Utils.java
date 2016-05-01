package blusunrize.immersiveengineering.common.util;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

public class Utils
{
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		if(!ApiUtils.isExistingOreName(oreName))
			return false;
		ItemStack comp = copyStackWithAmount(stack, 1);
		List<ItemStack> s = OreDictionary.getOres(oreName);
		for (ItemStack st:s)
			if (ItemStack.areItemStacksEqual(comp, st))
				return true;
		return false;
	}
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof ItemStack)
			return OreDictionary.itemMatches((ItemStack)o, stack, false);
		else if(o instanceof List)
		{
			for(Object io : (List)o)
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
	public static String[] dyeNames = {"Black","Red","Green","Brown","Blue","Purple","Cyan","LightGray","Gray","Pink","Lime","Yellow","LightBlue","Magenta","Orange","White"};
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
	public static boolean isDye(ItemStack stack)
	{
		if(stack==null)
			return false;
		if(stack.getItem().equals(Items.dye))
			return true;
		for(int dye=0;dye<dyeNames.length;dye++)
			if(compareToOreName(stack,"dye"+dyeNames[dye]))
				return true;
		return false;
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

	public static BlockPos toCC(Object object)
	{
		return ApiUtils.toBlockPos(object);
	}

	public static DirectionalBlockPos toDirCC(Object object, EnumFacing direction)
	{
		if(object instanceof BlockPos)
			return new DirectionalBlockPos((BlockPos)object, direction);
		if(object instanceof TileEntity)
			return new DirectionalBlockPos(((TileEntity)object).getPos(), direction);
		return null;
	}

	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof BlockPos && world!=null && world.isBlockLoaded((BlockPos)object))
		{
			TileEntity te = world.getTileEntity((BlockPos)object);
			if(te instanceof IImmersiveConnectable)
				return (IImmersiveConnectable) te;
		}
		return null;
	}

	public static boolean isBlockAt(World world, BlockPos pos, Block b, int meta)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock().equals(b))
			return meta<0||meta==OreDictionary.WILDCARD_VALUE || state.getBlock().getMetaFromState(state)==meta;
		return false;
	}
	public static boolean isOreBlockAt(World world, BlockPos pos, String oreName)
	{
		IBlockState state = world.getBlockState(pos);
		ItemStack stack = new ItemStack(state.getBlock(),1,state.getBlock().getMetaFromState(state));
		return compareToOreName(stack, oreName);
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
		return tile0.getPos().equals(tile1.getPos());
	}

	public static MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityLivingBase living, boolean bool)
	{
		float f = 1.0F;
		float f1 = living.prevRotationPitch + (living.rotationPitch - living.prevRotationPitch) * f;
		float f2 = living.prevRotationYaw + (living.rotationYaw - living.prevRotationYaw) * f;
		double d0 = living.prevPosX + (living.posX - living.prevPosX) * (double)f;
		double d1 = living.prevPosY + (living.posY - living.prevPosY) * (double)f + (double)(world.isRemote ? living.getEyeHeight() - (living instanceof EntityPlayer?((EntityPlayer)living).getDefaultEyeHeight():0) : living.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
		double d2 = living.prevPosZ + (living.posZ - living.prevPosZ) * (double)f;
		Vec3 vec3 = new Vec3(d0, d1, d2);
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
		return world.rayTraceBlocks(vec3, vec31, bool, !bool, false);
	}
	public static boolean canBlocksSeeOther(World world, BlockPos cc0, BlockPos cc1, Vec3 pos0, Vec3 pos1)
	{
		HashSet<BlockPos> inter = rayTrace(pos0, pos1, world);
		Iterator<BlockPos> it = inter.iterator();
		while (it.hasNext()) {
			BlockPos cc = it.next();
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

	public static Vec3 getFlowVector(World world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock() instanceof BlockFluidBase)
			return ((BlockFluidBase)world.getBlockState(pos).getBlock()).getFlowVector(world, pos);
		else if( !(world.getBlockState(pos).getBlock() instanceof BlockLiquid))
			return new Vec3(0, 0, 0);

		BlockLiquid block = (BlockLiquid)world.getBlockState(pos).getBlock();
		Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
		Material mat = block.getMaterial();
		int i = getEffectiveFlowDecay(world, pos, mat);

		for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
		{
			BlockPos blockpos = pos.offset(enumfacing);
			int j = getEffectiveFlowDecay(world, blockpos, mat);
			if(j<0)
			{
				if(!world.getBlockState(blockpos).getBlock().getMaterial().blocksMovement())
				{
					j = getEffectiveFlowDecay(world, blockpos.down(), mat);
					if(j>=0)
					{
						int k = j - (i - 8);
						vec3 = vec3.addVector((double)((blockpos.getX() - pos.getX()) * k), (double)((blockpos.getY() - pos.getY()) * k), (double)((blockpos.getZ() - pos.getZ()) * k));
					}
				}
			}
			else if(j>=0)
			{
				int l = j - i;
				vec3 = vec3.addVector((double)((blockpos.getX() - pos.getX()) * l), (double)((blockpos.getY() - pos.getY()) * l), (double)((blockpos.getZ() - pos.getZ()) * l));
			}
		}

		if(((Integer)world.getBlockState(pos).getValue(BlockLiquid.LEVEL)).intValue()>=8)
		{
			for(EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL)
			{
				BlockPos blockpos1 = pos.offset(enumfacing1);
				if(block.isBlockSolid(world, blockpos1, enumfacing1) || block.isBlockSolid(world, blockpos1.up(), enumfacing1))
				{
					vec3 = vec3.normalize().addVector(0.0D, -6.0D, 0.0D);
					break;
				}
			}
		}
		return vec3.normalize();
	}
	static int getEffectiveFlowDecay(IBlockAccess world, BlockPos pos, Material mat)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock().getMaterial() != mat)
			return -1;
		int l = state.getBlock().getMetaFromState(state);
		if (l >= 8)
			l = 0;
		return l;
	}
	public static Vec3 addVectors(Vec3 vec0, Vec3 vec1)
	{
		return vec0.addVector(vec1.xCoord,vec1.yCoord,vec1.zCoord);
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
			colors[i] = ItemDye.dyeColors[j];
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

	public static FluidStack drainFluidBlock(World world, BlockPos pos, boolean doDrain)
	{
		Block b = world.getBlockState(pos).getBlock();
		Fluid f = FluidRegistry.lookupFluidForBlock(b);

		if(f!=null)
		{
			if(b instanceof IFluidBlock)
			{
				if(((IFluidBlock)b).canDrain(world, pos))
					return ((IFluidBlock) b).drain(world, pos, doDrain);
				else
					return null;
			}
			else
			{
				if(b.getMetaFromState(world.getBlockState(pos))==0)
				{
					if(doDrain)
						world.setBlockToAir(pos);
					return new FluidStack(f, 1000);
				}
				return null;
			}
		}
		return null;
	}

	public static boolean placeFluidBlock(World world, BlockPos pos, FluidStack fluid)
	{
		if(fluid==null || fluid.getFluid()==null)
			return false;
		Block b = world.getBlockState(pos).getBlock();
		Block fluidBlock = fluid.getFluid().getBlock();

		if(Blocks.water.equals(fluidBlock))
			fluidBlock = Blocks.flowing_water;
		else if(Blocks.lava.equals(fluidBlock))
			fluidBlock = Blocks.flowing_lava;

		boolean canPlace = b==null||b.isAir(world,pos)||b.isReplaceable(world,pos);

		if(fluidBlock!=null && canPlace && fluid.amount>=1000)
		{
			boolean placed = false;
			if ((fluidBlock instanceof BlockFluidBase))
			{
				BlockFluidBase blockFluid = (BlockFluidBase)fluidBlock;
				placed = world.setBlockState(pos, fluidBlock.getStateFromMeta(blockFluid.getMaxRenderHeightMeta()));
			}
			else
				placed = world.setBlockState(pos, fluidBlock.getDefaultState());
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

	//	public static String nameFromStack(ItemStack stack)
	//	{
	//		if(stack==null)
	//			return "";
	//		try
	//		{
	//			return GameData.getItemRegistry().getNameForObject(stack.getItem());
	//		}
	//		catch (NullPointerException e) {}
	//		return "";
	//	}

	public static IBlockState getStateFromItemStack(ItemStack stack)
	{
		if(stack==null||stack.getItem()==null)
			return null;
		Block block = getBlockFromItem(stack.getItem());
		if(block!=null)
			return block.getStateFromMeta(stack.getItemDamage());
		return null;
	}
	public static Block getBlockFromItem(Item item)
	{
		if(item==Items.cauldron)
			return Blocks.cauldron;
		return Block.getBlockFromItem(item);
	}

	public static boolean canInsertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side)
	{
		if(inventory!=null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
			if(temp==null || temp.stackSize<stack.stackSize)
				return true;
		}
		return false;
	}
	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side)
	{
		if(inventory!=null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
			if(temp==null || temp.stackSize<stack.stackSize)
				return ItemHandlerHelper.insertItem(handler, stack, false);
		}
		return stack;
	}
	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side, boolean simulate)
	{
		if(inventory!=null && stack!=null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
		}
		return stack;
	}
	public static void dropStackAtPos(World world, BlockPos pos, ItemStack stack, EnumFacing facing)
	{
		if(stack!=null)
		{
			EntityItem ei = new EntityItem(world, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, stack.copy());
			ei.motionY = 0.025000000372529D;
			if(facing!=null)
			{
				ei.motionX = (0.075F * facing.getFrontOffsetX());
				ei.motionZ = (0.075F * facing.getFrontOffsetZ());
			}
			world.spawnEntityInWorld(ei);
		}
	}
	public static void dropStackAtPos(World world, BlockPos pos, ItemStack stack)
	{
		dropStackAtPos(world, pos, stack, null);	
	}
	//	public static ItemStack insertStackIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side)
	//	{
	//		if (stack == null || inventory == null)
	//			return null;
	//		int stackSize = stack.stackSize;
	//		if (inventory instanceof ISidedInventory)
	//		{
	//			ISidedInventory sidedInv = (ISidedInventory) inventory;
	//			int slots[] = sidedInv.getSlotsForFace(side);
	//			if (slots == null)
	//				return stack;
	//			for (int i=0; i<slots.length && stack!=null; i++)
	//			{
	//				if (sidedInv.canInsertItem(slots[i], stack, side))
	//				{
	//					ItemStack existingStack = inventory.getStackInSlot(slots[i]);
	//					if(OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
	//						stack = addToOccupiedSlot(sidedInv, slots[i], stack, existingStack);
	//				}
	//			}
	//			for (int i=0; i<slots.length && stack!=null; i++)
	//				if (inventory.getStackInSlot(slots[i]) == null && sidedInv.canInsertItem(slots[i], stack, side))
	//					stack = addToEmptyInventorySlot(sidedInv, slots[i], stack);
	//		}
	//		else
	//		{
	//			int invSize = inventory.getSizeInventory();
	//			for (int i=0; i<invSize && stack!=null; i++)
	//			{
	//				ItemStack existingStack = inventory.getStackInSlot(i);
	//				if (OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
	//					stack = addToOccupiedSlot(inventory, i, stack, existingStack);
	//			}
	//			for (int i=0; i<invSize && stack!=null; i++)
	//				if (inventory.getStackInSlot(i) == null)
	//					stack = addToEmptyInventorySlot(inventory, i, stack);
	//		}
	//		if (stack == null || stack.stackSize != stackSize)
	//			inventory.markDirty();
	//		return stack;
	//	}

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


//	public static boolean canInsertStackIntoInventory(IInventory inventory, ItemStack stack, EnumFacing side)
//	{
//		if(stack == null || inventory == null)
//			return false;
//		if(inventory instanceof ISidedInventory)
//		{
//			ISidedInventory sidedInv = (ISidedInventory) inventory;
//			int slots[] = sidedInv.getSlotsForFace(side);
//			if(slots == null)
//				return false;
//			for(int i=0; i<slots.length && stack!=null; i++)
//			{
//				if(sidedInv.canInsertItem(slots[i], stack, side) && sidedInv.isItemValidForSlot(slots[i], stack))
//				{
//					ItemStack existingStack = inventory.getStackInSlot(slots[i]);
//					if(existingStack==null)
//						return true;
//					else
//						if(OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
//							if(existingStack.stackSize+stack.stackSize<inventory.getInventoryStackLimit() && existingStack.stackSize+stack.stackSize<existingStack.getMaxStackSize())
//								return true;
//				}
//			}
//		}
//		else
//		{
//			int invSize = inventory.getSizeInventory();
//			for(int i=0; i<invSize && stack!=null; i++)
//				if(inventory.isItemValidForSlot(i, stack))
//				{
//					ItemStack existingStack = inventory.getStackInSlot(i);
//					if(existingStack==null)
//						return true;
//					else
//						if(OreDictionary.itemMatches(existingStack, stack, true)&&ItemStack.areItemStackTagsEqual(stack, existingStack))
//							if(existingStack.stackSize+stack.stackSize<inventory.getInventoryStackLimit() && existingStack.stackSize+stack.stackSize<existingStack.getMaxStackSize())
//								return true;
//				}
//		}
//		return false;
//	}

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
						player.dropItem(filledStack, false, true);
					player.openContainer.detectAndSendChanges();
					((EntityPlayerMP) player).updateCraftingInventory(player.openContainer, player.openContainer.getInventory());
				}
			handler.drain(null, fluid.amount, true);
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
						player.dropItem(filled, false, true);
				}
				else
				{
					fill = container.fill(equipped, tankFluid, true);
				}
				handler.drain(null, fill, true);
				player.openContainer.detectAndSendChanges();
				((EntityPlayerMP) player).updateCraftingInventory(player.openContainer, player.openContainer.getInventory());
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
			if(handler.fill(null, fluid, false) == fluid.amount || player.capabilities.isCreativeMode)
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
							player.dropItem(filledStack, false, true);
					}
					player.openContainer.detectAndSendChanges();
					((EntityPlayerMP) player).updateCraftingInventory(player.openContainer, player.openContainer.getInventory());
				}
				handler.fill(null, fluid, true);
				return true;
			}
		}
		else if(equipped.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)equipped.getItem();
			fluid = container.getFluid(equipped);
			if(handler.fill(null, fluid, false)>0)
			{
				if(world.isRemote)
					return true;

				int fill = handler.fill(null, fluid, true);
				if(equipped.stackSize > 1)
				{
					ItemStack emptied = copyStackWithAmount(equipped, 1);
					equipped.stackSize -= 1;
					container.drain(emptied, fill, true);
					if(!player.inventory.addItemStackToInventory(emptied))
						player.dropItem(emptied, false, true);
				}
				else
				{
					container.drain(equipped, fill, true);
				}
				player.openContainer.detectAndSendChanges();
				((EntityPlayerMP) player).updateCraftingInventory(player.openContainer, player.openContainer.getInventory());
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

	public static HashSet<BlockPos> rayTrace(Vec3 start, Vec3 end, World world)
	{
		HashSet<BlockPos> ret = new HashSet<BlockPos>();
		HashSet<BlockPos> checked = new HashSet<BlockPos>();
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
	private static void ray(double dif, Vec3 mov, Vec3 start, double lengthAdd, HashSet<BlockPos> ret, World world, HashSet<BlockPos> checked, Block tmp)
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

			BlockPos blockPos = new BlockPos((int) Math.floor(pos.xCoord),
					(int) Math.floor(pos.yCoord), (int) Math.floor(pos.zCoord));
			Block b;
			IBlockState state;
			if (!checked.contains(blockPos)&&i + lengthAdd+standartOff<dif)
			{
				state = world.getBlockState(blockPos);
				b = state.getBlock();
				if (b.canCollideCheck(state, false) && b.collisionRayTrace(world, blockPos, pos, posNext) != null)
					ret.add(blockPos);
				//				if (place)
				//					world.setBlockState(blockPos, tmp);
				checked.add(blockPos);
			}
			blockPos = new BlockPos((int) Math.floor(posPrev.xCoord), (int) Math.floor(posPrev.yCoord), (int) Math.floor(posPrev.zCoord));
			if (!checked.contains(blockPos)&&i + lengthAdd-standartOff<dif)
			{
				state = world.getBlockState(blockPos);
				b = state.getBlock();
				if (b.canCollideCheck(state, false) && b.collisionRayTrace(world, blockPos, posVeryPrev, posPrev) != null)
					ret.add(blockPos);
				//				if (place)
				//					world.setBlock(blockPos.posX, blockPos.posY, blockPos.posZ, tmp);
				checked.add(blockPos);
			}
		}
	}
	public static Vec3 scalarProd(Vec3 v, double s)
	{
		return new Vec3(v.xCoord*s, v.yCoord*s, v.zCoord*s);
	}
	public static BlockPos rayTraceForFirst(Vec3 start, Vec3 end, World w, HashSet<BlockPos> ignore)
	{
		HashSet<BlockPos> trace = rayTrace(start, end, w);
		for (BlockPos cc:ignore)
			trace.remove(cc);
		if (start.xCoord!=end.xCoord)
			trace = findMinOrMax(trace, start.xCoord>end.xCoord, 0);
		if (start.yCoord!=end.yCoord)
			trace = findMinOrMax(trace, start.yCoord>end.yCoord, 0);
		if (start.zCoord!=end.zCoord)
			trace = findMinOrMax(trace, start.zCoord>end.zCoord, 0);
		if (trace.size()>0)
		{
			BlockPos ret = trace.iterator().next();
			return ret;
		}
		return null;
	}
	public static HashSet<BlockPos> findMinOrMax(HashSet<BlockPos> in, boolean max, int coord) {
		HashSet<BlockPos> ret = new HashSet<BlockPos>();
		int currMinMax = max?Integer.MIN_VALUE:Integer.MAX_VALUE;
		//find minimum
		for (BlockPos cc:in)
		{
			int curr = (coord==0?cc.getX():(coord==1?cc.getY():cc.getY()));
			if (max^(curr<currMinMax))
				currMinMax = curr;
		}
		//fill ret set
		for (BlockPos cc:in)
		{
			int curr = (coord==0?cc.getX():(coord==1?cc.getY():cc.getZ()));
			if (curr==currMinMax)
				ret.add(cc);
		}
		return ret;
	}

	/**
	 * get tile entity without loading currently unloaded chunks
	 * @return return value of {@link net.minecraft.world.IBlockAccess#getTileEntity(int, int, int)} or always null if chunk is not loaded
	 */
	public static TileEntity getExistingTileEntity(World world, BlockPos pos)
	{
		if(world.isBlockLoaded(pos))
			return world.getTileEntity(pos);
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

	public static void modifyInvStackSize(ItemStack[] inv, int slot, int amount)
	{
		if(slot>=0&&slot<inv.length && inv[slot]!=null)
		{
			inv[slot].stackSize += amount;
			if(inv[slot].stackSize<=0)
				inv[slot] = null;
		}
	}

	public static int calcRedstoneFromInventory(IIEInventory inv)
	{
		if(inv==null)
			return 0;
		else
		{
			int i=0;
			float f = 0.0F;
			for(int j=0; j<inv.getInventory().length; ++j)
			{
				ItemStack itemstack = inv.getInventory()[j];
				if(itemstack!=null)
				{
					f += (float)itemstack.stackSize / (float)Math.min(inv.getSlotLimit(j), itemstack.getMaxStackSize());
					++i;
				}
			}
			f = f/(float)inv.getInventory().length;
			return MathHelper.floor_float(f * 14.0F) + (i > 0 ? 1 : 0);
		}
	}

	public static void generateChestContents(Random random, List<WeightedRandomChestContent> listIn, IIEInventory inv, int max)
	{
		for(int i=0; i<max; ++i)
		{
			WeightedRandomChestContent weightedrandomchestcontent = (WeightedRandomChestContent)WeightedRandom.getRandomItem(random, listIn);
			ItemStack[] stacks = net.minecraftforge.common.ChestGenHooks.generateStacks(random, weightedrandomchestcontent.theItemId, weightedrandomchestcontent.minStackSize, weightedrandomchestcontent.maxStackSize);
			ItemStack[] inventory = inv.getInventory();
			for(ItemStack stack : stacks)
				inventory[random.nextInt(inventory.length)] = stack;
		}
	}
}