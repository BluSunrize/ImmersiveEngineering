package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.Math.min;

public class Utils
{
	public static final Random RAND = new Random();

	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		if(!ApiUtils.isExistingOreName(oreName))
			return false;
		ItemStack comp = copyStackWithAmount(stack, 1);
		List<ItemStack> s = OreDictionary.getOres(oreName);
		for(ItemStack st:s)
			if(OreDictionary.itemMatches(st, comp, false))
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
			return OreDictionary.itemMatches((ItemStack)o, stack, false) && (!checkNBT || ((ItemStack)o).getItemDamage()==OreDictionary.WILDCARD_VALUE || Utils.compareItemNBT((ItemStack)o, stack));
		else if(o instanceof Collection)
		{
			for(Object io : (Collection)o)
				if(io instanceof ItemStack && OreDictionary.itemMatches((ItemStack)io, stack, false) && (!checkNBT || ((ItemStack)io).getItemDamage()==OreDictionary.WILDCARD_VALUE || Utils.compareItemNBT((ItemStack)io, stack)))
					return true;
		} else if(o instanceof IngredientStack)
			return ((IngredientStack)o).matchesItemStack(stack);
		else if(o instanceof ItemStack[])
		{
			for(ItemStack io : (ItemStack[])o)
				if(OreDictionary.itemMatches(io, stack, false) && (!checkNBT || io.getItemDamage()==OreDictionary.WILDCARD_VALUE || Utils.compareItemNBT(io, stack)))
					return true;
		} else if(o instanceof FluidStack)
		{
			FluidStack fs = FluidUtil.getFluidContained(stack);
			return fs != null && fs.containsFluid((FluidStack)o);
		}
		else if(o instanceof String)
			return compareToOreName(stack, (String)o);
		return false;
	}
	public static boolean compareItemNBT(ItemStack stack1, ItemStack stack2)
	{
		if((stack1.isEmpty()) != (stack2.isEmpty()))
			return false;
		boolean empty1 = (stack1.getTagCompound()==null||stack1.getTagCompound().hasNoTags());
		boolean empty2 = (stack2.getTagCompound()==null||stack2.getTagCompound().hasNoTags());
		if(empty1!=empty2)
			return false;
		if(!empty1 && !stack1.getTagCompound().equals(stack2.getTagCompound()))
			return false;
		return stack1.areCapsCompatible(stack2);
	}

	public static boolean canCombineArrays(ItemStack[] stacks, ItemStack[] target)
	{
		HashSet<IngredientStack> inputSet = new HashSet();
		for(ItemStack s : stacks)
			inputSet.add(new IngredientStack(s));
		for(ItemStack t : target)
		{
			int size = t.getCount();
			Iterator<IngredientStack> it = inputSet.iterator();
			while(it.hasNext())
			{
				IngredientStack in = it.next();
				if(in.matchesItemStackIgnoringSize(t))
				{
					int taken = Math.min(size, in.inputSize);
					size -= taken;
					in.inputSize -= taken;
					if(in.inputSize<=0)
						it.remove();
					if(size<=0)
						break;
				}
			}
			if(size>0)
				return false;
		}
		return true;
	}
	public static ItemStack copyStackWithAmount(ItemStack stack, int amount)
	{
		if(stack.isEmpty())
			return ItemStack.EMPTY;
		ItemStack s2 = stack.copy();
		s2.setCount(amount);
		return s2;
	}
	public static String[] dyeNames = {"Black","Red","Green","Brown","Blue","Purple","Cyan","LightGray","Gray","Pink","Lime","Yellow","LightBlue","Magenta","Orange","White"};
	public static int getDye(ItemStack stack)
	{
		if(stack.isEmpty())
			return -1;
		if(stack.getItem().equals(Items.DYE))
			return stack.getItemDamage();
		for(int dye=0;dye<dyeNames.length;dye++)
			if(compareToOreName(stack,"dye"+dyeNames[dye]))
				return dye;
		return -1;
	}
	public static boolean isDye(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		if(stack.getItem().equals(Items.DYE))
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

	static long UUIDBase = 109406000905L;
	static long UUIDAdd = 01L;
	public static UUID generateNewUUID()
	{
		UUID uuid = new UUID(UUIDBase,UUIDAdd);
		UUIDAdd++;
		return uuid;
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

	public static boolean isBlockAt(World world, BlockPos pos, Block b, int meta)
	{
		return blockstateMatches(world.getBlockState(pos), b, meta);
	}
	public static boolean blockstateMatches(IBlockState state, Block b, int meta)
	{
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

	public static boolean canFenceConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing, Material blockMaterial)
	{
		BlockPos other = pos.offset(facing);
		IBlockState state = world.getBlockState(other);
		Block block = world.getBlockState(other).getBlock();
		if(block.canBeConnectedTo(world, other, facing.getOpposite()))
			return true;
		BlockFaceShape blockfaceshape = state.getBlockFaceShape(world, other, facing.getOpposite());
		boolean flag = blockfaceshape == BlockFaceShape.MIDDLE_POLE && (state.getMaterial()==blockMaterial || block instanceof BlockFenceGate);
		return !isExceptBlockForAttachWithFence(block) && blockfaceshape == BlockFaceShape.SOLID || flag;
	}

	private static boolean isExceptionBlockForAttaching(Block block)
	{
		return block instanceof BlockShulkerBox|| block instanceof BlockLeaves|| block instanceof BlockTrapDoor || block == Blocks.BEACON || block == Blocks.CAULDRON || block == Blocks.GLASS || block == Blocks.GLOWSTONE || block == Blocks.ICE || block == Blocks.SEA_LANTERN || block == Blocks.STAINED_GLASS;
	}
	private static boolean isExceptBlockForAttachWithPiston(Block block)
	{
		return isExceptionBlockForAttaching(block) || block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD;
	}
	private static boolean isExceptBlockForAttachWithFence(Block block)
	{
		return isExceptBlockForAttachWithPiston(block) || block == Blocks.BARRIER || block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN || block == Blocks.LIT_PUMPKIN;
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
		return s.substring(0,1).toUpperCase(Locale.ENGLISH) + s.substring(1).toLowerCase(Locale.ENGLISH);
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
		return I18n.format(Lib.DESC_INFO+"mininglvl."+Math.max(-1, Math.min(lvl, 6)));
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

	public static EnumFacing rotateFacingTowardsDir(EnumFacing f, EnumFacing dir)
	{
		if(dir==EnumFacing.NORTH)
			return f;
		else if(dir==EnumFacing.SOUTH && f.getAxis()!=Axis.Y)
			return f.rotateY().rotateY();
		else if(dir==EnumFacing.WEST && f.getAxis()!=Axis.Y)
			return f.rotateYCCW();
		else if(dir==EnumFacing.EAST && f.getAxis()!=Axis.Y)
			return f.rotateY();
		else if(dir==EnumFacing.DOWN && f.getAxis()!=Axis.Y)
			return f.rotateAround(Axis.X);
		else if(dir==EnumFacing.UP && f.getAxis()!=Axis.X)
			return f.rotateAround(Axis.X).getOpposite();
		return f;
	}

	public static RayTraceResult getMovingObjectPositionFromPlayer(World world, EntityLivingBase living, boolean bool)
	{
		float f = 1.0F;
		float f1 = living.prevRotationPitch + (living.rotationPitch - living.prevRotationPitch) * f;
		float f2 = living.prevRotationYaw + (living.rotationYaw - living.prevRotationYaw) * f;
		double d0 = living.prevPosX + (living.posX - living.prevPosX) * (double)f;
		double d1 = living.prevPosY + (living.posY - living.prevPosY) * (double)f + (double)(world.isRemote ? living.getEyeHeight() - (living instanceof EntityPlayer?((EntityPlayer)living).getDefaultEyeHeight():0) : living.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
		double d2 = living.prevPosZ + (living.posZ - living.prevPosZ) * (double)f;
		Vec3d vec3 = new Vec3d(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if (living instanceof EntityPlayerMP)
			d3 = ((EntityPlayerMP)living).interactionManager.getBlockReachDistance();

		Vec3d vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
		return world.rayTraceBlocks(vec3, vec31, bool, !bool, false);
	}
	public static boolean canBlocksSeeOther(World world, BlockPos cc0, BlockPos cc1, Vec3d pos0, Vec3d pos1)
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

	public static List<EntityLivingBase> getTargetsInCone(World world, Vec3d start, Vec3d dir, float spreadAngle, float truncationLength)
	{
		double length = dir.lengthVector();
		Vec3d dirNorm = dir.normalize();
		double radius = Math.tan(spreadAngle/2)*length;

		Vec3d endLow = start.add(dir).subtract(radius,radius,radius);
		Vec3d endHigh = start.add(dir).addVector(radius,radius,radius);

		AxisAlignedBB box = new AxisAlignedBB(minInArray(start.x,endLow.x,endHigh.x), minInArray(start.y,endLow.y,endHigh.y), minInArray(start.z,endLow.z,endHigh.z),
				maxInArray(start.x,endLow.x,endHigh.x), maxInArray(start.y,endLow.y,endHigh.y), maxInArray(start.z,endLow.z,endHigh.z));

		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		Iterator<EntityLivingBase> iterator = list.iterator();
		while(iterator.hasNext())
		{
			EntityLivingBase e = iterator.next();
			if(!isPointInCone(start, dirNorm, radius, length, truncationLength, e.getPositionVector().subtract(start)))
				iterator.remove();
		}
		return list;
	}

	public static boolean isPointInConeByAngle(Vec3d start, Vec3d normDirection, double aperture, double length, Vec3d relativePoint)
	{
		return isPointInCone(start, normDirection, Math.tan(aperture/2)*length, length, 0, relativePoint);
	}
	public static boolean isPointInCone(Vec3d start, Vec3d normDirection, double radius, double length, Vec3d relativePoint)
	{
		return isPointInCone(start, normDirection, radius, length, 0, relativePoint);
	}
	public static boolean isPointInConeByAngle(Vec3d start, Vec3d normDirection, float aperture, double length, float truncationLength, Vec3d relativePoint)
	{
		return isPointInCone(start, normDirection, Math.tan(aperture/2)*length, length, truncationLength, relativePoint);
	}
	/**
	 * Checks if  point is contained within a cone in 3D space
	 *
	 * @param start tip of the cone
	 * @param normDirection normalized (length==1) vector, direction of cone
	 * @param radius radius at the end of the cone
	 * @param length length of the cone
	 * @param truncationLength optional lenght at which the cone is truncated (flat tip)
	 * @param relativePoint point to be checked, relative to {@code start}
	 */
	public static boolean isPointInCone(Vec3d start, Vec3d normDirection, double radius, double length, float truncationLength, Vec3d relativePoint)
	{
		double projectedDist = relativePoint.dotProduct(normDirection); //Orthogonal projection, establishing point's distance on cone direction vector
		if(projectedDist<truncationLength || projectedDist>length) //If projected distance is before truncation or beyond length, point not contained
			return false;

		double radiusAtDist = projectedDist/length * radius; //Radius of the cone at the projected distance
		Vec3d orthVec = relativePoint.subtract(normDirection.scale(projectedDist)); //Orthogonal vector between point and cone direction

		return orthVec.lengthSquared()<(radiusAtDist*radiusAtDist); //Check if Vector's length is shorter than radius -> point in cone
	}
	public static boolean isPointInTriangle(Vec3d tA, Vec3d tB, Vec3d tC, Vec3d point)
	{
		//Distance vectors to A (focuspoint of triangle)
		Vec3d v0 = tC.subtract(tA);
		Vec3d v1 = tB.subtract(tA);
		Vec3d v2 = point.subtract(tA);

		return isPointInTriangle(v0, v1, v2);
	}
	private static boolean isPointInTriangle(Vec3d leg0, Vec3d leg1, Vec3d targetVec)
	{
		//Dot products
		double dot00 = leg0.dotProduct(leg0);
		double dot01 = leg0.dotProduct(leg1);
		double dot02 = leg0.dotProduct(targetVec);
		double dot11 = leg1.dotProduct(leg1);
		double dot12 = leg1.dotProduct(targetVec);

		//Barycentric coordinates
		double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		return (u >= 0) && (v >= 0) && (u + v < 1);
	}
	private static Vec3d getVectorForRotation(float pitch, float yaw)
	{
		float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
		float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
		float f2 = -MathHelper.cos(-pitch * 0.017453292F);
		float f3 = MathHelper.sin(-pitch * 0.017453292F);
		return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
	}

	public static boolean isHammer(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		return stack.getItem().getToolClasses(stack).contains(Lib.TOOL_HAMMER);
	}

	public static boolean canBlockDamageSource(EntityLivingBase entity, DamageSource damageSourceIn)
	{
		if(!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking())
		{
			Vec3d vec3d = damageSourceIn.getDamageLocation();
			if(vec3d != null)
			{
				Vec3d vec3d1 = entity.getLook(1.0F);
				Vec3d vec3d2 = vec3d.subtractReverse(entity.getPositionVector()).normalize();
				vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);
				if(vec3d2.dotProduct(vec3d1) < 0)
					return true;
			}
		}
		return false;
	}

	public static Vec3d getFlowVector(World world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof BlockFluidBase)
			return ((BlockFluidBase)state.getBlock()).getFlowVector(world, pos);
		else if( !(state.getBlock() instanceof BlockLiquid))
			return new Vec3d(0, 0, 0);

		BlockLiquid block = (BlockLiquid)state.getBlock();
		Vec3d vec3 = new Vec3d(0.0D, 0.0D, 0.0D);
		Material mat = state.getMaterial();
		int i = getEffectiveFlowDecay(world, pos, mat);

		for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL)
		{
			BlockPos blockpos = pos.offset(enumfacing);
			int j = getEffectiveFlowDecay(world, blockpos, mat);
			if(j<0)
			{
				if(!world.getBlockState(blockpos).getMaterial().blocksMovement())
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

		if(state.getValue(BlockLiquid.LEVEL).intValue()>=8)
		{
			for(EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL)
			{
				BlockPos blockpos1 = pos.offset(enumfacing1);
				if(block.causesDownwardCurrent(world, blockpos1, enumfacing1) || block.causesDownwardCurrent(world, blockpos1.up(), enumfacing1))
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
		if(state.getMaterial() != mat)
			return -1;
		int l = state.getBlock().getMetaFromState(state);
		if (l >= 8)
			l = 0;
		return l;
	}
	public static Vec3d addVectors(Vec3d vec0, Vec3d vec1)
	{
		return vec0.addVector(vec1.x,vec1.y,vec1.z);
	}

	public static double minInArray(double... f)
	{
		if(f.length<1)
			return 0;
		double min = f[0];
		for(int i=1; i<f.length; i++)
			min = Math.min(min, f[i]);
		return min;
	}
	public static double maxInArray(double... f)
	{
		if(f.length<1)
			return 0;
		double max = f[0];
		for(int i=1; i<f.length; i++)
			max = Math.max(max, f[i]);
		return max;
	}

	public static boolean isVecInEntityHead(EntityLivingBase entity, Vec3d vec)
	{
		if(entity.height/entity.width<2)//Crude check to see if the entity is bipedal or at least upright (this should work for blazes)
			return false;
		double d = vec.y-(entity.posY+entity.getEyeHeight());
		return Math.abs(d) < .25;
	}

	public static void unlockIEAdvancement(EntityPlayer player, String name)
	{
		if(player instanceof EntityPlayerMP)
		{
			PlayerAdvancements advancements = ((EntityPlayerMP)player).getAdvancements();
			AdvancementManager manager = ((WorldServer)player.getEntityWorld()).getAdvancementManager();
			Advancement advancement = manager.getAdvancement(new ResourceLocation(ImmersiveEngineering.MODID, name));
			if(advancement!=null)
				advancements.grantCriterion(advancement, "code_trigger");
		}
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
			colors[i] = ItemDye.DYE_COLORS[j];
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

	public static Fluid getRelatedFluid(World w, BlockPos pos)
	{
		Block b = w.getBlockState(pos).getBlock();
		return FluidRegistry.lookupFluidForBlock(b);
	}

	public static boolean placeFluidBlock(World world, BlockPos pos, FluidStack fluid)
	{
		if(fluid==null || fluid.getFluid()==null)
			return false;
		IBlockState state = world.getBlockState(pos);
		Block b = state.getBlock();
		Block fluidBlock = fluid.getFluid().getBlock();

		if(Blocks.WATER.equals(fluidBlock))
			fluidBlock = Blocks.FLOWING_WATER;
		else if(Blocks.LAVA.equals(fluidBlock))
			fluidBlock = Blocks.FLOWING_LAVA;

		boolean canPlace = b==null||b.isAir(state,world,pos)||b.isReplaceable(world,pos);

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

//	public static Collection<ItemStack> getContainersFilledWith(FluidStack fluidStack)
//	{
//		List<ItemStack> containers = new ArrayList();
//		for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData())
//			if(data.fluid.containsFluid(fluidStack))
//				containers.add(data.filledContainer);
//		return containers;
//	}

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
		if(stack.isEmpty())
			return null;
		Block block = getBlockFromItem(stack.getItem());
		if(block!=null)
			return block.getStateFromMeta(stack.getItemDamage());
		return null;
	}
	public static Block getBlockFromItem(Item item)
	{
		if(item==Items.CAULDRON)
			return Blocks.CAULDRON;
		return Block.getBlockFromItem(item);
	}

	public static boolean canInsertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side)
	{
		if(!stack.isEmpty() && inventory!=null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
			if(temp.isEmpty() || temp.getCount() < stack.getCount())
				return true;
		}
		return false;
	}
	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side)
	{
		if(!stack.isEmpty() && inventory!=null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
			if(temp.isEmpty() || temp.getCount() < stack.getCount())
				return ItemHandlerHelper.insertItem(handler, stack, false);
		}
		return stack;
	}
	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side, boolean simulate)
	{
		if(inventory!=null && !stack.isEmpty() && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
		}
		return stack;
	}
	public static void dropStackAtPos(World world, BlockPos pos, ItemStack stack, EnumFacing facing)
	{
		if(!stack.isEmpty())
		{
			EntityItem ei = new EntityItem(world, pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5, stack.copy());
			ei.motionY = 0.025000000372529D;
			if(facing!=null)
			{
				ei.motionX = (0.075F * facing.getFrontOffsetX());
				ei.motionZ = (0.075F * facing.getFrontOffsetZ());
			}
			world.spawnEntity(ei);
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
	//					if(OreDictionary.itemMatches(existingStack, stack, true)&&Utils.compareItemNBT(stack, existingStack))
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
	//				if (OreDictionary.itemMatches(existingStack, stack, true)&&Utils.compareItemNBT(stack, existingStack))
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
		inventory.setInventorySlotContents(slot, copyStackWithAmount(stack, Math.min(stack.getCount(), stackLimit)));
		return stackLimit >= stack.getCount() ? ItemStack.EMPTY : stack.splitStack(stack.getCount() - stackLimit);
	}
	public static ItemStack addToOccupiedSlot(IInventory inventory, int slot, ItemStack stack, ItemStack existingStack)
	{
		int stackLimit = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
		if (stack.getCount() + existingStack.getCount() > stackLimit) {
			int stackDiff = stackLimit - existingStack.getCount();
			existingStack.setCount(stackLimit);
			stack.shrink(stackDiff);
			inventory.setInventorySlotContents(slot, existingStack);
			return stack;
		}
		existingStack.grow(min(stack.getCount(), stackLimit));
		inventory.setInventorySlotContents(slot, existingStack);
		return stackLimit >= stack.getCount() ? ItemStack.EMPTY : stack.splitStack(stack.getCount() - stackLimit);
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
	//						if(OreDictionary.itemMatches(existingStack, stack, true)&&Utils.compareItemNBT(stack, existingStack))
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
	//						if(OreDictionary.itemMatches(existingStack, stack, true)&&Utils.compareItemNBT(stack, existingStack))
	//							if(existingStack.stackSize+stack.stackSize<inventory.getInventoryStackLimit() && existingStack.stackSize+stack.stackSize<existingStack.getMaxStackSize())
	//								return true;
	//				}
	//		}
	//		return false;
	//	}

	public static ItemStack fillFluidContainer(IFluidHandler handler, ItemStack containerIn, ItemStack containerOut, @Nullable EntityPlayer player)
	{
		if(containerIn==null || containerIn.isEmpty())
			return ItemStack.EMPTY;
		if(containerIn.hasTagCompound() && containerIn.getTagCompound().hasNoTags())
			containerIn.setTagCompound(null);

		FluidActionResult result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, false);
		if(result.isSuccess())
		{
			final ItemStack full = result.getResult();
			if((containerOut.isEmpty() || OreDictionary.itemMatches(containerOut, full,true)))
			{
				if (!containerOut.isEmpty() && containerOut.getCount() + full.getCount() > containerOut.getMaxStackSize())
					return ItemStack.EMPTY;
				result = FluidUtil.tryFillContainer(containerIn, handler, Integer.MAX_VALUE, player, true);
				if (result.isSuccess()) {
					return result.getResult();
				}
			}
		}
		return ItemStack.EMPTY;
	}
	public static ItemStack drainFluidContainer(IFluidHandler handler, ItemStack containerIn, ItemStack containerOut, @Nullable EntityPlayer player)
	{
		if(containerIn==null || containerIn.isEmpty())
			return ItemStack.EMPTY;

		if(containerIn.hasTagCompound() && containerIn.getTagCompound().hasNoTags())
			containerIn.setTagCompound(null);

		FluidActionResult result = FluidUtil.tryEmptyContainer(containerIn, handler, Integer.MAX_VALUE, player, false);
		if(result.isSuccess())
		{
			ItemStack empty = result.getResult();
			if ((containerOut.isEmpty() || OreDictionary.itemMatches(containerOut,empty,true)))
			{
				if (!containerOut.isEmpty() && containerOut.getCount() + empty.getCount() > containerOut.getMaxStackSize())
					return ItemStack.EMPTY;
				result = FluidUtil.tryEmptyContainer(containerIn, handler, Integer.MAX_VALUE, player, true);
				if (result.isSuccess()) {
					return result.getResult();
				}
			}
		}
		return ItemStack.EMPTY;

	}

//	public static FluidStack getFluidFromItemStack(ItemStack stack)
//	{
//		if(stack==null)
//			return null;
//		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
//		if(fluid != null)
//			return fluid;
//		else if(stack.getItem() instanceof IFluidContainerItem)
//			return ((IFluidContainerItem)stack.getItem()).getFluid(stack);
//		return null;
//	}

	public static boolean isFluidRelatedItemStack(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
	}

	public static IRecipe findRecipe(InventoryCrafting crafting, World world)
	{
		return CraftingManager.findMatchingRecipe(crafting, world);
	}

	public static NonNullList<ItemStack> createNonNullItemStackListFromArray(ItemStack[] stacks)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
		for (int i = 0; i < stacks.length; i++)
		{
			list.set(i, stacks[i]);
		}
		return list;
	}

	public static NonNullList<ItemStack> createNonNullItemStackListFromItemStack(ItemStack stack)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(1, ItemStack.EMPTY);
		list.set(0, stack);
		return list;
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

		public static InventoryCrafting createFilledCraftingInventory(int w, int h, NonNullList<ItemStack> stacks)
		{
			InventoryCrafting invC = new Utils.InventoryCraftingFalse(w, h);
			for(int j = 0; j < w * h; j++)
				if(!stacks.get(j).isEmpty())
					invC.setInventorySlotContents(j, stacks.get(j).copy());
			return invC;
		}
	}

	public static HashSet<BlockPos> rayTrace(Vec3d start, Vec3d end, World world)
	{
		HashSet<BlockPos> ret = new HashSet<BlockPos>();
		HashSet<BlockPos> checked = new HashSet<BlockPos>();
		// x
		if (start.x>end.x)
		{
			Vec3d tmp = start;
			start = end;
			end = tmp;
		}
		double min = start.x;
		double dif =end.x-min;
		double lengthAdd = Math.ceil(min)-start.x;
		Vec3d mov = start.subtract(end);
		if (mov.x!=0)
		{
			mov = scalarProd(mov, 1 / mov.x);
			ray(dif, mov, start, lengthAdd, ret, world, checked, Blocks.DIAMOND_ORE);
		}
		// y
		if (mov.y!=0)
		{
			if (start.y>end.y)
			{
				Vec3d tmp = start;
				start = end;
				end = tmp;
			}
			min = start.y;
			dif = end.y-min;
			lengthAdd = Math.ceil(min)-start.y;
			mov = start.subtract(end);
			mov = scalarProd(mov, 1/mov.y);

			ray(dif, mov, start, lengthAdd, ret, world, checked, Blocks.IRON_ORE);
		}

		// z
		if (mov.z!=0)
		{
			if (start.z>end.z)
			{
				Vec3d tmp = start;
				start = end;
				end = tmp;
			}
			min = start.z;
			dif = end.z - min;
			lengthAdd = Math.ceil(min)-start.z;
			mov = start.subtract(end);
			mov = scalarProd(mov, 1 / mov.z);

			ray(dif, mov, start, lengthAdd, ret, world, checked, Blocks.GOLD_ORE);
		}
		return ret;
	}
	private static void ray(double dif, Vec3d mov, Vec3d start, double lengthAdd, HashSet<BlockPos> ret, World world, HashSet<BlockPos> checked, Block tmp)
	{
		//Do NOT set this to true unless for debugging. Causes blocks to be placed along the traced ray
		boolean place = false;
		double standartOff = .0625;
		for (int i = 0; i < dif; i++)
		{
			Vec3d pos = addVectors(start, scalarProd(mov, i + lengthAdd+standartOff));
			Vec3d posNext = addVectors(start,
					scalarProd(mov, i + 1 + lengthAdd+standartOff));
			Vec3d posPrev = addVectors(start,
					scalarProd(mov, i + lengthAdd-standartOff));
			Vec3d posVeryPrev = addVectors(start,
					scalarProd(mov, i - 1 + lengthAdd-standartOff));

			BlockPos blockPos = new BlockPos((int) Math.floor(pos.x),
					(int) Math.floor(pos.y), (int) Math.floor(pos.z));
			Block b;
			IBlockState state;
			if (!checked.contains(blockPos)&&i + lengthAdd+standartOff<dif)
			{
				state = world.getBlockState(blockPos);
				b = state.getBlock();
				if (b.canCollideCheck(state, false) && state.collisionRayTrace(world, blockPos, pos, posNext) != null)
					ret.add(blockPos);
				//				if (place)
				//					world.setBlockState(blockPos, tmp);
				checked.add(blockPos);
			}
			blockPos = new BlockPos((int) Math.floor(posPrev.x), (int) Math.floor(posPrev.y), (int) Math.floor(posPrev.z));
			if (!checked.contains(blockPos)&&i + lengthAdd-standartOff<dif)
			{
				state = world.getBlockState(blockPos);
				b = state.getBlock();
				if (b.canCollideCheck(state, false) && state.collisionRayTrace(world, blockPos, posVeryPrev, posPrev) != null)
					ret.add(blockPos);
				//				if (place)
				//					world.setBlock(blockPos.posX, blockPos.posY, blockPos.posZ, tmp);
				checked.add(blockPos);
			}
		}
	}
	public static Vec3d scalarProd(Vec3d v, double s)
	{
		return new Vec3d(v.x*s, v.y*s, v.z*s);
	}
	public static BlockPos rayTraceForFirst(Vec3d start, Vec3d end, World w, Set<BlockPos> ignore)
	{
		HashSet<BlockPos> trace = rayTrace(start, end, w);
		for (BlockPos cc:ignore)
			trace.remove(cc);
		if (start.x!=end.x)
			trace = findMinOrMax(trace, start.x>end.x, 0);
		if (start.y!=end.y)
			trace = findMinOrMax(trace, start.y>end.y, 0);
		if (start.z!=end.z)
			trace = findMinOrMax(trace, start.z>end.z, 0);
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
	 * @return return value of {@link IBlockAccess#getTileEntity(BlockPos)} or always null if chunk is not loaded
	 */
	public static TileEntity getExistingTileEntity(World world, BlockPos pos)
	{
		if(world.isBlockLoaded(pos))
			return world.getTileEntity(pos);
		return null;
	}
	public static NonNullList<ItemStack> readInventory(NBTTagList nbt, int size)
	{
		NonNullList<ItemStack> inv = NonNullList.withSize(size, ItemStack.EMPTY);
		int max = nbt.tagCount();
		for (int i = 0;i<max;i++)
		{
			NBTTagCompound itemTag = nbt.getCompoundTagAt(i);
			int slot = itemTag.getByte("Slot") & 255;
			if(slot>=0 && slot<size)
				inv.set(slot, new ItemStack(itemTag));
		}
		return inv;
	}
	public static NBTTagList writeInventory(ItemStack[] inv)
	{
		NBTTagList invList = new NBTTagList();
		for(int i=0; i<inv.length; i++)
			if(!inv[i].isEmpty())
			{
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				inv[i].writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
		return invList;
	}
	public static NBTTagList writeInventory(Collection<ItemStack> inv)
	{
		NBTTagList invList = new NBTTagList();
		byte slot = 0;
		for(ItemStack s : inv)
		{
			if(!s.isEmpty())
			{
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", slot);
				s.writeToNBT(itemTag);
				invList.appendTag(itemTag);
			}
			slot++;
		}
		return invList;
	}
	public static NonNullList<ItemStack> loadItemStacksFromNBT(NBTBase nbt)
	{
		NonNullList<ItemStack> itemStacks = NonNullList.create();
		if(nbt instanceof NBTTagCompound)
		{
			ItemStack stack = new ItemStack((NBTTagCompound)nbt);
			itemStacks.add(stack);
			return itemStacks;
		}
		else if(nbt instanceof NBTTagList)
		{
			NBTTagList list = (NBTTagList)nbt;
			return readInventory(list, list.tagCount());
		}
		return itemStacks;
	}

	public static void modifyInvStackSize(NonNullList<ItemStack> inv, int slot, int amount)
	{
		if(slot>=0&&slot< inv.size() && !inv.get(slot).isEmpty())
		{
			inv.get(slot).grow(amount);
			if(inv.get(slot).getCount() <= 0)
				inv.set(slot, ItemStack.EMPTY);
		}
	}

	public static void shuffleLootItems(List<ItemStack> stacks, int slotAmount, Random rand)
	{
		List<ItemStack> list = Lists.newArrayList();
		Iterator<ItemStack> iterator = stacks.iterator();
		while(iterator.hasNext())
		{
			ItemStack itemstack = iterator.next();
			if(itemstack.getCount() <= 0)
				iterator.remove();
			else if(itemstack.getCount() > 1)
			{
				list.add(itemstack);
				iterator.remove();
			}
		}
		slotAmount = slotAmount - stacks.size();
		while(slotAmount>0 && list.size()>0)
		{
			ItemStack itemstack2 = list.remove(MathHelper.getInt(rand, 0, list.size() - 1));
			int i = MathHelper.getInt(rand, 1, itemstack2.getCount() / 2);
			itemstack2.shrink(i);
			ItemStack itemstack1 = itemstack2.copy();
			itemstack1.setCount(i);

			if(itemstack2.getCount() > 1 && rand.nextBoolean())
				list.add(itemstack2);
			else
				stacks.add(itemstack2);

			if(itemstack1.getCount() > 1 && rand.nextBoolean())
				list.add(itemstack1);
			else
				stacks.add(itemstack1);
		}
		stacks.addAll(list);
		Collections.shuffle(stacks, rand);
	}
	private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer()).registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer()).registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();
	public static LootTable loadBuiltinLootTable(ResourceLocation resource, LootTableManager lootTableManager)
	{
		URL url = Utils.class.getResource("/assets/" + resource.getResourceDomain() + "/loot_tables/" + resource.getResourcePath() + ".json");
		if(url==null)
			return LootTable.EMPTY_LOOT_TABLE;
		else
		{
			String s;
			try
			{
				s = Resources.toString(url, Charsets.UTF_8);
			} catch(IOException ioexception)
			{
//				IELogger.warn(("Failed to load loot table " + resource.toString() + " from " + url.toString()));
				ioexception.printStackTrace();
				return LootTable.EMPTY_LOOT_TABLE;
			}

			try
			{
				return net.minecraftforge.common.ForgeHooks.loadLootTable(GSON_INSTANCE, resource, s, false, lootTableManager);
			} catch(JsonParseException jsonparseexception)
			{
//				IELogger.error(("Failed to load loot table " + resource.toString() + " from " + url.toString()));
				jsonparseexception.printStackTrace();
				return LootTable.EMPTY_LOOT_TABLE;
			}
		}
	}

	public static int calcRedstoneFromInventory(IIEInventory inv)
	{
		if(inv==null)
			return 0;
		else
		{
			int max = inv.getComparatedSize();
			int i=0;
			float f = 0.0F;
			for(int j = 0; j< max; ++j)
			{
				ItemStack itemstack = inv.getInventory().get(j);
				if(!itemstack.isEmpty())
				{
					f += (float) itemstack.getCount() / (float)Math.min(inv.getSlotLimit(j), itemstack.getMaxStackSize());
					++i;
				}
			}
			f = f/(float) max;
			return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
		}
	}


	public static Map<String, Object> saveStack(ItemStack stack)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if(!stack.isEmpty())
		{
			ret.put("size", stack.getCount());
			ret.put("name", Item.REGISTRY.getNameForObject(stack.getItem()));
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
		if(tank != null && tank.getFluid() != null)
		{
			ret.put("name", tank.getFluid().getFluid().getUnlocalizedName());
			ret.put("amount", tank.getFluidAmount());
			ret.put("capacity", tank.getCapacity());
			ret.put("hasTag", tank.getFluid().tag != null);
		}
		return ret;
	}

	public static Map<String, Object> saveFluidStack(FluidStack tank)
	{
		HashMap<String, Object> ret = new HashMap<>();
		if(tank != null && tank.getFluid() != null)
		{
			ret.put("name", tank.getFluid().getUnlocalizedName());
			ret.put("amount", tank.amount);
			ret.put("hasTag", tank.tag != null);
		}
		return ret;
	}
}