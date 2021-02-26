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
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
		if(!world.isRemote&&world.getGameTime()%8!=((getPos().getX()^getPos().getZ())&8))
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
		BlockPos pos = this.getPos().offset(getFacing());
		BlockState state = world.getBlockState(pos);
		if(state.hasComparatorInputOverride())
			return state.getComparatorInputOverride(world, pos);
		else if(state.isNormalCube(world, pos))
		{
			pos = pos.offset(getFacing());
			state = world.getBlockState(pos);
			if(state.hasComparatorInputOverride())
				return state.getComparatorInputOverride(world, pos);
			else if(state.getMaterial()==Material.AIR)
			{
				ItemFrameEntity entityitemframe = this.findItemFrame(world, getFacing(), pos);
				if(entityitemframe!=null)
					return entityitemframe.getAnalogOutput();
			}
		}
		return 0;
	}

	private ItemFrameEntity findItemFrame(World world, final Direction facing, BlockPos pos)
	{
		List<ItemFrameEntity> list = world.getEntitiesWithinAABB(ItemFrameEntity.class, new AxisAlignedBB(pos), entity -> entity!=null&&entity.getHorizontalFacing()==facing);
		return list.size()==1?list.get(0): null;
	}

	@Override
	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		signals[redstoneChannelSending.ordinal()] = (byte)Math.max(lastOutput, signals[redstoneChannelSending.ordinal()]);
		rsDirty = false;
	}

	@Override
	public ActionResultType screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vector3d hitVec)
	{
		ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneProbe, this);
		return ActionResultType.SUCCESS;
	}

	@Override
	public void receiveMessageFromClient(CompoundNBT message)
	{
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getInt("redstoneChannel"));
		if(message.contains("redstoneChannelSending"))
			redstoneChannelSending = DyeColor.byId(message.getInt("redstoneChannelSending"));
		updateAfterConfigure();
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("redstoneChannelSending", redstoneChannelSending.getId());
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		redstoneChannelSending = DyeColor.byId(nbt.getInt("redstoneChannelSending"));
	}

	@Override
	public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vector3d(.5+side.getXOffset()*(.375-conRadius), .5+side.getYOffset()*(.375-conRadius), .5+side.getZOffset()*(.375-conRadius));
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		float wMin = .28125f;
		float wMax = .71875f;
		switch(getFacing().getOpposite())
		{
			case UP:
			case DOWN:
				return VoxelShapes.create(wMin, 0, wMin, wMax, 1, wMax);
			case SOUTH:
			case NORTH:
				return VoxelShapes.create(wMin, wMin, 0, wMax, wMax, 1);
			case EAST:
			case WEST:
				return VoxelShapes.create(0, wMin, wMin, 1, wMax, wMax);
		}
		return VoxelShapes.fullCube();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		if("glass".equals(group))
			return MinecraftForgeClient.getRenderLayer()==RenderType.getTranslucent();
		return MinecraftForgeClient.getRenderLayer()==RenderType.getCutout();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Vector4f getRenderColor(BlockState object, String group, Vector4f original)
	{
		if("colour_in".equals(group))
		{
			float[] rgb = redstoneChannel.getColorComponentValues();
			return new Vector4f(rgb[0], rgb[1], rgb[2], 1);
		}
		else if("colour_out".equals(group))
		{
			float[] rgb = redstoneChannelSending.getColorComponentValues();
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
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(!Utils.isScrewdriver(player.getHeldItem(Hand.MAIN_HAND)))
			return null;
		return new ITextComponent[]{
				new TranslationTextComponent(Lib.DESC_INFO+"redstoneChannel.rec", I18n.format("item.minecraft.firework_star."+redstoneChannel.getTranslationKey())),
				new TranslationTextComponent(Lib.DESC_INFO+"redstoneChannel.send", I18n.format("item.minecraft.firework_star."+redstoneChannelSending.getTranslationKey()))
		};
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}
}