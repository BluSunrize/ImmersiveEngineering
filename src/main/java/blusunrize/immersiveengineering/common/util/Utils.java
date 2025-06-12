/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.Raytracer;
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.fakeworld.TemplateWorld;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.FireworkExplosion.Shape;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;

import static blusunrize.immersiveengineering.api.fluid.FluidUtils.getFluidContained;
import static net.minecraft.core.component.DataComponents.POTION_CONTENTS;

public class Utils
{
	public static final DecimalFormat NUMBERFORMAT_PREFIXED = new DecimalFormat("+#;-#");

	public static final BiMap<TagKey<Item>, DyeColor> DYES_BY_TAG =
			ImmutableBiMap.<TagKey<Item>, DyeColor>builder()
					.put(Tags.Items.DYES_BLACK, DyeColor.BLACK)
					.put(Tags.Items.DYES_RED, DyeColor.RED)
					.put(Tags.Items.DYES_GREEN, DyeColor.GREEN)
					.put(Tags.Items.DYES_BROWN, DyeColor.BROWN)
					.put(Tags.Items.DYES_BLUE, DyeColor.BLUE)
					.put(Tags.Items.DYES_PURPLE, DyeColor.PURPLE)
					.put(Tags.Items.DYES_CYAN, DyeColor.CYAN)
					.put(Tags.Items.DYES_LIGHT_GRAY, DyeColor.LIGHT_GRAY)
					.put(Tags.Items.DYES_GRAY, DyeColor.GRAY)
					.put(Tags.Items.DYES_PINK, DyeColor.PINK)
					.put(Tags.Items.DYES_LIME, DyeColor.LIME)
					.put(Tags.Items.DYES_YELLOW, DyeColor.YELLOW)
					.put(Tags.Items.DYES_LIGHT_BLUE, DyeColor.LIGHT_BLUE)
					.put(Tags.Items.DYES_MAGENTA, DyeColor.MAGENTA)
					.put(Tags.Items.DYES_ORANGE, DyeColor.ORANGE)
					.put(Tags.Items.DYES_WHITE, DyeColor.WHITE)
					.build();

	@Nullable
	public static DyeColor getDye(ItemStack stack)
	{
		if(stack.isEmpty())
			return null;
		if(stack.is(Tags.Items.DYES))
			for(Entry<TagKey<Item>, DyeColor> entry : DYES_BY_TAG.entrySet())
				if(stack.is(entry.getKey()))
					return entry.getValue();
		return null;
	}

	public static boolean isDye(ItemStack stack)
	{
		return stack.is(Tags.Items.DYES);
	}

