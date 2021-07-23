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
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorStructuralTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.math.Vector4f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BalloonTileEntity extends ConnectorStructuralTileEntity implements IPlayerInteraction, IHammerInteraction
{
	public int style = 0;
	public DyeColor colour0 = null;
	public DyeColor colour1 = null;
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "balloon"));

	public BalloonTileEntity()
	{
		super(IETileTypes.BALLOON.get());
		reInitCapability();
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
		if(nbt.contains("shader", NBT.TAG_COMPOUND))
		{
			shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "balloon"));
			shader.deserializeNBT(nbt.getCompound("shader"));
			reInitCapability();
		}
	}

	@Override
	public void writeCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("style", style);
		nbt.putInt("colour0", colour0!=null?colour0.getId(): -1);
		nbt.putInt("colour1", colour1!=null?colour1.getId(): -1);
		nbt.put("shader", shader.serializeNBT());
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

	LazyOptional<ShaderWrapper> shaderCap;

	private void reInitCapability()
	{
		if(shaderCap!=null)
			unregisterCap(shaderCap);
		shaderCap = registerConstantCap(shader);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityShader.SHADER_CAPABILITY)
			return shaderCap.cast();
		return super.getCapability(capability, facing);
	}

	@Nonnull
	@Override
	public String getCacheKey(@Nonnull BlockState object)
	{
		if(shader!=null&&!shader.getShaderItem().isEmpty()&&shader.getShaderItem().getItem() instanceof IShaderItem)
			return ((IShaderItem)shader.getShaderItem().getItem()).getShaderName(shader.getShaderItem()).toString();
		return colour0+":"+colour1+":"+style;
	}

	@Override
	public Vector4f getRenderColor(BlockState object, String group, Vector4f original)
	{
		if(shader!=null&&!shader.getShaderItem().isEmpty()&&shader.getShaderItem().getItem() instanceof IShaderItem)
			return original;
		if(style==0)
		{
			if(group.startsWith("balloon1_"))
				return Utils.vec4fFromDye(colour1);
			if(group.startsWith("balloon0_"))
				return Utils.vec4fFromDye(colour0);
		}
		else
		{
			if(group.endsWith("_1"))
				return Utils.vec4fFromDye(colour1);
			if(group.endsWith("_0"))
				return Utils.vec4fFromDye(colour0);
		}
		return original;
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		BlockPos end = con.getOtherEnd(here).getPosition();
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
	public boolean interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(!heldItem.isEmpty()&&heldItem.getItem() instanceof IShaderItem)
		{
			if(this.shader==null)
				this.shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "balloon"));
			this.shader.setShaderItem(ItemHandlerHelper.copyStackWithSize(heldItem, 1));
			markContainingBlockForUpdate(null);
			return true;
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
			return false;
		if(target==0)
		{
			if(colour0==heldDye)
				return false;
			colour0 = heldDye;
		}
		else
		{
			if(colour1==heldDye)
				return false;
			colour1 = heldDye;
		}
		markContainingBlockForUpdate(null);
		return true;
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

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		if(entity instanceof AbstractArrow)
		{
			Vec3 pos = Vec3.atCenterOf(getBlockPos());
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIREWORK_ROCKET_BLAST,
					SoundSource.BLOCKS, 1.5f, 0.7f);
			world.removeBlock(getBlockPos(), false);
			world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0, .05, 0);
			Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(this.shader);
			if(shader!=null)
				shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), null, shader.getRight().getShaderType().toString(), pos, null, .375f);

		}
	}

	@Override
	public Direction getFacing()
	{
		return Direction.NORTH;
	}

	@Override
	public void setFacing(Direction facing)
	{
	}
}