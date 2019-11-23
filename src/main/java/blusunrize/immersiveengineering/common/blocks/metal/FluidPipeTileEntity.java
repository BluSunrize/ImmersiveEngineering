/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.AdvancedAABB;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.newSetFromMap;

//TODO use cap references
public class FluidPipeTileEntity extends IEBaseTileEntity implements IFluidPipe, IAdvancedHasObjProperty,
		IOBJModelCallback<BlockState>, IColouredTile, IPlayerInteraction, IHammerInteraction, IPlacementInteraction,
		IAdvancedSelectionBounds, IAdvancedCollisionBounds, IAdditionalDrops, INeighbourChangeTile
{
	public static TileEntityType<FluidPipeTileEntity> TYPE;

	static ConcurrentHashMap<BlockPos, Set<DirectionalFluidOutput>> indirectConnections = new ConcurrentHashMap<>();
	public static ArrayList<Function<ItemStack, Boolean>> validPipeCovers = new ArrayList<>();
	public static ArrayList<Function<ItemStack, Boolean>> climbablePipeCovers = new ArrayList<>();

	public FluidPipeTileEntity()
	{
		super(TYPE);
	}

	public static void initCovers()
	{
		final ArrayList<ItemStack> scaffolds = Lists.newArrayList(new ItemStack(WoodenDecoration.treatedScaffolding));
		Stream.concat(
				MetalDecoration.aluScaffolding.values().stream(),
				MetalDecoration.steelScaffolding.values().stream()
		)
				.map(ItemStack::new)
				.forEach(scaffolds::add);
		Function<ItemStack, Boolean> defaultMatch = (input) -> {
			if(input.isEmpty())
				return false;
			for(ItemStack stack : scaffolds)
				if(ItemStack.areItemsEqual(stack, input))
					return true;
			return false;
		};
		FluidPipeTileEntity.validPipeCovers.add(defaultMatch);
		FluidPipeTileEntity.climbablePipeCovers.add(defaultMatch);
	}

	public int[] sideConfig = new int[]{0, 0, 0, 0, 0, 0};
	public ItemStack pipeCover = ItemStack.EMPTY;
	private byte connections = 0;
	@Nullable
	private DyeColor color = null;

	public static Set<DirectionalFluidOutput> getConnectedFluidHandlers(BlockPos node, World world)
	{
		if(indirectConnections.containsKey(node))
			return indirectConnections.get(node);

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		Set<DirectionalFluidOutput> fluidHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());
		openList.add(node);
		while(!openList.isEmpty()&&closedList.size() < 1024)
		{
			BlockPos next = openList.get(0);
			TileEntity pipeTile = Utils.getExistingTileEntity(world, next);
			if(!closedList.contains(next)&&(pipeTile instanceof IFluidPipe))
			{
				if(pipeTile instanceof FluidPipeTileEntity)
					closedList.add(next);
				for(int i = 0; i < 6; i++)
				{
					//						boolean b = (te instanceof FluidPipeTileEntity)? (((FluidPipeTileEntity) te).sideConfig[i]==0): (((FluidPumpTileEntity) te).sideConfig[i]==1);
					Direction fd = Direction.byIndex(i);
					if(((IFluidPipe)pipeTile).hasOutputConnection(fd))
					{
						BlockPos nextPos = next.offset(fd);
						TileEntity adjacentTile = Utils.getExistingTileEntity(world, nextPos);
						if(adjacentTile!=null)
							if(adjacentTile instanceof FluidPipeTileEntity)
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
			}
			openList.remove(0);
		}
		if(!world.isRemote)
		{
			if(!indirectConnections.containsKey(node))
			{
				indirectConnections.put(node, newSetFromMap(new ConcurrentHashMap<>()));
				indirectConnections.get(node).addAll(fluidHandlers);
			}
		}
		return fluidHandlers;
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		if(!world.isRemote)
		{

			//TODO this really shouldn't be necessary IMO...
			ApiUtils.addFutureServerTask(world, () -> {
				boolean changed = false;
				for(Direction f : Direction.VALUES)
					changed |= updateConnectionByte(f);
				if(changed)
				{
					world.notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
					markContainingBlockForUpdate(null);
				}
			}, true);
		}
	}

	@Override
	public void remove()
	{
		super.remove();
		if(!world.isRemote)
			indirectConnections.clear();
	}


	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		if(!(entity instanceof LivingEntity)||((LivingEntity)entity).isOnLadder()||pipeCover.isEmpty())
			return;
		else
		{
			boolean climb = false;
			for(Function<ItemStack, Boolean> f : climbablePipeCovers)
				if(f!=null&&f.apply(pipeCover)==Boolean.TRUE)
				{
					climb = true;
					break;
				}
			if(!climb)
				return;
			double motionX = entity.getMotion().x;
			double motionY = entity.getMotion().y;
			double motionZ = entity.getMotion().z;
			double maxSpeed = 0.15;
			motionX = MathHelper.clamp(motionX, -maxSpeed, maxSpeed);
			motionY = MathHelper.clamp(motionY, -maxSpeed, maxSpeed);
			motionZ = MathHelper.clamp(motionZ, -maxSpeed, maxSpeed);

			entity.fallDistance = 0f;

			if(motionY < 0&&entity instanceof PlayerEntity&&entity.isSneaking())
			{
				motionY = .05;
				return;
			}
			if(entity.collidedHorizontally)
				motionY = .2;
			entity.setMotion(motionX, motionY, motionZ);
		}
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		sideConfig = nbt.getIntArray("sideConfig");
		if(sideConfig==null||sideConfig.length!=6)
			sideConfig = new int[]{0, 0, 0, 0, 0, 0};
		pipeCover = ItemStack.read(nbt.getCompound("pipeCover"));
		DyeColor oldColor = color;
		if(nbt.contains("color", NBT.TAG_INT))
			color = DyeColor.byId(nbt.getInt("color"));
		else
			color = null;
		byte oldConns = connections;
		connections = nbt.getByte("connections");
		if(world!=null&&world.isRemote&&(connections!=oldConns||color!=oldColor))
		{
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putIntArray("sideConfig", sideConfig);
		if(!pipeCover.isEmpty())
			nbt.put("pipeCover", (pipeCover.write(new CompoundNBT())));
		nbt.putByte("connections", connections);
		if(color!=null)
			nbt.putInt("color", color.getId());
	}


	boolean canOutputPressurized(TileEntity output, boolean consumePower)
	{
		if(output instanceof IFluidPipe)
			return ((IFluidPipe)output).canOutputPressurized(consumePower);
		return false;
	}

	private EnumMap<Direction, LazyOptional<IFluidHandler>> sidedHandlers = new EnumMap<>(Direction.class);
	private EnumMap<Direction, CapabilityReference<IFluidHandler>> neighbors = new EnumMap<>(Direction.class);

	{
		for(Direction f : Direction.VALUES)
		{
			sidedHandlers.put(f, registerConstantCap(new PipeFluidHandler(this, f)));
			neighbors.put(f, CapabilityReference.forNeighbor(this, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f));
		}
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null&&sideConfig[facing.ordinal()]==0)
			return sidedHandlers.get(facing).cast();
		return super.getCapability(capability, facing);
	}

	/*TODO
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(IBlockState object, List<BakedQuad> quads)
	{
		if(!pipeCover.isEmpty())
		{
			Block b = Block.getBlockFromItem(pipeCover.getItem());
			IBlockState state = b!=null?b.getStateFromMeta(pipeCover.getMetadata()): Blocks.STONE.getDefaultState();
			IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
			BlockRenderLayer curL = MinecraftForgeClient.getRenderLayer();
			if(model!=null)
				for(BlockRenderLayer layer : BlockRenderLayer.values())
				{
					ForgeHooksClient.setRenderLayer(layer);
					for(EnumFacing facing : EnumFacing.VALUES)
						quads.addAll(model.getQuads(state, facing, 0));
					quads.addAll(model.getQuads(state, null, 0));
				}
			ForgeHooksClient.setRenderLayer(curL);
		}
		return quads;
	}
	*/

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getCacheKey(BlockState object)
	{
		return getRenderCacheKey();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Optional<TRSRTransformation> applyTransformations(BlockState object, String group, Optional<TRSRTransformation> transform)
	{
		return transform;
	}

	@Override
	public Collection<ItemStack> getExtraDrops(PlayerEntity player, BlockState state)
	{
		if(!pipeCover.isEmpty())
			return Lists.newArrayList(pipeCover);
		return null;
	}

	@Override
	public void onNeighborBlockChange(BlockPos otherPos)
	{
		Direction dir = Direction.getFacingFromVector(otherPos.getX()-pos.getX(),
				otherPos.getY()-pos.getY(), otherPos.getZ()-pos.getZ());
		if(updateConnectionByte(dir))
		{
			world.notifyNeighborsOfStateExcept(pos, getBlockState().getBlock(), dir);
			markContainingBlockForUpdate(null);
		}
		if(!getWorldNonnull().isRemote)
			indirectConnections.clear();
	}

	static class PipeFluidHandler implements IFluidHandler
	{
		FluidPipeTileEntity pipe;
		Direction facing;

		public PipeFluidHandler(FluidPipeTileEntity pipe, Direction facing)
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
			return 1000;
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
			ArrayList<DirectionalFluidOutput> outputList = new ArrayList<>(getConnectedFluidHandlers(pipe.getPos(), pipe.world));

			if(outputList.size() < 1)
//NO OUTPUTS!
				return 0;
			BlockPos ccFrom = new BlockPos(pipe.getPos().offset(facing));
			int sum = 0;
			HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
			for(DirectionalFluidOutput output : outputList)
			{
				BlockPos cc = Utils.toCC(output.containingTile);
				if(!cc.equals(ccFrom)&&pipe.world.isBlockLoaded(cc)&&!pipe.equals(output.containingTile))
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
						amount = (int)Math.ceil(MathHelper.clamp(amount, 1,
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
			return (resource.getOrCreateTag().contains("pressurized"))||
					pipe.canOutputPressurized(output.containingTile, false)
					?1000: 50;
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			return FluidStack.EMPTY;
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			return FluidStack.EMPTY;
		}
	}

	public static class DirectionalFluidOutput
	{
		IFluidHandler output;
		Direction direction;
		TileEntity containingTile;

		public DirectionalFluidOutput(IFluidHandler output, TileEntity containingTile, Direction direction)
		{
			this.output = output;
			this.direction = direction;
			this.containingTile = containingTile;
		}
	}

	public boolean updateConnectionByte(Direction dir)
	{
		final byte oldConn = connections;
		int i = dir.getIndex();
		int mask = 1<<i;
		connections &= ~mask;
		if(sideConfig[i]==0&&neighbors.get(dir).isPresent())
		{
			IFluidHandler handler = neighbors.get(dir).get();
			if(handler.getTanks() > 0)
				connections |= mask;
		}
		return oldConn!=connections;
	}

	public byte getAvailableConnectionByte()
	{
		byte connections = 0;
		int mask = 1<<6;
		for(Direction dir : Direction.VALUES)
		{
			mask >>= 1;
			if(neighbors.get(dir).isPresent())
			{
				IFluidHandler handler = neighbors.get(dir).get();
				if(handler.getTanks() > 0)
					connections |= mask;
			}
		}
		return connections;
	}

	public int getConnectionStyle(int connection)
	{
		if(sideConfig[connection]==-1)
			return 0;
		if((connections&(1<<connection))==0)
			return 0;

		if(connections!=3&&connections!=12&&connections!=48)
			return 1;
		//		TileEntity con = world.getTileEntity(xCoord+(connection==4?-1: connection==5?1: 0),yCoord+(connection==0?-1: connection==1?1: 0),zCoord+(connection==2?-1: connection==3?1: 0));
		TileEntity con = world.getTileEntity(getPos().offset(Direction.byIndex(connection)));
		if(con instanceof FluidPipeTileEntity)
		{
			byte tileConnections = ((FluidPipeTileEntity)con).connections;
			if(connections==tileConnections)
				return 0;
		}
		return 1;
	}

	public void toggleSide(int side)
	{
		sideConfig[side]++;
		if(sideConfig[side] > 0)
			sideConfig[side] = -1;
		markDirty();

		Direction fd = Direction.byIndex(side);
		TileEntity connected = world.getTileEntity(getPos().offset(fd));
		if(connected instanceof FluidPipeTileEntity)
		{
			((FluidPipeTileEntity)connected).sideConfig[fd.getOpposite().ordinal()] = sideConfig[side];
			connected.markDirty();
			world.addBlockEvent(getPos().offset(fd), getBlockState().getBlock(), 0, 0);
		}
		world.addBlockEvent(getPos(), getBlockState().getBlock(), 0, 0);
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		if(!pipeCover.isEmpty())
		{
			list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(-.03125f));
			return list;
		}
		if(/*connections==16||connections==32||*/connections==48)
		{
			list.add(new AxisAlignedBB(0, .25f, .25f, 1, .75f, .75f));
			if((connections&16)==0)
				list.add(new AxisAlignedBB(0, .125f, .125f, .125f, .875f, .875f));
			if((connections&32)==0)
				list.add(new AxisAlignedBB(.875f, .125f, .125f, 1, .875f, .875f));
		}
		else if(/*connections==4||connections==8||*/connections==12)
		{
			list.add(new AxisAlignedBB(.25f, .25f, 0, .75f, .75f, 1));
			if((connections&4)==0)
				list.add(new AxisAlignedBB(.125f, .125f, 0, .875f, .875f, .125f));
			if((connections&8)==0)
				list.add(new AxisAlignedBB(.125f, .125f, .875f, .875f, .875f, 1));
		}
		else if(/*connections==1||connections==2||*/connections==3)
		{
			list.add(new AxisAlignedBB(.25f, 0, .25f, .75f, 1, .75f));
			if((connections&1)==0)
				list.add(new AxisAlignedBB(.125f, 0, .125f, .875f, .125f, .875f));
			if((connections&2)==0)
				list.add(new AxisAlignedBB(.125f, .875f, .125f, .875f, 1, .875f));
		}
		else
		{
			list.add(new AxisAlignedBB(.25f, .25f, .25f, .75f, .75f, .75f));
			for(int i = 0; i < 6; i++)
			{
				if((connections&(1<<i))!=0)
					list.add(new AxisAlignedBB(i==4?0: i==5?.875f: .125f, i==0?0: i==1?.875f: .125f, i==2?0: i==3?.875f: .125f, i==4?.125f: i==5?1: .875f, i==0?.125f: i==1?1: .875f, i==2?.125f: i==3?1: .875f));
			}
		}
		return list;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List<AxisAlignedBB> list = Lists.newArrayList();
		byte availableConnections = getAvailableConnectionByte();
		double[] baseAABB = !pipeCover.isEmpty()?new double[]{.002, .998, .002, .998, .002, .998}: new double[]{.25, .75, .25, .75, .25, .75};
		for(int i = 0; i < 6; i++)
		{
			double depth = getConnectionStyle(i)==0?.25: .125;
			double size = getConnectionStyle(i)==0?.25: .125;
			//			if(pipeCover!=null)
			//				size = 0;
			if((availableConnections&0x1)==1)
				list.add(new AdvancedAABB(new AxisAlignedBB(i==4?0: i==5?1-depth: size, i==0?0: i==1?1-depth: size, i==2?0: i==3?1-depth: size, i==4?depth: i==5?1: 1-size, i==0?depth: i==1?1: 1-size, i==2?depth: i==3?1: 1-size), Direction.byIndex(i)));
			if((connections&(1<<i))!=0)
				baseAABB[i] += i%2==1?.125: -.125;
			baseAABB[i] = Math.min(Math.max(baseAABB[i], 0), 1);
			availableConnections = (byte)(availableConnections >> 1);
		}
		list.add(new AdvancedAABB(new AxisAlignedBB(baseAABB[4], baseAABB[0], baseAABB[2], baseAABB[5], baseAABB[1], baseAABB[3]), null));
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		if(box instanceof AdvancedAABB)
		{
			if(box.grow(.002).contains(mop.getHitVec()))
			{
				AxisAlignedBB changedBox = ((AdvancedAABB)box).fd!=null?box.grow(((AdvancedAABB)box).fd.getXOffset()!=0?0: .03125, ((AdvancedAABB)box).fd.getYOffset()!=0?0: .03125, ((AdvancedAABB)box).fd.getZOffset()!=0?0: .03125): box;
				list.add(changedBox);
				return true;
			}
		}
		return false;
	}

	public static HashMap<String, OBJState> cachedOBJStates = new HashMap<>();
	static String[] CONNECTIONS = new String[]{
			"con_yMin", "con_yMax", "con_zMin", "con_zMax", "con_xMin", "con_xMax"
	};

	String getRenderCacheKey()
	{
		StringBuilder key = new StringBuilder();
		for(int i = 0; i < 6; i++)
		{
			if((connections&(1<<i))!=0)
				key.append(getConnectionStyle(i)==1?"2": "1");
			else
				key.append("0");
		}
		if(!pipeCover.isEmpty())
			key.append("scaf:").append(pipeCover);
		key.append(color);
		return key.toString();
	}

	// Lowest 6 bits are conns, bits 8 to 14 (1&(b>>8)) ore conn style
	private static short getConnectionsFromKey(String key)
	{
		short ret = 0;
		for(int i = 0; i < 6; i++)
		{
			char c = key.charAt(i);
			switch(c)
			{
				case '0':
					//NOP
					break;
				case '2':
					ret |= (1<<i)|(1<<(i+8));
					break;
				case '1':
					ret |= (1<<i);
					break;
			}
		}
		return ret;
	}

	private static int getConnectionStyle(int dir, short conns)
	{
		return 1&(conns >> (dir+8));
	}

	@Override
	public OBJState getOBJState()
	{
		String key = getRenderCacheKey();
		return getStateFromKey(key);
	}

	public static OBJState getStateFromKey(String key)
	{
		if(!cachedOBJStates.containsKey(key))
		{
			ArrayList<String> parts = new ArrayList<>();
			Matrix4 rotationMatrix = new Matrix4(TRSRTransformation.identity().getMatrixVec());//TODO is getMatrixVec correct?
			short connections = getConnectionsFromKey(key);
//			if(pipeCover!=null)
//				parts.add("cover");
			int totalConnections = Integer.bitCount(connections&255);
			boolean straightY = (connections&3)==3;
			boolean straightZ = (connections&12)==12;
			boolean straightX = (connections&48)==48;
			switch(totalConnections)
			{
				case 0://stub
					parts.add("center");
					break;
				case 1://stopper
					parts.add("stopper");

					//default: y-
					if((connections&2)!=0)//y+
						rotationMatrix.rotate(Math.PI, 0, 0, 1);
					else if((connections&4)!=0)//z-
						rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
					else if((connections&8)!=0)//z+
						rotationMatrix.rotate(-Math.PI/2, 1, 0, 0);
					else if((connections&16)!=0)//x-
						rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
					else if((connections&32)!=0)//x+
						rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
					parts.add("con_yMin");
					break;
				case 2://straight or curve
					if(straightY)
					{
						parts.add("pipe_y");
						if(getConnectionStyle(0, connections)==1)
							parts.add("con_yMin");
						if(getConnectionStyle(1, connections)==1)
							parts.add("con_yMax");
					}
					else if(straightZ)
					{
						parts.add("pipe_z");
						if(getConnectionStyle(2, connections)==1)
							parts.add("con_zMin");
						if(getConnectionStyle(3, connections)==1)
							parts.add("con_zMax");
					}
					else if(straightX)
					{
						parts.add("pipe_x");
						if(getConnectionStyle(4, connections)==1)
							parts.add("con_xMin");
						if(getConnectionStyle(5, connections)==1)
							parts.add("con_xMax");
					}
					else
					{
						parts.add("curve");
						parts.add("con_yMin");
						parts.add("con_zMin");
						byte connectTo = (byte)(connections&60);
						if((connections&3)!=0)//curve to top or bottom
						{
							if(connectTo==16)//x-
								rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
							else if(connectTo==32)//x+
								rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);
							else if(connectTo==8)//z+
								rotationMatrix.rotate(Math.PI, 0, 1, 0);
							if((connections&2)!=0)//flip to top
								rotationMatrix.rotate(Math.PI, 0, 0, 1);

							//default: Curve to z-
						}
						else//curve to horizontal
						{
							rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
							if(connectTo==40)//z+ to x+
								rotationMatrix.rotate(Math.PI, 1, 0, 0);
							else if(connectTo==24)//z+ to x-
								rotationMatrix.rotate(-Math.PI/2, 1, 0, 0);
							else if(connectTo==36)//z- to x+
								rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
							//default: z- to x-
						}
					}
					break;
				case 3://tcross or tcurve
					if(straightX||straightZ||straightY)//has straight connect
					{
						parts.add("tcross");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_zMax");
						if(straightX)
						{
							rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
							if((connections&4)!=0)//z-
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
							else if((connections&8)!=0)//z+
								rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
							else if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI, 0, 0, 1);
							//default: Curve to y-
						}
						else if(straightY)
						{
							rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
							else if((connections&32)!=0)//x+
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
							else if((connections&8)!=0)//z+
								rotationMatrix.rotate(Math.PI, 0, 0, 1);
							//default: Curve to z-
						}
						else //default:z straight
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
							else if((connections&32)!=0)//x+
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
							else if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI, 0, 0, 1);
							//default: Curve to y-
						}
					}
					else //tcurve
					{
						parts.add("tcurve");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_xMax");
						//default y-, z-, x+
						if((connections&8)!=0)//z+
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0, 1, 0);
							else
								rotationMatrix.rotate(-Math.PI/2, 0, 1, 0);
						}
						else//z-
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
						}
						if((connections&2)!=0)//y+
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
					}
					break;
				case 4://cross or complex tcross
					boolean cross = (straightX&&straightZ)||(straightX&&straightY)||(straightZ&&straightY);
					if(cross)
					{
						parts.add("cross");
						parts.add("con_yMin");
						parts.add("con_yMax");
						parts.add("con_zMin");
						parts.add("con_zMax");
						if(!straightY)//x and z
							rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						else if(straightX)//x and y
							rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
					}
					else
					{
						parts.add("tcross2");
						parts.add("con_yMin");
						parts.add("con_zMin");
						parts.add("con_zMax");
						parts.add("con_xMax");
						if(straightZ)
						{
							//default y- z+- x+
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0, 1, 0);
							if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						}
						else if(straightY)
						{
							rotationMatrix.rotate(Math.PI/2, 1, 0, 0);
							//default y+- z- x+
							if((connections&8)!=0)//z+
							{
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
								if((connections&16)!=0)//x-
									rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
							}
							else if((connections&16)!=0)//x-
								rotationMatrix.rotate(-Math.PI/2, 0, 0, 1);
						}
						else
						{
							rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
							//default y- z- x+-
							if((connections&8)!=0)//z+
								rotationMatrix.rotate(Math.PI, 0, 1, 0);
							if((connections&2)!=0)//y+
								rotationMatrix.rotate(Math.PI/2, 0, 0, 1);
						}
					}
					break;
				case 5://complete tcross
					parts.add("tcross3");
					parts.add("con_yMin");
					parts.add("con_yMax");
					parts.add("con_zMin");
					parts.add("con_zMax");
					parts.add("con_xMax");
					//default y+- z+- x+
					if(straightZ)
					{
						if(straightY)
						{
							if((connections&16)!=0)//x-
								rotationMatrix.rotate(Math.PI, 0, 1, 0);
						}
						else if(straightX)
							rotationMatrix.rotate(((connections&2)!=0)?(Math.PI/2): (-Math.PI/2), 0, 0, 1);
					}
					else if(straightX)
					{
						rotationMatrix.rotate(Math.PI/2, 0, 1, 0);
						if((connections&8)!=0)//z+
							rotationMatrix.rotate(Math.PI, 0, 1, 0);
					}
					break;
				case 6://Full Crossing
					parts.add("con_yMin");
					parts.add("con_yMax");
					parts.add("con_zMin");
					parts.add("con_zMax");
					parts.add("con_xMin");
					parts.add("con_xMax");

					break;
			}
			//			connetionParts
			//			for(int i=0; i<6; i++)
			//				if(((FluidPipeTileEntity)tile).getConnectionStyle(i)==1)
			//					connectionCaps.add(CONNECTIONS[i]);

			Matrix4 tempMatr = new Matrix4();
			tempMatr.m03 = tempMatr.m13 = tempMatr.m23 = .5f;
			rotationMatrix.leftMultiply(tempMatr);
			tempMatr.invert();
			rotationMatrix = rotationMatrix.multiply(tempMatr);

			cachedOBJStates.put(key, new OBJState(parts, true, new TRSRTransformation(rotationMatrix.toMatrix4f())));
		}
		return cachedOBJStates.get(key);
	}


	@Override
	public int getRenderColour(int tintIndex)
	{
		return 0xffffff;
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem.isEmpty()&&player.isSneaking()&&!pipeCover.isEmpty())
		{
			if(!world.isRemote&&world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
			{
				ItemEntity entityitem = player.dropItem(pipeCover.copy(), false);
				if(entityitem!=null)
					entityitem.setNoPickupDelay();
			}
			pipeCover = ItemStack.EMPTY;
			this.markContainingBlockForUpdate(null);
			world.addBlockEvent(getPos(), getBlockState().getBlock(), 255, 0);
			return true;
		}
		else if(!heldItem.isEmpty()&&!player.isSneaking())
		{
			for(Function<ItemStack, Boolean> func : validPipeCovers)
				if(func.apply(heldItem)==Boolean.TRUE)
				{
					if(!ItemStack.areItemsEqual(pipeCover, heldItem))
					{
						if(!world.isRemote&&!pipeCover.isEmpty()&&world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
						{
							ItemEntity entityitem = player.dropItem(pipeCover.copy(), false);
							if(entityitem!=null)
								entityitem.setNoPickupDelay();
						}
						pipeCover = Utils.copyStackWithAmount(heldItem, 1);
						heldItem.shrink(1);
						this.markContainingBlockForUpdate(null);
						world.addBlockEvent(getPos(), getBlockState().getBlock(), 255, 0);
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
	public boolean hammerUseSide(Direction side, PlayerEntity player, Vec3d hitVec)
	{
		if(world.isRemote)
			return true;
		Direction fd = side;
		List<AxisAlignedBB> boxes = this.getAdvancedSelectionBounds();
		for(AxisAlignedBB box : boxes)
			if(box instanceof AdvancedAABB)
			{
				if(box.grow(.002).contains(new Vec3d(getPos().getX()+hitVec.x, getPos().getY()+hitVec.y, getPos().getZ()+hitVec.z)))
					if(((AdvancedAABB)box).fd!=null)
						fd = ((AdvancedAABB)box).fd;
			}
		if(fd!=null)
		{
			toggleSide(fd.ordinal());
			this.markContainingBlockForUpdate(null);
			FluidPipeTileEntity.indirectConnections.clear();
			return true;
		}
		return false;
	}

	@Override
	public void onTilePlaced(World world, BlockPos pos, BlockState state, Direction side, float hitX, float hitY, float hitZ, LivingEntity placer, ItemStack stack)
	{
		TileEntity te;
		for(Direction dir : Direction.values())
			if((te = world.getTileEntity(pos.offset(dir))) instanceof FluidPipeTileEntity)
				if(((FluidPipeTileEntity)te).color!=this.color)
					this.toggleSide(dir.ordinal());
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower)
	{
		return false;
	}

	@Override
	public boolean hasOutputConnection(Direction side)
	{
		return side!=null&&sideConfig[side.ordinal()]==0;
	}

	@Override
	public int getRenderColour(BlockState object, String group)
	{
		if(color!=null)
			return color.colorValue|(0xff000000);
		return 0xffffffff;
	}
}