	public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount, boolean stripPressure)
	{
		FluidStack fs = stack.copyWithAmount(amount);
		if(stripPressure)
			fs.remove(IEApiDataComponents.FLUID_PRESSURIZED);
		return fs;
	}

	private static final long UUID_BASE = 109406000905L;
	private static long UUIDAdd = 1L;

	public static UUID generateNewUUID()
	{
		UUID uuid = new UUID(UUID_BASE, UUIDAdd);
		UUIDAdd++;
		return uuid;
	}

	public static boolean isBlockAt(Level world, BlockPos pos, Block b)
	{
		return world.getBlockState(pos).getBlock()==b;
	}

	public static double generateLuckInfluencedDouble(
			double median, double deviation, double luck, RandomSource rng, boolean isBad, double luckScale
	)
	{
		double number = rng.nextDouble()*deviation;
		if(isBad)
			number = -number;
		number += luckScale*luck;
		if(deviation < 0)
			number = Math.max(number, deviation);
		else
			number = Math.min(number, deviation);
		return median+number;
	}

	public static String formatDouble(double d, String s)
	{
		DecimalFormat df = new DecimalFormat(s);
		return df.format(d);
	}

	public static String toScientificNotation(int value, String decimalPrecision, int useKilo)
	{
		float formatted = value >= 1000000000?value/1000000000f: value >= 1000000?value/1000000f: value >= useKilo?value/1000f: value;
		String notation = value >= 1000000000?"G": value >= 1000000?"M": value >= useKilo?"K": "";
		return formatDouble(formatted, "0."+decimalPrecision)+notation;
	}

	public static String toCamelCase(String s)
	{
		return s.substring(0, 1).toUpperCase(Locale.ENGLISH)+s.substring(1).toLowerCase(Locale.ENGLISH);
	}

	public static String getHarvestLevelName(Tier lvl)
	{
		// TODO this is terrible
		if(lvl instanceof Tiers named)
			return named.name();
		else
			return lvl.toString();
	}

	public static String getModName(String modid)
	{
		return ModList.get().getModContainerById(modid)
				.map(container -> container.getModInfo().getDisplayName())
				.orElse(modid);
	}

	public static <T> int findSequenceInList(List<T> list, T[] sequence, BiPredicate<T, T> equal)
	{
		if(list.size() <= 0||list.size() < sequence.length)
			return -1;

		for(int i = 0; i < list.size(); i++)
			if(equal.test(sequence[0], list.get(i)))
			{
				boolean found = true;
				for(int j = 1; j < sequence.length; j++)
					if(!(found = equal.test(sequence[j], list.get(i+j))))
						break;
				if(found)
					return i;
			}
		return -1;
	}

	public static Direction rotateFacingTowardsDir(Direction f, Direction dir)
	{
		if(dir==Direction.NORTH)
			return f;
		else if(dir==Direction.SOUTH&&f.getAxis()!=Axis.Y)
			return f.getClockWise().getClockWise();
		else if(dir==Direction.WEST&&f.getAxis()!=Axis.Y)
			return f.getCounterClockWise();
		else if(dir==Direction.EAST&&f.getAxis()!=Axis.Y)
			return f.getClockWise();
		else if(dir==Direction.DOWN&&f.getAxis()!=Axis.Y)
			return DirectionUtils.rotateAround(f, Axis.X);
		else if(dir==Direction.UP&&f.getAxis()!=Axis.X)
			return DirectionUtils.rotateAround(f, Axis.X).getOpposite();
		return f;
	}

	public static Vec3 getLivingFrontPos(LivingEntity entity, double offset, double height, HumanoidArm hand, boolean useSteppedYaw, float partialTicks)
	{
		double offsetX = hand==HumanoidArm.LEFT?-.3125: hand==HumanoidArm.RIGHT?.3125: 0;

		float yaw = entity.yRotO+(entity.getYRot()-entity.yRotO)*partialTicks;
		if(useSteppedYaw)
			yaw = entity.yBodyRotO+(entity.yBodyRot-entity.yBodyRotO)*partialTicks;
		float pitch = entity.xRotO+(entity.getXRot()-entity.xRotO)*partialTicks;

		float yawCos = Mth.cos(-yaw*(float)Math.PI/180-(float)Math.PI);
		float yawSin = Mth.sin(-yaw*(float)Math.PI/180-(float)Math.PI);
		float pitchCos = -Mth.cos(-pitch*(float)Math.PI/180);
		float pitchSin = Mth.sin(-pitch*(float)Math.PI/180);

		return new Vec3(entity.getX()+offsetX*yawCos+offset*pitchCos*yawSin, entity.getY()+offset*pitchSin+height, entity.getZ()+offset*pitchCos*yawCos-offsetX*yawSin);
	}

	public static List<LivingEntity> getTargetsInCone(Level world, Vec3 start, Vec3 dir, float spreadAngle, float truncationLength)
	{
		double length = dir.length();
		Vec3 dirNorm = dir.normalize();
		double radius = Math.tan(spreadAngle/2)*length;

		Vec3 endLow = start.add(dir).subtract(radius, radius, radius);
		Vec3 endHigh = start.add(dir).add(radius, radius, radius);

		AABB box = new AABB(minInArray(start.x, endLow.x, endHigh.x), minInArray(start.y, endLow.y, endHigh.y), minInArray(start.z, endLow.z, endHigh.z),
				maxInArray(start.x, endLow.x, endHigh.x), maxInArray(start.y, endLow.y, endHigh.y), maxInArray(start.z, endLow.z, endHigh.z));

		List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, box);
		list.removeIf(e -> !isPointInCone(dirNorm, radius, length, truncationLength, e.position().subtract(start)));
		return list;
	}

	/**
	 * Checks if  point is contained within a cone in 3D space
	 *
	 * @param normDirection    normalized (length==1) vector, direction of cone
	 * @param radius           radius at the end of the cone
	 * @param length           length of the cone
	 * @param truncationLength optional lenght at which the cone is truncated (flat tip)
	 * @param relativePoint    point to be checked, relative to {@code start}
	 */
	public static boolean isPointInCone(Vec3 normDirection, double radius, double length, float truncationLength, Vec3 relativePoint)
	{
		double projectedDist = relativePoint.dot(normDirection); //Orthogonal projection, establishing point's distance on cone direction vector
		if(projectedDist < truncationLength||projectedDist > length) //If projected distance is before truncation or beyond length, point not contained
			return false;

		double radiusAtDist = projectedDist/length*radius; //Radius of the cone at the projected distance
		Vec3 orthVec = relativePoint.subtract(normDirection.scale(projectedDist)); //Orthogonal vector between point and cone direction

		return orthVec.lengthSqr() < (radiusAtDist*radiusAtDist); //Check if Vector's length is shorter than radius -> point in cone
	}

	public static boolean isPointInTriangle(Vec3 tA, Vec3 tB, Vec3 tC, Vec3 point)
	{
		//Distance vectors to A (focuspoint of triangle)
		Vec3 v0 = tC.subtract(tA);
		Vec3 v1 = tB.subtract(tA);
		Vec3 v2 = point.subtract(tA);

		return isPointInTriangle(v0, v1, v2);
	}

	private static boolean isPointInTriangle(Vec3 leg0, Vec3 leg1, Vec3 targetVec)
	{
		//Dot products
		double dot00 = leg0.dot(leg0);
		double dot01 = leg0.dot(leg1);
		double dot02 = leg0.dot(targetVec);
		double dot11 = leg1.dot(leg1);
		double dot12 = leg1.dot(targetVec);

		//Barycentric coordinates
		double invDenom = 1/(dot00*dot11-dot01*dot01);
		double u = (dot11*dot02-dot01*dot12)*invDenom;
		double v = (dot00*dot12-dot01*dot02)*invDenom;

		return (u >= 0)&&(v >= 0)&&(u+v < 1);
	}

	public static void attractEnemies(LivingEntity target, float radius)
	{
		attractEnemies(target, radius, null);
	}

	public static void attractEnemies(LivingEntity target, float radius, Predicate<Monster> predicate)
	{
		AABB aabb = new AABB(target.getX()-radius, target.getY()-radius, target.getZ()-radius, target.getX()+radius, target.getY()+radius, target.getZ()+radius);

		List<Monster> list = target.getCommandSenderWorld().getEntitiesOfClass(Monster.class, aabb);
		for(Monster mob : list)
			if(predicate==null||predicate.test(mob))
			{
				mob.setTarget(target);
				mob.lookAt(target, 180, 0);
			}
	}

	public static boolean isHammer(ItemStack stack)
	{
		return stack.is(IETags.hammers);
	}

	public static boolean isScrewdriver(ItemStack stack)
	{
		return stack.is(IETags.screwdrivers);
	}

	public static boolean canBlockDamageSource(LivingEntity entity, DamageSource damageSourceIn)
	{
		if(!damageSourceIn.is(DamageTypeTags.BYPASSES_ARMOR)&&entity.isBlocking())
		{
			Vec3 vec3d = damageSourceIn.getSourcePosition();
			if(vec3d!=null)
			{
				Vec3 vec3d1 = entity.getViewVector(1.0F);
				Vec3 vec3d2 = vec3d.vectorTo(entity.position()).normalize();
				vec3d2 = new Vec3(vec3d2.x, 0.0D, vec3d2.z);
				return vec3d2.dot(vec3d1) < 0;
			}
		}
		return false;
	}

	public static Vec3 getScaledFlowVector(Level world, BlockPos pos)
	{
		BlockState bState = world.getBlockState(pos);
		FluidState fState = bState.getFluidState();
		return fState.getFlow(world, pos).scale(fState.getOwnHeight());
	}

	public static double minInArray(double... f)
	{
		if(f.length < 1)
			return 0;
		double min = f[0];
		for(int i = 1; i < f.length; i++)
			min = Math.min(min, f[i]);
		return min;
	}

	public static double maxInArray(double... f)
	{
		if(f.length < 1)
			return 0;
		double max = f[0];
		for(int i = 1; i < f.length; i++)
			max = Math.max(max, f[i]);
		return max;
	}

	public static boolean isVecInEntityHead(LivingEntity entity, Vec3 vec)
	{
		if(entity.getBbHeight()/entity.getBbWidth() < 2)//Crude check to see if the entity is bipedal or at least upright (this should work for blazes)
			return false;
		double d = vec.y-(entity.getY()+entity.getEyeHeight());
		return Math.abs(d) < .25;
	}

	public static boolean hasIEAdvancement(Player player, String name)
	{
		if(player instanceof ServerPlayer)
		{
			PlayerAdvancements advancements = ((ServerPlayer)player).getAdvancements();
			ServerAdvancementManager manager = ((ServerLevel)player.getCommandSenderWorld()).getServer().getAdvancements();
			AdvancementHolder advancement = manager.get(IEApi.ieLoc(name));
			if(advancement!=null)
				return advancements.getOrStartProgress(advancement).isDone();
		}
		return false;
	}
	public static void unlockIEAdvancement(Player player, String name)
	{
		if(player instanceof ServerPlayer)
		{
			PlayerAdvancements advancements = ((ServerPlayer)player).getAdvancements();
			ServerAdvancementManager manager = ((ServerLevel)player.getCommandSenderWorld()).getServer().getAdvancements();
			AdvancementHolder advancement = manager.get(IEApi.ieLoc(name));
			if(advancement!=null)
				advancements.award(advancement, "code_trigger");
		}
	}

	public static List<FireworkExplosion> getRandomFireworkExplosion(Random rand)
	{
		int[] colors = new int[rand.nextInt(8)+1];
		for(int i = 0; i < colors.length; i++)
		{
			int j = rand.nextInt(11)+1;
			//no black, brown, light grey, grey or white
			if(j > 2)
				j++;
			if(j > 6)
				j += 2;
			colors[i] = DyeColor.byId(j).getFireworkColor();
		}
		return List.of(new FireworkExplosion(
				Shape.values()[rand.nextInt(Shape.values().length)],
				IntList.of(colors),
				IntList.of(colors),
				true,
				true
		));
	}

	public static int intFromRGBA(Vector4f rgba)
	{
		float[] array = {
				rgba.x(),
				rgba.y(),
				rgba.z(),
				rgba.w(),
		};
		return intFromRGBA(array);
	}

	public static int intFromRGBA(float[] rgba)
	{
		int ret = (int)(255*rgba[3]);
		ret = (ret<<8)+(int)(255*rgba[0]);
		ret = (ret<<8)+(int)(255*rgba[1]);
		ret = (ret<<8)+(int)(255*rgba[2]);
		return ret;
	}

	public static Vector4f vec4fFromDye(DyeColor dyeColor)
	{
		if(dyeColor==null)
			return new Vector4f(1, 1, 1, 1);
		int rgb = dyeColor.getTextureDiffuseColor();
		return new Vector4f(((rgb>>16)&255)/255f, ((rgb>>8)&255)/255f, (rgb&255)/255f, 1);
	}

	public static Vector4f vec4fFromInt(int argb)
	{
		return new Vector4f(
				((argb >> 16)&255)/255f,
				((argb >> 8)&255)/255f,
				(argb&255)/255f,
				((argb >> 24)&255)/255f
		);
	}

	public static FluidStack drainFluidBlock(Level world, BlockPos pos, FluidAction action)
	{
		BlockState b = world.getBlockState(pos);
		FluidState f = b.getFluidState();

		if(f.isSource()&&b.getBlock() instanceof BucketPickup bucketPickup)
		{
			if(action.execute())
				bucketPickup.pickupBlock(null, world, pos, b);
			return new FluidStack(f.getType(), FluidType.BUCKET_VOLUME);
		}
		return FluidStack.EMPTY;
	}

	public static Fluid getRelatedFluid(Level w, BlockPos pos)
	{
		return w.getFluidState(pos).getType();
	}

	//Stolen from BucketItem
	public static boolean placeFluidBlock(Level worldIn, BlockPos posIn, FluidStack fluidStack)
	{
		Fluid fluid = fluidStack.getFluid();
		if(!(fluid instanceof FlowingFluid)||fluidStack.getAmount() < FluidType.BUCKET_VOLUME)
			return false;
		else
		{
			BlockState blockstate = worldIn.getBlockState(posIn);
			boolean flag = !blockstate.isSolid();
			boolean flag1 = blockstate.canBeReplaced();
			if(worldIn.isEmptyBlock(posIn)||flag||flag1||blockstate.getBlock() instanceof LiquidBlockContainer&&((LiquidBlockContainer)blockstate.getBlock()).canPlaceLiquid(null, worldIn, posIn, blockstate, fluid))
			{
				if(worldIn.dimensionType().ultraWarm()&&fluid.is(FluidTags.WATER))
				{
					int i = posIn.getX();
					int j = posIn.getY();
					int k = posIn.getZ();
					worldIn.playSound(null, posIn, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F+(worldIn.random.nextFloat()-worldIn.random.nextFloat())*0.8F);

					for(int l = 0; l < 8; ++l)
						worldIn.addParticle(ParticleTypes.LARGE_SMOKE, i+Math.random(), j+Math.random(), k+Math.random(), 0.0D, 0.0D, 0.0D);
				}
				else if(blockstate.getBlock() instanceof LiquidBlockContainer&&fluid==Fluids.WATER)
					((LiquidBlockContainer)blockstate.getBlock()).placeLiquid(worldIn, posIn, blockstate, ((FlowingFluid)fluid).getSource(false));
				else
				{
					if(!worldIn.isClientSide&&(flag||flag1)&&!blockstate.liquid())
						worldIn.destroyBlock(posIn, true);

					worldIn.setBlock(posIn, fluid.defaultFluidState().createLegacyBlock(), 11);
				}
				fluidStack.shrink(FluidType.BUCKET_VOLUME);
				return true;
			}
			else
				return false;
		}
	}

	public static BlockState getStateFromItemStack(ItemStack stack)
	{
		if(stack.isEmpty())
			return null;
		Block block = Block.byItem(stack.getItem());
		if(block!=Blocks.AIR)
			return block.defaultBlockState();
		return null;
	}

	public static ItemStack insertStackIntoInventory(IEBlockCapabilityCache<IItemHandler> ref, ItemStack stack, boolean simulate)
	{
		return insertStackIntoInventory(ref.getCapability(), stack, simulate);
	}

	public static ItemStack insertStackIntoInventory(Supplier<@Nullable IItemHandler> ref, ItemStack stack, boolean simulate)
	{
		return insertStackIntoInventory(ref.get(), stack, simulate);
	}

	private static ItemStack insertStackIntoInventory(IItemHandler handler, ItemStack stack, boolean simulate)
	{
		if(handler!=null&&!stack.isEmpty())
			return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
		else
			return stack;
	}


	public static void dropStackAtPos(Level world, DirectionalBlockPos pos, ItemStack stack)
	{
		dropStackAtPos(world, pos.position(), stack, pos.side());
	}

	public static void dropStackAtPos(Level world, BlockPos pos, ItemStack stack, @Nonnull Direction facing)
	{

		if(!stack.isEmpty())
		{
			ItemEntity ei = new ItemEntity(world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, stack.copy());
			ei.setDeltaMovement(0.075*facing.getStepX(), 0.025, 0.075*facing.getStepZ());
			world.addFreshEntity(ei);
		}
	}

	public static boolean isFluidRelatedItemStack(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		return stack.getCapability(FluidHandler.ITEM)!=null;
	}

	public static Optional<RecipeHolder<CraftingRecipe>> findCraftingRecipe(CraftingInput crafting, Level world)
	{
		return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, world);
	}

	public static NonNullList<ItemStack> createNonNullItemStackListFromItemStack(ItemStack stack)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(1, ItemStack.EMPTY);
		list.set(0, stack);
		return list;
	}

	public static boolean isVecInBlock(Vec3 vec3d, BlockPos pos, BlockPos offset, double eps)
	{
		return vec3d.x >= pos.getX()-offset.getX()-eps&&
				vec3d.x <= pos.getX()-offset.getX()+1+eps&&
				vec3d.y >= pos.getY()-offset.getY()-eps&&
				vec3d.y <= pos.getY()-offset.getY()+1+eps&&
				vec3d.z >= pos.getZ()-offset.getZ()-eps&&
				vec3d.z <= pos.getZ()-offset.getZ()+1+eps;
	}

	public static Vec3 withCoordinate(Vec3 vertex, Axis axis, double value)
	{
		switch(axis)
		{
			case X:
				return new Vec3(value, vertex.y, vertex.z);
			case Y:
				return new Vec3(vertex.x, value, vertex.z);
			case Z:
				return new Vec3(vertex.x, vertex.y, value);
		}
		return vertex;
	}

	public static BlockPos rayTraceForFirst(Vec3 start, Vec3 end, Level w, Set<BlockPos> ignore)
	{
		Set<BlockPos> trace = Raytracer.rayTrace(start, end, w);
		for(BlockPos cc : ignore)
			trace.remove(cc);
		if(start.x!=end.x)
			trace = findMinOrMax(trace, start.x > end.x, 0);
		if(start.y!=end.y)
			trace = findMinOrMax(trace, start.y > end.y, 0);
		if(start.z!=end.z)
			trace = findMinOrMax(trace, start.z > end.z, 0);
		if(trace.size() > 0)
			return trace.iterator().next();
		return null;
	}

	public static Set<BlockPos> findMinOrMax(Set<BlockPos> in, boolean max, int coord)
	{
		Set<BlockPos> ret = new HashSet<>();
		int currMinMax = max?Integer.MIN_VALUE: Integer.MAX_VALUE;
		//find minimum
		for(BlockPos cc : in)
		{
			int curr = (coord==0?cc.getX(): (coord==1?cc.getY(): cc.getY()));
			if(max^(curr < currMinMax))
				currMinMax = curr;
		}
		//fill ret set
		for(BlockPos cc : in)
		{
			int curr = (coord==0?cc.getX(): (coord==1?cc.getY(): cc.getZ()));
			if(curr==currMinMax)
				ret.add(cc);
		}
		return ret;
	}

	/**
	 * get tile entity without loading currently unloaded chunks
	 *
	 * @return return value of {@link Level#getBlockEntity(BlockPos)} or always null if chunk is not loaded
	 */
	// TODO change this to use SafeChunkUtils
	public static BlockEntity getExistingTileEntity(Level world, BlockPos pos)
	{
		if(world==null)
			return null;
		if(world.hasChunkAt(pos))
			return world.getBlockEntity(pos);
		return null;
	}

	public static void modifyInvStackSize(NonNullList<ItemStack> inv, int slot, int amount)
	{
		if(slot >= 0&&slot < inv.size()&&!inv.get(slot).isEmpty())
		{
			inv.get(slot).grow(amount);
			if(inv.get(slot).getCount() <= 0)
				inv.set(slot, ItemStack.EMPTY);
		}
	}

	@Deprecated
	public static int calcRedstoneFromInventory(IIEInventory inv)
	{
		if(inv==null||inv.getInventory()==null)
			return 0;
		else
			return calcRedstoneFromInventory(inv.getComparatedSize(), inv.getInventory()::get, inv::getSlotLimit);
	}

	public static int calcRedstoneFromInventory(int maxSlot, IItemHandler inv)
	{
		return calcRedstoneFromInventory(maxSlot, inv::getStackInSlot, inv::getSlotLimit);
	}

	private static int calcRedstoneFromInventory(int maxSlot, IntFunction<ItemStack> getStack, Int2IntFunction getSlotLimit)
	{
		int i = 0;
		float f = 0.0F;
		for(int j = 0; j < maxSlot; ++j)
		{
			ItemStack itemstack = getStack.apply(j);
			if(!itemstack.isEmpty())
			{
				f += (float)itemstack.getCount()/(float)Math.min(getSlotLimit.get(j), itemstack.getMaxStackSize());
				++i;
			}
		}
		f = f/(float)maxSlot;
		return Mth.floor(f*14.0F)+(i > 0?1: 0);
	}

	public static void getDrops(BlockState state, LootContext originalCtx, Consumer<ItemStack> out)
	{
		ResourceKey<LootTable> lootKey = state.getBlock().getLootTable();
		if(lootKey==BuiltInLootTables.EMPTY)
			return;
		LootParams lootcontext = new LootParams.Builder(originalCtx.getLevel())
				.withOptionalParameter(LootContextParams.TOOL, originalCtx.getParamOrNull(LootContextParams.TOOL))
				.withOptionalParameter(LootContextParams.ORIGIN, originalCtx.getParamOrNull(LootContextParams.ORIGIN))
				.withParameter(LootContextParams.BLOCK_STATE, state)
				.create(LootContextParamSets.BLOCK);
		ServerLevel serverworld = lootcontext.getLevel();
		LootTable loottable = serverworld.getServer().reloadableRegistries().getLootTable(lootKey);
		loottable.getRandomItems(lootcontext, out);
	}

	public static ItemStack getPickBlock(BlockState state, HitResult rtr, @Nullable Player player)
	{
		if(player==null)
			return ItemStack.EMPTY;
		final RegistryAccess registries = player.level().registryAccess();
		final LevelReader w = TemplateWorld.createSingleBlock(state, registries);
		return state.getBlock().getCloneItemStack(state, rtr, w, BlockPos.ZERO, player);
	}

	public static ItemStack getPickBlock(BlockState state)
	{
		return getPickBlock(
				state, new BlockHitResult(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO, false),
				ImmersiveEngineering.proxy.getClientPlayer()
		);
	}

	public static List<AABB> flipBoxes(boolean flipFront, boolean flipRight, List<AABB> boxes)
	{
		return flipBoxes(flipFront, flipRight, boxes.toArray(new AABB[0]));
	}

	public static List<AABB> flipBoxes(boolean flipFront, boolean flipRight, AABB... boxes)
	{
		List<AABB> ret = new ArrayList<>(boxes.length);
		for(AABB aabb : boxes)
			ret.add(flipBox(flipFront, flipRight, aabb));
		return ret;
	}

	public static AABB flipBox(boolean flipFront, boolean flipRight, AABB aabb)
	{
		AABB result = aabb;
		if(flipRight)
			result = new AABB(1-result.maxX, result.minY, result.minZ, 1-result.minX, result.maxY, result.maxZ);
		if(flipFront)
			result = new AABB(result.minX, result.minY, 1-result.maxZ, result.maxX, result.maxY, 1-result.minZ);
		return result;
	}

	public static FluidStack deriveFluidStack(@Nonnull ItemStack stack)
	{
		{
			var potionContents = stack.get(POTION_CONTENTS);
			return (potionContents==null)?
					getFluidContained(stack).orElse(FluidStack.EMPTY):
					PotionFluid.getFluidStackForType(potionContents.potion(), 250, PotionFluid.PotionBottleType.fromItem(stack.getItemHolder()));
		}
	}
}
