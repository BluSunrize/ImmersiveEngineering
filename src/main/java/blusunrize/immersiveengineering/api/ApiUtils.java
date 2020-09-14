/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.utils.*;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.api.wires.utils.WirecoilUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.JsonUtils;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.IETags.getIngot;

public class ApiUtils
{
	public static final SetRestrictedField<Consumer<TileEntity>> disableTicking = new SetRestrictedField<>();

	@Deprecated
	public static boolean compareToOreName(ItemStack stack, ResourceLocation oreName)
	{
		return TagUtils.isInBlockOrItemTag(stack, oreName);
	}

	@Deprecated
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		return ItemUtils.stackMatchesObject(stack, o);
	}

	@Deprecated
	public static boolean stackMatchesObject(ItemStack stack, Object o, boolean checkNBT)
	{
		return ItemUtils.stackMatchesObject(stack, o, checkNBT);
	}

	@Deprecated
	public static ItemStack copyStackWithAmount(ItemStack stack, int amount)
	{
		return ItemUtils.copyStackWithAmount(stack, amount);
	}

	@Deprecated
	public static boolean stacksMatchIngredientList(List<Ingredient> list, NonNullList<ItemStack> stacks)
	{
		return IngredientUtils.stacksMatchIngredientList(list, stacks);
	}

	@Deprecated
	public static boolean stacksMatchIngredientWithSizeList(List<IngredientWithSize> list, NonNullList<ItemStack> stacks)
	{
		return IngredientUtils.stacksMatchIngredientWithSizeList(list, stacks);
	}

	@Deprecated
	public static Ingredient createIngredientFromList(List<ItemStack> list)
	{
		return IngredientUtils.createIngredientFromList(list);
	}

	@Deprecated
	public static ComparableItemStack createComparableItemStack(ItemStack stack, boolean copy)
	{
		return ComparableItemStack.create(stack, copy);
	}

	@Deprecated
	public static ComparableItemStack createComparableItemStack(ItemStack stack, boolean copy, boolean useNbt)
	{
		return ComparableItemStack.create(stack, copy, useNbt);
	}

	public static JsonElement jsonSerializeFluidStack(FluidStack fluidStack)
	{
		if(fluidStack==null)
			return JsonNull.INSTANCE;
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("fluid", fluidStack.getFluid().getRegistryName().toString());
		jsonObject.addProperty("amount", fluidStack.getAmount());
		if(fluidStack.hasTag())
			jsonObject.addProperty("tag", fluidStack.getTag().toString());
		return jsonObject;
	}

	public static FluidStack jsonDeserializeFluidStack(JsonObject jsonObject)
	{
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(JSONUtils.getString(jsonObject, "fluid")));
		int amount = JSONUtils.getInt(jsonObject, "amount");
		FluidStack fluidStack = new FluidStack(fluid, amount);
		if(JSONUtils.hasField(jsonObject, "tag"))
			fluidStack.setTag(JsonUtils.readNBT(jsonObject, "tag"));
		return fluidStack;
	}

	@Deprecated
	public static boolean isNonemptyItemTag(ResourceLocation name)
	{
		return TagUtils.isNonemptyItemTag(name);
	}

	@Deprecated
	public static boolean isNonemptyBlockTag(ResourceLocation name)
	{
		return TagUtils.isNonemptyBlockTag(name);
	}

	@Deprecated
	public static boolean isNonemptyBlockOrItemTag(ResourceLocation name)
	{
		return TagUtils.isNonemptyBlockOrItemTag(name);
	}

	@Deprecated
	public static NonNullList<ItemStack> getItemsInTag(ResourceLocation name)
	{
		return TagUtils.getItemsInTag(name);
	}

	@Deprecated
	public static boolean isMetalComponent(ItemStack stack, String componentType)
	{
		return TagUtils.isInPrefixedTag(stack, componentType);
	}

	@Deprecated
	public static String getMetalComponentType(ItemStack stack, String... componentTypes)
	{
		return TagUtils.getMatchingPrefix(stack, componentTypes);
	}

	@Deprecated
	public static Collection<ResourceLocation> getMatchingTagNames(ItemStack stack)
	{
		return TagUtils.getMatchingTagNames(stack);
	}

	@Deprecated
	public static String[] getMetalComponentTypeAndMetal(ItemStack stack, String... componentTypes)
	{
		return TagUtils.getMatchingPrefixAndRemaining(stack, componentTypes);
	}

	@Deprecated
	public static boolean isIngot(ItemStack stack)
	{
		return TagUtils.isIngot(stack);
	}

	@Deprecated
	public static boolean isPlate(ItemStack stack)
	{
		return TagUtils.isPlate(stack);
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

	@Deprecated
	public static LazyOptional<IItemHandler> findItemHandlerAtPos(World world, BlockPos pos, Direction side, boolean allowCart)
	{
		return CapabilityUtils.findItemHandlerAtPos(world, pos, side, allowCart);
	}

	@Deprecated
	public static boolean canInsertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side)
	{
		return CapabilityUtils.canInsertStackIntoInventory(inventory, stack, side);
	}

	@Deprecated
	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side)
	{
		return CapabilityUtils.insertStackIntoInventory(inventory, stack, side);
	}

	@Deprecated
	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side, boolean simulate)
	{
		return CapabilityUtils.insertStackIntoInventory(inventory, stack, side, simulate);
	}

	@Deprecated
	public static BlockPos toBlockPos(Object object)
	{
		return WireUtils.toBlockPos(object);
	}

	@Deprecated
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		return WireUtils.toIIC(object, world);
	}

	@Deprecated
	public static IImmersiveConnectable toIIC(Object object, World world, boolean allowProxies)
	{
		return WireUtils.toIIC(object, world, allowProxies);
	}

	@Deprecated
	public static Vec3d getVecForIICAt(LocalWireNetwork net, ConnectionPoint pos, Connection conn, boolean fromOtherEnd)
	{
		return WireUtils.getVecForIICAt(net, pos, conn, fromOtherEnd);
	}

	public static Vec3d addVectors(Vec3d vec0, Vec3d vec1)
	{
		return vec0.add(vec1.x, vec1.y, vec1.z);
	}

	@Deprecated
	public static Vec3d[] getConnectionCatenary(Vec3d start, Vec3d end, double slack)
	{
		return WireUtils.getConnectionCatenary(start, end, slack);
	}

	public static double getDim(Vec3d vec, int dim)
	{
		return dim==0?vec.x: (dim==1?vec.y: vec.z);
	}

	@Deprecated
	public static BlockPos offsetDim(BlockPos p, int dim, int amount)
	{
		return p.add(dim==0?amount: 0, dim==1?amount: 0, dim==2?amount: 0);
	}

	@Deprecated
	public static Vec3d offsetDim(Vec3d p, int dim, double amount)
	{
		return p.add(dim==0?amount: 0, dim==1?amount: 0, dim==2?amount: 0);
	}

	@Deprecated
	public static void raytraceAlongCatenary(Connection conn, LocalWireNetwork net, Consumer<Triple<BlockPos, Vec3d, Vec3d>> in,
											 Consumer<Triple<BlockPos, Vec3d, Vec3d>> close)
	{
		WireUtils.raytraceAlongCatenary(conn, net, in, close);
	}

	@Deprecated
	public static void raytraceAlongCatenaryRelative(Connection conn, Consumer<Triple<BlockPos, Vec3d, Vec3d>> in,
													 Consumer<Triple<BlockPos, Vec3d, Vec3d>> close, Vec3d vStart,
													 Vec3d vEnd)
	{
		WireUtils.raytraceAlongCatenaryRelative(conn, in, close, vStart, vEnd);
	}

	@Deprecated
	public static void raytraceAlongCatenary(CatenaryData data, BlockPos offset, Consumer<Triple<BlockPos, Vec3d, Vec3d>> in,
											 Consumer<Triple<BlockPos, Vec3d, Vec3d>> close)
	{
		WireUtils.raytraceAlongCatenary(data, offset, in, close);
	}

	@Deprecated
	public static WireType getWireTypeFromNBT(CompoundNBT tag, String key)
	{
		return WireUtils.getWireTypeFromNBT(tag, key);
	}

	@Deprecated
	public static ActionResultType doCoilUse(IWireCoil coil, PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ)
	{
		return WirecoilUtils.doCoilUse(coil, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Deprecated
	public static Set<BlockPos> findObstructingBlocks(World world, Connection conn, Set<BlockPos> ignore)
	{
		return WireUtils.findObstructingBlocks(world, conn, ignore);
	}

	@Deprecated
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

	@Deprecated
	public static boolean hasPlayerIngredient(PlayerEntity player, IngredientWithSize ingredient)
	{
		return IngredientUtils.hasPlayerIngredient(player, ingredient);
	}

	@Deprecated
	public static void consumePlayerIngredient(PlayerEntity player, IngredientWithSize ingredient)
	{
		IngredientUtils.consumePlayerIngredient(player, ingredient);
	}

	public static <T extends Comparable<T>> Map<T, Integer> sortMap(Map<T, Integer> map, boolean inverse)
	{
		TreeMap<T, Integer> sortedMap = new TreeMap<>(new ValueComparator<T>(map, inverse));
		sortedMap.putAll(map);
		return sortedMap;
	}

	public static <T extends TileEntity> void checkForNeedlessTicking(T te, Predicate<T> shouldDisable)
	{
		if(!te.getWorld().isRemote&&shouldDisable.test(te))
			disableTicking.getValue().accept(te);
	}

	@Deprecated
	public static boolean preventsConnection(World worldIn, BlockPos pos, BlockState state, Vec3d a, Vec3d b)
	{
		return WireUtils.preventsConnection(worldIn, pos, state, a, b);
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

	@Deprecated
	public static Connection raytraceWires(World world, Vec3d start, Vec3d end, @Nullable Connection ignored)
	{
		return WireUtils.raytraceWires(world, start, end, ignored);
	}

	@Deprecated
	public static Connection getConnectionMovedThrough(World world, LivingEntity e)
	{
		return WireUtils.getConnectionMovedThrough(world, e);
	}

	@Deprecated
	public static Connection getTargetConnection(World world, PlayerEntity player, Connection ignored, double maxDistance)
	{
		return WireUtils.getTargetConnection(world, player, ignored, maxDistance);
	}

	public static void addFutureServerTask(World world, Runnable task, boolean forceFuture)
	{
		LogicalSide side = world.isRemote?LogicalSide.CLIENT: LogicalSide.SERVER;
		//TODO this sometimes causes NPEs?
		ThreadTaskExecutor<? super TickDelayedTask> tmp = LogicalSidedProvider.WORKQUEUE.get(side);
		if(forceFuture)
		{
			int tick;
			if(world.isRemote)
				tick = 0;
			else
				tick = ((MinecraftServer)tmp).getTickCounter();
			tmp.enqueue(new TickDelayedTask(tick, task));
		}
		else
			tmp.deferTask(task);
	}

	public static void addFutureServerTask(World world, Runnable task)
	{
		addFutureServerTask(world, task, false);
	}

	@Deprecated
	public static void moveConnectionEnd(Connection conn, ConnectionPoint currEnd, ConnectionPoint newEnd, World world)
	{
		WireUtils.moveConnectionEnd(conn, currEnd, newEnd, world);
	}

	@Deprecated
	public static <T> LazyOptional<T> constantOptional(T val)
	{
		return CapabilityUtils.constantOptional(val);
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
	@Deprecated
	public static Function<BakedQuad, BakedQuad> transformQuad(TransformationMatrix transform, Int2IntFunction colorMultiplier)
	{
		return new QuadTransformer(transform, colorMultiplier);
	}
}
