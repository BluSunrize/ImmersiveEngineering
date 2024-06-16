/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.api.wires.WireType.STRUCTURE_CATEGORY;

public class BalloonBlockEntity extends ImmersiveConnectableBlockEntity implements IPlayerInteraction, IHammerInteraction, IBlockBounds
{
	public int style = 0;
	public DyeColor colour0 = null;
	public DyeColor colour1 = null;
	private ShaderWrapper_Direct shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "balloon"));

	public BalloonBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.BALLOON.get(), pos, state);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		final int oldStyle = style;
		final DyeColor oldC0 = colour0;
		final DyeColor oldC1 = colour1;
		style = nbt.getInt("style");
		int tmpIdx = nbt.getInt("colour0");
		colour0 = tmpIdx >= 0&&tmpIdx < DyeColor.values().length?DyeColor.byId(tmpIdx): null;
		tmpIdx = nbt.getInt("colour1");
		colour1 = tmpIdx >= 0&&tmpIdx < DyeColor.values().length?DyeColor.byId(tmpIdx): null;
		if(oldStyle!=style||oldC0!=colour0||oldC1!=colour1)
			requestModelDataUpdate();
		if(nbt.contains("shader", Tag.TAG_COMPOUND))
			shader = ShaderWrapper_Direct.SERIALIZER.read(this, nbt.getCompound("shader"));
		markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("style", style);
		nbt.putInt("colour0", colour0!=null?colour0.getId(): -1);
		nbt.putInt("colour1", colour1!=null?colour1.getId(): -1);
		nbt.put("shader", ShaderWrapper_Direct.SERIALIZER.write(shader));
	}

	@Nonnull
	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.box(.125, 0, .125, .875, .9375, .875);
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return super.triggerEvent(id, arg);
	}

	public static void registerCapabilities(BECapabilityRegistrar<BalloonBlockEntity> registrar)
	{
		registrar.registerAllContexts(CapabilityShader.BLOCK, be -> be.shader);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return STRUCTURE_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		BlockPos end = other.position();
		int xDif = end.getX()-getBlockPos().getX();
		int zDif = end.getZ()-getBlockPos().getZ();
		int yDif = end.getY()-getBlockPos().getY();
		if(yDif < 0)
		{
			double dist = Math.sqrt(xDif*xDif+zDif*zDif);
			if(dist/Math.abs(yDif) < 2.5)
				return new Vec3(.5, .09375, .5);
		}
		if(Math.abs(zDif) > Math.abs(xDif))
			return new Vec3(.5, .09375, zDif > 0?.78125: .21875);
		else
			return new Vec3(xDif > 0?.78125: .21875, .09375, .5);
	}

	@Override
	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(player.isShiftKeyDown())
		{
			if(!level.isClientSide)
			{
				ItemEntity entityitem = new ItemEntity(level, player.getX(), player.getY(), player.getZ(),
						new ItemStack(level.getBlockState(getBlockPos()).getBlock()),
						0, 0, 0);
				level.removeBlock(getBlockPos(), false);
				level.addFreshEntity(entityitem);
			}
			return InteractionResult.SUCCESS;
		}
		else if(!heldItem.isEmpty()&&heldItem.getItem() instanceof IShaderItem)
		{
			this.shader.setShaderItem(ItemHandlerHelper.copyStackWithSize(heldItem, 1));
			markContainingBlockForUpdate(null);
			return InteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
		}
		int target = 0;
		if(side.getAxis()==Axis.Y&&style==0)
			target = (hitX < .375||hitX > .625)&&(hitZ < .375||hitZ > .625)?1: 0;
		else if(side.getAxis()==Axis.Z)
		{
			if(style==0)
				target = (hitX < .375||hitX > .625)?1: 0;
			else
				target = (hitY > .5625&&hitY < .75)?1: 0;
		}
		else if(side.getAxis()==Axis.X)
		{
			if(style==0)
				target = (hitZ < .375||hitZ > .625)?1: 0;
			else
				target = (hitY > .5625&&hitY < .75)?1: 0;
		}
		DyeColor heldDye = Utils.getDye(heldItem);
		if(heldDye==null)
			return InteractionResult.PASS;
		if(target==0)
		{
			if(colour0==heldDye)
				return InteractionResult.PASS;
			colour0 = heldDye;
		}
		else
		{
			if(colour1==heldDye)
				return InteractionResult.PASS;
			colour1 = heldDye;
		}
		markContainingBlockForUpdate(null);
		return InteractionResult.sidedSuccess(getLevelNonnull().isClientSide);
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
		{
			style = 1-style;
			markContainingBlockForUpdate(null);
		}
		return true;
	}

	public ShaderWrapper getShader()
	{
		return shader;
	}
}