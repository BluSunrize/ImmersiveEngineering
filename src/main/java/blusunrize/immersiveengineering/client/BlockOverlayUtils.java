package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.mixin.accessors.client.WorldRendererAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.gui.Font;
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
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Collection;

public class BlockOverlayUtils
{
	/* ----------- OVERLAY TEXT ----------- */

	public static void drawBlockOverlayText(PoseStack transform, Component[] text, int scaledWidth, int scaledHeight)
	{
		if(text!=null&&text.length > 0)
		{
			int i = 0;
			MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			for(Component s : text)
				if(s!=null)
					ClientUtils.font().drawInBatch(
							Language.getInstance().getVisualOrder(s),
							scaledWidth/2+8, scaledHeight/2+8+(i++)*ClientUtils.font().lineHeight, 0xffffffff, true,
							transform.last().pose(), buffer, false, 0, 0xf000f0
					);
			buffer.endBatch();
		}
	}

	/* ----------- ARROWS ----------- */

	private final static float[][] quarterRotationArrowCoords = {
			{.375F, 0},
			{.5F, -.125F},
			{.4375F, -.125F},
			{.4375F, -.25F},
			{.25F, -.4375F},
			{0, -.4375F},
			{0, -.3125F},
			{.1875F, -.3125F},
			{.3125F, -.1875F},
			{.3125F, -.125F},
			{.25F, -.125F}
	};
	private final static float[][] quarterRotationArrowQuads = {
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

	private final static float[][] halfRotationArrowCoords = {
			{.375F, 0},
			{.5F, -.125F},
			{.4375F, -.125F},
			{.4375F, -.25F},
			{.25F, -.4375F},
			{-.25F, -.4375F},
			{-.4375F, -.25F},
			{-.4375F, -.0625F},
			{-.3125F, -.0625F},
			{-.3125F, -.1875F},
			{-.1875F, -.3125F},
			{.1875F, -.3125F},
			{.3125F, -.1875F},
			{.3125F, -.125F},
			{.25F, -.125F}
	};
	private final static float[][] halfRotationArrowQuads = {
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
		float[][] rotationArrowCoords;
		float[][] rotationArrowQuads;
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
			transform.mulPose(new Quaternion(0, -rotation, 0, false));
			transform.scale(1, 1, -1);
			vertexOrder = new int[]{2, 3, 1, 0};
		}
		else
		{
			transform.mulPose(new Quaternion(0, rotation, 0, false));
			vertexOrder = new int[]{0, 1, 3, 2};
		}
		transform.pushPose();
		VertexConsumer builder = buffer.getBuffer(IERenderTypes.LINES);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.last().pose();
			for(int i = 0; i <= rotationArrowCoords.length; i++)
			{
				float[] p = rotationArrowCoords[i%rotationArrowCoords.length];
				if(i > 0)
					builder.vertex(mat, p[0], 0, p[1]).color(0, 0, 0, 0.4F).endVertex();
				if(i!=rotationArrowCoords.length)
					builder.vertex(mat, p[0], 0, p[1]).color(0, 0, 0, 0.4F).endVertex();
			}
			transform.mulPose(new Quaternion(0, 180, 0, true));
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
					float[] p = rotationArrowQuads[i+offset];
					builder.vertex(mat, p[0], 0, p[1]).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
				}
			transform.mulPose(new Quaternion(0, 180, 0, true));
		}
		transform.popPose();
		transform.popPose();
	}

	private final static float[][] arrowCoords = {{0, .375f}, {.3125f, .0625f}, {.125f, .0625f}, {.125f, -.375f}, {-.125f, -.375f}, {-.125f, .0625f}, {-.3125f, .0625f}};

	/**
	 * Draw an arrow on a face, pointed at a specific direction, used by conveyors for placement.
	 */
	public static void drawBlockOverlayArrow(Matrix4f transform, MultiBufferSource buffers, Vec3 directionVec,
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
				triBuilder.vertex(transform, (float)p.x, (float)p.y, (float)p.z)
						.color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F)
						.endVertex();
		}
		VertexConsumer lineBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_LINES);
		for(int i = 0; i <= translatedPositions.length; i++)
		{
			Vec3 point = translatedPositions[i%translatedPositions.length];
			int max = i==0||i==translatedPositions.length?1: 2;
			for(int j = 0; j < max; ++j)
				lineBuilder.vertex(transform, (float)point.x, (float)point.y, (float)point.z)
						.color(0, 0, 0, 0.4F)
						.endVertex();
		}
	}

	/**
	 * Draw additional block breaking texture at targeted positions
	 */
	public static void drawAdditionalBlockbreak(DrawSelectionEvent.HighlightBlock ev, Player player, Collection<BlockPos> blocks)
	{
		Vec3 renderView = ev.getInfo().getPosition();
		for(BlockPos pos : blocks)
			((WorldRendererAccess)ev.getContext()).callRenderHitOutline(
					ev.getMatrix(),
					ev.getBuffers().getBuffer(RenderType.lines()),
					player,
					renderView.x, renderView.y, renderView.z,
					pos,
					ClientUtils.mc().level.getBlockState(pos)
			);

		PoseStack transform = ev.getMatrix();
		transform.pushPose();
		transform.translate(-renderView.x, -renderView.y, -renderView.z);
		MultiPlayerGameMode controllerMP = ClientUtils.mc().gameMode;
		if(controllerMP.isDestroying())
			RenderUtils.drawBlockDamageTexture(transform, ev.getBuffers(), player.level, blocks);
		transform.popPose();
	}

	/* ----------- MAPS ----------- */

	/**
	 * Draw overlay for a map in a frame, based on where the player's cursor is on the map
	 */
	public static void renderOreveinMapOverlays(PoseStack transform, ItemFrame frameEntity, HitResult rayTraceResult, int scaledWidth, int scaledHeight)
	{
		if(frameEntity!=null)
		{
			ItemStack frameItem = frameEntity.getItem();
			if(frameItem.getItem()==Items.FILLED_MAP&&ItemNBTHelper.hasKey(frameItem, "Decorations", 9))
			{
				Level world = frameEntity.getCommandSenderWorld();
				MapItemSavedData mapData = MapItem.getSavedData(frameItem, world);
				if(mapData!=null)
				{
					Font font = ClientUtils.font();
					// Map center is usually only calculated serverside, so we gotta do it manually
					mapData.setOrigin(world.getLevelData().getXSpawn(), world.getLevelData().getZSpawn(), mapData.scale);
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
						case DOWN:
							cursorH = hitVec.x;
							cursorV = 1-hitVec.z;
							break;
						case UP:
							cursorH = hitVec.x;
							cursorV = hitVec.z;
							break;
						case NORTH:
							cursorH = 1-hitVec.x;
							cursorV = 1-hitVec.y;
							break;
						case SOUTH:
							cursorH = hitVec.x;
							cursorV = 1-hitVec.y;
							break;
						case WEST:
							cursorH = hitVec.z;
							cursorV = 1-hitVec.y;
							break;
						case EAST:
							cursorH = 1-hitVec.z;
							cursorV = 1-hitVec.y;
							break;
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
							float f = (float)(sampleX-(double)mapData.x)/(float)mapScale;
							float f1 = (float)(sampleZ-(double)mapData.z)/(float)mapScale;
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
								minerals = tagCompound.getList("minerals", NBT.TAG_STRING);
							}
						}
					}
					if(minerals!=null)
						for(int i = 0; i < minerals.size(); i++)
						{
							MineralMix mix = MineralMix.mineralList.get(new ResourceLocation(minerals.getString(i)));
							if(mix!=null)
								font.drawShadow(transform, I18n.get(mix.getTranslationKey()), scaledWidth/2+8, scaledHeight/2+8+i*font.lineHeight, 0xffffff);
						}
				}
			}
		}
	}
}
