/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.network.MessageObstructedConnection;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.common.IERecipes.getIngot;

public class ApiUtils
{
	public static boolean compareToOreName(ItemStack stack, ResourceLocation oreName)
	{
		if(!isNonemptyItemTag(oreName))
			return false;
		Tag<Item> itemTag = ItemTags.getCollection().get(oreName);
		if(itemTag!=null&&itemTag.getAllElements().contains(stack.getItem()))
			return true;
		Tag<Block> blockTag = BlockTags.getCollection().get(oreName);
		if(blockTag!=null&&blockTag.getAllElements()
				.stream()
				.map(IItemProvider::asItem)
				.anyMatch(i -> stack.getItem()==i))
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
			return ItemStack.areItemsEqual((ItemStack)o, stack)&&
					(!checkNBT||Utils.compareItemNBT((ItemStack)o, stack));
		else if(o instanceof Collection)
		{
			for(Object io : (Collection)o)
				if(stackMatchesObject(stack, io, checkNBT))
					return true;
		}
		else if(o instanceof IngredientStack)
			return ((IngredientStack)o).matchesItemStack(stack);
		else if(o instanceof ItemStack[])
		{
			for(ItemStack io : (ItemStack[])o)
				if(ItemStack.areItemsEqual(io, stack)&&(!checkNBT||Utils.compareItemNBT(io, stack)))
					return true;
		}
		else if(o instanceof FluidStack)
			return FluidUtil.getFluidContained(stack)
					.map(fs -> fs.containsFluid((FluidStack)o))
					.orElse(false);
		else if(o instanceof ResourceLocation)
			return compareToOreName(stack, (ResourceLocation)o);
		else
			throw new IllegalArgumentException("Comparisong object "+o+" of class "+o.getClass()+" is invalid!");
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
								amount = 0;
							}
							else
							{
								amount -= query.getCount();
								query.setCount(0);
							}
						}
						if(query.getCount() <= 0)
							it.remove();
						if(amount <= 0)
							break;
					}
				}
				if(amount > 0)
					return false;
			}
		return true;
	}

	public static Ingredient createIngredientFromList(List<ItemStack> list)
	{
		return Ingredient.fromStacks(list.toArray(new ItemStack[0]));
	}

	@Deprecated
	public static ComparableItemStack createComparableItemStack(ItemStack stack)
	{
		return createComparableItemStack(stack, true);
	}


	public static ComparableItemStack createComparableItemStack(ItemStack stack, boolean copy)
	{
		return createComparableItemStack(stack, copy, stack.hasTag()&&!stack.getOrCreateTag().isEmpty());
	}

	public static ComparableItemStack createComparableItemStack(ItemStack stack, boolean copy, boolean useNbt)
	{
		ComparableItemStack comp = new ComparableItemStack(stack, true, copy);
		comp.setUseNBT(useNbt);
		return comp;
	}

	public static boolean isNonemptyItemTag(ResourceLocation name)
	{
		Tag<Item> t = ItemTags.getCollection().getTagMap().get(name);
		return t!=null&&!t.getAllElements().isEmpty();
	}

	public static boolean isNonemptyBlockTag(ResourceLocation name)
	{
		Tag<Block> t = BlockTags.getCollection().getTagMap().get(name);
		return t!=null&&!t.getAllElements().isEmpty();
	}

	public static boolean isNonemptyBlockOrItemTag(ResourceLocation name)
	{
		return isNonemptyBlockTag(name)||isNonemptyItemTag(name);
	}

	public static NonNullList<ItemStack> getItemsInTag(ResourceLocation name)
	{
		NonNullList<ItemStack> ret = NonNullList.create();
		addItemsInTag(ret, ItemTags.getCollection().get(name));
		addItemsInTag(ret, BlockTags.getCollection().get(name));
		return ret;
	}

	private static <T extends IItemProvider> void addItemsInTag(NonNullList<ItemStack> out, Tag<T> in)
	{
		if(in!=null)
			in.getAllElements().stream()
					.map(ItemStack::new)
					.forEach(out::add);
	}

	public static boolean isMetalComponent(ItemStack stack, String componentType)
	{
		return getMetalComponentType(stack, componentType)!=null;
	}

	public static String getMetalComponentType(ItemStack stack, String... componentTypes)
	{
		for(ResourceLocation name : getMatchingTagNames(stack))
		{
			for(String componentType : componentTypes)
				if(name.getPath().startsWith(componentType))
					return componentType;
		}
		return null;
	}

	public static Collection<ResourceLocation> getMatchingTagNames(ItemStack stack)
	{
		Collection<ResourceLocation> ret = new HashSet<>(stack.getItem().getTags());
		Block b = Block.getBlockFromItem(stack.getItem());
		if(b!=Blocks.AIR)
			ret.addAll(b.getTags());
		return ret;
	}

	public static String[] getMetalComponentTypeAndMetal(ItemStack stack, String... componentTypes)
	{
		for(ResourceLocation name : getMatchingTagNames(stack))
		{
			for(String componentType : componentTypes)
				if(name.getPath().startsWith(componentType))
					return new String[]{componentType, name.getPath().substring(componentType.length())};
		}
		return null;
	}

	public static boolean isIngot(ItemStack stack)
	{
		return isMetalComponent(stack, "ingots/");
	}

	public static boolean isPlate(ItemStack stack)
	{
		return isMetalComponent(stack, "plates/");
	}

	public static int getComponentIngotWorth(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[0]);
		String key = getMetalComponentType(stack, keys);
		if(key!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(key);
			if(relation!=null&&relation.length > 1)
			{
				double val = relation[0]/(double)relation[1];
				return (int)val;
			}
		}
		return 0;
	}

	public static ItemStack breakStackIntoIngots(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[0]);
		String[] type = getMetalComponentTypeAndMetal(stack, keys);
		if(type!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(type[0]);
			if(relation!=null&&relation.length > 1)
			{
				double val = relation[0]/(double)relation[1];
				return copyStackWithAmount(IEApi.getPreferredTagStack(getIngot(type[1])), (int)val);
			}
		}
		return ItemStack.EMPTY;
	}

	public static Pair<ItemStack, Double> breakStackIntoPreciseIngots(ItemStack stack)
	{
		String[] keys = IEApi.prefixToIngotMap.keySet().toArray(new String[0]);
		String[] type = getMetalComponentTypeAndMetal(stack, keys);
		if(type!=null)
		{
			Integer[] relation = IEApi.prefixToIngotMap.get(type[0]);
			if(relation!=null&&relation.length > 1)
			{
				double val = relation[0]/(double)relation[1];
				return new ImmutablePair<>(IEApi.getPreferredTagStack(getIngot(type[1])), val);
			}
		}
		return null;
	}

	public static boolean canInsertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side)
	{
		if(!stack.isEmpty()&&inventory!=null)
		{
			return inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
					.map(handler -> {
						ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
						return temp.isEmpty()||temp.getCount() < stack.getCount();
					})
					.orElse(false);
		}
		return false;
	}

	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side)
	{
		if(!stack.isEmpty()&&inventory!=null)
		{
			return inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
					.map(handler -> {
						ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
						if(temp.isEmpty()||temp.getCount() < stack.getCount())
							return ItemHandlerHelper.insertItem(handler, stack, false);
						return stack;
					})
					.orElse(stack);
		}
		return stack;
	}

	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side, boolean simulate)
	{
		if(inventory!=null&&!stack.isEmpty())
		{
			return inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
					.map(handler -> ItemHandlerHelper.insertItem(handler, stack.copy(), simulate))
					.orElse(stack);
		}
		return stack;
	}

	public static BlockPos toBlockPos(Object object)
	{
		if(object instanceof BlockPos)
			return (BlockPos)object;
		if(object instanceof TileEntity)
			return ((TileEntity)object).getPos();
		if(object instanceof IICProxy)
			return ((IICProxy)object).getPos();
		return null;
	}

	@Deprecated
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		return toIIC(object, world, true);
	}

	@Deprecated
	public static IImmersiveConnectable toIIC(Object object, World world, boolean allowProxies)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof BlockPos)
		{
			BlockPos pos = (BlockPos)object;
			if(world!=null&&(allowProxies||world.isBlockLoaded(pos)))
				return GlobalWireNetwork.getNetwork(world).getLocalNet(pos).getConnector(pos);
		}
		return null;
	}

	public static Vec3d getVecForIICAt(LocalWireNetwork net, ConnectionPoint pos, Connection conn, boolean fromOtherEnd)
	{
		Vec3d offset = Vec3d.ZERO;
		//Force loading
		IImmersiveConnectable iicPos = net.getConnector(pos.getPosition());
		Preconditions.checkArgument(!(iicPos instanceof IICProxy));
		if(iicPos!=null)
			offset = iicPos.getConnectionOffset(conn, pos);
		if(fromOtherEnd)
		{
			BlockPos posA = pos.getPosition();
			BlockPos posB = conn.getOtherEnd(pos).getPosition();
			offset = offset.add(posA.getX()-posB.getX(), posA.getY()-posB.getY(), posA.getZ()-posB.getZ());
		}
		return offset;
	}

	public static Vec3d addVectors(Vec3d vec0, Vec3d vec1)
	{
		return vec0.add(vec1.x, vec1.y, vec1.z);
	}

	public static double acosh(double x)
	{
		//See http://mathworld.wolfram.com/InverseHyperbolicCosine.html
		return Math.log(x+Math.sqrt(x+1)*Math.sqrt(x-1));
	}

	public static Vec3d[] getConnectionCatenary(Vec3d start, Vec3d end, double slack)
	{
		final int vertices = 17;
		double dx = (end.x)-(start.x);
		double dy = (end.y)-(start.y);
		double dz = (end.z)-(start.z);
		double dw = Math.sqrt(dx*dx+dz*dz);
		double k = Math.sqrt(dx*dx+dy*dy+dz*dz)*slack;
		double l = 0;
		int limiter = 0;
		while(limiter < 300)
		{
			limiter++;
			l += 0.01;
			if(Math.sinh(l)/l >= Math.sqrt(k*k-dy*dy)/dw)
				break;
		}
		double a = dw/2/l;
		double offsetX = (0+dw-a*Math.log((k+dy)/(k-dy)))*0.5;
		double offsetY = (dy+0-k*Math.cosh(l)/Math.sinh(l))*0.5;
		Vec3d[] vex = new Vec3d[vertices+1];

		vex[0] = new Vec3d(start.x, start.y, start.z);
		for(int i = 1; i < vertices; i++)
		{
			float posRelative = i/(float)vertices;
			double x = 0+dx*posRelative;
			double z = 0+dz*posRelative;
			double y = a*Math.cosh((dw*posRelative-offsetX)/a)+offsetY;
			vex[i] = new Vec3d(start.x+x, start.y+y, start.z+z);
		}
		vex[vertices] = new Vec3d(end.x, end.y, end.z);

		return vex;
	}


	public static double getDim(Vec3d vec, int dim)
	{
		return dim==0?vec.x: (dim==1?vec.y: vec.z);
	}

	public static BlockPos offsetDim(BlockPos p, int dim, int amount)
	{
		return p.add(dim==0?amount: 0, dim==1?amount: 0, dim==2?amount: 0);
	}

	public static Vec3d offsetDim(Vec3d p, int dim, double amount)
	{
		return p.add(dim==0?amount: 0, dim==1?amount: 0, dim==2?amount: 0);
	}

	public static boolean raytraceAlongCatenary(Connection conn, LocalWireNetwork net, Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop,
												Consumer<Triple<BlockPos, Vec3d, Vec3d>> close)
	{
		Vec3d vStart = getVecForIICAt(net, conn.getEndA(), conn, false);
		Vec3d vEnd = getVecForIICAt(net, conn.getEndB(), conn, true);
		IELogger.logger.info("Start: {}, end: {}", vStart, vEnd);
		return raytraceAlongCatenaryRelative(conn, shouldStop, close, vStart, vEnd);
	}

	/**
	 * Use raytraceAlongCatenaryRelative instead, pass vStart and vEnd relative to the start of the connection
	 */
	@Deprecated
	public static boolean raytraceAlongCatenary(Connection conn, Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop,
												Consumer<Triple<BlockPos, Vec3d, Vec3d>> close, Vec3d vStart, Vec3d vEnd)
	{
		BlockPos posA = conn.getEndA().getPosition();
		return raytraceAlongCatenaryRelative(conn, shouldStop, close,
				vStart.subtract(posA.getX(), posA.getY(), posA.getZ()),
				vEnd.subtract(posA.getX(), posA.getY(), posA.getZ()));
	}

	public static boolean raytraceAlongCatenaryRelative(Connection conn, Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop,
														Consumer<Triple<BlockPos, Vec3d, Vec3d>> close, Vec3d vStart, Vec3d vEnd)
	{
		IELogger.logger.info("Start is {}, end {}", vStart, vEnd);
		conn.generateCatenaryData(vStart, vEnd);
		HashMap<BlockPos, Vec3d> halfScanned = new HashMap<>();
		HashSet<BlockPos> done = new HashSet<>();
		HashSet<Triple<BlockPos, Vec3d, Vec3d>> near = new HashSet<>();
		Vec3d across = vEnd.subtract(vStart);
		across = new Vec3d(across.x, 0, across.z);
		double lengthHor = across.length();
		BlockPos startPos = conn.getEndA().getPosition();
		BlockPos endPos = conn.getEndB().getPosition();
		halfScanned.put(startPos, vStart);
		halfScanned.put(endPos, vEnd);
		//Raytrace X&Z
		for(int dim = 0; dim <= 2; dim += 2)
		{
			int start = (int)Math.ceil(Math.min(getDim(vStart, dim), getDim(vEnd, dim)));
			int end = (int)Math.ceil(Math.max(getDim(vStart, dim), getDim(vEnd, dim)));
			for(int i = start; i < end; i++)
			{
				double factor = (i-getDim(vStart, dim))/getDim(across, dim);
				Vec3d pos = conn.getPoint(factor, conn.getEndA());

				if(handleVec(pos, pos, 0, halfScanned, done, shouldStop, near, startPos))
					return false;
			}
		}
		//Raytrace Y
		boolean vertical = vStart.x==vEnd.x&&vStart.z==vEnd.z;
		if(vertical)
		{
			for(int y = (int)Math.ceil(Math.min(vStart.y, vEnd.y)); y <= Math.floor(Math.max(vStart.y, vEnd.y)); y++)
			{
				Vec3d pos = new Vec3d(vStart.x, y, vStart.z);
				if(handleVec(pos, pos, 0, halfScanned, done, shouldStop, near, startPos))
					return false;
			}
		}
		else
		{
			CatenaryData data = conn.catData;
			double min = data.getA()+data.getOffsetY()+vStart.y;
			for(int i = 0; i < 2; i++)
			{
				double factor = i==0?1: -1;
				double max = i==0?vEnd.y: vStart.y;
				for(int y = (int)Math.ceil(min); y <= Math.floor(max); y++)
				{
					double yReal = y-vStart.y;
					double posRel;
					Vec3d pos;
					posRel = (factor*acosh((yReal-data.getOffsetY())/data.getA())*data.getA()+data.getOffsetX())/lengthHor;
					pos = new Vec3d(vStart.x+across.x*posRel, y, vStart.z+across.z*posRel);

					if(posRel >= 0&&posRel <= 1&&handleVec(pos, pos, 0, halfScanned, done, shouldStop, near, startPos))
						return false;
				}
			}
		}
		for(Triple<BlockPos, Vec3d, Vec3d> p : near)
			close.accept(p);
		for(Map.Entry<BlockPos, Vec3d> p : halfScanned.entrySet())
			if(shouldStop.test(new ImmutableTriple<>(p.getKey(), p.getValue(), p.getValue())))
				return false;
		return true;
	}

	private static boolean handleVec(Vec3d pos, Vec3d origPos, int start, HashMap<BlockPos, Vec3d> halfScanned, HashSet<BlockPos> done,
									 Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop, HashSet<Triple<BlockPos, Vec3d, Vec3d>> near,
									 BlockPos offset)
	{
		final double DELTA_HIT = 1e-5;
		final double EPSILON = 1e-5;
		boolean calledOther = false;
		for(int i = start; i < 3; i++)
		{
			double coord = getDim(pos, i);
			double diff = coord-Math.floor(coord);
			if(diff < DELTA_HIT)
			{
				if(handleVec(offsetDim(pos, i, -(diff+EPSILON)), origPos, i+1, halfScanned, done, shouldStop, near, offset))
					return true;
				calledOther = true;
			}
			diff = Math.ceil(coord)-coord;
			if(diff < DELTA_HIT)
			{
				if(handleVec(offsetDim(pos, i, diff+EPSILON), origPos, i+1, halfScanned, done, shouldStop, near, offset))
					return true;
				calledOther = true;
			}
		}
		if(!calledOther)
		{
			BlockPos blockPos = new BlockPos(pos);
			return handlePos(origPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
					blockPos.add(offset), halfScanned, done, shouldStop, near);
		}
		return false;
	}

	private static boolean handlePos(Vec3d pos, BlockPos posB, HashMap<BlockPos, Vec3d> halfScanned, HashSet<BlockPos> done,
									 Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop, HashSet<Triple<BlockPos, Vec3d, Vec3d>> near)
	{
		final double DELTA_NEAR = .3;
		if(!done.contains(posB))
		{
			if(halfScanned.containsKey(posB)&&!pos.equals(halfScanned.get(posB)))
			{
				Triple<BlockPos, Vec3d, Vec3d> added = new ImmutableTriple<>(posB, halfScanned.get(posB), pos);
				boolean stop = shouldStop.test(added);
				done.add(posB);
				halfScanned.remove(posB);
				near.removeIf((t) -> t.getLeft().equals(posB));
				if(stop)
					return true;
				for(int i = 0; i < 3; i++)
				{
					double coord = getDim(pos, i);
					double diff = coord-Math.floor(coord);
					if(diff < DELTA_NEAR)
						near.add(new ImmutableTriple<>(offsetDim(posB, i, -1), added.getMiddle(), added.getRight()));
					diff = Math.ceil(coord)-coord;
					if(diff < DELTA_NEAR)
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

	public static WireType getWireTypeFromNBT(CompoundNBT tag, String key)
	{
		return WireType.getValue(tag.getString(key));
	}

	public static ActionResultType doCoilUse(IWireCoil coil, PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof IImmersiveConnectable&&((IImmersiveConnectable)tileEntity).canConnect())
		{
			ItemStack stack = player.getHeldItem(hand);
			TargetingInfo targetHere = new TargetingInfo(side, hitX-pos.getX(), hitY-pos.getY(), hitZ-pos.getZ());
			WireType wire = coil.getWireType(stack);
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(wire, targetHere);
			BlockPos offsetHere = pos.subtract(masterPos);
			tileEntity = world.getTileEntity(masterPos);
			if(!(tileEntity instanceof IImmersiveConnectable)||!((IImmersiveConnectable)tileEntity).canConnect())
				return ActionResultType.PASS;
			IImmersiveConnectable iicHere = (IImmersiveConnectable)tileEntity;
			ConnectionPoint cpHere = iicHere.getTargetedPoint(targetHere, offsetHere);

			if(cpHere==null||!((IImmersiveConnectable)tileEntity).canConnectCable(wire, cpHere, offsetHere)||
					!coil.canConnectCable(stack, tileEntity))
			{
				if(!world.isRemote)
					player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"wrongCable"), true);
				return ActionResultType.FAIL;
			}

			if(!world.isRemote)
				if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
				{
					DimensionBlockPos linkingPos = new DimensionBlockPos(masterPos, world);
					CompoundNBT linkingNBT = new CompoundNBT();
					linkingNBT.put("master", linkingPos.toNBT());
					linkingNBT.put("offset", NBTUtil.writeBlockPos(offsetHere));
					stack.getOrCreateTag().put("linkingPos", linkingNBT);
					CompoundNBT targetNbt = new CompoundNBT();
					targetHere.writeToNBT(targetNbt);
					ItemNBTHelper.setTagCompound(stack, "targettingInfo", targetNbt);
				}
				else
				{
					CompoundNBT linkNBT = stack.getOrCreateTag().getCompound("linkingPos");
					DimensionBlockPos linkPos = new DimensionBlockPos(linkNBT.getCompound("master"));
					BlockPos offsetLink = NBTUtil.readBlockPos(linkNBT.getCompound("offset"));
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos.pos);
					int distanceSq = (int)Math.ceil(linkPos.pos.distanceSq(masterPos));
					int maxLengthSq = coil.getMaxLength(stack); //not squared yet
					maxLengthSq *= maxLengthSq;
					if(linkPos.dimension!=world.getDimension().getType())
						player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"wrongDimension"), true);
					else if(linkPos.pos.equals(masterPos))
						player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"sameConnection"), true);
					else if(distanceSq > maxLengthSq)
						player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"tooFar"), true);
					else
					{
						TargetingInfo targetLink = TargetingInfo.readFromNBT(ItemNBTHelper.getTagCompound(stack, "targettingInfo"));
						if(!(tileEntityLinkingPos instanceof IImmersiveConnectable))
						{
							player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"invalidPoint"), true);
						}
						else
						{
							IImmersiveConnectable iicLink = (IImmersiveConnectable)tileEntityLinkingPos;
							ConnectionPoint cpLink = iicLink.getTargetedPoint(targetLink, offsetLink);
							if(!((IImmersiveConnectable)tileEntityLinkingPos).canConnectCable(wire, cpLink, offsetLink)||
									!((IImmersiveConnectable)tileEntityLinkingPos).getConnectionMaster(wire, targetLink).equals(linkPos.pos)||
									!coil.canConnectCable(stack, tileEntityLinkingPos))
							{
								player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"invalidPoint"), true);
							}
							else
							{
								GlobalWireNetwork net = GlobalWireNetwork.getNetwork(world);
								assert cpHere!=null&&cpLink!=null;//TODO display error as usual, this is just for testing
								boolean connectionExists = false;
								LocalWireNetwork localA = net.getLocalNet(cpHere);
								LocalWireNetwork localB = net.getLocalNet(cpLink);
								if(localA==localB)
								{
									Collection<Connection> outputs = localA.getConnections(cpHere);
									if(outputs!=null)
										for(Connection con : outputs)
											if(!con.isInternal()&&con.getOtherEnd(cpHere).equals(cpLink))
												connectionExists = true;
								}
								if(connectionExists)
									player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"connectionExists"), true);
								else
								{
									Set<BlockPos> ignore = new HashSet<>();
									ignore.addAll(iicHere.getIgnored(iicLink));
									ignore.addAll(iicLink.getIgnored(iicHere));
									BlockPos.MutableBlockPos failedReason = new BlockPos.MutableBlockPos();
									Connection tempConn = new Connection(wire, cpHere, cpLink);
									Vec3d start = iicHere.getConnectionOffset(tempConn, cpHere);
									Vec3d end = iicLink.getConnectionOffset(tempConn, cpLink);
									boolean canSee = ApiUtils.raytraceAlongCatenaryRelative(tempConn, (p) -> {
										if(ignore.contains(p.getLeft()))
											return false;
										BlockState state = world.getBlockState(p.getLeft());
										if(ApiUtils.preventsConnection(world, p.getLeft(), state, p.getMiddle(), p.getRight()))
										{
											failedReason.setPos(p.getLeft());
											return true;
										}
										return false;
									}, (p) -> {
									}, start, end.add(new Vec3d(linkPos.pos.subtract(masterPos))));
									if(canSee)
									{
										Connection conn = new Connection(wire, cpHere, cpLink);
										net.addConnection(conn);

										iicHere.connectCable(wire, cpHere, iicLink, cpLink);
										iicLink.connectCable(wire, cpLink, iicHere, cpHere);
										Utils.unlockIEAdvancement(player, "main/connect_wire");

										if(!player.abilities.isCreativeMode)
											coil.consumeWire(stack, (int)Math.sqrt(distanceSq));
										((TileEntity)iicHere).markDirty();
										//TODO is this needed with the new sync system?
										world.addBlockEvent(masterPos, ((TileEntity)iicHere).getBlockState().getBlock(), -1, 0);
										BlockState state = world.getBlockState(masterPos);
										world.notifyBlockUpdate(masterPos, state, state, 3);
										((TileEntity)iicLink).markDirty();
										world.addBlockEvent(linkPos.pos, ((TileEntity)iicLink).getBlockState().getBlock(), -1, 0);
										state = world.getBlockState(linkPos.pos);
										world.notifyBlockUpdate(linkPos.pos, state, state, 3);
									}
									else
									{
										player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_WARN+"cantSee"), true);
										ImmersiveEngineering.packetHandler.send(
												PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(failedReason)),
												new MessageObstructedConnection(tempConn, failedReason, player.world));
									}
								}
							}
						}
					}
					ItemNBTHelper.remove(stack, "linkingPos");
					ItemNBTHelper.remove(stack, "targettingInfo");
				}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	public static Object convertToValidRecipeInput(Object input)
	{
		if(input instanceof Tag)
			input = ((Tag)input).getId();
		if(input instanceof ItemStack)
			return input;
		else if(input instanceof Item)
			return new ItemStack((Item)input);
		else if(input instanceof Block)
			return new ItemStack((Block)input);
		else if(input instanceof List)
			return input;
		else if(input instanceof ResourceLocation)
			return input;
		else
			throw new RuntimeException("Recipe Inputs must always be ItemStack, Item, Block or ResourceLocation (tag name), "+input+" is invalid");
	}

	public static IngredientStack createIngredientStack(Object input)
	{
		if(input instanceof IngredientStack)
			return (IngredientStack)input;
		else if(input instanceof ItemStack)
			return new IngredientStack((ItemStack)input);
		else if(input instanceof Item)
			return new IngredientStack(new ItemStack((Item)input));
		else if(input instanceof Block)
			return new IngredientStack(new ItemStack((Block)input));
		else if(input instanceof Ingredient)
			return new IngredientStack(Arrays.asList(((Ingredient)input).getMatchingStacks()));
		else if(input instanceof Tag)
			return new IngredientStack(((Tag)input).getId());
		else if(input instanceof List)
		{
			if (!((List)input).isEmpty())
			{
				if(((List)input).get(0) instanceof ItemStack)
					return new IngredientStack(((List<ItemStack>)input));
				else if(((List)input).get(0) instanceof ResourceLocation)
				{
					List<ItemStack> itemList = new ArrayList<>();
					for(ResourceLocation s : ((List<ResourceLocation>)input))
						itemList.addAll(getItemsInTag(s));
					return new IngredientStack(itemList);
				}
			}
			else
				return new IngredientStack(ItemStack.EMPTY);
		}
		else if(input instanceof ItemStack[])
			return new IngredientStack(Arrays.asList((ItemStack[])input));
		else if(input instanceof ResourceLocation[])
		{
			ArrayList<ItemStack> itemList = new ArrayList<>();
			for(ResourceLocation s : ((ResourceLocation[])input))
				itemList.addAll(getItemsInTag(s));
			return new IngredientStack(itemList);
		}
		else if(input instanceof ResourceLocation)
			return new IngredientStack((ResourceLocation)input);
		else if(input instanceof FluidStack)
			return new IngredientStack((FluidStack)input);
		throw new RuntimeException("Recipe Ingredients must always be ItemStack, Item, Block, List<ItemStack>, String (OreDictionary name) or FluidStack; "+input+" is invalid");
	}

	public static boolean hasPlayerIngredient(PlayerEntity player, IngredientStack ingredient)
	{
		int amount = ingredient.inputSize;
		ItemStack itemstack;
		for(Hand hand : Hand.values())
		{
			itemstack = player.getHeldItem(hand);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				amount -= itemstack.getCount();
				if(amount <= 0)
					return true;
			}
		}
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			itemstack = player.inventory.getStackInSlot(i);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				amount -= itemstack.getCount();
				if(amount <= 0)
					return true;
			}
		}
		return amount <= 0;
	}

	public static void consumePlayerIngredient(PlayerEntity player, IngredientStack ingredient)
	{
		int amount = ingredient.inputSize;
		ItemStack itemstack;
		for(Hand hand : Hand.values())
		{
			itemstack = player.getHeldItem(hand);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				int taken = Math.min(amount, itemstack.getCount());
				amount -= taken;
				itemstack.shrink(taken);
				if(itemstack.getCount() <= 0)
					player.setHeldItem(hand, ItemStack.EMPTY);
				if(amount <= 0)
					return;
			}
		}
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			itemstack = player.inventory.getStackInSlot(i);
			if(ingredient.matchesItemStackIgnoringSize(itemstack))
			{
				int taken = Math.min(amount, itemstack.getCount());
				amount -= taken;
				itemstack.shrink(taken);
				if(itemstack.getCount() <= 0)
					player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
				if(amount <= 0)
					return;
			}
		}
	}

	public static <T extends Comparable<T>> Map<T, Integer> sortMap(Map<T, Integer> map, boolean inverse)
	{
		TreeMap<T, Integer> sortedMap = new TreeMap<>(new ValueComparator<T>(map, inverse));
		sortedMap.putAll(map);
		return sortedMap;
	}

	public static <T extends TileEntity & IGeneralMultiblock> void checkForNeedlessTicking(T te)
	{
		if(!te.getWorld().isRemote&&te.isDummy())
			EventHandler.REMOVE_FROM_TICKING.add(te);
	}

	public static boolean preventsConnection(World worldIn, BlockPos pos, BlockState state, Vec3d a, Vec3d b)
	{
		for(AxisAlignedBB aabb : state.getCollisionShape(worldIn, pos).toBoundingBoxList())
		{
			aabb = aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ()).grow(1e-5);
			if(aabb.contains(a)||aabb.contains(b))
				return true;
		}
		return false;
	}

	//Based on net.minecraft.entity.EntityLivingBase.knockBack
	public static void knockbackNoSource(LivingEntity entity, double strength, double xRatio, double zRatio)
	{
		entity.isAirBorne = true;
		Vec3d motionOld = entity.getMotion();
		Vec3d toAdd = (new Vec3d(xRatio, 0.0D, zRatio)).normalize().scale(strength);
		entity.setMotion(
				motionOld.x/2.0D-toAdd.x,
				entity.onGround?Math.min(0.4D, motionOld.y/2.0D+strength): motionOld.y,
				motionOld.z/2.0D-toAdd.z);
	}

	public static Connection raytraceWires(World world, Vec3d start, Vec3d end, @Nullable Connection ignored)
	{
		GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
		WireCollisionData collisionData = global.getCollisionData();
		AtomicReference<Connection> ret = new AtomicReference<>();
		AtomicDouble minDistSq = new AtomicDouble(Double.POSITIVE_INFINITY);
		Utils.rayTrace(start, end, world, (pos) ->
		{
			Collection<CollisionInfo> infoAtPos = collisionData.getCollisionInfo(pos);
			for(CollisionInfo wireInfo : infoAtPos)
			{
				Connection c = wireInfo.conn;
				if(ignored==null||!c.hasSameConnectors(ignored))
				{
					Vec3d startRelative = start.add(-pos.getX(), -pos.getY(), -pos.getZ());
					Vec3d across = wireInfo.intersectB.subtract(wireInfo.intersectA);
					double t = Utils.getCoeffForMinDistance(startRelative, wireInfo.intersectA, across);
					t = MathHelper.clamp(t, 0, 1);
					Vec3d closest = wireInfo.intersectA.add(t*across.x, t*across.y, t*across.z);
					double distSq = closest.squareDistanceTo(startRelative);
					if(distSq < minDistSq.get())
					{
						ret.set(c);
						minDistSq.set(distSq);
					}
				}
			}
		});
		return ret.get();
	}

	public static Connection getConnectionMovedThrough(World world, LivingEntity e)
	{
		Vec3d start = e.getEyePosition(0);
		Vec3d end = e.getEyePosition(1);
		return raytraceWires(world, start, end, null);
	}

	public static Connection getTargetConnection(World world, PlayerEntity player, Connection ignored, double maxDistance)
	{
		Vec3d look = player.getLookVec();
		Vec3d start = player.getEyePosition(1);
		Vec3d end = start.add(look.scale(maxDistance));
		return raytraceWires(world, start, end, ignored);
	}

	public static void addFutureServerTask(World world, Runnable task, boolean forceFuture)
	{
		if(forceFuture)
			new Thread(() -> addFutureServerTask(world, task)).start();
		else
			addFutureServerTask(world, task);
	}
	public static void addFutureServerTask(World world, Runnable task)
	{
		LogicalSide side = world.isRemote?LogicalSide.CLIENT: LogicalSide.SERVER;
		ThreadTaskExecutor<?> tmp = LogicalSidedProvider.WORKQUEUE.get(side);
		tmp.deferTask(task);
	}

	public static void moveConnectionEnd(Connection conn, ConnectionPoint currEnd, ConnectionPoint newEnd, World world)
	{
		//TODO too lazy to move this to ConnPoints right now :)
		//ConnectionPoint fixedPos = conn.getOtherEnd(currEnd);
		//LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getLocalNet(newEnd);
		//IImmersiveConnectable otherSide = net.getConnector(fixedPos);
		//Vec3d start = ApiUtils.getVecForIICAt(net, fixedPos, conn);
		//Vec3d end = ApiUtils.getVecForIICAt(net, currEnd, conn);
		//if(otherSide==null||otherSide.moveConnectionTo(conn, newEnd))
		{
			//TODO
			//ImmersiveNetHandler.INSTANCE.removeConnection(world, conn, start, end);
			//Connection newConn = new Connection(fixedPos, newEnd, conn.cableType, conn.length);
			//ImmersiveNetHandler.INSTANCE.addConnection(world, fixedPos, newConn);
			//ImmersiveNetHandler.INSTANCE.addConnection(world, newEnd,
			//		new Connection(newEnd, fixedPos, conn.cableType, conn.length));
			//ImmersiveNetHandler.INSTANCE.addBlockData(world, newConn);
		}
	}

	public static <T> LazyOptional<T> constantOptional(T val)
	{
		return LazyOptional.of(() -> val);
	}

	public static class ValueComparator<T extends Comparable<T>> implements java.util.Comparator<T>
	{
		Map<T, Integer> base;
		boolean inverse;

		public ValueComparator(Map<T, Integer> base, boolean inverse)
		{
			this.base = base;
			this.inverse = inverse;
		}

		@Override
		public int compare(T s0, T s1)//Cant return equal to keys separate
		{
			int v0 = base.get(s0);
			int v1 = base.get(s1);
			int ret;
			if(v0 > v1)
				ret = -1;
			else if(v0 < v1)
				ret = 1;
			else
				ret = s0.compareTo(s1);
			return ret*(inverse?-1: 1);
		}

		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof ValueComparator))
				return false;
			ValueComparator other = (ValueComparator)obj;
			return other.base==base&&other.inverse==inverse;
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static Function<BakedQuad, BakedQuad> transformQuad(Matrix4 mat, @Nullable VertexFormat ignored,
															   Int2IntFunction colorMultiplier)
	{
		return new QuadTransformer(mat, colorMultiplier);
	}

	@OnlyIn(Dist.CLIENT)
	private static class QuadTransformer implements Function<BakedQuad, BakedQuad>
	{
		private final Matrix4 transform;
		private final Matrix4 normalTransform;
		@Nullable
		private final Int2IntFunction colorTransform;
		private UnpackedBakedQuad.Builder currentQuadBuilder;
		private final Map<VertexFormat, IVertexConsumer> consumers = new HashMap<>();

		private QuadTransformer(Matrix4 transform, @Nullable Int2IntFunction colorTransform)
		{
			this.transform = transform;
			this.colorTransform = colorTransform;
			this.normalTransform = transform.copy();
			normalTransform.transpose().invert();
		}

		@Override
		public BakedQuad apply(BakedQuad q)
		{
			IVertexConsumer transformer = consumers.computeIfAbsent(q.getFormat(), this::createConsumer);
			assert transformer!=null;
			currentQuadBuilder = new UnpackedBakedQuad.Builder(q.getFormat());
			q.pipe(transformer);
			return currentQuadBuilder.build();
		}

		private IVertexConsumer createConsumer(VertexFormat f)
		{
			int posPos = -1;
			int normPos = -1;
			int colorPos = -1;
			for(int i = 0; i < f.getElements().size(); i++)
				if(f.getElement(i).getUsage()==VertexFormatElement.Usage.POSITION)
					posPos = i;
				else if(f.getElement(i).getUsage()==VertexFormatElement.Usage.NORMAL)
					normPos = i;
				else if(f.getElement(i).getUsage()==VertexFormatElement.Usage.COLOR)
					colorPos = i;
			if(posPos==-1)
				return null;
			final int posPosFinal = posPos;
			final int normPosFinal = normPos;
			final int colorPosFinal = colorPos;
			return new IVertexConsumer()
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
					currentQuadBuilder.setQuadTint(tint);
					tintIndex = tint;
				}

				@Override
				public void setQuadOrientation(@Nonnull Direction orientation)
				{
					Vec3d newFront = normalTransform.apply(new Vec3d(orientation.getDirectionVec()));
					Direction newOrientation = Direction.getFacingFromVector((float)newFront.x, (float)newFront.y,
							(float)newFront.z);
					currentQuadBuilder.setQuadOrientation(newOrientation);
				}

				@Override
				public void setApplyDiffuseLighting(boolean diffuse)
				{
					currentQuadBuilder.setApplyDiffuseLighting(diffuse);
				}

				@Override
				public void setTexture(@Nonnull TextureAtlasSprite texture)
				{
					currentQuadBuilder.setTexture(texture);
				}

				@Override
				public void put(int element, @Nonnull float... data)
				{
					if(element==posPosFinal&&transform!=null)
					{
						Vec3d newPos = transform.apply(new Vec3d(data[0], data[1], data[2]));
						data = new float[3];
						data[0] = (float)newPos.x;
						data[1] = (float)newPos.y;
						data[2] = (float)newPos.z;
					}
					else if(element==normPosFinal&&normalTransform!=null)
					{
						Vec3d newNormal = normalTransform.apply(new Vec3d(data[0], data[1], data[2]));
						data = new float[3];
						data[0] = (float)newNormal.x;
						data[1] = (float)newNormal.y;
						data[2] = (float)newNormal.z;
					}
					else if(element==colorPosFinal)
					{
						if(tintIndex!=-1&&colorTransform!=null)
						{
							int multiplier = colorTransform.apply(tintIndex);
							if(multiplier!=0)
							{
								float r = (float)(multiplier >> 16&255)/255.0F;
								float g = (float)(multiplier >> 8&255)/255.0F;
								float b = (float)(multiplier&255)/255.0F;
								float[] oldData = data;
								data = new float[4];
								data[0] = oldData[0]*r;
								data[1] = oldData[1]*g;
								data[2] = oldData[2]*b;
								data[3] = oldData[3];
							}
						}
					}
					currentQuadBuilder.put(element, data);
				}
			};
		}
	}
}