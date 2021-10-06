/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ConnectorProbeTileEntity extends ConnectorRedstoneTileEntity
{
	public DyeColor redstoneChannelSending = DyeColor.WHITE;
	private int lastOutput = 0;

	public ConnectorProbeTileEntity()
	{
		super(IETileTypes.CONNECTOR_PROBE.get());
	}

	@Override
	public void tick()
	{
		if(!level.isClientSide&&level.getGameTime()%8==((getBlockPos().getX()^getBlockPos().getZ())&7))
		{
			int out = getComparatorSignal();
			if(out!=lastOutput)
			{
				this.lastOutput = out;
				this.rsDirty = true;
			}
		}
		super.tick();
	}

	@Override
	public boolean isRSInput()
	{
		return true;
	}

	@Override
	public boolean isRSOutput()
	{
		return true;
	}

	private int getComparatorSignal()
	{
		BlockPos pos = this.getBlockPos().relative(getFacing());
		BlockState state = level.getBlockState(pos);
		if(state.hasAnalogOutputSignal())
			return state.getAnalogOutputSignal(level, pos);
		else if(state.isRedstoneConductor(level, pos))
		{
			pos = pos.relative(getFacing());
			state = level.getBlockState(pos);
			if(state.hasAnalogOutputSignal())
				return state.getAnalogOutputSignal(level, pos);
			else if(state.getMaterial()==Material.AIR)
			{
				ItemFrame entityitemframe = this.findItemFrame(level, getFacing(), pos);
				if(entityitemframe!=null)
					return entityitemframe.getAnalogOutput();
			}
		}
		return 0;
	}

	private ItemFrame findItemFrame(Level world, final Direction facing, BlockPos pos)
	{
		List<ItemFrame> list = world.getEntitiesOfClass(ItemFrame.class, new AABB(pos), entity -> entity!=null&&entity.getDirection()==facing);
		return list.size()==1?list.get(0): null;
	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		signals[redstoneChannelSending.ordinal()] = (byte)lastOutput;
		rsDirty = false;
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneProbe, this);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getInt("redstoneChannel"));
		if(message.contains("redstoneChannelSending"))
			redstoneChannelSending = DyeColor.byId(message.getInt("redstoneChannelSending"));
		updateAfterConfigure();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("redstoneChannelSending", redstoneChannelSending.getId());
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		redstoneChannelSending = DyeColor.byId(nbt.getInt("redstoneChannelSending"));
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3(.5+side.getStepX()*(.375-conRadius), .5+side.getStepY()*(.375-conRadius), .5+side.getStepZ()*(.375-conRadius));
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		float wMin = .28125f;
		float wMax = .71875f;
		switch(getFacing().getOpposite())
		{
			case UP:
			case DOWN:
				return Shapes.box(wMin, 0, wMin, wMax, 1, wMax);
			case SOUTH:
			case NORTH:
				return Shapes.box(wMin, wMin, 0, wMax, wMax, 1);
			case EAST:
			case WEST:
				return Shapes.box(0, wMin, wMin, 1, wMax, wMax);
		}
		return Shapes.block();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		if("glass".equals(group))
			return MinecraftForgeClient.getRenderLayer()==RenderType.translucent();
		return MinecraftForgeClient.getRenderLayer()==RenderType.cutout();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Vector4f getRenderColor(BlockState object, String group, Vector4f original)
	{
		if("colour_in".equals(group))
		{
			float[] rgb = redstoneChannel.getTextureDiffuseColors();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		else if("colour_out".equals(group))
		{
			float[] rgb = redstoneChannelSending.getTextureDiffuseColors();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		return original;
	}

	@Override
	public String getCacheKey(BlockState object)
	{
		return redstoneChannel+";"+redstoneChannelSending;
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getItemInHand(InteractionHand.MAIN_HAND)))
			return null;
		return new Component[]{
				new TranslatableComponent(Lib.DESC_INFO+"redstoneChannel.rec", I18n.get("item.minecraft.firework_star."+redstoneChannel.getName())),
				new TranslatableComponent(Lib.DESC_INFO+"redstoneChannel.send", I18n.get("item.minecraft.firework_star."+redstoneChannelSending.getName()))
		};
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}
}