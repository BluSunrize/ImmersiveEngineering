/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.wires.IICProxy;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection.vertices;

public class ApiUtils
{
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		if(!isExistingOreName(oreName))
			return false;
		ItemStack comp = copyStackWithAmount(stack, 1);
		List<ItemStack> s = OreDictionary.getOres(oreName);
		for(ItemStack st:s)
			if(OreDictionary.itemMatches(st, stack, false))
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
			return OreDictionary.itemMatches((ItemStack)o, stack, false) && (!checkNBT || ((ItemStack)o).getItemDamage() == OreDictionary.WILDCARD_VALUE || Utils.compareItemNBT((ItemStack)o, stack));
		else if(o instanceof Collection)
		{
			for(Object io : (Collection)o)
				if(io instanceof ItemStack && OreDictionary.itemMatches((ItemStack)io, stack, false) && (!checkNBT || ((ItemStack)io).getItemDamage() == OreDictionary.WILDCARD_VALUE || Utils.compareItemNBT((ItemStack)io, stack)))
					return true;
		} else if(o instanceof IngredientStack)
			return ((IngredientStack)o).matchesItemStack(stack);
		else if(o instanceof ItemStack[])
		{
			for(ItemStack io : (ItemStack[])o)
				if(OreDictionary.itemMatches(io, stack, false) && (!checkNBT || io.getItemDamage() == OreDictionary.WILDCARD_VALUE || Utils.compareItemNBT(io, stack)))
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
	public static ItemStack copyStackWithAmount(ItemStack stack, int amount)
	{
		if(stack.isEmpty())
			return ItemStack.EMPTY;
		ItemStack s2 = stack.copy();
		s2.setCount(amount);
		return s2;
	}
	public static boolean stacksMatchIngredientList(List<IngredientStack> list, NonNullList<ItemStack> stacks)
	{
		ArrayList<ItemStack> queryList = new ArrayList<ItemStack>(stacks.size());
		for(ItemStack s : stacks)
			if(!s.isEmpty())
				queryList.add(s.copy());

		for(IngredientStack ingr : list)
			if(ingr!=null)
			{
				int amount = ingr.inputSize;
				Iterator<ItemStack> it = queryList.iterator();
				while(it.hasNext())
				{
					ItemStack query = it.next();
					if(!query.isEmpty())
					{
						if(ingr.matchesItemStackIgnoringSize(query))
						{
							if(query.getCount() > amount)
							{
								query.shrink(amount);
								amount=0;
							}
							else
							{
								amount-=query.getCount();
								query.setCount(0);
							}
						}
						if(query.getCount()<=0)
							it.remove();
						if(amount<=0)
							break;
					}
				}
				if(amount>0)
					return false;
			}
		return true;
	}

	public static Ingredient createIngredientFromList(List<ItemStack> list)
	{
		return Ingredient.fromStacks(list.toArray(new ItemStack[list.size()]));
	}

	public static ComparableItemStack createComparableItemStack(ItemStack stack)
	{
		return createComparableItemStack(stack, true);
	}

	public static ComparableItemStack createComparableItemStack(ItemStack stack, boolean copy)
	{
		ComparableItemStack comp = new ComparableItemStack(stack, true, copy);
		if(stack.hasTagCompound() && !stack.getTagCompound().hasNoTags())
			comp.setUseNBT(true);
		return comp;
	}

	public static boolean isExistingOreName(String name)
	{
		if(!OreDictionary.doesOreNameExist(name))
			return false;
		else
			return !OreDictionary.getOres(name).isEmpty();
	}

	public static boolean isMetalComponent(ItemStack stack, String componentType)
	{
		return getMetalComponentType(stack, componentType)!=null;
	}
	public static String getMetalComponentType(ItemStack stack, String... componentTypes)
	{
		ItemStack comp = copyStackWithAmount(stack, 1);
		for(String oreName : OreDictionary.getOreNames())//This is super ugly, but I don't want to force the latest forge ._.
			for(int iType=0; iType<componentTypes.length; iType++)
				if(oreName.startsWith(componentTypes[iType]))
				{
					List<ItemStack> s = OreDictionary.getOres(oreName);
					for(ItemStack st : s)
						if(ItemStack.areItemStacksEqual(comp, st))
							return componentTypes[iType];
				}
		return null;
	}
	public static String[] getMetalComponentTypeAndMetal(ItemStack stack, String... componentTypes)
	{
		ItemStack comp = copyStackWithAmount(stack, 1);
		for(String oreName : OreDictionary.getOreNames())//This is super ugly, but I don't want to force the latest forge ._.
			for(int iType=0; iType<componentTypes.length; iType++)
				if(oreName.startsWith(componentTypes[iType]))
				{
					List<ItemStack> s = OreDictionary.getOres(oreName);
					for(ItemStack st : s)
						if(ItemStack.areItemStacksEqual(comp, st))
							return new String[]{componentTypes[iType], oreName.substring(componentTypes[iType].length())};
				}
		return null;
	}
	public static boolean isIngot(ItemStack stack)
	{
		return isMetalComponent(stack, "ingot");
	}
	public static boolean isPlate(ItemStack stack)
	{
		return isMetalComponent(stack, "plate");
	}
	public static int getComponentIngotWorth(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[IEApi.prefixToIngotMap.size()]);
		String key = getMetalComponentType(stack, keys);
		if(key!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(key);
			if(relation!=null && relation.length>1)
			{
				double val = relation[0]/(double)relation[1];
				return (int)val;
			}
		}
		return 0;
	}
	public static ItemStack breakStackIntoIngots(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[IEApi.prefixToIngotMap.size()]);
		String[] type = getMetalComponentTypeAndMetal(stack, keys);
		if(type!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(type[0]);
			if(relation!=null && relation.length>1)
			{
				double val = relation[0]/(double)relation[1];
				return copyStackWithAmount(IEApi.getPreferredOreStack("ingot"+type[1]), (int)val);
			}
		}
		return ItemStack.EMPTY;
	}
	public static Object[] breakStackIntoPreciseIngots(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[IEApi.prefixToIngotMap.size()]);
		String[] type = getMetalComponentTypeAndMetal(stack, keys);
		if(type!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(type[0]);
			if(relation!=null && relation.length>1)
			{
				double val = relation[0]/(double)relation[1];
				return new Object[]{IEApi.getPreferredOreStack("ingot"+type[1]),val};
			}
		}
		return null;
	}

	public static boolean canInsertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side)
	{
		if(!stack.isEmpty() && inventory != null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
			return temp.isEmpty()||temp.getCount() < stack.getCount();
		}
		return false;
	}

	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, EnumFacing side)
	{
		if(!stack.isEmpty() && inventory != null && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
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
		if(inventory != null && !stack.isEmpty() && inventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
		{
			IItemHandler handler = inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
		}
		return stack;
	}

	public static BlockPos toBlockPos(Object object)
	{
		if(object instanceof BlockPos)
			return (BlockPos)object;
		if(object instanceof TileEntity)
			return ((TileEntity)object).getPos();
		if (object instanceof IICProxy)
			return ((IICProxy) object).getPos();
		return null;
	}
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		return toIIC(object, world, true);
	}


	public static IImmersiveConnectable toIIC(Object object, World world, boolean allowProxies)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof BlockPos)
		{
			if (world!=null && world.isBlockLoaded((BlockPos)object))
			{
				TileEntity te = world.getTileEntity((BlockPos)object);
				if(te instanceof IImmersiveConnectable)
					return (IImmersiveConnectable) te;
			}
			if (allowProxies)
			{
				DimensionBlockPos pos = new DimensionBlockPos((BlockPos)object, world);
				if (ImmersiveNetHandler.INSTANCE.proxies.containsKey(pos))
					return ImmersiveNetHandler.INSTANCE.proxies.get(pos);
			}
		}
		return null;
	}

	public static Vec3d getVecForIICAt(World world, BlockPos p, Connection conn)
	{
		Vec3d vStart = new Vec3d(p.getX(),p.getY(),p.getZ());
		//Force loading
		IImmersiveConnectable iicStart = toIIC(p, world, false);
		if(iicStart!=null)
			vStart = addVectors(vStart, iicStart.getConnectionOffset(conn));
		return vStart;
	}

	public static Vec3d addVectors(Vec3d vec0, Vec3d vec1)
	{
		return vec0.addVector(vec1.x,vec1.y,vec1.z);
	}

	public static double acosh(double x) {
		//See http://mathworld.wolfram.com/InverseHyperbolicCosine.html
		return Math.log(x+Math.sqrt(x+1)*Math.sqrt(x-1));
	}

	public static Vec3d[] getConnectionCatenary(Connection connection, Vec3d start, Vec3d end)
	{
		boolean vertical = connection.end.getX()==connection.start.getX() && connection.end.getZ()==connection.start.getZ();

		if(vertical)
			return new Vec3d[]{new Vec3d(start.x, start.y, start.z), new Vec3d(end.x, end.y, end.z)};

		return getConnectionCatenary(start, end, connection.cableType.getSlack(), connection);
	}

	public static Vec3d[] getConnectionCatenary(Vec3d start, Vec3d end, double slack)
	{
		return getConnectionCatenary(start, end, slack, null);
	}

	public static Vec3d[] getConnectionCatenary(Vec3d start, Vec3d end, double slack, @Nullable Connection c)
	{
		double dx = (end.x)-(start.x);
		double dy = (end.y)-(start.y);
		double dz = (end.z)-(start.z);
		double dw = Math.sqrt(dx*dx + dz*dz);
		double k = Math.sqrt(dx*dx + dy*dy + dz*dz) * slack;
		double l = 0;
		int limiter = 0;
		while(limiter<300)
		{
			limiter++;
			l += 0.01;
			if (Math.sinh(l)/l >= Math.sqrt(k*k - dy*dy)/dw)
				break;
		}
		double a = dw/2/l;
		double offsetX = (0+dw-a*Math.log((k+dy)/(k-dy)))*0.5;
		double offsetY = (dy+0-k*Math.cosh(l)/Math.sinh(l))*0.5;
		if (c!=null)
		{
			c.catOffsetX = offsetX;
			c.catOffsetY = offsetY;
			c.catA = a;
		}

		Vec3d[] vex = new Vec3d[vertices+1];

		vex[0] = new Vec3d(start.x, start.y, start.z);
		for(int i=1; i<vertices; i++)
		{
			float posRelative = i/(float)vertices;
			double x = 0 + dx * posRelative;
			double z = 0 + dz * posRelative;
			double y = a * Math.cosh((dw*posRelative-offsetX)/a)+offsetY;
			vex[i] = new Vec3d(start.x+x, start.y+y, start.z+z);
		}
		vex[vertices] = new Vec3d(end.x, end.y, end.z);

		return vex;
	}

	public static double getDim(Vec3d vec, int dim) {
		return dim==0?vec.x:(dim==1?vec.y:vec.z);
	}

	public static BlockPos offsetDim(BlockPos p, int dim, int amount) {
		return p.add(dim==0?amount:0, dim==1?amount:0, dim==2?amount:0);
	}

	public static Vec3d offsetDim(Vec3d p, int dim, double amount) {
		return p.addVector(dim==0?amount:0, dim==1?amount:0, dim==2?amount:0);
	}

	public static boolean raytraceAlongCatenary(Connection conn, World w, Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop,
												Consumer<Triple<BlockPos, Vec3d, Vec3d>> close)
	{
		Vec3d vStart = getVecForIICAt(w, conn.start, conn);
		Vec3d vEnd = getVecForIICAt(w, conn.end, conn);
		return raytraceAlongCatenary(conn, shouldStop, close, vStart, vEnd);
	}

	public static boolean raytraceAlongCatenary(Connection conn, Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop,
												Consumer<Triple<BlockPos, Vec3d, Vec3d>> close, Vec3d vStart, Vec3d vEnd)
	{
		conn.getSubVertices(vStart, vEnd);
		HashMap<BlockPos, Vec3d> halfScanned = new HashMap<>();
		HashSet<BlockPos> done = new HashSet<>();
		HashSet<Triple<BlockPos, Vec3d, Vec3d>> near = new HashSet<>();
		Vec3d across = vEnd.subtract(vStart);
		across = new Vec3d(across.x, 0, across.z);
		double lengthHor = across.lengthVector();
		halfScanned.put(conn.start, vStart);
		halfScanned.put(conn.end, vEnd);
		//Raytrace X&Z
		for (int dim = 0; dim <= 2; dim += 2)
		{
			int start = (int) Math.ceil(Math.min(getDim(vStart, dim), getDim(vEnd, dim)));
			int end = (int) Math.floor(Math.max(getDim(vStart, dim), getDim(vEnd, dim)));
			for (int i = start; (start > end ? i >= end : i <= end); i += (end == start ? 1 : Math.signum(end - start)))
			{
				double factor = (i - getDim(vStart, dim)) / getDim(across, dim);
				Vec3d pos = conn.getVecAt(factor, vStart, across, lengthHor);

				if (handleVec(pos, pos, 0, halfScanned, done, shouldStop, near))
					return false;
			}
		}
		//Raytrace Y
		double min = conn.catA + conn.catOffsetY + vStart.y;
		for (int i = 0; i < 2; i++)
		{
			double factor = i == 0 ? 1 : -1;
			double max = i == 0 ? vEnd.y : vStart.y;
			for (int y = (int) Math.ceil(min); y <= Math.floor(max); y++)
			{
				double yReal = y - vStart.y;
				double posRel = (factor * acosh((yReal - conn.catOffsetY) / conn.catA) * conn.catA + conn.catOffsetX) / lengthHor;
				Vec3d pos = new Vec3d(vStart.x + across.x * posRel, y, vStart.z + across.z * posRel);

				if (posRel>=0&&posRel<=1&&handleVec(pos, pos, 0, halfScanned, done, shouldStop, near))
					return false;
			}
		}
		for (Triple<BlockPos, Vec3d, Vec3d> p : near)
			close.accept(p);
		for (Map.Entry<BlockPos, Vec3d> p : halfScanned.entrySet())
			if (shouldStop.test(new ImmutableTriple<>(p.getKey(), p.getValue(), p.getValue())))
				return false;
		return true;
	}

	private static boolean handleVec(Vec3d pos, Vec3d origPos, int start, HashMap<BlockPos, Vec3d> halfScanned, HashSet<BlockPos> done,
									 Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop, HashSet<Triple<BlockPos, Vec3d, Vec3d>> near)
	{
		final double DELTA_HIT = 1e-5;
		final double EPSILON = 1e-5;
		boolean calledOther = false;
		for (int i = start; i < 3; i++)
		{
			double coord = getDim(pos, i);
			double diff = coord - Math.floor(coord);
			if (diff < DELTA_HIT)
			{
				if (handleVec(offsetDim(pos, i, -(diff + EPSILON)), origPos, i + 1, halfScanned, done, shouldStop, near))
					return true;
				calledOther = true;
			}
			diff = Math.ceil(coord) - coord;
			if (diff < DELTA_HIT)
			{
				if (handleVec(offsetDim(pos, i, diff + EPSILON), origPos, i + 1, halfScanned, done, shouldStop, near))
					return true;
				calledOther = true;
			}
		}
		if (!calledOther)
			return handlePos(origPos, new BlockPos(pos), halfScanned, done, shouldStop, near);
		return false;
	}

	private static boolean handlePos(Vec3d pos, BlockPos posB, HashMap<BlockPos, Vec3d> halfScanned, HashSet<BlockPos> done,
									 Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop, HashSet<Triple<BlockPos, Vec3d, Vec3d>> near)
	{
		final double DELTA_NEAR = .3;
		if (!done.contains(posB))
		{
			if (halfScanned.containsKey(posB)&&!pos.equals(halfScanned.get(posB)))
			{
				Triple<BlockPos, Vec3d, Vec3d> added = new ImmutableTriple<>(posB, halfScanned.get(posB), pos);
				boolean stop = shouldStop.test(added);
				done.add(posB);
				halfScanned.remove(posB);
				near.removeIf((t) -> t.getLeft().equals(posB));
				if (stop)
					return true;
				for (int i = 0;i<3;i++)
				{
					double coord = getDim(pos, i);
					double diff = coord-Math.floor(coord);
					if (diff<DELTA_NEAR)
						near.add(new ImmutableTriple<>(offsetDim(posB, i, -1), added.getMiddle(), added.getRight()));
					diff = Math.ceil(coord)-coord;
					if (diff<DELTA_NEAR)
						near.add(new ImmutableTriple<>(offsetDim(posB, i, 1), added.getMiddle(), added.getRight()));
				}
			}
			else
			{
				halfScanned.put(posB, pos);
			}
		}
		return false;
	}

	public static WireType getWireTypeFromNBT(NBTTagCompound tag, String key)
	{
		//Legacy code for old save data, where types used to be integers
		if(tag.getTag(key) instanceof NBTTagInt)
		{
			int i = tag.getInteger(key);
			return i==1?WireType.ELECTRUM: i==2?WireType.STEEL: i==3?WireType.STRUCTURE_ROPE: i==4?WireType.STRUCTURE_STEEL: WireType.COPPER;
		}
		else
			return WireType.getValue(tag.getString(key));
	}

	public static Object convertToValidRecipeInput(Object input)
	{
		if(input instanceof ItemStack)
			return input;
		else if(input instanceof Item)
			return new ItemStack((Item)input);
		else if(input instanceof Block)
			return new ItemStack((Block)input);
		else if(input instanceof List)
			return input;
		else if(input instanceof String)
		{
			if(!ApiUtils.isExistingOreName((String)input))
				return null;
			List<ItemStack> l = OreDictionary.getOres((String)input);
			if(!l.isEmpty())
				return l;
			else
				return null;
		}
		else
			throw new RuntimeException("Recipe Inputs must always be ItemStack, Item, Block or String (OreDictionary name), "+input+" is invalid");
	}

	public static IngredientStack createIngredientStack(Object input, boolean preferWildcard)
	{
		if(input instanceof IngredientStack)
			return (IngredientStack)input;
		else if(input instanceof ItemStack)
			return new IngredientStack((ItemStack)input);
		else if(input instanceof Item)
		{
			if(preferWildcard)
				return new IngredientStack(new ItemStack((Item)input,1,OreDictionary.WILDCARD_VALUE));
			return new IngredientStack(new ItemStack((Item)input));
		}
		else if(input instanceof Block)
		{
			if(preferWildcard)
				return new IngredientStack(new ItemStack((Block)input,1,OreDictionary.WILDCARD_VALUE));
			return new IngredientStack(new ItemStack((Block)input));
		} else if(input instanceof List && !((List)input).isEmpty())
		{
			if(((List)input).get(0) instanceof ItemStack)
				return new IngredientStack(((List<ItemStack>) input));
			else if(((List)input).get(0) instanceof String)
			{
				ArrayList<ItemStack> itemList = new ArrayList();
				for(String s : ((List<String>) input))
					itemList.addAll(OreDictionary.getOres(s));
				return new IngredientStack(itemList);
			}
		} else if(input instanceof ItemStack[])
			return new IngredientStack(Arrays.asList((ItemStack[]) input));
		else if(input instanceof String[])
		{
			ArrayList<ItemStack> itemList = new ArrayList();
			for(String s : ((String[]) input))
				itemList.addAll(OreDictionary.getOres(s));
			return new IngredientStack(itemList);
		}
		else if(input instanceof String)
			return new IngredientStack((String)input);
		else if(input instanceof FluidStack)
			return new IngredientStack((FluidStack)input);
		throw new RuntimeException("Recipe Ingredients must always be ItemStack, Item, Block, List<ItemStack>, String (OreDictionary name) or FluidStack; "+input+" is invalid");
	}
	public static IngredientStack createIngredientStack(Object input)
	{
		return createIngredientStack(input, false);
	}

	public static ItemStack getItemStackFromObject(Object o)
	{
		if(o instanceof ItemStack)
			return (ItemStack)o;
		else if(o instanceof Item)
			return new ItemStack((Item)o);
		else if(o instanceof Block)
			return new ItemStack((Block)o);
		else if(o instanceof List)
			return ((List<ItemStack>)o).get(0);
		else if(o instanceof String)
		{
			if(!isExistingOreName((String)o))
				return ItemStack.EMPTY;
			List<ItemStack> l = OreDictionary.getOres((String)o);
			if(!l.isEmpty())
				return l.get(0);
			else
				return ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	public static boolean hasPlayerIngredient(EntityPlayer player, IngredientStack ingredient)
	{
		int amount = ingredient.inputSize;
		ItemStack itemstack;
		for(EnumHand hand : EnumHand.values())
		{
			itemstack = player.getHeldItem(hand);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				amount -= itemstack.getCount();
				if(amount <= 0)
					return true;
			}
		}
		for(int i=0; i<player.inventory.getSizeInventory(); i++)
		{
			itemstack = player.inventory.getStackInSlot(i);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				amount -= itemstack.getCount();
				if(amount<=0)
					return true;
			}
		}
		return amount <=0;
	}
	public static void consumePlayerIngredient(EntityPlayer player, IngredientStack ingredient)
	{
		int amount = ingredient.inputSize;
		ItemStack itemstack;
		for(EnumHand hand : EnumHand.values())
		{
			itemstack = player.getHeldItem(hand);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				int taken = Math.min(amount, itemstack.getCount());
				amount -= taken;
				itemstack.shrink(taken);
				if(itemstack.getCount() <= 0)
					player.setHeldItem(hand, ItemStack.EMPTY);
				if(amount<=0)
					return;
			}
		}
		for(int i=0; i<player.inventory.getSizeInventory(); i++)
		{
			itemstack = player.inventory.getStackInSlot(i);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				int taken = Math.min(amount, itemstack.getCount());
				amount -= taken;
				itemstack.shrink(taken);
				if(itemstack.getCount() <= 0)
					player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
				if(amount<=0)
					return;
			}
		}
	}

	public static Map<String, Integer> sortMap(Map<String, Integer> map, boolean inverse)
	{
		TreeMap<String,Integer> sortedMap = new TreeMap<String,Integer>(new ValueComparator(map, inverse));
		sortedMap.putAll(map);
		return sortedMap;
	}

	public static <T extends TileEntity&IGeneralMultiblock> void checkForNeedlessTicking(T te)
	{
		if (!te.getWorld().isRemote&&te.isLogicDummy())
			EventHandler.REMOVE_FROM_TICKING.add(te);
	}

	public static boolean preventsConnection(World worldIn, BlockPos pos, IBlockState state, Vec3d a, Vec3d b)
	{
		if (state.getBlock().canCollideCheck(state, false))
		{
			AxisAlignedBB aabb = state.getBoundingBox(worldIn, pos).offset(pos).grow(1e-5);
			if (aabb.contains(a)||aabb.contains(b))
				return true;
			RayTraceResult rayResult = state.collisionRayTrace(worldIn, pos, a, b);
			return rayResult != null && rayResult.typeOfHit == RayTraceResult.Type.BLOCK;
		}
		return false;
	}

	//Based on net.minecraft.entity.EntityLivingBase.knockBack
	public static void knockbackNoSource(EntityLivingBase entity, double strength, double xRatio, double zRatio)
	{
		entity.isAirBorne = true;
		float factor = MathHelper.sqrt(xRatio * xRatio + zRatio * zRatio);
		entity.motionX /= 2;
		entity.motionZ /= 2;
		entity.motionX -= xRatio / (double)factor * strength;
		entity.motionZ -= zRatio / (double)factor * strength;

		if (entity.onGround)
		{
			entity.motionY /= 2;
			entity.motionY += strength;

			if (entity.motionY > 0.4)
			{
				entity.motionY = 0.4;
			}
		}
	}

	public static class ValueComparator implements java.util.Comparator<String>
	{
		Map<String, Integer> base;
		boolean inverse;
		public ValueComparator(Map<String, Integer> base, boolean inverse)
		{
			this.base = base;
			this.inverse = inverse;
		}
		@Override
		public int compare(String s0, String s1)//Cant return equal to keys separate
		{
			if(inverse)
			{
				if (base.get(s0) <= base.get(s1))
					return -1;
				else
					return 1;
			}
			else
			{
				if (base.get(s0) >= base.get(s1))
					return -1;
				else
					return 1;
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof ValueComparator))
				return false;
			ValueComparator other = (ValueComparator) obj;
			return other.base == base && other.inverse == inverse;
		}
	}

	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getRegisterSprite(TextureMap map, String path)
	{
		TextureAtlasSprite sprite = map.getTextureExtry(path);
		if(sprite==null)
		{
			map.registerSprite(new ResourceLocation(path));
			sprite = map.getTextureExtry(path);
		}
		return sprite;
	}
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getRegisterSprite(TextureMap map, ResourceLocation path)
	{
		TextureAtlasSprite sprite = map.getTextureExtry(path.toString());
		if(sprite==null)
		{
			map.registerSprite(path);
			sprite = map.getTextureExtry(path.toString());
		}
		return sprite;
	}

	@SideOnly(Side.CLIENT)
	public static Function<BakedQuad, BakedQuad> transformQuad(Matrix4 mat, VertexFormat f,
															   Function<Integer, Integer> colorMultiplier)
	{
		int posPos = -1;
		int normPos = -1;
		int colorPos = -1;
		for (int i = 0;i<f.getElements().size();i++)
			if (f.getElement(i).getUsage()== VertexFormatElement.EnumUsage.POSITION)
				posPos = i;
			else if (f.getElement(i).getUsage()== VertexFormatElement.EnumUsage.NORMAL)
				normPos = i;
			else if (f.getElement(i).getUsage()== VertexFormatElement.EnumUsage.COLOR)
				colorPos = i;
		if (posPos==-1)
			return null;
		final int posPosFinal = posPos;
		final int normPosFinal = normPos;
		final int colorPosFinal = colorPos;
		AtomicReference<UnpackedBakedQuad.Builder> ref = new AtomicReference<>();
		Matrix4 inverse = mat.copy();
		inverse.invert();
		inverse.transpose();
		IVertexConsumer transformer = new IVertexConsumer()
		{
			int tintIndex = -1;
			@Nonnull
			@Override
			public VertexFormat getVertexFormat()
			{
				return f;
			}

			@Override
			public void setQuadTint(int tint)
			{
				ref.get().setQuadTint(tint);
				tintIndex = tint;
			}

			@Override
			public void setQuadOrientation(@Nonnull EnumFacing orientation)
			{
				ref.get().setQuadOrientation(orientation);
			}

			@Override
			public void setApplyDiffuseLighting(boolean diffuse)
			{
				ref.get().setApplyDiffuseLighting(diffuse);
			}

			@Override
			public void setTexture(@Nonnull TextureAtlasSprite texture)
			{
				ref.get().setTexture(texture);
			}

			@Override
			public void put(int element, @Nonnull float... data)
			{
				if (element==posPosFinal)
				{
					Vector3f newPos = mat.apply(new Vector3f(data[0], data[1], data[2]));
					data = new float[3];
					data[0] = newPos.x;
					data[1] = newPos.y;
					data[2] = newPos.z;
				}
				else if (element==normPosFinal)
				{
					Vector3f newNormal = inverse.apply(new Vector3f(data[0], data[1], data[2]));
					data = new float[3];
					data[0] = newNormal.x;
					data[1] = newNormal.y;
					data[2] = newNormal.z;
				}
				else if (element==colorPosFinal)
				{
					if (tintIndex!=-1&&colorMultiplier!=null)
					{
						int multiplier = colorMultiplier.apply(tintIndex);
						if (multiplier != 0)
						{
							float r = (float) (multiplier >> 16 & 255) / 255.0F;
							float g = (float) (multiplier >> 8 & 255) / 255.0F;
							float b = (float) (multiplier & 255) / 255.0F;
							float[] oldData = data;
							data = new float[4];
							data[0] = oldData[0] * r;
							data[1] = oldData[1] * g;
							data[2] = oldData[2] * b;
							data[3] = oldData[3];
						}
					}
				}
				ref.get().put(element, data);
			}
		};
		return (q)->{
			ref.set(new UnpackedBakedQuad.Builder(f));
			q.pipe(transformer);
			return ref.get().build();
		};
	}
}