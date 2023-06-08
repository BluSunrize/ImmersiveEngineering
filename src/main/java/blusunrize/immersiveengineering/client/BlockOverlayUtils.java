/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.mixin.accessors.client.WorldRendererAccess;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Arrays;
import java.util.Collection;

public class BlockOverlayUtils
{
	/* ----------- OVERLAY TEXT ----------- */

	public static void drawBlockOverlayText(PoseStack transform, Component[] text, int scaledWidth, int scaledHeight)
	{
		if(text!=null&&text.length > 0)
			drawBlockOverlayText(transform, Arrays.asList(text), scaledWidth, scaledHeight);
	}

	public static void drawBlockOverlayText(PoseStack transform, Iterable<Component> text, int scaledWidth, int scaledHeight)
	{
		int i = 0;
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		for(Component s : text)
			if(s!=null)
				ClientUtils.font().drawInBatch(
						Language.getInstance().getVisualOrder(s),
						scaledWidth/2+8, scaledHeight/2+8+(i++)*ClientUtils.font().lineHeight, 0xffffffff, true,
						transform.last().pose(), buffer, DisplayMode.NORMAL, 0, 0xf000f0
				);
		buffer.endBatch();
	}

	/* ----------- ARROWS ----------- */

	private final static Vec2[] quarterRotationArrowCoords = {
			new Vec2(.375F, 0),
			new Vec2(.5F, -.125F),
			new Vec2(.4375F, -.125F),
			new Vec2(.4375F, -.25F),
			new Vec2(.25F, -.4375F),
			new Vec2(0, -.4375F),
			new Vec2(0, -.3125F),
			new Vec2(.1875F, -.3125F),
			new Vec2(.3125F, -.1875F),
			new Vec2(.3125F, -.125F),
			new Vec2(.25F, -.125F)
	};
	private final static Vec2[] quarterRotationArrowQuads = {
			quarterRotationArrowCoords[5],
			quarterRotationArrowCoords[6],
			quarterRotationArrowCoords[4],
			quarterRotationArrowCoords[7],
			quarterRotationArrowCoords[3],
			quarterRotationArrowCoords[8],
			quarterRotationArrowCoords[2],
			quarterRotationArrowCoords[9],
			quarterRotationArrowCoords[1],
			quarterRotationArrowCoords[10],
			quarterRotationArrowCoords[0],
			quarterRotationArrowCoords[0]
	};

	private final static Vec2[] halfRotationArrowCoords = {
			new Vec2(.375F, 0),
			new Vec2(.5F, -.125F),
			new Vec2(.4375F, -.125F),
			new Vec2(.4375F, -.25F),
			new Vec2(.25F, -.4375F),
			new Vec2(-.25F, -.4375F),
			new Vec2(-.4375F, -.25F),
			new Vec2(-.4375F, -.0625F),
			new Vec2(-.3125F, -.0625F),
			new Vec2(-.3125F, -.1875F),
			new Vec2(-.1875F, -.3125F),
			new Vec2(.1875F, -.3125F),
			new Vec2(.3125F, -.1875F),
			new Vec2(.3125F, -.125F),
			new Vec2(.25F, -.125F)
	};
	private final static Vec2[] halfRotationArrowQuads = {
			halfRotationArrowCoords[7],
			halfRotationArrowCoords[8],
			halfRotationArrowCoords[6],
			halfRotationArrowCoords[9],
			halfRotationArrowCoords[5],
			halfRotationArrowCoords[10],
			halfRotationArrowCoords[4],
			halfRotationArrowCoords[11],
			halfRotationArrowCoords[3],
			halfRotationArrowCoords[12],
			halfRotationArrowCoords[2],
			halfRotationArrowCoords[13],
			halfRotationArrowCoords[1],
			halfRotationArrowCoords[14],
			halfRotationArrowCoords[0],
			halfRotationArrowCoords[0]
	};

