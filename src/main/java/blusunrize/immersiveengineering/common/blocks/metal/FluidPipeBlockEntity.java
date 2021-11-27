/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock.IELadderBlock;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.WorldMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.FORGE)
public class FluidPipeBlockEntity extends IEBaseBlockEntity implements IFluidPipe, IColouredBE, IPlayerInteraction,
		IHammerInteraction, IPlacementInteraction, ISelectionBounds, ICollisionBounds, IAdditionalDrops
{
	static WorldMap<BlockPos, Set<DirectionalFluidOutput>> indirectConnections = new WorldMap<>();
	public static ArrayList<Predicate<Block>> validPipeCovers = new ArrayList<>();
	public static ArrayList<Predicate<Block>> climbablePipeCovers = new ArrayList<>();

	public FluidPipeBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FLUID_PIPE.get(), pos, state);
	}

	public static void initCovers()
	{
		validPipeCovers.add(IETags.scaffoldingAlu::contains);
		validPipeCovers.add(IETags.scaffoldingSteel::contains);
		validPipeCovers.add(input -> input==WoodenDecoration.TREATED_SCAFFOLDING.get());

		climbablePipeCovers.add(IETags.scaffoldingAlu::contains);
		climbablePipeCovers.add(IETags.scaffoldingSteel::contains);
		climbablePipeCovers.add(input -> input==WoodenDecoration.TREATED_SCAFFOLDING.get());
	}

	public Object2BooleanMap<Direction> sideConfig = new Object2BooleanOpenHashMap<>();

	{
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, true);
	}

	public Block cover = Blocks.AIR;
	private byte connections = 0;
	@Nullable
	private DyeColor color = null;

	public static Set<DirectionalFluidOutput> getConnectedFluidHandlers(BlockPos node, Level world)
	{
		if(world.isClientSide)
			return ImmutableSet.of();
		Set<DirectionalFluidOutput> cachedResult = indirectConnections.get(world, node);
		if(cachedResult!=null)
			return cachedResult;

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		Set<DirectionalFluidOutput> fluidHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());
		openList.add(node);
		while(!openList.isEmpty()&&closedList.size() < 1024)
		{
			BlockPos next = openList.get(0);
			BlockEntity pipeTile = Utils.getExistingTileEntity(world, next);
			if(!closedList.contains(next)&&(pipeTile instanceof IFluidPipe))
			{
				if(pipeTile instanceof FluidPipeBlockEntity)
					closedList.add(next);
				for(Direction fd : DirectionUtils.VALUES)
					if(((IFluidPipe)pipeTile).hasOutputConnection(fd))
					{
						BlockPos nextPos = next.relative(fd);
						BlockEntity adjacentTile = Utils.getExistingTileEntity(world, nextPos);
						if(adjacentTile!=null)
							if(adjacentTile instanceof FluidPipeBlockEntity)
								openList.add(nextPos);
							else
							{
								LazyOptional<IFluidHandler> handlerOptional = adjacentTile.getCapability(
										CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fd.getOpposite());
								handlerOptional.ifPresent(handler ->
								{
									if(handler.getTanks() > 0)
										fluidHandlers.add(new DirectionalFluidOutput(handler, adjacentTile, fd));
								});
							}
					}
			}
			openList.remove(0);
		}
		indirectConnections.put(world, node, fluidHandlers);
		return fluidHandlers;
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		if(level!=null&&!level.isClientSide)
			EventHandler.SERVER_TASKS.add(() -> {
				boolean changed = false;
				for(Direction f : DirectionUtils.VALUES)
					changed |= updateConnectionByte(f);
				if(changed)
				{
					level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
					markContainingBlockForUpdate(null);
				}
			});
	}

	@Override
	public void setRemovedIE()
	{
		super.setRemovedIE();
		if(level!=null&&!level.isClientSide)
			indirectConnections.clearDimension(level);
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(entity instanceof LivingEntity&&!((LivingEntity)entity).onClimbable()&&this.cover!=Blocks.AIR)
		{
			boolean climb = false;
			for(Predicate<Block> f : climbablePipeCovers)
				if(f!=null&&f.test(cover))
				{
					climb = true;
					break;
				}
			if(climb)
				IELadderBlock.applyLadderLogic(entity);
		}
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int[] config = nbt.getIntArray("sideConfig");
		for(int i = 0; i < 6; ++i)
		{
			Direction curDir = Direction.from3DDataValue(i);
			if(i < config.length)
			{
				boolean connected = config[i]!=0;
				sideConfig.put(curDir, connected);
				if(connected)
					setValidHandler(curDir);
				else
					invalidateHandler(curDir);
			}
			else
			{
				sideConfig.put(curDir, false);
				invalidateHandler(curDir);
			}
		}
		cover = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("cover")));
		DyeColor oldColor = color;
		if(nbt.contains("color", NBT.TAG_INT))
			color = DyeColor.byId(nbt.getInt("color"));
		else
			color = null;
		byte oldConns = connections;
		connections = nbt.getByte("connections");
		if(level!=null&&level.isClientSide&&(connections!=oldConns||color!=oldColor))
		{
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int[] config = new int[6];
		for(int i = 0; i < 6; ++i)
			if(sideConfig.getBoolean(Direction.from3DDataValue(i)))
				config[i] = 1;
		nbt.putIntArray("sideConfig", config);
		if(hasCover())
			nbt.putString("cover", ForgeRegistries.BLOCKS.getKey(cover).toString());
		nbt.putByte("connections", connections);
		if(color!=null)
			nbt.putInt("color", color.getId());
	}

	boolean canOutputPressurized(BlockEntity output, boolean consumePower)
	{
		if(output instanceof IFluidPipe)
			return ((IFluidPipe)output).canOutputPressurized(consumePower);
		return false;
	}

	private final Map<Direction, ResettableCapability<IFluidHandler>> sidedHandlers = new EnumMap<>(Direction.class);
	private final Map<Direction, CapabilityReference<IFluidHandler>> neighbors = CapabilityReference.forAllNeighbors(
			this, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
	);

	{
		for(Direction f : DirectionUtils.VALUES)
			sidedHandlers.put(f, registerCapability(new PipeFluidHandler(this, f)));
	}

	private void invalidateHandler(Direction side)
	{
		ResettableCapability<IFluidHandler> handler = sidedHandlers.get(side);
		if(handler!=null)
		{
			sidedHandlers.put(side, null);
			handler.reset();
		}
	}

	private void setValidHandler(Direction side)
	{
		ResettableCapability<IFluidHandler> handler = sidedHandlers.get(side);
		if(handler==null)
			sidedHandlers.put(side, registerCapability(new PipeFluidHandler(this, side)));
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null&&sideConfig.getBoolean(facing))
			return sidedHandlers.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	protected boolean hasCover()
	{
		return this.cover!=Blocks.AIR;
	}

	@Override
	public Collection<ItemStack> getExtraDrops(Player player, BlockState state)
	{
		if(hasCover())
			return Lists.newArrayList(new ItemStack(cover));
		return null;
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		Direction dir = Direction.getNearest(otherPos.getX()-worldPosition.getX(),
				otherPos.getY()-worldPosition.getY(), otherPos.getZ()-worldPosition.getZ());
		if(updateConnectionByte(dir))
		{
			Level world = getLevelNonnull();
			world.updateNeighborsAtExceptFromFacing(worldPosition, getBlockState().getBlock(), dir);
			markContainingBlockForUpdate(null);
			if(!world.isClientSide)
				indirectConnections.clearDimension(world);
		}
	}

	static class PipeFluidHandler implements IFluidHandler
	{
		private static final Random CURRENT_TICK_RANDOM = new Random();

		FluidPipeBlockEntity pipe;
		Direction facing;

		public PipeFluidHandler(FluidPipeBlockEntity pipe, Direction facing)
		{
			this.pipe = pipe;
			this.facing = facing;
		}

		@Override
		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return FluidAttributes.BUCKET_VOLUME;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return tank==0;
		}

		@Override
		public int fill(FluidStack resource, FluidAction doFill)
		{
			if(resource==null)
				return 0;
			int canAccept = resource.getAmount();
			if(canAccept <= 0)
				return 0;
			Set<DirectionalFluidOutput> outputList = getConnectedFluidHandlers(pipe.getBlockPos(), pipe.level);

			if(outputList.size() < 1)
				//NO OUTPUTS!
				return 0;
			BlockPos ccFrom = new BlockPos(pipe.getBlockPos().relative(facing));
			int sum = 0;
			HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
			for(DirectionalFluidOutput output : outputList)
			{
				BlockPos cc = output.containingTile.getBlockPos();
				if(!cc.equals(ccFrom)&&pipe.level.hasChunkAt(cc)&&!pipe.equals(output.containingTile))
				{
					int limit = getTranferrableAmount(resource, output);
					int tileSpecificAcceptedFluid = Math.min(limit, canAccept);
					int temp = output.output.fill(Utils.copyFluidStackWithAmount(resource, tileSpecificAcceptedFluid, true), FluidAction.SIMULATE);
					if(temp > 0)
					{
						sorting.put(output, temp);
						sum += temp;
					}
				}
			}
			if(sum > 0)
			{
				int f = 0;
				for(DirectionalFluidOutput output : sorting.keySet())
				{
					int amount = sorting.get(output);
					if(sum > resource.getAmount())
					{
						int limit = getTranferrableAmount(resource, output);
						int tileSpecificAcceptedFluid = Math.min(limit, canAccept);
						float prio = amount/(float)sum;
						amount = (int)Math.ceil(Mth.clamp(amount, 1,
								Math.min(resource.getAmount()*prio, tileSpecificAcceptedFluid)));
						amount = Math.min(amount, canAccept);
					}
					int r = output.output.fill(Utils.copyFluidStackWithAmount(resource, amount, true), doFill);
					if(r > 50)
						pipe.canOutputPressurized(output.containingTile, true);
					f += r;
					canAccept -= r;
					if(canAccept <= 0)
						break;
				}
				return f;
			}
			return 0;
		}

		private int getTranferrableAmount(FluidStack resource, DirectionalFluidOutput output)
		{
			return (resource.hasTag()&&resource.getOrCreateTag().contains("pressurized"))||
					pipe.canOutputPressurized(output.containingTile, false)
					?FluidAttributes.BUCKET_VOLUME: FluidAttributes.BUCKET_VOLUME/20;
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			return this.drain(resource.getAmount(), doDrain);
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(maxDrain <= 0)
				return FluidStack.EMPTY;

			Level world = pipe.getLevelNonnull();
			List<DirectionalFluidOutput> outputList = new ArrayList<>(getConnectedFluidHandlers(pipe.getBlockPos(), world));
			BlockPos ccFrom = new BlockPos(pipe.getBlockPos().relative(facing));
			outputList.removeIf(output -> ccFrom.equals(output.containingTile.getBlockPos()));

			if(outputList.size() < 1)
				return FluidStack.EMPTY;

			CURRENT_TICK_RANDOM.setSeed(HashCommon.mix(world.getGameTime()));
			int chosen = outputList.size()==1?0: CURRENT_TICK_RANDOM.nextInt(outputList.size());
			DirectionalFluidOutput output = outputList.get(chosen);
			FluidStack available = output.output.drain(maxDrain, FluidAction.SIMULATE);
			int limit = getTranferrableAmount(available, output);
			int actualTake = Math.min(limit, maxDrain);
			return output.output.drain(actualTake, doDrain);
		}
	}

	public static class DirectionalFluidOutput
	{
		IFluidHandler output;
		Direction direction;
		BlockEntity containingTile;

		public DirectionalFluidOutput(IFluidHandler output, BlockEntity containingTile, Direction direction)
		{
			this.output = output;
			this.direction = direction;
			this.containingTile = containingTile;
		}
	}

	public boolean updateConnectionByte(Direction dir)
	{
		if(level==null||level.isClientSide||!SafeChunkUtils.isChunkSafe(level, worldPosition.relative(dir)))
			return false;
		final byte oldConn = connections;
		int i = dir.get3DDataValue();
		int mask = 1<<i;
		connections &= ~mask;
		if(sideConfig.getBoolean(dir))
		{
			IFluidHandler handler = neighbors.get(dir).getNullable();
			if(handler!=null&&handler.getTanks() > 0)
				connections |= mask;
		}
		return oldConn!=connections;
	}

	public byte getAvailableConnectionByte()
	{
		byte availableConnections = connections;
		int mask = 1;
		for(Direction dir : DirectionUtils.VALUES)
		{
			if((availableConnections&mask)==0)
			{
				if(level.getBlockEntity(getBlockPos().relative(dir)) instanceof FluidPipeBlockEntity)
					availableConnections |= mask;
				else
				{
					IFluidHandler handler = neighbors.get(dir).getNullable();
					if(handler!=null&&handler.getTanks() > 0)
						availableConnections |= mask;
				}
			}
			mask <<= 1;
		}
		return availableConnections;
	}

	public ConnectionStyle getConnectionStyle(Direction connection)
	{
		if((connections&(1<<connection.get3DDataValue()))==0)
			return ConnectionStyle.NO_CONNECTION;

		if(connections!=3&&connections!=12&&connections!=48) //add flange if not a straight pipe
			return ConnectionStyle.FLANGE;
		BlockEntity con = Utils.getExistingTileEntity(level, getBlockPos().relative(connection));
		if(con instanceof FluidPipeBlockEntity pipe)
		{
			int tileConnections = pipe.connections|(1<<connection.getOpposite().get3DDataValue());
			if(connections==tileConnections) //if neighbor pipe is also straight and in same direction, don't add flanges
				return ConnectionStyle.PLAIN;
		}
		return ConnectionStyle.FLANGE;
	}

	public void toggleSide(Direction side)
	{
		boolean newSideConnected = !sideConfig.getBoolean(side);
		setSide(side, newSideConnected);
	}

	public void setSide(Direction side, boolean connectable)
	{
		setSide(side, connectable, true);
	}

	public void setSide(Direction side, boolean connectable, boolean firstPipe)
	{
		sideConfig.put(side, connectable);
		if(connectable)
			setValidHandler(side);
		else
			invalidateHandler(side);
		setChanged();
		if(firstPipe)
		{
			BlockEntity neighborTile = level.getBlockEntity(getBlockPos().relative(side));
			if(neighborTile instanceof FluidPipeBlockEntity)
				((FluidPipeBlockEntity)neighborTile).setSide(side.getOpposite(), connectable, false);
			updateConnectionByte(side); //yes, this is not meant for neighborTile
		}
		level.blockEvent(getBlockPos(), getBlockState().getBlock(), 0, 0);
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(FluidPipeBlockEntity::getBoxes);

	@Override
	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		return SHAPES.get(new BoundingBoxKey(false, this));
	}

	@Override
	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		//TODO needs to be a more generic check!
		boolean hammer = ctx!=null&&ctx.isHoldingItem(Tools.HAMMER.get());
		return SHAPES.get(new BoundingBoxKey(hammer, this));
	}

	private static List<AABB> getBoxes(BoundingBoxKey key)
	{
		List<AABB> list = Lists.newArrayList();
		if(!key.showToolView&&key.hasCover)
		{
			list.add(new AABB(0, 0, 0, 1, 1, 1).inflate(-.03125f));
			return list;
		}
		byte availableConnections = key.availableConnections;
		byte activeConnections = key.connections;
		double[] baseAABB = key.hasCover?new double[]{.002, .998, .002, .998, .002, .998}: new double[]{.25, .75, .25, .75, .25, .75};
		for(Direction d : DirectionUtils.VALUES)
		{
			int i = d.get3DDataValue();
			if((availableConnections&1)==1)
			{
				if((activeConnections&1)==1||key.showToolView)
				{
					list.add(new AABB(
							i==4?0: i==5?0.75: 0.25, i==0?0: i==1?0.75: 0.25, i==2?0: i==3?0.75: 0.25,
							i==4?0.25: i==5?1: 0.75, i==0?0.25: i==1?1: 0.75, i==2?0.25: i==3?1: 0.75
					));
					if(key.connectionStyles.get(d)==ConnectionStyle.FLANGE)
						list.add(new AABB(
								i==4?0: i==5?0.875: 0.125, i==0?0: i==1?0.875: 0.125, i==2?0: i==3?0.875: 0.125,
								i==4?0.125: i==5?1: 0.875, i==0?0.125: i==1?1: 0.875, i==2?0.125: i==3?1: 0.875
						));
				}
			}
			availableConnections = (byte)(availableConnections >> 1);
			activeConnections = (byte)(activeConnections >> 1);
		}
		list.add(new AABB(baseAABB[4], baseAABB[0], baseAABB[2], baseAABB[5], baseAABB[1], baseAABB[3]));
		return list;
	}

	private static class BoundingBoxKey
	{
		private final boolean showToolView;
		private final byte connections;
		private final byte availableConnections;
		private final boolean hasCover;
		private final Map<Direction, ConnectionStyle> connectionStyles = new EnumMap<>(Direction.class);

		private BoundingBoxKey(boolean showToolView, FluidPipeBlockEntity te)
		{
			this.showToolView = showToolView;
			this.connections = te.connections;
			this.availableConnections = te.getAvailableConnectionByte();
			this.hasCover = te.hasCover();
			for(Direction d : DirectionUtils.VALUES)
				connectionStyles.put(d, te.getConnectionStyle(d));
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			BoundingBoxKey that = (BoundingBoxKey)o;
			return showToolView==that.showToolView&&
					connections==that.connections&&
					availableConnections==that.availableConnections&&
					hasCover==that.hasCover&&
					connectionStyles.equals(that.connectionStyles);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(showToolView, connections, availableConnections, hasCover, connectionStyles);
		}

	}

	@Override
	public int getRenderColour(int tintIndex)
	{
		return 0xffffff;
	}

	public void dropCover(Player player)
	{
		if(!level.isClientSide&&hasCover()&&level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
		{
			ItemEntity entityitem = player.drop(new ItemStack(cover), false);
			if(entityitem!=null)
				entityitem.setNoPickUpDelay();
		}
	}

	@Override
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem.isEmpty()&&player.isShiftKeyDown()&&hasCover())
		{
			dropCover(player);
			this.cover = Blocks.AIR;
			this.markContainingBlockForUpdate(null);
			level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
			return true;
		}
		else if(!heldItem.isEmpty()&&!player.isShiftKeyDown())
		{
			Block heldBlock = Block.byItem(heldItem.getItem());
			if(heldBlock!=Blocks.AIR)
				for(Predicate<Block> func : validPipeCovers)
					if(func.test(heldBlock))
					{
						if(this.cover!=heldBlock)
						{
							dropCover(player);
							this.cover = heldBlock;
							heldItem.shrink(1);
							this.markContainingBlockForUpdate(null);
							level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
							return true;
						}
					}
			DyeColor heldDye = Utils.getDye(heldItem);
			if(heldDye!=null)
			{
				color = heldDye;
				markContainingBlockForUpdate(null);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide)
			return true;
		hitVec = hitVec.subtract(Vec3.atLowerCornerOf(worldPosition));
		Direction fd = side;
		List<AABB> boxes = getBoxes(new BoundingBoxKey(true, this));
		for(AABB box : boxes)
			if(box.inflate(.002).contains(hitVec))
			{
				for(Direction d : DirectionUtils.VALUES)
				{
					Vec3 testVec = new Vec3(0.5+0.5*d.getStepX(), 0.5+0.5*d.getStepY(), 0.5+0.5*d.getStepZ());
					if(box.inflate(0.002).contains(testVec))
					{
						fd = d;
						break;
					}
				}
				break;
			}
		if(fd!=null)
		{
			toggleSide(fd);
			this.markContainingBlockForUpdate(null);
			FluidPipeBlockEntity.indirectConnections.clearDimension(level);
			return true;
		}
		return false;
	}

	@Override
	public void onBEPlaced(Level world, BlockPos pos, BlockState state, Direction side, float hitX, float hitY, float hitZ, LivingEntity placer, ItemStack stack)
	{
		if(!world.isClientSide())
		{
			BlockEntity te;
			for(Direction dir : Direction.values())
				if((te = world.getBlockEntity(pos.relative(dir))) instanceof FluidPipeBlockEntity)
				{
					FluidPipeBlockEntity neighborPipe = (FluidPipeBlockEntity)te;
					if(neighborPipe.color!=this.color||!neighborPipe.sideConfig.getBoolean(dir.getOpposite()))
						this.setSide(dir, false);
				}
		}
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower)
	{
		return false;
	}

	@Override
	public boolean hasOutputConnection(Direction side)
	{
		return side!=null&&sideConfig.getBoolean(side);
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload ev)
	{
		if(!ev.getWorld().isClientSide()&&ev.getWorld() instanceof Level level)
			indirectConnections.clearDimension(level);
	}

	@Nullable
	public DyeColor getColor()
	{
		return color;
	}

	public enum ConnectionStyle
	{
		NO_CONNECTION,
		PLAIN,
		FLANGE
	}
}
