package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.mixin.accessors.client.WorldRendererAccess;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Collection;

public class BlockOverlayUtils
{
	/* ----------- OVERLAY TEXT ----------- */

	public static void drawBlockOverlayText(MatrixStack transform, ITextComponent[] text, int scaledWidth, int scaledHeight)
	{
		if(text!=null&&text.length > 0)
		{
			int i = 0;
			IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
			for(ITextComponent s : text)
				if(s!=null)
					ClientUtils.font().func_238416_a_(
							LanguageMap.getInstance().func_241870_a(s),
							scaledWidth/2+8, scaledHeight/2+8+(i++)*ClientUtils.font().FONT_HEIGHT, 0xffffffff, true,
							transform.getLast().getMatrix(), buffer, false, 0, 0xf000f0
					);
			buffer.finish();
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
	public static void drawCircularRotationArrows(IRenderTypeBuffer buffer, MatrixStack transform, float rotation, boolean flip, boolean halfCircle)
	{
		transform.push();
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
			transform.rotate(new Quaternion(0, -rotation, 0, false));
			transform.scale(1, 1, -1);
			vertexOrder = new int[]{2, 3, 1, 0};
		}
		else
		{
			transform.rotate(new Quaternion(0, rotation, 0, false));
			vertexOrder = new int[]{0, 1, 3, 2};
		}
		transform.push();
		IVertexBuilder builder = buffer.getBuffer(IERenderTypes.LINES);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.getLast().getMatrix();
			for(int i = 0; i <= rotationArrowCoords.length; i++)
			{
				float[] p = rotationArrowCoords[i%rotationArrowCoords.length];
				if(i > 0)
					builder.pos(mat, p[0], 0, p[1]).color(0, 0, 0, 0.4F).endVertex();
				if(i!=rotationArrowCoords.length)
					builder.pos(mat, p[0], 0, p[1]).color(0, 0, 0, 0.4F).endVertex();
			}
			transform.rotate(new Quaternion(0, 180, 0, true));
		}
		transform.pop();
		transform.push();
		builder = buffer.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
		for(int arrowId = 0; arrowId < 2; ++arrowId)
		{
			Matrix4f mat = transform.getLast().getMatrix();
			for(int i = 0; i+3 < rotationArrowQuads.length; i += 2)
				for(int offset : vertexOrder)
				{
					float[] p = rotationArrowQuads[i+offset];
					builder.pos(mat, p[0], 0, p[1]).color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F).endVertex();
				}
			transform.rotate(new Quaternion(0, 180, 0, true));
		}
		transform.pop();
		transform.pop();
	}

	private final static float[][] arrowCoords = {{0, .375f}, {.3125f, .0625f}, {.125f, .0625f}, {.125f, -.375f}, {-.125f, -.375f}, {-.125f, .0625f}, {-.3125f, .0625f}};

	/**
	 * Draw an arrow on a face, pointed at a specific direction, used by conveyors for placement.
	 */
	public static void drawBlockOverlayArrow(Matrix4f transform, IRenderTypeBuffer buffers, Vector3d directionVec,
											 Direction side, AxisAlignedBB targetedBB)
	{
		Vector3d[] translatedPositions = new Vector3d[arrowCoords.length];
		Matrix4 mat = new Matrix4();
		Vector3d defaultDir = side.getAxis()==Axis.Y?new Vector3d(0, 0, 1): new Vector3d(0, 1, 0);
		directionVec = directionVec.normalize();
		double angle = Math.acos(defaultDir.dotProduct(directionVec));
		Vector3d axis = defaultDir.crossProduct(directionVec);
		mat.rotate(angle, axis.x, axis.y, axis.z);
		if(side.getAxis()==Axis.Z)
			mat.rotate(Math.PI/2, 1, 0, 0).rotate(Math.PI, 0, 1, 0);
		else if(side.getAxis()==Axis.X)
			mat.rotate(Math.PI/2, 0, 0, 1).rotate(Math.PI/2, 0, 1, 0);
		for(int i = 0; i < translatedPositions.length; i++)
		{
			Vector3d vec = mat.apply(new Vector3d(arrowCoords[i][0], 0, arrowCoords[i][1])).add(.5, .5, .5);
			if(targetedBB!=null)
				vec = new Vector3d(side==Direction.WEST?targetedBB.minX-.002: side==Direction.EAST?targetedBB.maxX+.002: vec.x, side==Direction.DOWN?targetedBB.minY-.002: side==Direction.UP?targetedBB.maxY+.002: vec.y, side==Direction.NORTH?targetedBB.minZ-.002: side==Direction.SOUTH?targetedBB.maxZ+.002: vec.z);
			translatedPositions[i] = vec;
		}

		IVertexBuilder triBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_TRIANGLES);
		Vector3d center = translatedPositions[0];
		for(int i = 2; i < translatedPositions.length; i++)
		{
			Vector3d point = translatedPositions[i];
			Vector3d prevPoint = translatedPositions[i-1];
			for(Vector3d p : new Vector3d[]{center, prevPoint, point})
				triBuilder.pos(transform, (float)p.x, (float)p.y, (float)p.z)
						.color(Lib.COLOUR_F_ImmersiveOrange[0], Lib.COLOUR_F_ImmersiveOrange[1], Lib.COLOUR_F_ImmersiveOrange[2], 0.4F)
						.endVertex();
		}
		IVertexBuilder lineBuilder = buffers.getBuffer(IERenderTypes.TRANSLUCENT_LINES);
		for(int i = 0; i <= translatedPositions.length; i++)
		{
			Vector3d point = translatedPositions[i%translatedPositions.length];
			int max = i==0||i==translatedPositions.length?1: 2;
			for(int j = 0; j < max; ++j)
				lineBuilder.pos(transform, (float)point.x, (float)point.y, (float)point.z)
						.color(0, 0, 0, 0.4F)
						.endVertex();
		}
	}

	/**
	 * Draw additional block breaking texture at targeted positions
	 */
	public static void drawAdditionalBlockbreak(DrawHighlightEvent ev, PlayerEntity player, float partialTicks, Collection<BlockPos> blocks)
	{
		Vector3d renderView = ev.getInfo().getProjectedView();
		for(BlockPos pos : blocks)
			((WorldRendererAccess)ev.getContext()).callDrawSelectionBox(
					ev.getMatrix(),
					ev.getBuffers().getBuffer(RenderType.getLines()),
					player,
					renderView.x, renderView.y, renderView.z,
					pos,
					ClientUtils.mc().world.getBlockState(pos)
			);

		MatrixStack transform = ev.getMatrix();
		transform.push();
		transform.translate(-renderView.x, -renderView.y, -renderView.z);
		PlayerController controllerMP = ClientUtils.mc().playerController;
		if(controllerMP.getIsHittingBlock())
			ClientUtils.drawBlockDamageTexture(transform, ev.getBuffers(), player, partialTicks, player.world, blocks);
		transform.pop();
	}

	/* ----------- MAPS ----------- */

	/**
	 * Draw overlay for a map in a frame, based on where the player's cursor is on the map
	 */
	public static void renderOreveinMapOverlays(MatrixStack transform, ItemFrameEntity frameEntity, RayTraceResult rayTraceResult, int scaledWidth, int scaledHeight)
	{
		if(frameEntity!=null)
		{
			ItemStack frameItem = frameEntity.getDisplayedItem();
			if(frameItem.getItem()==Items.FILLED_MAP&&ItemNBTHelper.hasKey(frameItem, "Decorations", 9))
			{
				World world = frameEntity.getEntityWorld();
				MapData mapData = FilledMapItem.getMapData(frameItem, world);
				if(mapData!=null)
				{
					FontRenderer font = ClientUtils.font();
					// Map center is usually only calculated serverside, so we gotta do it manually
					mapData.calculateMapCenter(world.getWorldInfo().getSpawnX(), world.getWorldInfo().getSpawnZ(), mapData.scale);
					int mapScale = 1<<mapData.scale;
					float mapRotation = (frameEntity.getRotation()%4)*1.5708f;

					// Player hit vector, relative to frame block pos
					Vector3d hitVec = rayTraceResult.getHitVec().subtract(Vector3d.copy(frameEntity.getHangingPosition()));
					Direction frameDir = frameEntity.getHorizontalFacing();
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

					ListNBT minerals = null;
					double lastDist = Double.MAX_VALUE;
					ListNBT nbttaglist = frameItem.getTag().getList("Decorations", 10);
					for(INBT inbt : nbttaglist)
					{
						CompoundNBT tagCompound = (CompoundNBT)inbt;
						String id = tagCompound.getString("id");
						if(id.startsWith("ie:coresample_")&&tagCompound.contains("minerals"))
						{
							double sampleX = tagCompound.getDouble("x");
							double sampleZ = tagCompound.getDouble("z");
							// Map coordinates require some pretty funky maths. I tried to simplify this,
							// and ran into issues that made highlighting fail on certain markers.
							// This implementation works, so I just won't touch it again.
							float f = (float)(sampleX-(double)mapData.xCenter)/(float)mapScale;
							float f1 = (float)(sampleZ-(double)mapData.zCenter)/(float)mapScale;
							byte b0 = (byte)((int)((double)(f*2.0F)+0.5D));
							byte b1 = (byte)((int)((double)(f1*2.0F)+0.5D));
							// Make it a vector, rotate it around the map center
							Vector3d mapPos = new Vector3d(0, b1, b0);
							mapPos = mapPos.rotatePitch(mapRotation);
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
								font.drawStringWithShadow(transform, I18n.format(mix.getTranslationKey()), scaledWidth/2+8, scaledHeight/2+8+i*font.FONT_HEIGHT, 0xffffff);
						}
				}
			}
		}
	}
}