	/**
	 * Draw spinning arrows, used by the turntable.
	 */
	public static void drawCircularRotationArrows(MultiBufferSource buffer, PoseStack transform, float rotation, boolean flip, boolean halfCircle)
	{
		transform.pushPose();
		transform.translate(0, 0.502, 0);
		Vec2[] rotationArrowCoords;
		Vec2[] rotationArrowQuads;
		if(halfCircle)
		{
			rotationArrowCoords = halfRotationArrowCoords;
			rotationArrowQuads = halfRotationArrowQuads;
		}
		else
		{
			rotationArrowCoords = quarterRotationArrowCoords;
			rotationArrowQuads = quarterRotationArrowQuads;
		}

		int[] vertexOrder;
		if(flip)
		{
			transform.mulPose(new Quaternionf().rotateXYZ(0, -rotation, 0));
			transform.scale(1, 1, -1);
			vertexOrder = new int[]{2, 3, 1, 0};
		}
		else
		{
			transform.mulPose(new Quaternionf().rotateXYZ(0, rotation, 0));
			vertexOrder = new int[]{0, 1, 3, 2};
		}
		transform.pushPose();
		VertexConsumer builder = buffer.getBuffer(IERenderTypes.LINES);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.last().pose();
			for(int i = 0; i < rotationArrowCoords.length; i++)
			{
				Vec2 here = rotationArrowCoords[i];
				Vec2 next = rotationArrowCoords[(i+1)%rotationArrowCoords.length];
				Vec2 diff = new Vec2(next.x-here.x, next.y-here.y).normalized();
				for(Vec2 v : ImmutableList.of(here, next))
					builder.vertex(mat, v.x, 0, v.y)
							.color(0, 0, 0, 0.4F)
							.normal(transform.last().normal(), diff.x, 0, diff.y)
							.endVertex();
			}
			transform.mulPose(new Quaternionf().rotateXYZ(0, Mth.PI, 0));
		}
		transform.popPose();
		transform.pushPose();
		builder = buffer.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.last().pose();
			for(int i = 0; i+3 < rotationArrowQuads.length; i += 2)
				for(int offset : vertexOrder)
				{
					Vec2 p = rotationArrowQuads[i+offset];
					builder.vertex(mat, p.x, 0, p.y)
							.color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F)
							.endVertex();
				}
			transform.mulPose(new Quaternionf().rotateXYZ(0, Mth.PI, 0));
		}
		transform.popPose();
		transform.popPose();
	}

	private final static float[][] arrowCoords = {{0, .375f}, {.3125f, .0625f}, {.125f, .0625f}, {.125f, -.375f}, {-.125f, -.375f}, {-.125f, .0625f}, {-.3125f, .0625f}};

	/**
	 * Draw an arrow on a face, pointed at a specific direction, used by conveyors for placement.
	 */
	public static void drawBlockOverlayArrow(Pose transform, MultiBufferSource buffers, Vec3 directionVec,
											 Direction side, AABB targetedBB)
	{
		Vec3[] translatedPositions = new Vec3[arrowCoords.length];
		Matrix4 mat = new Matrix4();
		Vec3 defaultDir = side.getAxis()==Axis.Y?new Vec3(0, 0, 1): new Vec3(0, 1, 0);
		directionVec = directionVec.normalize();
		double angle = Math.acos(defaultDir.dot(directionVec));
		Vec3 axis = defaultDir.cross(directionVec);
		mat.rotate(angle, axis.x, axis.y, axis.z);
		if(side.getAxis()==Axis.Z)
			mat.rotate(Math.PI/2, 1, 0, 0).rotate(Math.PI, 0, 1, 0);
		else if(side.getAxis()==Axis.X)
			mat.rotate(Math.PI/2, 0, 0, 1).rotate(Math.PI/2, 0, 1, 0);
		for(int i = 0; i < translatedPositions.length; i++)
		{
			Vec3 vec = mat.apply(new Vec3(arrowCoords[i][0], 0, arrowCoords[i][1])).add(.5, .5, .5);
			if(targetedBB!=null)
				vec = new Vec3(side==Direction.WEST?targetedBB.minX-.002: side==Direction.EAST?targetedBB.maxX+.002: vec.x, side==Direction.DOWN?targetedBB.minY-.002: side==Direction.UP?targetedBB.maxY+.002: vec.y, side==Direction.NORTH?targetedBB.minZ-.002: side==Direction.SOUTH?targetedBB.maxZ+.002: vec.z);
			translatedPositions[i] = vec;
		}

		VertexConsumer triBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_TRIANGLES);
		Vec3 center = translatedPositions[0];
		for(int i = 2; i < translatedPositions.length; i++)
		{
			Vec3 point = translatedPositions[i];
			Vec3 prevPoint = translatedPositions[i-1];
			for(Vec3 p : new Vec3[]{center, prevPoint, point})
				triBuilder.vertex(transform.pose(), (float)p.x, (float)p.y, (float)p.z)
						.color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F)
						.endVertex();
		}
		VertexConsumer lineBuilder = buffers.getBuffer(IERenderTypes.LINES);
		for(int i = 0; i < translatedPositions.length; i++)
		{
			Vec3 point = translatedPositions[i];
			Vec3 next = translatedPositions[(i+1)%translatedPositions.length];
			Vec3 diff = next.subtract(point).normalize();
			for(Vec3 p : ImmutableList.of(point, next))
				lineBuilder.vertex(transform.pose(), (float)p.x, (float)p.y, (float)p.z)
						.color(0, 0, 0, 0.4F)
						.normal(transform.normal(), (float)diff.x, (float)diff.y, (float)diff.z)
						.endVertex();
		}
	}

	/**
	 * Draw additional block breaking texture at targeted positions
	 */
	public static void drawAdditionalBlockbreak(RenderHighlightEvent.Block ev, Player player, Collection<BlockPos> blocks)
	{
		Vec3 renderView = ev.getCamera().getPosition();
		for(BlockPos pos : blocks)
			((WorldRendererAccess)ev.getLevelRenderer()).callRenderHitOutline(
					ev.getPoseStack(),
					ev.getMultiBufferSource().getBuffer(RenderType.lines()),
					player,
					renderView.x, renderView.y, renderView.z,
					pos,
					ClientUtils.mc().level.getBlockState(pos)
			);

		PoseStack transform = ev.getPoseStack();
		transform.pushPose();
		transform.translate(-renderView.x, -renderView.y, -renderView.z);
		MultiPlayerGameMode controllerMP = ClientUtils.mc().gameMode;
		if(controllerMP.isDestroying())
			RenderUtils.drawBlockDamageTexture(transform, ev.getMultiBufferSource(), player.level(), blocks);
		transform.popPose();
	}

	/* ----------- MAPS ----------- */

	/**
	 * Draw overlay for a map in a frame, based on where the player's cursor is on the map
	 */
	public static void renderOreveinMapOverlays(GuiGraphics graphics, ItemFrame frameEntity, HitResult rayTraceResult, int scaledWidth, int scaledHeight)
	{
		if(frameEntity==null)
			return;
		ItemStack frameItem = frameEntity.getItem();
		if(frameItem.getItem()==Items.FILLED_MAP&&ItemNBTHelper.hasKey(frameItem, "Decorations", 9))
		{
			Level world = frameEntity.getCommandSenderWorld();
			MapItemSavedData mapData = MapItem.getSavedData(frameItem, world);
			if(mapData!=null)
			{
				Font font = ClientUtils.font();
				int mapScale = 1<<mapData.scale;
				float mapRotation = (frameEntity.getRotation()%4)*1.5708f;

				// Player hit vector, relative to frame block pos
				Vec3 hitVec = rayTraceResult.getLocation().subtract(Vec3.atLowerCornerOf(frameEntity.getPos()));
				Direction frameDir = frameEntity.getDirection();
				double cursorH = 0;
				double cursorV = 0;
				// Get a 0-1 cursor coordinate; this could be ternary operator, but switchcase is easier to read
				switch(frameDir)
				{
					case DOWN ->
					{
						cursorH = hitVec.x;
						cursorV = 1-hitVec.z;
					}
					case UP ->
					{
						cursorH = hitVec.x;
						cursorV = hitVec.z;
					}
					case NORTH ->
					{
						cursorH = 1-hitVec.x;
						cursorV = 1-hitVec.y;
					}
					case SOUTH ->
					{
						cursorH = hitVec.x;
						cursorV = 1-hitVec.y;
					}
					case WEST ->
					{
						cursorH = hitVec.z;
						cursorV = 1-hitVec.y;
					}
					case EAST ->
					{
						cursorH = 1-hitVec.z;
						cursorV = 1-hitVec.y;
					}
				}
				// Multiply it to the number scale vanilla maps use
				cursorH *= 128;
				cursorV *= 128;

				ListTag minerals = null;
				double lastDist = Double.MAX_VALUE;
				ListTag nbttaglist = frameItem.getTag().getList("Decorations", 10);
				for(Tag inbt : nbttaglist)
				{
					CompoundTag tagCompound = (CompoundTag)inbt;
					String id = tagCompound.getString("id");
					if(id.startsWith("ie:coresample_")&&tagCompound.contains("minerals"))
					{
						double sampleX = tagCompound.getDouble("x");
						double sampleZ = tagCompound.getDouble("z");
						// Map coordinates require some pretty funky maths. I tried to simplify this,
						// and ran into issues that made highlighting fail on certain markers.
						// This implementation works, so I just won't touch it again.
						float f = (float)(sampleX-(double)mapData.centerX)/(float)mapScale;
						float f1 = (float)(sampleZ-(double)mapData.centerZ)/(float)mapScale;
						byte b0 = (byte)((int)((double)(f*2.0F)+0.5D));
						byte b1 = (byte)((int)((double)(f1*2.0F)+0.5D));
						// Make it a vector, rotate it around the map center
						Vec3 mapPos = new Vec3(0, b1, b0);
						mapPos = mapPos.xRot(mapRotation);
						// Turn it into a 0.0 to 128.0 offset
						double offsetH = (mapPos.z/2.0F+64.0F);
						double offsetV = (mapPos.y/2.0F+64.0F);
						// Get cursor distance
						double dH = cursorH-offsetH;
						double dV = cursorV-offsetV;
						double dist = dH*dH+dV*dV;
						if(dist < 10&&dist < lastDist)
						{
							lastDist = dist;
							minerals = tagCompound.getList("minerals", Tag.TAG_STRING);
						}
					}
				}
				if(minerals!=null)
					for(int i = 0; i < minerals.size(); i++)
					{
						MineralMix mix = MineralMix.RECIPES.getById(Minecraft.getInstance().level, new ResourceLocation(minerals.getString(i)));
						if(mix!=null)
							graphics.drawString(font, I18n.get(mix.getTranslationKey()), scaledWidth/2+8, scaledHeight/2+8+i*font.lineHeight, 0xffffff, true);
					}
			}
		}
	}
}
